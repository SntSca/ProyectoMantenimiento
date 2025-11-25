import { Injectable } from '@angular/core';
import { HttpClient} from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiConfigService } from './../shared/services/api-config.service';
import { SharedService } from '@shared';

@Injectable({
  providedIn: 'root'
})
export class PersonalService {

  constructor(private http: HttpClient,
     private readonly apiConfig: ApiConfigService,
     private readonly sharedService: SharedService
  ) { }


  // =============================================================================
  // MÉTODOS PARA LOGIN
  // =============================================================================

  /**
   * Realiza login de personal (gestor/administrador)
   */
  login(email: string, password: string): Observable<any> {
    const url = this.apiConfig.getUsersAuthenticationUrl('privilegedLogin');
    return this.http.post(url, { email, password });
  }

  // =============================================================================
  // MÉTODOS PARA CAMBIO DE CONTRASEÑA
  // =============================================================================

  async enviarCorreoRecuperacion(email: string): Promise<void> {
    try {
      const url = this.apiConfig.getUsersPasswordsUrl('forgotPrivileged');
      if (!url) throw new Error('Endpoint for forgotPasswordPrivileged not configured in ApiConfigService');

      const res = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email })
      });

      if (!res.ok) {
        const errText = await res.text();
        throw new Error(`Error ${res.status}: ${errText}`);
      }

    } catch (error) {
      console.error('Error al enviar correo de recuperación:', error);
      throw error;
    }
  }

  //AÑADIR LLAMADAS DE GET POR ID

  getCreador(userId: string): Observable<any> {
  
    const url = this.apiConfig.getManagementCreatorUrl('getCreator').replace('{creatorId}', userId);
    return this.http.get(url, { headers: this.sharedService.getAuthHeaders() });
  }

  /**
   * Obtiene datos del administrador
   */
  getAdministrador(userId: string): Observable<any> {
    
    const url = this.apiConfig.getManagementAdminUrl('getAdmin').replace('{adminId}', userId);
    return this.http.get(url, { headers: this.sharedService.getAuthHeaders() });
  }

  // =============================================================================
  // MÉTODOS PARA 2FA
  // =============================================================================

  sendTwoFactorEmailAdmin(): Observable<any> {
    const url = this.apiConfig.getUsersAuthentication2FAUrl('sendEmailAdmin');
    return this.http.post(url, {},  { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  sendTwoFactorEmailCreator(): Observable<any> {
    const url = this.apiConfig.getUsersAuthentication2FAUrl('sendEmailCreator');
    return this.http.post(url, {},  { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  verifyEmailCodeCreator(code: string): Observable<any> {
    const url = this.apiConfig.getUsersAuthentication2FAUrl('verifyEmailCreator');
    return this.http.post(url, {code}, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  verifyEmailAdmin(code: string): Observable<any> {
    const url = this.apiConfig.getUsersAuthentication2FAUrl('verifyEmailAdmin');
    return this.http.post(url, {code}, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  generateQRCreator(): Observable<any> {
    const url = this.apiConfig.getUsersAuthentication2FAUrl('generateQRCreator');
    return this.http.post(url, {}, { headers: this.sharedService.getAuthHeaders() });
  }
  
  generateQRAdmin(): Observable<any> {
    const url = this.apiConfig.getUsersAuthentication2FAUrl('generateQRAdmin');
    return this.http.post(url, {}, { headers: this.sharedService.getAuthHeaders() });
  }

  confirm3fAsetupCreator(code: string): Observable<any> {
    const url = this.apiConfig.getUsersAuthentication2FAUrl('confirmTOTPCreator');
    return this.http.post(url, { code }, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  verify3fACodeCreator(code: string): Observable<any> {
    const url = this.apiConfig.getUsersAuthentication2FAUrl('verifyTOTPCreator');
    return this.http.post(url, { code }, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  confirm3fAsetupAdmin(code: string): Observable<any> {
    const url = this.apiConfig.getUsersAuthentication2FAUrl('confirmTOTPAdmin');
    return this.http.post(url, { code }, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  } 
  
  verify3fACodeAdmin(code: string): Observable<any> {
    const url = this.apiConfig.getUsersAuthentication2FAUrl('verifyTOTPAdmin');
    return this.http.post(url, { code }, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }
}