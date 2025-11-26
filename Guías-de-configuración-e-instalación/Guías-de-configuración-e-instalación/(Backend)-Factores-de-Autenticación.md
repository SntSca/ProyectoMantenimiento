# 🔐 Implementación de Triple Factor de Autenticación - EsiMedia

  

## 📋 Resumen de la Implementación

  

Hemos implementado exitosamente un sistema de **triple factor de autenticación** para tu aplicación EsiMedia.

  

### ✅ Factores Implementados:

  

1. **Factor 1: Contraseña** - ✅ Ya estaba implementado

2. **Factor 2: Código por Email** - ✅ Implementado

3. **Factor 3: Código QR/TOTP** - ✅ Implementado

  

---

  

## 🗂️ Archivos Creados/Modificados:

  

### 📁 Nuevas Entidades:

- `CodigoVerificacion.java` - Gestiona códigos temporales de verificación

- Campos agregados a `Usuario.java` para 2FA

  

### 📁 Nuevos Servicios:

- `TwoFactorAuthService.java` - Servicio principal para 2FA

- Método `sendTwoFactorCode()` agregado a `EmailService.java`

  

### 📁 Nuevos Controladores:

- `TwoFactorAuthController.java` - Endpoints para gestión de 2FA

  

### 📁 Nuevos Repositorios:

- `CodigoVerificacionRepository.java` - Acceso a datos de códigos

  

### 📁 Servicios Modificados:

- `UsersService.java` - Autenticación multi-paso implementada

  

### 📁 Dependencias Agregadas (pom.xml):

```xml

<!-- 2FA/TOTP -->

<dependency>

    <groupId>com.warrenstrange</groupId>

    <artifactId>googleauth</artifactId>

    <version>1.5.0</version>

</dependency>

  

<!-- QR Code Generation -->

<dependency>

    <groupId>com.google.zxing</groupId>

    <artifactId>core</artifactId>

    <version>3.5.3</version>

</dependency>

  

<dependency>

    <groupId>com.google.zxing</groupId>

    <artifactId>javase</artifactId>

    <version>3.5.3</version>

</dependency>

```

  

### 📁 Plantillas de Email:

- `two-factor-code.html.txt` - Plantilla para códigos 2FA

  

---

  

## 🚀 Flujo de Autenticación:

  

### **Paso 1: Login con Contraseña**

```http

POST /users/login/step1

{

    "emailOrAlias": "usuario@email.com",

    "password": "contraseña123"

}

```

**Respuesta:**

```json

{

    "sessionToken": "uuid-temporal",

    "nextStep": 2,

    "message": "Código enviado a us***@email.com",

    "requiresEmail": true,

    "requiresTotp": false

}

```

  

### **Paso 2: Verificación por Email**

```http

POST /users/login/step2

{

    "sessionToken": "uuid-temporal",

    "emailCode": "123456"

}

```

**Respuesta (sin TOTP):**

```json

{

    "success": true,

    "token": "jwt-token",

    "username": "usuario@email.com",

    "message": "Login exitoso"

}

```

  

**Respuesta (con TOTP configurado):**

```json

{

    "sessionToken": "uuid-temporal",

    "nextStep": 3,

    "message": "Introduce tu código TOTP o código de respaldo",

    "requiresTotp": true

}

```

  

### **Paso 3: Verificación TOTP/Backup**

```http

POST /users/login/step3

{

    "sessionToken": "uuid-temporal",

    "totpCode": "654321"  // O "backupCode": "ABC12345"

}

```

**Respuesta:**

```json

{

    "success": true,

    "token": "jwt-token",

    "username": "usuario@email.com",

    "message": "Login exitoso con triple factor de autenticación"

}

```

  

---

  

## 🔧 Gestión de 2FA:

  

### **Configurar TOTP (Código QR)**

```http

POST /auth/2fa/totp/setup

Authorization: Bearer jwt-token

```

**Respuesta:**

```json

{

    "qrCodeUrl": "otpauth://totp/EsiMedia:usuario@email.com?secret=...",

    "secretKey": "JBSWY3DPEHPK3PXP",

    "message": "Escanea el código QR con tu aplicación de autenticación"

}

```

  

### **Confirmar Configuración TOTP**

```http

POST /auth/2fa/totp/confirm

Authorization: Bearer jwt-token

{

    "code": "123456"

}

```

  

### **Enviar Código por Email**

```http

POST /auth/2fa/email/send

Authorization: Bearer jwt-token

```

  

### **Verificar Código de Email**

```http

POST /auth/2fa/email/verify

Authorization: Bearer jwt-token

{

    "code": "123456"

}

```

  

---

  

## 📱 Aplicaciones de Autenticación Compatibles:

  

- **Google Authenticator**

- **Microsoft Authenticator**

- **Authy**

- **1Password**

- **Bitwarden**

  

---

  

## 🔒 Características de Seguridad:

  

✅ **Códigos de Email:**

- Expiran en 10 minutos

- Solo se pueden usar una vez

- Se invalidan automáticamente al generar nuevos

  

✅ **Códigos TOTP:**

- Rotan cada 30 segundos

- Basados en el estándar RFC 6238

- Funcionan offline una vez configurados

  

✅ **Códigos de Respaldo:**

- 10 códigos alfanuméricos únicos

- Se eliminan después de usarse

- Se regeneran al reconfigurar TOTP

  

✅ **Sesiones Temporales:**

- Expiran en 15 minutos

- Se limpian automáticamente

- Validación de pasos secuencial

  

---

  

## 🎯 Próximos Pasos:

  
  

1. **Configurar Variables de Entorno** para email en `application.properties`:

   

-    email.remitente=tu-email@gmail.com

-    email.passwordApp=tu-app-password

-    email.confirmationUrl=http://localhost:8080/users/confirm/

   

  
2. **Frontend Integration:**

-    Implementar flujo de 3 pasos en Angular

-    Mostrar códigos QR para configuración TOTP

-    Manejar sesiones temporales entre pasos

  

---

  
