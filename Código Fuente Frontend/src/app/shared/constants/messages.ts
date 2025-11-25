/**
 * ===========================
 * MENSAJES DE VALIDACIÓN CENTRALIZADOS
 * ===========================
 * 
 * Este archivo contiene todos los mensajes de error y validación
 * de la aplicación para asegurar consistencia y facilitar
 * el mantenimiento y la internacionalización futura.
 */

// ===========================
// TIPOS DE CAMPOS
// ===========================
import { IMAGENES_PERMITIDAS, FICHEROS_PERMITIDOS } from './interfaces';


export enum FieldType {
  EMAIL = 'email',
  PASSWORD = 'password',
  NAME = 'nombre',
  FIRSTNAME = 'primerApellido',
  LASTNAME = 'segundoApellido',
  ALIAS = 'alias',
  BIRTHDATE = 'fechaNacimiento',
  SPECIALITY = 'especialidad',
  DESCRIPTION = 'descripcion',
  PHONE = 'telefono',
  ADDRESS = 'direccion',
  CONFIRMPASSWORD = 'confirmarPassword',
  CURRENTPASSWORD = 'passwordActual',
  PHOTO = 'fotoPerfil',
  CONTENT_TYPE = 'tipoContenido',
  DEPARTMENT = 'departamento',
  TITULO = 'titulo',
  TAGS = 'tags',
  DURACION = 'duracion',
  PEGI = 'pegi',
  FORMATO_IMAGEN = 'formatoImagen',
  URL = 'url',
  RESOLUCION = 'resolucion',
  VIP = 'vip',
  FECHA_EXPIRACION = 'fechaExpiracion',
  IMAGEN_MINIATURA = 'imagenMiniatura',
  ARCHIVO_AUDIO = 'archivoAudio',
  ARCHIVO_VIDEO = 'archivoVideo'
}



export enum ValidationErrorType {
  REQUIRED = 'required',
  EMAIL = 'email',
  PATTERN = 'pattern',
  MINLENGTH = 'minlength',
  MAXLENGTH = 'maxlength',
  MIN = 'min',
  MAX = 'max',
  MISMATCH = 'mismatch',
  INVALID_FORMAT = 'invalidFormat',
  WEAK_PASSWORD = 'weakPassword',
  EXISTING_EMAIL = 'existingEmail',
  EXISTING_ALIAS = 'existingAlias',
  INVALID_DATE = 'invalidDate',
  FUTURE_DATE = 'futureDate',
  TOO_YOUNG = 'tooYoung',
  TOO_OLD = 'tooOld',
  NO_UPPERCASE = 'noUppercase',
  NO_LOWERCASE = 'noLowercase',
  NO_NUMBER = 'noNumber',
  NO_SPECIAL_CHAR = 'noSpecialChar',
  FILE_INVALID_TYPE = 'fileInvalidType',
  FILE_TOO_LARGE = 'fileTooLarge',
  FILE_NO_FILE = 'fileNoFile',
  FILE_READ_ERROR = 'fileReadError',
  IMAGE_INVALID_TYPE = 'imageInvalidType',
  IMAGE_TOO_LARGE = 'imageTooLarge',
  AUDIO_INVALID_TYPE = 'audioInvalidType',
  AUDIO_TOO_LARGE = 'audioTooLarge',
  INVALID_VIDEO_URL = 'invalidVideoUrl',
  NOT_FUTURE_DATE = 'notFutureDate',
  RESOLUTION_4K_NOT_ALLOWED = 'resolution4kNotAllowed'
}

