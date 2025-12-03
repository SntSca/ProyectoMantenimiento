import { Injectable } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { MessagesService } from './messages.service';
import { FieldType, ValidationErrorType } from '../constants/messages';
import { FICHEROS_PERMITIDOS, IMAGENES_PERMITIDAS} from '../constants/interfaces';

@Injectable({
  providedIn: 'root'
})
export class FormValidationService {
  

  constructor(
    private readonly fb: FormBuilder,
    private readonly messagesService: MessagesService
  ) {}

  // =============================================================================
  // MÉTODOS PRIVADOS PARA VALIDACIONES COMUNES
  // =============================================================================

  private get nameValidators() {
    return [
      Validators.required,
      Validators.minLength(2),
      Validators.maxLength(50),
      Validators.pattern(/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/)
    ];
  }

  private get lastNameValidators() {
    return [
      Validators.maxLength(50),
      Validators.pattern(/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]*$/)
    ];
  }

  private get emailValidators() {
    return [
      Validators.required,
      Validators.email,
      Validators.pattern(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/),
    ];
  }

  private get aliasValidators() {
    return [
      Validators.required,
      Validators.minLength(3),
      Validators.maxLength(12),
      Validators.pattern(/^\w+$/)
    ];
  }

  private get aliasOptionalValidators() {
    return [
      Validators.minLength(3),
      Validators.maxLength(12),
      Validators.pattern(/^\w+$/)
    ];
  }

  private get passwordValidators() {
    return [
      Validators.required,
      Validators.minLength(12),
      Validators.maxLength(50),
      FormValidationService.securePasswordValidator
    ];
  }

  private get birthDateValidators() {
    return [
      Validators.required,
      Validators.pattern(/^\d{4}-\d{2}-\d{2}$/),
      FormValidationService.birthDateValidator
    ];
  }

  private get requiredPasswordValidators() {
    return [Validators.required];
  }

  private get especialidadValidators() {
    return [Validators.required];
  }

  private get tipoContenidoValidators() {
    return [Validators.required];
  }

  private get departamentoValidators() {
    return [
      Validators.required,
      Validators.minLength(2),
      Validators.maxLength(100)
    ];
  }

  private get tituloValidators() {
    return [
      Validators.required,
      Validators.minLength(3),
      Validators.maxLength(100)
    ];
  }

  private get descripcionValidators() {
    return [
      Validators.required,
      Validators.minLength(10),
      Validators.maxLength(500)
    ];
  }

  private get tagsValidators() {
    return [Validators.required, Validators.minLength(1)];
  }

  private get duracionValidators() {
    return [
      Validators.required,
    ];
  }

  private get pegiValidators() {
  return [
    Validators.required,
    Validators.min(3)  // Requiere al menos 3, ya que 0 no es válido
  ];
  }
  private get fechaExpiracionValidators() {
    return [
      FormValidationService.futureDateValidator
    ];
  }

  private get verificationCodeValidators() {
    return [
      Validators.required,
      Validators.pattern(/^\d{6}$/),
      Validators.minLength(6),
      Validators.maxLength(6)
    ];
  }

  private get urlValidators() {
    return [
      Validators.required,
      Validators.pattern(/^https?:\/\/.+$/),
      FormValidationService.videoUrlValidator
    ];
  }

  private get resolucionValidators() {
    return [Validators.required];
  }

  /**
   * Crea un formulario de login con validaciones estándar
   */
  createLoginForm(): FormGroup {
    return this.fb.group({
      email: ['', this.emailValidators],
      password: ['', Validators.required]
    });
  }

  /**
   * Crea un formulario de registro de usuario
   */
  createUserRegistrationForm(): FormGroup {
    return this.fb.group({
      nombre: ['', this.nameValidators],
      primerApellido: ['', this.nameValidators],
      segundoApellido: ['', this.lastNameValidators],
      email: ['', this.emailValidators],
      alias: ['', this.aliasOptionalValidators],
      password: ['', this.passwordValidators],
      confirmarPassword: ['', this.requiredPasswordValidators],
      fechaNacimiento: ['', this.birthDateValidators]
    }, { validators: FormValidationService.passwordMatchValidator });
  }

  
  createAdministradorRegistrationForm(): FormGroup {
    return this.fb.group({
      nombre: ['', this.nameValidators],
      primerApellido: ['', this.nameValidators],
      segundoApellido: ['', this.lastNameValidators],
      alias: ['', this.aliasValidators],
      email: ['', this.emailValidators],
      password: ['', this.passwordValidators],
      confirmarPassword: ['', this.requiredPasswordValidators],
      departamento: ['', this.departamentoValidators]
    }, { validators: FormValidationService.passwordMatchValidator });
  }

  /**
   * Crea un formulario de registro de gestor
   */
  createGestorRegistrationForm(): FormGroup {
    return this.fb.group({
      nombre: ['', this.nameValidators],
      primerApellido: ['', this.nameValidators],
      segundoApellido: ['', this.lastNameValidators],
      email: ['', this.emailValidators],
      alias: ['', this.aliasValidators],
      password: ['', this.passwordValidators],
      confirmarPassword: ['', this.requiredPasswordValidators],
      especialidad: ['', this.especialidadValidators],
      tipoContenido: ['', this.tipoContenidoValidators],
      descripcion: ['', this.descripcionValidators]
    }, { validators: FormValidationService.passwordMatchValidator });
  }

  /**
   * Crea formulario para perfil de usuario (editable excepto email)
   */
  createUserProfileForm(): FormGroup {
    return this.fb.group({
      nombre: ['', this.nameValidators],
      primerApellido: ['', this.nameValidators],
      segundoApellido: ['', this.lastNameValidators],
      alias: ['', this.aliasOptionalValidators],
      fechaNacimiento: ['', this.birthDateValidators],
      fotoPerfil: ['']
    });
  }

  createGestorProfileForm(): FormGroup {
    return this.fb.group({
      nombre: ['', this.nameValidators],
      primerApellido: ['', this.nameValidators],
      segundoApellido: ['', this.lastNameValidators],
      alias: ['', this.aliasValidators],
      especialidad: ['', this.especialidadValidators],
      descripcion: ['', this.descripcionValidators],
      fotoPerfil: ['']
    });
  }

  createAdministradorProfileForm(): FormGroup {
    return this.fb.group({
      nombre: ['', this.nameValidators],
      primerApellido: ['', this.nameValidators],
      segundoApellido: ['', this.lastNameValidators],
      alias: ['', this.aliasValidators],
      fotoPerfil: ['']
    });
  }

  
  /**
   * Crea formulario para cambio de contraseña
   */
  createChangePasswordForm(): FormGroup {
    return this.fb.group({
      currentPassword: ['', [
        Validators.required
      ]],
      newPassword: ['', [
        Validators.required,
        Validators.minLength(12),
        Validators.maxLength(50),
        FormValidationService.securePasswordValidator
      ]],
      confirmPassword: ['', [
        Validators.required
      ]]
    }, { 
      validators: (control: AbstractControl): ValidationErrors | null => {
        const newPassword = control.get('newPassword');
        const confirmPassword = control.get('confirmPassword');
        
        if (!newPassword || !confirmPassword || !newPassword.value || !confirmPassword.value) {
          return null;
        }
        
        return newPassword.value === confirmPassword.value ? null : { passwordMismatch: true };
      }
    });
  }


  /**
   * Crea formulario para recuperación de contraseña
   */
  createRecoveryForm(): FormGroup {
    return this.fb.group({
      email: ['', [Validators.required, Validators.email, Validators.pattern(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/)]]
    });
  }

  /**
   * Crea formulario para verificación de email con código de 6 dígitos
   */
  createVerifyEmailForm(): FormGroup {
    return this.fb.group({
      verificationCode: ['', this.verificationCodeValidators]
    });
  }

  /**
   * Crea un formulario para subir contenido de audio
   */
  createAudioContenidoForm(): FormGroup {
    return this.fb.group({
      titulo: ['', this.tituloValidators],
      descripcion: ['', this.descripcionValidators],
      tags: [[], this.tagsValidators],
      vip: [false],
      fechaExpiracion: [''], 
      duracion: ['', this.duracionValidators],
      pegi: [0, this.pegiValidators]
    });
  }

  /**
   * Crea un formulario para subir contenido de video
   */
  createVideoContenidoForm(): FormGroup {
    return this.fb.group({
      titulo: ['', this.tituloValidators],
      descripcion: ['', this.descripcionValidators],
      tags: [[], this.tagsValidators],
      vip: [false],
      fechaExpiracion: ['', this.fechaExpiracionValidators],
      duracion: ['', this.duracionValidators],
      pegi: [0, this.pegiValidators],
      url: ['', this.urlValidators],
      resolucion: ['', this.resolucionValidators]
    }, { validators: FormValidationService.videoResolutionValidator });
  }

  updateContenidoForm(): FormGroup {
    return this.fb.group({
      titulo: ['', this.tituloValidators],
      descripcion: ['', this.descripcionValidators],
      tags: [[], this.tagsValidators],
      vip: [false],
      fechaExpiracion: ['', this.fechaExpiracionValidators],
      pegi: [0, this.pegiValidators],
      visibilidad: [false]
    });
  }

  /**
   * Crea un formulario para crear una lista privada
   */
  createListaForm(): FormGroup {
    return this.fb.group({
      nombre: ['', this.tituloValidators],
      descripcion: ['', this.descripcionValidators]
    });
  }


  /**
   * Marca todos los campos del formulario como tocados
   */
  markAllFieldsAsTouched(form: FormGroup): void {
    form.markAllAsTouched();
  }

  /**
   * Valida si el formulario es válido antes del envío
   */
  validateFormBeforeSubmit(form: FormGroup): boolean {
    if (form.valid) {
      return true;
    } else {
      this.markAllFieldsAsTouched(form);
      return false;
    }
  }

  /**
   * Valida un archivo de imagen
   */
  validateImageFile(file: File): { isValid: boolean; error?: string } {
    // Verificar tipo de archivo
    const allowedExtensions = IMAGENES_PERMITIDAS.extensiones.map(ext => '.' + ext);
    const fileExtension = '.' + file.name.split('.').pop()?.toLowerCase();

    if (!allowedExtensions.includes(fileExtension)) {
      return {
        isValid: false,
        error: this.messagesService.getFieldErrorMessage(FieldType.IMAGEN_MINIATURA, ValidationErrorType.IMAGE_INVALID_TYPE)
      };
    }

    // Verificar tamaño
    if (file.size > IMAGENES_PERMITIDAS.tamanoMaximo) {
      return {
        isValid: false,
        error: this.messagesService.getFieldErrorMessage(FieldType.IMAGEN_MINIATURA, ValidationErrorType.IMAGE_TOO_LARGE)
      };
    }

    return { isValid: true };
  }

  /**
   * Valida un archivo de audio
   */
  validateAudioFile(file: File): { isValid: boolean; error?: string } {
    // Verificar tipo de archivo
    const allowedExtensions = FICHEROS_PERMITIDOS.extensiones.map(ext => '.' + ext);
    const fileExtension = '.' + file.name.split('.').pop()?.toLowerCase();

    if (!allowedExtensions.includes(fileExtension)) {
      return {
        isValid: false,
        error: this.messagesService.getFieldErrorMessage(FieldType.ARCHIVO_AUDIO, ValidationErrorType.AUDIO_INVALID_TYPE)
      };
    }

    // Verificar tamaño
    if (file.size > FICHEROS_PERMITIDOS.tamanoMaximo) {
      return {
        isValid: false,
        error: this.messagesService.getFieldErrorMessage(FieldType.ARCHIVO_AUDIO, ValidationErrorType.AUDIO_TOO_LARGE)
      };
    }
    return { isValid: true };
  }


  async triggerFileInput(inputId: string): Promise<void> {
    const fileInput = document.getElementById(inputId) as HTMLInputElement;
    if (fileInput) {
      fileInput.click();
    }
  }

  /**
   * Validador personalizado para contraseñas seguras
   */
  static securePasswordValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return null; // El validator 'required' se encargará de este caso
    }

    const value = control.value;
    const hasUppercase = /[A-Z]/.test(value);
    const hasLowercase = /[a-z]/.test(value);
    const hasNumber = /\d/.test(value);
    const hasSpecialChar = /[-!@#$%^&*()_+=[\]{}|;':",./<>?]/.test(value);

    const errors: ValidationErrors = {};

    if (!hasUppercase) errors['noUppercase'] = true;
    if (!hasLowercase) errors['noLowercase'] = true;
    if (!hasNumber) errors['noNumber'] = true;
    if (!hasSpecialChar) errors['noSpecialChar'] = true;

    return Object.keys(errors).length ? errors : null;
  }

  /**
   * Validador personalizado para URLs de video (YouTube o Vimeo)
   */
  static videoUrlValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return null; // El validator 'required' se encargará de este caso
    }

    const value = control.value.toLowerCase();
    const isYouTube = value.includes('youtube.com') || value.includes('youtu.be');
    const isVimeo = value.includes('vimeo.com');

    if (!isYouTube && !isVimeo) {
      return { 'invalidVideoUrl': true };
    }

    return null;
  }

  /**
   * Validador personalizado para fechas de nacimiento
   */
  static birthDateValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return null; // El validator 'required' se encargará de este caso
    }

    const value = control.value;
    const birthDate = new Date(value);
    const today = new Date();
    
    // Verificar si la fecha es válida
    if (Number.isNaN(birthDate.getTime())) {
      return { 'invalidDate': true };
    }

    // Verificar si la fecha es futura
    if (birthDate > today) {
      return { 'futureDate': true };
    }

    // Verificar si la fecha es anterior a 1900
    if (birthDate.getFullYear() < 1900) {
      return { 'tooOld': true };
    }

    // Calcular la edad
    const age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    const dayDiff = today.getDate() - birthDate.getDate();
    
    let actualAge = age;
    if (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)) {
      actualAge--;
    }

    // Verificar edad mínima de 4 años
    if (actualAge < 4) {
      return { 'tooYoung': true };
    }

    return null;
  }

  static futureDateValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return null; // El validator 'required' se encargará de este caso
    }
    const value = control.value;
    const inputDate = new Date(value);
    const today = new Date();

    // Verificar si la fecha es futura
    if (inputDate <= today) {
      return { 'notFutureDate': true };
    }

        // Verificar si la fecha es válida
    if (Number.isNaN(inputDate.getTime())) {
      return { 'invalidDate': true };
    }

    return null;
  }



  /**
   * Validador para verificar que las contraseñas coincidan
   */
  static passwordMatchValidator(formGroup: AbstractControl): ValidationErrors | null {
    if (!(formGroup instanceof FormGroup)) {
      return null;
    }

    const password = formGroup.get('password');
    const confirmarPassword = formGroup.get('confirmarPassword');

    if (!password || !confirmarPassword || !password.value || !confirmarPassword.value) {
      return null;
    }

    if (password.value === confirmarPassword.value) {
      // Si las contraseñas coinciden, limpiar el error de mismatch pero conservar otros errores
      const errors = confirmarPassword.errors;
      if (errors) {
        delete errors['mismatch'];
        confirmarPassword.setErrors(Object.keys(errors).length ? errors : null);
      }
    } else {
      confirmarPassword.setErrors({ mismatch: true });
      return { mismatch: true };
    }

    return null;
  }

  /**
   * Crea formulario para reset de contraseña
   */
    createResetPasswordForm(): FormGroup {
    return this.fb.group({
      newPassword: ['', [
        Validators.required,
        Validators.minLength(12),
        Validators.maxLength(50),
        FormValidationService.securePasswordValidator
      ]],
      confirmPassword: ['', [
        Validators.required
      ]]
    }, { 
      validators: (control: AbstractControl): ValidationErrors | null => {
        const newPassword = control.get('newPassword');
        const confirmPassword = control.get('confirmPassword');
        
        if (!newPassword || !confirmPassword || !newPassword.value || !confirmPassword.value) {
          return null;
        }
        
        return newPassword.value === confirmPassword.value ? null : { passwordMismatch: true };
      }
    });
  }

  /**
   * Validador personalizado para resolución de video según tipo de contenido VIP
   * Los contenidos no VIP solo pueden usar resoluciones 720p y 1080p
   * Los contenidos VIP pueden usar cualquier resolución incluyendo 4K
   */
  static videoResolutionValidator(formGroup: AbstractControl): ValidationErrors | null {
    if (!(formGroup instanceof FormGroup)) {
      return null;
    }

    const vipControl = formGroup.get('vip');
    const resolucionControl = formGroup.get('resolucion');

    if (!vipControl || !resolucionControl) {
      return null;
    }

    const isVip = vipControl.value === true;
    const resolucion = resolucionControl.value;

    // Si no es VIP y la resolución es 4K, mostrar error
    if (!isVip && resolucion === '4K') {
      resolucionControl.setErrors({ resolution4kNotAllowed: true });
      return { resolution4kNotAllowed: true };
    }

    // Si es válido, limpiar errores de resolución (pero conservar otros errores)
    if (resolucionControl.errors) {
      delete resolucionControl.errors['resolution4kNotAllowed'];
      if (Object.keys(resolucionControl.errors).length === 0) {
        resolucionControl.setErrors(null);
      }
    }

    return null;
  }


}