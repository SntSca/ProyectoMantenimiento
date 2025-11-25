import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common'; // Para *ngIf, *ngFor
import { RouterModule, Router, ActivatedRoute } from '@angular/router'; // Para routerLink y Router
import { FormsModule } from '@angular/forms'; // Si usas formularios reactivos
import { Subject } from 'rxjs';
import { APP_ROUTES, USUARIO_ROUTES, Videos, Audios, Contenido, ContentService, SharedService, Tags, UserStatus, FilterService} from '@shared';
import { UsuarioService } from '../../usuario.service';
import { NotificationService } from '../../../shared/services/notification.service';

@Component({
  selector: 'app-inicio-usuario',
  standalone: true, // Cambia a true
  imports: [CommonModule, RouterModule, FormsModule], // Agrega módulos necesarios
  templateUrl: './inicio-usuario.component.html',
})
export class InicioUsuarioComponent implements OnInit, OnDestroy {


  actualUser: UserStatus = {} as UserStatus;
 
  
  searchQuery: string = '';
  searchTerm: string = '';
  filtroTagsContenidos: string[] = [];
  isTipoContenidoContenidos: boolean | null = null;
  filtroTipoContenidoContenidos: string | null = null;
  filtroRestriccionEdad: number | null = null;
  showTagFilters: boolean = false;
  private readonly searchSubject = new Subject<string>();
  private readonly destroy$ = new Subject<void>();

  tags: Tags[] = [];
  videos: Videos[] = [];
  audios: Audios[] = [];
  contenidos: Contenido[] = [];
  contenidosFiltrados: Contenido[] = [];
  contenidosBase: Contenido[] = []; // Contenidos base para filtrar (todos o favoritos)
  
  // Estados de carga
  loadingVideos = false;
  loadingAudios = false;
  loadingContent = false;
  showingFavorites = false;
  
  // Estado del menú
  activeMenuSection: string = 'inicio';
  
  // Subject para búsqueda dinámica

 


  public readonly USUARIO_ROUTES = USUARIO_ROUTES;
  public readonly APP_ROUTES = APP_ROUTES;


  isListView: boolean = false;

  constructor(
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly contentService: ContentService,
    private readonly sharedService: SharedService,
    private readonly filterService: FilterService,
    private readonly usuarioService: UsuarioService,
    private readonly notificationService: NotificationService
  ) {}
  
  
  ngOnInit(): void {

    if (localStorage.getItem('user_data')) {
      // Los datos ya están procesados y guardados, asignar directamente
      this.actualUser = JSON.parse(localStorage.getItem('user_data')!);
      // Procesar la foto de perfil para mostrarla
      this.processProfilePhoto();
    }



    // Suscribirse a cambios en query parameters para reaccionar dinámicamente
    this.route.queryParams.subscribe(params => {
      if (params['section'] === 'favoritos') {
        this.loadFavorites();
      }
      else {
        this.loadContent();
      }
    });

    this.sharedService.getTags().subscribe((tags) => {
      this.tags = tags;
    });

    // Configurar búsqueda dinámica con debounce
    this.sharedService.setupDynamicSearch(this.searchSubject, (searchTerm) => {
      this.searchTerm = searchTerm;
      this.aplicarFiltros();
    }, this.destroy$);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.searchSubject.complete();
  }

  private processProfilePhoto(): void {
    // Las fotos ahora vienen como URI, no necesitan procesamiento
    if (this.actualUser.fotoPerfil && !this.actualUser.fotoPerfil.startsWith('blob:') && !this.actualUser.fotoPerfil.startsWith('data:')) {
      // Ya es una URI válida, no hacer nada
    }
  }

  onSearchInputChange(searchTerm: string): void {
    this.sharedService.handleSearchInput(this.searchSubject, searchTerm);
  }

  // Getter y setter para searchTerm con debounce
  get searchTermValue(): string {
    return this.searchTerm;
  }

  set searchTermValue(value: string) {
    this.searchTerm = value;
    this.onSearchInputChange(value);
  }

  buscar(): void {
    this.aplicarFiltros();
  }

  aplicarFiltros(): void {
    // Usar contenidosBase en lugar de contenidos para mantener el contexto (todos o favoritos)
    const baseParaFiltrar = this.showingFavorites ? this.contenidosBase : this.contenidos;
    this.contenidosFiltrados = [...this.filterService.aplicarFiltros(baseParaFiltrar, {
      filtroTagsContenidos: this.filtroTagsContenidos,
      filtroTipoContenidoContenidos: this.filtroTipoContenidoContenidos,
      filtroRestriccionEdad: this.filtroRestriccionEdad
    })];
    this.contenidosFiltrados = [...this.filterService.filtrarPorBusqueda(this.contenidosFiltrados, this.searchTerm, 'contenidos')];
  }

  secondsToMinutesSeconds(seconds: number): string {
    return this.sharedService.secondsToMinutesSeconds(seconds);
  }

  filtrarPorTags(tag: string): void {
    // Si el tag ya está seleccionado, quitarlo; si no, agregarlo
    const index = this.filtroTagsContenidos.indexOf(tag);
    if (index > -1) {
      this.filtroTagsContenidos.splice(index, 1);
    } else {
      this.filtroTagsContenidos.push(tag);
    }
    this.aplicarFiltros();
    this.searchTermValue = '';
  }

  filtrarPorTipoContenidoContenidos(tipo: string | null): void {
    this.filtroTipoContenidoContenidos = tipo;
    this.aplicarFiltros();
    this.searchTermValue = '';
  }

