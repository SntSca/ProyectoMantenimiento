import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subject, forkJoin } from 'rxjs';
import Swal from 'sweetalert2';

import {
  APP_ROUTES,
  USUARIO_ROUTES,
  Videos,
  Audios,
  Contenido,
  ContentService,
  SharedService,
  Tags,
  UserStatus,
  FilterService
} from '@shared';
import { UsuarioService } from '../../usuario.service';

// 👇 Toasts (info, warning, etc.)
import { NotificationService as UiNotificationService } from '../../../shared/services/notification.service';

// 👇 Buzón de notificaciones (backend)
import {
  NotificationsService,
  Notification as UserNotification
} from '../../../shared/services/notifications.service';

@Component({
  selector: 'app-inicio-usuario',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './inicio-usuario.component.html',
  styles: [`
    .notification-bell-container {
      position: relative;
      cursor: pointer;
      display: inline-block;
    }

    .bell-icon {
      font-size: 1.5rem;
    }

    .badge {
      position: absolute;
      top: -5px;
      right: -5px;
      background-color: #ef4444;
      color: white;
      border-radius: 50%;
      padding: 0.15rem 0.4rem;
      font-size: 0.75rem;
      font-weight: bold;
    }

    .notifications-dropdown {
      position: absolute;
      right: 0;
      top: 2rem;
      width: 300px;
      max-height: 400px;
      overflow-y: auto;
      background: white;
      border: 1px solid #ddd;
      border-radius: 0.5rem;
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
      padding: 0.5rem;
      z-index: 100;
    }

    /* resto de tu CSS... */
  `]
})
export class InicioUsuarioComponent implements OnInit, OnDestroy {

  // Usuario actual (cabecera)
  actualUser: UserStatus = {} as UserStatus;

  // Id del usuario actual (obtenido del JWT)
  private userId!: string;

  // Búsqueda / filtros
  searchQuery: string = '';
  searchTerm: string = '';
  filtroTagsContenidos: string[] = [];
  isTipoContenidoContenidos: boolean | null = null;
  filtroTipoContenidoContenidos: string | null = null;
  filtroRestriccionEdad: number | null = null;
  showTagFilters: boolean = false;

  private readonly searchSubject = new Subject<string>();
  private readonly destroy$ = new Subject<void>();

  // Datos
  tags: Tags[] = [];
  videos: Videos[] = [];
  audios: Audios[] = [];
  contenidos: Contenido[] = [];
  contenidosFiltrados: Contenido[] = [];
  contenidosBase: Contenido[] = []; // Base de filtrado (todos o favoritos)

  // Estados de carga
  loadingVideos = false;
  loadingAudios = false;
  loadingContent = false;
  showingFavorites = false;

  // Menú lateral
  activeMenuSection: string = 'inicio';

  public readonly USUARIO_ROUTES = USUARIO_ROUTES;
  public readonly APP_ROUTES = APP_ROUTES;

  isListView: boolean = false;

  // ====== BUZÓN DE NOTIFICACIONES ======
  notifications: UserNotification[] = [];
  loadingNotis = false;
  notisError?: string;

  // Flag para mostrar el modal
  showNotisForm: boolean = false;

  // Dropdown de notificaciones
  showNotisDropdown: boolean = false;
  unreadCount = 0;

  // ⚡ Para evitar mostrar varias veces la misma alerta de contenido
  private shownAlertIds = new Set<string>();

  needsGustosSelection: boolean = false;

  constructor(
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly contentService: ContentService,
    private readonly sharedService: SharedService,
    private readonly filterService: FilterService,
    private readonly usuarioService: UsuarioService,
    // toasts
    private readonly uiNotificationService: UiNotificationService,
    // backend notificaciones
    private readonly notificationsService: NotificationsService
  ) {}

  // =============================================================================
  // CICLO DE VIDA
  // =============================================================================

  ngOnInit(): void {
    this.initCurrentUserFromJwt();

    // 1️⃣ Cargar todas las notificaciones al iniciar
    this.loadNotifications();

    this.route.queryParams.subscribe(params => {
      if (params['section'] === 'favoritos') {
        this.loadFavorites();
      } else {
        this.loadContent();
      }
    });

    // 2️⃣ Cargar tags disponibles
    this.sharedService.getTags().subscribe(tags => this.tags = tags);

    // 3️⃣ Configurar búsqueda dinámica con debounce
    this.sharedService.setupDynamicSearch(
      this.searchSubject,
      (searchTerm) => {
        this.searchTerm = searchTerm;
        this.aplicarFiltros();
      },
      this.destroy$
    );
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
        console.warn('[InicioUsuario] No se han podido obtener credenciales del JWT');
        this.router.navigate([this.APP_ROUTES.home]);
        return;
      }

