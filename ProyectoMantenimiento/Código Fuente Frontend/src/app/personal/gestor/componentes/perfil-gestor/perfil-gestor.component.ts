import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { 
  FormValidationService, 
  APP_ROUTES, 
  GESTOR_ROUTES, 
  Especialidades, 
  TIPO_CONTENIDOS, 
  GestorStatus, 
  SharedService 
} from '@shared';
import { CambiarPasswordModalComponent } from 'src/app/shared/components/cambiar-password-modal/cambiar-password-modal.component';
import { PersonalService } from '../../../personal.service';
import { GestorService } from '../../gestor.service';
import { NotificationService } from '../../../../shared/services/notification.service';

@Component({
  selector: 'app-perfil-gestor',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule, CambiarPasswordModalComponent],
  templateUrl: './perfil-gestor.component.html',
})
export class PerfilGestorComponent implements OnInit {
  // =============================================================================
  // PROPIEDADES DEL COMPONENTE
  // =============================================================================
  
  readonly tipoContenidos = TIPO_CONTENIDOS;

  gestorProfile: GestorStatus = {} as GestorStatus;
  especialidades: Especialidades[] = [];

  gestorForm!: FormGroup;
  passwordForm!: FormGroup;
  
  isEditMode: boolean = false;
  showChangePasswordModal: boolean = false;
  showDeleteConfirmation: boolean = false;
  profilePhotoFile: File | null = null;

  public readonly GESTOR_ROUTES = GESTOR_ROUTES;
  public readonly APP_ROUTES = APP_ROUTES;

  constructor(
    private readonly router: Router,
    private readonly formValidationService: FormValidationService,
    private readonly personalService: PersonalService,
    private readonly gestorService: GestorService,
    private readonly sharedService: SharedService,
    private readonly notificationService: NotificationService
  ) {}

  // =============================================================================
  // MÉTODOS DE INICIALIZACIÓN
  // =============================================================================

  ngOnInit(): void {
    // 1) Cargar especialidades desde el servicio
    this.sharedService.getEspecialidades().subscribe((especialidades) => {
      this.especialidades = especialidades;
    });

    this.loadGestorProfile();
  }

  /**
   * Carga el perfil del gestor usando el userId del JWT
   * (misma idea que loadGestor en InicioGestorComponent, pero adaptado aquí)
   */
  private loadGestorProfile(): void {
    try {
      const credentials = this.sharedService.obtainCredentials();

      if (!credentials?.userId || credentials?.rol !== 'CREADOR') {
        console.warn('[PerfilGestor] Credenciales no válidas o rol distinto de CREADOR');
        this.notificationService.error('No se ha podido cargar tu perfil. Inicia sesión de nuevo.');
        this.sharedService.performLogout(this.router, this.APP_ROUTES.home);
        return;
      }

      const userId = credentials.userId;

      this.personalService.getCreador(userId).subscribe({
        next: (creadorData: any) => {
          const apellidosRaw = creadorData.apellidos ?? '';
          const [primerApellido, segundoApellido] = apellidosRaw.split(' ');

          this.gestorProfile = {
            idUsuario: creadorData.idUsuario,
            tipo: 'gestor',
            nombre: creadorData.nombre,
            primerApellido: primerApellido || '',
            segundoApellido: segundoApellido || '',
            email: creadorData.email,
            alias: creadorData.alias,
            tipoContenido: creadorData.tipoContenido,
            especialidad: creadorData.especialidad,
            descripcion: creadorData.descripcion,
            fotoPerfil: creadorData.fotoPerfil ?? 'assets/porDefecto.jpg',
            bloqueado: creadorData.bloqueado ?? false,
            validado: creadorData.validado ?? false,
            twoFactorEnabled: creadorData.twoFactorEnabled,
            thirdFactorEnabled: creadorData.thirdFactorEnabled
          } as GestorStatus;

          // Procesar la foto y preparar formularios una vez tenemos los datos
          this.processProfilePhoto();
          this.initializeForms();
        },
        error: (error) => {
          console.error('[PerfilGestor] Error al cargar datos del gestor:', error);
          this.notificationService.error('No se ha podido cargar tu perfil. Inténtalo más tarde.');
        }
      });
    } catch (error) {
      console.error('[PerfilGestor] Error obteniendo credenciales del JWT:', error);
      this.notificationService.error('No se ha podido cargar tu perfil. Inicia sesión de nuevo.');
      this.sharedService.performLogout(this.router, this.APP_ROUTES.home);
    }
  }

  private initializeForms(): void {
    this.gestorForm = this.formValidationService.createGestorProfileForm();
    this.passwordForm = this.formValidationService.createChangePasswordForm();
    
    // Prellenar el formulario con los datos del gestor
    this.gestorForm.patchValue({
      nombre: this.gestorProfile.nombre,
      primerApellido: this.gestorProfile.primerApellido,
      segundoApellido: this.gestorProfile.segundoApellido,
      alias: this.gestorProfile.alias,
      especialidad: this.gestorProfile.especialidad,
      descripcion: this.gestorProfile.descripcion,
      fotoPerfil: this.gestorProfile.fotoPerfil,
    });
  }

  private processProfilePhoto(): void {
    // Las fotos vienen como URI, no hacemos nada especial
    if (this.gestorProfile.fotoPerfil && 
        !this.gestorProfile.fotoPerfil.startsWith('blob:') && 
        !this.gestorProfile.fotoPerfil.startsWith('data:')) {
      // ya es una URI válida
    }
  }

