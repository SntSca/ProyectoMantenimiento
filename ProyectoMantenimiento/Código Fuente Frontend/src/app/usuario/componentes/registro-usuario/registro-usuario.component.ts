import { Component, OnInit } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import {
  FormValidationService,
  SharedService,
  IMAGENES_PERMITIDAS,
  APP_ROUTES,
  USUARIO_ROUTES,
  GESTOR_ROUTES
} from '@shared';
import { VipComponent } from '../vip/vip.component';
import { UsuarioService } from '../../usuario.service';
import { NotificationService } from '../../../shared/services/notification.service';

@Component({
  selector: 'app-registro-usuario',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, VipComponent],
  templateUrl: './registro-usuario.component.html',
})
export class RegistroUsuarioComponent implements OnInit {

  // =============================================================================
  // PROPIEDADES DEL FORMULARIO
  // =============================================================================

  userForm!: FormGroup;
  flagVIP: boolean = false;
  fotoPerfil: File | null = null;
  fotoPreviewUrl: string | null = null;
  isLoading: boolean = false;

  // Modales VIP
  showVipConfirmation: boolean = false;
  showCancelVipConfirmation: boolean = false;

  formatosImagen = IMAGENES_PERMITIDAS;
  acceptExtensionsImagen: string =
    IMAGENES_PERMITIDAS.extensiones.length
      ? '.' + IMAGENES_PERMITIDAS.extensiones.join(', .')
      : '';


  showPassword: boolean = false;
  showConfirmPassword: boolean = false;

  // Rutas
  public readonly APP_ROUTES = APP_ROUTES;
  public readonly USUARIO_ROUTES = USUARIO_ROUTES;
  public readonly GESTOR_ROUTES = GESTOR_ROUTES;

  // =============================================================================
  // PROPIEDADES PARA DICCIONARIO EXTERNO (HIBP)
  // =============================================================================

  private pwnedCount: number | null = null;
  private pwnedCheckedFor: string = '';
  isCheckingPwned: boolean = false;

  constructor(
    private readonly router: Router,
    private readonly formValidationService: FormValidationService,
    private readonly usuarioService: UsuarioService,
    private readonly sharedService: SharedService,
    private readonly notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.userForm = this.formValidationService.createUserRegistrationForm();
  }

  // =============================================================================
  // LÓGICA DICCIONARIO EXTERNO (HIBP)
  // =============================================================================

  private async sha1ForHIBP(text: string): Promise<string> {
    const encoder = new TextEncoder();
    const data = encoder.encode(text);
    const hashBuffer = await crypto.subtle.digest('SHA-1', data);
    return Array.from(new Uint8Array(hashBuffer))
      .map((b) => b.toString(16).padStart(2, '0'))
      .join('')
      .toUpperCase();
  }


  private async checkPasswordPwned(password: string): Promise<number> {
    if (!password) return 0;

    const fullHash = await this.sha1ForHIBP(password);
    const prefix = fullHash.slice(0, 5);
    const suffix = fullHash.slice(5);

    const response = await fetch(`https://api.pwnedpasswords.com/range/${prefix}`, {
      headers: { 'Add-Padding': 'true' },
    });

    if (!response.ok) {
      throw new Error('Fallo consultando diccionario de contraseñas comprometidas');
    }

    const body = await response.text();
    const lines = body.split('\n');

    for (const line of lines) {
      const [hashSuffix, countStr] = line.trim().split(':');
      if ((hashSuffix || '').toUpperCase() === suffix) {
        const count = parseInt((countStr || '').replace(/\D/g, ''), 10);
        return Number.isNaN(count) ? 0 : count;
      }
    }

    return 0;
  }

 
  private async ensurePasswordNotPwned(password: string): Promise<boolean> {
    if (!password) return true;


    if (this.pwnedCheckedFor === password && this.pwnedCount !== null) {
      if (this.pwnedCount > 0) {
        this.notificationService.warning(
          `Esta contraseña aparece en filtraciones públicas ${this.pwnedCount} veces. Por favor, elige otra distinta.`,
          'Contraseña comprometida'
        );
        return false;
      }
      return true;
    }

    this.isCheckingPwned = true;
    try {
      this.pwnedCount = await this.checkPasswordPwned(password);
      this.pwnedCheckedFor = password;

      if (this.pwnedCount > 0) {
        this.notificationService.warning(
          `Esta contraseña aparece en filtraciones públicas ${this.pwnedCount} veces. Por favor, elige otra distinta.`,
          'Contraseña comprometida'
        );
        return false;
      }

      return true;
    } catch (error) {
      console.error('Error comprobando si la contraseña está en filtraciones públicas', error);

      this.pwnedCount = null;
      this.pwnedCheckedFor = password;
      this.notificationService.warning(
        'No se ha podido comprobar si tu contraseña está en filtraciones públicas. Inténtalo de nuevo más tarde o usa otra contraseña.',
        'Aviso de seguridad'
      );
      return false;
    } finally {
      this.isCheckingPwned = false;
    }

  }

