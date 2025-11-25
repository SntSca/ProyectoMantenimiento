import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

export interface AppConfig {
  apiBaseUrl: string;
  apiVersion?: string;
  requestTimeout: number;
  maxFileSizeMB: number;
  allowedAudioTypes: string[];
  allowedVideoTypes: string[];
  authTokenKey: string;
  refreshTokenKey: string;
}

@Injectable({
  providedIn: 'root'
})
export class ConfigService {
  
  private config: AppConfig;

  constructor() {
    this.config = this.loadConfig();
  }

  /**
   * Carga la configuración desde environment y variables del entorno del navegador
   */
  private loadConfig(): AppConfig {
    // Configuración base desde environment
    const baseConfig: AppConfig = {
      apiBaseUrl: environment.apiBaseUrl,
      apiVersion: 'v1',
      requestTimeout: 30000,
      maxFileSizeMB: 100,
      allowedAudioTypes: ['audio/mpeg', 'audio/wav', 'audio/ogg'],
      allowedVideoTypes: ['video/mp4', 'video/webm', 'video/ogg'],
      authTokenKey: 'authToken',
      refreshTokenKey: 'refreshToken'
    };

    // En producción, se pueden sobrescribir estos valores desde variables del servidor
    // o desde un archivo de configuración cargado dinámicamente
    if (environment.production) {
      // Ejemplo: cargar configuración desde el servidor
      const runtimeConfig = this.getRuntimeConfig();
      return { ...baseConfig, ...runtimeConfig };
    }

    return baseConfig;
  }

  /**
   * Obtiene configuración en tiempo de ejecución (desde servidor o localStorage)
   */
  private getRuntimeConfig(): Partial<AppConfig> {
    try {
      // Opción 1: Desde localStorage (si se almacena configuración ahí)
      const storedConfig = localStorage.getItem('appConfig');
      if (storedConfig) {
        return JSON.parse(storedConfig);
      }

      // Opción 2: Desde variables globales del window (inyectadas por el servidor)
      const windowConfig = (window as any).APP_CONFIG;
      if (windowConfig) {
        return windowConfig;
      }

      return {};
    } catch (error) {
      console.warn('Error loading runtime config:', error);
      return {};
    }
  }

  /**
   * Obtiene la configuración actual
   */
  getConfig(): AppConfig {
    return { ...this.config };
  }

  /**
   * Obtiene la URL base de la API
   */
  getApiBaseUrl(): string {
    return this.config.apiBaseUrl;
  }

  /**
   * Obtiene el timeout para requests
   */
  getRequestTimeout(): number {
    return this.config.requestTimeout;
  }

  /**
   * Obtiene el tamaño máximo permitido para archivos
   */
  getMaxFileSizeMB(): number {
    return this.config.maxFileSizeMB;
  }

  /**
   * Obtiene los tipos de audio permitidos
   */
  getAllowedAudioTypes(): string[] {
    return [...this.config.allowedAudioTypes];
  }

  /**
   * Obtiene los tipos de video permitidos
   */
  getAllowedVideoTypes(): string[] {
    return [...this.config.allowedVideoTypes];
  }

  /**
   * Obtiene la clave del token de autenticación
   */
  getAuthTokenKey(): string {
    return this.config.authTokenKey;
  }

  /**
   * Obtiene la clave del refresh token
   */
  getRefreshTokenKey(): string {
    return this.config.refreshTokenKey;
  }

  /**
   * Valida si un tipo de archivo es permitido
   */
  isFileTypeAllowed(fileType: string, mediaType: 'AUDIO' | 'VIDEO'): boolean {
    const allowedTypes = mediaType === 'AUDIO' 
      ? this.getAllowedAudioTypes() 
      : this.getAllowedVideoTypes();
    return allowedTypes.includes(fileType);
  }

  /**
   * Valida si el tamaño del archivo es permitido
   */
  isFileSizeAllowed(fileSizeBytes: number): boolean {
    const maxSizeBytes = this.getMaxFileSizeMB() * 1024 * 1024;
    return fileSizeBytes <= maxSizeBytes;
  }

  /**
   * Actualiza la configuración (útil para cambios en tiempo de ejecución)
   */
  updateConfig(newConfig: Partial<AppConfig>): void {
    this.config = { ...this.config, ...newConfig };
  }
}