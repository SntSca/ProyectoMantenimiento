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

import com.esimedia.features.auth.enums.Rol;
import com.esimedia.features.lists.dto.AgregarContenidoPublicoDTO;
import com.esimedia.features.lists.dto.ContenidoListaResponseDTO;
import com.esimedia.features.lists.dto.EliminarContenidoPublicoDTO;
import com.esimedia.features.lists.dto.ListaPublicaReproduccionDTO;
import com.esimedia.features.lists.dto.ListaResponseDTO;
import com.esimedia.features.lists.dto.ListaUpdateFieldsPublicasDTO;
import com.esimedia.features.lists.services.PublicListService;
import com.esimedia.shared.util.JwtValidationUtil;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/content/lists")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", exposedHeaders = "Authorization")
public class PublicListController {

    private static final Logger logger = LoggerFactory.getLogger(PublicListController.class);
    private static final String ERROR_INTERNO_SERVIDOR = "Error interno del servidor";

    private final PublicListService publicListService;
    private final JwtValidationUtil jwtValidationService;

    public PublicListController(PublicListService publicListService,
                               JwtValidationUtil jwtValidationService) {
        this.publicListService = publicListService;
        this.jwtValidationService = jwtValidationService;
    }

    /**
     * Crear una lista de reproducción pública
     */
    @PostMapping("/public")
    public ResponseEntity<String> crearListaPublica(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ListaPublicaReproduccionDTO listaDTO) {
        ResponseEntity<String> response;
        try {
            
            // Validar que es un creador de contenido y obtener su ID
            String userId = jwtValidationService.validarGetCreador(authHeader);

            // Validar que el usuario del JWT es el mismo que está en el DTO
            if (!userId.equals(listaDTO.getIdCreadorUsuario())) {
                response = ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No puedes crear listas para otros usuarios");
            }
            else {
                String result = publicListService.crearListaPublica(listaDTO);
                response = ResponseEntity.ok(result);
            }
        }
        catch (ResponseStatusException e) {
            logger.warn("Error creando lista pública: {}", e.getReason());
            response = ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
        catch (Exception e) {
            logger.error("Error interno creando lista pública: {}", e.getMessage());
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ERROR_INTERNO_SERVIDOR);
        }
        return response;
    }

    /**
     * Agregar contenido a una lista pública
     */
    @PutMapping("/public/add-content")
    public ResponseEntity<String> agregarContenidoListaPublica(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody AgregarContenidoPublicoDTO agregarDTO) {
        ResponseEntity<String> response;
        try {
            
            // Validar que es un creador de contenido
            jwtValidationService.validarGetCreador(authHeader);

            String result = publicListService.agregarContenidoListaPublica(agregarDTO);
            response = ResponseEntity.ok(result);
        }
        catch (ResponseStatusException e) {
            logger.warn("Error agregando contenido a lista pública: {}", e.getReason());
            response = ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
        catch (Exception e) {
            logger.error("Error interno agregando contenido a lista pública: {}", e.getMessage());
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ERROR_INTERNO_SERVIDOR);
        }
        return response;
    }

    /**
     * Consultar todas las listas públicas con contenidos completos
     */
    @GetMapping("/public/contents")
    public ResponseEntity<List<ListaResponseDTO>> consultarTodasListasPublicasConContenidos(
            @RequestHeader("Authorization") String authHeader) {
        ResponseEntity<List<ListaResponseDTO>> response;
        try {
            // Validar que es un usuario registrado (cualquier usuario puede ver listas públicas)
            jwtValidationService.validarGenerico(authHeader);
            
            // Filtrar listas según el rol del usuario
            Rol userRole = jwtValidationService.getRolFromToken(authHeader);

            List<ListaResponseDTO> listas = publicListService.obtenerTodasListasPublicasConContenidos();
            
            if (userRole == Rol.NORMAL) {
                listas = listas.stream()
                    .filter(lista -> Boolean.TRUE.equals(lista.getVisibilidad()))
                    .map(lista -> {
                        ListaResponseDTO filteredLista = new ListaResponseDTO();
                        filteredLista.setIdLista(lista.getIdLista());
                        filteredLista.setNombre(lista.getNombre());
                        filteredLista.setDescripcion(lista.getDescripcion());
                        filteredLista.setIdCreadorUsuario(lista.getIdCreadorUsuario());
                        filteredLista.setVisibilidad(lista.getVisibilidad());
                        // Filtrar contenidos visibles
                        List<ContenidoListaResponseDTO> contenidosVisibles = lista.getContenidos().stream()
                            .filter(contenido -> contenido.isVisibilidad())
                            .toList();
                        filteredLista.setContenidos(contenidosVisibles);
                        return filteredLista;
                    })
                    .toList();
            }
            
            response = ResponseEntity.ok(listas);
        }
        catch (ResponseStatusException e) {
            logger.warn("Error consultando todas las listas públicas con contenidos: {}", e.getReason());
            response = ResponseEntity.status(e.getStatusCode()).body(null);
        }
        catch (Exception e) {
            logger.error("Error interno consultando todas las listas públicas con contenidos: {}", e.getMessage());
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return response;
    }

    /**
     * Actualizar campos principales de una lista pública (nombre, descripción, visibilidad)
     * Solo el creador puede modificar sus listas
     */
    @PutMapping("/public/fields")
    public ResponseEntity<String> actualizarCamposListaPublica(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ListaUpdateFieldsPublicasDTO updateDTO) {
        ResponseEntity<String> response;
        try {
            // Validar que es un creador de contenido y obtener su ID
            jwtValidationService.validarGetCreador(authHeader);
            
            String result = publicListService.actualizarCamposListaPublica(updateDTO);
            response = ResponseEntity.ok(result);
        }
        catch (ResponseStatusException e) {
            logger.warn("Error actualizando campos de lista pública: {}", e.getReason());
            response = ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
        catch (Exception e) {
            logger.error("Error interno actualizando campos de lista pública: {}", e.getMessage());
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ERROR_INTERNO_SERVIDOR);
        }
        return response;
    }

    /**
     * Eliminar lista pública
     */
    @DeleteMapping("/public/{idLista}")
    public ResponseEntity<String> eliminarListaPublica(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String idLista) {
        ResponseEntity<String> response;
        try {
            // Validar que es un creador de contenido y obtener su ID
            String userId = jwtValidationService.validarGetCreador(authHeader);

            String result = publicListService.eliminarListaPublica(idLista, userId);
            response = ResponseEntity.ok(result);
        }
        catch (ResponseStatusException e) {
            logger.warn("Error eliminando lista pública: {}", e.getReason());
            response = ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
        catch (Exception e) {
            logger.error("Error interno eliminando lista pública: {}", e.getMessage());
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ERROR_INTERNO_SERVIDOR);
        }
        return response;
    }

    /**
     * Eliminar contenido de una lista pública
     */
    @DeleteMapping("/public/remove-content")
    public ResponseEntity<String> eliminarContenidoListaPublica(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody EliminarContenidoPublicoDTO eliminarDTO) {
        ResponseEntity<String> response;
        try {
            // Validar que es un creador de contenido
            jwtValidationService.validarGetCreador(authHeader);
            
            String result = publicListService.eliminarContenidoListaPublica(eliminarDTO);
            response = ResponseEntity.ok(result);
        }
        catch (ResponseStatusException e) {
            logger.warn("Error eliminando contenido de lista pública: {}", e.getReason());
            response = ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
        catch (Exception e) {
            logger.error("Error interno eliminando contenido de lista pública: {}", e.getMessage());
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ERROR_INTERNO_SERVIDOR);
        }
        return response;
    }
}