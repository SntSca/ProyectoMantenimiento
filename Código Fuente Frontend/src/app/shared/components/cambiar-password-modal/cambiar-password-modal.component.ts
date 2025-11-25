import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { FormValidationService, APP_ROUTES, SharedService } from '@shared';

@Component({
  selector: 'app-cambiar-password-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './cambiar-password-modal.component.html'
})
export class CambiarPasswordModalComponent {
  @Input() visible: boolean = false;
  @Input() passwordForm!: FormGroup;
  @Input() variant: string = 'gestor-variant';
  @Input() navigateOnConfirm: boolean = true;
  @Input() title: string = 'Cambiar Contraseña';
  @Input() confirmButtonText: string = 'Cambiar Contraseña';

  @Output() confirm = new EventEmitter<void>();
  @Output() cancelChange = new EventEmitter<void>();

  // Propiedades para toggle de contraseña
  showCurrentPassword: boolean = false;
  showNewPassword: boolean = false;
  showConfirmPassword: boolean = false;

  constructor(
    private readonly formValidationService: FormValidationService,
    private readonly router: Router,
    private readonly sharedService: SharedService
  ) {}

  // Método para manejar teclas en el modal
  onModalKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      this.cancelChange.emit();
    }
  }

  isFieldInvalid(fieldName: string): boolean {
    return this.sharedService.isFieldInvalid(this.passwordForm, fieldName);
  }

  getFieldError(fieldName: string): string {
    return this.sharedService.getFieldError(this.passwordForm, fieldName);
  }

  isSubmitDisabled(): boolean {
    return this.passwordForm.invalid 
  }

  getPasswordRequirements(): readonly string[] {
    return this.sharedService.getPasswordRequirements();
  }

  togglePassword(field: string): void {
    if (field === 'currentPassword') {
      this.showCurrentPassword = this.sharedService.togglePasswordVisibility(this.showCurrentPassword);
    } else if (field === 'newPassword') {
      this.showNewPassword = this.sharedService.togglePasswordVisibility(this.showNewPassword);
    } else if (field === 'confirmPassword') {
      this.showConfirmPassword = this.sharedService.togglePasswordVisibility(this.showConfirmPassword);
    }
  }

  onConfirm(): void {
    this.confirm.emit();
    if (this.navigateOnConfirm) {
      this.router.navigate([APP_ROUTES.home]);
    }
  }
}
