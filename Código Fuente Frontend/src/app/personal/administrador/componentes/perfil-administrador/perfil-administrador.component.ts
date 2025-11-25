import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FormValidationService, APP_ROUTES, ADMINISTRADOR_ROUTES, AdministradorStatus, DEPARTAMENTOS, SharedService } from '@shared';
import { AdministradorService } from '../../administrador.service';
import { CambiarPasswordModalComponent } from 'src/app/shared/components/cambiar-password-modal/cambiar-password-modal.component';
import { NotificationService } from '../../../../shared/services/notification.service';



@Component({
  selector: 'app-perfil-administrador',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule, CambiarPasswordModalComponent],
  templateUrl: './perfil-administrador.component.html',
})
export class PerfilAdministradorComponent implements OnInit {

  administradorProfile: AdministradorStatus = {} as AdministradorStatus;

  administradorForm!: FormGroup;
  passwordForm!: FormGroup;
  
  isEditMode: boolean = false;
  showChangePasswordModal: boolean = false;
  profilePhotoFile: File | null = null;

  readonly APP_ROUTES = APP_ROUTES;
  readonly ADMINISTRADOR_ROUTES = ADMINISTRADOR_ROUTES;

  // Datos estáticos
  readonly departamentos = DEPARTAMENTOS;


  constructor(
    private readonly router: Router,
    private readonly formValidationService: FormValidationService,
    private readonly administradorService: AdministradorService,
    private readonly sharedService: SharedService,
    private readonly notificationService: NotificationService
  ) { }


  // =============================================================================
  // MÉTODOS DE INICIALIZACIÓN
  // =============================================================================

  ngOnInit(): void {
    
    
    if(localStorage.getItem('admin_data')){
      this.administradorProfile = JSON.parse(localStorage.getItem('admin_data')!);
    }

    this.administradorProfile.fotoPerfil ??= 'assets/porDefecto.jpg';
    // Procesar la foto de perfil para mostrarla
    this.processProfilePhoto();
    this.initializeForms();
  }

  private initializeForms(): void {
    // Crear el formulario de perfil pasando el alias actual para excluirlo de la validación de unicidad
    this.administradorForm = this.formValidationService.createAdministradorProfileForm();
    this.passwordForm = this.formValidationService.createChangePasswordForm();
    
    // Prellenar el formulario con los datos del usuario (sin departamento, no es editable)
    this.administradorForm.patchValue({
      nombre: this.administradorProfile.nombre,
      primerApellido: this.administradorProfile.primerApellido,
      segundoApellido: this.administradorProfile.segundoApellido,
      alias: this.administradorProfile.alias,
      fotoPerfil: this.administradorProfile.fotoPerfil
    });
  }

  private processProfilePhoto(): void {
    // Las fotos ahora vienen como URI, no necesitan procesamiento
    if (this.administradorProfile.fotoPerfil && !this.administradorProfile.fotoPerfil.startsWith('blob:') && !this.administradorProfile.fotoPerfil.startsWith('assets/') && !this.administradorProfile.fotoPerfil.startsWith('data:')) {
      // Ya es una URI válida, no hacer nada
    }
  }

  // =============================================================================
  // MÉTODOS DEL SERVICIO 
  // =============================================================================


  async saveProfile(): Promise<void> {
    if (!this.formValidationService.validateFormBeforeSubmit(this.administradorForm)) {
      return;
    }

    const formData = this.administradorForm.value;
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
    
    
    this.administradorService.guardarEdicion(this.administradorProfile.idUsuario, this.administradorProfile.tipo, formData).subscribe({
      next: (response) => {
        this.administradorProfile = {
          ...this.administradorProfile,
          nombre: formData.nombre,
          primerApellido: formData.apellidos.split(' ')[0] || '',
          segundoApellido: formData.apellidos.split(' ')[1] || '',
          alias: formData.alias
        };

        localStorage.setItem('admin_data', JSON.stringify(this.administradorProfile));

        this.isEditMode = false;
        this.notificationService.success('Tu perfil ha sido actualizado correctamente.', 'Perfil actualizado');
      },
      error: (error) => {
        console.error('Error al actualizar perfil:', error);
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
        this.administradorProfile.fotoPerfil = result.previewUrl!;
        break;
      case 'ERROR_INVALID_TYPE':
      case 'ERROR_TOO_LARGE':
      case 'ERROR_NO_FILE':
        this.notificationService.warning(result.message ?? 'Error al procesar la foto de perfil.');
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

  confirmChangePassword(): void {
    if (!this.formValidationService.validateFormBeforeSubmit(this.passwordForm)) {
      return;
    }
    

    this.administradorService.changePassword(this.administradorProfile.idUsuario, this.passwordForm).subscribe({
      next: () => {
        
        this.notificationService.success('Tu contraseña ha sido cambiada correctamente.', 'Contraseña actualizada');
        this.cancelChangePassword();
      },
      error: (error) => {
        this.notificationService.error('No se pudo cambiar la contraseña. Inténtalo de nuevo.');
      }
    });
  }
  
  toggleEditMode(): void {
    this.isEditMode = !this.isEditMode;
    if (this.isEditMode) {
      // Recargar datos en el formulario (sin departamento, no es editable)
      this.administradorForm.patchValue({
        nombre: this.administradorProfile.nombre,
        primerApellido: this.administradorProfile.primerApellido,
        segundoApellido: this.administradorProfile.segundoApellido,
        alias: this.administradorProfile.alias
      });
    }
  }

    cancelEdit(): void {
    this.isEditMode = false;
    // Resetear el formulario a los valores originales (sin departamento)
    this.administradorForm.patchValue({
      nombre: this.administradorProfile.nombre,
      primerApellido: this.administradorProfile.primerApellido,
      segundoApellido: this.administradorProfile.segundoApellido,
      alias: this.administradorProfile.alias
    });
    // Limpiar validaciones
    this.administradorForm.markAsUntouched();
    this.administradorForm.markAsPristine();
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
    return this.sharedService.isFieldInvalid(this.administradorForm, fieldName);
  }

  getFieldError(fieldName: string): string {
    return this.sharedService.getFieldError(this.administradorForm, fieldName);
  }

}
