package com.esimedia.features.content.entity;


import com.esimedia.features.content.dto.ContentAudioUploadDTO;
import com.esimedia.features.content.dto.ContentUploadDTO;
import com.esimedia.features.content.dto.ContentVideoUploadDTO;
import com.esimedia.features.content.enums.Resolucion;
import com.esimedia.features.content.enums.RestriccionEdad;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContenidoFactoryTest {

    // ===================== CREAR CONTENIDO AUDIO =====================
    @Test
    void crearContenidoAudioDTO_success() {
        ContentAudioUploadDTO dto = new ContentAudioUploadDTO();
        dto.setTitulo("Titulo Audio");
        dto.setDescripcion("Descripcion Audio");
        dto.setDuracion(120);
        dto.setEsVIP(true);

        ContenidosAudio contenido = (ContenidosAudio) ContenidoFactory.crearContenido(dto);
        assertNotNull(contenido);
        assertEquals("Titulo Audio", contenido.getTitulo());
        assertTrue(contenido.isEsVIP());
    }

    @Test
    void crearContenidoAudioDTO_nullDTO_throws() {
        assertThrows(NullPointerException.class, () -> ContenidoFactory.crearContenido((ContentUploadDTO) null));
    }

    // ===================== CREAR CONTENIDO VIDEO =====================
    @Test
    void crearContenidoVideoDTO_success() {
        ContentVideoUploadDTO dto = new ContentVideoUploadDTO();
        dto.setTitulo("Titulo Video");
        dto.setDescripcion("Descripcion Video");
        dto.setDuracion(300);
        dto.setEsVIP(false);
        dto.setRestriccionEdad(18);
        dto.setUrlArchivo("http://archivo.mp4");
        dto.setResolucion(Resolucion.FHD_1080.name());

        ContenidosVideo contenido = (ContenidosVideo) ContenidoFactory.crearContenido(dto);
        assertNotNull(contenido);
        assertEquals("Titulo Video", contenido.getTitulo());
        assertFalse(contenido.isEsVIP());
        assertEquals(RestriccionEdad.ADULTOS, contenido.getRestriccionEdad());
        assertEquals("http://archivo.mp4", contenido.getUrlArchivo());
    }

    @Test
    void crearContenidoDTO_tipoNoSoportado_throws() {
        ContentUploadDTO dto = new ContentUploadDTO();
        assertThrows(IllegalArgumentException.class, () -> ContenidoFactory.crearContenido(dto));
    }
}
