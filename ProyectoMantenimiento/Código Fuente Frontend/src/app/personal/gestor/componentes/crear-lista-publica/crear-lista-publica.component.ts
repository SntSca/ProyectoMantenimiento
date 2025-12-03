import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { 
  FormValidationService, 
  APP_ROUTES, 
  Contenido, 
  ContentService, 
  SharedService, 
  Tags, 
  FilterService,
  USUARIO_ROUTES,
  GESTOR_ROUTES,
  GestorStatus,
  ListaPublica,
  MOCK_GESTORES
} from '@shared';
import { NotificationService } from '../../../../shared/services/notification.service';
import { PersonalService } from '../../../personal.service';

@Component({
  selector: 'app-crear-lista-publica',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: '../../../../shared/components/crear-lista/crear-lista.component.html'
})
export class CrearListaPublicaComponent implements OnInit, OnDestroy {
  
  // Constantes de rutas
  public readonly GESTOR_ROUTES = GESTOR_ROUTES;
  public readonly USUARIO_ROUTES = USUARIO_ROUTES;
  public readonly APP_ROUTES = APP_ROUTES;

  // Modo edición
  isEditMode = false;
  listaId: string | null = null;
  listaOriginal: ListaPublica | null = null;
  contenidosOriginales: Set<string> = new Set();

  // Textos personalizables (dinámicos según el modo)
  get tituloFormulario(): string {
    return this.isEditMode ? 'Editar Lista Pública' : 'Crear Lista Pública';
  }
  
  get subtituloFormulario(): string {
    return this.isEditMode ? 'Modifica el contenido de tu lista pública' : 'Organiza contenidos en listas públicas para tus usuarios';
  }
  
  get textoBotonConfirmar(): string {
    return this.isEditMode ? '✓ Confirmar edición' : '✓ Confirmar y crear lista pública';
  }
  
  public readonly esGestor = true; // Para identificar el tipo de usuario

  // Gestor actual
  actualUser: GestorStatus = {} as GestorStatus;

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
    private readonly personalService: PersonalService
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
    this.initCurrentGestorFromJwt();

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

  // =============================================================================
  // =============================================================================