  // =============================================================================
  // MÉTODOS DEL SERVICIO
  // =============================================================================

  async saveProfile(): Promise<void> {
    if (!this.formValidationService.validateFormBeforeSubmit(this.gestorForm)) {
      return;
    }

    const formData = this.gestorForm.value;
    
    if (this.profilePhotoFile !== null) {
      formData.fotoPerfil = await new Promise<string>((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(this.profilePhotoFile!);
        reader.onload = () => resolve(reader.result as string);
        reader.onerror = () => reject(new Error('Error al convertir archivo a data URI'));
      });
    }

    formData.apellidos = formData.primerApellido + ' ' + formData.segundoApellido;
    if (this.profilePhotoFile === null) {
      formData.fotoPerfil = null;
    }
    formData.aliasCreador = formData.alias;
    delete formData.primerApellido;
    delete formData.segundoApellido;

    this.gestorService.updateProfile(this.gestorProfile.idUsuario, formData).subscribe({
      next: () => {
        this.gestorProfile = {
          ...this.gestorProfile,
          nombre: formData.nombre,
          primerApellido: formData.apellidos.split(' ')[0],
          segundoApellido: formData.apellidos.split(' ')[1],
          alias: formData.alias,
          especialidad: formData.especialidad,
          descripcion: formData.descripcion,
          fotoPerfil: formData.fotoPerfil ?? this.gestorProfile.fotoPerfil
        };


        this.isEditMode = false;
        this.notificationService.success('Tu perfil ha sido actualizado correctamente.', 'Perfil actualizado');
      },
      error: (error) => {
        console.error('Error al actualizar perfil:', error);
        this.notificationService.error('No se pudo actualizar el perfil. Inténtalo de nuevo.');
      }
    });
  }

  confirmChangePassword(): void {
    if (!this.formValidationService.validateFormBeforeSubmit(this.passwordForm)) {
      return;
    }

    this.gestorService.changePassword(this.gestorProfile.idUsuario, this.passwordForm).subscribe({
      next: () => {
        this.notificationService.success('Tu contraseña ha sido actualizada con éxito.', 'Contraseña cambiada');
        this.cancelChangePassword();
      },
      error: () => {
        this.notificationService.error('No se pudo cambiar la contraseña. Inténtalo de nuevo.');
      }
    });
  }

  // Confirmar baja de cuenta
  confirmDeleteAccount(): void {
    this.gestorService.deleteAccount().subscribe({
      next: () => {
        this.notificationService.success('Tu cuenta ha sido eliminada correctamente.', 'Cuenta eliminada');
        this.sharedService.logout();
        this.router.navigate([APP_ROUTES.home]);
      },
      error: (error) => {
        console.error('Error al eliminar cuenta:', error);
        this.notificationService.error('No se pudo eliminar la cuenta. Inténtalo de nuevo.');
      }
    });
  }

  // =============================================================================
  // MÉTODOS DE GESTIÓN DE ARCHIVOS
  // =============================================================================

  async onFileSelected(event: Event): Promise<void> {
    const result = await this.sharedService.handlePhotoUpload(event, 5);
    
    switch (result.status) {
      case 'SUCCESS':
        this.profilePhotoFile = result.file!;
        this.gestorProfile.fotoPerfil = result.previewUrl!;
        this.gestorForm.patchValue({ fotoPerfil: result.previewUrl! });
        break;
      case 'ERROR_INVALID_TYPE':
      case 'ERROR_TOO_LARGE':
      case 'ERROR_NO_FILE':
        this.notificationService.warning(result.message ?? 'Error al subir la foto');
        break;
    }
  }

  // =============================================================================
  // MÉTODOS DE MODALES
  // =============================================================================

  changePassword(): void {
    this.showChangePasswordModal = true;
    this.passwordForm.reset();
  }

  cancelChangePassword(): void {
    this.showChangePasswordModal = false;
    this.passwordForm.reset();
  }

  showDeleteAccount(): void {
    this.showDeleteConfirmation = true;
  }

  cancelDelete(): void {
    this.showDeleteConfirmation = false;
  }

  toggleEditMode(): void {
    this.isEditMode = !this.isEditMode;
    if (this.isEditMode) {
      this.gestorForm.patchValue({
        nombre: this.gestorProfile.nombre,
        primerApellido: this.gestorProfile.primerApellido,
        segundoApellido: this.gestorProfile.segundoApellido,
        alias: this.gestorProfile.alias,
        especialidad: this.gestorProfile.especialidad,
        descripcion: this.gestorProfile.descripcion,
        fotoPerfil: this.gestorProfile.fotoPerfil
      });
    }
  }

  cancelEdit(): void {
    this.isEditMode = false;
    this.gestorForm.patchValue({
      nombre: this.gestorProfile.nombre,
      primerApellido: this.gestorProfile.primerApellido,
      segundoApellido: this.gestorProfile.segundoApellido,
      alias: this.gestorProfile.alias,
      especialidad: this.gestorProfile.especialidad,
      descripcion: this.gestorProfile.descripcion,
      fotoPerfil: this.gestorProfile.fotoPerfil
    });
    this.gestorForm.markAsUntouched();
    this.gestorForm.markAsPristine();
  }

  onModalKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      this.cancelChangePassword();
    }
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

  preventKeyboardInput(event: KeyboardEvent): void {
    this.sharedService.preventKeyboardInput(event);
  }
}
