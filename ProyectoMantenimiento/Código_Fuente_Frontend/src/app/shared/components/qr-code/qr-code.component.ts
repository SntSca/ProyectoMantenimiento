import { Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule} from '@angular/router';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { QRCodeComponent, QRCodeErrorCorrectionLevel } from 'angularx-qrcode';
import { APP_ROUTES, FormValidationService, SharedService} from '@shared';
import { AuthService } from '../../services/auth.service';
import { UsuarioService } from '../../../usuario/usuario.service';
import { PersonalService } from '../../../personal/personal.service';

@Component({
  selector: 'app-qr-code',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, QRCodeComponent],
  templateUrl: './qr-code.component.html',
  styleUrls: ['../verify-email/verify-email.component.scss']
})
export class QrCodeComponent implements OnInit {

  public readonly APP_ROUTES = APP_ROUTES;

  // Estados del componente
  qrVerifyForm!: FormGroup;
  isLoading = false;
  codeVerified = false;
  errorMessage = '';
  
  // Control de vistas
  showQR = true; // true = muestra QR, false = muestra verificación de código
  canGoBackToQR = false; // true = permite volver al QR (solo si el usuario lo escaneó manualmente)
  

  qrCodeUrl = '';
  secretKey = '';
  
  
  // Configuración del QR
  errorLevel: QRCodeErrorCorrectionLevel = 'M';
  darkColor = '#000000';
  lightColor = '#ffffff';

  constructor(
    private readonly formValidationService: FormValidationService,
    private readonly sharedService: SharedService,
    private readonly usuarioService: UsuarioService,
    private readonly personalService: PersonalService,
    private readonly authService: AuthService,
  ) {
    // Recibir thirdFactorEnabled del state de la navegación
    const thirdFactorEnabled = history.state?.thirdFactorEnabled ?? false;
    
    // Si thirdFactorEnabled = true → viene del login, mostrar verificación directamente
    // Si thirdFactorEnabled = false → primera vez activando 3FA, mostrar QR
    this.showQR = !thirdFactorEnabled;
    this.canGoBackToQR = false;
    
  }
  

  ngOnInit(): void {
    this.initializeForm();
    if(this.showQR){
      this.loadQRData();
    }
  }



  /**
   * Inicializa el formulario de verificación del código del autenticador
   */
  private initializeForm(): void {
    this.qrVerifyForm = this.formValidationService.createVerifyEmailForm();
  }


  private loadQRData(): void {
    
    
    // Obtener credenciales del usuario
    const credentials = this.sharedService.obtainCredentials();
    
    if (!credentials) {
      this.errorMessage = 'No se pudo obtener las credenciales del usuario';
      return;
    }

    const { rol } = credentials;
    

    // Llamar al endpoint correspondiente según el rol
    this.generateQRByRole(rol);
  }

  /**
   * Genera el QR llamando al endpoint correspondiente según el rol del usuario
   */
  private generateQRByRole(rol: string): void {
    this.isLoading = true;

    switch (rol) {
      case 'NORMAL':
        
        this.usuarioService.generateQRCode().subscribe({
          next: (response) => {
            this.handleQRResponse(response);
          },
          error: (error) => {
            this.handleQRError(error, 'NORMAL');
          }
        });
        break;

      case 'CREADOR':
        
        this.personalService.generateQRCreator().subscribe({
          next: (response) => {
            this.handleQRResponse(response);
          },
          error: (error) => {
            this.handleQRError(error, 'CREADOR');
          }
        });
        break;

      case 'ADMINISTRADOR':
        
        this.personalService.generateQRAdmin().subscribe({
          next: (response) => {
            this.handleQRResponse(response);
          },
          error: (error) => {
            this.handleQRError(error, 'ADMINISTRADOR');
          }
        });
        break;

      default:
        this.isLoading = false;
        this.errorMessage = `Rol desconocido: ${rol}`;
        console.error('❌ Rol no válido:', rol);
    }
  }

  /**
   * Maneja la respuesta exitosa del backend con los datos del QR
   */
  private handleQRResponse(response: any): void {
    this.isLoading = false;

    
    this.qrCodeUrl = response.qrCodeUrl 
    this.secretKey = response.secretKey 
    
    
  }

