import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormGroup } from '@angular/forms';
import { CambiarPasswordModalComponent } from '../cambiar-password-modal/cambiar-password-modal.component';
import { FormValidationService, SharedService, APP_ROUTES } from '../../index';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, CambiarPasswordModalComponent],
  templateUrl: './reset-password.component.html',
})
export class ResetPasswordComponent implements OnInit {

  token: string = '';
  passwordForm!: FormGroup;
  showModal: boolean = false;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly formValidationService: FormValidationService,
    private readonly sharedService: SharedService,
    private readonly notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    // Si hay token al llegar a reset-password, limpiarlo
    this.sharedService.clearSession();
    this.token = this.route.snapshot.params['resetToken'];
    if (!this.token) {
      this.notificationService.error('El token de recuperación no es válido.', 'Token inválido');
      this.router.navigate([APP_ROUTES.home]);
      return;
    }
    this.passwordForm = this.formValidationService.createResetPasswordForm();
    this.showModal = true;
  }

  onConfirm(): void {
    if (!this.formValidationService.validateFormBeforeSubmit(this.passwordForm)) {
      return;
    }

    const newPassword = this.passwordForm.value.newPassword;

    this.sharedService.resetPassword(this.token, newPassword).then(() => {
      this.notificationService.success('Tu contraseña ha sido restablecida correctamente.', 'Contraseña actualizada');
      this.router.navigate([APP_ROUTES.home]);
    }).catch((error) => {
      console.error('Error al resetear contraseña:', error);
      this.notificationService.error('No se pudo restablecer la contraseña. Inténtalo de nuevo.');
    });
  }

  onCancel(): void {
    this.showModal = false;
    this.router.navigate([APP_ROUTES.home]);
  }
}