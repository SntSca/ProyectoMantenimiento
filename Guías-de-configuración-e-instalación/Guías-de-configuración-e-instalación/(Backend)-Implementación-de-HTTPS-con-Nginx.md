# ESIMedia Backend - Configuración HTTPS con nginx

Este documento explica cómo configurar HTTPS con nginx para el desarrollo del backend de ESIMedia. Esta configuración permite que el backend Spring Boot sea accesible de forma segura durante el desarrollo.

## 📋 Requisitos Previos

### Software necesario:
1. **OpenSSL** - Para generar certificados SSL
2. **nginx** - Servidor web/proxy reverso  
3. **Java 17** - Para ejecutar Spring Boot
4. **Maven** - Para compilar y ejecutar el proyecto
5. **PowerShell** - Para ejecutar comandos

## 🚀 Instalación Paso a Paso

### PASO 1: Instalar OpenSSL

#### Opción A: Usando Chocolatey (Recomendado)
```powershell
# Instalar Chocolatey si no lo tienes
Set-ExecutionPolicy Bypass -Scope Process -Force
[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Instalar OpenSSL
choco install openssl
```

#### Opción B: Descarga manual
1. Descargar desde: https://slproweb.com/products/Win32OpenSSL.html
2. Instalar y agregar al PATH del sistema

### PASO 2: Instalar nginx

#### Opción A: Usando Chocolatey (Recomendado)
```powershell
choco install nginx
```

Es importante tomar en cuenta que tendrás que añadir al PATH la dirección de la carpeta donde se haya instalado nginx para poderlo utilizar. 

#### Opción B: Descarga manual
```powershell
# Crear directorio para nginx
New-Item -ItemType Directory -Path "C:\nginx" -Force

# Descargar nginx
Invoke-WebRequest -Uri "http://nginx.org/download/nginx-1.24.0.zip" -OutFile "$env:TEMP\nginx.zip"

# Extraer
Expand-Archive -Path "$env:TEMP\nginx.zip" -DestinationPath "$env:TEMP" -Force

# Mover a destino final
Move-Item -Path "$env:TEMP\nginx-1.24.0\*" -Destination "C:\nginx" -Force

# Limpiar archivos temporales
Remove-Item "$env:TEMP\nginx.zip"
Remove-Item "$env:TEMP\nginx-1.24.0" -Recurse -Force
```

### PASO 3: Clonar y configurar el proyecto

```powershell
# Clonar el repositorio (si no lo tienes)
git clone https://github.com/ivanjimeneztajuelo/G07-EsiMedia-PI-User-Backend.git
cd G07-EsiMedia-PI-User-Backend

# Verificar que la rama contiene la configuración nginx
git checkout Rodrigo-Dev
```

### PASO 4: Generar certificados SSL

En tu proyecto, crear el directorio nginx-config que contendrá los siguientes elementos:
![imagen.png](/.attachments/imagen-12b2083b-05ef-4658-ab31-f540540cd479.png)

En caso de que no sepas como crearlos, cópialos desde el directorio donde se te haya instalado `nginx`.

Revisa también el contenido que deberías de tener en los archivos de configuración en el anexo de esta página.

```powershell
# Ir al directorio ssl
cd nginx-config\ssl

# Generar clave privada
openssl genrsa -out localhost.key 2048

# Generar certificado autofirmado
openssl req -new -x509 -key localhost.key -out localhost.crt -days 365 -config ssl.conf -extensions v3_req

# Verificar que se generaron correctamente
ls
```

### PASO 5: Configurar nginx

```powershell
# Hacer backup de la configuración original
Copy-Item "C:\nginx\conf\nginx.conf" "C:\nginx\conf\nginx.conf.backup"

# Copiar nuestra configuración personalizada
Copy-Item "nginx-config\nginx.conf" "C:\nginx\conf\nginx.conf"

# Crear directorio ssl en nginx
New-Item -ItemType Directory -Path "C:\nginx\conf\ssl" -Force

# Copiar certificados SSL
Copy-Item "nginx-config\ssl\localhost.crt" "C:\nginx\conf\ssl\"
Copy-Item "nginx-config\ssl\localhost.key" "C:\nginx\conf\ssl\"
```

