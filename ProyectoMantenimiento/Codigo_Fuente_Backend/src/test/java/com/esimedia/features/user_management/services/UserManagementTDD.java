package com.esimedia.features.user_management.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.user_management.http.UserManagementController;
import com.esimedia.shared.util.JwtValidationUtil;

class UserManagementTDD {

    @Mock
    private JwtValidationUtil jwtValidationService;

    @Mock
    private UserListService userListService;

    @InjectMocks
    private UserManagementController userManagementController;

    private static final String VALID_TOKEN = "validToken";
    private static final String INVALID_TOKEN = "invalidToken";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllUsersWithValidTokenAndUsers() {
        try {
            when(jwtValidationService.validarGetAdmin(VALID_TOKEN)).thenReturn("admin123");

            Map<String, Object> users = new HashMap<>();
            users.put("normalUsers", Collections.singletonList("user1"));
            when(userListService.getAllUsers()).thenReturn(users);

            ResponseEntity<Map<String, Object>> response = userManagementController.getAllUsers(VALID_TOKEN);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, ((List<?>) response.getBody().get("normalUsers")).size());
        } 
        catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    void testGetAllUsersWithValidTokenAndNoUsers() {
        try {
            when(jwtValidationService.validarGetAdmin(VALID_TOKEN)).thenReturn("admin123");

            Map<String, Object> users = new HashMap<>();
            users.put("normalUsers", Collections.emptyList());
            when(userListService.getAllUsers()).thenReturn(users);

            ResponseEntity<Map<String, Object>> response = userManagementController.getAllUsers(VALID_TOKEN);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(((List<?>) response.getBody().get("normalUsers")).isEmpty());
        } 
        catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    void testGetAllUsersWithInvalidToken() {
        when(jwtValidationService.validarGetAdmin(INVALID_TOKEN)).thenThrow(new IllegalArgumentException("Token inválido"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userManagementController.getAllUsers(INVALID_TOKEN);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void testGetAllUsersWithInternalError() {
        try {
            when(jwtValidationService.validarGetAdmin(VALID_TOKEN)).thenReturn("admin123");
            when(userListService.getAllUsers()).thenThrow(new RuntimeException("DB error"));

            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
                userManagementController.getAllUsers(VALID_TOKEN);
            });

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        } 
        catch (Exception e) {
            fail("Exception should not be thrown");
        }
    }

    @Test
    void testGetAllUsersWithoutAuthorizationHeader() {
        when(jwtValidationService.validarGetAdmin(null)).thenThrow(new IllegalArgumentException("Token inválido"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userManagementController.getAllUsers(null);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void testGetAllUsersWithEmptyAuthorizationHeader() {
        when(jwtValidationService.validarGetAdmin("")).thenThrow(new IllegalArgumentException("Token vacío"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userManagementController.getAllUsers("");
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void testGetAllUsersWithMalformedToken() {
        String malformedToken = "malformedToken";
        when(jwtValidationService.validarGetAdmin(malformedToken)).thenThrow(new IllegalArgumentException("Formato inválido"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userManagementController.getAllUsers(malformedToken);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void testGetAllUsersWithNonAdminToken() {
        when(jwtValidationService.validarGetAdmin(VALID_TOKEN)).thenThrow(new IllegalArgumentException("No es admin"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userManagementController.getAllUsers(VALID_TOKEN);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void testGetAllUsersWithExpiredToken() {
        when(jwtValidationService.validarGetAdmin(VALID_TOKEN)).thenThrow(new IllegalArgumentException("Token expirado"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userManagementController.getAllUsers(VALID_TOKEN);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void testGetAllUsersWithNullResponseFromService() {
        try {
            when(jwtValidationService.validarGetAdmin(VALID_TOKEN)).thenReturn("admin123");
            when(userListService.getAllUsers()).thenReturn(null);

            ResponseEntity<Map<String, Object>> response = userManagementController.getAllUsers(VALID_TOKEN);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNull(response.getBody());
        } 
        catch (Exception e) {
            fail("Exception should not be thrown");
        }
    }

    @Test
    void testGetAllUsersWithPartialData() {
        try {
            when(jwtValidationService.validarGetAdmin(VALID_TOKEN)).thenReturn("admin123");

            Map<String, Object> users = new HashMap<>();
            users.put("normalUsers", Collections.singletonList("user1"));
            users.put("adminUsers", null);
            when(userListService.getAllUsers()).thenReturn(users);

            ResponseEntity<Map<String, Object>> response = userManagementController.getAllUsers(VALID_TOKEN);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, ((List<?>) response.getBody().get("normalUsers")).size());
            assertNull(response.getBody().get("adminUsers"));
        } 
        catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }
}
