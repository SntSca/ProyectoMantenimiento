import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormGroup, FormsModule, ReactiveFormsModule, FormControl } from '@angular/forms';
import {
  FormValidationService,
  APP_ROUTES,
  GESTOR_ROUTES,
  IMAGENES_PERMITIDAS,
  FICHEROS_PERMITIDOS,
  PEGI_PERMITIDO,
  RESOLUCIONES_PERMITIDAS,
  MOCK_GESTORES,
  GestorStatus,
  SharedService,
  ContentService,
  Tags
} from '@shared';
import { GestorService } from '../../gestor.service';
import { PersonalService } from '../../../personal.service';
import { SafeResourceUrl } from '@angular/platform-browser';
import { NotificationService } from '../../../../shared/services/notification.service';

@Component({
  selector: 'app-subir-contenido',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './subir-contenido.component.html',
})
export class SubirContenidoComponent implements OnInit {

  actualGestor: GestorStatus = {} as GestorStatus;
  
  audioForm!: FormGroup;
  videoForm!: FormGroup;
  selectedAudioFile: File | null = null;
  selectedVideoUrl: string = '';
  selectedThumbnail: File | null = null;
  audioPreviewUrl: string | null = null;
  videoPreviewUrl: string | null = null;
  thumbnailPreviewUrl: string | null = null;
  isDragOver: boolean = false;
  audioFileDuration: number | null = null;
  isCalculatingDuration: boolean = false;
  embedUrl: SafeResourceUrl | null = null;
  isTagsDropdownOpen: boolean = false;

  public readonly GESTOR_ROUTES = GESTOR_ROUTES;
  public readonly APP_ROUTES = APP_ROUTES;

  tags: Tags[] = [];
  resoluciones = RESOLUCIONES_PERMITIDAS;
  formatosImagen = IMAGENES_PERMITIDAS;
  formatosFicheros = FICHEROS_PERMITIDOS;
  pegiOptions = PEGI_PERMITIDO;

  acceptExtensionsAudio: string = FICHEROS_PERMITIDOS.extensiones.length
    ? '.' + FICHEROS_PERMITIDOS.extensiones.join(', .')
    : '';
  acceptExtensionsImagen: string = IMAGENES_PERMITIDAS.extensiones.length
    ? '.' + IMAGENES_PERMITIDAS.extensiones.join(', .')
    : '';

  constructor(
    private readonly formValidationService: FormValidationService,
    private readonly sharedService: SharedService,
    private readonly gestorService: GestorService,
    private readonly contentService: ContentService,
    private readonly notificationService: NotificationService,
    private readonly personalService: PersonalService
  ) {}

  // =============================================================================
  // INICIALIZACIÓN
  // =============================================================================

  ngOnInit(): void {
    // 1) Inicializar SIEMPRE los formularios antes de que Angular pinte nada
    this.initializeForms();

    this.loadGestor();

    // 3) Cargar tags
    this.sharedService.getTags().subscribe((tags) => {
      this.tags = tags;
    });
  }

  /**
   * Crea SIEMPRE ambos formularios para evitar que currentForm sea undefined.
   */
  private initializeForms(): void {
    this.audioForm = this.formValidationService.createAudioContenidoForm();
    this.audioForm.addControl('visibilidad', new FormControl(true));

    this.videoForm = this.formValidationService.createVideoContenidoForm();
    this.videoForm.addControl('visibilidad', new FormControl(true));
  }


  /**
   * Carga el gestor usando el userId del token.
   * Si falla, usa MOCK_GESTORES[0] como fallback.
   * Ya NO inicializa formularios aquí (eso se hace en ngOnInit).
   */
  private loadGestor(): void {
    try {
      const credentials = this.sharedService.obtainCredentials();

      if (!credentials?.userId || credentials?.rol !== 'CREADOR') {
        console.warn('[SubirContenido] Credenciales no válidas o rol distinto de CREADOR, usando MOCK_GESTORES[0]');
        this.actualGestor = MOCK_GESTORES[0];
        return;
      }

      const userId = credentials.userId;

      this.personalService.getCreador(userId).subscribe({
        next: (creadorData: any) => {
          const apellidosRaw = creadorData.apellidos ?? '';
          const [primerApellido, segundoApellido] = apellidosRaw.split(' ');

          this.actualGestor = {
            idUsuario: creadorData.idUsuario,
            tipo: 'gestor',
            nombre: creadorData.nombre,
            primerApellido: primerApellido || '',
            segundoApellido: segundoApellido || '',
            email: creadorData.email,
            alias: creadorData.alias,
            tipoContenido: creadorData.tipoContenido,
            especialidad: creadorData.especialidad,
            descripcion: creadorData.descripcion,
            fotoPerfil: creadorData.fotoPerfil ?? 'assets/porDefecto.jpg',
            bloqueado: creadorData.bloqueado ?? false,
            validado: creadorData.validado ?? false,
            twoFactorEnabled: creadorData.twoFactorEnabled,
            thirdFactorEnabled: creadorData.thirdFactorEnabled
          } as GestorStatus;
        },
        error: (error) => {
          console.error('[SubirContenido] Error al cargar gestor, usando MOCK_GESTORES[0]:', error);
          this.actualGestor = MOCK_GESTORES[0];
        }
      });
    } catch (error) {
      console.error('[SubirContenido] Error al obtener credenciales, usando MOCK_GESTORES[0]:', error);
      this.actualGestor = MOCK_GESTORES[0];
    }
  }

