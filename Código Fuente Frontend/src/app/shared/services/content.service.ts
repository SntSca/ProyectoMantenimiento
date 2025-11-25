import { Injectable } from '@angular/core';
import { HttpClient} from '@angular/common/http';
import { Observable, of, catchError, map } from 'rxjs';
import { ApiConfigService } from './api-config.service';
import { Videos, Audios, Contenido, ListaPublica, ListaPrivada} from '../constants/interfaces';
import { MOCK_VIDEOS, MOCK_AUDIOS } from '../constants/mock_data';
import { SharedService } from './shared.services';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

type ApiContentData = {
  id: string | null;
  titulo: string;
  descripcion: string;
  tags: string[];
  duracion: number;
  esVIP: boolean;
  fechaSubida: string | null;
  fechaExpiracion: string | null;
  restriccionEdad: number;
  miniatura: string;
  formatoMiniatura: string;
  visibilidad: boolean;
  valoracionMedia: number;
  especialidad: string;
  tipo: 'VIDEO' | 'AUDIO';
  visualizaciones: number;
  valoracionUsuario?: number;
  resolucion?: number | string;
  urlArchivo?: string;
  fichero?: string;
  ficheroExtension?: string;
};

@Injectable({
  providedIn: 'root'
})
export class ContentService {

  constructor(
    private readonly http: HttpClient,
    private readonly apiConfig: ApiConfigService,
    private readonly sharedService: SharedService,
    private readonly sanitizer: DomSanitizer
  ) {}

  /**
   * Convierte una miniatura base64 en una Data URL segura
   */
  private createMiniaturaDataUrl(base64: string, formato: string): string {
    // Si la miniatura ya es una Data URL completa, retornarla tal cual
    if (base64?.startsWith('data:')) {
      return base64;
    }
    
    // Si la miniatura está vacía, retornar una imagen por defecto o vacío
    if (!base64) {
      return '';
    }
    
    // Construir la Data URL con el formato correcto
    const mimeType = formato?.toLowerCase() || 'image/jpeg';
    const fullMimeType = mimeType.startsWith('image/') ? mimeType : `image/${mimeType}`;
    
    return `data:${fullMimeType};base64,${base64}`;
  }

  /**
   * Mapea datos del API a entidades del frontend
   * Convierte fechas de string a Date y asegura valores por defecto
   */
  private mapApiDataToEntities(apiData: ApiContentData[]): Contenido[] {
    return apiData.map((item) => {
      // Detectar tipo por presencia de campos específicos
      const isVideo = 'urlArchivo' in item && 'resolucion' in item;
      
      // Base común para ambos tipos
      const base = {
        id: item.id,
        titulo: item.titulo,
        descripcion: item.descripcion,
        tags: Array.isArray(item.tags) ? item.tags : [item.tags],
        duracion: item.duracion,
        duracionFormateada: this.sharedService.secondsToMinutesSeconds(item.duracion),
        esVIP: item.esVIP,
        fechaSubida: item.fechaSubida ? new Date(item.fechaSubida) : null,
        fechaExpiracion: item.fechaExpiracion ? new Date(item.fechaExpiracion) : null,
        restriccionEdad: item.restriccionEdad ?? 3,
        miniatura: item.miniatura, // Usar la Data URL procesada
        formatoMiniatura: item.formatoMiniatura,
        visibilidad: item.visibilidad,
        valoracionMedia: item.valoracionMedia ?? 0,
        especialidad: item.especialidad,
        visualizaciones: item.visualizaciones ?? 0,
        valoracionUsuario: item.valoracionUsuario ?? 0,
        
      };
      
      if (isVideo && item.urlArchivo && item.resolucion !== undefined) {
        return {
          ...base,
          tipo: 'VIDEO' as const,
          urlArchivo: item.urlArchivo,
          resolucion: typeof item.resolucion === 'string' ? item.resolucion : String(item.resolucion),
        } as Videos;
      } else {
        return {
          ...base,
          tipo: 'AUDIO' as const,
          fichero: item.fichero!,
          ficheroExtension: item.ficheroExtension!,
        } as Audios;
      }
    });
  }


