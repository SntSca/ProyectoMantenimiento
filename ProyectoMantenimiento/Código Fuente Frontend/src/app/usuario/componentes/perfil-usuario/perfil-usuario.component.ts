import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  FormValidationService,
  APP_ROUTES,
  MOCK_USERS,
  CambiarPasswordModalComponent,
  UserStatus,
  SharedService,
  Tags,             
} from '@shared';
import { VipComponent } from '../vip/vip.component';
import { UsuarioService } from '../../usuario.service';
import { USUARIO_ROUTES } from '../../usuario.routes';
import { NotificationService } from '../../../shared/services/notification.service';

@Component({
  selector: 'app-perfil-usuario',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    CambiarPasswordModalComponent,
    VipComponent
  ],
  templateUrl: './perfil-usuario.component.html',
})
export class PerfilUsuarioComponent implements OnInit {

  // =========================
  // PERFIL USUARIO
  // =========================
  userProfile: UserStatus = {} as UserStatus;

  profileForm!: FormGroup;
  passwordForm!: FormGroup;
  
  isEditMode: boolean = false;
  showDeleteConfirmation: boolean = false;
  showVipConfirmation: boolean = false;
  showCancelVipConfirmation: boolean = false;
  showChangePasswordModal: boolean = false;
  profilePhotoFile: File | null = null;

  public readonly APP_ROUTES = APP_ROUTES;
  public readonly USUARIO_ROUTES = USUARIO_ROUTES;

  // =========================
  // GUSTOS / PREFERENCIAS
  // =========================
  tagsDisponibles: Tags[] = [];
  gustosSeleccionados: string[] = [];
  loadingGustos: boolean = false;
  savingGustos: boolean = false;

  constructor(
    private readonly router: Router,
    private readonly formValidationService: FormValidationService,
    private readonly usuarioService: UsuarioService,
    private readonly sharedService: SharedService,
    private readonly notificationService: NotificationService
  ) {}

  // =============================================================================
  // CICLO DE VIDA
  // =============================================================================

  ngOnInit(): void {
    this.initCurrentUserFromJwt();
    this.cargarTagsYGustos();
  }

  private initCurrentUserFromJwt(): void {
    try {
      const credentials = this.sharedService.obtainCredentials();
      if (!credentials?.userId) {
        console.warn('[PerfilUsuario] No se han podido obtener credenciales del JWT');
        this.router.navigate([this.APP_ROUTES.home]);
        return;
      }

      const userId = credentials.userId;

      this.usuarioService.getUsuario(userId).subscribe({
        next: (userData: any) => {
          this.userProfile = {
            idUsuario: userData.idUsuario,
            tipo: 'usuario',
            nombre: userData.nombre,
            primerApellido: userData.apellidos?.split(' ')[0] || '',
            segundoApellido: userData.apellidos?.split(' ')[1] || '',
            email: userData.email,
            alias: userData.alias,
            fechaNacimiento: userData.fechaNacimiento,
            flagVIP: userData.flagVIP,
            fotoPerfil: userData.fotoPerfil ?? 'assets/porDefecto.jpg',
            bloqueado: userData.bloqueado,
            twoFactorEnabled: userData.twoFactorEnabled,
            thirdFactorEnabled: userData.thirdFactorEnabled
          } as UserStatus;

          this.processProfilePhoto();
          this.initializeForms();
        },
        error: (error) => {
          console.error('[PerfilUsuario] Error al obtener datos del usuario, usando MOCK_USERS[0]:', error);
          this.userProfile = MOCK_USERS[0];
          this.processProfilePhoto();
          this.initializeForms();
        }
      });
    } catch (e) {
      console.error('[PerfilUsuario] Error al obtener credenciales del JWT:', e);
      this.router.navigate([this.APP_ROUTES.home]);
    }
  }

  private initializeForms(): void {
    this.profileForm = this.formValidationService.createUserProfileForm();
    this.passwordForm = this.formValidationService.createChangePasswordForm();
    
    this.profileForm.patchValue({
      nombre: this.userProfile.nombre,
      primerApellido: this.userProfile.primerApellido,
      segundoApellido: this.userProfile.segundoApellido,
      alias: this.userProfile.alias,
      fechaNacimiento: this.formatDateForInput(this.userProfile.fechaNacimiento)
    });
  }

  private processProfilePhoto(): void {
    // Las fotos vienen como URI o base64; si es null, ya la hemos puesto por defecto
    if (this.userProfile.fotoPerfil &&
        !this.userProfile.fotoPerfil.startsWith('blob:') &&
        !this.userProfile.fotoPerfil.startsWith('data:')) {
      // Ya es una URI válida, no hacemos nada
    }
  }