  toggleRestriccionEdad(): void {
    if (this.filtroRestriccionEdad === null) {
      this.filtroRestriccionEdad = 3;
    } else if (this.filtroRestriccionEdad === 3) {
      this.filtroRestriccionEdad = 7;
    } else if (this.filtroRestriccionEdad === 7) {
      this.filtroRestriccionEdad = 12;
    } else if (this.filtroRestriccionEdad === 12) {
      this.filtroRestriccionEdad = 16;
    } else {
      this.filtroRestriccionEdad = null;
    }
    this.aplicarFiltros();
  }

  restablecerFiltros(): void {
    this.filtroTagsContenidos = [];
    this.filtroTipoContenidoContenidos = null;
    this.isTipoContenidoContenidos = null;
    this.filtroRestriccionEdad = null;
    this.searchTermValue = '';
    this.aplicarFiltros();
  }



  limpiarFiltrosTags(): void {
    this.filtroTagsContenidos = [];
    this.aplicarFiltros();
    this.searchTermValue = '';
  }

  toggleTagFilters(): void {
    this.showTagFilters = !this.showTagFilters;
  }

  isTagActive(tagNombre: string): boolean {
    return this.filtroTagsContenidos.includes(tagNombre);
  }
  
  // =============================================================================
  // MÉTODOS DE VISTA
  // =============================================================================

  toggleView(): void {
    this.isListView = this.sharedService.toggleView(this.isListView);
  }

  verContenido(contenido: Contenido): void {

    if(contenido.esVIP && !this.actualUser.flagVIP){
      this.notificationService.info('¡Hazte VIP para acceder a contenido exclusivo!', 'Contenido VIP');
      return;
    }
    const edad = this.sharedService.calculateAge(this.actualUser.fechaNacimiento);
      if (edad < contenido.restriccionEdad) {  // Asume que Contenido tiene 'restriccionEdad' (edad mínima)
        this.notificationService.warning('No tienes la edad suficiente para ver este contenido.', 'Acceso restringido');
        return;
      }
    this.router.navigate([USUARIO_ROUTES.visualizar, contenido.tipo, contenido.id], { state: { fromNavigation: true } });
  }

  // =============================================================================
  // MÉTODOS DE CARGA DE DATOS
  // =============================================================================

  /**
   * Carga el contenido desde el API
   */
  private loadContent(): void {
    this.loadingContent = true;

    this.sharedService.loadContent(this.contentService).subscribe({
      next: (result) => {
        this.contenidos = result.contenidos;
        this.contenidosBase = [...result.contenidos]; // Usar todos los contenidos como base
        this.contenidosFiltrados = [...result.contenidos];
        this.loadingContent = false;
      },
      error: (error: any) => {
        console.error('Error al cargar contenido:', error);
        this.loadingContent = false;
      }
    });
  }


  public refreshContent(): void {
    this.activeMenuSection = 'inicio';
    this.showingFavorites = false;
    this.contenidosBase = []; // Resetear base
    this.loadContent();
  }

  /**
   * Cambia la sección activa del menú
   */
  public setActiveMenuSection(section: string): void {
    this.activeMenuSection = section;
    
    // Si cambia a inicio, refrescar contenido
    if (section === 'inicio') {
      this.refreshContent();
    }
    
    // Si cambia a listas públicas, navegar con el query parameter correcto
    if (section === 'listas-publicas') {
      this.router.navigate([USUARIO_ROUTES.listas], { queryParams: { tipo: 'publicas' } });
    }
  }

  /**
   * Verifica si una sección del menú está activa
   */
  public isMenuSectionActive(section: string): boolean {
    return this.activeMenuSection === section;
  }

  /**
   * Carga los contenidos favoritos del usuario
   */
  public loadFavorites(): void {
    if (!this.actualUser.idUsuario) return;

    this.activeMenuSection = 'favoritos';
    this.loadingContent = true;
    this.showingFavorites = true;
    
    // Si no hay contenidos cargados, cargarlos primero
    if (this.contenidos.length === 0) {
      this.sharedService.loadContent(this.contentService).subscribe({
        next: (result) => {
          this.contenidos = result.contenidos;
          this.loadFavoriteIds();
        },
        error: (error) => {
          console.error('Error al cargar contenido:', error);
          this.loadingContent = false;
        }
      });
    } else {
      this.loadFavoriteIds();
    }
  }

  /**
   * Carga los IDs de favoritos y filtra los contenidos
   */
  private loadFavoriteIds(): void {
    this.contentService.getFavoriteContents().subscribe({
      next: (favoriteIds) => {
        const favoritos = this.contenidos.filter(contenido =>
          favoriteIds.includes(contenido.id!)
        );
        this.contenidosBase = [...favoritos]; // Guardar favoritos como base para filtros
        this.contenidosFiltrados = [...favoritos];
        this.loadingContent = false;
      },
      error: (error) => {
        console.error('Error al cargar favoritos:', error);
        this.loadingContent = false;
      }
    });
  }

  /**
   * Elimina un contenido de favoritos
   */
  public removeFavorite(event: Event, contenido: Contenido): void {
    event.stopPropagation(); // Evitar que se ejecute verContenido()
    
    if (!this.actualUser.idUsuario || !contenido.id) return;

    this.contentService.removeFavoriteContent(contenido.id).subscribe({
      next: () => {
        
        // Quitar el contenido de la lista mostrada
        this.contenidosFiltrados = this.contenidosFiltrados.filter(c => c.id !== contenido.id);
      },
      error: (error) => {
        console.error('Error al eliminar de favoritos:', error);
      }
    });
  }

  /**
   * Método para cerrar sesión
   */
  logout(): void {
    this.sharedService.performLogout(this.router, this.APP_ROUTES.home);
  }
  getStarsArray(rating: number): number[] {
    return this.sharedService.getStarsArray(rating);
  }
}