  private initCurrentGestorFromJwt(): void {
    try {
      const credentials = this.sharedService.obtainCredentials();

      if (!credentials?.userId || credentials?.rol !== 'CREADOR') {
        console.warn('[CrearListaPublica] Credenciales no válidas o rol distinto de CREADOR, usando MOCK_GESTORES[0]');
        this.actualUser = { 
          ...MOCK_GESTORES[0], 
          fotoPerfil: MOCK_GESTORES[0].fotoPerfil ?? 'assets/porDefecto.jpg' 
        };
        return;
      }

      const userId = credentials.userId;

      this.personalService.getCreador(userId).subscribe({
        next: (creadorData: any) => {
          const apellidosRaw = creadorData.apellidos ?? '';
          const [primerApellido, segundoApellido] = apellidosRaw.split(' ');

          this.actualUser = {
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
          console.error('[CrearListaPublica] Error al obtener datos del gestor, usando MOCK_GESTORES[0]:', error);
          this.actualUser = { 
            ...MOCK_GESTORES[0], 
            fotoPerfil: MOCK_GESTORES[0].fotoPerfil ?? 'assets/porDefecto.jpg' 
          };
        }
      });
    } catch (e) {
      console.error('[CrearListaPublica] Error al obtener credenciales del JWT, usando MOCK_GESTORES[0]:', e);
      this.actualUser = { 
        ...MOCK_GESTORES[0], 
        fotoPerfil: MOCK_GESTORES[0].fotoPerfil ?? 'assets/porDefecto.jpg' 
      };
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
    this.contentService.getPublicLists().subscribe({
      next: (listas) => {
        this.listaOriginal = listas.find(lista => lista.idLista === listaId) || null;
        if (this.listaOriginal) {
          // Rellenar el formulario con los datos de la lista
          this.listaForm.patchValue({
            nombre: this.listaOriginal.nombre,
            descripcion: this.listaOriginal.descripcion
          });

          // Marcar contenidos como seleccionados
          if (this.listaOriginal.contenidos) {
            for (const contenido of this.listaOriginal.contenidos) {
              this.selectedContentIds.add(contenido.id);
              this.contenidosOriginales.add(contenido.id);
            }
          }
        }
      },
      error: (error) => {
        console.error('Error al cargar lista para editar:', error);
        this.router.navigate([GESTOR_ROUTES.listas]);
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
  // MÉTODOS DE MODO CREACIÓN
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
  // MÉTODOS DE ACCIONES
  // =============================================================================

  confirmarCreacion(): void {
    if (this.selectedContentIds.size === 0) {
      this.notificationService.warning('Debes seleccionar al menos un contenido para la lista.', 'Selección requerida');
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

    const nuevaLista: ListaPublica = {
      idLista: `lista-${Date.now()}`, // ID temporal
      nombre: this.listaForm.value.nombre,
      descripcion: this.listaForm.value.descripcion || '',
      idCreadorUsuario: this.actualUser.idUsuario,
      contenidos: contenidosSeleccionados,
      visibilidad: true
    };

    this.contentService.createPublicList(nuevaLista).subscribe({
      next: () => {
        this.notificationService.success('Tu lista pública ha sido creada correctamente.', 'Lista creada');
        this.router.navigate([GESTOR_ROUTES.listas]);
      },
      error: (error) => {
        console.error('Error al crear la lista pública:', error);
        this.notificationService.error('No se pudo crear la lista. Inténtalo de nuevo.');
      }
    });
  }

  private confirmarEdicion(): void {
    if (!this.listaOriginal || !this.listaId) return;

    const contenidosActuales = new Set(this.selectedContentIds);
    const contenidosOriginales = this.contenidosOriginales;

    const contenidosAñadidos = Array.from(contenidosActuales).filter(id => !contenidosOriginales.has(id));
    const contenidosEliminados = Array.from(contenidosOriginales).filter(id => !contenidosActuales.has(id));

    const listaEditada: ListaPublica = {
      ...this.listaOriginal,
      nombre: this.listaForm.value.nombre,
      descripcion: this.listaForm.value.descripcion || ''
    };

    this.contentService.editPublicList(listaEditada).subscribe({
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
      };
      operaciones.push(this.contentService.addContentToPublicList(dataAdd));
    }

    if (eliminados.length > 0) {
      const dataRemove = {
        idLista: this.listaId!,
        idsContenido: eliminados,
      };
      operaciones.push(this.contentService.removeContentFromPublicList(dataRemove));
    }

    if (operaciones.length > 0) {
      Promise.all(operaciones.map(op => op.toPromise())).then(
        () => {
          this.notificationService.success('La lista ha sido editada correctamente.', 'Lista actualizada');
          this.router.navigate([GESTOR_ROUTES.listas]);
        },
        (error) => {
          console.error('Error al gestionar contenidos:', error);
          this.notificationService.warning('Lista editada, pero hubo errores al gestionar algunos contenidos.', 'Advertencia');
          this.router.navigate([GESTOR_ROUTES.listas]);
        }
      );
    } else {
      this.notificationService.success('La lista ha sido editada correctamente.', 'Lista actualizada');
      this.router.navigate([GESTOR_ROUTES.listas]);
    }
  }

  cancelarCreacion(): void {
    if (this.isCreationMode) {
      if (this.isEditMode) {
        this.router.navigate([GESTOR_ROUTES.listas], { queryParams: { tipo: 'publicas' } });
      } else {
        this.isCreationMode = false;
        this.selectedContentIds.clear();
      }
    } else {
      this.listaForm.reset();
      this.router.navigate([GESTOR_ROUTES.inicio]);
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
      this.router.navigate([GESTOR_ROUTES.inicio]);
    } else if (section === 'mis-contenidos') {
      this.router.navigate([GESTOR_ROUTES.subirContenido]);
    } else if (section === 'mis-listas') {
      this.router.navigate([GESTOR_ROUTES.listas]);
    }
  }

  isMenuSectionActive(section: string): boolean {
    return this.activeMenuSection === section;
  }

  // =============================================================================
  // AUXILIARES
  // =============================================================================

  logout(): void {
    this.sharedService.performLogout(this.router, this.APP_ROUTES.home);
  }

  getStarsArray(rating: number): number[] {
    return this.sharedService.getStarsArray(rating);
  }

  secondsToMinutesSeconds(seconds: number): string {
    return this.sharedService.secondsToMinutesSeconds(seconds);
  }

  loadFavorites(): void {
    return;
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
