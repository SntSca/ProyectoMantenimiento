package com.esimedia.features.auth.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdministradorTest {
    
    @Test
    void testDefaultConstructor() {
        Administrador admin = new Administrador();
        
        assertNull(admin.getIdUsuario());
        assertNull(admin.getNombre());
        assertNull(admin.getApellidos());
        assertNull(admin.getEmail());
        assertNull(admin.getAlias());
        assertNull(admin.getPassword());
        assertNull(admin.getDepartamento());
    }

    @Test
    void testBuilderWithAllFields() {
        String fotoPerfil = "data:image/jpeg;base64,/9j/4AAQSkZJRgABA...";
        
        Administrador admin = Administrador.builder()
            .nombre("Carlos")
            .apellidos("Rodriguez")
            .email("carlos@example.com")
            .alias("carlosrodriguez")
            .password("Admin123!")
            .departamento("IT")
            .fotoPerfil(fotoPerfil)
            .build();
        
        assertEquals("Carlos", admin.getNombre());
        assertEquals("Rodriguez", admin.getApellidos());
        assertEquals("carlos@example.com", admin.getEmail());
        assertEquals("carlosrodriguez", admin.getAlias());
        assertEquals("Admin123!", admin.getPassword());
        assertEquals("IT", admin.getDepartamento());
        assertEquals(fotoPerfil, admin.getFotoPerfil());
    }

    @Test
    void testBuilderWithDefaultValues() {
        Administrador admin = Administrador.builder()
            .nombre("Maria")
            .apellidos("Garcia")
            .email("maria@example.com")
            .alias("mariagarcia")
            .password("Pass456!")
            .build();
        
        assertEquals("Maria", admin.getNombre());
        assertEquals("Garcia", admin.getApellidos());
        assertEquals("maria@example.com", admin.getEmail());
        assertEquals("mariagarcia", admin.getAlias());
        assertEquals("Pass456!", admin.getPassword());
        assertNull(admin.getDepartamento());
    }

    @Test
    void testSetters() {
        Administrador admin = new Administrador();
        
        admin.setDepartamento("HR");
        
        assertEquals("HR", admin.getDepartamento());
    }

    @Test
    void testEqualsWithSameObject() {
        Administrador admin = Administrador.builder()
            .nombre("Ana")
            .email("ana@example.com")
            .build();
        
        assertEquals(admin, admin);
    }

    @Test
    void testEqualsWithEqualObjects() {
        Administrador admin1 = Administrador.builder()
            .nombre("Luis")
            .apellidos("Martinez")
            .email("luis@example.com")
            .alias("luismartinez")
            .password("Luis123!")
            .departamento("Finance")
            .build();
        
        Administrador admin2 = Administrador.builder()
            .nombre("Luis")
            .apellidos("Martinez")
            .email("luis@example.com")
            .alias("luismartinez")
            .password("Luis123!")
            .departamento("Finance")
            .build();
        
        assertEquals(admin1, admin2);
    }

    @Test
    void testEqualsWithDifferentObjects() {
        Administrador admin1 = Administrador.builder()
            .nombre("Pedro")
            .email("pedro@example.com")
            .build();
        
        Administrador admin2 = Administrador.builder()
            .nombre("Pablo")
            .email("pablo@example.com")
            .build();
        
        assertNotEquals(admin1, admin2);
    }

    @Test
    void testEqualsWithNull() {
        Administrador admin = new Administrador();
        
        assertNotEquals(null, admin);
    }

    @Test
    void testEqualsWithDifferentClass() {
        Administrador admin = new Administrador();
        String otherObject = "test";
        
        assertNotEquals(admin, otherObject);
    }

    @Test
    void testHashCodeConsistency() {
        Administrador admin = Administrador.builder()
            .nombre("Sofia")
            .apellidos("Ruiz")
            .email("sofia@example.com")
            .alias("sofiaruiz")
            .password("Sofia123!")
            .departamento("Marketing")
            .build();
        
        int hashCode1 = admin.hashCode();
        int hashCode2 = admin.hashCode();
        
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testHashCodeWithEqualObjects() {
        Administrador admin1 = Administrador.builder()
            .nombre("Diego")
            .apellidos("Sanchez")
            .email("diego@example.com")
            .alias("diegos")
            .password("Diego123!")
            .departamento("IT")
            .build();
        
        Administrador admin2 = Administrador.builder()
            .nombre("Diego")
            .apellidos("Sanchez")
            .email("diego@example.com")
            .alias("diegos")
            .password("Diego123!")
            .departamento("IT")
            .build();
        
        assertEquals(admin1.hashCode(), admin2.hashCode());
    }

    @Test
    void testCanEqual() {
        Administrador admin1 = new Administrador();
        Administrador admin2 = new Administrador();

        assertEquals(admin1, admin2);
    }

    @Test
    void testEqualsWithDifferentDepartamento() {
        Administrador admin1 = Administrador.builder()
            .nombre("Elena")
            .email("elena@example.com")
            .departamento("IT")
            .build();
        
        Administrador admin2 = Administrador.builder()
            .nombre("Elena")
            .email("elena@example.com")
            .departamento("HR")
            .build();
        
        assertNotEquals(admin1, admin2);
    }

    @Test
    void testEqualsWithNullDepartamento() {
        Administrador admin1 = Administrador.builder()
            .nombre("Miguel")
            .email("miguel@example.com")
            .departamento(null)
            .build();
        
        Administrador admin2 = Administrador.builder()
            .nombre("Miguel")
            .email("miguel@example.com")
            .departamento("IT")
            .build();
        
        assertNotEquals(admin1, admin2);
    }

    @Test
    void testEqualsWithBothNullDepartamento() {
        Administrador admin1 = Administrador.builder()
            .nombre("Laura")
            .email("laura@example.com")
            .departamento(null)
            .build();
        
        Administrador admin2 = Administrador.builder()
            .nombre("Laura")
            .email("laura@example.com")
            .departamento(null)
            .build();
        
        assertEquals(admin1, admin2);
    }

    @Test
    void testToString() {
        Administrador admin = Administrador.builder()
            .nombre("Pablo")
            .email("pablo@example.com")
            .departamento("Sales")
            .build();
        
        String result = admin.toString();
        
        assertNotNull(result);
        assertTrue(result.contains("Administrador"));
    }

    @Test
    void testGetters() {
        Administrador admin = new Administrador();
        admin.setDepartamento("Legal");
        
        assertEquals("Legal", admin.getDepartamento());
    }

    @Test
    void testBuilderFluentInterface() {
        String fotoPerfil = "data:image/png;base64,iVBORw0KGgoAAAA...";
        
        Administrador admin = Administrador.builder()
            .nombre("Roberto")
            .apellidos("Fernandez")
            .email("roberto@example.com")
            .alias("robertof")
            .password("Roberto123!")
            .departamento("Operations")
            .fotoPerfil(fotoPerfil)
            .build();
        
        assertNotNull(admin);
        assertEquals("Roberto", admin.getNombre());
        assertEquals("Operations", admin.getDepartamento());
        assertEquals(fotoPerfil, admin.getFotoPerfil());
    }

    @Test
    void testHashCodeWithNullValues() {
        Administrador admin = new Administrador();
        
        assertDoesNotThrow(admin::hashCode);
    }

    @Test
    void testToStringWithNullValues() {
        Administrador admin = new Administrador();
        
        assertDoesNotThrow(admin::toString);
    }

    @Test
    void testEqualsWithDifferentNombre() {
        Administrador admin1 = Administrador.builder()
            .nombre("Carlos")
            .email("test@example.com")
            .departamento("IT")
            .build();
        
        Administrador admin2 = Administrador.builder()
            .nombre("Pedro")
            .email("test@example.com")
            .departamento("IT")
            .build();
        
        assertNotEquals(admin1, admin2);
    }

    @Test
    void testEqualsWithDifferentEmail() {
        Administrador admin1 = Administrador.builder()
            .nombre("Carlos")
            .email("carlos1@example.com")
            .departamento("IT")
            .build();
        
        Administrador admin2 = Administrador.builder()
            .nombre("Carlos")
            .email("carlos2@example.com")
            .departamento("IT")
            .build();
        
        assertNotEquals(admin1, admin2);
    }

    @Test
    void testEqualsWithDifferentApellidos() {
        Administrador admin1 = Administrador.builder()
            .nombre("Carlos")
            .apellidos("Rodriguez")
            .email("carlos@example.com")
            .build();
        
        Administrador admin2 = Administrador.builder()
            .nombre("Carlos")
            .apellidos("Martinez")
            .email("carlos@example.com")
            .build();
        
        assertNotEquals(admin1, admin2);
    }

    @Test
    void testEqualsWithDifferentAlias() {
        Administrador admin1 = Administrador.builder()
            .nombre("Carlos")
            .email("carlos@example.com")
            .alias("carlos1")
            .build();
        
        Administrador admin2 = Administrador.builder()
            .nombre("Carlos")
            .email("carlos@example.com")
            .alias("carlos2")
            .build();
        
        assertNotEquals(admin1, admin2);
    }

    @Test
    void testEqualsWithDifferentPassword() {
        Administrador admin1 = Administrador.builder()
            .nombre("Carlos")
            .email("carlos@example.com")
            .password("Pass123!")
            .build();
        
        Administrador admin2 = Administrador.builder()
            .nombre("Carlos")
            .email("carlos@example.com")
            .password("Pass456!")
            .build();
        
        assertNotEquals(admin1, admin2);
    }

    @Test
    void testEqualsWithDifferentFotoPerfil() {
        Administrador admin1 = Administrador.builder()
            .nombre("Carlos")
            .email("carlos@example.com")
            .fotoPerfil("data:image/jpeg;base64,/9j/4AAQSkZJRgABA...")
            .build();
        
        Administrador admin2 = Administrador.builder()
            .nombre("Carlos")
            .email("carlos@example.com")
            .fotoPerfil("data:image/png;base64,iVBORw0KGgoAAAA...")
            .build();
        
        assertNotEquals(admin1, admin2);
    }

    @Test
    void testCanEqualWithDifferentClass() {
        Administrador admin = new Administrador();
        UsuarioNormal usuario = new UsuarioNormal();
        
        assertNotEquals(admin, usuario);
    }

    @Test
    void testSetDepartamentoMultipleTimes() {
        Administrador admin = new Administrador();
        
        admin.setDepartamento("IT");
        assertEquals("IT", admin.getDepartamento());
        
        admin.setDepartamento("HR");
        assertEquals("HR", admin.getDepartamento());
        
        admin.setDepartamento(null);
        assertNull(admin.getDepartamento());
    }
}
