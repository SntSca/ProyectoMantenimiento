import { Injectable } from '@angular/core';
import { 
  FIELD_ERROR_MESSAGES, 
  GENERIC_ERROR_MESSAGES, 
  SUCCESS_MESSAGES,
  CONFIRMATION_MESSAGES,
  INFO_MESSAGES,
  FILE_MESSAGES,
  FieldType, 
  ValidationErrorType 
} from '../constants/messages';

@Injectable({
  providedIn: 'root'
})
export class MessagesService {

  /**
   * Obtiene el mensaje de error para un campo y tipo de error específico
   */
   getFieldErrorMessage(
    fieldType: FieldType | string, 
    errorType: ValidationErrorType | string, 
    errorData?: any
  ): string {
    // Convertir string a enum si es necesario
    const field = fieldType as keyof typeof FIELD_ERROR_MESSAGES;
    const error = errorType as keyof typeof FIELD_ERROR_MESSAGES[typeof field];

    // Buscar mensaje específico para el campo
    const fieldMessages = FIELD_ERROR_MESSAGES[field];
    if (fieldMessages?.[error]) {
      let message = fieldMessages[error] as string;

      // Reemplazar placeholders con datos reales
      if (errorData) {
        message = this.replacePlaceholders(message, errorData);
      }

      return message;
    }

    // Mensaje genérico si no se encuentra uno específico
    return GENERIC_ERROR_MESSAGES.FIELD_INVALID;
  }

  /**
   * Obtiene el mensaje de error basado en los errores de Angular Forms
   */
  getAngularFormErrorMessage(fieldType: FieldType | string, errors: any): string {
    if (!errors) return '';

    // Mapear errores de Angular Forms a nuestros tipos
    const errorMappings = [
      { angularError: 'required', ourError: ValidationErrorType.REQUIRED },
      { angularError: 'email', ourError: ValidationErrorType.EMAIL },
      { angularError: 'pattern', ourError: ValidationErrorType.PATTERN },
      { angularError: 'minlength', ourError: ValidationErrorType.MINLENGTH },
      { angularError: 'maxlength', ourError: ValidationErrorType.MAXLENGTH },
      { angularError: 'min', ourError: ValidationErrorType.MIN },
      { angularError: 'max', ourError: ValidationErrorType.MAX },
      { angularError: 'mismatch', ourError: ValidationErrorType.MISMATCH },
      { angularError: 'noUppercase', ourError: ValidationErrorType.NO_UPPERCASE },
      { angularError: 'noLowercase', ourError: ValidationErrorType.NO_LOWERCASE },
      { angularError: 'noNumber', ourError: ValidationErrorType.NO_NUMBER },
      { angularError: 'noSpecialChar', ourError: ValidationErrorType.NO_SPECIAL_CHAR },
      { angularError: 'existingEmail', ourError: ValidationErrorType.EXISTING_EMAIL },
      { angularError: 'existingAlias', ourError: ValidationErrorType.EXISTING_ALIAS },
      { angularError: 'invalidDate', ourError: ValidationErrorType.INVALID_DATE },
      { angularError: 'futureDate', ourError: ValidationErrorType.FUTURE_DATE },
      { angularError: 'tooYoung', ourError: ValidationErrorType.TOO_YOUNG },
      { angularError: 'tooOld', ourError: ValidationErrorType.TOO_OLD },
      { angularError: 'fileInvalidType', ourError: ValidationErrorType.FILE_INVALID_TYPE },
      { angularError: 'fileTooLarge', ourError: ValidationErrorType.FILE_TOO_LARGE },
      { angularError: 'fileNoFile', ourError: ValidationErrorType.FILE_NO_FILE },
      { angularError: 'fileReadError', ourError: ValidationErrorType.FILE_READ_ERROR },
      { angularError: 'imageInvalidType', ourError: ValidationErrorType.IMAGE_INVALID_TYPE },
      { angularError: 'imageTooLarge', ourError: ValidationErrorType.IMAGE_TOO_LARGE },
      { angularError: 'audioInvalidType', ourError: ValidationErrorType.AUDIO_INVALID_TYPE },
      { angularError: 'audioTooLarge', ourError: ValidationErrorType.AUDIO_TOO_LARGE },
      { angularError: 'invalidVideoUrl', ourError: ValidationErrorType.INVALID_VIDEO_URL },
    ];

    // Buscar el primer error que coincida
    for (const mapping of errorMappings) {
      if (errors[mapping.angularError]) {
        return this.getFieldErrorMessage(
          fieldType, 
          mapping.ourError, 
          errors[mapping.angularError]
        );
      }
    }

    return GENERIC_ERROR_MESSAGES.FIELD_INVALID;
  }

