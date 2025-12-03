package com.esimedia.features.auth.services;

import com.esimedia.features.auth.enums.Rol;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SessionTimeoutServiceTest {

    private SessionTimeoutService service;

    @BeforeEach
    void setUp() {
        service = new SessionTimeoutService();
    }

    // Helper: acceso al mapa privado activeSessions
    private Map<String, SessionTimeoutService.SessionInfo> getActiveSessions() throws Exception {
        Field field = SessionTimeoutService.class.getDeclaredField("activeSessions");
        field.setAccessible(true);
        return (Map<String, SessionTimeoutService.SessionInfo>) field.get(service);
    }

    // Helper: obtener timeout por rol mediante reflexión
    private long getTimeoutConstant(String name) throws Exception {
        Field f = SessionTimeoutService.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.getLong(null);
    }

    // ---------------------------------------------------------------------
    // registerSession()
    // ---------------------------------------------------------------------
    @Test
    void testRegisterSession() throws Exception {
        service.registerSession("user1", Rol.NORMAL);

        Map<String, SessionTimeoutService.SessionInfo> sessions = getActiveSessions();
        assertTrue(sessions.containsKey("user1"));

        SessionTimeoutService.SessionInfo info = sessions.get("user1");
        assertEquals("user1", info.getUserId());
        assertEquals(Rol.NORMAL, info.getRol());
    }

    // ---------------------------------------------------------------------
    // getAbsoluteTimeoutByRole() (cubierto indirectamente, pero añadimos test directo)
    // ---------------------------------------------------------------------
    @Test
    void testAbsoluteTimeoutByRole_Normal() throws Exception {
        service.registerSession("u", Rol.NORMAL);
        long timeout = service.getSessionInfo("u").getAbsoluteExpirationTime()
                - service.getSessionInfo("u").getLoginTime();

        assertEquals(getTimeoutConstant("ABSOLUTE_TIMEOUT_NORMAL"), timeout);
    }

    @Test
    void testAbsoluteTimeoutByRole_Creador() throws Exception {
        service.registerSession("u", Rol.CREADOR);
        long timeout = service.getSessionInfo("u").getAbsoluteExpirationTime()
                - service.getSessionInfo("u").getLoginTime();

        assertEquals(getTimeoutConstant("ABSOLUTE_TIMEOUT_CREADOR"), timeout);
    }

    @Test
    void testAbsoluteTimeoutByRole_Admin() throws Exception {
        service.registerSession("u", Rol.ADMINISTRADOR);
        long timeout = service.getSessionInfo("u").getAbsoluteExpirationTime()
                - service.getSessionInfo("u").getLoginTime();

        assertEquals(getTimeoutConstant("ABSOLUTE_TIMEOUT_ADMIN"), timeout);
    }

    // ---------------------------------------------------------------------
    // isSessionValid()
    // ---------------------------------------------------------------------
    @Test
    void testIsSessionValid_NoSession() {
        assertFalse(service.isSessionValid("nope"));
    }

    @Test
    void testIsSessionValid_ValidSession() {
        service.registerSession("user1", Rol.NORMAL);
        assertTrue(service.isSessionValid("user1"));
    }

    @Test
    void testIsSessionValid_ExpiredSession() throws Exception {
        service.registerSession("user1", Rol.NORMAL);

        // Manipular expiración forzando una fecha antigua
        Map<String, SessionTimeoutService.SessionInfo> map = getActiveSessions();
        SessionTimeoutService.SessionInfo info = map.get("user1");

        Field expField = SessionTimeoutService.SessionInfo.class.getDeclaredField("absoluteExpirationTime");
        expField.setAccessible(true);
        expField.set(info, System.currentTimeMillis() - 1000); // ya está expirada

        assertFalse(service.isSessionValid("user1"));
        assertFalse(map.containsKey("user1")); // se elimina
    }

    // ---------------------------------------------------------------------
    // invalidateSession()
    // ---------------------------------------------------------------------
    @Test
    void testInvalidateSession() throws Exception {
        service.registerSession("user1", Rol.NORMAL);
        service.invalidateSession("user1");

        Map<String, SessionTimeoutService.SessionInfo> map = getActiveSessions();
        assertFalse(map.containsKey("user1"));
    }

    @Test
    void testInvalidateSession_NoExisting() {
        assertDoesNotThrow(() -> service.invalidateSession("nope"));
    }

    // ---------------------------------------------------------------------
    // getRemainingSessionTime()
    // ---------------------------------------------------------------------
    @Test
    void testGetRemainingSessionTime_NoSession() {
        assertEquals(-1, service.getRemainingSessionTime("nope"));
    }

    @Test
    void testGetRemainingSessionTime_ValidSession() {
        service.registerSession("user1", Rol.NORMAL);
        long remaining = service.getRemainingSessionTime("user1");

        assertTrue(remaining > 0);
    }

    @Test
    void testGetRemainingSessionTime_Expired() throws Exception {
        service.registerSession("user1", Rol.NORMAL);

        Map<String, SessionTimeoutService.SessionInfo> map = getActiveSessions();
        SessionTimeoutService.SessionInfo info = map.get("user1");

        Field expField = SessionTimeoutService.SessionInfo.class.getDeclaredField("absoluteExpirationTime");
        expField.setAccessible(true);
        expField.set(info, System.currentTimeMillis() - 1000);

        assertEquals(-1, service.getRemainingSessionTime("user1"));
    }

    // ---------------------------------------------------------------------
    // getSessionInfo()
    // ---------------------------------------------------------------------
    @Test
    void testGetSessionInfo() {
        service.registerSession("user1", Rol.NORMAL);

        SessionTimeoutService.SessionInfo info = service.getSessionInfo("user1");
        assertNotNull(info);
        assertEquals("user1", info.getUserId());
    }

    @Test
    void testGetSessionInfo_NoSession() {
        assertNull(service.getSessionInfo("none"));
    }

    // ---------------------------------------------------------------------
    // cleanupExpiredSessions()
    // ---------------------------------------------------------------------
    @Test
    void testCleanupExpiredSessions_WithExpired() throws Exception {
        service.registerSession("u1", Rol.NORMAL);
        service.registerSession("u2", Rol.NORMAL);

        Map<String, SessionTimeoutService.SessionInfo> map = getActiveSessions();

        // Forzar expiración de u1
        SessionTimeoutService.SessionInfo info = map.get("u1");
        Field expField = SessionTimeoutService.SessionInfo.class.getDeclaredField("absoluteExpirationTime");
        expField.setAccessible(true);
        expField.set(info, System.currentTimeMillis() - 1000);

        service.cleanupExpiredSessions();

        assertFalse(map.containsKey("u1"));
        assertTrue(map.containsKey("u2"));
    }

    @Test
    void testCleanupExpiredSessions_NoneExpired() throws Exception {
        service.registerSession("u1", Rol.NORMAL);

        Map<String, SessionTimeoutService.SessionInfo> map = getActiveSessions();
        int before = map.size();

        service.cleanupExpiredSessions();

        assertEquals(before, map.size());
    }

    // ---------------------------------------------------------------------
    // getActiveSessionsCount()
    // ---------------------------------------------------------------------
    @Test
    void testGetActiveSessionsCount() {
        service.registerSession("u1", Rol.NORMAL);
        service.registerSession("u2", Rol.NORMAL);

        assertEquals(2, service.getActiveSessionsCount());
    }
}