  // =============================================================================
  // MÉTODOS DEL SERVICIO (REGISTRO)
  // =============================================================================

  async onSubmit(): Promise<void> {
    if (this.isLoading) return;


    const isValid = this.formValidationService.validateFormBeforeSubmit(this.userForm);
    if (!isValid) {
      return;
    }


    const password: string = this.userForm.get('password')?.value;


    const passwordOk = await this.ensurePasswordNotPwned(password);
    if (!passwordOk) {

      return;
    }

    this.isLoading = true;

    const formData: any = { ...this.userForm.value };
    delete formData.confirmarPassword;
    formData.flagVip = this.flagVIP;
    formData.rol = 'NORMAL';
    formData.apellidos = formData.primerApellido + ' ' + formData.segundoApellido;
    delete formData.primerApellido;
    delete formData.segundoApellido;


    if (this.fotoPerfil !== null) {
      try {
        formData.fotoPerfil = await new Promise<string>((resolve, reject) => {
          const reader = new FileReader();
          reader.readAsDataURL(this.fotoPerfil!);
          reader.onload = () => resolve(reader.result as string);
          reader.onerror = () => reject(new Error('Error al convertir archivo a data URI'));
        });
      } catch {
        this.isLoading = false;
        this.notificationService.error('Error al procesar la foto de perfil.');
        return;
      }
    }

    this.usuarioService.registerUsuario(formData).subscribe({
      next: () => {
        this.isLoading = false;
        this.notificationService.success(
          'Revise su correo electrónico para activar su cuenta.',
          'Registro completado'
        );
        this.router.navigateByUrl('/');
      },
      error: () => {
        this.isLoading = false;
        this.notificationService.error(
          'Ha ocurrido un error durante el registro. Por favor, inténtalo de nuevo.'
        );
      },
    });
  }


  toggleVipStatus(): void {
    this.flagVIP = !this.flagVIP;
  }

  showUpgradeToVip(): void {
    this.showVipConfirmation = true;
  }

  showCancelVip(): void {
    this.showCancelVipConfirmation = true;
  }

  confirmVipUpgrade(): void {
    this.flagVIP = true;
    this.showVipConfirmation = false;
  }

  cancelVipUpgrade(): void {
    this.showVipConfirmation = false;
  }

  confirmVipCancel(): void {
    this.flagVIP = false;
    this.showCancelVipConfirmation = false;
  }

  cancelVipCancel(): void {
    this.showCancelVipConfirmation = false;
  }


  onFotoPerfilSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.[0]) {
      this.fotoPerfil = input.files[0];

      const validation = this.formValidationService.validateImageFile(this.fotoPerfil);
      if (!validation.isValid) {
        this.notificationService.warning(validation.error ?? 'Archivo de imagen no válido');
        this.fotoPerfil = null;
        return;
      }

      this.fotoPreviewUrl = URL.createObjectURL(this.fotoPerfil);
    }
  }

  async triggerFileUpload(): Promise<void> {
    await this.formValidationService.triggerFileInput('photoUpload');
  }

  isFieldInvalid(fieldName: string): boolean {
    return this.sharedService.isFieldInvalid(this.userForm, fieldName);
  }

  getFieldError(fieldName: string): string {
    return this.sharedService.getFieldError(this.userForm, fieldName);
  }

  isSubmitDisabled(): boolean {
    const isInvalid = this.userForm.invalid;
    const isLoading = this.isLoading;
    return isInvalid || isLoading;
  }

  getPasswordRequirements(): readonly string[] {
    return [
      ...this.sharedService.getPasswordRequirements(),
      'No debe aparecer en filtraciones públicas conocidas.'
    ];
  }


  preventKeyboardInput(event: KeyboardEvent): void {
    this.sharedService.preventKeyboardInput(event);
  }

  togglePassword(field: string): void {
    if (field === 'password') {
      this.showPassword = this.sharedService.togglePasswordVisibility(this.showPassword);
    } else if (field === 'confirmPassword') {
      this.showConfirmPassword = this.sharedService.togglePasswordVisibility(this.showConfirmPassword);
    }
  }
}
