// Interfaces para los modelos principales de la app

export interface UserStatus {
  idUsuario: string;
  tipo: 'usuario';
  nombre: string;
  primerApellido: string;
  segundoApellido?: string;
  email: string;
  alias: string;
  fechaNacimiento: string;
  flagVIP: boolean;
  fotoPerfil: string | null;
  bloqueado: boolean;
  twoFactorEnabled: boolean;
  thirdFactorEnabled: boolean;
}

export interface GestorStatus {
  idUsuario: string;
  tipo: 'gestor';
  nombre: string;
  primerApellido: string;
  segundoApellido?: string;
  email: string;
  alias: string;
  tipoContenido: string;
  especialidad: string [];
  descripcion: string;
  fotoPerfil: string | null;
  bloqueado: boolean;
  validado: boolean;
  twoFactorEnabled: boolean;
  thirdFactorEnabled: boolean;
}


export interface AdministradorStatus {
  idUsuario: string;
  tipo: 'administrador';
  nombre: string;
  primerApellido: string;
  segundoApellido?: string;
  email: string;
  alias: string;
  fotoPerfil: string | null;
  departamento: string;
  bloqueado: boolean;
  twoFactorEnabled: boolean;
  thirdFactorEnabled: boolean;
}

export interface Contenido{
  id: string | null;
  titulo: string;
  descripcion: string;
  tags: string[];
  duracion: number;
  duracionFormateada?: string;
  esVIP: boolean;
  fechaSubida: Date |null;
  fechaExpiracion: Date | null;
  restriccionEdad: number;
  miniatura: string;
  formatoMiniatura: string;
  visibilidad: boolean;
  valoracionMedia: number;
  especialidad: string;
  tipo: 'VIDEO' | 'AUDIO';
  visualizaciones: number;
  valoracionUsuario: number;
}

export interface Audios extends Contenido {
  fichero: any;
  ficheroExtension: string;
}

export interface Videos extends Contenido {
  urlArchivo: string;
  resolucion: string;
}

export interface ListaPublica{
  idLista: string;
  nombre: string;
  descripcion: string;
  idCreadorUsuario: string;
  contenidos?: any[];
  visibilidad: boolean;
}

export interface ListaPrivada {
  idLista: string;
  nombre: string;
  descripcion: string;
  idCreadorUsuario: string;
  contenidos?: any[];
}



export interface Especialidades {
  id: number;
  nombre: string;
}

export interface Tags {
  id: number;
  nombre: string;
}

// Configuraciones de archivos permitidos
export const IMAGENES_PERMITIDAS = {
  extensiones: ['jpg', 'jpeg', 'png', 'webp'],
  tamanoMaximo: 1024 * 1024 // 1MB en bytes
};

export const FICHEROS_PERMITIDOS = {
  extensiones: ['mp3', 'wav', 'flac', 'aac', 'ogg', 'mp4', 'mkv', 'avi', 'mov', ],
  tamanoMaximo: 1024 * 1024 // 1MB en bytes
};
export interface ResolucionOption { label: string; value: string }
export const RESOLUCIONES_PERMITIDAS: ResolucionOption[] = [
  { label: '4K', value: '4K' },
  { label: '1080p', value: '1080' },
  { label: '720p', value: '720' }
];
export const PEGI_PERMITIDO: (3| 7 | 12 | 16 | 18)[] = [3, 7, 12, 16, 18];


export const ESPECIALIDADES_PERMITIDAS: Especialidades[] = [
  { id: 0, nombre: 'Tecnología' },
  { id: 1, nombre: 'Cocina' },
  { id: 2, nombre: 'Viajes' },
  { id: 3, nombre: 'Salud y Bienestar' },
  { id: 4, nombre: 'Arte y Diseño' },
  { id: 5, nombre: 'Negocios y Emprendimiento' },
  { id: 6, nombre: 'Educación y Aprendizaje' },
  { id: 7, nombre: 'Deporte y Fitness' },
  { id: 8, nombre: 'Moda y Belleza' },
  { id: 9, nombre: 'Cine, Música y Entretenimiento' }
];

export const TIPO_CONTENIDOS = [
  'AUDIO',
  'VIDEO'
];

export const DEPARTAMENTOS = [
  'Recursos Humanos',
  'Marketing',
  'Desarrollo',
  'Ventas',
  'Atención al Cliente',
  'Finanzas',
  'Operaciones',
  'Legal'
];

/**
 * Interface para la respuesta del endpoint getAllUsers del backend
 */
export interface GetAllUsersResponse {
  normalUsers: UserStatus[];
  administrators: AdministradorStatus[];
  contentCreators: GestorStatus[];
}