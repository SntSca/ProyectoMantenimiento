import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FormValidationService, APP_ROUTES, GESTOR_ROUTES , Especialidades, TIPO_CONTENIDOS, MOCK_GESTORES, GestorStatus, SharedService } from '@shared';
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
  ) { }



  // =============================================================================
  // MÉTODOS DE INICIALIZACIÓN
  // =============================================================================

  ngOnInit(): void {
    
    // Cargar especialidades desde el servicio
    this.sharedService.getEspecialidades().subscribe((especialidades) => {
      this.especialidades = especialidades;
    });
    // Cargar datos del gestor desde localStorage
    if (localStorage.getItem('gestor_data')) {
      this.gestorProfile = JSON.parse(localStorage.getItem('gestor_data')!);
    }
    else{
      this.gestorProfile = MOCK_GESTORES[0];
    }
    // Procesar la foto de perfil para mostrarla
    this.processProfilePhoto();
    this.initializeForms();
  }

  private initializeForms(): void {
    // Crear el formulario de perfil pasando el alias actual para excluirlo de la validación de unicidad
    this.gestorForm= this.formValidationService.createGestorProfileForm();
    this.passwordForm = this.formValidationService.createChangePasswordForm();
    
    // Prellenar el formulario con los datos del usuario (excepto email)
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
    // Las fotos ahora vienen como URI, no necesitan procesamiento
    if (this.gestorProfile.fotoPerfil && !this.gestorProfile.fotoPerfil.startsWith('blob:') && !this.gestorProfile.fotoPerfil.startsWith('data:')) {
      // Ya es una URI válida, no hacer nada
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
      next: (response) => {
        this.gestorProfile = {
          ...this.gestorProfile,
          nombre: formData.nombre,
          primerApellido: formData.apellidos.split(' ')[0],
          segundoApellido: formData.apellidos.split(' ')[1],
          alias: formData.alias,
          especialidad: formData.especialidad,
          descripcion: formData.descripcion,
        };
        localStorage.setItem('gestor_data', JSON.stringify(this.gestorProfile));
        
        this.isEditMode = false;
        this.notificationService.success('Tu perfil ha sido actualizado correctamente.', 'Perfil actualizado');
      },
      error: (error) => {
        console.error('Error al actualizar perfil:', error);
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
      error: (error) => {
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
      }
    });
  }  // =============================================================================
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
    // Resetear el formulario de contraseña
    this.passwordForm.reset();
  }

  cancelChangePassword(): void {
    this.showChangePasswordModal = false;
    this.passwordForm.reset();
  }

  // Mostrar confirmación de baja
  showDeleteAccount(): void {
    this.showDeleteConfirmation = true;
  }

  cancelDelete(): void {
    this.showDeleteConfirmation = false;
  }

  toggleEditMode(): void {
    this.isEditMode = !this.isEditMode;
    if (this.isEditMode) {
      // Recargar datos en el formulario
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
    // Resetear el formulario a los valores originales
    this.gestorForm.patchValue({
      nombre: this.gestorProfile.nombre,
      primerApellido: this.gestorProfile.primerApellido,
      segundoApellido: this.gestorProfile.segundoApellido,
      alias: this.gestorProfile.alias,
      especialidad: this.gestorProfile.especialidad,
      descripcion: this.gestorProfile.descripcion,
      fotoPerfil: this.gestorProfile.fotoPerfil
    });
    // Limpiar validaciones
    this.gestorForm.markAsUntouched();
    this.gestorForm.markAsPristine();
  }

  onModalKeyDown(event: KeyboardEvent): void {
    // Example: close modal on Escape key
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