  /**
   * Obtener audio por ID
   */
  getAudio(id: string | number): Observable<any> {
    const url = this.apiConfig.getAudioUrl(id);
    return this.http.get(url, { headers: this.sharedService.getAuthHeaders() });
  }

  /**
   * Obtener video por ID
   */
  getVideo(id: string | number): Observable<any> {
    const url = this.apiConfig.getVideoUrl(id);
    return this.http.get(url, { headers: this.sharedService.getAuthHeaders() });
  }

  /**
   * Obtener el contenido (videos + audios)
   */
  getAllContent(): Observable<Contenido[]> {
    const url = this.apiConfig.getContentUrl('getAllContent');
    return this.http.get<ApiContentData[]>(url, { headers: this.sharedService.getAuthHeaders() }).pipe(
      map(apiData => this.mapApiDataToEntities(apiData)),
      catchError((error) => {
        console.warn('Error al obtener contenido desde API, usando datos mock:', error);
        // Combinar mocks como fallback
        return of([...MOCK_VIDEOS, ...MOCK_AUDIOS]);
      })
    );
  }


  /**
   * Obtener los audios
   */
  getAllAudios(): Observable<Audios[]> {
    const url = this.apiConfig.getContentUrl('getAllAudios');
    return this.http.get<ApiContentData[]>(url, { headers: this.sharedService.getAuthHeaders() }).pipe(
      map(apiData => this.mapApiDataToEntities(apiData) as Audios[]),
      catchError((error) => {
        console.warn('Error al obtener audios desde API, usando datos mock:', error);
        return of(MOCK_AUDIOS);
      })
    );
  }

  /**
   * Obtener los videos
   */
  getAllVideos(): Observable<Videos[]> {
    const url = this.apiConfig.getContentUrl('getAllVideos');
    return this.http.get<ApiContentData[]>(url, { headers: this.sharedService.getAuthHeaders() }).pipe(
      map(apiData => this.mapApiDataToEntities(apiData) as Videos[]),
      catchError((error) => {
        console.warn('Error al obtener videos desde API, usando datos mock:', error);
        return of(MOCK_VIDEOS);
      })
    );
  }


  /**
   * Obtener contenido específico por ID
   * @param id ID del contenido a obtener
   * @param type Tipo de contenido ('AUDIO' | 'VIDEO')
   */
  getContentById(id: string, type: 'AUDIO' | 'VIDEO'): Observable<Contenido | null> {
    const endpoint = type === 'AUDIO' ? 'getAudio' : 'getVideo';
    const url = `${this.apiConfig.getContentUrl(endpoint)}/${id}`;

    return this.http.get<ApiContentData>(url, { headers: this.sharedService.getAuthHeaders() }).pipe(
      map(apiData => {
        // Agregar el ID si no viene en la respuesta
        if (!apiData.id) {
          apiData.id = id;
        }
        
        // Usar el mapeo unificado
        const mapped = this.mapApiDataToEntities([apiData]);
        return mapped.length > 0 ? mapped[0] : null;
      }),
      catchError((error) => {
        console.error(`Error al obtener ${type} con ID ${id}:`, error);
        
        // Fallback: buscar en los datos mock
        const mockData = type === 'VIDEO' ? MOCK_VIDEOS : MOCK_AUDIOS;
        const found = mockData.find(item => item.id === id);
        
        if (found) {
          console.warn(`Usando datos mock para ${type} con ID ${id}`);
          return of(found);
        }
        
        return of(null);
      })
    );
  }

  /**
   * Obtener audio específico por ID
   * @param id ID del audio a obtener
   */
  getAudioById(id: string): Observable<Audios | null> {
    return this.getContentById(id, 'AUDIO') as Observable<Audios | null>;
  }