  /**
   * Maneja los errores al generar el QR
   */
  private handleQRError(error: any, rol: string): void {
    this.isLoading = false;
    this.errorMessage = 'Error al generar el código QR. Por favor, inténtalo de nuevo.';
    console.error(`❌ Error al generar QR para ${rol}:`, error);
  }

  /**
   * Maneja cuando el usuario indica que ya escaneó el QR
   */
  onQRScanned(): void {
    this.showQR = false; // Cambiar a vista de verificación
    this.canGoBackToQR = true; // Permitir volver al QR
    this.errorMessage = '';
    
  }

  /**
   * Vuelve a mostrar el código QR
   */
  backToQR(): void {
    this.showQR = true; // Volver a mostrar QR
    this.errorMessage = '';
    this.qrVerifyForm.reset();
    
  }

  /**
   * Maneja el envío del formulario de verificación del código
   */
  onVerifyCode(): void {
    if (this.qrVerifyForm.invalid || this.isLoading) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const code = this.qrVerifyForm.get('verificationCode')?.value;
    
    // Obtener credenciales del usuario
    const credentials = this.sharedService.obtainCredentials();
    if (!credentials) {
      this.errorMessage = 'No se pudo obtener las credenciales del usuario';
      this.isLoading = false;
      return;
    }

    const rol = credentials.rol;
    const userId = credentials.userId;
    // Determinar si es confirmación (primera vez) o verificación (login)
    // canGoBackToQR = true significa que el usuario escaneó el QR manualmente (primera vez)
    const isFirstTime = this.canGoBackToQR;
    
    // Llamar al método correspondiente según rol y flujo
    this.verifyTOTPByRole(rol, userId, code, isFirstTime);
  }

  /**
   * Verifica el código TOTP llamando al endpoint correspondiente según el rol y el flujo
   */
  private verifyTOTPByRole(rol: string, userId: string, code: string, isFirstTime: boolean): void {
    let observable: any;

    switch (rol) {
      case 'NORMAL':
        observable = isFirstTime 
          ? this.usuarioService.confirm3fAsetup(code)
          : this.usuarioService.verify3fACode(code);
        
        break;

      case 'CREADOR':
        observable = isFirstTime
          ? this.personalService.confirm3fAsetupCreator(code)
          : this.personalService.verify3fACodeCreator(code);
        
        break;

      case 'ADMINISTRADOR':
        observable = isFirstTime
          ? this.personalService.confirm3fAsetupAdmin(code)
          : this.personalService.verify3fACodeAdmin(code);
        
        break;

      default:
        this.isLoading = false;
        this.errorMessage = `Rol desconocido: ${rol}`;
        console.error('❌ Rol no válido:', rol);
        return;
    }

    // Ejecutar la petición
    observable.subscribe({
      next: (response: any) => {
        this.handleVerifySuccess(response, userId, rol);
      },
      error: (error: any) => {
        this.handleVerifyError(error, rol);
      }
    });
  }

  /**
   * Maneja la respuesta exitosa de la verificación TOTP
   */
  private handleVerifySuccess(response: any, userId:string, rol: string): void {
    this.isLoading = false;
    this.codeVerified = true;
    
    
    // Timeout de 3 segundos para que el usuario pueda leer el mensaje de éxito
    setTimeout(async () => {
      await this.authService.loadUserDataByRole(userId, rol);
    }, 3000);
  }



  /**
   * Maneja los errores en la verificación TOTP
   */
  private handleVerifyError(error: any, rol: string): void {
    this.isLoading = false;
    this.errorMessage = 'Código incorrecto. Por favor, verifica e inténtalo de nuevo.';
    console.error(`❌ Error al verificar código TOTP para ${rol}:`, error);
  }

  /**
   * Verifica si un campo del formulario es inválido
   */
  isFieldInvalid(fieldName: string): boolean {
    return this.sharedService.isFieldInvalid(this.qrVerifyForm, fieldName);
  }

  /**
   * Obtiene el mensaje de error para un campo específico
   */
  getFieldError(fieldName: string): string {
    return this.sharedService.getFieldError(this.qrVerifyForm, fieldName);
  }
}
