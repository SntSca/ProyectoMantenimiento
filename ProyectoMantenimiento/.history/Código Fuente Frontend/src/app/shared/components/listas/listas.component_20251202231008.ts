import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormGroup } from '@angular/forms';
import { Subject } from 'rxjs';
import { 
  APP_ROUTES, 
  USUARIO_ROUTES,
  GESTOR_ROUTES,
  SharedService,
  ContentService,
  FormValidationService,
  UserStatus,
  GestorStatus,
  AdministradorStatus,
  ListaPublica,
  ListaPrivada,
  Contenido,
  Videos,
} from '@shared';
import { UsuarioService } from '../../../usuario/usuario.service';
import { NotificationService } from '../../services/notification.service';
import { ConfirmationService } from '../../services/confirmation.service';


@Component({
  selector: 'app-listas',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './listas.component.html'
})
export class ListasComponent implements OnInit, OnDestroy {
  
  // Inputs para personalización
  @Input() esGestor = false;
  @Input() esAdministrador = false;
  @Input() tituloSeccion = 'Mis Listas';
  @Input() subtituloSeccion = 'Organiza y gestiona tus listas de reproducción';
  @Input() ROUTES: any = {};
  @Input() tipoLista: 'publicas' | 'privadas' = 'privadas'; // Tipo de lista a mostrar

  // Usuario actual (puede ser usuario, gestor o administrador)
  actualUser: UserStatus | GestorStatus | AdministradorStatus = {} as UserStatus;
  public readonly APP_ROUTES = APP_ROUTES;

  // Datos de listas
  listas: (ListaPrivada | ListaPublica)[] = [];
  listasFiltradas: ListaPrivada[] = [];
  listaEditada: ListaPrivada | ListaPublica | null = null;  
  
  // Estado de búsqueda
  searchTerm: string = '';
  private readonly searchSubject = new Subject<string>();
  private readonly destroy$ = new Subject<void>();
  
  // Estado de expansión de listas
  private readonly expandedListas: Set<string> = new Set();

  // Estado de edición de listas
  editingListaId: string | null = null;
  editingVisibilidad: boolean = false;
  editForm: FormGroup | null = null;
  submitted: boolean = false;

  constructor(
    private readonly router: Router,
    private readonly sharedService: SharedService,
    private readonly contentService: ContentService,
    private readonly validationService: FormValidationService,
    private readonly usuarioService: UsuarioService,
    private readonly notificationService: NotificationService,
    private readonly confirmationService: ConfirmationService
  ) {}

  ngOnInit(): void {
    // Obtener usuario actual según el rol
    if (this.esGestor && sessionStorage.getItem('gestor_data')) {
      this.actualUser = JSON.parse(sessionStorage.getItem('gestor_data')!);
    } else if (this.esAdministrador && sessionStorage.getItem('admin_data')) {
      this.actualUser = JSON.parse(sessionStorage.getItem('admin_data')!);
    } else if (sessionStorage.getItem('user_data')) {
      this.actualUser = JSON.parse(sessionStorage.getItem('user_data')!);
    }

    // Cargar listas
    this.loadListas();

    // Configurar búsqueda dinámica con debounce
    this.sharedService.setupDynamicSearch(this.searchSubject, (searchTerm) => {
      this.searchTerm = searchTerm;
      this.buscar();
    }, this.destroy$);
  }

  // =============================================================================
  // MÉTODOS DE CARGA DE DATOS
  // =============================================================================