// ===========================
// MENSAJES DE ERROR POR CAMPO
// ===========================
export const FIELD_ERROR_MESSAGES = {
  [FieldType.EMAIL]: {
    [ValidationErrorType.REQUIRED]: 'El correo electrónico es obligatorio',
    [ValidationErrorType.EMAIL]: 'Por favor ingresa un correo electrónico válido',
    [ValidationErrorType.PATTERN]: 'El formato del correo electrónico no es válido',
    [ValidationErrorType.EXISTING_EMAIL]: 'Este correo electrónico ya está registrado'
  },
  
  [FieldType.PASSWORD]: {
    [ValidationErrorType.REQUIRED]: 'La contraseña es obligatoria',
    [ValidationErrorType.MINLENGTH]: 'La contraseña debe tener al menos 12 caracteres',
    [ValidationErrorType.MAXLENGTH]: 'La contraseña no puede exceder {max} caracteres',
    [ValidationErrorType.WEAK_PASSWORD]: 'La contraseña debe contener al menos una mayúscula, una minúscula y un número',
    [ValidationErrorType.NO_UPPERCASE]: 'La contraseña debe contener al menos una letra mayúscula',
    [ValidationErrorType.NO_LOWERCASE]: 'La contraseña debe contener al menos una letra minúscula',
    [ValidationErrorType.NO_NUMBER]: 'La contraseña debe contener al menos un número',
    [ValidationErrorType.NO_SPECIAL_CHAR]: 'La contraseña debe contener al menos un símbolo especial'
  },

  [FieldType.CONFIRMPASSWORD]: {
    [ValidationErrorType.REQUIRED]: 'Confirma tu contraseña',
    [ValidationErrorType.MISMATCH]: 'Las contraseñas no coinciden'
  },

  [FieldType.CURRENTPASSWORD]: {
    [ValidationErrorType.REQUIRED]: 'Ingresa tu contraseña actual',
    [ValidationErrorType.INVALID_FORMAT]: 'La contraseña actual es incorrecta'
  },

  [FieldType.NAME]: {
    [ValidationErrorType.REQUIRED]: 'El nombre es obligatorio',
    [ValidationErrorType.MINLENGTH]: 'El nombre debe tener al menos {min} caracteres',
    [ValidationErrorType.MAXLENGTH]: 'El nombre no puede exceder {max} caracteres',
    [ValidationErrorType.PATTERN]: 'El nombre solo puede contener letras y espacios'
  },

  [FieldType.FIRSTNAME]: {
    [ValidationErrorType.REQUIRED]: 'El primer apellido es obligatorio',
    [ValidationErrorType.MINLENGTH]: 'El primer apellido debe tener al menos {min} caracteres',
    [ValidationErrorType.MAXLENGTH]: 'El primer apellido no puede exceder {max} caracteres',
    [ValidationErrorType.PATTERN]: 'El apellido solo puede contener letras y espacios'
  },

  [FieldType.LASTNAME]: {
    [ValidationErrorType.REQUIRED]: 'El segundo apellido es obligatorio',
    [ValidationErrorType.MINLENGTH]: 'El segundo apellido debe tener al menos {min} caracteres',
    [ValidationErrorType.MAXLENGTH]: 'El segundo apellido no puede exceder {max} caracteres',
    [ValidationErrorType.PATTERN]: 'El apellido solo puede contener letras y espacios'
  },

  [FieldType.ALIAS]: {
    [ValidationErrorType.REQUIRED]: 'El alias es obligatorio',
    [ValidationErrorType.MINLENGTH]: 'El alias debe tener al menos {min} caracteres',
    [ValidationErrorType.MAXLENGTH]: 'El alias no puede exceder 12 caracteres',
    [ValidationErrorType.PATTERN]: 'El alias solo puede contener letras, números y guiones bajos',
    [ValidationErrorType.EXISTING_ALIAS]: 'Este alias ya está en uso. Por favor elige otro'
  },

  [FieldType.BIRTHDATE]: {
    [ValidationErrorType.REQUIRED]: 'La fecha de nacimiento es obligatoria',
    [ValidationErrorType.INVALID_DATE]: 'Por favor ingresa una fecha válida',
    [ValidationErrorType.PATTERN]: 'El formato de fecha debe ser YYYY-MM-DD',
    [ValidationErrorType.FUTURE_DATE]: 'La fecha de nacimiento no puede ser futura',
    [ValidationErrorType.TOO_YOUNG]: 'No se permite el registro de usuarios menores de 4 años',
    [ValidationErrorType.TOO_OLD]: 'La fecha de nacimiento no puede ser anterior al año 1900',
    [ValidationErrorType.MIN]: 'Debes ser mayor de edad para registrarte'
  },

  [FieldType.SPECIALITY]: {
    [ValidationErrorType.REQUIRED]: 'La especialidad es obligatoria',
    [ValidationErrorType.MINLENGTH]: 'La especialidad debe tener al menos {min} caracteres',
    [ValidationErrorType.MAXLENGTH]: 'La especialidad no puede exceder {max} caracteres'
  },

  [FieldType.CONTENT_TYPE]: {
    [ValidationErrorType.REQUIRED]: 'Debes seleccionar el tipo de contenido que gestionarás.'
  },

  [FieldType.DESCRIPTION]: {
    [ValidationErrorType.REQUIRED]: 'La descripción es obligatoria',
    [ValidationErrorType.MINLENGTH]: 'La descripción debe tener al menos {min} caracteres',
    [ValidationErrorType.MAXLENGTH]: 'La descripción no puede exceder {max} caracteres'
  },

  [FieldType.PHOTO]: {
    [ValidationErrorType.FILE_INVALID_TYPE]: 'Por favor, selecciona un archivo de imagen válido (JPG, PNG, GIF, etc.)',
    [ValidationErrorType.FILE_TOO_LARGE]: 'El archivo es demasiado grande. Máximo {max}MB permitido',
    [ValidationErrorType.FILE_NO_FILE]: 'No se seleccionó ningún archivo',
    [ValidationErrorType.FILE_READ_ERROR]: 'Error al procesar el archivo de imagen'
  },
  [FieldType.DEPARTMENT]: {
    [ValidationErrorType.REQUIRED]: 'El departamento es obligatorio'
  },
  [FieldType.TITULO]: {
    [ValidationErrorType.REQUIRED]: 'El título es obligatorio',
    [ValidationErrorType.MINLENGTH]: 'El título debe tener al menos {min} caracteres',
    [ValidationErrorType.MAXLENGTH]: 'El título no puede exceder {max} caracteres'
  },

  [FieldType.TAGS]: {
    [ValidationErrorType.REQUIRED]: 'Debes seleccionar al menos un tag'
  },

  [FieldType.DURACION]: {
    [ValidationErrorType.REQUIRED]: 'La duración es obligatoria',
    [ValidationErrorType.MIN]: 'La duración debe ser mayor a {min} minuto(s)'
  },

  [FieldType.PEGI]: {
    [ValidationErrorType.REQUIRED]: 'Debes seleccionar una clasificación PEGI',
    [ValidationErrorType.MIN]: 'La clasificación PEGI debe ser al menos {min}',
    [ValidationErrorType.MAX]: 'La clasificación PEGI no puede exceder {max}'
  },

  [FieldType.FORMATO_IMAGEN]: {
    [ValidationErrorType.REQUIRED]: 'Debes seleccionar un formato de imagen'
  },

  [FieldType.URL]: {
    [ValidationErrorType.REQUIRED]: 'La URL del video es obligatoria',
    [ValidationErrorType.PATTERN]: 'La URL debe ser una dirección web válida (http:// o https://)',
    [ValidationErrorType.INVALID_VIDEO_URL]: 'La URL debe ser de YouTube o Vimeo'
  },

  [FieldType.RESOLUCION]: {
    [ValidationErrorType.REQUIRED]: 'Debes seleccionar una resolución',
    [ValidationErrorType.RESOLUTION_4K_NOT_ALLOWED]: 'La resolución 4K solo está disponible para contenido VIP'
  },

  [FieldType.VIP]: {
    // No requiere validación específica
  },

  [FieldType.FECHA_EXPIRACION]: {
    [ValidationErrorType.NOT_FUTURE_DATE]: 'La fecha de expiración debe ser una fecha futura',
  },

  [FieldType.IMAGEN_MINIATURA]: {
    [ValidationErrorType.IMAGE_INVALID_TYPE]: `Por favor selecciona un archivo de imagen válido (${IMAGENES_PERMITIDAS.extensiones.map(ext => ext.toUpperCase()).join(', ')})`,
    [ValidationErrorType.IMAGE_TOO_LARGE]: `La imagen es demasiado grande. Máximo ${IMAGENES_PERMITIDAS.tamanoMaximo / (1024 * 1024)}MB permitido`
  },

  [FieldType.ARCHIVO_AUDIO]: {
    [ValidationErrorType.AUDIO_INVALID_TYPE]: `Por favor selecciona un archivo válido (${FICHEROS_PERMITIDOS.extensiones.map(ext => ext.toUpperCase()).join(', ')})`,
    [ValidationErrorType.AUDIO_TOO_LARGE]: `El archivo es demasiado grande. Máximo ${FICHEROS_PERMITIDOS.tamanoMaximo / (1024 * 1024)}MB permitido`
  },

} as const;

