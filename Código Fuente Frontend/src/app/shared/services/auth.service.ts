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
      
      
      // Guardar token en localStorage
      localStorage.setItem('token', jwtToken);
      
      const credentials = this.sharedService.obtainCredentials();
      
      
      if(twoFactorEnabled) {
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
      const userData = await firstValueFrom(this.usuarioService.getUsuario(userId));
      
      // Procesar y guardar los datos ya parseados para evitar procesamiento repetido
      const processedUserData = {
        idUsuario: userData.idUsuario,
        nombre: userData.nombre,
        primerApellido: userData.apellidos.split(' ')[0] || '',
        segundoApellido: userData.apellidos.split(' ')[1] || '',
        email: userData.email,
        alias: userData.alias,
        fechaNacimiento: userData.fechaNacimiento,
        fotoPerfil: userData.fotoPerfil ?? 'assets/porDefecto.jpg',
        flagVIP: userData.flagVIP,
        twoFactorEnabled: userData.twoFactorEnabled,
        thirdFactorEnabled: userData.thirdFactorEnabled

      };
      
      localStorage.setItem('user_data', JSON.stringify(processedUserData));
      
      
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
      
      const creadorData = await firstValueFrom(this.personalService.getCreador(userId));
      
      
      // Procesar y guardar los datos ya parseados para evitar procesamiento repetido
      const processedGestorData = {
        idUsuario: creadorData.idUsuario,
        tipo: 'gestor',
        nombre: creadorData.nombre,
        primerApellido: creadorData.apellidos.split(' ')[0] || '',
        segundoApellido: creadorData.apellidos.split(' ')[1] || '',
        email: creadorData.email,
        alias: creadorData.alias,
        tipoContenido: creadorData.tipoContenido,
        especialidad: creadorData.especialidad,
        descripcion: creadorData.descripcion,
        fotoPerfil: creadorData.fotoPerfil ?? 'assets/porDefecto.jpg',
        twoFactorEnabled: creadorData.twoFactorEnabled,
        thirdFactorEnabled: creadorData.thirdFactorEnabled
      };
      
      localStorage.setItem('gestor_data', JSON.stringify(processedGestorData));
      

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
      const adminData = await firstValueFrom(this.personalService.getAdministrador(userId));
      
      // Procesar y guardar los datos ya parseados para evitar procesamiento repetido
      const processedAdminData = {
        idUsuario: adminData.idUsuario,
        nombre: adminData.nombre,
        tipo: 'administrador',
        primerApellido: adminData.apellidos.split(' ')[0] || '',
        segundoApellido: adminData.apellidos.split(' ')[1] || '',
        email: adminData.email,
        alias: adminData.alias,
        fotoPerfil: adminData.fotoPerfil ?? 'assets/porDefecto.jpg',
        departamento: adminData.departamento,
        twoFactorEnabled: adminData.twoFactorEnabled,
        thirdFactorEnabled: adminData.thirdFactorEnabled
      };
      
      localStorage.setItem('admin_data', JSON.stringify(processedAdminData));

    } catch (error) {
      console.error('Error al obtener datos de administrador:', error);
      throw error;
    }
  }


  
  /**
   * Limpia la sesión antes de hacer login (útil cuando el usuario vuelve al login)
   */
  clearSessionBeforeLogin(): void {
    const itemsToClear = ['token', 'authToken', 'user_data', 'gestor_data', 'admin_data'];
    for (const item of itemsToClear) {
      localStorage.removeItem(item);
    }
  }
}