  private loadListas(): void {
    // Cargar según el tipo de lista especificado
    if (this.tipoLista === 'privadas') {
      this.contentService.getPrivateLists().subscribe(listas => {
        
        this.listas = listas;
        this.listasFiltradas = [...this.listas];
      });
    } else {
      this.contentService.getPublicLists().subscribe(listas => {
        
        this.listas = listas;
        this.listasFiltradas = [...this.listas];
      });
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.searchSubject.complete();
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




  // =============================================================================
  // MÉTODOS DE BÚSQUEDA Y FILTRADO
  // =============================================================================

  buscar(): void {
    if (!this.searchTerm.trim()) {
      this.listasFiltradas = [...this.listas];
      return;
    }

    const searchLower = this.searchTerm.toLowerCase().trim();
    this.listasFiltradas = this.listas.filter(lista => 
      lista.nombre.toLowerCase().includes(searchLower) ||
      lista.descripcion.toLowerCase().includes(searchLower)
    );
  }

  restablecerBusqueda(): void {
    this.searchTermValue = '';
    this.listasFiltradas = [...this.listas];
  }

  // =============================================================================
  // MÉTODOS DE EXPANSIÓN/COLAPSO
  // =============================================================================

  toggleLista(idLista: string): void {
    if (this.expandedListas.has(idLista)) {
      this.expandedListas.delete(idLista);
    } else {
      this.expandedListas.add(idLista);
    }
  }

  isListaExpanded(idLista: string): boolean {
    return this.expandedListas.has(idLista);
  }

  // =============================================================================
  // MÉTODOS AUXILIARES
  // =============================================================================

  getStarsArray(rating: number): number[] {
    return this.sharedService.getStarsArray(rating);
  }

  logout(): void {
    this.sharedService.performLogout(this.router, this.APP_ROUTES.home);
  }

  getResolucion(contenido: Contenido): string {
    if (contenido.tipo === 'VIDEO') {
        const resolucion = (contenido as Videos).resolucion;
        return resolucion === '2160' ? '4K' : resolucion || 'N/A';
    }
    return '';
  }

  // =============================================================================
  // MÉTODOS PARA CAMBIO DE MODO (USUARIO)
  // =============================================================================

  cambiarModoListas(modo: 'publicas' | 'privadas'): void {
    this.tipoLista = modo;
    this.restablecerBusqueda();
    this.loadListas();
  }

  // Verifica si es usuario (no gestor ni administrador) para mostrar el selector
  get esUsuario(): boolean {
    return !this.esGestor && !this.esAdministrador;
  }

  // =============================================================================
  // MÉTODOS DE NAVEGACIÓN
  // =============================================================================

  visualizarContenido(contenido: Contenido): void {
    if (!this.esUsuario) return;
  
    this.router.navigate([USUARIO_ROUTES.visualizar, contenido.tipo, contenido.id]);
  }

  onContenidoKeydown(event: KeyboardEvent, contenido: Contenido): void {
    if (!this.esUsuario) return;
    
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      this.visualizarContenido(contenido);
    }
  }

  // =============================================================================
  // MÉTODOS DE EDICIÓN DE LISTAS
  // =============================================================================

  iniciarEdicion(lista: ListaPrivada | ListaPublica): void {
    // Desexpandir la lista si está expandida
    this.expandedListas.delete(lista.idLista);
    
    this.editingListaId = lista.idLista;
    this.editingVisibilidad = (lista as ListaPublica).visibilidad || false;
    this.submitted = false; // Resetear el estado de enviado
    
    // Crear el formulario con los valores actuales
    this.editForm = this.validationService.createListaForm();
    this.editForm.patchValue({
      nombre: lista.nombre,
      descripcion: lista.descripcion
    });
  }

  cancelarEdicion(): void {
    this.editingListaId = null;
    this.editingVisibilidad = false;
    this.editForm = null;
    this.submitted = false;
  }

  confirmarCambios(): void {
    if (!this.editingListaId || !this.editForm) {
      return;
    }

    // Marcar como enviado para mostrar errores de validación
    this.submitted = true;

    // Validar formulario antes de enviar
    if (!this.validationService.validateFormBeforeSubmit(this.editForm)) {
      
      return;
    }

    // Buscar la lista original para mantener sus propiedades
    const listaOriginal = this.listas.find(lista => lista.idLista === this.editingListaId);
    if (!listaOriginal) {
      console.error('No se encontró la lista original');
      return;
    }

    const formValues = this.editForm.value;

    if (this.tipoLista === 'privadas') {
      this.listaEditada = {
        ...listaOriginal,
        nombre: formValues.nombre.trim(),
        descripcion: formValues.descripcion.trim()
      } as ListaPrivada;

      this.contentService.editPrivateList(this.listaEditada).subscribe({
        next: () => {
          
          this.loadListas();
          this.cancelarEdicion();
        },
        error: (error: any) => {
          console.error('Error al editar lista privada:', error);
        }
      });
    } else {
      this.listaEditada = {
        ...listaOriginal,
        nombre: formValues.nombre.trim(),
        descripcion: formValues.descripcion.trim(),
        visibilidad: this.editingVisibilidad
      } as ListaPublica;

      this.contentService.editPublicList(this.listaEditada as ListaPublica).subscribe({
        next: () => {
          
          this.loadListas();
          this.cancelarEdicion();
        },
        error: (error: any) => {
          console.error('Error al editar lista pública:', error);
        }
      });
    }
  }

  isEditing(listaId: string): boolean {
    return this.editingListaId === listaId;
  }

  isFieldInvalid(fieldName: string): boolean {
    if (!this.editForm) return false;
    return this.sharedService.isFieldInvalid(this.editForm, fieldName, this.submitted);
  }

  getFieldError(fieldName: string): string {
    if (!this.editForm) return '';
    return this.sharedService.getFieldError(this.editForm, fieldName);
  }

  isListaHidden(lista: ListaPrivada | ListaPublica): boolean {
    // Solo para gestores y administradores viendo listas públicas
    if (!(this.esGestor || this.esAdministrador) || this.tipoLista !== 'publicas') {
      return false;
    }
    
    // Cast a ListaPublica para acceder a visibilidad
    const listaPublica = lista as ListaPublica;
    return !listaPublica.visibilidad;
  }

  isContenidoHidden(contenido: Contenido): boolean {
    // Solo para gestores y administradores
    if (!(this.esGestor || this.esAdministrador)) {
      return false;
    }
    
    return !contenido.visibilidad;
  }

  // =============================================================================
  // MÉTODOS DE NAVEGACIÓN PARA EDITAR CONTENIDOS
  // =============================================================================

  modificarContenidos(lista: ListaPrivada | ListaPublica): void {
    if (this.tipoLista === 'privadas') {
      // Navegar a editar lista privada
      
      this.router.navigate([USUARIO_ROUTES.editarListaPrivada, lista.idLista]);
    } else {
      // Navegar a editar lista pública 
      
      this.router.navigate([GESTOR_ROUTES.editarListaPublica, lista.idLista]);
    }
  }

  // =============================================================================
  // MÉTODOS DE ELIMINACIÓN DE LISTAS
  // =============================================================================

  async eliminarLista(lista: ListaPrivada | ListaPublica): Promise<void> {
    const nombreLista = lista.nombre;
    const confirmacion = await this.confirmationService.confirmDelete(nombreLista);
    
    if (!confirmacion) {
      return;
    }

    if (this.tipoLista === 'privadas') {
      this.eliminarListaPrivada(lista.idLista, nombreLista);
    } else {
      this.eliminarListaPublica(lista.idLista, nombreLista);
    }
  }

  private eliminarListaPrivada(idLista: string, nombreLista: string): void {
    this.contentService.deletePrivateList(idLista).subscribe({
      next: () => {
        this.notificationService.success(`La lista "${nombreLista}" ha sido eliminada.`, 'Lista eliminada');
        this.loadListas(); // Recargar las listas
      },
      error: (error) => {
        console.error('Error al eliminar lista privada:', error);
        this.notificationService.error('No se pudo eliminar la lista. Inténtalo de nuevo.');
      }
    });
  }

  private eliminarListaPublica(idLista: string, nombreLista: string): void {
    this.contentService.deletePublicList(idLista).subscribe({
      next: () => {
        this.notificationService.success(`La lista "${nombreLista}" ha sido eliminada.`, 'Lista eliminada');
        this.loadListas(); // Recargar las listas
      },
      error: (error) => {
        console.error('Error al eliminar lista pública:', error);
        this.notificationService.error('No se pudo eliminar la lista. Inténtalo de nuevo.');
      }
    });
  }

  // =============================================================================
  // MÉTODOS DE NAVEGACIÓN ESPECIALES
  // =============================================================================

  /**
   * Carga los contenidos favoritos del usuario (solo para usuarios)
   * Navega a la página de inicio con query parameter para cargar favoritos
   */
  loadFavorites(): void {
    if (!this.esUsuario) return;

    // Navegar a inicio con query parameter para activar favoritos
    this.router.navigate([USUARIO_ROUTES.inicio], { 
      queryParams: { section: 'favoritos' }
    });
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
onKeyPress(event: KeyboardEvent) {
  console.log('Key press:', event.key);
}
}