### PASO 6: Verificar configuración

```powershell
# Verificar que la configuración de nginx es válida
cd "C:\nginx"
.\nginx.exe -t
```

Deberías ver:
```
nginx: the configuration file C:\nginx/conf/nginx.conf syntax is ok
nginx: configuration file C:\nginx/conf/nginx.conf test is successful
```

## 🏃‍♂️ Ejecutar el Sistema

### Opción A
En una terminal, ejecutas los siguientes comandos (debes de estar en el directorio raíz del proyecto):
```powershell
cd .\nginx-config\

# Ahora ejecutas lo siguiente para cargar la configuración de nginx
nginx -c .\nginx.conf
```

En otra terminal, podrás ejecutar la aplicación de nginx, sabiendo que ahora las peticiones no se harán sobre http://localhost:9090, sino sobre https:localhost/, siendo nginx el que redirige al backend.


### Opción B

#### Terminal 1: Iniciar nginx
```powershell
# Ir al directorio de nginx
cd "C:\nginx"

# Iniciar nginx
.\nginx.exe

# Verificar que está ejecutándose
Get-Process -Name "nginx"
```

#### Terminal 2: Iniciar el backend Spring Boot
```powershell
# Ir al directorio del proyecto
cd "ruta\a\tu\proyecto\G07-EsiMedia-PI-User-Backend"

# Iniciar el backend
mvn spring-boot:run
```

## 🌐 URLs Disponibles

Una vez que ambos servicios estén ejecutándose:

- **🌐 Página principal HTTPS:** `https://localhost`
- **🔗 API Backend HTTPS:** `https://localhost/api/`
- **💚 Health Check:** `https://localhost/health`
- **↗️ Redirección automática:** `http://localhost` → `https://localhost`

## 📡 Endpoints de la API Disponibles

### 👤 Gestión de Usuarios (`/users`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `https://localhost/users/register-standard` | Registrar usuario estándar |
| `POST` | `https://localhost/users/register-creator` | Registrar creador de contenido |
| `GET` | `https://localhost/users/pending-creators` | Obtener creadores pendientes de validación |
| `PUT` | `https://localhost/users/validate-creator/{creatorId}` | Validar un creador de contenido |

### 🔐 Gestión de Tokens (`/tokens`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `https://localhost/tokens/` | Endpoint base para gestión de tokens JWT |

### 🧪 Testing y Diagnóstico (`/api/test`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `https://localhost/api/test/health` | Estado de salud del sistema |
| `GET` | `https://localhost/api/test/db-info` | Información de la base de datos MongoDB |
| `GET` | `https://localhost/api/test/create-collections` | Crear colecciones de MongoDB |
| `POST` | `https://localhost/api/test/crear-usuario-normal` | Crear usuario de prueba |
| `POST` | `https://localhost/api/test/crear-sesion-coleccion` | Crear colección de sesiones |
| `POST` | `https://localhost/api/test/validar-creador-contenido` | Validar compatibilidad creador-contenido |
| `POST` | `https://localhost/api/test/validar-contenido-creador` | Validar contenido de creador |

### 📝 Ejemplo de uso con curl:

```bash
# Registrar usuario estándar
curl -X POST https://localhost/users/register-standard \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan",
    "apellidos": "Pérez García",
    "email": "juan.perez@test.com",
    "alias": "juan.perez",
    "password": "Password123!"
  }' \
  -k

# Health check del sistema
curl -X GET https://localhost/api/test/health -k

# Obtener información de la base de datos
curl -X GET https://localhost/api/test/db-info -k
```

**Nota:** El parámetro `-k` en curl es necesario para omitir la verificación del certificado SSL autofirmado.

## 🔧 Comandos de Gestión

### Verificar estado de los servicios
```powershell
# Ver procesos nginx
Get-Process -Name "nginx"

# Ver procesos Java (Spring Boot)
Get-Process -Name "java"

# Verificar puertos
netstat -ano | findstr ":443"  # HTTPS
netstat -ano | findstr ":9090" # Backend
```