  // =============================================================================
  // GUSTOS / PREFERENCIAS (LÓGICA)
  // =============================================================================

  private cargarTagsYGustos(): void {
    this.loadingGustos = true;

    // 1) Cargar todos los tags disponibles
    this.sharedService.getTags().subscribe({
      next: (tags) => {
        this.tagsDisponibles = tags;

        // 2) Cargar gustos actuales del usuario
        this.usuarioService.getGustos().subscribe({
          next: (gustos) => {
            this.gustosSeleccionados = gustos;
            this.loadingGustos = false;
          },
          error: (err) => {
            console.error('[PerfilUsuario] Error cargando gustos:', err);
            this.loadingGustos = false;
          }
        });
      },
      error: (err) => {
        console.error('[PerfilUsuario] Error cargando tags:', err);
        this.loadingGustos = false;
      }
    });
  }
  // Estado para mostrar el modal de selección de gustos
  showGustosForm: boolean = false;


  // Método para abrir/cerrar el form
  toggleGustosForm(): void {
    this.showGustosForm = !this.showGustosForm;
  }


  isTagSelected(tagNombre: string): boolean {
    return this.gustosSeleccionados.includes(tagNombre);
  }

  toggleGusto(tagNombre: string): void {
    if (this.isTagSelected(tagNombre)) {
      this.gustosSeleccionados = this.gustosSeleccionados.filter(t => t !== tagNombre);
    } else {
      this.gustosSeleccionados = [...this.gustosSeleccionados, tagNombre];
    }
  }

  guardarGustos(): void {
    this.savingGustos = true;
    this.usuarioService.updateGustos(this.gustosSeleccionados).subscribe({
      next: (gustosActualizados) => {
        this.gustosSeleccionados = gustosActualizados;
        this.savingGustos = false;
        this.notificationService.success(
          'Tus preferencias de contenido se han guardado correctamente.',
          'Gustos actualizados'
        );
      },
      error: (err) => {
        console.error('[PerfilUsuario] Error guardando gustos:', err);
        this.savingGustos = false;
        this.notificationService.error(
          'No se han podido guardar tus gustos. Inténtalo de nuevo más tarde.',
          'Error'
        );
      }
    });
  }

  // =============================================================================
  // MÉTODOS DEL SERVICIO (PERFIL)
  // =============================================================================

  async saveProfile(): Promise<void> {
    if (!this.formValidationService.validateFormBeforeSubmit(this.profileForm)) {
      return;
    }

    const formData = this.profileForm.value;

    if (this.profilePhotoFile !== null) {
      formData.fotoPerfil = await new Promise<string>((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(this.profilePhotoFile!);
        reader.onload = () => resolve(reader.result as string);
        reader.onerror = () => reject(new Error('Error al convertir archivo a data URI'));
      });
    }

    formData.apellidos = formData.primerApellido + ' ' + formData.segundoApellido;
    delete formData.primerApellido;
    delete formData.segundoApellido;
    
    this.usuarioService.updateProfile(this.userProfile.idUsuario, formData).subscribe({
      next: () => {
        this.userProfile = {
          ...this.userProfile,
          nombre: formData.nombre,
          primerApellido: formData.apellidos.split(' ')[0],
          segundoApellido: formData.apellidos.split(' ')[1],
          alias: formData.alias,
          fechaNacimiento: formData.fechaNacimiento,
          // Si hemos enviado fotoPerfil en el form, la actualizamos también
          fotoPerfil: formData.fotoPerfil ?? this.userProfile.fotoPerfil
        };

        this.isEditMode = false;
        this.notificationService.success('Tu perfil ha sido actualizado con éxito', 'Perfil actualizado');
      },
      error: (error) => {
        console.error('Error al actualizar perfil:', error);
        this.notificationService.error('No se pudo actualizar el perfil. Por favor, inténtalo de nuevo.');
      }
    });
  }

  confirmDeleteAccount(): void {
    this.usuarioService.deleteAccount(this.userProfile.email).subscribe({
      next: () => {
        this.notificationService.success('Tu cuenta ha sido eliminada correctamente', 'Cuenta eliminada');
        this.sharedService.logout();
        this.router.navigate([APP_ROUTES.home]);
      },
      error: (error) => {
        console.error('Error al eliminar cuenta:', error);
      }
    });
  }

  toggleVipStatus(): void {
    this.usuarioService.toggleVipStatus(this.userProfile.flagVIP).subscribe({
      next: () => {
        this.userProfile.flagVIP = !this.userProfile.flagVIP;
        this.showVipConfirmation = false;
        this.showCancelVipConfirmation = false;
        this.notificationService.success(
          this.userProfile.flagVIP
            ? 'Te has suscrito correctamente a VIP.'
            : 'Has cancelado tu suscripción VIP.',
          'Estado VIP actualizado'
        );
      },
      error: (error) => {
        console.error('Error al actualizar estado VIP:', error);
        this.notificationService.error('No se pudo actualizar el estado VIP. Inténtalo de nuevo.');
      }
    });
  }

