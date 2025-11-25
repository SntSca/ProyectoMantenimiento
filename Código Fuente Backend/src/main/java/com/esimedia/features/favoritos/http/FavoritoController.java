package com.esimedia.features.favoritos.http;

import com.esimedia.features.favoritos.dto.FavoritoDTO;
import com.esimedia.features.favoritos.services.FavoritoService;
import com.esimedia.shared.util.JwtValidationUtil;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/favoritos")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", exposedHeaders = "Authorization")
public class FavoritoController {

    private static final Logger logger = LoggerFactory.getLogger(FavoritoController.class);

    private final FavoritoService favoritoService;
    private final JwtValidationUtil jwtValidationService;

    public FavoritoController(FavoritoService favoritoService, JwtValidationUtil jwtValidationService) {
        this.favoritoService = favoritoService;
        this.jwtValidationService = jwtValidationService;
    }

    @PostMapping("/add")
    public ResponseEntity<String> agregarFavorito(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody FavoritoDTO favoritoDTO) {
        try {
            String idUsuario = jwtValidationService.validarGetUsuario(authHeader);

            logger.info("Solicitud para agregar favorito: usuario={}, contenido={}", idUsuario, favoritoDTO.getIdContenido());

            String resultado = favoritoService.agregarFavorito(idUsuario, favoritoDTO);
            return ResponseEntity.ok(resultado);
        } 
        catch (Exception e) {
            logger.error("Error en agregarFavorito: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<String> eliminarFavorito(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody FavoritoDTO favoritoDTO) {
        try {
            String idUsuario = jwtValidationService.validarGetUsuario(authHeader);

            logger.info("Solicitud para eliminar favorito: usuario={}, contenido={}", idUsuario, favoritoDTO.getIdContenido());

            String resultado = favoritoService.eliminarFavorito(idUsuario, favoritoDTO);
            return ResponseEntity.ok(resultado);
        } 
        catch (Exception e) {
            logger.error("Error en eliminarFavorito: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    @GetMapping("/mis-favoritos")
    public ResponseEntity<List<String>> obtenerFavoritos(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String idUsuario = jwtValidationService.validarGetUsuario(authHeader);
            logger.info("Obteniendo favoritos del usuario {}", idUsuario);

            List<String> favoritos = favoritoService.obtenerFavoritosPorUsuario(idUsuario);
            return ResponseEntity.ok(favoritos);
        } 
        catch (Exception e) {
            logger.error("Error en obtenerFavoritos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