### Gestionar nginx
```powershell
# Detener nginx
Get-Process -Name "nginx" | Stop-Process -Force

# Reiniciar nginx
cd "C:\nginx"
.\nginx.exe

# Recargar configuración sin detener
cd "C:\nginx"
.\nginx.exe -s reload

# Verificar configuración
cd "C:\nginx"
.\nginx.exe -t
```

## 🔒 Certificados SSL y Advertencias de Seguridad

### ⚠️ Por qué aparece "Tu conexión no es privada" / "Conexión no es segura"

Cuando accedas a `https://localhost`, tu navegador mostrará una advertencia de seguridad. **Esto es completamente normal** para certificados autofirmados en desarrollo y no indica ningún problema con tu configuración.

#### 🔍 ¿Por qué sucede esto?

1. **Certificado autofirmado**: Creamos nuestro propio certificado SSL en lugar de obtenerlo de una Autoridad de Certificación (CA) reconocida
2. **No está en el almacén de confianza**: El navegador no reconoce nuestro certificado como "confiable"
3. **Solo es para desarrollo**: En producción se usarían certificados oficiales (Let's Encrypt, etc.)

#### 🌐 Cómo proceder en cada navegador:

**Google Chrome:**
1. Ve a `https://localhost`
2. Verás: **"Tu conexión no es privada"** / **"Your connection is not private"**
3. Haz clic en **"Avanzado"** / **"Advanced"**
4. Haz clic en **"Continuar a localhost (no seguro)"** / **"Proceed to localhost (unsafe)"**

**Mozilla Firefox:**
1. Ve a `https://localhost`
2. Verás: **"Advertencia: riesgo potencial de seguridad a continuación"**
3. Haz clic en **"Avanzado"**
4. Haz clic en **"Aceptar el riesgo y continuar"**

**Microsoft Edge:**
1. Ve a `https://localhost`
2. Verás: **"Tu conexión no es privada"**
3. Haz clic en **"Avanzado"**
4. Haz clic en **"Continuar a localhost"**

### 🛠️ En herramientas de desarrollo:

**Postman:**
```
1. Ve a Settings (⚙️)
2. Busca "SSL certificate verification"
3. Desactívalo para requests de desarrollo
4. O importa el certificado localhost.crt
```

**Insomnia:**
```
1. Ve a Preferences
2. Busca "Validate certificates"
3. Desactívalo para desarrollo local
```

**curl (línea de comandos):**
```bash
# Opción 1: Omitir verificación SSL (recomendado para desarrollo)
curl -k https://localhost/api/test/health

# Opción 2: Especificar certificado
curl --cacert nginx-config/ssl/localhost.crt https://localhost/api/test/health
```

### ✅ ¿Es seguro continuar?

**SÍ, es completamente seguro** para desarrollo local porque:
- El tráfico sigue estando cifrado (HTTPS funciona)
- Solo estás conectándote a tu propia máquina (`localhost`)
- No hay riesgo de ataques man-in-the-middle en localhost
- Es la práctica estándar para desarrollo con HTTPS

### 🚀 Para producción:

En un entorno de producción real, necesitarías:
1. **Dominio real**: En lugar de `localhost`
2. **Certificado válido**: De Let's Encrypt, Cloudflare, etc.
3. **DNS configurado**: Para que apunte a tu servidor
4. **Sin advertencias**: Los usuarios verían el candado verde sin advertencias

## 🗂️ Estructura de Archivos Creados

```
G07-EsiMedia-PI-User-Backend/
├── nginx-config/
│   ├── nginx.conf          # Configuración principal de nginx
│   ├── ssl/
│   │   ├── ssl.conf        # Configuración para generar certificados
│   │   ├── localhost.key   # Clave privada (generada)
│   │   └── localhost.crt   # Certificado SSL (generado)
│   └── README.md           # Este archivo
└── ... (resto del proyecto Spring Boot)
```

## 🛠️ Configuración Técnica

### nginx actúa como:
- **Proxy reverso:** Redirige peticiones a Spring Boot (puerto 9090)
- **Terminación SSL:** Maneja HTTPS y convierte a HTTP interno
- **Servidor web:** Sirve contenido estático y redirecciones

### Flujo de peticiones:
```
Cliente HTTPS (443) → nginx → Backend HTTP (9090) → nginx → Cliente HTTPS
```

### Headers de seguridad incluidos:
- `Strict-Transport-Security`
- `X-Frame-Options`  
- `X-Content-Type-Options`
- `X-XSS-Protection`
- `Referrer-Policy`

## ❗ Solución de Problemas

### Error: "nginx no se reconoce como comando"
- Asegúrate de estar en el directorio `C:\nginx`
- O agrega `C:\nginx` al PATH del sistema

### Error: "openssl no se reconoce como comando"  
- Instala OpenSSL correctamente
- Reinicia PowerShell después de la instalación

### Error: "ERR_SSL_KEY_USAGE_INCOMPATIBLE"
- Los certificados fueron regenerados con configuración corregida
- Si persiste, regenera los certificados siguiendo el PASO 4

### Error: "No se puede conectar al puerto 9090"
- Verifica que Spring Boot esté ejecutándose: `Get-Process -Name "java"`
- Verifica el puerto: `netstat -ano | findstr ":9090"`

### nginx no inicia:
```powershell
# Verificar configuración
cd "C:\nginx"
.\nginx.exe -t

# Ver logs de error
Get-Content "C:\nginx\logs\error.log" -Tail 10
```

### Conflictos de puerto:
```powershell
# Ver qué está usando los puertos 80 y 443
netstat -ano | findstr ":80"
netstat -ano | findstr ":443"

# Detener otros servicios si es necesario
```

## 🔄 Para Desarrollo en Equipo

### Cada desarrollador debe:
1. Seguir todos los pasos de instalación
2. Generar sus propios certificados SSL
3. Configurar nginx en su máquina local
4. Los certificados son únicos por máquina (no compartir)

### Variables de entorno:
- El proyecto ya está configurado para desarrollo local
- Los certificados se generan para `localhost` y `127.0.0.1`

## 📝 Notas Importantes

- ⚠️ **Solo para desarrollo:** Los certificados son autofirmados
- 🔒 **En producción:** Usar certificados válidos (Let's Encrypt, etc.)
- 🌐 **Puerto backend:** 9090 (configurado en `application.properties`)
- 🔧 **Puerto nginx:** 443 (HTTPS) y 80 (HTTP redirect)

## 🆘 Soporte

Si tienes problemas:
1. Verifica que todos los requisitos previos están instalados
2. Revisa los logs de nginx: `C:\nginx\logs\error.log`
3. Verifica que no hay conflictos de puertos
4. Asegúrate de ejecutar PowerShell como administrador si es necesario

## Anexo: Archivos y su contenido
Para que la configuración se ejecute correctamente, tendrás que tener el siguiente contenido en el nginx-conf:
```conf
# Configuración de nginx para ESIMedia Backend con HTTPS
# Archivo: nginx.conf

# Configuración global
worker_processes auto;
error_log logs/error.log;
pid nginx.pid;

events {
    worker_connections 1024;
}

http {
    include       mime.types;
    default_type  application/octet-stream;
    
    # Configuración de logs
    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';
    
    access_log logs/access.log main;
    
    # Configuración básica
    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;
    
    # Configuración SSL/TLS
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_prefer_server_ciphers on;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-SHA384;
    ssl_session_timeout 10m;
    ssl_session_cache shared:SSL:10m;
    ssl_session_tickets off;
    
    # Configuración de compresión
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types
        text/plain
        text/css
        text/xml
        text/javascript
        application/json
        application/javascript
        application/xml+rss
        application/atom+xml
        image/svg+xml;

    # Servidor HTTPS - Puerto 443
    server {
        listen 443 ssl http2;
        server_name localhost 127.0.0.1;
        
        # Certificados SSL
        ssl_certificate ssl/localhost.crt;
        ssl_certificate_key ssl/localhost.key;
        
        # Headers de seguridad
        add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
        add_header X-Frame-Options DENY always;
        add_header X-Content-Type-Options nosniff always;
        add_header X-XSS-Protection "1; mode=block" always;
        add_header Referrer-Policy "strict-origin-when-cross-origin" always;
        
        # Configuración de proxy para el backend Spring Boot - Endpoints disponibles
        # POST /users/register-standard
        # POST /users/register-creator
        # GET /users/pending-creators  
        # PUT /users/validate-creator/{creatorId}
        location /users/ {
            proxy_pass http://127.0.0.1:9090/users/;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection 'upgrade';
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto https;
            proxy_cache_bypass $http_upgrade;
            
            # CORS headers para los endpoints disponibles
            add_header Access-Control-Allow-Origin "https://localhost:4200" always;
            add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
            add_header Access-Control-Allow-Headers "DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization" always;
            add_header Access-Control-Expose-Headers "Content-Length,Content-Range,Authorization" always;
            
            # Manejar preflight requests para CORS
            if ($request_method = 'OPTIONS') {
                add_header Access-Control-Allow-Origin "https://localhost:4200";
                add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS";
                add_header Access-Control-Allow-Headers "DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization";
                add_header Access-Control-Max-Age 1728000;
                add_header Content-Type "text/plain; charset=utf-8";
                add_header Content-Length 0;
                return 204;
            }
            
            # Timeouts
            proxy_connect_timeout 60s;
            proxy_send_timeout 60s;
            proxy_read_timeout 60s;
        }
        
        # Configuración de proxy para otros endpoints de la API (si los hay)
        location /api/ {
            proxy_pass http://127.0.0.1:9090/api/;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection 'upgrade';
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto https;
            proxy_cache_bypass $http_upgrade;
            
            # Timeouts
            proxy_connect_timeout 60s;
            proxy_send_timeout 60s;
            proxy_read_timeout 60s;
        }
        
        # Página de bienvenida o frontend (opcional)
        location / {
            return 200 '<!DOCTYPE html>
<html>
<head>
    <title>ESIMedia Backend - HTTPS Enabled</title>
    <style>
        body { font-family: Arial, sans-serif; text-align: center; margin-top: 100px; }
        .container { max-width: 600px; margin: 0 auto; }
        .success { color: #28a745; }
        .info { color: #007bff; }
    </style>
</head>
<body>
    <div class="container">
        <h1 class="success">🔒 HTTPS Configurado Correctamente</h1>
        <p class="info">ESIMedia Backend está funcionando con HTTPS</p>
        <h3>Endpoints disponibles (HTTP → HTTPS):</h3>
        <ul style="text-align: left; display: inline-block;">
            <li><strong>POST</strong> https://localhost/users/register-standard</li>
            <li><strong>POST</strong> https://localhost/users/register-creator</li>
            <li><strong>GET</strong> https://localhost/users/pending-creators</li>
            <li><strong>PUT</strong> https://localhost/users/validate-creator/{creatorId}</li>
        </ul>
        <p>Puerto del backend: <strong>9090</strong></p>
        <hr>
        <small>Para desarrollo local - Certificado autofirmado</small>
    </div>
</body>
</html>';
            add_header Content-Type text/html;
        }
        
        # Health check
        location /health {
            return 200 "OK - HTTPS Enabled";
            add_header Content-Type text/plain;
        }
    }
    
    # Redirección HTTP a HTTPS
    server {
        listen 80;
        server_name localhost 127.0.0.1;
        
        return 301 https://$server_name$request_uri;
    }
}
```

Archivo `mime.types`:
```conf
types {
    text/html html htm shtml;
    text/css css;
    text/xml xml;
    image/gif gif;
    image/jpeg jpeg jpg;
    application/javascript js;
    application/json json;
    application/pdf pdf;
    image/png png;
    image/svg+xml svg;
}

```

> Configuración creada para el proyecto ESIMedia - Backends
> Universidad de Castilla-La Mancha - Escuela Superior de Informática