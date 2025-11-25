package com.esimedia.features.lists.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.lists.dto.ListaPublicaReproduccionDTO;
import com.esimedia.features.lists.entity.ListaContenidoPublica;
import com.esimedia.features.lists.entity.ListaPublica;
import com.esimedia.features.lists.repository.ListaContenidoPublicaRepository;
import com.esimedia.features.lists.repository.ListaPublicaRepository;
import com.esimedia.shared.util.ContentValidationUtil;

@Service
public class PublicListCreationService {

    private static final Logger logger = LoggerFactory.getLogger(PublicListCreationService.class);

    private final ListaPublicaRepository listaPublicaRepository;
    private final ListaContenidoPublicaRepository listaContenidoPublicaRepository;
    private final ContentValidationUtil contentValidationUtil;

    public PublicListCreationService(
            ListaPublicaRepository listaPublicaRepository,
            ListaContenidoPublicaRepository listaContenidoPublicaRepository,
            ContentValidationUtil contentValidationUtil) {
        this.listaPublicaRepository = listaPublicaRepository;
        this.listaContenidoPublicaRepository = listaContenidoPublicaRepository;
        this.contentValidationUtil = contentValidationUtil;
    }

    /**
     * Crear una lista de reproducción pública
     */
    public String crearListaPublica(ListaPublicaReproduccionDTO listaDTO) {
        logger.info("Creando lista pública: {}", listaDTO.getNombre());

        // Validar que no existe una lista pública con el mismo nombre
        if (listaPublicaRepository.existsByNombre(listaDTO.getNombre())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Ya existe una lista pública con el nombre: " + listaDTO.getNombre());
        }

        // Validar que los contenidos existen
        for (String idContenido : listaDTO.getContenidos()) {
            contentValidationUtil.validarContenidoExistente(idContenido);
        }

        // Crear la lista pública
        ListaPublica lista = ListaPublica.builder()
            .nombre(listaDTO.getNombre())
            .descripcion(listaDTO.getDescripcion())
            .idCreadorUsuario(listaDTO.getIdCreadorUsuario())
            .visibilidad(listaDTO.getVisibilidad())
            .build();

        ListaPublica listaGuardada = listaPublicaRepository.save(lista);

        // Agregar contenidos a la lista
        for (String idContenido : listaDTO.getContenidos()) {
            ListaContenidoPublica contenido = ListaContenidoPublica.builder()
                .idLista(listaGuardada.getIdLista())
                .idContenido(idContenido)
                .build();
            listaContenidoPublicaRepository.save(contenido);
        }

        logger.info("Lista pública creada exitosamente: {}", listaGuardada.getIdLista());
        return "Lista pública creada exitosamente";
    }
}