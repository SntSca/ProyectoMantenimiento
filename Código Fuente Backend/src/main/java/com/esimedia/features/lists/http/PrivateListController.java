package com.esimedia.features.lists.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.lists.dto.AgregarContenidoDTO;
import com.esimedia.features.lists.dto.ContenidoListaResponseDTO;
import com.esimedia.features.lists.dto.EliminarContenidoDTO;
import com.esimedia.features.lists.dto.ListaPrivadaReproduccionDTO;
import com.esimedia.features.lists.dto.ListaPrivadaResponseDTO;
import com.esimedia.features.lists.dto.ListaUpdateFieldsPrivadasDTO;
import com.esimedia.features.lists.services.PrivateListService;
import com.esimedia.shared.util.JwtValidationUtil;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/content/lists")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", exposedHeaders = "Authorization")
public class PrivateListController {

    private static final Logger logger = LoggerFactory.getLogger(PrivateListController.class);
    private static final String ERROR_INTERNO_SERVIDOR = "Error interno del servidor";

    private final PrivateListService privateListService;
    private final JwtValidationUtil jwtValidationService;

    public PrivateListController(PrivateListService privateListService,
                                JwtValidationUtil jwtValidationService) {
        this.privateListService = privateListService;
        this.jwtValidationService = jwtValidationService;
    }

    /**
     * Crear una lista de reproducción privada
     */
    @PostMapping("/private")
    public ResponseEntity<String> crearListaPrivada(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ListaPrivadaReproduccionDTO listaDTO) {
        ResponseEntity<String> response;
        try {
            // Validar que es un usuario registrado y obtener su ID
            String userId = jwtValidationService.validarGetUsuario(authHeader);

            // Validar que el usuario del JWT es el mismo que está en el DTO
            if (!userId.equals(listaDTO.getIdCreadorUsuario())) {
                response = ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No puedes crear listas para otros usuarios");
            }
            else {

                String result = privateListService.crearListaPrivada(listaDTO);
                response = ResponseEntity.ok(result);
            }
        }
        catch (ResponseStatusException e) {
            logger.warn("Error creando lista privada: {}", e.getReason());
            response = ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
        catch (Exception e) {
            logger.error("Error interno creando lista privada: {}", e.getMessage());
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ERROR_INTERNO_SERVIDOR);
        }
        return response;
    }

    /**
     * Agregar contenido a una lista privada
     */
    @PutMapping("/private/add-content")
    public ResponseEntity<String> agregarContenidoListaPrivada(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody AgregarContenidoDTO agregarDTO) {
        ResponseEntity<String> response;
        try {
            // Validar que es un usuario registrado y obtener su ID
            String userId = jwtValidationService.validarGetUsuario(authHeader);
            
            // Validar que el usuario del JWT es el mismo que está en el DTO
            if (!userId.equals(agregarDTO.getIdUsuario())) {
                response = ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No puedes modificar listas de otros usuarios");
            }
            else {
                String result = privateListService.agregarContenidoListaPrivada(agregarDTO);
                response = ResponseEntity.ok(result);
            }
        }
        catch (ResponseStatusException e) {
            logger.warn("Error agregando contenido a lista privada: {}", e.getReason());
            response = ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
        catch (Exception e) {
            logger.error("Error interno agregando contenido a lista privada: {}", e.getMessage());
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ERROR_INTERNO_SERVIDOR);
        }
        return response;
    }

