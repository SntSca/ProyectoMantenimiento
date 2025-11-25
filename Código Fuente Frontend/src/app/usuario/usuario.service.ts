import { Injectable } from '@angular/core';
import { ApiConfigService } from '../shared/services/api-config.service';
import { SharedService} from '@shared';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class UsuarioService {

  constructor(private readonly http: HttpClient,
    private readonly apiConfig: ApiConfigService,
    private readonly sharedService: SharedService) {
  
  }

  login(email:string, password:string): Observable<any> {
    const url = this.apiConfig.getUsersAuthenticationUrl('login');
    return this.http.post(url, { email, password });
  }

  registerUsuario(userData: any): Observable<any> {
    const url = this.apiConfig.getUsersRegisterUrl('standard');
    return this.http.post(url, userData, { responseType: 'text' });
  }

  sendTwoFactorEmail(): Observable<any> {
    const url = this.apiConfig.getUsersAuthentication2FAUrl('sendEmail');
    return this.http.post(url,{ }, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  verifyEmailCode(code: string): Observable<any> {
    const url = this.apiConfig.getUsersAuthentication2FAUrl('verifyEmail');
    return this.http.post(url, { code }, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  generateQRCode(): Observable<any> {
    const url = this.apiConfig.getUsersAuthentication2FAUrl('generateQR');
    return this.http.post(url, {}, { headers: this.sharedService.getAuthHeaders() });
  }

  confirm3fAsetup(code: string): Observable<any> {
    const url = this.apiConfig.getUsersAuthentication2FAUrl('confirmTOTP');
    return this.http.post(url, { code }, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  verify3fACode(code: string): Observable<any> {
    const url = this.apiConfig.getUsersAuthentication2FAUrl('verifyTOTP');
    return this.http.post(url, { code }, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  disable2FA(): Observable<any> {
    const url = this.apiConfig.getUsersAuthentication2FAUrl('disable2FA');
    return this.http.post(url, {}, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  disable3FA(): Observable<any> {
    const url = this.apiConfig.getUsersAuthentication2FAUrl('disable3FA');
    return this.http.post(url, {}, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  getUsuario(userId: string): Observable<any> {
    const url = this.apiConfig.getManagementUserUrl('getUser').replace('{userId}', userId);
    return this.http.get(url, { headers: this.sharedService.getAuthHeaders() });
  }

  updateProfile(userId: string, profileData: any): Observable<any> {
    const url = this.apiConfig.getManagementUserUrl('updateProfile').replace('{userId}', userId);
    return this.http.put(url, profileData, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  deleteAccount(userId: string): Observable<any> {
    const url = this.apiConfig.getManagementUserUrl('selfDelete').replace('{userId}', userId);
    return this.http.delete(url, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  changePassword(userId: string, passwordForm: any): Observable<any> {
    const url = this.apiConfig.getManagementUserUrl('changePassword').replace('{userId}', userId);
    const body = {
      oldPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword
    };
    return this.http.put(url, body, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  toggleVipStatus(flagVIP: boolean): Observable<any> {
    const url = this.apiConfig.getManagementUserUrl('toggleVipStatus')
    return this.http.put(url, { flagVIP }, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  async enviarCorreoRecuperacion(email: string): Promise<void> {
    try {
      const url = this.apiConfig.getUsersPasswordsUrl('forgot');
      if (!url) throw new Error('Endpoint for forgotPassword not configured in ApiConfigService');

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
}