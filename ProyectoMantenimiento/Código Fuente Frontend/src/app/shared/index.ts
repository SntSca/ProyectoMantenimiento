/**
 * ===========================
 * ÍNDICE DE SERVICIOS COMPARTIDOS
 * ===========================
 * 
 * Exportación centralizada de los servicios de validación.
 * Las constantes y tipos se mantienen como implementación interna.
 */

// Servicios principales
export { ApiConfigService } from './services/api-config.service';
export { ContentService } from './services/content.service';
export { ConfigService } from './services/config.service';
export { FormValidationService } from './services/validation.service';
export { SharedService } from './services/shared.services';
export { FilterService } from './services/filter.service';
export { AuthService } from './services/auth.service';
export { ConfirmationService } from './services/confirmation.service';
export { InactivityService } from './services/inactivity.service';

// Guards
export { AuthGuard } from './guards/auth.guard';

// Componentes compartidos
export { CambiarPasswordModalComponent } from './components/cambiar-password-modal/cambiar-password-modal.component';
export { VerifyEmailComponent } from './components/verify-email/verify-email.component';
export { QrCodeComponent } from './components/qr-code/qr-code.component';



// Constantes de rutas
export const APP_ROUTES = {
  home: '/',
  verifyEmail: '/verify-email',
  thirdFactor: '/third-factor'
};
export { USUARIO_ROUTES } from '../usuario/usuario.routes';
export { GESTOR_ROUTES } from '../personal/gestor/gestor.routes';
export { ADMINISTRADOR_ROUTES } from '../personal/administrador/administrador.routes';
export { PERSONAL_ROUTES } from '../personal/personal.routes';


// Interfaces
export * from './constants/interfaces';
export * from './constants/mock_data';

