import { Component, OnInit } from '@angular/core';
import { Router, RouterModule} from '@angular/router';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { FormValidationService, APP_ROUTES, SharedService } from '@shared';
import { PersonalService } from '../../personal.service';


@Component({
  selector: 'app-recuperar-password-personal',
  standalone: true,
  imports: [RouterModule, ReactiveFormsModule, CommonModule],
  templateUrl: '../../../shared/components/recuperar-password/recuperar-password.component.html',

})
export class RecuperarPasswordPersonalComponent implements OnInit {


  recoveryForm!: FormGroup;
  isLoading: boolean = false;
  emailSent: boolean = false;
  errorMessage: string = '';


  public readonly APP_ROUTES = APP_ROUTES;

  constructor(
    private readonly router: Router,
    private readonly formValidationService: FormValidationService,
    private readonly sharedService: SharedService,
    private readonly personalService: PersonalService
  ) {}

  // =============================================================================
  // MÉTODOS DE INICIALIZACIÓN
  // =============================================================================

  private initializeForm(): void {
    this.recoveryForm = this.formValidationService.createRecoveryForm();
  }

  ngOnInit(): void {
    // Si hay token al llegar a recuperar-password, limpiarlo
    this.sharedService.clearSession();
    this.initializeForm();
  }


  // =============================================================================
  // MÉTODOS DEL SERVICIO
  // =============================================================================

  async onRecuperarPassword(): Promise<void> {
    if (this.recoveryForm.invalid) {
      this.recoveryForm.markAllAsTouched();
      return;
    }

    const email = this.recoveryForm.get('email')?.value;

    this.isLoading = true;
    this.errorMessage = '';

    try {
      await this.personalService.enviarCorreoRecuperacion(email);
      this.emailSent = true;
    } catch (error) {
      this.errorMessage = 'Error al enviar el correo. Intenta nuevamente.';
      console.error('Error:', error);
    } finally {
      this.isLoading = false;
    }
  }


  // =============================================================================
  // MÉTODOS DE VALIDACIÓN
  // =============================================================================

  isFieldInvalid(fieldName: string): boolean {
    return this.sharedService.isFieldInvalid(this.recoveryForm, fieldName);
  }

  getFieldError(fieldName: string): string {
    return this.sharedService.getFieldError(this.recoveryForm, fieldName);
  }

  get email(): string {
    return this.recoveryForm.get('email')?.value || '';
  }
}