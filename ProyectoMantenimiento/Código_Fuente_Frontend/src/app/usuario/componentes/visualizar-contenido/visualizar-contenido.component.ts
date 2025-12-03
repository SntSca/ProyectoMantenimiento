import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import {
  Contenido,
  Videos,
  Audios,
  UserStatus,
  Tags,
  APP_ROUTES,
  USUARIO_ROUTES,
  MOCK_USERS,
  SharedService,
  ContentService
} from '@shared';
import { DomSanitizer, SafeStyle, SafeResourceUrl } from '@angular/platform-browser';
import { UsuarioService } from '../../usuario.service';

@Component({
  selector: 'app-visualizar-contenido',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './visualizar-contenido.component.html',
  styleUrls: ['./visualizar-contenido.component.scss']
})
export class VisualizarContenidoComponent implements OnInit, OnDestroy {

  @Input() contenido: Contenido | Videos | Audios | null = null;

  userProfile: UserStatus = {} as UserStatus;
  tags: Tags[] = [];

  mediaSrc: string = '';
  thumbnailSrc: string = '';
  thumbnailStyle: SafeStyle = '';

  hoverRating: number = 0;
  isFavorite: boolean = false;

  public readonly APP_ROUTES = APP_ROUTES;
  public readonly USUARIO_ROUTES = USUARIO_ROUTES;
  
