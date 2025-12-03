import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Subject, Observable, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil, finalize, map } from 'rxjs/operators';
import { MOCK_TAGS} from '../constants/mock_data';
import { Especialidades, ESPECIALIDADES_PERMITIDAS, Tags } from '../constants/interfaces';
import { ApiConfigService } from './api-config.service';
import { MessagesService } from './messages.service';
import { jwtDecode } from 'jwt-decode';
import { FormGroup } from '@angular/forms';
import { NotificationService } from './notification.service';
import { ConfirmationService } from './confirmation.service';

export interface SessionInfo {
  loginTime: string;       // ISO string
  expirationTime: string;  // ISO string
}

@Injectable({
  providedIn: 'root'
})



export class SharedService {


  constructor(
    private readonly apiConfig: ApiConfigService,
    private readonly http: HttpClient,
    private readonly messagesService: MessagesService,
    private readonly notificationService: NotificationService,
    private readonly confirmationService: ConfirmationService
  ) { }


    async resetPassword(token: string, newPassword: string): Promise<void> {
      try {
        const url = this.apiConfig.getUsersPasswordsUrl('reset');
        if (!url) throw new Error('Endpoint for resetPassword not configured in ApiConfigService');

        const res = await fetch(url, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ token, newPassword })
        });

        if (!res.ok) {
          const errText = await res.text();
          throw new Error(`Error ${res.status}: ${errText}`);
        }

      } catch (error) {
        this.notificationService.error('Ocurrió un error al intentar restablecer la contraseña.');
        throw error;
      }
    }

    
  getTags(): Observable<Tags[]> {
    
    return of (MOCK_TAGS);
  }

  getEspecialidades(): Observable<Especialidades[]> {
  
    return of(ESPECIALIDADES_PERMITIDAS);
  }
  
  decodeJWT(token: string): any{
    try {
      const decoded: any = jwtDecode(token);
      return decoded ;
    } catch (error) {
      console.error('Error al decodificar el token:', error);
      return null;
    }
  }

   getAuthHeaders(): HttpHeaders | undefined {
    const token = sessionStorage.getItem('token');
    if (token) {
      return new HttpHeaders({
        'Authorization': `Bearer ${token}`
      });
    }
    return undefined;
  }


  logout(): Observable<any> {
    const url = this.apiConfig.getUsersAuthenticationUrl('logout');
    const headers = this.getAuthHeaders();
    
    return this.http.post(url, {}, { headers }).pipe(
      finalize(() => {
        this.clearSession();
      })
    );
  }

  getSessionInfo(): Observable<SessionInfo> {
    return this.http.get<SessionInfo>(`${this.apiConfig}/auth/session/info`);
  }

  clearSession(): void {
    sessionStorage.removeItem('authToken');
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('admin_data');
    sessionStorage.removeItem('user_data');
    sessionStorage.removeItem('gestor_data');
    
  }

  fileToBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => {
        const result = reader.result as string;
        
        const base64 = result.split(',')[1];
        resolve(base64);
      };
      reader.onerror = () => reject(new Error('Error al convertir archivo a base64'));
    });
  }

  base64ToFile(base64String: string, mimeType?: string): Promise<File> {
    return new Promise((resolve, reject) => {
      try {
        const base64Data = base64String.includes(',') ? base64String.split(',')[1] : base64String;
        
        const byteCharacters = atob(base64Data);
        const byteNumbers = new Array(byteCharacters.length);
        for (let i = 0; i < byteCharacters.length; i++) {
          byteNumbers[i] = byteCharacters.codePointAt(i) ?? 0;
        }
        const byteArray = new Uint8Array(byteNumbers);
        
        const finalMimeType = mimeType ?? (base64String.includes('data:') ? base64String.split(';')[0].split(':')[1] : 'application/octet-stream');
        
        const file = new File([byteArray], 'file', { type: finalMimeType });
        resolve(file);
      } catch (error) {
        reject(new Error(`Error converting base64 to file: ${error}`));
      }
    });
  }
  
  secondsToMinutesSeconds(seconds: number): string {
    if (!Number.isFinite(seconds) || seconds <= 0) return '00:00';
    const total = Math.floor(seconds);
    const minutes = Math.floor(total / 60);
    const secs = total % 60;
    const mm = String(minutes).padStart(2, '0');
    const ss = String(secs).padStart(2, '0');
    return `${mm}:${ss}`;
  }


  setupDynamicSearch(searchSubject: Subject<string>, callback: (searchTerm: string) => void, destroy$: Subject<void>): void {
    searchSubject
      .pipe(
        debounceTime(1000), 
        distinctUntilChanged(),
        takeUntil(destroy$) 
      )
      .subscribe(searchTerm => {
        callback(searchTerm);
      });
  }

  togglePasswordVisibility(showPassword: boolean): boolean {
    return !showPassword;
  }

  async performLogout(router: any, homeRoute: string): Promise<void> {
    const confirmed = await this.confirmationService.confirmLogout();
    if (!confirmed) {
      return;
    }

    this.logout().subscribe({
      next: (response) => {
        
        router.navigate([homeRoute]);
      },
      error: (error) => {
        router.navigate([homeRoute]);
      }
    });
  }


  isFieldInvalid(form: any, fieldName: string, submitted?: boolean): boolean {
    const field = form?.get(fieldName);
    return field?.invalid && (field.touched || field.dirty || submitted);
  }


  getFieldError(form: any, fieldName: string): string {
    const field = form?.get(fieldName);

    if (field?.errors) {
      return this.messagesService.getAngularFormErrorMessage(fieldName, field.errors);
    }

    return '';
  }
  // Método para manejar la subida de fotos
  async handlePhotoUpload(event: Event, maxSizeMB: number = 5): Promise<{ status: string; message?: string; file?: File; previewUrl?: string }> {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) {
      return { status: 'ERROR_NO_FILE', message: 'No se seleccionó ningún archivo.' };
    }

    const file = input.files[0];
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
      return { status: 'ERROR_INVALID_TYPE', message: 'Tipo de archivo no permitido. Solo se permiten imágenes JPEG, PNG, GIF y WebP.' };
    }

    const maxSizeBytes = maxSizeMB * 1024 * 1024;
    if (file.size > maxSizeBytes) {
      return { status: 'ERROR_TOO_LARGE', message: `El archivo es demasiado grande. El tamaño máximo permitido es ${maxSizeMB} MB.` };
    }

    const previewUrl = URL.createObjectURL(file);
    return { status: 'SUCCESS', file, previewUrl };
  }

  // Método para obtener los requisitos de contraseña
  getPasswordRequirements(): readonly string[] {
    return this.messagesService.getPasswordRequirements();
  }

  // Método para alternar la vista entre lista y grid
  toggleView(isListView: boolean): boolean {
    return !isListView;
  }

  // Método para manejar entrada de búsqueda
  handleSearchInput(searchSubject: Subject<string>, searchTerm: string): void {
    searchSubject.next(searchTerm);
  }

  // Método para calcular la edad a partir de fecha de nacimiento
  calculateAge(fechaNacimiento: string): number {
    const hoy = new Date();
    const nacimiento = new Date(fechaNacimiento);
    let edad = hoy.getFullYear() - nacimiento.getFullYear();
    const mes = hoy.getMonth() - nacimiento.getMonth();
    if (mes < 0 || (mes === 0 && hoy.getDate() < nacimiento.getDate())) {
      edad--;
    }
    return edad;
  }

  // Método para prevenir entrada de teclado manual en campos como fecha
  preventKeyboardInput(event: KeyboardEvent): void {
    // Permitir teclas de navegación y control
    const allowedKeys = ['Tab', 'Enter', 'Escape', 'ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight', 'Backspace', 'Delete'];
    if (!allowedKeys.includes(event.key)) {
      event.preventDefault();
    }
  }

  // Método para cargar contenido de manera centralizada
  loadContent(contentService: any, destroy$?: Subject<void>): Observable<{ contenidos: any[], contenidosFiltrados: any[] }> {
    let observable = contentService.getAllContent();

    if (destroy$) {
      observable = observable.pipe(takeUntil(destroy$));
    }
    

    return observable.pipe(
      map((data: any[]) => {
        // Formatear duración para todos los contenidos
        const contenidos = data.map(contenido => ({
          ...contenido,
        }));

        const contenidosFiltrados = [...contenidos];

        return { contenidos, contenidosFiltrados };
      })
    );
  }

  // =============================================================================
  // MÉTODOS PARA MANEJAR TAGS EN FORMULARIOS
  // =============================================================================

  toggleTagsDropdown(isOpen: boolean): boolean {
    return !isOpen;
  }

  closeTagsDropdown(): void {
    // Este método se usa con HostListener en los componentes
  }

  onTagChange(tag: Tags, checked: boolean, form: FormGroup): void {
    const currentTags = form.get('tags')?.value || [];
    if (checked) {
      if (!currentTags.includes(tag.nombre)) {
        form.get('tags')?.setValue([...currentTags, tag.nombre]);
      }
    } else {
      form.get('tags')?.setValue(currentTags.filter((t: string) => t !== tag.nombre));
    }
  }

  isTagSelected(tag: Tags, form: FormGroup): boolean {
    const currentTags = form.get('tags')?.value || [];
    return currentTags.includes(tag.nombre);
  }

  getSelectedTagsText(form: FormGroup): string {
    const selectedTags = form.get('tags')?.value || [];
    if (selectedTags.length === 0) {
      return 'Seleccionar tags';
    } else if (selectedTags.length === 1) {
      return selectedTags[0];
    } else {
      return `${selectedTags.length} tags seleccionados`;
    }
  }

  getStarsArray(rating: number): number[] {
    const stars: number[] = [];
    // Redondear hacia abajo para obtener medias estrellas (ej. 3.6 -> 3.5)
    const flooredRating = Math.floor(rating * 2) / 2;

    for (let i = 1; i <= 5; i++) {
      if (flooredRating >= i) {
        stars.push(1); // Estrella completa
      } else if (flooredRating >= i - 0.5) {
        stars.push(0.5); // Media estrella
      } else {
        stars.push(0); // Estrella vacía
      }
    }
    return stars;
  }

  /**
   * Obtiene las credenciales del usuario desde el token JWT almacenado en sessionStorage
   * @returns Objeto con userId y rol, o null si no se puede obtener
   */
  obtainCredentials(): { userId: string; rol: string } {
    const token = sessionStorage.getItem('token');
    
    if (!token) {
      console.error('❌ Token no encontrado en sessionStorage');
      return { userId: '', rol: '' };
    }

    const decoded = this.decodeJWT(token);
    
    if (!decoded) {
      console.error('❌ Error al decodificar el JWT');
      return { userId: '', rol: '' };
    }
    
    return { userId: decoded.sub, rol: decoded.rol };
  }

}