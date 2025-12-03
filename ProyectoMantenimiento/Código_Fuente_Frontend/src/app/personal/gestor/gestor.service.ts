import { Injectable } from '@angular/core';
import { HttpClient, HttpEvent, HttpRequest,} from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiConfigService } from '../../shared/services/api-config.service';
import { SharedService } from '@shared';

@Injectable({
  providedIn: 'root'
})
export class GestorService {

  constructor(
    private readonly http: HttpClient,
    private readonly apiConfig: ApiConfigService,
    private readonly sharedService: SharedService
  ) {}

  /**
   * Registro de gestor (creador de contenido)
   */
  registerGestor(gestorData: any): Observable<any> {
    const url = this.apiConfig.getUsersRegisterUrl('creator');
    return this.http.post(url, gestorData, { responseType: 'text' });
  }

  changePassword(creatorId: string, passwordForm: any): Observable<any> {
    const url = this.apiConfig.getManagementCreatorUrl('changePassword').replace('{creatorId}', creatorId);
    const body = {
      oldPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword
    };
    return this.http.put(url, body, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  /**
   * Actualización de perfil de gestor
   * Nota: Este endpoint podría requerir implementación específica en el backend
   */
  updateProfile(creatorId: string, profileData: any): Observable<any> {
    const url = this.apiConfig.getManagementCreatorUrl('updateProfile').replace('{creatorId}', creatorId);
    return this.http.put(url, profileData, { headers: this.sharedService.getAuthHeaders(), responseType: 'text'  });
  }

  /**
   * Eliminación de cuenta del gestor (self delete)
   */
  deleteAccount(): Observable<any> {
    const url = this.apiConfig.getManagementCreatorUrl('selfDelete');
    return this.http.delete(url, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  

  /**
   * Subida de archivo de audio con progreso
   */
  uploadAudio(file: File, metadata?: any): Observable<HttpEvent<any>> {
    const url = this.apiConfig.getContentUrl('uploadAudio');
    const formData = new FormData();
    formData.append('file', file, file.name);
    
    if (metadata) {
      // Agregar metadata como campos separados o como JSON string
      for (const key of Object.keys(metadata)) {
        formData.append(key, metadata[key]);
      }
    }

    const request = new HttpRequest('POST', url, formData, {
      reportProgress: true,
      headers: this.sharedService.getAuthHeaders()
    });

    return this.http.request(request);
  }

  /**
   * Subida de audio como JSON (por ejemplo cuando el fichero ya está en base64)
   * El backend debe aceptar la misma URL que uploadAudio pero con cuerpo JSON.
   */
  uploadAudioJson(audioData: any): Observable<any> {
    const url = this.apiConfig.getContentUrl('uploadAudio');
    return this.http.post(url, audioData, { headers: this.sharedService.getAuthHeaders(), responseType: 'text'  });
  }

  /**
   * Subida de archivo de video con progreso
   */
  uploadVideoFile(file: File, metadata?: any): Observable<HttpEvent<any>> {
    const url = this.apiConfig.getContentUrl('uploadVideo');
    const formData = new FormData();
    formData.append('file', file, file.name);
    
    if (metadata) {
      // Agregar metadata como campos separados o como JSON string
      for (const key of Object.keys(metadata)) {
        formData.append(key, metadata[key]);
      }
    }

    const request = new HttpRequest('POST', url, formData, {
      reportProgress: true,
      headers: this.sharedService.getAuthHeaders()
    });

    return this.http.request(request);
  }

  /**
   * Subida de video con URL (para videos externos)
   */
  uploadVideo(videoData: any): Observable<any> {
    const url = this.apiConfig.getContentUrl('uploadVideo');
    return this.http.post(url, videoData, { headers: this.sharedService.getAuthHeaders() , responseType: 'text' });
  }

}