// ===========================
// MENSAJES GENÉRICOS
// ===========================
export const GENERIC_ERROR_MESSAGES = {
  FIELD_INVALID: 'Este campo no es válido',
  REQUIRED_FIELD: 'Este campo es obligatorio',
  INVALID_FORMAT: 'El formato no es válido',
  CONNECTION_ERROR: 'Error de conexión. Por favor intenta nuevamente',
  SERVER_ERROR: 'Error del servidor. Por favor intenta más tarde',
  UNAUTHORIZED: 'No tienes permisos para realizar esta acción',
  SESSION_EXPIRED: 'Tu sesión ha expirado. Por favor inicia sesión nuevamente'
} as const;

// ===========================
// MENSAJES DE ÉXITO
// ===========================
export const SUCCESS_MESSAGES = {
  LOGIN_SUCCESS: 'Inicio de sesión exitoso',
  REGISTER_SUCCESS: 'Registro exitoso. ¡Bienvenido!',
  PROFILE_UPDATED: 'Perfil actualizado correctamente',
  PASSWORD_CHANGED: 'Contraseña cambiada exitosamente',
  EMAIL_SENT: 'Correo electrónico enviado correctamente',
  DATA_SAVED: 'Datos guardados correctamente'
} as const;

// ===========================
// MENSAJES INFORMATIVOS
// ===========================
export const INFO_MESSAGES = {
  PASSWORD_REQUIREMENTS: [
    'Al menos 12 caracteres',
    'Al menos 1 letra mayúscula',
    'Al menos 1 letra minúscula', 
    'Al menos 1 número',
    'Al menos 1 símbolo especial'
  ]
} as const;