      this.userId = credentials.userId;

      // ⚡ Traer usuario y gustos en paralelo
      forkJoin({
        usuario: this.usuarioService.getUsuario(this.userId),
        gustos: this.usuarioService.getGustos()
      }).subscribe({
        next: ({ usuario, gustos }) => {
          // Asignamos datos básicos + gustos
          this.actualUser = {
            idUsuario: usuario.idUsuario,
            tipo: 'usuario',
            nombre: usuario.nombre,
            primerApellido: usuario.apellidos?.split(' ')[0] || '',
            segundoApellido: usuario.apellidos?.split(' ')[1] || '',
            email: usuario.email,
            alias: usuario.alias,
            fechaNacimiento: usuario.fechaNacimiento,
            flagVIP: usuario.flagVIP,
            fotoPerfil: usuario.fotoPerfil ?? 'assets/porDefecto.jpg',
            bloqueado: usuario.bloqueado,
            twoFactorEnabled: usuario.twoFactorEnabled,
            thirdFactorEnabled: usuario.thirdFactorEnabled,
            gustos: gustos || []
          } as UserStatus;

          if (this.actualUser.gustos?.length === 0) {
            this.uiNotificationService.warning(
              '⚠️ Aún no has seleccionado tus gustos. ¡Selecciona tus gustos para recibir notificaciones personalizadas!',
              'Gustos no seleccionados'
            );
          }
        },
        error: (err) => {
          console.error('[InicioUsuario] Error cargando usuario o gustos:', err);
        }
      });

    } catch (e) {
      console.error('[InicioUsuario] Error al obtener credenciales del JWT:', e);
      this.router.navigate([this.APP_ROUTES.home]);
      return;
    }

    // ⚠️ ALERTAS DE CONTENIDO POR CADUCAR (evitar repetidos + duración 30s)
    this.usuarioService.getExpiringAlerts().subscribe({
      next: (alerts) => {
        if (!alerts || alerts.length === 0) {
          return;
        }

        alerts
          .filter(alert => !!alert.id && !this.shownAlertIds.has(alert.id))
          .forEach(alert => {
            if (alert.id) {
              this.shownAlertIds.add(alert.id);
            }

            this.uiNotificationService.warning(
              `⏳ ${alert.body}`,
              alert.title || 'Alerta de contenido'
            );
          });
      },
      error: (err) => {
        console.error('[InicioUsuario] Error cargando alertas de contenido', err);
      }
    });
  }

  // =============================================================================
  // BÚSQUEDA Y FILTROS
  // =============================================================================

  onSearchInputChange(searchTerm: string): void {
    this.sharedService.handleSearchInput(this.searchSubject, searchTerm);
  }

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

  // Función para alternar el modal
  toggleNotisForm(): void {
    this.showNotisForm = !this.showNotisForm;
  }

  aplicarFiltros(): void {
    const baseParaFiltrar = this.showingFavorites ? this.contenidosBase : this.contenidos;

    this.contenidosFiltrados = [
      ...this.filterService.aplicarFiltros(baseParaFiltrar, {
        filtroTagsContenidos: this.filtroTagsContenidos,
        filtroTipoContenidoContenidos: this.filtroTipoContenidoContenidos,
        filtroRestriccionEdad: this.filtroRestriccionEdad
      })
    ];

    this.contenidosFiltrados = [
      ...this.filterService.filtrarPorBusqueda(this.contenidosFiltrados, this.searchTerm, 'contenidos')
    ];
  }

  filtrarPorTags(tag: string): void {
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

  secondsToMinutesSeconds(seconds: number): string {
    return this.sharedService.secondsToMinutesSeconds(seconds);
  }

  // =============================================================================
  // VISTA / NAVEGACIÓN DE CONTENIDO
  // =============================================================================

  toggleView(): void {
    this.isListView = this.sharedService.toggleView(this.isListView);
  }

  verContenido(contenido: Contenido): void {
    if (contenido.esVIP && !this.actualUser.flagVIP) {
      this.uiNotificationService.info('¡Hazte VIP para acceder a contenido exclusivo!', 'Contenido VIP');
      return;
    }

    const edad = this.sharedService.calculateAge(this.actualUser.fechaNacimiento);
    if (edad < contenido.restriccionEdad) {
      this.uiNotificationService.warning('No tienes la edad suficiente para ver este contenido.', 'Acceso restringido');
      return;
    }

    this.router.navigate(
      [USUARIO_ROUTES.visualizar, contenido.tipo, contenido.id],
      { state: { fromNavigation: true } }
    );
  }

  // =============================================================================
  // CARGA DE DATOS
  // =============================================================================

  private loadContent(): void {
    this.loadingContent = true;

    this.sharedService.loadContent(this.contentService).subscribe({
      next: (result) => {
        this.contenidos = result.contenidos;
        this.contenidosBase = [...result.contenidos];
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
    this.contenidosBase = [];
    this.loadContent();
    this.loadNotifications(); // refrescamos también el buzón al volver a inicio
  }

  // =============================================================================
  // MENÚ LATERAL
  // =============================================================================

  public setActiveMenuSection(section: string): void {
    this.activeMenuSection = section;

    if (section === 'inicio') {
      this.refreshContent();
    }

    if (section === 'listas-publicas') {
      this.router.navigate([USUARIO_ROUTES.listas], { queryParams: { tipo: 'publicas' } });
    }

    if (section === 'listas-privadas') {
      this.router.navigate([USUARIO_ROUTES.listas], { queryParams: { tipo: 'privadas' } });
    }
  }

  public isMenuSectionActive(section: string): boolean {
    return this.activeMenuSection === section;
  }

  // =============================================================================
  // FAVORITOS
  // =============================================================================

  public loadFavorites(): void {
    this.activeMenuSection = 'favoritos';
    this.loadingContent = true;
    this.showingFavorites = true;

    // Si no hay contenidos cargados, primero los cargamos
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

  private loadFavoriteIds(): void {
    this.contentService.getFavoriteContents().subscribe({
      next: (favoriteIds) => {
        const favoritos = this.contenidos.filter(contenido =>
          favoriteIds.includes(contenido.id!)
        );
        this.contenidosBase = [...favoritos];
        this.contenidosFiltrados = [...favoritos];
        this.loadingContent = false;
      },
      error: (error) => {
        console.error('Error al cargar favoritos:', error);
        this.loadingContent = false;
      }
    });
  }

  public removeFavorite(event: Event, contenido: Contenido): void {
    event.stopPropagation();

    if (!contenido.id) return;

    this.contentService.removeFavoriteContent(contenido.id).subscribe({
      next: () => {
        this.contenidosFiltrados = this.contenidosFiltrados.filter(c => c.id !== contenido.id);
      },
      error: (error) => {
        console.error('Error al eliminar de favoritos:', error);
      }
    });
  }

  // =============================================================================
  // BUZÓN DE NOTIFICACIONES
  // =============================================================================

  // Cargar notificaciones y actualizar contador de no leídas
  private loadNotifications(): void {
    this.loadingNotis = true;
    this.notisError = undefined;

    this.notificationsService.getInbox().subscribe({
      next: (list) => {
        this.notifications = list;
        this.updateUnreadCount();
        this.loadingNotis = false;
      },
      error: (err) => {
        console.error('[InicioUsuario] Error cargando notificaciones', err);
        this.notisError = 'Error cargando notificaciones';
        this.loadingNotis = false;
      }
    });
  }

  // Actualizar contador de no leídas
  private updateUnreadCount(): void {
    this.unreadCount = this.notifications.filter(n => !n.read).length;
  }

  // Cuando se marca una notificación como leída
  public markNotificationAsRead(notification: UserNotification): void {
    if (notification.read) return;

    this.notificationsService.markAsRead(notification.id).subscribe({
      next: () => {
        notification.read = true;
        this.updateUnreadCount(); // ⚡ actualizar contador
      },
      error: (err) => {
        console.error('[InicioUsuario] Error marcando notificación como leída', err);
      }
    });
  }

  // También cuando se elimina
  public deleteNotification(notification: UserNotification, event?: Event): void {
    if (event) event.stopPropagation();

    this.notificationsService.deleteNotification(notification.id).subscribe({
      next: () => {
        this.notifications = this.notifications.filter(n => n.id !== notification.id);
        this.updateUnreadCount(); // ⚡ actualizar contador
      },
      error: (err) => {
        console.error('[InicioUsuario] Error eliminando notificación', err);
      }
    });
  }

  // =============================================================================
  // SESIÓN / UTILIDADES
  // =============================================================================

  logout(): void {
    this.sharedService.performLogout(this.router, this.APP_ROUTES.home);
  }

  getStarsArray(rating: number): number[] {
    return this.sharedService.getStarsArray(rating);
  }

  toggleNotisDropdown(): void {
    this.showNotisDropdown = !this.showNotisDropdown;

    // Si abres el dropdown, puedes recalcular el contador de no leídos
    this.unreadCount = this.notifications.filter(n => !n.read).length;
  }

  openNotificationsModal(): void {
  const notis = this.notifications;

  const html = notis.length
    ? notis.map(n => {
        const isRead = !!n.read;

        const markBtnHtml = isRead
          ? `<button disabled style="
                margin-right:5px;
                background:#888;
                color:white;
                border:none;
                padding:5px 10px;
                border-radius:4px;
                cursor:default;
                transition:0.2s;
             ">Leída</button>`
          : `<button class="mark-read-btn" data-id="${n.id}" style="
                margin-right:5px;
                background:#4CAF50;
                color:white;
                border:none;
                padding:5px 10px;
                border-radius:4px;
                cursor:pointer;
                transition:0.2s;
             ">Marcar como leída</button>`;

        return `
        <div id="noti-${n.id}" style="
          padding:12px;
          border-bottom:1px solid #eee;
          background:${isRead ? '#f9f9f9' : '#fff7e6'};
          border-radius:6px;
          margin-bottom:8px;
          box-shadow:0 1px 3px rgba(0,0,0,0.05);
        ">
          <b style="font-size:14px;">${n.type === 'ALERT' ? '⚠️ ' : ''}${n.title}</b>
          <small style="display:block; color:#888; margin-bottom:5px;">
            ${n.createdAt ? new Date(n.createdAt).toLocaleString() : ''}
          </small>
          <p style="margin:5px 0 10px 0; font-size:13px;">${n.body}</p>
          ${markBtnHtml}
          <button class="delete-btn" data-id="${n.id}" style="
            background:#f44336;
            color:white;
            border:none;
            padding:5px 10px;
            border-radius:4px;
            cursor:pointer;
            transition:0.2s;
          ">Eliminar</button>
        </div>`;
      }).join('')
    : '<p>No tienes notificaciones por ahora.</p>';

  Swal.fire({
    title: 'Buzón de notificaciones',
    html,
    width: '420px',
    showCloseButton: true,
    showConfirmButton: false,
    customClass: { popup: 'notifications-swal-popup' },
    didOpen: () => {
      // Solo habrá botones con esta clase en notificaciones NO leídas
      const markButtons = Swal.getHtmlContainer()
        ?.querySelectorAll<HTMLButtonElement>('.mark-read-btn');

      if (markButtons) {
        for (const btn of markButtons) {
          btn.addEventListener('click', () => {
            const idStr = btn.getAttribute('data-id');
            if (!idStr) return;
            const noti = this.notifications.find(n => String(n.id) === idStr);
            if (noti) this.markNotificationAsRead(noti);

            btn.disabled = true;
            btn.textContent = 'Leída';
            btn.style.background = '#888';
            btn.style.cursor = 'default';
          });
        }
      }

      const deleteButtons = Swal.getHtmlContainer()
        ?.querySelectorAll<HTMLButtonElement>('.delete-btn');

      if (deleteButtons) {
        for (const btn of deleteButtons) {
          btn.addEventListener('click', () => {
            const idStr = btn.getAttribute('data-id');
            if (!idStr) return;
            const noti = this.notifications.find(n => String(n.id) === idStr);
            if (noti) this.deleteNotification(noti);

            const div = document.getElementById(`noti-${idStr}`);
            div?.remove();
            const htmlContainer = Swal.getHtmlContainer();
            const remainingNotis = htmlContainer?.querySelectorAll('[id^="noti-"]') ?? [];

            if (htmlContainer && remainingNotis.length === 0) {
              htmlContainer.innerHTML = `
                <p style="text-align:center; margin: 12px 0;">
                  No tienes notificaciones por ahora.
                </p>
              `;
            }
          });
        }
      }
    }
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
  onKeyDown(event: KeyboardEvent) {
    console.log('Key down:', event.key);
  }

  onKeyPress(event: KeyboardEvent) {
    console.log('Key press:', event.key);
  }

  onKeyUp(event: KeyboardEvent) {
    console.log('Key up:', event.key);
  }





}
