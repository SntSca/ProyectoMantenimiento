import { Component, OnInit } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms'; // Agrega ReactiveFormsModule
import { Router, RouterModule } from '@angular/router'; // Agrega RouterModule
import { CommonModule } from '@angular/common'; // Para directivas comunes
import { FormValidationService,SharedService, IMAGENES_PERMITIDAS, APP_ROUTES, USUARIO_ROUTES, GESTOR_ROUTES } from '@shared';
import { VipComponent } from '../vip/vip.component'; // Importa VipComponent
import { UsuarioService } from '../../usuario.service';
import { NotificationService } from '../../../shared/services/notification.service';

@Component({
  selector: 'app-registro-usuario',
  standalone: true, // Cambia a true
  imports: [CommonModule, ReactiveFormsModule, RouterModule, VipComponent], // Agrega imports necesarios
  templateUrl: './registro-usuario.component.html',
})
export class RegistroUsuarioComponent implements OnInit {
  
  // Propiedades del componente
  userForm!: FormGroup;
  flagVIP: boolean = false;
  fotoPerfil: File | null = null;
  fotoPreviewUrl: string | null = null;
  isLoading: boolean = false;

  // Propiedades para modales VIP
  showVipConfirmation: boolean = false;
  showCancelVipConfirmation: boolean = false;

  formatosImagen = IMAGENES_PERMITIDAS;
  acceptExtensionsImagen: string = IMAGENES_PERMITIDAS.extensiones.length ? '.' + IMAGENES_PERMITIDAS.extensiones.join(', .') : '';

  // Propiedades para toggle de contraseña
  showPassword: boolean = false;
  showConfirmPassword: boolean = false;


  // Constantes de rutas para usar en templates
  public readonly APP_ROUTES = APP_ROUTES;
  public readonly USUARIO_ROUTES = USUARIO_ROUTES;
  public readonly GESTOR_ROUTES = GESTOR_ROUTES;

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
  // MÉTODOS DEL SERVICIO
  // =============================================================================

  async onSubmit(): Promise<void> {
    if (this.isLoading) return;

    if (this.formValidationService.validateFormBeforeSubmit(this.userForm)) {
      this.isLoading = true;

      const formData = this.userForm.value;
      delete formData.confirmarPassword;
      formData.flagVip = this.flagVIP;
      formData.rol ='NORMAL';
      formData.apellidos= formData.primerApellido + ' ' + formData.segundoApellido;
      delete formData.primerApellido;
      delete formData.segundoApellido;

      if (this.fotoPerfil !== null) {
        formData.fotoPerfil = await new Promise<string>((resolve, reject) => {
          const reader = new FileReader();
          reader.readAsDataURL(this.fotoPerfil!);
          reader.onload = () => resolve(reader.result as string);
          reader.onerror = () => reject(new Error('Error al convertir archivo a data URI'));
        });
      }

      

      this.usuarioService.registerUsuario(formData).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.notificationService.success('Revise su correo electrónico para activar su cuenta.', 'Registro completado');
          this.router.navigateByUrl('/');
        },
        error: (error) => {
          this.isLoading = false;
          this.notificationService.error('Ha ocurrido un error durante el registro. Por favor, inténtalo de nuevo.');
        }
      });
    }
  }

  toggleVipStatus(): void {
    this.flagVIP = !this.flagVIP;
    
  }

  // =============================================================================
  // MÉTODOS DE GESTIÓN DE MODALES VIP
  // =============================================================================

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

  // =============================================================================
  // MÉTODOS DE GESTION DE ARCHIVOS
  // =============================================================================

    onFotoPerfilSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.[0]) {
      this.fotoPerfil = input.files[0];

      // Usar el método de validación del servicio
      const validation = this.formValidationService.validateImageFile(this.fotoPerfil);
      if (!validation.isValid) {
        this.notificationService.warning(validation.error ?? 'Archivo de imagen no válido');
        this.fotoPerfil = null;
        return;
      }

      // Crear preview URL para imagen
      this.fotoPreviewUrl = URL.createObjectURL(this.fotoPerfil);
    }
  }

  async triggerFileUpload(): Promise<void> {
    await this.formValidationService.triggerFileInput('photoUpload');
  }

  // =============================================================================
  // MÉTODOS DE VALIDACIÓN/UTILIDAD
  // =============================================================================
  
  isFieldInvalid(fieldName: string): boolean {
    return this.sharedService.isFieldInvalid(this.userForm, fieldName);
  }

  getFieldError(fieldName: string): string {
    return this.sharedService.getFieldError(this.userForm, fieldName);
  }

  isSubmitDisabled(): boolean {
    const isInvalid = this.userForm.invalid;
    const isLoading = this.isLoading;
    const result = isInvalid || isLoading;
    
    return result;
  }

  getPasswordRequirements(): readonly string[] {
    return this.sharedService.getPasswordRequirements();
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