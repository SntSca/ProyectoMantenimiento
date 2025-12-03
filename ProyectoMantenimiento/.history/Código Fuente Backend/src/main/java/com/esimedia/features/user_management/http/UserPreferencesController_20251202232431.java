package com.esimedia.features.user_management.http;

import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;
import com.esimedia.shared.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class UserPreferencesController {

    private final UsuarioNormalRepository usuarioNormalRepository;
    private final JwtUtil jwtUtil;

    // Constante para evitar duplicación de literal
    private static final String GUSTOS_TAGS_KEY = "gustosTags";
    
    private String getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        return jwtUtil.getUserIdFromToken(authHeader);
    }

    /**
     * Obtener los gustos (tags) del usuario logado.
     */
    @GetMapping("/gustos")
    public ResponseEntity<Map<String, List<String>>> getGustos(HttpServletRequest request) {
        String userId = getCurrentUserId(request);

        UsuarioNormal usuario = usuarioNormalRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<String> gustos = usuario.getGustosTags();
        if (gustos == null) {
            gustos = List.of();
        }

        return ResponseEntity.ok(Map.of(GUSTOS_TAGS_KEY, gustos));
    }

    /**
     * Actualizar los gustos (tags) del usuario logado.
     * Body esperado:
     * { "gustosTags": ["Acción", "Comedia", "Drama"] }
     */
    @PutMapping("/gustos")
    public ResponseEntity<Map<String, List<String>>> updateGustos(
            HttpServletRequest request,
            @RequestBody Map<String, List<String>> body) {

        String userId = getCurrentUserId(request);
        List<String> gustosTags = body.getOrDefault(GUSTOS_TAGS_KEY, List.of());

        UsuarioNormal usuario = usuarioNormalRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setGustosTags(gustosTags);
        usuarioNormalRepository.save(usuario);

        return ResponseEntity.ok(Map.of(GUSTOS_TAGS_KEY, gustosTags));
    }
}
