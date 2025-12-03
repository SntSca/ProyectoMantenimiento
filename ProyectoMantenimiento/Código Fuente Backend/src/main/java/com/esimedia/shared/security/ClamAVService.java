package com.esimedia.shared.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.Socket;

/**
 * Servicio de integración con ClamAV para análisis de virus en archivos.
 * Utiliza el protocolo INSTREAM de ClamAV para escanear archivos antes de guardarlos.
 */
@Service
public class ClamAVService {

    private static final Logger logger = LoggerFactory.getLogger(ClamAVService.class);
    
    private static final byte[] INSTREAM_CMD = "zINSTREAM\0".getBytes();
    private static final byte[] PING_CMD = "zPING\0".getBytes();
    private static final int CHUNK_SIZE = 4096;
    private static final String VIRUS_DETECTED = "FOUND";
    private static final String SCAN_OK = "OK";

    @Value("${antivirus.enabled:false}")
    private boolean antivirusEnabled;

    @Value("${antivirus.clamav.host:localhost}")
    private String clamavHost;

    @Value("${antivirus.clamav.port:3310}")
    private int clamavPort;

    @Value("${antivirus.clamav.timeout:30}")
    private int clamavTimeout;

    /**
     * Escanea un archivo usando ClamAV.
     * 
     * @param fileContent Contenido del archivo en bytes
     * @param filename    Nombre del archivo (solo para logging)
     * @return true si el archivo es seguro, false si se detectó un virus
     * @throws AntivirusException si hay un error de conexión con ClamAV o si el servicio está deshabilitado
     */
    public boolean scanFile(byte[] fileContent, String filename) throws AntivirusException {
        if (!antivirusEnabled) {
            logger.warn("Antivirus deshabilitado. El archivo {} no será escaneado.", filename);
            return true;
        }

        if (fileContent == null || fileContent.length == 0) {
            logger.warn("Archivo {} vacío. No se puede escanear.", filename);
            return true;
        }

        try {
            return performScan(fileContent, filename);
        } 
        catch (IOException e) {
            logger.error("Error de conexión con ClamAV al escanear {}: {}", filename, e.getMessage());
            throw new AntivirusException("Error conectando con el antivirus: " + e.getMessage(), e);
        } 
        catch (Exception e) {
            logger.error("Error inesperado escaneando archivo {} con ClamAV: {}", filename, e.getMessage());
            throw new AntivirusException("Error escaneando el archivo: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica la disponibilidad del servicio ClamAV.
     * 
     * @return true si ClamAV está disponible, false en caso contrario
     */
    public boolean isAvailable() {
        if (!antivirusEnabled) {
            return false;
        }

        try {
            return performPing();
        } 
        catch (Exception e) {
            logger.warn("ClamAV no disponible: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Realiza un PING a ClamAV para verificar su disponibilidad.
     * 
     * @return true si ClamAV responde correctamente
     * @throws IOException si hay error de conexión
     */
    private boolean performPing() throws IOException {
        try (Socket socket = createSocket()) {
            socket.getOutputStream().write(PING_CMD);
            socket.getOutputStream().flush();

            byte[] response = new byte[1024];
            int bytesRead = socket.getInputStream().read(response);
            
            if (bytesRead > 0) {
                String responseStr = new String(response, 0, bytesRead).trim();
                logger.debug("ClamAV PING response: {}", responseStr);
                return responseStr.contains("PONG");
            }
            return false;
        }
    }

    /**
     * Realiza el escaneo del archivo usando el protocolo INSTREAM.
     * 
     * @param fileContent Contenido del archivo
     * @param filename    Nombre del archivo
     * @return true si el archivo es seguro
     * @throws IOException si hay error de comunicación
     */
    private boolean performScan(byte[] fileContent, String filename) throws IOException {
        try (Socket socket = createSocket();
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Enviar comando INSTREAM
            dos.write(INSTREAM_CMD);
            dos.flush();
            logger.info("Comando INSTREAM enviado al servicio ClamAV para: {}", filename);

            // Enviar tamaño del archivo seguido del contenido en chunks
            sendFileInChunks(dos, fileContent);

            // Enviar 0x00000000 para indicar fin del stream
            dos.writeInt(0);
            dos.flush();
            logger.info("Fin del stream enviado para: {}", filename);

            // Leer la respuesta
            String response = reader.readLine();
            logger.debug("ClamAV response para {}: {}", filename, response);

            if (response == null) {
                logger.warn("No se recibió respuesta de ClamAV para: {}", filename);
                throw new IOException("No se recibió respuesta de ClamAV");
            }

            // Verificar si se detectó un virus
            if (response.contains(VIRUS_DETECTED)) {
                logger.warn("¡VIRUS DETECTADO en archivo {}! Respuesta: {}", filename, response);
                return false;
            }

            if (response.contains(SCAN_OK)) {
                logger.info("Archivo {} pasó el escaneo de ClamAV correctamente", filename);
                return true;
            }

            logger.warn("Respuesta inesperada de ClamAV para {}: {}", filename, response);
            return false;

        }
    }

    /**
     * Envía el contenido del archivo en chunks al servicio ClamAV.
     * 
     * @param dos         DataOutputStream para escribir
     * @param fileContent Contenido del archivo
     * @throws IOException si hay error escribiendo
     */
    private void sendFileInChunks(DataOutputStream dos, byte[] fileContent) throws IOException {
        int offset = 0;
        
        while (offset < fileContent.length) {
            int chunkLength = Math.min(CHUNK_SIZE, fileContent.length - offset);
            
            // Escribir longitud del chunk en big-endian (4 bytes)
            dos.writeInt(chunkLength);
            
            // Escribir datos del chunk
            dos.write(fileContent, offset, chunkLength);
            dos.flush();
            
            offset += chunkLength;
            logger.debug("Chunk de {} bytes enviado. Total: {}/{}", chunkLength, offset, fileContent.length);
        }
    }

    /**
     * Crea una conexión socket con ClamAV con timeout configurado.
     * 
     * @return Socket conectado a ClamAV
     * @throws IOException si hay error creando la conexión
     */
    private Socket createSocket() throws IOException {
        Socket socket = new Socket();
        boolean success = false;
        try {
            // connect with a timeout (milliseconds)
            socket.connect(new java.net.InetSocketAddress(clamavHost, clamavPort), clamavTimeout * 1000);
            socket.setSoTimeout(clamavTimeout * 1000);
            logger.debug("Socket conectado a ClamAV en {}:{} con timeout {}s", clamavHost, clamavPort, clamavTimeout);
            success = true;
            return socket;
        }
        finally {
            // If connection or setup failed, ensure the socket is closed to avoid resource leak
            if (!success) {
                try {
                    socket.close();
                } 
                catch (IOException ex) {
                    logger.debug("Error cerrando socket tras fallo de conexión: {}", ex.getMessage());
                }
            }
        }
    }

    /**
     * Escanea múltiples archivos.
     * 
     * @param files Mapa de nombre de archivo a contenido
     * @return true si todos los archivos son seguros
     * @throws AntivirusException si algún archivo contiene un virus o hay error de conexión
     */
    public boolean scanFiles(java.util.Map<String, byte[]> files) throws AntivirusException {
        for (var entry : files.entrySet()) {
            if (!scanFile(entry.getValue(), entry.getKey())) {
                return false;
            }
        }
        return true;
    }
}