  confirmChangePassword(): void {
    if (!this.formValidationService.validateFormBeforeSubmit(this.passwordForm)) {
      return;
    }

    this.usuarioService.changePassword(this.userProfile.idUsuario, this.passwordForm).subscribe({
      next: () => {
        this.notificationService.success('Tu contraseña ha sido actualizada con éxito', 'Contraseña cambiada');
        this.cancelChangePassword();
      },
      error: (error) => {
        this.notificationService.error('No se pudo cambiar la contraseña. Por favor, inténtalo de nuevo.');
      }
    });
  }

  // =============================================================================
  // GESTIÓN DE ARCHIVOS Y MODALES
  // =============================================================================

  async onPhotoUpload(event: Event): Promise<void> {
    const result = await this.sharedService.handlePhotoUpload(event, 5);
    
    switch (result.status) {
      case 'SUCCESS':
        this.profilePhotoFile = result.file!;
        this.userProfile.fotoPerfil = result.previewUrl!;
        this.profileForm.patchValue({ fotoPerfil: result.previewUrl! });
        break;
      case 'ERROR_INVALID_TYPE':
      case 'ERROR_TOO_LARGE':
      case 'ERROR_NO_FILE':
        this.notificationService.warning(result.message ?? 'Error al subir la foto');
        break;
    }
  }

  cancelEdit(): void {
    this.isEditMode = false;
    this.profileForm.patchValue({
      nombre: this.userProfile.nombre,
      primerApellido: this.userProfile.primerApellido,
      segundoApellido: this.userProfile.segundoApellido,
      alias: this.userProfile.alias,
      fechaNacimiento: this.formatDateForInput(this.userProfile.fechaNacimiento)
    });
    this.profileForm.markAsUntouched();
    this.profileForm.markAsPristine();
  }

  toggleEditMode(): void {
    this.isEditMode = !this.isEditMode;
    if (this.isEditMode) {
      this.profileForm.patchValue({
        nombre: this.userProfile.nombre,
        primerApellido: this.userProfile.primerApellido,
        segundoApellido: this.userProfile.segundoApellido,
        alias: this.userProfile.alias,
        fechaNacimiento: this.formatDateForInput(this.userProfile.fechaNacimiento)
      });
    }
  }

  showDeleteAccount(): void {
    this.showDeleteConfirmation = true;
  }

  cancelDelete(): void {
    this.showDeleteConfirmation = false;
  }
  
  showUpgradeToVip(): void {
    this.showVipConfirmation = true;
  }

  cancelVipUpgrade(): void {
    this.showVipConfirmation = false;
  }
  
  showCancelVip(): void {
    this.showCancelVipConfirmation = true;
  }

  cancelVipCancel(): void {
    this.showCancelVipConfirmation = false;
  }

  changePassword(): void {
    this.showChangePasswordModal = true;
    this.passwordForm.reset();
  }

  cancelChangePassword(): void {
    this.showChangePasswordModal = false;
    this.passwordForm.reset();
  }

  // =============================================================================
  // VALIDACIÓN / UTILIDADES
  // =============================================================================

  isFieldInvalid(fieldName: string): boolean {
    return this.sharedService.isFieldInvalid(this.profileForm, fieldName);
  }

  getFieldError(fieldName: string): string {
    return this.sharedService.getFieldError(this.profileForm, fieldName);
  }

  preventKeyboardInput(event: KeyboardEvent): void {
    this.sharedService.preventKeyboardInput(event);
  }

  private formatDateForInput(dateString: string): string {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toISOString().split('T')[0];
  }

  // =============================================================================
  // 2FA / 3FA
  // =============================================================================

  activate2FA(): void {
    this.router.navigate(['/verify-email']);
  }

  activate3FA(): void {
    this.router.navigate([APP_ROUTES.thirdFactor], { 
      state: { thirdFactorEnabled: false } 
    });
  }

  disable2FA(): void {
    this.usuarioService.disable2FA().subscribe({
      next: () => {
        this.userProfile.twoFactorEnabled = false;
      },
      error: (error) => {
        console.error('Error al desactivar 2FA:', error);
      }
    });
  }

  disable3FA(): void {
    this.usuarioService.disable3FA().subscribe({
      next: () => {
        this.userProfile.thirdFactorEnabled = false;
      },
      error: (error) => {
        console.error('Error al desactivar 3FA:', error);
      }
    });
  }
}
