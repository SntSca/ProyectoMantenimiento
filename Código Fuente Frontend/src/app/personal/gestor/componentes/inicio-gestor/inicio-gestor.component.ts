import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { FormsModule, ReactiveFormsModule, FormGroup } from '@angular/forms';
import { Router, RouterModule} from '@angular/router';
import { CommonModule } from '@angular/common';
import { APP_ROUTES, GESTOR_ROUTES, GestorStatus, Contenido, Videos, Audios , SharedService, ContentService, MOCK_GESTORES, Tags, FilterService, FormValidationService} from '@shared';
import { Subject } from 'rxjs';
import { NotificationService } from '../../../../shared/services/notification.service';
import { ConfirmationService } from '../../../../shared/services/confirmation.service';

@Component({
  selector: 'app-inicio-gestor',
  standalone: true,
  imports: [RouterModule, FormsModule, ReactiveFormsModule, CommonModule],
  templateUrl: './inicio-gestor.component.html',
})

export class InicioGestorComponent implements OnInit, OnDestroy {

  actualGestor: GestorStatus = {} as GestorStatus;

  searchTerm: string = '';
  filtroTagsContenidos: string[] = [];
  isTipoContenidoContenidos: boolean | null = null;
  filtroTipoContenidoContenidos: string | null = null;
  filtroVisibilidadContenidos: boolean | null = null;
  filtroRestriccionEdad: number | null = null;

  // Subject para búsqueda dinámica
  private readonly searchSubject = new Subject<string>();
  private readonly destroy$ = new Subject<void>();

  // Modo edición
  isEditing: boolean = false;
  editingContent: Contenido | null = null;
  updateContenidoForm!: FormGroup;
  isTagsDropdownOpen: boolean = false;

  videos: Videos[] = [];
  audios: Audios[] = [];
  tags: Tags[] = [];
  contenidos: Contenido[] = [];
  contenidosFiltrados: Contenido[] = [];


  // Estados de carga
  loadingContent = false;

  public readonly GESTOR_ROUTES = GESTOR_ROUTES;
  public readonly APP_ROUTES = APP_ROUTES;

  isListView: boolean = false;
  showTagFilters: boolean = false;

  constructor(
    private readonly router: Router,
    private readonly contentService: ContentService,
    private readonly sharedService: SharedService,
    private readonly filterService: FilterService,
    private readonly formValidationService: FormValidationService,
    private readonly notificationService: NotificationService,
    private readonly confirmationService: ConfirmationService
  ) {}

  // =============================================================================
  // MÉTODOS DE INICIALIZACIÓN
  // =============================================================================