  /**
   * Obtiene mensajes de éxito
   */
  getSuccessMessage(key: keyof typeof SUCCESS_MESSAGES): string {
    return SUCCESS_MESSAGES[key];
  }

  /**
   * Obtiene mensajes de confirmación
   */
  getConfirmationMessage(key: keyof typeof CONFIRMATION_MESSAGES): string {
    return CONFIRMATION_MESSAGES[key];
  }


  /**
   * Reemplaza placeholders en los mensajes con datos reales
   */
  private replacePlaceholders(message: string, data: any): string {
    let result = message;
    
    // Reemplazar placeholders comunes
    if (data.requiredLength !== undefined) {
      result = result.replace('{min}', data.requiredLength.toString());
      result = result.replace('{max}', data.requiredLength.toString());
    }
    
    if (data.actualLength !== undefined) {
      result = result.replace('{actual}', data.actualLength.toString());
    }

    if (data.min !== undefined) {
      result = result.replace('{min}', data.min.toString());
    }

    if (data.max !== undefined) {
      result = result.replace('{max}', data.max.toString());
    }

    return result;
  }

  /**
   * Método de conveniencia para campos de login
   */
  getLoginFieldError(fieldName: 'email' | 'password', errors: any): string {
    const fieldType = fieldName === 'email' ? FieldType.EMAIL : FieldType.PASSWORD;
    return this.getAngularFormErrorMessage(fieldType, errors);
  }

  /**
   * Método de conveniencia para campos de registro
   */
  getRegistrationFieldError(fieldName: string, errors: any): string {
    // Mapear nombres de campos del formulario a nuestros tipos
    const fieldMapping: Record<string, FieldType> = {
      'email': FieldType.EMAIL,
      'password': FieldType.PASSWORD,
      'confirmarPassword': FieldType.CONFIRMPASSWORD,
      'currentPassword': FieldType.CURRENTPASSWORD,
      'newPassword': FieldType.PASSWORD,
      'confirmPassword': FieldType.CONFIRMPASSWORD,
      'nombre': FieldType.NAME,
      'primerApellido': FieldType.FIRSTNAME,
      'segundoApellido': FieldType.LASTNAME,
      'alias': FieldType.ALIAS,
      'fechaNacimiento': FieldType.BIRTHDATE,
      'especialidad': FieldType.SPECIALITY,
      'descripcion': FieldType.DESCRIPTION,
      'telefono': FieldType.PHONE,
      'direccion': FieldType.ADDRESS,
      'fotoPerfil': FieldType.PHOTO,
      'departamento': FieldType.DEPARTMENT,
      'tipoContenido': FieldType.CONTENT_TYPE,
      'titulo': FieldType.TITULO,
      'tags': FieldType.TAGS,
      'duracion': FieldType.DURACION,
      'pegi': FieldType.PEGI,
      'formatoImagen': FieldType.FORMATO_IMAGEN,
      'url': FieldType.URL,
      'resolucion': FieldType.RESOLUCION,
      'vip': FieldType.VIP,
      'fechaExpiracion': FieldType.FECHA_EXPIRACION,
      'imagenMiniatura': FieldType.IMAGEN_MINIATURA,
      'archivoAudio': FieldType.ARCHIVO_AUDIO,
    };

    const fieldType = fieldMapping[fieldName] || fieldName as FieldType;
    return this.getAngularFormErrorMessage(fieldType, errors);
  }

  /**
   * Obtiene los requisitos de contraseña para mostrar como ayuda
   */
  getPasswordRequirements(): readonly string[] {
    return INFO_MESSAGES.PASSWORD_REQUIREMENTS;
  }

  /**
   * Obtiene mensajes de archivos
   */
  getFileMessage(key: keyof typeof FILE_MESSAGES, data?: { max?: number }): string {
    let message: string = FILE_MESSAGES[key];
    
    // Reemplazar placeholders si es necesario
    if (data) {
      message = this.replacePlaceholders(message, data);
    }
    
    return message;
  }
}