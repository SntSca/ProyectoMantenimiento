package com.esimedia.features.auth.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LeakLookupServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private LeakLookupService service;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        // configurar api key y enable=true mediante reflexión (porque son @Value)
        Field keyField = LeakLookupService.class.getDeclaredField("leakLookupApiKey");
        keyField.setAccessible(true);
        keyField.set(service, "TEST_KEY");

        Field enabledField = LeakLookupService.class.getDeclaredField("enableLeakCheck");
        enabledField.setAccessible(true);
        enabledField.set(service, true);
    }

    // -------------------------------------------------------------------------
    // 1. isPasswordCompromised()
    // -------------------------------------------------------------------------

    @Test
    void testIsPasswordCompromised_ServiceDisabled_ReturnsFalse() throws Exception {
        Field enabledField = LeakLookupService.class.getDeclaredField("enableLeakCheck");
        enabledField.setAccessible(true);
        enabledField.set(service, false);

        assertFalse(service.isPasswordCompromised("abc"));
    }

    @Test
    void testIsPasswordCompromised_NullPassword_ReturnsFalse() {
        assertFalse(service.isPasswordCompromised(null));
    }

    @Test
    void testIsPasswordCompromised_EmptyPassword_ReturnsFalse() {
        assertFalse(service.isPasswordCompromised(""));
    }

    @Test
    void testIsPasswordCompromised_Exception_ReturnsFalse() throws Exception {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("API error"));

        assertFalse(service.isPasswordCompromised("password123"));
    }


    // -------------------------------------------------------------------------
    // 2. checkWithLeakLookupAPI() --- contraseña comprometida
    // -------------------------------------------------------------------------

    @Test
    void testCheckWithLeakLookupAPI_Compromised() throws Exception {
        // SHA1("test") = a94a8fe5ccb19ba61c4c0873d391e987982fbbd3
        // prefix = A94A8
        // suffix = FE5CCB19BA61C4C0873D391E987982FBBD3

        String jsonResponse = "{ \"hashes\": [\"FE5CCB19BA61C4C0873D391E987982FBBD3\"] }";

        ResponseEntity<String> okResponse =
                new ResponseEntity<>(jsonResponse, HttpStatus.OK);

        when(restTemplate.getForEntity(contains("A94A8"), eq(String.class)))
                .thenReturn(okResponse);

        JsonNode node = new ObjectMapper().readTree(jsonResponse);
        when(objectMapper.readTree(jsonResponse)).thenReturn(node);

        assertTrue(invokePrivateCheck("test"));
    }

    // -------------------------------------------------------------------------
    // 3. checkWithLeakLookupAPI() --- no comprometida
    // -------------------------------------------------------------------------

    @Test
    void testCheckWithLeakLookupAPI_NotCompromised() throws Exception {
        String jsonResponse = "{ \"hashes\": [\"AAAABBBBCCCCDDDD\"] }";

        ResponseEntity<String> okResponse =
                new ResponseEntity<>(jsonResponse, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(okResponse);

        JsonNode node = new ObjectMapper().readTree(jsonResponse);
        when(objectMapper.readTree(jsonResponse)).thenReturn(node);

        assertFalse(invokePrivateCheck("test"));
    }

    // -------------------------------------------------------------------------
    // 4. Respuesta no 200
    // -------------------------------------------------------------------------

    @Test
    void testCheckWithLeakLookupAPI_Non200Response() throws Exception {
        ResponseEntity<String> badResponse =
                new ResponseEntity<>("", HttpStatus.BAD_REQUEST);

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(badResponse);

        assertFalse(invokePrivateCheck("test"));
    }


    // -------------------------------------------------------------------------
    // 5. Excepción de RestTemplate
    // -------------------------------------------------------------------------

    @Test
    void testCheckWithLeakLookupAPI_RestClientException() throws Exception {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("Error"));

        assertFalse(invokePrivateCheck("test"));
    }


    // -------------------------------------------------------------------------
    // 6. computeSHA1()
    // -------------------------------------------------------------------------

    @Test
    void testComputeSHA1() throws Exception {
        var m = LeakLookupService.class.getDeclaredMethod("computeSHA1", String.class);
        m.setAccessible(true);

        String sha1 = (String) m.invoke(service, "test");

        assertEquals("a94a8fe5ccb19ba61c4c0873d391e987982fbbd3", sha1);
    }


    // -------------------------------------------------------------------------
    // 7. bytesToHex()
    // -------------------------------------------------------------------------

    @Test
    void testBytesToHex() throws Exception {
        var m = LeakLookupService.class.getDeclaredMethod("bytesToHex", byte[].class);
        m.setAccessible(true);

        byte[] bytes = { 0x0F, (byte) 0xA0 };

        String hex = (String) m.invoke(null, (Object) bytes);

        assertEquals("0fa0", hex);
    }


    // -------------------------------------------------------------------------
    // 8. Mensaje estático
    // -------------------------------------------------------------------------

    @Test
    void testGetCompromisedPasswordMessage() {
        String msg = LeakLookupService.getCompromisedPasswordMessage();

        assertTrue(msg.contains("comprometida"));
        assertTrue(msg.contains("brechas"));
    }


    // -------------------------------------------------------------------------
    // MÉTODO AUXILIAR PARA INVOCAR checkWithLeakLookupAPI PRIVADO
    // -------------------------------------------------------------------------
    private boolean invokePrivateCheck(String password) throws Exception {
        var m = LeakLookupService.class.getDeclaredMethod("checkWithLeakLookupAPI", String.class);
        m.setAccessible(true);
        return (boolean) m.invoke(service, password);
    }
}
