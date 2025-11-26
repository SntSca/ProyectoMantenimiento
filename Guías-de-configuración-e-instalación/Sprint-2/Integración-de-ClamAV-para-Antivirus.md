# Integración de ClamAV para Antivirus

## Descripción General

Se ha implementado una integración completa de **ClamAV** (antivirus de código abierto) en el backend de EsiMedia para proteger contra archivos maliciosos. El sistema escanea:

- **Fotos de perfil**: En registros y actualizaciones de usuarios (UsuarioNormal, CreadorContenido, Administradores)
- **Contenido de audio**: Archivos MP3/WAV y miniaturas en subidas de audio
- **Miniaturas de video**: Imágenes asociadas a videos en subidas de video

---

## Arquitectura de la Solución

### 1. Servicio ClamAV (`ClamAVService.java`)

**Ubicación**: `src/main/java/com/esimedia/shared/security/ClamAVService.java`

**Características principales**:
- Conexión socket remota a ClamAV usando el protocolo INSTREAM
- Envío de archivos en chunks para soportar archivos grandes
- Manejo de timeouts y excepciones de conexión
- Verificación de disponibilidad del servicio (PING)
- Logging detallado de operaciones

**Métodos principales**:
```java
boolean scanFile(byte[] fileContent, String filename)  // Escanea un archivo individual
boolean scanFiles(Map<String, byte[]> files)           // Escanea múltiples archivos
boolean isAvailable()                                   // Verifica disponibilidad de ClamAV
```

**Protocolo INSTREAM**:
- El cliente envía: `zINSTREAM\0`
- Luego envía chunks de archivo con su tamaño (4 bytes, big-endian)
- Finaliza con `0x00000000` (0 bytes)
- ClamAV responde con: `stream: (OK|FOUND|ERROR MESSAGE)`

---

## Configuración

### Archivo `application.properties`

```properties
# Configuración Antivirus ClamAV
antivirus.enabled=${ANTIVIRUS_ENABLED:true}
antivirus.clamav.host=${CLAMAV_HOST:localhost}
antivirus.clamav.port=${CLAMAV_PORT:3310}
antivirus.clamav.timeout=${CLAMAV_TIMEOUT:30}
```

**Variables de entorno**:
- `ANTIVIRUS_ENABLED`: Habilita/deshabilita el escaneo (default: true)
- `CLAMAV_HOST`: Host del servidor ClamAV (default: localhost)
- `CLAMAV_PORT`: Puerto del servicio ClamAV (default: 3310)
- `CLAMAV_TIMEOUT`: Timeout en segundos (default: 30s)

**Ejemplo con Docker Compose**:
```yaml
clamav:
  image: clamav/clamav:latest
  ports:
    - "3310:3310"
  environment:
    - CLAMAV_HOST=0.0.0.0
    - CLAMAV_PORT=3310
```

---

## Integración en Servicios

### 1. AudioContentService

**Método**: `processAudioContent(String userId, ContentAudioUploadDTO audioDTO)`

**Flujo**:
1. Decodificar archivo de audio desde Base64
2. **Escanear archivo de audio** con ClamAV
3. Decodificar miniatura (si existe)
4. **Escanear miniatura** con ClamAV
5. Si ambos pasan, guardar el contenido
6. Si alguno falla, devolver error: `"El archivo contiene malware. No ha sido permitida la subida."`

**Respuestas**:
- ✅ **OK**: `"SUCCESS:Contenido de audio subido exitosamente"`
- ❌ **VIRUS en audio**: `"ERROR:El archivo de audio contiene malware..."`
- ❌ **VIRUS en miniatura**: `"ERROR:La miniatura contiene malware..."`

### 2. VideoContentService

**Método**: `createAndSaveVideoContent(String userId, ContentVideoUploadDTO videoDTO)`

**Flujo**:
1. Decodificar miniatura desde Base64 (si existe)
2. **Escanear miniatura** con ClamAV
3. Si pasa, guardar el contenido
4. Si falla, devolver error

**Nota**: El archivo de video se almacena como URL (no como binario), por lo que no requiere escaneo local.

### 3. ManagementService

**Métodos**:
- `updateBasicUserFields()` - Actualiza perfil de UsuarioNormal
- `updateCreatorFields()` - Actualiza perfil de CreadorContenido
- `updateAdminFields()` - Actualiza perfil de Administrador

**Flujo**:
1. Decodificar foto de perfil desde Base64 (si existe)
2. **Escanear foto de perfil** con ClamAV
3. Si pasa, guardar los cambios
4. Si falla, lanzar excepción: `"La foto de perfil contiene malware..."`

---

## Manejo de Errores

### Excepciones de ClamAV

```java
// Conexión rechazada
RuntimeException: "Error conectando con el antivirus: Connection refused"

// Timeout
SocketTimeoutException: "Timeout escaneando el archivo: filename.ext"

// Virus detectado
boolean scanFile(...) -> false

// ClamAV no disponible
isAvailable() -> false
```

### Respuestas HTTP

