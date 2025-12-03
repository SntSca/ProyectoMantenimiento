package com.esimedia.features.notifications.http;

import com.esimedia.features.notifications.dto.NotificationDTO;
import com.esimedia.features.notifications.services.NotificationService;
import com.esimedia.shared.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationControllerTest {

    private final NotificationService notificationService = mock(NotificationService.class);
    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final NotificationController controller =
            new NotificationController(notificationService, jwtUtil);

    private HttpServletRequest mockRequest(String token, String userId) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.getUserIdFromToken("Bearer " + token)).thenReturn(userId);
        return request;
    }

    @Test
    void getInboxShouldCallServiceWithCurrentUser() {
        String token = "fake.jwt";
        String userId = "user-123";

        HttpServletRequest request = mockRequest(token, userId);

        List<NotificationDTO> expectedList = List.of(
                new NotificationDTO("1", "NOTIFICATION", "NEW_CONTENT", "t", "b", false),
                new NotificationDTO("2", "ALERT", "ALERT_GENERIC", "t2", "b2", true)
        );
        when(notificationService.getInbox(userId)).thenReturn(expectedList);

        var response = controller.getInbox(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedList, response.getBody());

        verify(notificationService, times(1)).getInbox(userId);
    }

    @Test
    void getUnreadCountShouldReturnCountMap() {
        String token = "fake.jwt";
        String userId = "user-123";

        HttpServletRequest request = mockRequest(token, userId);

        when(notificationService.getUnreadCount(userId)).thenReturn(5L);

        var response = controller.getUnreadCount(request);

        assertEquals(200, response.getStatusCode().value());
        Map<String, Long> body = response.getBody();
        assertNotNull(body);
        assertEquals(5L, body.get("unreadCount"));

        verify(notificationService, times(1)).getUnreadCount(userId);
    }

    @Test
    void markAsReadShouldCallServiceAndReturnNoContent() {
        String token = "fake.jwt";
        String userId = "user-123";
        String notificationId = "notif-1";

        HttpServletRequest request = mockRequest(token, userId);

        var response = controller.markAsRead(notificationId, request);

        assertEquals(204, response.getStatusCode().value());

        verify(notificationService, times(1)).markAsRead(notificationId, userId);
    }

    @Test
    void deleteNotificationShouldCallServiceAndReturnNoContent() {
        String token = "fake.jwt";
        String userId = "user-123";
        String notificationId = "notif-2";

        HttpServletRequest request = mockRequest(token, userId);

        var response = controller.deleteNotification(notificationId, request);

        assertEquals(204, response.getStatusCode().value());

        verify(notificationService, times(1)).deleteNotification(notificationId, userId);
    }

    @Test
    void getExpiringAlertsShouldFilterAlertsFromInbox() {
        String token = "fake.jwt";
        String userId = "user-123";

        HttpServletRequest request = mockRequest(token, userId);

        NotificationDTO notif1 = new NotificationDTO("1", "NOTIFICATION", "NEW_CONTENT", "t1", "b1", false);
        NotificationDTO notif2 = new NotificationDTO("2", "ALERT", "ALERT_GENERIC", "t2", "b2", false);
        NotificationDTO notif3 = new NotificationDTO("3", "ALERT", "EXPIRING_SOON", "t3", "b3", false);

        when(notificationService.getInbox(userId)).thenReturn(List.of(notif1, notif2, notif3));

        var response = controller.getExpiringAlerts(request);

        assertEquals(200, response.getStatusCode().value());
        List<NotificationDTO> alerts = response.getBody();
        assertNotNull(alerts);
        assertEquals(2, alerts.size());
        assertTrue(alerts.stream().allMatch(n -> "ALERT".equals(n.type())));
    }

    @Test
    void getCurrentUserIdShouldThrowWhenMissingAuthorizationHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            // uso indirecto a través de getInbox
            controller.getInbox(request);
        });

        assertEquals("Missing or invalid Authorization header", ex.getMessage());
        verifyNoInteractions(notificationService);
    }

    @Test
    void getCurrentUserIdShouldThrowWhenAuthorizationNotBearer() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Token something");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            controller.getInbox(request);
        });

        assertEquals("Missing or invalid Authorization header", ex.getMessage());
        verifyNoInteractions(notificationService);
    }
}
