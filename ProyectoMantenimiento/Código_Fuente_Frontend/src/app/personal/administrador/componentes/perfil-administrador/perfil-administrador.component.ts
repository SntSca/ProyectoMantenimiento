import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  FormValidationService,
  APP_ROUTES,
  ADMINISTRADOR_ROUTES,
  AdministradorStatus,
  DEPARTAMENTOS,
  SharedService
} from '@shared';
import { AdministradorService } from '../../administrador.service';
import { CambiarPasswordModalComponent } from 'src/app/shared/components/cambiar-password-modal/cambiar-password-modal.component';
import { NotificationService } from '../../../../shared/services/notification.service';
import { firstValueFrom } from 'rxjs';

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

  isEditMode = false;
  showChangePasswordModal = false;
  profilePhotoFile: File | null = null;

  readonly APP_ROUTES = APP_ROUTES;
  readonly ADMINISTRADOR_ROUTES = ADMINISTRADOR_ROUTES;
  readonly departamentos = DEPARTAMENTOS;

  constructor(
    private readonly router: Router,
    private readonly formValidationService: FormValidationService,
    private readonly administradorService: AdministradorService,
    private readonly sharedService: SharedService,
    private readonly notificationService: NotificationService
  ) {}

  // ============================================================================
  // INIT
  // ============================================================================

  ngOnInit(): void {
    this.loadAdministrator().then(() => {
      this.administradorProfile.fotoPerfil ??= 'assets/porDefecto.jpg';
      this.initializeForms();
    });
  }

  /**
   * Carga el administrador actual exactamente igual que InicioAdministrador,
   */
  private async loadAdministrator(): Promise<void> {
    try {
      const credentials = this.sharedService.obtainCredentials();
      if (!credentials?.userId || credentials?.rol !== 'ADMINISTRADOR') {
        throw new Error('Credenciales inválidas');
      }

      const response = await firstValueFrom(this.administradorService.getAllUsers());
      const admins = response.administrators || [];

      const admin = admins.find(a => a.idUsuario === credentials.userId);

      if (!admin) throw new Error('No encontrado');


      this.administradorProfile = {
        idUsuario: admin.idUsuario,
        tipo: 'administrador',
        nombre: admin.nombre,
        primerApellido: (admin.primerApellido ?? ''),
        segundoApellido: (admin.segundoApellido ?? ''),
        email: admin.email,
        alias: admin.alias,
        fotoPerfil: admin.fotoPerfil ?? 'assets/porDefecto.jpg',
        departamento: admin.departamento,
        bloqueado: admin.bloqueado ?? false,
        twoFactorEnabled: admin.twoFactorEnabled,
        thirdFactorEnabled: admin.thirdFactorEnabled
      };

    } catch (error) {
      console.error('[PerfilAdministrador] Error cargando admin:', error);
    }
  }

  private initializeForms(): void {
    this.administradorForm = this.formValidationService.createAdministradorProfileForm();
    this.passwordForm = this.formValidationService.createChangePasswordForm();

    this.administradorForm.patchValue({
      nombre: this.administradorProfile.nombre,
      primerApellido: this.administradorProfile.primerApellido,
      segundoApellido: this.administradorProfile.segundoApellido,
      alias: this.administradorProfile.alias,
      fotoPerfil: this.administradorProfile.fotoPerfil
    });
  }

  // ============================================================================
  // GUARDAR PERFIL
  // ============================================================================

  async saveProfile(): Promise<void> {
    if (!this.formValidationService.validateFormBeforeSubmit(this.administradorForm)) return;

    const formData = this.administradorForm.value;

    // Si se seleccionó una imagen nueva
    if (this.profilePhotoFile) {
      formData.fotoPerfil = await new Promise<string>((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(this.profilePhotoFile!);
        reader.onload = () => resolve(reader.result as string);
        reader.onerror = () => reject(new Error('Error al procesar imagen'));
      });
    }

    formData.apellidos = `${formData.primerApellido} ${formData.segundoApellido}`;

    delete formData.primerApellido;
    delete formData.segundoApellido;

    this.administradorService.guardarEdicion(
      this.administradorProfile.idUsuario,
      this.administradorProfile.tipo,
      formData
    ).subscribe({
      next: () => {
        this.notificationService.success('Tu perfil ha sido actualizado correctamente.', 'Perfil actualizado');

        this.loadAdministrator()
          .then(() => {
            this.initializeForms();
            this.isEditMode = false;
          })
          .catch(err => console.error('[PerfilAdministrador] Error recargando admin después de guardar:', err));
      },
      error: (err) => console.error('Error al actualizar:', err)
    });
  }

  // ============================================================================
  // ARCHIVOS
  // ============================================================================

  async onFileSelected(event: Event): Promise<void> {
    const result = await this.sharedService.handlePhotoUpload(event, 5);

    if (result.status === 'SUCCESS') {
      this.profilePhotoFile = result.file!;
      this.administradorProfile.fotoPerfil = result.previewUrl!;
    } else {
      this.notificationService.warning(result.message ?? 'Error al procesar la foto.');
    }
  }

  // ============================================================================
  // CAMBIAR CONTRASEÑA
  // ============================================================================

  changePassword(): void {
    this.showChangePasswordModal = true;
    this.passwordForm.reset();
  }

  cancelChangePassword(): void {
    this.showChangePasswordModal = false;
    this.passwordForm.reset();
  }

  confirmChangePassword(): void {
    if (!this.formValidationService.validateFormBeforeSubmit(this.passwordForm)) return;

    this.administradorService.changePassword(
      this.administradorProfile.idUsuario,
      this.passwordForm.value
    ).subscribe({
      next: () => {
        this.notificationService.success('Contraseña actualizada correctamente', 'Éxito');
        this.cancelChangePassword();
      },
      error: () => {
        this.notificationService.error('No se pudo cambiar la contraseña.');
      }
    });
  }

  // ============================================================================
  // UI
  // ============================================================================

  toggleEditMode(): void {
    this.isEditMode = !this.isEditMode;

    if (this.isEditMode) {
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
    this.initializeForms();
    this.administradorForm.markAsPristine();
    this.administradorForm.markAsUntouched();
  }

  isFieldInvalid(field: string): boolean {
    return this.sharedService.isFieldInvalid(this.administradorForm, field);
  }

  getFieldError(field: string): string {
    return this.sharedService.getFieldError(this.administradorForm, field);
  }
}