    /**
     * Consultar todas las listas privadas con contenidos completos
     */
    @GetMapping("/private/contents")
    public ResponseEntity<List<ListaPrivadaResponseDTO>> consultarTodasListasPrivadasConContenidos(
            @RequestHeader("Authorization") String authHeader) {
        ResponseEntity<List<ListaPrivadaResponseDTO>> response;
        try {
            // Validar que es un usuario registrado y obtener su ID
            String userId = jwtValidationService.validarGetUsuario(authHeader);
            
            List<ListaPrivadaResponseDTO> listas = privateListService.obtenerTodasListasPrivadasConContenidos(userId);
            
            // Filtrar listas visibles y contenidos visibles
            listas = listas.stream()
                .map(lista -> {
                    ListaPrivadaResponseDTO filteredLista = new ListaPrivadaResponseDTO();
                    filteredLista.setIdLista(lista.getIdLista());
                    filteredLista.setNombre(lista.getNombre());
                    filteredLista.setDescripcion(lista.getDescripcion());
                    filteredLista.setIdCreadorUsuario(lista.getIdCreadorUsuario());
                    // Filtrar contenidos visibles
                    List<ContenidoListaResponseDTO> contenidosVisibles = lista.getContenidos().stream()
                        .filter(contenido -> contenido.isVisibilidad())
                        .toList();
                    filteredLista.setContenidos(contenidosVisibles);
                    return filteredLista;
                })
                .toList();
            
            response = ResponseEntity.ok(listas);
        }
        catch (ResponseStatusException e) {
            logger.warn("Error consultando listas privadas con contenidos: {}", e.getReason());
            response = ResponseEntity.status(e.getStatusCode()).body(null);
        }
        catch (Exception e) {
            logger.error("Error interno consultando listas privadas con contenidos: {}", e.getMessage());
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return response;
    }

    /**
     * Actualizar campos principales de una lista privada (nombre, descripción, visibilidad)
     */
    @PutMapping("/private/fields")
    public ResponseEntity<String> actualizarCamposListaPrivada(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ListaUpdateFieldsPrivadasDTO updateDTO) {
        ResponseEntity<String> response;
        try {
            // Validar que es un usuario registrado y obtener su ID
            String userId = jwtValidationService.validarGetUsuario(authHeader);
            
            String result = privateListService.actualizarCamposListaPrivada(updateDTO, userId);
            response = ResponseEntity.ok(result);
        }
        catch (ResponseStatusException e) {
            logger.warn("Error actualizando campos de lista privada: {}", e.getReason());
            response = ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
        catch (Exception e) {
            logger.error("Error interno actualizando campos de lista privada: {}", e.getMessage());
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ERROR_INTERNO_SERVIDOR);
        }
        return response;
    }

    /**
     * Eliminar lista privada
     */
    @DeleteMapping("/private/{idLista}")
    public ResponseEntity<String> eliminarListaPrivada(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String idLista) {
        ResponseEntity<String> response;
        try {
            // Validar que es un usuario registrado y obtener su ID
            String userId = jwtValidationService.validarGetUsuario(authHeader);

            String result = privateListService.eliminarListaPrivada(idLista, userId);
            response = ResponseEntity.ok(result);
        }
        catch (ResponseStatusException e) {
            logger.warn("Error eliminando lista privada: {}", e.getReason());
            response = ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
        catch (Exception e) {
            logger.error("Error interno eliminando lista privada: {}", e.getMessage());
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ERROR_INTERNO_SERVIDOR);
        }
        return response;
    }

    /**
     * Eliminar contenido de una lista privada
     */
    @DeleteMapping("/private/remove-content")
    public ResponseEntity<String> eliminarContenidoListaPrivada(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody EliminarContenidoDTO eliminarDTO) {
        ResponseEntity<String> response;
        try {
            // Validar que es un usuario registrado y obtener su ID
            String userId = jwtValidationService.validarGetUsuario(authHeader);
            
            // Validar que el usuario del JWT es el mismo que está en el DTO
            if (!userId.equals(eliminarDTO.getIdUsuario())) {
                response = ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No puedes modificar listas de otros usuarios");
            }
            else {
                String result = privateListService.eliminarContenidoListaPrivada(eliminarDTO);
                response = ResponseEntity.ok(result);
            }
        }
        catch (ResponseStatusException e) {
            logger.warn("Error eliminando contenido de lista privada: {}", e.getReason());
            response = ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
        catch (Exception e) {
            logger.error("Error interno eliminando contenido de lista privada: {}", e.getMessage());
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ERROR_INTERNO_SERVIDOR);
        }
        return response;
    }
}