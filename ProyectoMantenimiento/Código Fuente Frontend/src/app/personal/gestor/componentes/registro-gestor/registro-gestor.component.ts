import { Component, OnInit } from '@angular/core';
import { FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule} from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormValidationService, APP_ROUTES, USUARIO_ROUTES, PERSONAL_ROUTES, GESTOR_ROUTES, IMAGENES_PERMITIDAS, Especialidades, SharedService} from '@shared';
import { GestorService } from '../../gestor.service';
import { NotificationService } from '../../../../shared/services/notification.service';

@Component({
  selector: 'app-registro-gestor',
  standalone: true,
  imports: [RouterModule, FormsModule, ReactiveFormsModule, CommonModule],
  templateUrl: './registro-gestor.component.html',

})
export class RegistroGestorComponent implements OnInit {
  
  // Propiedades del componente
  gestorForm!: FormGroup;
  fotoPerfil: File | null = null;
  fotoPreviewUrl: string | null = null;
  isLoading: boolean = false;

  formatosImagen = IMAGENES_PERMITIDAS;
  acceptExtensionsImagen: string = IMAGENES_PERMITIDAS.extensiones.length ? '.' + IMAGENES_PERMITIDAS.extensiones.join(', .') : '';

  // Propiedades para toggle de contraseña
  showPassword: boolean = false;
  showConfirmPassword: boolean = false;

  // Constantes de rutas para usar en templates
  public readonly APP_ROUTES = APP_ROUTES;
  public readonly USUARIO_ROUTES = USUARIO_ROUTES;
  public readonly GESTOR_ROUTES = GESTOR_ROUTES;
  public readonly PERSONAL_ROUTES = PERSONAL_ROUTES;

  especialidades: Especialidades[] = [];

  constructor(
    private readonly router: Router,
    private readonly formValidationService: FormValidationService,
    private readonly gestorService: GestorService,
    private readonly sharedService: SharedService,
    private readonly notificationService: NotificationService
  ) {}

  // =============================================================================
  // MÉTODOS DE INICIALIZACIÓN
  // =============================================================================
  
  ngOnInit(): void {
    this.gestorForm = this.formValidationService.createGestorRegistrationForm();
    this.sharedService.getEspecialidades().subscribe((especialidades) => {
      this.especialidades = especialidades;
    });
  }

 
  // =============================================================================
  // MÉTODOS DEL SERVICIO
  // =============================================================================

  async onSubmit(): Promise<void> {
    if (this.isLoading) return;

    if (this.formValidationService.validateFormBeforeSubmit(this.gestorForm)) {
      this.isLoading = true;
      
      const formData = this.gestorForm.value;
      formData.especialidad = this.gestorForm.value.especialidad;
      formData.apellidos= formData.primerApellido + ' ' + formData.segundoApellido;
      delete formData.primerApellido;
      delete formData.segundoApellido;
      delete formData.confirmarPassword;
      formData.rol ='CREADOR';
      formData.aliasCreador= formData.alias;
      formData.fotoPerfil = null;

      if (this.fotoPerfil !== null) {
        formData.fotoPerfil = await new Promise<string>((resolve, reject) => {
          const reader = new FileReader();
          reader.readAsDataURL(this.fotoPerfil!);
          reader.onload = () => resolve(reader.result as string);
          reader.onerror = () => reject(new Error('Error al convertir archivo a data URI'));
        });
      }

      

      this.gestorService.registerGestor(formData).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.notificationService.success('Espere a que un administrador apruebe su cuenta.', 'Registro completado');
          this.router.navigateByUrl('/');
        },
        error: (error) => {
          this.isLoading = false;
          this.notificationService.error('Ha ocurrido un error. Por favor, inténtalo de nuevo.', 'Error en el registro');
        }
      });
    }
  }

  // =============================================================================
  // MÉTODOS DE MANEJO DE ARCHIVOS
  // =============================================================================

  onFotoPerfilSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.[0]) {
      this.fotoPerfil = input.files[0];

      // Usar el método de validación del servicio
      const validation = this.formValidationService.validateImageFile(this.fotoPerfil);
      if (!validation.isValid) {
        this.notificationService.warning(validation.error ?? 'Archivo no válido');
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
  // MÉTODOS DE VALIDACIÓN
  // =============================================================================
  
  isFieldInvalid(fieldName: string): boolean {
    return this.sharedService.isFieldInvalid(this.gestorForm, fieldName);
  }

  getFieldError(fieldName: string): string {
    return this.sharedService.getFieldError(this.gestorForm, fieldName);
  }

  isSubmitDisabled(): boolean {
    return this.gestorForm.invalid || this.isLoading;
  }

  getPasswordRequirements(): readonly string[] {
    return this.sharedService.getPasswordRequirements();
  }

  togglePassword(field: string): void {
    if (field === 'password') {
      this.showPassword = this.sharedService.togglePasswordVisibility(this.showPassword);
    } else if (field === 'confirmPassword') {
      this.showConfirmPassword = this.sharedService.togglePasswordVisibility(this.showConfirmPassword);
    }
  }

}
