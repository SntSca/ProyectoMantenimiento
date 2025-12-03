package com.esimedia.features.notifications.http;

import com.esimedia.features.notifications.dto.NotificationDTO;
import com.esimedia.features.notifications.services.NotificationService;
import com.esimedia.shared.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    /**
     * Obtiene el id de usuario (subject) a partir del JWT del header Authorization.
     */
    private String getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        return jwtUtil.getUserIdFromToken(authHeader);
    }

    // GET /api/notifications
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getInbox(HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        List<NotificationDTO> notifications = notificationService.getInbox(userId);
        return ResponseEntity.ok(notifications);
    }

    // GET /api/notifications/unread-count
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    // PATCH /api/notifications/{id}/read
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable String id,
                                           HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        notificationService.markAsRead(id, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable String id,
                                                   HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        notificationService.deleteNotification(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<NotificationDTO>> getExpiringAlerts(HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        List<NotificationDTO> alerts = notificationService.getInbox(userId).stream()
                .filter(n -> "ALERT".equals(n.type())) 
                .toList();
        return ResponseEntity.ok(alerts);
    }


}
