import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import {
  APP_ROUTES,
  FormValidationService,
  SharedService,
} from '@shared';
import { UsuarioService } from '../../../usuario/usuario.service';
import { PersonalService } from '../../../personal/personal.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './verify-email.component.html',
  styleUrls: ['./verify-email.component.scss'],
})
export class VerifyEmailComponent implements OnInit {
  public readonly APP_ROUTES = APP_ROUTES;

  // Estados del componente
  verifyEmailForm!: FormGroup;
  isLoading = false;
  codeVerified = false;
  errorMessage = '';
  thirdFactorEnabled = false; // Flag recibido desde el login

  constructor(
    private readonly formValidationService: FormValidationService,
    private readonly sharedService: SharedService,
    private readonly usuarioService: UsuarioService,
    private readonly personalService: PersonalService,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {
    // Recibir el state de la navegación
    this.thirdFactorEnabled = history.state?.thirdFactorEnabled ?? false;
    
  }

  ngOnInit(): void {
    this.initializeForm();
    this.sendEmail(); // Enviar email automáticamente al cargar el componente
  }

  /**
   * Inicializa el formulario de verificación
   */
  private initializeForm(): void {
    this.verifyEmailForm = this.formValidationService.createVerifyEmailForm();
  }

  /**
   * Envía el email de segundo factor según el rol del usuario
   */
  sendEmail(): void {
    

    const credentials = this.sharedService.obtainCredentials();
    if (!credentials) {
      this.errorMessage = 'No se pudo obtener las credenciales del usuario';
      return;
    }
    const { rol } = credentials;

    switch (rol) {
      case 'NORMAL': {
        this.usuarioService.sendTwoFactorEmail().subscribe({
          next: () => {
            
          },
          error: (error) => {
            console.error( '❌ Error al enviar email de 2FA para NORMAL:', error);
          },
        });
        break;
      }
      case 'CREADOR': {
        this.personalService.sendTwoFactorEmailCreator().subscribe({
          next: () => {
            
          },
          error: (error) => {
            console.error('❌ Error al enviar email de 2FA para CREADOR:',error);
          },
        });
        break;
      }
      case 'ADMINISTRADOR': {
        this.personalService.sendTwoFactorEmailAdmin().subscribe({
          next: () => {
            
          },
          error: (error) => {
            console.error('❌ Error al enviar email de 2FA para ADMINISTRADOR:', error);
          },
        });
        break;
      }
      default:
        console.error('Rol no válido para segundo factor:', rol);
        throw new Error(`Rol no válido para segundo factor: ${rol}`);
    }
  }

  /**
   * Maneja el envío del formulario de verificación
   */
  onVerifyEmail(): void {
    if (this.verifyEmailForm.invalid || this.isLoading) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    

    // Obtener credenciales del usuario
    const credentials = this.sharedService.obtainCredentials();
    if (!credentials) {
      this.isLoading = false;
      this.errorMessage = 'No se pudo obtener las credenciales del usuario';
      return;
    }
    
    const code = this.verifyEmailForm.get('verificationCode')?.value;

    
    this.verifyEmailByRole(credentials.rol, credentials.userId, code);
  }

  /**
   * Verifica el código de email según el rol del usuario
   */
  private verifyEmailByRole(rol: string, userId: string, code: string): void {
    switch (rol) {
      case 'NORMAL':
        this.usuarioService.verifyEmailCode(code).subscribe({
          next: (response) => {
            this.loginAfterVerification(userId, rol);
          },
          error: (error) => {
            this.isLoading = false;
            this.errorMessage =
              'Error al verificar el código. Por favor, inténtalo de nuevo.';
            console.error('Error durante la verificación para NORMAL:', error);
          },
        });
        break;
      case 'CREADOR':
        this.personalService.verifyEmailCodeCreator(code).subscribe({
          next: (response) => {
            this.loginAfterVerification(userId, rol);
          },
          error: (error) => {
            this.isLoading = false;
            this.errorMessage =
              'Error al verificar el código. Por favor, inténtalo de nuevo.';
            console.error('Error durante la verificación para CREADOR:', error);
          },
        });
        break;
      case 'ADMINISTRADOR':
        this.personalService.verifyEmailAdmin(code).subscribe({
          next: (response) => {
            this.loginAfterVerification(userId, rol);
          },
          error: (error) => {
            this.isLoading = false;
            this.errorMessage =
              'Error al verificar el código. Por favor, inténtalo de nuevo.';
            console.error(
              'Error durante la verificación para ADMINISTRADOR:',
              error
            );
          },
        });
        break;
      default:
        this.isLoading = false;
        this.errorMessage = `Rol desconocido: ${rol}`;
        console.error(
          '❌ Rol no válido durante la verificación de email:',
          rol
        );
    }
  }

  private loginAfterVerification(userId: string, rol: string): void {
    this.isLoading = false;
    this.codeVerified = true;
    
    
    if (this.thirdFactorEnabled || rol!='NORMAL') {
      
      this.router.navigate([APP_ROUTES.thirdFactor], {
        state: { thirdFactorEnabled: this.thirdFactorEnabled },
      });
    } else {
      
      setTimeout(async () => {
        await this.authService.loadUserDataByRole(userId, rol);
      }, 3000);
    }
  }

  isFieldInvalid(fieldName: string): boolean {
    return this.sharedService.isFieldInvalid(this.verifyEmailForm, fieldName);
  }

  getFieldError(fieldName: string): string {
    return this.sharedService.getFieldError(this.verifyEmailForm, fieldName);
  }
}
