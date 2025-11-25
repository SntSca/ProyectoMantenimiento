import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FormValidationService, APP_ROUTES, MOCK_USERS, CambiarPasswordModalComponent, UserStatus, SharedService } from '@shared';
import { VipComponent } from '../vip/vip.component';
import { UsuarioService } from '../../usuario.service';
import { USUARIO_ROUTES } from '../../usuario.routes';
import { NotificationService } from '../../../shared/services/notification.service';


@Component({
  selector: 'app-perfil-usuario',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule, CambiarPasswordModalComponent, VipComponent],
  templateUrl: './perfil-usuario.component.html',

})
export class PerfilUsuarioComponent implements OnInit {

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

  constructor(
    private readonly router: Router,
    private readonly formValidationService: FormValidationService,
    private readonly usuarioService: UsuarioService,
    private readonly sharedService: SharedService,
    private readonly notificationService: NotificationService
  ) {}

  

  // =============================================================================
  // MÉTODOS DE INICIALIZACIÓN
  // =============================================================================

  ngOnInit(): void {

    if (localStorage.getItem('user_data')) {
      // Los datos ya están procesados y guardados, asignar directamente
      this.userProfile = JSON.parse(localStorage.getItem('user_data')!);
    }
    else{
      this.userProfile = MOCK_USERS[0];
    }
    // Procesar la foto de perfil para mostrarla
    this.processProfilePhoto();
    // Cargar
    this.initializeForms();
    
  } 

  private initializeForms(): void {
    // Crear el formulario de perfil pasando el alias actual para excluirlo de la validación de unicidad
    this.profileForm = this.formValidationService.createUserProfileForm();
    this.passwordForm = this.formValidationService.createChangePasswordForm();
    
    // Prellenar el formulario con los datos del usuario (excepto email)
    this.profileForm.patchValue({
      nombre: this.userProfile.nombre,
      primerApellido: this.userProfile.primerApellido,
      segundoApellido: this.userProfile.segundoApellido,
      alias: this.userProfile.alias,
      fechaNacimiento: this.formatDateForInput(this.userProfile.fechaNacimiento)
    });
  }

  private processProfilePhoto(): void {
    // Las fotos ahora vienen como URI, no necesitan procesamiento
    if (this.userProfile.fotoPerfil && !this.userProfile.fotoPerfil.startsWith('blob:') && !this.userProfile.fotoPerfil.startsWith('data:')) {
      // Ya es una URI válida, no hacer nada
    }
  }




  // =============================================================================
  // MÉTODOS DEL SERVICIO
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
      next: (response) => {
        this.userProfile = {
          ...this.userProfile,
          nombre: formData.nombre,
          primerApellido: formData.apellidos.split(' ')[0],
          segundoApellido: formData.apellidos.split(' ')[1],
          alias: formData.alias,
          fechaNacimiento: formData.fechaNacimiento
        };

        localStorage.setItem('user_data', JSON.stringify({
          ...this.userProfile,
          apellidos: formData.apellidos
        }));
        this.isEditMode = false;
        this.notificationService.success('Tu perfil ha sido actualizado con éxito', 'Perfil actualizado');
      },
      error: (error) => {
        console.error('Error al actualizar perfil:', error);
        this.notificationService.error('No se pudo actualizar el perfil. Por favor, inténtalo de nuevo.');
      }
    });
  }


  // Confirmar baja de cuenta
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
      next: (response) => {
        this.userProfile.flagVIP = !this.userProfile.flagVIP;
        localStorage.setItem('user_data', JSON.stringify(this.userProfile));
        this.showVipConfirmation = false;
        this.showCancelVipConfirmation = false;
      },
      error: (error) => {
        console.error('Error al actualizar estado VIP:', error);
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
  // MÉTODOS DE GESTIÓN DE ARCHIVOS Y MODALES
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
    // Resetear el formulario a los valores originales
    this.profileForm.patchValue({
      nombre: this.userProfile.nombre,
      primerApellido: this.userProfile.primerApellido,
      segundoApellido: this.userProfile.segundoApellido,
      alias: this.userProfile.alias,
      fechaNacimiento: this.formatDateForInput(this.userProfile.fechaNacimiento)
    });
    // Limpiar validaciones
    this.profileForm.markAsUntouched();
    this.profileForm.markAsPristine();
  }

  toggleEditMode(): void {
    this.isEditMode = !this.isEditMode;
    if (this.isEditMode) {
      // Recargar datos en el formulario
      this.profileForm.patchValue({
        nombre: this.userProfile.nombre,
        primerApellido: this.userProfile.primerApellido,
        segundoApellido: this.userProfile.segundoApellido,
        alias: this.userProfile.alias,
        fechaNacimiento: this.formatDateForInput(this.userProfile.fechaNacimiento)
      });
    }
  }

  // Mostrar confirmación de baja
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
    // Resetear el formulario de contraseña
    this.passwordForm.reset();
  }

  cancelChangePassword(): void {
    this.showChangePasswordModal = false;
    this.passwordForm.reset();
  }


  // =============================================================================
  // MÉTODOS DE VALIDACIÓN/UTILIDAD
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
    return date.toISOString().split('T')[0]; // Returns yyyy-MM-dd format
  }

  // =============================================================================
  // MÉTODOS PARA 3FA
  // =============================================================================

  /**
   * Activa el 3FA navegando al componente QR
   */
  activate2FA(): void {
    
    // SEGURIDAD: Navegar sin parámetros expuestos para evitar manipulación
    this.router.navigate(['/verify-email']);
  }

  activate3FA(): void {
    
    // SEGURIDAD: Enviar thirdFactorEnabled=true en state para indicar que es activación
    this.router.navigate([APP_ROUTES.thirdFactor], { 
      state: { thirdFactorEnabled: false } 
    });
  }

  /**
   * Desactiva el 2FA del usuario
   */
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

  /**
   * Desactiva el 3FA del usuario
   */
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