  ngOnInit(): void {

    
    
    if (localStorage.getItem('gestor_data')) {
      this.actualGestor = JSON.parse(localStorage.getItem('gestor_data')!);
    }
    else{
      this.actualGestor = MOCK_GESTORES[0];
    }
   
    localStorage.setItem('actualGestor', JSON.stringify(this.actualGestor));

    this.loadContent();
  
    this.actualGestor.fotoPerfil ??= 'assets/porDefecto.jpg';
    this.updateContenidoForm = this.formValidationService.updateContenidoForm();

    this.sharedService.getTags().subscribe((tags: any[]) => {
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

  // =============================================================================
  // MÉTODOS DEL SERVICIO
  // =============================================================================
  onSearchInputChange(searchTerm: string): void {
    this.sharedService.handleSearchInput(this.searchSubject, searchTerm);
  }

  // Getter y setter para searchTerm con búsqueda dinámica
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
    this.contenidosFiltrados = this.filterService.aplicarFiltros(this.contenidos, {
      filtroTagsContenidos: this.filtroTagsContenidos,
      filtroTipoContenidoContenidos: this.filtroTipoContenidoContenidos,
      filtroVisibilidadContenidos: this.filtroVisibilidadContenidos,
      filtroRestriccionEdad: this.filtroRestriccionEdad
    });
    this.contenidosFiltrados = this.filterService.filtrarPorBusqueda(this.contenidosFiltrados, this.searchTerm, 'contenidos');
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

  limpiarFiltrosTags(): void {
    this.filtroTagsContenidos = [];
    this.aplicarFiltros();
    this.searchTermValue = '';
  }

  isTagActive(tagNombre: string): boolean {
    return this.filtroTagsContenidos.includes(tagNombre);
  }

  filtrarPorTipoContenidoContenidos(tipo: string | null): void {
    this.filtroTipoContenidoContenidos = tipo;
    this.aplicarFiltros();
    this.searchTermValue = '';
  }

  toggleVisibilidadContenidos(): void {
    let nuevoEstado: boolean | null;
    // Calcular el siguiente estado
    if (this.filtroVisibilidadContenidos === null) {
      nuevoEstado = true;  // de "todos" a "visibles"
    } else if (this.filtroVisibilidadContenidos === true) {
      nuevoEstado = false; // de "visibles" a "no visibles"
    } else {
      nuevoEstado = null;  // de "no visibles" a "todos"
    }
    
    this.filtroVisibilidadContenidos = nuevoEstado;
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

  // =============================================================================
  // MÉTODOS DE VISTA
  // =============================================================================

  toggleView(): void {
    this.isListView = this.sharedService.toggleView(this.isListView);
  }

  toggleTagFilters(): void {
    this.showTagFilters = !this.showTagFilters;
  }

  restablecerFiltros(): void {
    // Resetear todos los filtros
    this.filtroTagsContenidos = [];
    this.filtroTipoContenidoContenidos = null;
    this.isTipoContenidoContenidos = null;
    this.filtroVisibilidadContenidos = null;
    this.filtroRestriccionEdad = null;
    this.searchTermValue = '';
    this.isListView = false;
    
    // Cerrar contenedor de filtros desplegables
    this.showTagFilters = false;
    
    // Recargar contenido sin filtros
    this.aplicarFiltros();
  }

  // =============================================================================
  // MÉTODOS DE CARGA DE DATOS
  // =============================================================================


  private loadContent(): void {
    this.loadingContent = true;

    this.sharedService.loadContent(this.contentService).subscribe({
      next: (result) => {
        this.contenidos = result.contenidos;
        this.contenidosFiltrados = result.contenidosFiltrados;
        this.loadingContent = false;
        
      },
      error: (error) => {
        console.error('Error al cargar contenido:', error);
        this.loadingContent = false;
      }
    });
  }

  public refreshContent(): void {
    this.loadContent();
  }

  secondsToMinutesSeconds(seconds: number): string {
    return this.sharedService.secondsToMinutesSeconds(seconds);
  }

  logout(): void {
    this.sharedService.performLogout(this.router, this.APP_ROUTES.home);
  }

  // =============================================================================
  // MÉTODOS DE EDICIÓN
  // =============================================================================

  editContent(contenido: Contenido): void {
    // Verificar que el tipo de contenido coincida con el tipo del gestor actual
    if (contenido.tipo !== this.actualGestor.tipoContenido) {
      this.notificationService.warning(`Solo puedes editar contenidos de tipo ${this.actualGestor.tipoContenido.toLowerCase()}`);
      return;
    }

    this.editingContent = contenido;
    this.isEditing = true;

    // Llenar el formulario con los datos actuales
    this.updateContenidoForm.patchValue({
      titulo: contenido.titulo,
      descripcion: contenido.descripcion,
      tags: contenido.tags,
      vip: contenido.esVIP,
      fechaExpiracion: contenido.fechaExpiracion ? new Date(contenido.fechaExpiracion).toISOString().split('T')[0] : '',
      pegi: contenido.restriccionEdad,
      visibilidad: contenido.visibilidad
    });
  }

  cancelEdit(): void {
    this.isEditing = false;
    this.editingContent = null;
    this.updateContenidoForm.reset();
  }

  saveEdit(): void {
    if (!this.formValidationService.validateFormBeforeSubmit(this.updateContenidoForm) || !this.editingContent) {
      return;
    }

    const formData = this.updateContenidoForm.value;
    const updatedContent = {
      ...this.editingContent,
      titulo: formData.titulo,
      descripcion: formData.descripcion,
      tags: formData.tags,
      esVIP: formData.vip,
      fechaExpiracion: formData.fechaExpiracion || null,
      restriccionEdad: formData.pegi,
      visibilidad: formData.visibilidad
    };
    

    
    if (this.editingContent.tipo === 'VIDEO' && this.editingContent.id) {
      this.contentService.updateVideo(updatedContent, this.editingContent.id).subscribe({
        next: (response) => {
          this.notificationService.success('El contenido ha sido actualizado correctamente.', 'Contenido actualizado');
          this.cancelEdit();
          this.refreshContent();
        },
        error: (error) => {
          this.notificationService.error('No se pudo actualizar el contenido. Inténtalo de nuevo.');
          console.error('Error updating content:', error);
        }
       });
      } 
      else if (this.editingContent.tipo === 'AUDIO' && this.editingContent.id) {
        this.contentService.updateAudio(updatedContent, this.editingContent.id).subscribe({
          next: (response) => {
            this.notificationService.success('El contenido ha sido actualizado correctamente.', 'Contenido actualizado');
            this.cancelEdit();
            this.refreshContent();
          },
          error: (error) => {
            this.notificationService.error('No se pudo actualizar el contenido. Inténtalo de nuevo.');
            console.error('Error updating content:', error);
          }
        });
      }
   }

  // =============================================================================
  // MÉTODOS PARA MANEJAR TAGS EN EDICIÓN
  // =============================================================================

  toggleTagsDropdown(): void {
    this.isTagsDropdownOpen = this.sharedService.toggleTagsDropdown(this.isTagsDropdownOpen);
  }

  closeTagsDropdown(): void {
    this.isTagsDropdownOpen = false;
  }

  onTagChange(tag: Tags, checked: boolean): void {
    this.sharedService.onTagChange(tag, checked, this.updateContenidoForm);
  }

  isTagSelected(tag: Tags): boolean {
    return this.sharedService.isTagSelected(tag, this.updateContenidoForm);
  }

  getSelectedTagsText(): string {
    return this.sharedService.getSelectedTagsText(this.updateContenidoForm);
  }

  // =============================================================================
  // MÉTODOS DE VALIDACIÓN PARA EDICIÓN
  // =============================================================================

  isFieldInvalid(fieldName: string): boolean {
    return this.sharedService.isFieldInvalid(this.updateContenidoForm, fieldName);
  }

  getFieldError(fieldName: string): string {
    return this.sharedService.getFieldError(this.updateContenidoForm, fieldName);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.custom-dropdown')) {
      this.closeTagsDropdown();
    }
  }

  getStarsArray(rating: number): number[] {
    return this.sharedService.getStarsArray(rating);
  }

  getResolucion(contenido: Contenido): string {
    if (contenido.tipo === 'VIDEO') {
      const resolucion = (contenido as Videos).resolucion;
      return resolucion === '2160' ? '4K' : resolucion || 'N/A';
    }
    return '';
  }

  // =============================================================================
  // MÉTODO PARA ELIMINAR CONTENIDO
  // =============================================================================

  public async deleteContent(event: Event, contenido: Contenido): Promise<void> {
    event.stopPropagation(); // Evitar que se ejecute editContent()
    
    if (!contenido.id) return;

    const confirmDelete = await this.confirmationService.confirmDelete(contenido.titulo);
    if (!confirmDelete) return;

    if (contenido.tipo === 'VIDEO') {
      
      this.contentService.deleteVideo(contenido.id).subscribe({
        next: () => {
          this.notificationService.success('El contenido ha sido eliminado correctamente.', 'Contenido eliminado');
          // Quitar el contenido de la lista mostrada
          this.contenidos = this.contenidos.filter(c => c.id !== contenido.id);
          this.contenidosFiltrados = this.contenidosFiltrados.filter(c => c.id !== contenido.id);
        },
        error: (error) => {
          console.error('Error al eliminar video:', error);
          this.notificationService.error('No se pudo eliminar el contenido. Inténtalo de nuevo.');
        }
      });
    } else if (contenido.tipo === 'AUDIO') {
      this.contentService.deleteAudio(contenido.id).subscribe({
        next: () => {
          this.notificationService.success('El contenido ha sido eliminado correctamente.', 'Contenido eliminado');
          // Quitar el contenido de la lista mostrada
          this.contenidos = this.contenidos.filter(c => c.id !== contenido.id);
          this.contenidosFiltrados = this.contenidosFiltrados.filter(c => c.id !== contenido.id);
        },
        error: (error) => {
          console.error('Error al eliminar audio:', error);
          this.notificationService.error('No se pudo eliminar el contenido. Inténtalo de nuevo.');
        }
      });
    }
  }
}

