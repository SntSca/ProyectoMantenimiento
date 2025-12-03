import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { SharedService, GetAllUsersResponse } from '@shared';
import { HttpClient } from '@angular/common/http';
import { ApiConfigService } from '../../shared/services/api-config.service';

@Injectable({
  providedIn: 'root'
})
export class AdministradorService {

  constructor(private readonly http: HttpClient, private readonly apiConfig: ApiConfigService
    , private readonly sharedService: SharedService
  ) { 
  
  }

  // =============================================================================
  // MÉTODOS PARA INTERACCIÓN CON BBnDD FUTURA
  // =============================================================================


  changePassword(adminId: string, passwordForm: any): Observable<any> {
    const url = this.apiConfig.getManagementAdminUrl('changePassword').replace('{adminId}', adminId);
    const body = {
      oldPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword
    };
    return this.http.put(url, body, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }
  /**
   * Actualiza el estado de bloqueo de un usuario, gestor o administrador
   */
  toggleBloqueo(elemento: any): Observable<any> {
    let url: string;
    switch (elemento.tipo) {
      case 'usuario':
        url = this.apiConfig.getManagementAdminUrl('toggleUserBlock').replace('{userId}', elemento.idUsuario.toString());
        break;
      case 'gestor':
        url = this.apiConfig.getManagementAdminUrl('toggleCreatorBlock').replace('{creatorId}', elemento.idUsuario.toString());
        break;
      default:
        return of(null); // No bloquear administradores
    }

    const body = {
      blocked: !elemento.bloqueado // Invertir el estado actual
    };

    return this.http.put(url, body, { headers: this.sharedService.getAuthHeaders() });
  }

  /**
   * Actualiza el estado de validación de un gestor
   */
  toggleValidacionGestor(gestorID : string): Observable<any> {
    const url = this.apiConfig.getManagementAdminUrl('validateCreator').replace('{creatorId}', gestorID);
    return this.http.put(url, {}, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }


  eliminarElemento(elemento: any): Observable<void> {
    let url: string;
    switch (elemento.tipo) {
      case 'gestor':
        url = this.apiConfig.getManagementAdminUrl('deleteCreator').replace('{creatorId}', elemento.idUsuario.toString());
        break;
      case 'administrador':
        url = this.apiConfig.getManagementAdminUrl('deleteAdmin').replace('{adminId}', elemento.idUsuario.toString());
        break;
      default:
        throw new Error('Tipo de elemento no soportado para eliminación');
    }
    return this.http.delete<void>(url, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' as 'json' });
  }
  /**
   * Guarda los cambios realizados en la edición de un usuario, gestor o administrador
   */
  guardarEdicion(userId: string, rol:string, elemento: any): Observable<any> {
    let url: string;

    switch (rol) {
      case 'usuario':
        url = this.apiConfig.getManagementAdminUrl('updateUser').replace('{userId}', userId);
        return this.http.put(url, elemento, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
      case 'gestor':
        url = this.apiConfig.getManagementAdminUrl('updateCreator').replace('{creatorId}', userId);
        return this.http.put(url, elemento, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
      case 'administrador':
        url = this.apiConfig.getManagementAdminUrl('updateAdmin').replace('{adminId}', userId);
        return this.http.put(url, elemento, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
      default:
        break;
    }
    return of(elemento); // Simular respuesta
  }


  /**
   * Crea un nuevo administrador
   */
  crearAdministrador(admin: any): Observable<any> {
    const url = this.apiConfig.getUsersRegisterUrl('admin');
    return this.http.post(url, admin, { headers: this.sharedService.getAuthHeaders() , responseType: 'text' });
  }

  /**
   * Obtiene todos los usuarios de todos los tipos en una sola llamada al endpoint centralizado
   */
  getAllUsers(): Observable<GetAllUsersResponse> {
    return this.http.get<GetAllUsersResponse>(
      this.apiConfig.getUsersManageUrl('getAllUsers'), 
      { headers: this.sharedService.getAuthHeaders() }
    );
  }
  

}