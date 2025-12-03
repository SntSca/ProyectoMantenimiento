import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { SharedService } from './shared.services';
import { UsuarioService } from '../../usuario/usuario.service';
import { PersonalService } from '../../personal/personal.service';
import { USUARIO_ROUTES } from '../../usuario/usuario.routes';
import { GESTOR_ROUTES } from '../../personal/gestor/gestor.routes';
import { ADMINISTRADOR_ROUTES } from '../../personal/administrador/administrador.routes';
import { APP_ROUTES } from '../index';

/**
 * Servicio centralizado para gestionar toda la lógica de autenticación
 * Consolida la lógica de login que estaba duplicada en múltiples componentes
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly TOKEN_KEY = 'token';

  constructor(
    private readonly sharedService: SharedService,
    private readonly usuarioService: UsuarioService,
    private readonly personalService: PersonalService,
    private readonly router: Router
  ) {}

  /**
   * Login unificado que maneja usuarios normales y privilegiados (gestor/admin)
   * @param email Email del usuario
   * @param password Contraseña del usuario
   * @param isPrivileged true para personal (gestor/admin), false para usuarios normales
   * @returns Promise que se resuelve cuando el login es exitoso
   * @throws Error si el login falla
   */
  async login(email: string, password: string, isPrivileged: boolean = false): Promise<void> {
    // Seleccionar el servicio correcto según el tipo de usuario
    const loginService = isPrivileged ? this.personalService : this.usuarioService;
    
    try {
      // Realizar la llamada de login
      const response = await firstValueFrom(loginService.login(email, password));
      
      const jwtToken = response.token;
      const twoFactorEnabled = response.twoFactorEnabled;
      const thirdFactorEnabled = response.thirdFactorEnabled;
      
      sessionStorage.setItem(this.TOKEN_KEY, jwtToken);
      const cookieName = 'jwt';
      const cookieValue = jwtToken;
      const days = 1; 
      const expires = new Date();
      expires.setTime(expires.getTime() + (days * 24 * 60 * 60 * 1000));
      document.cookie = `${cookieName}=${cookieValue}; expires=${expires.toUTCString()}; path=/;`;
      const credentials = this.sharedService.obtainCredentials();
      
      if (twoFactorEnabled) {
        // Navegar a pantalla de verificación de email
        // Enviamos thirdFactorEnabled en el state para que verify-email sepa si debe activar 3FA
        this.router.navigate([APP_ROUTES.verifyEmail], { 
          state: { thirdFactorEnabled } 
        });
      } else {
        await this.loadUserDataByRole(credentials.userId, credentials.rol);
      }
      
    } catch (error) {
      console.error('Error en login:', error);
      throw error;
    }
  }

  /**
   * Restaura la sesión a partir del token cuando se recarga la página.
   * Llamar en app.component.ts (ngOnInit) o en un guard de rutas.
   */
  async restoreSession(): Promise<void> {
    const token = sessionStorage.getItem(this.TOKEN_KEY);

    // Si no hay token, no hay sesión que restaurar
    if (!token) {
      return;
    }

    try {
      const credentials = this.sharedService.obtainCredentials();

      // Si por lo que sea el token está corrupto o no hay datos, limpiar
      if (!credentials?.userId || !credentials?.rol) {
        this.clearSessionBeforeLogin();
        return;
      }

      const { userId, rol } = credentials;

      // Siempre reconstruimos desde backend según el rol.
      await this.loadUserDataByRole(userId, rol);

    } catch (error) {
      console.error('Error al restaurar sesión desde sessionStorage:', error);
      this.clearSessionBeforeLogin();
    }
  }

  /**
   * Carga los datos del usuario según su rol y redirige a la página correspondiente
   * @param userId ID del usuario
   * @param rol Rol del usuario (NORMAL, CREADOR, ADMINISTRADOR)
   */
  public async loadUserDataByRole(userId: string, rol: string): Promise<void> {
    switch (rol) {
      case 'NORMAL':
        await this.loadNormalUser(userId);
        this.router.navigate([USUARIO_ROUTES.inicio]);
        break;
        
      case 'CREADOR':
        await this.loadCreator(userId);
        this.router.navigate([GESTOR_ROUTES.inicio]);
        break;
        
      case 'ADMINISTRADOR':
        await this.loadAdmin(userId);
        this.router.navigate([ADMINISTRADOR_ROUTES.inicio]);
        break;
        
      default:
        console.error('Rol desconocido:', rol);
        throw new Error(`Rol desconocido: ${rol}`);
    }
  }

  /**
   * Carga y procesa los datos de un usuario normal
   */
  private async loadNormalUser(userId: string): Promise<void> {
    try {
      await firstValueFrom(this.usuarioService.getUsuario(userId));

    } catch (error) {
      console.error('Error al obtener datos de usuario:', error);
      throw error;
    }
  }

  /**
   * Carga y procesa los datos de un creador de contenido (gestor)
   */
  private async loadCreator(userId: string): Promise<void> {
    try {
      await firstValueFrom(this.personalService.getCreador(userId));
      

    } catch (error) {
      console.error('❌ [AUTH] Error al obtener datos de creador:', error);
      throw error;
    }
  }

  /**
   * Carga y procesa los datos de un administrador
   */
  private async loadAdmin(userId: string): Promise<void> {
    try {
      await firstValueFrom(this.personalService.getAdministrador(userId));
      

    } catch (error) {
      console.error('Error al obtener datos de administrador:', error);
      throw error;
    }
  }
  

  clearSessionBeforeLogin(): void {
    const itemsToClear = [
      this.TOKEN_KEY,
      'authToken' // por compatibilidad con código viejo si lo tuvieras
    ];
    for (const item of itemsToClear) {
      sessionStorage.removeItem(item);
      document.cookie = 'jwt=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
    }
  }
}
