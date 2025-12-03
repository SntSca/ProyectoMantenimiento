import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { 
  FormValidationService, 
  APP_ROUTES, 
  USUARIO_ROUTES,
  GESTOR_ROUTES,
  Contenido, 
  ContentService, 
  SharedService, 
  Tags, 
  UserStatus,
  FilterService,
  ListaPrivada
} from '@shared';
import { NotificationService } from '../../../shared/services/notification.service';
import { UsuarioService } from '../../usuario.service';

@Component({
  selector: 'app-crear-lista-privada',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: '../../../shared/components/crear-lista/crear-lista.component.html'
})
export class CrearListaPrivadaComponent implements OnInit, OnDestroy {
  
  // Constantes de rutas
  public readonly USUARIO_ROUTES = USUARIO_ROUTES;
  public readonly GESTOR_ROUTES = GESTOR_ROUTES;
  public readonly APP_ROUTES = APP_ROUTES;

  // Modo edición
  isEditMode = false;
  listaId: string | null = null;
  listaOriginal: ListaPrivada | null = null;
  contenidosOriginales: Set<string> = new Set();

  // Textos personalizables (dinámicos según el modo)
  get tituloFormulario(): string {
    return this.isEditMode ? 'Editar Lista Privada' : 'Crear Lista Privada';
  }
  
  get subtituloFormulario(): string {
    return this.isEditMode ? 'Modifica el contenido de tu lista personalizada' : 'Organiza tu contenido favorito en listas personalizadas';
  }
  
  get textoBotonConfirmar(): string {
    return this.isEditMode ? '✓ Confirmar edición' : '✓ Confirmar y crear lista privada';
  }
  
  public readonly esGestor = false; // Para identificar el tipo de usuario

  // Usuario actual (cabecera del formulario)
  actualUser: UserStatus = {} as UserStatus;

  // Id del usuario actual (sacado del JWT)
  private userId!: string;

  // Formulario
  listaForm: FormGroup;
  submitted = false;

  // Estado del componente
  isCreationMode = false; // Modo de selección de contenido
  selectedContentIds: Set<string> = new Set(); // IDs de contenidos seleccionados
  activeMenuSection: string = 'crear-lista'; // Sección activa del menú

  // Contenidos
  contenidos: Contenido[] = [];
  contenidosFiltrados: Contenido[] = [];
  tags: Tags[] = [];

