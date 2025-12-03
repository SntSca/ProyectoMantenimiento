import { Component, OnInit } from '@angular/core';
import { FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule} from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormValidationService, SharedService, APP_ROUTES, GESTOR_ROUTES, PERSONAL_ROUTES, USUARIO_ROUTES, AuthService } from '@shared';
import { NotificationService } from '../../../shared/services/notification.service';




@Component({
  selector: 'app-login-personal',
  standalone: true,
  imports: [RouterModule, FormsModule, ReactiveFormsModule, CommonModule],  
  templateUrl: './login-personal.component.html',

})
export class LoginPersonalComponent implements OnInit {

  loginForm!: FormGroup;
  showPassword: boolean = false;
  isLoading: boolean = false;

  public readonly APP_ROUTES = APP_ROUTES;
  public readonly GESTOR_ROUTES = GESTOR_ROUTES;
  public readonly PERSONAL_ROUTES = PERSONAL_ROUTES;
  public readonly USUARIO_ROUTES = USUARIO_ROUTES;



  constructor(
    private readonly router: Router, 
    private readonly formValidationService: FormValidationService,
    private readonly authService: AuthService,
    private readonly sharedService: SharedService,
    private readonly notificationService: NotificationService
  ) {}

  // =============================================================================
  // MÉTODOS DE INICIALIZACIÓN
  // =============================================================================
  ngOnInit(): void {
    this.loginForm = this.formValidationService.createLoginForm();
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
      // Usar el nuevo AuthService centralizado (isPrivileged = true)
      await this.authService.login(formData.email, formData.password, true);
      // El AuthService ya maneja la navegación según el rol (CREADOR o ADMINISTRADOR)
    } catch (error) {
      console.error('Error en el login:', error);
      this.notificationService.error('Por favor, verifica tu correo y contraseña.', 'Error en el login');
    } finally {
      this.isLoading = false;
    }
  }


  // =============================================================================
  // MÉTODOS DE VALIDACIÓN/UTILIDAD
  // =============================================================================

  getFieldError(fieldName: string): string {
    return this.sharedService.getFieldError(this.loginForm, fieldName);
  }

  isFieldInvalid(fieldName: string): boolean {
    return this.sharedService.isFieldInvalid(this.loginForm, fieldName);
  }

  togglePassword(): void {
    this.showPassword = this.sharedService.togglePasswordVisibility(this.showPassword);
  }
}
