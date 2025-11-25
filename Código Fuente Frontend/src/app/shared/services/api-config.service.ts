import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { ConfigService } from './config.service';

@Injectable({
  providedIn: 'root'
})
export class ApiConfigService {
  
  private readonly endpoints = environment.endpoints;

  constructor(private configService: ConfigService) {}

  /**
   * Construye URL completa para endpoint de usuarios
   */
  getUsersAuthenticationUrl(endpoint: keyof typeof environment.endpoints.users.auth): string {
    const authEndpoint = this.endpoints.users.auth[endpoint];
    // Verificar que es un string y no un objeto anidado
    if (typeof authEndpoint !== 'string') {
      throw new Error(`Invalid auth endpoint: ${String(endpoint)} is not a string endpoint`);
    }
    return `${this.getBaseUrl()}${authEndpoint}`;
  }
  getUsersAuthentication2FAUrl(endpoint: keyof typeof environment.endpoints.users.auth.auth2fa): string {
    return `${this.getBaseUrl()}${this.endpoints.users.auth.auth2fa[endpoint]}`;
  }
  getUsersPasswordsUrl(endpoint: keyof typeof environment.endpoints.users.password): string {
    return `${this.getBaseUrl()}${this.endpoints.users.password[endpoint]}`;
  }
  getUsersRegisterUrl(endpoint: keyof typeof environment.endpoints.users.register): string {
    return `${this.getBaseUrl()}${this.endpoints.users.register[endpoint]}`;
  }
  getUsersManageUrl(endpoint: keyof typeof environment.endpoints.users.manage): string {  
    return `${this.getBaseUrl()}${this.endpoints.users.manage[endpoint]}`;
  }

  /**
   * Construye URL completa para endpoint de gestión
   */
  getManagementUserUrl(endpoint: keyof typeof environment.endpoints.management.user): string {
    return `${this.getBaseUrl()}${this.endpoints.management.user[endpoint]}`;
  }
  getManagementCreatorUrl(endpoint: keyof typeof environment.endpoints.management.creator): string {
    return `${this.getBaseUrl()}${this.endpoints.management.creator[endpoint]}`;
  }
  getManagementAdminUrl(endpoint: keyof typeof environment.endpoints.management.admin): string {
    return `${this.getBaseUrl()}${this.endpoints.management.admin[endpoint]}`;
  }

  /**
   * Construye URL completa para endpoint de contenido
   */
  getContentUrl(endpoint: keyof typeof environment.endpoints.content): string {
    const contentEndpoint = this.endpoints.content[endpoint];
    // Verificar que es un string y no un objeto anidado
    if (typeof contentEndpoint !== 'string') {
      throw new Error(`Invalid content endpoint: ${String(endpoint)} is not a string endpoint`);
    }
    return `${this.getBaseUrl()}${contentEndpoint}`;
  }

  getPublicListsContentUrl(endpoint: keyof typeof environment.endpoints.content.publicLists): string {
    return `${this.getBaseUrl()}${this.endpoints.content.publicLists[endpoint]}`;
  }

  getPrivateListsContentUrl(endpoint: keyof typeof environment.endpoints.content.privateLists): string {
    return `${this.getBaseUrl()}${this.endpoints.content.privateLists[endpoint]}`;
  }

  getFavoritesContentUrl(endpoint: keyof typeof environment.endpoints.content.favoritos): string {
    return `${this.getBaseUrl()}${this.endpoints.content.favoritos[endpoint]}`;
  }
  /**
   * Construye URL completa para endpoint de tokens
   */
  getTokensUrl(endpoint: keyof typeof environment.endpoints.tokens): string {
    return `${this.getBaseUrl()}${this.endpoints.tokens[endpoint]}`;
  }

  /**
   * Construye URL con parámetros dinámicos
   * @param baseUrl URL base
   * @param params Objeto con parámetros a reemplazar
   * @example 
   * buildUrlWithParams('/users/confirm/{tokenId}', { tokenId: '12345' })
   * // Retorna: '/users/confirm/12345'
   */
  buildUrlWithParams(baseUrl: string, params: Record<string, string | number>): string {
    let url = `${this.getBaseUrl()}${baseUrl}`;
    
    Object.entries(params).forEach(([key, value]) => {
      url = url.replace(`{${key}}`, String(value));
    });
    
    return url;
  }

  /**
   * URLs específicas con parámetros más usadas
   */


  getValidateCreatorUrl(creatorId: string | number): string {
    return this.buildUrlWithParams(this.endpoints.users.manage.validateCreator, { creatorId });
  }

  getValidateResetTokenUrl(token: string): string {
    return this.buildUrlWithParams(this.endpoints.users.password.validateResetToken, { token });
  }

  getAudioUrl(id: string | number): string {
    return this.buildUrlWithParams(this.endpoints.content.getAudio, { id });
  }

  getVideoUrl(id: string | number): string {
    return this.buildUrlWithParams(this.endpoints.content.getVideo, { id });
  }

  /**
   * Obtiene la URL base de la API
   */
  getBaseUrl(): string {
    return this.configService.getApiBaseUrl();
  }
}