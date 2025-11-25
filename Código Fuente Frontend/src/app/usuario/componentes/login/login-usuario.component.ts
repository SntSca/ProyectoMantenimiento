import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup} from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { FormValidationService, SharedService, APP_ROUTES, USUARIO_ROUTES, GESTOR_ROUTES, PERSONAL_ROUTES, AuthService } from '@shared';
import { NotificationService } from '../../../shared/services/notification.service';

@Component({
  selector: 'app-login-usuario',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login-usuario.component.html',
})
export class LoginUsuarioComponent implements OnInit {

  
  // Propiedades del componente
  loginForm!: FormGroup;
  showPassword = false;
  isLoading = false;

  // Constantes de rutas para usar en templates
  public readonly APP_ROUTES = APP_ROUTES;
  public readonly USUARIO_ROUTES = USUARIO_ROUTES;
  public readonly GESTOR_ROUTES = GESTOR_ROUTES;
  public readonly PERSONAL_ROUTES = PERSONAL_ROUTES;

  constructor(
    private readonly router: Router, 
    private readonly formValidationService: FormValidationService,
    private readonly authService: AuthService,
    private readonly sharedService: SharedService,
    private readonly notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loginForm = this.formValidationService.createLoginForm();
    // Limpiar sesión si existe alguna al llegar al login
    this.authService.clearSessionBeforeLogin();
  }


  // =============================================================================
  // MÉTODOS DEL SERVICIO
  // =============================================================================

  async onLogin(): Promise<void> {
    if (this.isLoading) return;
    
    if (!this.formValidationService.validateFormBeforeSubmit(this.loginForm)) {
      return;
    }
    
    this.isLoading = true;
    
    const formData = this.loginForm.value;
    
    
    try {
      // Usar el nuevo AuthService centralizado
      await this.authService.login(formData.email, formData.password, false);
      // El AuthService ya maneja la navegación
    } catch (error) {
      console.error('Error en el login:', error);
      this.notificationService.error('Por favor, verifica tu correo y contraseña', 'Error en el login');
    } finally {
      this.isLoading = false;
    }
  }

  onForgotPassword(event: Event): void {
    event.preventDefault();
  }

  // =============================================================================
  // MÉTODOS DE VALIDACIÓN/UTILIDAD
  // =============================================================================
  
  isFieldInvalid(fieldName: string): boolean {
    return this.sharedService.isFieldInvalid(this.loginForm, fieldName);
  }

  getFieldError(fieldName: string): string {
    return this.sharedService.getFieldError(this.loginForm, fieldName);
  }

  togglePassword(): void {
    this.showPassword = this.sharedService.togglePasswordVisibility(this.showPassword);
  }
}
