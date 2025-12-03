package com.esimedia.features.lists.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.esimedia.features.lists.dto.ListaPrivadaReproduccionDTO;
import com.esimedia.features.lists.entity.ListaContenidoPrivada;
import com.esimedia.features.lists.entity.ListaPrivada;
import com.esimedia.features.lists.repository.ListaContenidoPrivadaRepository;
import com.esimedia.features.lists.repository.ListaPrivadaRepository;
import com.esimedia.shared.util.ContentValidationUtil;

@Service
public class PrivateListCreationService {

    private static final Logger logger = LoggerFactory.getLogger(PrivateListCreationService.class);

    private final ListaPrivadaRepository listaPrivadaRepository;
    private final ListaContenidoPrivadaRepository listaContenidoPrivadaRepository;
    private final ContentValidationUtil contentValidationUtil;

    public PrivateListCreationService(
            ListaPrivadaRepository listaPrivadaRepository,
            ListaContenidoPrivadaRepository listaContenidoPrivadaRepository,
            ContentValidationUtil contentValidationUtil) {
        this.listaPrivadaRepository = listaPrivadaRepository;
        this.listaContenidoPrivadaRepository = listaContenidoPrivadaRepository;
        this.contentValidationUtil = contentValidationUtil;
    }

    /**
     * Crear una lista de reproducción privada
     */
    public String crearListaPrivada(ListaPrivadaReproduccionDTO listaDTO) {
        logger.info("Creando lista privada: {} para usuario: {}",
            listaDTO.getNombre(), listaDTO.getIdCreadorUsuario());

        // Validar que los contenidos existen (audio o video)
        for (String idContenido : listaDTO.getContenidos()) {
            contentValidationUtil.validarContenidoExistente(idContenido);
        }

        // Crear la lista privada
        ListaPrivada lista = ListaPrivada.builder()
            .nombre(listaDTO.getNombre())
            .descripcion(listaDTO.getDescripcion())
            .idCreadorUsuario(listaDTO.getIdCreadorUsuario())
            .build();

        ListaPrivada listaGuardada = listaPrivadaRepository.save(lista);

        // Agregar contenidos a la lista
        for (String idContenido : listaDTO.getContenidos()) {
            ListaContenidoPrivada contenido = ListaContenidoPrivada.builder()
                .idLista(listaGuardada.getIdLista())
                .idContenido(idContenido)
                .build();
            listaContenidoPrivadaRepository.save(contenido);
        }

        logger.info("Lista privada creada exitosamente: {}", listaGuardada.getIdLista());
        return "Lista privada creada exitosamente";
    }
}