package com.esimedia.features.auth.services;

import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginAttemptServiceTest {

    @Mock
    private EmailService emailService;
    
    @Mock
    private UsuarioNormalRepository userNormalRepository;

    @InjectMocks
    private LoginAttemptService loginAttemptService;

    private final String testUsername = "test@example.com";
    private final String testIp = "192.168.1.1";
    private UsuarioNormal testUser;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        testUser = UsuarioNormal.builder()
            .email(testUsername)
            .alias("testuser")
            .nombre("Test User")
            .apellidos("Test Apellidos")
            .password("TestPassword123!")
            .build();
        
        when(userNormalRepository.findByemail(testUsername)).thenReturn(Optional.of(testUser));
        doNothing().when(emailService).sendSecurityAlertEmail(any(), anyInt());
    }

    // ========== isBlocked - Cubre branches principales ==========

    @Test
    void testIsBlocked_NotBlocked() {
        // Cubre: info == null -> false
        assertFalse(loginAttemptService.isBlocked(testUsername, testIp));
    }

    @Test
    void testIsBlocked_BlockedInTime() {
        // Cubre: blockStartTime != 0 AND dentro del tiempo -> true
        for (int i = 0; i < 5; i++) {
            loginAttemptService.recordFailedAttempt(testUsername, testIp);
        }
        assertTrue(loginAttemptService.isBlocked(testUsername, testIp));
    }

    // ========== recordFailedAttempt - Cubre branches ==========

    @Test
    void testRecordFailedAttempt_BlocksAtMax() {
        // Cubre: currentAttempts >= maxAttempts
        for (int i = 0; i < 5; i++) {
            loginAttemptService.recordFailedAttempt(testUsername, testIp);
        }
        
        assertTrue(loginAttemptService.isBlocked(testUsername, testIp));
        verify(emailService).sendSecurityAlertEmail(testUser, 5);
    }

    @Test
    void testRecordFailedAttempt_IgnoresWhenBlocked() {
        // Cubre: isBlocked() -> return early
        for (int i = 0; i < 5; i++) {
            loginAttemptService.recordFailedAttempt(testUsername, testIp);
        }
        loginAttemptService.recordFailedAttempt(testUsername, testIp);
        
        verify(emailService, times(1)).sendSecurityAlertEmail(any(), anyInt());
    }

    @Test
    void testRecordFailedAttempt_UserNotFound() {
        // Cubre: Optional.empty()
        String unknownUser = "unknown@test.com";
        when(userNormalRepository.findByemail(unknownUser)).thenReturn(Optional.empty());
        
        for (int i = 0; i < 5; i++) {
            loginAttemptService.recordFailedAttempt(unknownUser, testIp);
        }
        
        assertTrue(loginAttemptService.isBlocked(unknownUser, testIp));
    }

    @Test
    void testRecordFailedAttempt_EmailException() {
        // Cubre: catch en handleBlock
        doThrow(new RuntimeException("Error")).when(emailService).sendSecurityAlertEmail(any(), anyInt());
        
        for (int i = 0; i < 5; i++) {
            loginAttemptService.recordFailedAttempt(testUsername, testIp);
        }
        
        assertTrue(loginAttemptService.isBlocked(testUsername, testIp));
    }

    @Test
    void testRecordFailedAttempt_AdaptiveMode() {
        // Cubre: isAdaptive -> maxAttempts = 2
        for (int i = 0; i < 20; i++) {
            loginAttemptService.recordFailedAttempt("user" + i + "@test.com", testIp);
        }
        
        String newUser = "new@test.com";
        when(userNormalRepository.findByemail(newUser)).thenReturn(Optional.empty());
        
        loginAttemptService.recordFailedAttempt(newUser, testIp);
        loginAttemptService.recordFailedAttempt(newUser, testIp);
        
        assertTrue(loginAttemptService.isBlocked(newUser, testIp));
    }

    // ========== resetAttempts ==========

    @Test
    void testResetAttempts() {
        for (int i = 0; i < 5; i++) {
            loginAttemptService.recordFailedAttempt(testUsername, testIp);
        }
        
        loginAttemptService.resetAttempts(testUsername, testIp);
        assertFalse(loginAttemptService.isBlocked(testUsername, testIp));
    }

    // ========== isIpBlocked - Sin tests largos ==========

    @Test
    void testIsIpBlocked_NotBlocked() {
        // Cubre: until == null -> false
        assertFalse(loginAttemptService.isIpBlocked("1.2.3.4"));
    }

    @Test
    void testIsIpBlocked_Blocked() {
        // Cubre: until > now -> true
        for (int i = 0; i < 20; i++) {
            loginAttemptService.recordFailedAttempt("user" + i + "@test.com", testIp);
        }
        assertTrue(loginAttemptService.isIpBlocked(testIp));
    }

    // ========== getIpRetryAfterSeconds ==========

    @Test
    void testGetIpRetryAfterSeconds_NotBlocked() {
        // Cubre: until == null -> 0
        assertEquals(0, loginAttemptService.getIpRetryAfterSeconds("1.2.3.4"));
    }

    @Test
    void testGetIpRetryAfterSeconds_Blocked() {
        // Cubre: diff > 0 -> segundos
        for (int i = 0; i < 20; i++) {
            loginAttemptService.recordFailedAttempt("user" + i + "@test.com", testIp);
        }
        long retry = loginAttemptService.getIpRetryAfterSeconds(testIp);
        assertTrue(retry > 0 && retry <= 300);
    }

    // ========== getUserRetryAfterSeconds ==========

    @Test
    void testGetUserRetryAfterSeconds_NotBlocked() {
        // Cubre: info == null -> 0
        assertEquals(0, loginAttemptService.getUserRetryAfterSeconds(testUsername, testIp));
    }

    // ========== shouldActivateAdaptive ==========

    @Test
    void testShouldActivateAdaptive_Below() {
        // Cubre: users.size() < 20 -> false
        for (int i = 0; i < 10; i++) {
            loginAttemptService.recordFailedAttempt("user" + i + "@test.com", testIp);
        }
        assertFalse(loginAttemptService.isIpBlocked(testIp));
    }

    @Test
    void testShouldActivateAdaptive_Reaches() {
        // Cubre: users.size() >= 20 -> true
        for (int i = 0; i < 20; i++) {
            loginAttemptService.recordFailedAttempt("user" + i + "@test.com", testIp);
        }
        assertTrue(loginAttemptService.isIpBlocked(testIp));
    }
}