| Situación | Código | Respuesta |
|-----------|--------|-----------|
| Virus detectado | 400 | `"El archivo contiene malware. No ha sido permitida la subida."` |
| Error de conexión | 500 | `"Error conectando con el antivirus"` |
| Timeout | 500 | `"Timeout escaneando el archivo"` |
| Archivo seguro | 200 | `"Contenido subido exitosamente"` |

---

## Logging

El sistema genera logs detallados en 5 niveles:

**INFO** (Operaciones normales):
```
Iniciando escaneo de antivirus para archivo de audio
Archivo de audio pasó el escaneo de antivirus correctamente
Contenido de audio creado exitosamente con ID: 507f1f77bcf86cd799439011
```

**WARN** (Potenciales problemas):
```
¡VIRUS DETECTADO en archivo_nombre.mp3! Respuesta: stream: Trojan.Generic FOUND
Virus detectado en miniatura: titulo_video
```

**ERROR** (Errores críticos):
```
Error de conexión con ClamAV al escanear archivo_audio.mp3: Connection refused
Timeout escaneando archivo_nombre.wav con ClamAV
```

**DEBUG** (Información de depuración):
```
Socket conectado a ClamAV en localhost:3310 con timeout 30s
ClamAV response para audio_titulo.mp3: stream: OK
Chunk de 4096 bytes enviado. Total: 8192/16384
```

---

## Testing

### Verificar Conectividad con ClamAV

```bash
# Desde PowerShell (Windows)
Test-NetConnection -ComputerName localhost -Port 3310

# Desde Bash (Linux/Mac)
nc -zv localhost 3310
```

### Archivos de Prueba

Para probar sin virus real, ClamAV proporciona una cadena EICAR (no maliciosa):

```
X5O!P%@AP[4\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*
```

Guardar en un archivo `.txt` e intentar subirlo debería ser detectado como virus.

### Desactivar ClamAV para Desarrollo

```properties
# En application.properties o variable de entorno
antivirus.enabled=false
```

Cuando está deshabilitado:
- `scanFile()` devuelve `true` (archivo "seguro")
- `isAvailable()` devuelve `false`
- Se genera log: `"Antivirus deshabilitado. El archivo X no será escaneado."`

---

## Endpoints Afectados

### Registro y Actualización de Perfiles

| Endpoint | Método | Afectado |
|----------|--------|----------|
| `/users/register-standard` | POST | Foto de perfil (si se añade) |
| `/users/register-creator` | POST | Foto de perfil (si se añade) |
| `/management/user/profile/{userId}` | PUT | Foto de perfil |
| `/management/creator/profile/{creatorId}` | PUT | Foto de perfil |
| `/management/admin/profile` | PUT | Foto de perfil |

### Subida de Contenido

| Endpoint | Método | Afectado |
|----------|--------|----------|
| `/content/upload-audio` | POST | Audio + Miniatura |
| `/content/upload-video` | POST | Miniatura |

---

## Flujo de Datos

```
Usuario carga archivo
    ↓
Controller recibe petición
    ↓
Service decodifica Base64 → bytes
    ↓
ClamAVService.scanFile(bytes, filename)
    ↓
├─ Conexión socket a ClamAV
├─ Envía comando INSTREAM
├─ Envía archivo en chunks
├─ Espera respuesta
└─ Retorna boolean (true/false)
    ↓
Si escaneo = OK:
├─ Guardar en BD
└─ Retornar 200 OK
    ↓
Si escaneo = VIRUS:
├─ Rechazar archivo
└─ Retornar 400 Bad Request
    ↓
Si error de conexión:
├─ Lanzar excepción
└─ Retornar 500 Internal Server Error
```

---

## Performance y Consideraciones

### Límites de Archivo
- **Tamaño máximo recomendado**: 100MB (configurable)
- **Timeout**: 30 segundos (configurable)
- **Chunk size**: 4KB para transmisión eficiente

### Optimizaciones
1. **Escaneo antes de guardar**: Evita llenar BD con archivos infectados
2. **Chunks en streaming**: No carga todo en memoria
3. **Timeout configurable**: Previene bloqueos indefinidos
4. **Opcional**: Puede deshabilitarse en desarrollo

### Escalabilidad
- Una instancia de ClamAV puede soportar múltiples conexiones concurrentes
- Para alto volumen, considerar pool de conexiones o múltiples instancias

---

## Próximos Pasos

1. **Desplegar ClamAV en producción**:
   ```bash
   docker run -d --name clamav -p 3310:3310 clamav/clamav:latest
   ```

2. **Configurar variables de entorno** en servidor

3. **Monitorear logs** de ClamAV:
   ```bash
   docker logs -f clamav
   ```

4. **Actualizar definiciones de virus**:
   ```bash
   docker exec clamav freshclam
   ```

5. **Realizar testing** con archivos de prueba EICAR

---

## Referencias

- [ClamAV Documentation](https://docs.clamav.net/)
- [ClamAV INSTREAM Protocol](https://docs.clamav.net/manual/Scanning/Clamd.html#instream)
- [Docker ClamAV Image](https://hub.docker.com/r/clamav/clamav)
- [EICAR Test File](https://www.eicar.org/)

---

**Última actualización**: 13 de Noviembre de 2025
**Versión**: 1.0
