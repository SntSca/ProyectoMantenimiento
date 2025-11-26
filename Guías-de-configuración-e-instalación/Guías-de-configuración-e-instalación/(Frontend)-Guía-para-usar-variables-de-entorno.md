En el caso de que necesites usar alguna variable de entorno como constante (por ejemplo las URL que llamen al backend), necesitarás tener en cuenta la siguiente guía:


# Configuración de API Endpoints

  

Este sistema permite manejar las URLs de los endpoints de manera centralizada y configurable, evitando hardcodear URLs en el código fuente.

  

## Estructura de Configuración

  

### 1. Archivos de Environment

  

Los archivos de environment (`src/environments/`) contienen la configuración base:

  

- `environment.ts` - Configuración de desarrollo

- `environment.prod.ts` - Configuración de producción

  

### 2. Servicios de Configuración

  

- **`ApiConfigService`** - Maneja las URLs de los endpoints

- **`ConfigService`** - Configuración general de la aplicación

- **`ContentService`** - Servicio para endpoints de contenido

- **`GestorService`** - Servicio para operaciones de gestores/creadores

  

## Endpoints Disponibles

  

### Usuarios (/users)

- `registerStandard` - POST /users/register-standard

- `registerCreator` - POST /users/register-creator

- `registerAdmin` - POST /users/register-admin

- `login` - POST /users/login

- `privilegedLogin` - POST /users/privileged-login

- `confirm/{tokenId}` - GET /users/confirm/{tokenId}

- `forgotPassword` - POST /users/forgot-password

- `forgotPasswordPrivileged` - POST /users/forgot-password-privileged

- `resetPassword` - POST /users/reset-password

- `validateResetToken/{token}` - GET /users/validate-reset-token/{token}

- `pendingCreators` - GET /users/pending-creators

- `validateCreator/{creatorId}` - PUT /users/validate-creator/{creatorId}

- `normalUsers` - GET /users/normal-users

- `administrators` - GET /users/administrators

- `contentCreators` - GET /users/content-creators

- `cleanupTokens` - POST /users/cleanup-tokens

  

### Contenido (/content)

- `uploadAudio` - POST /content/upload-audio

- `uploadVideo` - POST /content/upload-video

- `getAudio/{id}` - GET /content/getAudio/{id}

- `getVideo/{id}` - GET /content/getVideo/{id}

- `getAllContent` - GET /content/getAllContent

- `getAllAudios` - GET /content/getAllAudios

- `getAllVideos` - GET /content/getAllVideos

  

## Uso en Componentes

  

### Ejemplo básico con ApiConfigService

  

```typescript

import { ApiConfigService } from '../shared/services/api-config.service';

  

constructor(

  private http: HttpClient,

  private apiConfig: ApiConfigService

) {}

  

// Uso directo

login(credentials: any) {

  const url = this.apiConfig.getUsersUrl('login');

  return this.http.post(url, credentials);

}

  

// Con parámetros dinámicos

confirmAccount(tokenId: string) {

  const url = this.apiConfig.getUserConfirmUrl(tokenId);

  return this.http.get(url);

}

```

  

### Ejemplo con servicios especializados

  

```typescript

import { GestorService } from '../personal/gestor/gestor.service';

import { ContentService } from '../shared/services/content.service';

  

// Subida de archivo con progreso

uploadAudio(file: File) {

  return this.gestorService.uploadAudio(file, { title: 'Mi audio' })

    .subscribe(event => {

      if (event.type === HttpEventType.UploadProgress) {

        const progress = Math.round(100 * event.loaded / event.total!);

        console.log(\`Upload progress: \${progress}%\`);

      } else if (event.type === HttpEventType.Response) {

        console.log('Upload completed:', event.body);

      }

    });

}

```

  

## Configuración por Entorno

  

### Desarrollo

```typescript

// src/environments/environment.ts

export const environment = {

  production: false,

  apiBaseUrl: 'https://localhost',

  // ... endpoints

};

```

  

### Producción

```typescript

// src/environments/environment.prod.ts

export const environment = {

  production: true,

  apiBaseUrl: 'https://api.esimedia.com',

  // ... endpoints

};

```

  

### Configuración Dinámica

  

Para cambiar URLs en tiempo de ejecución sin recompilar:

  

```typescript

// En el servidor, inyectar configuración en index.html

<script>

  window.APP_CONFIG = {

    apiBaseUrl: 'https://api-custom.example.com'

  };

</script>

  

// O usar localStorage

localStorage.setItem('appConfig', JSON.stringify({

  apiBaseUrl: 'https://api-staging.example.com'

}));

```

  

## Autenticación

  

Los servicios manejan automáticamente los headers de autenticación:

  

```typescript

// El token se obtiene automáticamente de localStorage

// Claves soportadas: 'authToken' o 'token'

localStorage.setItem('authToken', 'your-jwt-token');

  

// Los servicios incluirán automáticamente:

// Authorization: Bearer your-jwt-token

```

  

## Validación de Archivos

  

```typescript

import { ConfigService } from '../shared/services/config.service';

  

// Validar tipo de archivo

if (!this.configService.isFileTypeAllowed(file.type, 'audio')) {

  console.error('Tipo de archivo no permitido');

}

  

// Validar tamaño

if (!this.configService.isFileSizeAllowed(file.size)) {

  console.error('Archivo demasiado grande');

}

```

  

## Personalización

  

### Agregar nuevos endpoints

  

1. Actualizar `environment.ts` y `environment.prod.ts`:

  

```typescript

endpoints: {

  users: {

    // ... existentes

    newEndpoint: '/users/new-endpoint'

  }

}

```

  

2. Actualizar `ApiConfigService` si es necesario

3. Usar en servicios o componentes

  

### Cambiar configuración de archivos

  

```typescript

// En ConfigService constructor o método de inicialización

this.configService.updateConfig({

  maxFileSizeMB: 200,

  allowedAudioTypes: ['audio/mpeg', 'audio/wav', 'audio/flac']

});

```

  

## Estructura de Archivos

  

```

src/

├── environments/

│   ├── environment.ts

│   └── environment.prod.ts

├── app/

│   ├── shared/

│   │   └── services/

│   │       ├── api-config.service.ts

│   │       ├── config.service.ts

│   │       └── content.service.ts

│   ├── personal/

│   │   └── gestor/

│   │       └── gestor.service.ts

│   └── usuario/

│       └── usuario.service.ts

└── .env.example

```