  // Búsqueda y filtros
  searchTerm: string = '';
  filtroTags: string[] = [];
  filtroTipoContenidoContenidos: string | null = null;
  filtroRestriccionEdad: number | null = null;
  showTagFilters: boolean = false;
  private readonly searchSubject = new Subject<string>();
  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly formValidationService: FormValidationService,
    private readonly contentService: ContentService,
    private readonly sharedService: SharedService,
    private readonly filterService: FilterService,
    private readonly notificationService: NotificationService,
    private readonly usuarioService: UsuarioService
  ) {
    this.listaForm = this.formValidationService.createListaForm();
    
    // Detectar modo edición inmediatamente para evitar parpadeos
    this.listaId = this.route.snapshot.paramMap.get('id');
    this.isEditMode = !!this.listaId;
    
    // Si es modo edición, iniciar directamente en modo selección de contenido
    if (this.isEditMode) {
      this.isCreationMode = true;
    }
  }

  ngOnInit(): void {
    // 1) Obtener userId desde el JWT (sin usar sessionStorage directamente aquí)
    this.initCurrentUserFromJwt();

    // 2) Cargar contenidos
    this.loadContent();

    // 3) Cargar tags
    this.sharedService.getTags().subscribe((tags: any[]) => {
      this.tags = tags;
    });

    // 4) Si es modo edición, cargar datos de la lista
    if (this.isEditMode && this.listaId) {
      this.loadListaForEdit(this.listaId);
    }

    // 5) Configurar búsqueda dinámica
    this.sharedService.setupDynamicSearch(this.searchSubject, (searchTerm) => {
      this.searchTerm = searchTerm;
      this.buscar();
    }, this.destroy$);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.searchSubject.complete();
  }


  private initCurrentUserFromJwt(): void {
    try {
      const credentials = this.sharedService.obtainCredentials();
      if (!credentials?.userId) {
        console.warn('[CrearListaPrivada] No se han podido obtener credenciales del JWT');
        // Si no tienes userId, mejor redirigir al login / inicio
        this.router.navigate([this.APP_ROUTES.home]);
        return;
      }

      this.userId = credentials.userId;

      // Llamar al backend para obtener los datos completos del usuario
      this.usuarioService.getUsuario(this.userId).subscribe({
        next: (userData: any) => {
          this.actualUser = {
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
          console.error('[CrearListaPrivada] Error al obtener datos del usuario:', error);
          // En caso de error, al menos que no rompa el template
          this.actualUser = {
            fotoPerfil: 'assets/porDefecto.jpg',
            alias: 'Mi perfil',
            nombre: 'Mi perfil'
          } as unknown as UserStatus;
        }
      });
    } catch (e) {
      console.error('[CrearListaPrivada] Error al obtener credenciales del JWT:', e);
      this.router.navigate([this.APP_ROUTES.home]);
    }
  }

  // =============================================================================
  // MÉTODOS DE CARGA DE DATOS
  // =============================================================================

  private loadContent(): void {
    this.sharedService.loadContent(this.contentService).subscribe({
      next: (result) => {
        this.contenidos = result.contenidos;
        this.contenidosFiltrados = result.contenidosFiltrados;
      },
      error: (error) => {
        console.error('Error al cargar contenido:', error);
      }
    });
  }

  private loadListaForEdit(listaId: string): void {
    this.contentService.getPrivateLists().subscribe({
      next: (listas) => {
        this.listaOriginal = listas.find((lista: ListaPrivada) => lista.idLista === listaId) || null;
        if (this.listaOriginal) {
          // Rellenar el formulario con los datos de la lista
          this.listaForm.patchValue({
            nombre: this.listaOriginal.nombre,
            descripcion: this.listaOriginal.descripcion
          });

          // Marcar contenidos como seleccionados
          if (this.listaOriginal.contenidos) {
            for (const contenido of this.listaOriginal.contenidos) {
              if (contenido.id) {
                this.selectedContentIds.add(contenido.id);
                this.contenidosOriginales.add(contenido.id);
              }
            }
          }
        }
      },
      error: (error) => {
        console.error('Error al cargar lista para editar:', error);
        this.router.navigate([USUARIO_ROUTES.listas]);
      }
    });
  }

  // =============================================================================
  // MÉTODOS DE FORMULARIO
  // =============================================================================

  isFieldInvalid(fieldName: string): boolean {
    return this.sharedService.isFieldInvalid(this.listaForm, fieldName, this.submitted);
  }

  getFieldError(fieldName: string): string {
    return this.sharedService.getFieldError(this.listaForm, fieldName);
  }

  // =============================================================================
  // MODO CREACIÓN
  // =============================================================================

  activarModoCreacion(): void {
    if (!this.formValidationService.validateFormBeforeSubmit(this.listaForm)) {
      this.submitted = true;
      return;
    }

    this.isCreationMode = true;
    this.selectedContentIds.clear();
  }

  toggleContentSelection(contenido: Contenido): void {
    if (!this.isCreationMode) return;

    const id = contenido.id;
    if (!id) return;

    if (this.selectedContentIds.has(id)) {
      this.selectedContentIds.delete(id);
    } else {
      this.selectedContentIds.add(id);
    }
  }

  isContentSelected(contenido: Contenido): boolean {
    return contenido.id ? this.selectedContentIds.has(contenido.id) : false;
  }

  // =============================================================================
  // ACCIONES
  // =============================================================================

  confirmarCreacion(): void {
    if (this.selectedContentIds.size === 0) {
      this.notificationService.warning('Debes seleccionar al menos un contenido para crear la lista.');
      return;
    }

    if (this.isEditMode) {
      this.confirmarEdicion();
    } else {
      this.confirmarCreacionNueva();
    }
  }

  private confirmarCreacionNueva(): void {
    const contenidosSeleccionados = Array.from(this.selectedContentIds);

    const nuevaLista: ListaPrivada = {
      idLista: `lista-${Date.now()}`, // ID temporal
      nombre: this.listaForm.value.nombre,
      descripcion: this.listaForm.value.descripcion || '',
      idCreadorUsuario: this.userId,
      contenidos: contenidosSeleccionados
    };

    this.contentService.createPrivateList(nuevaLista).subscribe({
      next: () => {
        this.notificationService.success('Tu lista privada ha sido creada correctamente.', 'Lista creada');
        this.router.navigate([USUARIO_ROUTES.listas], { queryParams: { tipo: 'privadas' } });
      },
      error: (error) => {
        console.error('Error al crear la lista privada:', error);
        this.notificationService.error('No se pudo crear la lista. Por favor, inténtalo de nuevo.', 'Error al crear lista');
      }
    });
  }

  private confirmarEdicion(): void {
    if (!this.listaOriginal || !this.listaId) return;

    const contenidosActuales = new Set(this.selectedContentIds);
    const contenidosOriginales = this.contenidosOriginales;

    const contenidosAñadidos = Array.from(contenidosActuales).filter(id => !contenidosOriginales.has(id));
    const contenidosEliminados = Array.from(contenidosOriginales).filter(id => !contenidosActuales.has(id));

    const listaEditada: ListaPrivada = {
      ...this.listaOriginal,
      nombre: this.listaForm.value.nombre,
      descripcion: this.listaForm.value.descripcion || ''
    };

    this.contentService.editPrivateList(listaEditada).subscribe({
      next: () => {
        this.gestionarCambiosContenido(contenidosAñadidos, contenidosEliminados);
      },
      error: (error) => {
        console.error('Error al editar la lista:', error);
        this.notificationService.error('No se pudo editar la lista. Inténtalo de nuevo.');
      }
    });
  }

  private gestionarCambiosContenido(añadidos: string[], eliminados: string[]): void {
    const operaciones: any[] = [];

    if (añadidos.length > 0) {
      const dataAdd = {
        idLista: this.listaId!,
        idsContenido: añadidos,
        idUsuario: this.userId
      };
      operaciones.push(this.contentService.addContentToPrivateList(dataAdd));
    }

    if (eliminados.length > 0) {
      const dataRemove = {
        idLista: this.listaId!,
        idsContenido: eliminados,
        idUsuario: this.userId
      };
      operaciones.push(this.contentService.removeContentFromPrivateList(dataRemove));
    }

    if (operaciones.length > 0) {
      Promise.all(operaciones.map(op => op.toPromise())).then(
        () => {
          this.notificationService.success('Tu lista ha sido editada correctamente.', 'Lista actualizada');
          this.router.navigate([USUARIO_ROUTES.listas], { queryParams: { tipo: 'privadas' } });
        },
        (error) => {
          console.error('Error al gestionar contenidos:', error);
          this.notificationService.warning('Lista editada, pero hubo errores al gestionar algunos contenidos.');
          this.router.navigate([USUARIO_ROUTES.listas], { queryParams: { tipo: 'privadas' } });
        }
      );
    } else {
      this.notificationService.success('Tu lista ha sido editada correctamente.', 'Lista actualizada');
      this.router.navigate([USUARIO_ROUTES.listas], { queryParams: { tipo: 'privadas' } });
    }
  }

  cancelarCreacion(): void {
    if (this.isCreationMode) {
      if (this.isEditMode) {
        this.router.navigate([USUARIO_ROUTES.listas], { queryParams: { tipo: 'privadas' } });
      } else {
        this.isCreationMode = false;
        this.selectedContentIds.clear();
      }
    } else {
      this.listaForm.reset();
      this.router.navigate([USUARIO_ROUTES.inicio]);
    }
  }

  // =============================================================================
  // BÚSQUEDA Y FILTROS
  // =============================================================================

  get searchTermValue(): string {
    return this.searchTerm;
  }

  set searchTermValue(value: string) {
    this.searchTerm = value;
    this.sharedService.handleSearchInput(this.searchSubject, value);
  }

  buscar(): void {
    this.aplicarFiltros();
  }

  aplicarFiltros(): void {
    this.contenidosFiltrados = [...this.filterService.aplicarFiltros(this.contenidos, {
      filtroTagsContenidos: this.filtroTags,
      filtroTipoContenidoContenidos: this.filtroTipoContenidoContenidos,
      filtroRestriccionEdad: this.filtroRestriccionEdad
    })];
    
    this.contenidosFiltrados = [...this.filterService.filtrarPorBusqueda(this.contenidosFiltrados, this.searchTerm, 'contenidos')];
  }

  filtrarPorTags(tag: string | null): void {
    if (tag === null) {
      this.filtroTags = [];
    } else {
      const index = this.filtroTags.indexOf(tag);
      if (index > -1) {
        this.filtroTags.splice(index, 1);
      } else {
        this.filtroTags.push(tag);
      }
    }
    this.aplicarFiltros();
  }

  isTagActive(tag: string): boolean {
    return this.filtroTags.includes(tag);
  }

  toggleTagFilters(): void {
    this.showTagFilters = !this.showTagFilters;
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
    this.filtroTags = [];
    this.filtroTipoContenidoContenidos = null;
    this.filtroRestriccionEdad = null;
    this.searchTerm = '';
    this.showTagFilters = false;
    this.aplicarFiltros();
  }

  // =============================================================================
  // MENÚ LATERAL
  // =============================================================================

  setActiveMenuSection(section: string): void {
    this.activeMenuSection = section;
    
    if (section === 'inicio') {
      this.router.navigate([USUARIO_ROUTES.inicio]);
    } else if (section === 'listas-publicas') {
      this.router.navigate([USUARIO_ROUTES.listas], { queryParams: { tipo: 'publicas' } });
    } else if (section === 'listas-privadas') {
      this.router.navigate([USUARIO_ROUTES.listas], { queryParams: { tipo: 'privadas' } });
    }
  }

  isMenuSectionActive(section: string): boolean {
    return this.activeMenuSection === section;
  }

  loadFavorites(): void {
    this.router.navigate([USUARIO_ROUTES.inicio], { 
      queryParams: { section: 'favoritos'}
    });
  }

  logout(): void {
    this.sharedService.performLogout(this.router, this.APP_ROUTES.home);
  }


  getStarsArray(rating: number): number[] {
    return this.sharedService.getStarsArray(rating);
  }

  secondsToMinutesSeconds(seconds: number): string {
    return this.sharedService.secondsToMinutesSeconds(seconds);
  }
  isSidebarOpen: boolean = false;
  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
  }
  closeSidebarIfMobile(): void {
    if (window.innerWidth <= 768) {
      this.isSidebarOpen = false;
    }
  }

}