  // Variables para el menú de navegación
  activeMenuSection: string = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router, 
    private readonly sharedService: SharedService,
    private readonly contentService: ContentService,
    private readonly sanitizer: DomSanitizer,
    private readonly usuarioService: UsuarioService
  ) {}

  // =============================================================================
  // CICLO DE VIDA
  // =============================================================================

  ngOnInit(): void {
    // 1) Cargar tags
    this.sharedService.getTags().subscribe((tags) => {
      this.tags = tags;
    });

    // 2) Cargar usuario actual desde JWT + backend
    this.initCurrentUserFromJwt();

    // 3) Cargar contenido (inyectado o desde ruta)
    if (this.contenido) {
      this.setMediaSrc();
      this.loadFavoriteStatus();
    } else {
      const id = this.route.snapshot.paramMap.get('id');
      const tipo_contenido = this.route.snapshot.paramMap.get('tipo');
      if (id && tipo_contenido) {
        this.loadContentById(tipo_contenido, id);
      }
    }
  }

  ngOnDestroy(): void {
    // Limpiar la URL del objeto si fue creada para audio
    if (this.mediaSrc && this.contenido?.tipo === 'AUDIO') {
      URL.revokeObjectURL(this.mediaSrc);
    }
    // Limpiar también la URL de la miniatura
    if (this.thumbnailSrc && this.contenido?.tipo === 'AUDIO' && this.thumbnailSrc.startsWith('blob:')) {
      URL.revokeObjectURL(this.thumbnailSrc);
    }
  }


  private initCurrentUserFromJwt(): void {
    try {
      const credentials = this.sharedService.obtainCredentials();
      if (!credentials?.userId) {
        console.warn('[VisualizarContenido] No se han podido obtener credenciales del JWT');
        // No forzamos navegación aquí para no romper la vista si se usa embed, pero podrías redirigir si quieres
        this.userProfile = MOCK_USERS[0];
        return;
      }

      const userId = credentials.userId;

      this.usuarioService.getUsuario(userId).subscribe({
        next: (userData: any) => {
          this.userProfile = {
            idUsuario: userData.idUsuario,
            tipo: 'usuario',
            nombre: userData.nombre,
            primerApellido: userData.apellidos?.split(' ')[0] || '',
            segundoApellido: userData.apellidos?.split(' ')[1] || '',
            email: userData.email,
            alias: userData.alias,
            fechaNacimiento: userData.fechaNacimiento,
            flagVIP: userData.flagVIP,
            fotoPerfil: userData.fotoPerfil ?? 'assets/porDefecto.jpg',
            bloqueado: userData.bloqueado,
            twoFactorEnabled: userData.twoFactorEnabled,
            thirdFactorEnabled: userData.thirdFactorEnabled
          } as UserStatus;
        },
        error: (error) => {
          console.error('[VisualizarContenido] Error al obtener datos del usuario, usando MOCK_USERS[0]:', error);
          this.userProfile = MOCK_USERS[0];
        }
      });
    } catch (e) {
      console.error('[VisualizarContenido] Error al obtener credenciales del JWT:', e);
      this.userProfile = MOCK_USERS[0];
    }
  }

  // =============================================================================
  // CARGA DE CONTENIDO
  // =============================================================================

  /**
   * Carga contenido específico por ID desde el API
   */
  private loadContentById(tipoContenido: string, id: string): void {
    if (tipoContenido === 'AUDIO') {
      this.contentService.getAudioById(id).subscribe({
        next: (audio) => {
          if (audio) {
            this.contenido = audio;
            this.setMediaSrc();
            this.contentService.viewedAudio(id).subscribe();
            this.contenido.visualizaciones += 1;
          }
        },
        error: (error) => {
          console.error('Error obteniendo audio:', error);
        }
      });
    } else if (tipoContenido === 'VIDEO') {
      this.contentService.getVideoById(id).subscribe({
        next: (video) => {
          if (video) {
            this.contenido = video;
            this.setMediaSrc();
            this.contentService.viewedVideo(id).subscribe();
            this.contenido.visualizaciones += 1;
          }
        },
        error: (error) => {
          console.error('Error obteniendo video:', error);
        }
      });
    }
  }

  // Método para setear mediaSrc de forma asíncrona
  private async setMediaSrc(): Promise<void> {
    if (!this.contenido) return;

    const contenido = this.contenido as Contenido;
    const thumbnailBlob = await this.sharedService.base64ToFile(contenido.miniatura);
    this.thumbnailSrc = URL.createObjectURL(thumbnailBlob);

    this.thumbnailStyle = this.sanitizer.bypassSecurityTrustStyle(`url("${this.thumbnailSrc}")`);
    
    if (this.contenido.tipo === 'VIDEO') {
      this.mediaSrc = (this.contenido as Videos).urlArchivo;
    } else if (this.contenido.tipo === 'AUDIO') {
      const audio = this.contenido as Audios;
      const blob = await this.sharedService.base64ToFile(audio.fichero);
      this.mediaSrc = URL.createObjectURL(blob);
    }

    // Cargar estado de favoritos una vez que tenemos el ID
    this.loadFavoriteStatus();
  }

  // =============================================================================
  // FAVORITOS Y VALORACIONES
  // =============================================================================

  rateContent(rating: number): void {
    if (this.contenido && this.contenido.valoracionUsuario != 0) {
      return;
    }

    if (!this.contenido?.id) return;

    this.contenido.valoracionUsuario = rating;
    this.contentService.rateContent(this.contenido.id, rating).subscribe({
      next: () => {
        this.loadContentById(this.contenido!.tipo, this.contenido!.id!);
      },
      error: (error) => {
        console.error('Error al enviar la valoración:', error);
      }
    });
  }

  toggleFavorite(): void {
    if (!this.contenido?.id) return;

    const previousState = this.isFavorite;
    this.isFavorite = !this.isFavorite;
    
    if (this.isFavorite) {
      this.contentService.addFavoriteContent(this.contenido.id).subscribe({
        next: () => {},
        error: (error) => {
          console.error('Error al añadir a favoritos:', error);
          this.isFavorite = previousState;
        }
      });
    } else {
      this.contentService.removeFavoriteContent(this.contenido.id).subscribe({
        next: () => {},
        error: (error) => {
          console.error('Error al eliminar de favoritos:', error);
          this.isFavorite = previousState;
        }
      });
    }
  }

  private loadFavoriteStatus(): void {
    if (!this.contenido?.id) return;

    this.contentService.getFavoriteContents().subscribe({
      next: (favorites) => {
        this.isFavorite = favorites.includes(this.contenido!.id!);
      },
      error: (error) => {
        console.error('Error al cargar favoritos:', error);
        this.isFavorite = false;
      }
    });
  }

  // =============================================================================
  // UTILIDADES DE VISTA
  // =============================================================================

  getEmbedUrl(): SafeResourceUrl | null {
    return this.contentService.getSafeEmbedUrl(
      this.contenido?.tipo === 'VIDEO'
        ? (this.contenido as Videos)?.urlArchivo ?? ''
        : ''
    );
  }

  getStarsArray(rating: number): number[] {
    return this.sharedService.getStarsArray(rating);
  }

  // =============================================================================
  // MÉTODOS DE NAVEGACIÓN DEL MENÚ
  // =============================================================================

  public setActiveMenuSection(section: string): void {
    this.activeMenuSection = section;
    
    if (section === 'inicio') {
      this.router.navigate([USUARIO_ROUTES.inicio]);
    }
    
    if (section === 'listas-publicas') {
      this.router.navigate([USUARIO_ROUTES.listas], { queryParams: { tipo: 'publicas' } });
    }
    
    if (section === 'favoritos') {
      this.router.navigate([USUARIO_ROUTES.inicio], { queryParams: { section: 'favoritos' } });
    }
  }

  public isMenuSectionActive(section: string): boolean {
    return this.activeMenuSection === section;
  }

  public loadFavorites(): void {
    this.setActiveMenuSection('favoritos');
  }

  public logout(): void {
    this.sharedService.performLogout(this.router, APP_ROUTES.home);
  }
}