// ===========================
// MENSAJES DE ARCHIVOS
// ===========================
export const FILE_MESSAGES = {
  SUCCESS: 'Imagen cargada correctamente',
  ERROR_INVALID_TYPE: 'Por favor, selecciona un archivo de imagen válido (JPG, PNG, GIF, etc.)',
  ERROR_TOO_LARGE: 'El archivo es demasiado grande. Máximo {max}MB permitido',
  ERROR_NO_FILE: 'No se seleccionó ningún archivo',
  ERROR_READ_ERROR: 'Error al procesar el archivo de imagen'
} as const;

// ===========================
// MENSAJES DE CONFIRMACIÓN
// ===========================
export const CONFIRMATION_MESSAGES = {
  DELETE_ACCOUNT: '¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer',
  LOGOUT: '¿Estás seguro de que deseas cerrar sesión?',
  CANCEL_VIP: '¿Deseas cancelar tu suscripción VIP?',
  DELETE_VIDEO: '¿Estás seguro de que deseas eliminar este video?',
  UNSAVED_CHANGES: 'Tienes cambios sin guardar. ¿Deseas salir sin guardar?'
} as const;

// ===========================
// HELPER TYPES
// ===========================
export type FieldMessages = Record<ValidationErrorType, string>;
export type AllFieldMessages = Record<FieldType, Partial<FieldMessages>>;