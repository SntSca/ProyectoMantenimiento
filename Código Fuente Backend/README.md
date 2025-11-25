# G07-EsiMedia-PI-User-Backend

## Pipeline de CI/CD

Este proyecto incluye configuraciones para ejecutar el pipeline de CI/CD tanto en Azure Pipelines como localmente usando Docker.

### Opción 1: Azure Pipelines (Recomendado para CI/CD)

El pipeline se ejecuta automáticamente en Azure DevOps cuando se hace push a las ramas `Development` o `releases`.

**Fases:**
1. **Build**: Compilación con Maven en Docker
2. **Test**: Ejecución de tests con Maven en Docker
3. **SonarQube**: Análisis de calidad de código en Docker (requiere VPN para SAML)

### Opción 2: Ejecución Local con Docker

Para ejecutar todo el pipeline localmente:

#### Prerrequisitos
- Docker instalado
- OpenConnect (para VPN si es necesario)
- Variables de entorno configuradas

#### Configuración
1. Copiar `.env.example` a `.env` y configurar las variables:
   ```bash
   cp .env.example .env
   # Editar .env con tus valores
   ```

2. Hacer ejecutable el script:
   ```bash
   chmod +x run-pipeline-local.sh
   ```

#### Ejecución con Script Bash
```bash
./run-pipeline-local.sh
```

#### Ejecución con Docker Compose
```bash
docker-compose up
```

**Nota:** Si SonarQube requiere VPN para SAML, asegúrate de que las credenciales VPN estén configuradas en el archivo `.env`.

### Variables de Entorno Requeridas

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `SONAR_TOKEN` | Token de autenticación para SonarQube | `sqp_...` |
| `URL_SONARQUBE` | URL del servidor SonarQube | `https://sonar.example.com` |
| `VPN_USERNAME` | Usuario para VPN (opcional) | `usuario@domain.com` |
| `VPN_PASSWORD` | Password para VPN (opcional) | `password123` |

### Dockerfile

El `Dockerfile` incluye:
- OpenJDK 17
- Maven 3.9.6
- Sonar Scanner
- Dependencias Maven descargadas offline