  /**
   * Obtener video específico por ID
   * @param id ID del video a obtener
   */
  getVideoById(id: string): Observable<Videos | null> {
    return this.getContentById(id, 'VIDEO') as Observable<Videos | null>;
  }

  updateVideo(updatedContent: Contenido, videoId: string): Observable<any> {
    const url = this.apiConfig.getContentUrl('updateVideo').replace('{id}', videoId);
    return this.http.put(url, updatedContent, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }
  updateAudio(updatedContent: Contenido, audioId: string): Observable<any> {
    const url = this.apiConfig.getContentUrl('updateAudio').replace('{id}', audioId);
    return this.http.put(url, updatedContent, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  deleteAudio(audioId: string): Observable<any> {
    const url = this.apiConfig.getContentUrl('deleteAudio').replace('{id}', audioId);
    return this.http.delete(url, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  deleteVideo(videoId: string): Observable<any> {
    const url = this.apiConfig.getContentUrl('deleteVideo').replace('{id}', videoId);
    return this.http.delete(url, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  rateContent(contentId: string, rating: number): Observable<any> {
    const url = this.apiConfig.getContentUrl('rateContent').replace('{id}', contentId);
    return this.http.post(url, rating, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  viewedVideo(contentId: string): Observable<any> {
    const url = this.apiConfig.getContentUrl('viewedVideo').replace('{id}', contentId);
    return this.http.post(url, {}, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  viewedAudio(contentId: string): Observable<any> {
    const url = this.apiConfig.getContentUrl('viewedAudio').replace('{id}', contentId);
    return this.http.post(url, {}, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  /**
   * Buscar contenido en la lista por ID 
   * Útil para casos donde ya tienes la lista cargada
   */
  findContentInList(contentList: Contenido[], id: string): Contenido | undefined {
    return contentList.find(content => content.id === id);
  }

  createPublicList(publicList: ListaPublica): Observable<any> {
    const url = this.apiConfig.getPublicListsContentUrl('create');
    return this.http.post(url, publicList, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  createPrivateList(privateList: ListaPrivada): Observable<any> {
    const url = this.apiConfig.getPrivateListsContentUrl('create');
    return this.http.post(url, privateList, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  getPublicLists(): Observable<ListaPublica[]> {
    const url = this.apiConfig.getPublicListsContentUrl('get');
    return this.http.get<ListaPublica[]>(url, { headers: this.sharedService.getAuthHeaders() }).pipe(
      map(listas => {
        
        return listas.map(lista => {
          const listaMapeada = {
            ...lista,
            contenidos: lista.contenidos ? this.mapApiDataToEntities(lista.contenidos) : []
          };
          
          return listaMapeada;
        });
      }),
      catchError((error) => {
        console.error('Error al obtener listas públicas:', error);
        return of([]);
      })
    );
  }

  getPrivateLists(): Observable<ListaPrivada[]> {
    const url = this.apiConfig.getPrivateListsContentUrl('get');
    return this.http.get<ListaPrivada[]>(url, { headers: this.sharedService.getAuthHeaders() }).pipe(
      map(listas => {
        
        return listas.map(lista => {
          const listaMapeada = {
            ...lista,
            contenidos: lista.contenidos ? this.mapApiDataToEntities(lista.contenidos) : []
          };
          
          return listaMapeada;
        });
      }),
      catchError((error) => {
        console.error('Error al obtener listas privadas:', error);
        return of([]);
      })
    );
  }

  editPublicList(lista: ListaPublica): Observable<any> {
    const url = this.apiConfig.getPublicListsContentUrl('edit');
    const body = {
      idLista: lista.idLista,
      nombre: lista.nombre,
      descripcion: lista.descripcion,
      visibilidad: lista.visibilidad
    };
    return this.http.put(url, body, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  editPrivateList(lista: ListaPrivada): Observable<any> {
    const url = this.apiConfig.getPrivateListsContentUrl('edit');
    const body = {
      idLista: lista.idLista,
      nombre: lista.nombre,
      descripcion: lista.descripcion
    };
    return this.http.put(url, body, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  
  addContentToPrivateList(data: { idLista: string, idsContenido: string[], idUsuario: string }): Observable<any> {
    const url = this.apiConfig.getPrivateListsContentUrl('addContent');
    return this.http.put(url, data, { headers: this.sharedService.getAuthHeaders() , responseType: 'text' });
  }

  removeContentFromPrivateList(data: { idLista: string, idsContenido: string[], idUsuario: string }): Observable<any> {
    const url = this.apiConfig.getPrivateListsContentUrl('removeContent');
    return this.http.delete(url, { body: data, headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  addContentToPublicList(data: { idLista: string, idsContenido: string[] }): Observable<any> {
    const url = this.apiConfig.getPublicListsContentUrl('addContent');
    return this.http.put(url, data, { headers: this.sharedService.getAuthHeaders() , responseType: 'text' });
  }

  removeContentFromPublicList(data: { idLista: string, idsContenido: string[] }): Observable<any> {
    const url = this.apiConfig.getPublicListsContentUrl('removeContent');
    return this.http.delete(url, { body: data, headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  deletePublicList(idLista: string): Observable<any> {
    const url = this.apiConfig.getPublicListsContentUrl('deleteList').replace('{idLista}', idLista);
    return this.http.delete(url, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }
  
  deletePrivateList(idLista: string): Observable<any> {
    const url = this.apiConfig.getPrivateListsContentUrl('deleteList').replace('{idLista}', idLista);
    return this.http.delete(url, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  addFavoriteContent(idContenido: string): Observable<any> {
    const url = this.apiConfig.getFavoritesContentUrl('add');
    return this.http.post(url, { idContenido }, { headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }

  removeFavoriteContent(idContenido: string): Observable<any> {
    const url = this.apiConfig.getFavoritesContentUrl('remove');
    return this.http.delete(url, { body: { idContenido }, headers: this.sharedService.getAuthHeaders(), responseType: 'text' });
  }
  getFavoriteContents(): Observable<string[]> {
    const url = this.apiConfig.getFavoritesContentUrl('getAll');
    return this.http.get<string[]>(url, { headers: this.sharedService.getAuthHeaders() });
  }
      


  getSafeEmbedUrl(url: string): SafeResourceUrl {
    const videoInfo = this.extractVideoInfo(url);
    let embedUrl = '';
    if (videoInfo) {
      if (videoInfo.provider === 'youtube') {
        // YouTube video IDs are 11 characters, alphanumeric + _ -
        if (/^[A-Za-z0-9_-]{11}$/.test(videoInfo.id)) {
          embedUrl = `https://www.youtube.com/embed/${videoInfo.id}`;
        }
      } else if (videoInfo.provider === 'vimeo') {
        // Vimeo IDs are numeric
        if (/^\d+$/.test(videoInfo.id)) {
          embedUrl = `https://player.vimeo.com/video/${videoInfo.id}`;
        }
      }
    }
    // Only bypass sanitization if we have a valid, constructed embed URL
    return embedUrl ? this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl) : '';
  }

  extractVideoInfo(url: string): { provider: 'youtube' | 'vimeo', id: string } | null {
    if (url.includes('youtube.com') || url.includes('youtu.be')) {
      const regExp = /^.*(youtu.be\/|v\/|u\/\w\/|embed\/|watch\?v=|&v=)([^#&?]*).*/;
      const match = regExp.exec(url);
      if (match?.[2]?.length === 11) {
        return { provider: 'youtube', id: match[2] };
      }
    } else if (url.includes('vimeo.com')) {
      const regExp = /vimeo\.com\/(\d+)/;
      const match = regExp.exec(url);
      if (match?.[1]) {
        return { provider: 'vimeo', id: match[1] };
      }
    }
    return null;
  }
}