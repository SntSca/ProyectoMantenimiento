# Guía de Uso del ContentService Actualizado

  

  

## 🔧 **Funcionalidades Añadidas**

  

### **1. Mapeo Automático de Datos API**

- ✅ **Detección automática** de tipo (audio/video) por presencia de campos

- ✅ **Transformación** de `tags[]` → `tags` string  

- ✅ **Conversión** de fechas string → Date objects

- ✅ **Generación** de IDs únicos para contenido

- ✅ **Normalización** de campos (urlArchivo → url, etc.)

  

### **2. Interfaces para Datos del API**

```typescript

interface ApiAudioData {

  titulo: string;

  descripcion: string;

  tags: string[];            // Array en API

  duracion: number;

  fechaSubida: string;       // String en API  

  fechaExpiracion: string | null;

  esVIP: boolean;

  miniatura: string;

  formatoMiniatura: string;

  fichero: string;           // Campo específico de audio

  ficheroExtension: string;

  restriccionEdad?: number;

}

  

interface ApiVideoData {

  // ... campos comunes ...

  urlArchivo: string;        // Mapeado a 'url' en Videos

  resolucion: string;        // Campo específico de video

  restriccionEdad?: number;

}

```

  

### **3. Funciones de Mapeo**

- `mapApiDataToEntities()` - Mapea array de API a entidades

- `mapToVideo()` - Convierte datos API a entidad Videos

- `mapToAudio()` - Convierte datos API a entidad Audios  

- `separateContent()` - Separa contenido en videos y audios

  

## 🚀 **Cómo Usar en Componentes**

  

### **Ejemplo: Cargar Todo el Contenido**

```typescript

export class MiComponente implements OnInit {

  contenidos: Contenido[] = [];

  videos: Videos[] = [];

  audios: Audios[] = [];

  loading = false;

  

  constructor(private contentService: ContentService) {}

  

  ngOnInit() {

    this.loadContent();

  }

  

  loadContent() {

    this.loading = true;

    // Opción 1: Contenido combinado

    this.contentService.getContentCombined()

      .subscribe({

        next: (data) => {

          this.videos = data.videos;     // Videos mapeados

          this.audios = data.audios;     // Audios mapeados  

          this.contenidos = data.all;    // Lista combinada

          this.loading = false;

          console.log('✅ Contenido cargado:', data);

        },

        error: (error) => {

          console.error('❌ Error:', error);

          this.loading = false;

        }

      });

  }

}

```

  

### **Ejemplo: Solo Videos o Solo Audios**

```typescript

// Solo videos

this.contentService.getAllVideos()

  .subscribe(videos => {

    this.videos = videos; // Ya mapeados a entidad Videos

  });

  

// Solo audios  

this.contentService.getAllAudios()

  .subscribe(audios => {

    this.audios = audios; // Ya mapeados a entidad Audios

  });

```

  

## 📊 **Mapeo de Campos**

  

| Campo API | Campo Interface | Transformación |

|-----------|----------------|----------------|

| `tags: string[]` | `tags: string` | `array.join(', ')` |

| `fechaSubida: string` | `fechaSubida: Date` | `new Date(string)` |

| `urlArchivo` | `url` | Directo |

| `fichero` | `fichero` | Directo |

| `restriccionEdad` | `restriccionEdad` | Default: 3 |

| - | `id` | Generado: `content-${timestamp}-${index}` |

| - | `tipo` | Detectado: `'audio' \| 'video'` |

  

## ✅ **Respuesta de Ejemplo Procesada**

  

**Entrada (API):**

```json

{

  "titulo": "Mi audio de prueba",

  "tags": ["musica", "podcast"],

  "fechaSubida": "2025-10-17",

  "fichero": "data:audio/wav;base64,..."

}

```

  

**Salida (Entidad):**

```typescript

{

  id: "content-1729180000000-0",

  titulo: "Mi audio de prueba",

  tags: "musica, podcast",

  fechaSubida: Date(2025-10-17),

  tipo: "audio",

  fichero: "data:audio/wav;base64,...",

  // ... resto de campos mapeados

}

```

  

## 🔄 **Fallback a Mocks**

  

Si el API falla, automáticamente usa los mocks existentes:

```typescript

catchError((error) => {

  console.warn('Error al obtener contenido desde API, usando datos mock:', error);

  return of([...MOCK_VIDEOS, ...MOCK_AUDIOS]);

})

```

  

## 🎯 **Estado Actual**

  

- ✅ **ContentService** actualizado con mapeo completo

- ✅ **Detección automática** de tipo audio/video

- ✅ **Mapeo de todos los campos** según interfaces existentes

- ✅ **Fallback robusto** a mocks si API falla

- ✅ **TypeScript** compilando sin errores

  

Los componentes existentes (`inicio-usuario`, `inicio-gestor`, `inicio-administrador`, `visualizar-contenido`) ya están configurados para usar este servicio y deberían mostrar los datos del API correctamente mapeados.

  

## 🧪 **Para Probar**

  

1. **Ejecutar aplicación**: `ng serve`

2. **Abrir consola** del navegador  

3. **Navegar** a inicio de usuario/gestor

4. **Verificar logs**: Debe mostrar "✅ Contenido cargado desde API"

5. **Inspeccionar datos**: Los contenidos deben tener IDs, tipos y campos correctos