  /**
   * Siempre devuelve un FormGroup válido:
   * - Si el gestor es de VIDEO -> videoForm
   * - En cualquier otro caso -> audioForm
   */
  get currentForm(): FormGroup {
    if (this.actualGestor?.tipoContenido === 'VIDEO') {
      return this.videoForm;
    }
    // Por defecto, o si tipoContenido === 'AUDIO' o aún no está cargado
    return this.audioForm;
  }

  // =============================================================================
  // MANEJO DE FICHEROS (AUDIO / MINIATURA / VIDEO)
  // =============================================================================

  async onFileSelected(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    if (input.files?.[0]) {
      this.selectedAudioFile = input.files[0];

      const validation = this.formValidationService.validateAudioFile(this.selectedAudioFile);
      if (!validation.isValid) {
        this.notificationService.warning(validation.error ?? 'Archivo no válido');
        this.selectedAudioFile = null;
        return;
      }

      this.audioPreviewUrl = URL.createObjectURL(this.selectedAudioFile);
      this.isCalculatingDuration = true;
      const duration = await this.getFileDuration(this.selectedAudioFile);
      this.isCalculatingDuration = false;

      if (duration === null) {
        this.handleDurationFailure();
        return;
      }
      this.handleDurationSuccess(duration);
    }
  }

  private handleDurationFailure(): void {
    this.notificationService.error('No se pudo obtener la duración del archivo. Inténtalo de nuevo.');
    if (this.audioPreviewUrl) {
      try { URL.revokeObjectURL(this.audioPreviewUrl); } catch {}
    }
    this.selectedAudioFile = null;
    this.audioPreviewUrl = null;
    this.audioFileDuration = null;
  }

  private handleDurationSuccess(duration: number): void {
    this.audioFileDuration = duration;
    this.audioForm.patchValue({ duracion: this.audioFileDuration });
  }

  private async getFileDuration(file: File): Promise<number | null> {
    const objectUrl = URL.createObjectURL(file);
    const mediaElement: HTMLAudioElement = new Audio();
    mediaElement.preload = 'metadata';

    const readDurationFromElement = (): Promise<number | null> => {
      return new Promise<number | null>((resolve) => {
        let timeoutId: any;

        const cleanup = () => {
          mediaElement.removeEventListener('loadedmetadata', onLoaded);
          mediaElement.removeEventListener('error', onError);
          if (timeoutId) clearTimeout(timeoutId);
          try { mediaElement.src = ''; } catch {}
          try { URL.revokeObjectURL(objectUrl); } catch {}
        };

        const onLoaded = (): void => {
          const d = Number.isFinite(mediaElement.duration) ? Math.round(mediaElement.duration) : null;
          cleanup();
          resolve(d);
        };

        const onError = (): void => {
          cleanup();
          resolve(null);
        };

        mediaElement.addEventListener('loadedmetadata', onLoaded);
        mediaElement.addEventListener('error', onError);
        mediaElement.src = objectUrl;

        timeoutId = setTimeout(() => {
          cleanup();
          resolve(null);
        }, 4000);
      });
    };

    try {
      const durationFromElement = await readDurationFromElement();
      if (durationFromElement !== null) return durationFromElement;

      try {
        const arrayBuffer = await file.arrayBuffer();
        const AudioCtx = (globalThis as any).AudioContext || (globalThis as any).webkitAudioContext;
        if (!AudioCtx) return null;
        const audioCtx = new AudioCtx();
        const audioBuffer = await new Promise<AudioBuffer>((res, rej) => {
          const onDecoded = (decoded: AudioBuffer) => res(decoded);
          const onDecodeError = (err: any) => rej(new Error(String(err ?? 'decodeAudioData error')));
          audioCtx.decodeAudioData(arrayBuffer, onDecoded, onDecodeError);
        });
        const duration = Math.round(audioBuffer.duration);
        try { audioCtx.close(); } catch {}
        return duration;
      } catch (e) {
        console.warn('WebAudio decode fallback failed:', e);
        return null;
      }
    } catch (e) {
      console.error('Error al obtener duración del archivo:', e);
      return null;
    } finally {
      try { URL.revokeObjectURL(objectUrl); } catch {}
    }
  }

  onThumbnailSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.[0]) {
      this.selectedThumbnail = input.files[0];

      const validation = this.formValidationService.validateImageFile(this.selectedThumbnail);
      if (!validation.isValid) {
        this.notificationService.warning(validation.error ?? 'Archivo no válido');
        this.selectedThumbnail = null;
        return;
      }

      this.thumbnailPreviewUrl = URL.createObjectURL(this.selectedThumbnail);
    }
  }

  onVideoUrlChange(url: string): void {
    this.selectedVideoUrl = url;
    this.videoPreviewUrl = url;
    this.embedUrl = this.getEmbedUrl(url);
  }

  // =============================================================================
  // SUBMIT
  // =============================================================================

  onSubmit(): void {
    if (this.actualGestor.tipoContenido === 'AUDIO') {
      this.submitAudio();
    } else {
      this.submitVideo();
    }
  }

  async submitAudio(): Promise<void> {
    if (!this.formValidationService.validateFormBeforeSubmit(this.currentForm)) {
      return;
    }

    if (!this.selectedAudioFile || this.isCalculatingDuration || this.audioFileDuration === null || !this.selectedThumbnail) {
      return;
    }

    try {
      const metadata = await this.prepareCommonMetadata(this.audioForm, 'AUDIO');
      const base64Audio = await this.sharedService.fileToBase64(this.selectedAudioFile);
      const fileExt = (this.selectedAudioFile.name.split('.').pop() ?? '').toLowerCase();

      const payload = {
        ...metadata,
        fichero: base64Audio,
        ficheroExtension: fileExt ? `audio/${fileExt}` : ''
      };

      this.gestorService.uploadAudioJson(payload).subscribe({
        next: () => {
          this.notificationService.success('Tu contenido de audio ha sido subido correctamente.', 'Audio subido');
          this.resetForm();
        },
        error: () => {
          this.notificationService.error('No se pudo subir el audio. Inténtalo de nuevo.');
        }
      });
    } catch (error) {
      console.error('Error al convertir archivos a base64:', error);
    }
  }

  async submitVideo(): Promise<void> {
    if (!this.formValidationService.validateFormBeforeSubmit(this.currentForm) || !this.selectedThumbnail) {
      return;
    }

    try {
      const metadata = await this.prepareCommonMetadata(this.videoForm, 'VIDEO');
      const payload = {
        ...metadata,
        urlArchivo: this.selectedVideoUrl,
        resolucion: this.videoForm.value.resolucion
      };

      this.gestorService.uploadVideo(payload).subscribe({
        next: () => {
          this.notificationService.success('Tu contenido de video ha sido subido correctamente.', 'Video subido');
          this.resetForm();
        },
        error: () => {
          this.notificationService.error('No se pudo subir el video. Inténtalo de nuevo.');
        }
      });
    } catch (error) {
      console.error('Error al procesar el video:', error);
      this.notificationService.error('No se pudo procesar el video. Inténtalo de nuevo.');
    }
  }

  private async prepareCommonMetadata(form: FormGroup, tipo: 'AUDIO' | 'VIDEO'): Promise<any> {
    const base64Thumbnail = await this.sharedService.fileToBase64(this.selectedThumbnail!);
    const formData = form.value;
    formData.categorias = formData.tags;

    return {
      titulo: formData.titulo,
      descripcion: formData.descripcion,
      tags: formData.categorias,
      duracion: formData.duracion,
      esVIP: formData.vip ?? false,
      fechaSubida: null,
      fechaExpiracion: formData.fechaExpiracion ?? null,
      restriccionEdad: formData.pegi,
      miniatura: base64Thumbnail,
      especialidad: this.actualGestor.especialidad[0],
      visibilidad: formData.visibilidad ?? true,  // ⬅ USAMOS LO QUE MARCA EL FORM
      formatoMiniatura: 'image/' + (this.selectedThumbnail!.name.split('.').pop() ?? '')
    };

  }

  resetForm(): void {
    this.currentForm.reset();
    this.selectedAudioFile = null;
    this.selectedVideoUrl = '';
    this.selectedThumbnail = null;
    this.audioPreviewUrl = null;
    this.videoPreviewUrl = null;
    this.thumbnailPreviewUrl = null;
    this.audioFileDuration = null;
    this.embedUrl = '';
  }

  // =============================================================================
  // VALIDACIÓN Y TAGS
  // =============================================================================

  isFieldInvalid(form: FormGroup, fieldName: string): boolean {
    return this.sharedService.isFieldInvalid(form, fieldName);
  }

  getFieldError(form: FormGroup, fieldName: string): string {
    return this.sharedService.getFieldError(form, fieldName);
  }

  getEmbedUrl(url: string): SafeResourceUrl | null {
    return this.contentService.getSafeEmbedUrl(url);
  }

  toggleTagsDropdown(): void {
    this.isTagsDropdownOpen = this.sharedService.toggleTagsDropdown(this.isTagsDropdownOpen);
  }

  closeTagsDropdown(): void {
    this.isTagsDropdownOpen = false;
  }

  onTagChange(tag: Tags, checked: boolean): void {
    this.sharedService.onTagChange(tag, checked, this.currentForm);
  }

  isTagSelected(tag: Tags): boolean {
    return this.sharedService.isTagSelected(tag, this.currentForm);
  }

  getSelectedTagsText(): string {
    return this.sharedService.getSelectedTagsText(this.currentForm);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.custom-dropdown')) {
      this.closeTagsDropdown();
    }
  }
}
