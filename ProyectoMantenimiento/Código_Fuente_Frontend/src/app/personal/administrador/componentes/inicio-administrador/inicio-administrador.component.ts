import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Subject, firstValueFrom } from 'rxjs';
import {
  FormValidationService,
  APP_ROUTES,
  ADMINISTRADOR_ROUTES,
  IMAGENES_PERMITIDAS,
  MOCK_ADMINISTRADORES,
  UserStatus,
  AdministradorStatus,
  GestorStatus,
  Contenido,
  DEPARTAMENTOS,
  ContentService,
  SharedService,
  Especialidades,
  Tags,
  GetAllUsersResponse,
  FilterService,
  Videos
} from '@shared';
import { AdministradorService } from '../../administrador.service';
import { NotificationService } from '../../../../shared/services/notification.service';
import { ConfirmationService } from '../../../../shared/services/confirmation.service';


@Component({
  selector: 'app-inicio-administrador',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './inicio-administrador.component.html',
})
export class InicioAdministradorComponent implements OnInit, OnDestroy {

  actualAdministrator: AdministradorStatus = {} as AdministradorStatus;
  searchTerm: string = '';

  // Subject para búsqueda dinámica
  private readonly searchSubject = new Subject<string>();
  private readonly destroy$ = new Subject<void>();
  
  // Propiedades Paneles
  vipUserCount: number = 0;
  validatedGestorCount: number = 0;
  activeAdministradorCount: number = 0;
  contenidosAudios: number = 0;
  contenidosVideos: number = 0;

  seccionActual: string = 'inicio';
  elementosFiltrados: any[] = [];

  filtroVip: boolean | null = null;
  filtroEspecialidadGestores: string[] = [];
  filtroTagsContenidos: string[] = [];

  filtroTipoContenidoContenidos: string | null = null;
  isTipoContenidoContenidos: boolean | null = null;

  filtroVisibilidadContenidos: boolean | null = null;
  filtroRestriccionEdad: number | null = null;

  filtroDepartamento: string[] = [];

  isTipoContenido: boolean | null = null; // null = todos, true = audio, false = video
  filtroTipoContenido: string | null = null;

  isBloqueado: boolean | null = null; // null = todos, true = bloqueados, false = activos
  filtroEstado: boolean | null = null; // Para el toggle de estado en gestores

  isValidado: boolean | null = null; // null = todos, true = validados, false = no validados
  filtroValidacion: boolean | null = null; // Para el toggle de validación en gestores

  // Propiedades para edición de tarjetas completas
  tarjetaEditando: any = null;
  datosAEditar: any = null;
  datosOriginales: any = null;
  editForm: FormGroup | null = null;
  editModel: any = null; // clon temporal para editar sin mutar la lista
  submitted = false;
  previewImage: string | undefined;

  // Propiedades para vista grid/list de contenidos
  isListView: boolean = false;
  showTagFilters: boolean = false;
  showEspecialidadFilters: boolean = false;
  showDepartamentoFilters: boolean = false;

  processer: any = null;

  // Propiedades para toggle de contraseña
  showPassword: boolean = false;
  showConfirmPassword: boolean = false;

  // Getter para saber si estamos en modo edición
  get isEditingMode(): boolean {
    return !!this.tarjetaEditando;
  }

  // Constantes de rutas para usar en templates
  public readonly ADMINISTRADOR_ROUTES = ADMINISTRADOR_ROUTES;
  public readonly APP_ROUTES = APP_ROUTES;
  public readonly DEPARTAMENTOS = DEPARTAMENTOS;
  
  formatosImagen = IMAGENES_PERMITIDAS;
  acceptExtensionsImagen: string =
    IMAGENES_PERMITIDAS.extensiones.length
      ? '.' + IMAGENES_PERMITIDAS.extensiones.join(', .')
      : '';

  especialidades: Especialidades[] = [];
  tags: Tags[] = [];
  userProfile: UserStatus[] = [];
  gestorProfile: GestorStatus[] = [];
  administratorProfile: AdministradorStatus[] = [];

  contenidos: Contenido[] = [];
  contenidosFiltrados: Contenido[] = [];

  // Estados de carga
  loadingContent = false;

  // Propiedades para estadísticas
  top5VideosVisualizados: Contenido[] = [];
  top5MejorValorados: Contenido[] = [];
  top5CategoriasVistas: { nombre: string; visualizaciones: number }[] = [];

  constructor(
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly formValidationService: FormValidationService,
    private readonly contentService: ContentService,
    private readonly administradorService: AdministradorService,
    private readonly sharedService: SharedService,
    private readonly filterService: FilterService,
    private readonly notificationService: NotificationService,
    private readonly confirmationService: ConfirmationService
  ) {}

  // =============================================================================
  // MÉTODOS DE INICIALIZACIÓN
  // =============================================================================

ngOnInit(): void {
  void this.initialize();
}

private async initialize(): Promise<void> {
  // 1) Cargar el administrador actual usando el JWT
  await this.loadAdministrator();

  // 2) Leer el parámetro de consulta 'seccion' si existe
  this.route.queryParams.subscribe(params => {
    if (params['seccion']) {
      this.cambiarSeccion(params['seccion']);
    }
  });
  
  // 3) Cargar datos de usuarios/gestores/admins
  this.administradorService.getAllUsers().subscribe({
    next: (response: GetAllUsersResponse) => {
      // Procesar usuarios normales
      this.userProfile = ((response.normalUsers || []) as any[]).map(user => ({
        idUsuario: user.idUsuario,
        tipo: 'usuario',
        nombre: user.nombre,
        primerApellido: user.apellidos.split(' ')[0],
        segundoApellido: user.apellidos.split(' ')[1],
        email: user.email,
        alias: user.alias,
        fechaNacimiento: user.fechaNacimiento ? new Date(user.fechaNacimiento).toISOString().split('T')[0] : '',
        flagVIP: user.flagVIP,
        fotoPerfil: user.fotoPerfil ?? 'assets/porDefecto.jpg',
        bloqueado: user.bloqueado,
        twoFactorEnabled: user.twoFactorEnabled,
        thirdFactorEnabled: user.thirdFactorEnabled
      }));

      // Procesar gestores/creadores
      this.gestorProfile = ((response.contentCreators || []) as any[]).map(gestor => ({
        idUsuario: gestor.idUsuario,
        tipo: 'gestor',
        nombre: gestor.nombre,
        primerApellido: gestor.apellidos.split(' ')[0],
        segundoApellido: gestor.apellidos.split(' ')[1],
        email: gestor.email,
        alias: gestor.aliasCreador,
        tipoContenido: gestor.tipoContenido,
        especialidad: gestor.especialidad,
        descripcion: gestor.descripcion,
        fotoPerfil: gestor.fotoPerfil ?? 'assets/porDefecto.jpg',
        bloqueado: gestor.bloqueado,
        validado: gestor.validado,
        twoFactorEnabled: gestor.twoFactorEnabled,
        thirdFactorEnabled: gestor.thirdFactorEnabled
      }));

      // Procesar administradores (excluyendo el actual)
      this.administratorProfile = ((response.administrators || []) as any[])
        .filter(admin => admin.email !== this.actualAdministrator.email)
        .map(admin => ({
          idUsuario: admin.idUsuario,
          tipo: 'administrador',
          nombre: admin.nombre,
          primerApellido: admin.apellidos.split(' ')[0],
          segundoApellido: admin.apellidos.split(' ')[1],
          email: admin.email,
          alias: admin.alias,
          fotoPerfil: admin.fotoPerfil ?? 'assets/porDefecto.jpg',
          departamento: admin.departamento,
          bloqueado: admin.bloqueado,
          twoFactorEnabled: admin.twoFactorEnabled,
          thirdFactorEnabled: admin.thirdFactorEnabled
        }));
    },
    error: (error) => {
      console.error('[InicioAdministrador] Error al obtener usuarios:', error);
    }
  });

  // 4) Tags y especialidades
  this.sharedService.getTags().subscribe((tags: any[]) => {
    this.tags = tags;
  });
  this.sharedService.getEspecialidades().subscribe((especialidades: any[]) => {
    this.especialidades = especialidades;
  });

  // 5) Búsqueda dinámica
  this.sharedService.setupDynamicSearch(this.searchSubject, (searchTerm) => {
    this.searchTerm = searchTerm;
    this.buscar();
  }, this.destroy$);

  // 6) Contenido + estadísticas
  this.loadContent();
}


  /**
   * Carga los datos del administrador actual desde backend usando el userId del token.
   */
  public async loadAdministrator(): Promise<void> {
  try {
    const credentials = this.sharedService.obtainCredentials();

    if (!credentials?.userId || credentials?.rol !== 'ADMINISTRADOR') {
      console.warn('[InicioAdministrador] Credenciales no válidas o rol != ADMINISTRADOR, usando MOCK_ADMINISTRADORES[0]');
      this.actualAdministrator = MOCK_ADMINISTRADORES[0];
      return;
    }

    const userId = credentials.userId;
    const response = await firstValueFrom(
      this.administradorService.getAllUsers()
    );

    const admins = (response.administrators || []) as any[];

    const adminData = admins.find(a => a.idUsuario === userId);

    if (!adminData) {
      console.warn('[InicioAdministrador] No se ha encontrado administrador con ese userId, usando mock');
      this.actualAdministrator = MOCK_ADMINISTRADORES[0];
      return;
    }

    const apellidosRaw = adminData.apellidos ?? '';
    const [primerApellido, segundoApellido] = apellidosRaw.split(' ');

    const admin: AdministradorStatus = {
      idUsuario: adminData.idUsuario,
      tipo: 'administrador',
      nombre: adminData.nombre,
      primerApellido: primerApellido || '',
      segundoApellido: segundoApellido || '',
      email: adminData.email,
      alias: adminData.alias,
      fotoPerfil: adminData.fotoPerfil ?? 'assets/porDefecto.jpg',
      departamento: adminData.departamento,
      bloqueado: adminData.bloqueado ?? false,
      twoFactorEnabled: adminData.twoFactorEnabled,
      thirdFactorEnabled: adminData.thirdFactorEnabled
    };

    this.actualAdministrator = admin;
  } catch (error) {
    console.error('[InicioAdministrador] Error al cargar administrador, usando MOCK_ADMINISTRADORES[0]:', error);
    this.actualAdministrator = MOCK_ADMINISTRADORES[0];
  }
}



  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.searchSubject.complete();
  }

  // =============================================================================
  // MÉTODOS DE AUTENTICACIÓN
  // =============================================================================

  logout(): void {
    this.sharedService.performLogout(this.router, this.APP_ROUTES.home);
  }

  // ===========================================================================  
  // MÉTODOS DE ESTADÍSTICAS
  // =============================================================================

  calcularEstadisticasPanel(): void {
    this.vipUserCount = this.userProfile.filter(user => user.flagVIP).length;
    this.validatedGestorCount = this.gestorProfile.filter(gestor => gestor.validado).length;
    this.activeAdministradorCount = this.administratorProfile.filter(admin => !admin.bloqueado).length;
    this.contenidosAudios = this.contenidos.filter(contenido => contenido.tipo === 'AUDIO').length;
    this.contenidosVideos = this.contenidos.filter(contenido => contenido.tipo === 'VIDEO').length;
    
    if (this.contenidos.length > 0) {
      this.calcularEstadisticas();
    }
  }

  // =============================================================================
  // MÉTODOS DE ESTADÍSTICAS DETALLADAS
  // =============================================================================

  calcularEstadisticas(): void {
    this.top5VideosVisualizados = [...this.contenidos]
      .sort((a, b) => (b.visualizaciones || 0) - (a.visualizaciones || 0))
      .slice(0, 5);

    this.top5MejorValorados = [...this.contenidos]
      .filter(c => c.valoracionMedia && c.valoracionMedia > 0)
      .sort((a, b) => (b.valoracionMedia || 0) - (a.valoracionMedia || 0))
      .slice(0, 5);

    const tagVisualizaciones = new Map<string, number>();
    
    for (const contenido of this.contenidos) {
      for (const tag of contenido.tags) {
        const visualizacionesActuales = tagVisualizaciones.get(tag) ?? 0;
        tagVisualizaciones.set(tag, visualizacionesActuales + (contenido.visualizaciones || 0));
      }
    }

    this.top5CategoriasVistas = Array.from(tagVisualizaciones.entries())
      .map(([nombre, visualizaciones]) => ({ nombre, visualizaciones }))
      .sort((a, b) => b.visualizaciones - a.visualizaciones)
      .slice(0, 5);
  }

  private loadContent(): void {
    this.sharedService.loadContent(this.contentService).subscribe({
      next: (result) => {
        this.contenidos = result.contenidos;
        this.contenidosFiltrados = result.contenidosFiltrados;
        this.calcularEstadisticasPanel();
      },
      error: (error) => {
        console.error('Error al cargar contenido:', error);
      }
    });
  }

  secondsToMinutesSeconds(seconds: number): string {
    return this.sharedService.secondsToMinutesSeconds(seconds);
  }

  // =============================================================================
  // MÉTODOS DE GESTIÓN DE FOTOS
  // =============================================================================

  async onFileSelected(event: Event): Promise<void> {
    const result = await this.sharedService.handlePhotoUpload(event, 5);
    
    switch (result.status) {
      case 'SUCCESS':
        if (this.tarjetaEditando) {
          this.tarjetaEditando.fotoPerfil = result.previewUrl!;
          if (this.editModel) {
            this.editModel.fotoPerfil = result.previewUrl!;
          }
          if (this.editForm) {
            this.editForm.patchValue({ fotoPerfil: result.previewUrl! });
          }
        } else if (this.seccionActual === 'administradores' && !this.datosOriginales) {
          this.previewImage = result.previewUrl!;
        }
        break;
      case 'ERROR_INVALID_TYPE':
      case 'ERROR_TOO_LARGE':
      case 'ERROR_NO_FILE':
        this.notificationService.warning(result.message ?? 'Error al procesar la imagen.');
        break;
    }
  }

  // =============================================================================
  // MÉTODOS DE BÚSQUEDA DINÁMICA
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

  // =============================================================================
  // MÉTODOS DEL SERVICIO
  // =============================================================================
  
  buscar(): void {
    switch (this.seccionActual) {
      case 'usuarios':
        this.aplicarFiltrosUsuarios();
        break;
      case 'gestores':
        this.aplicarFiltrosGestores();
        break;
      case 'administradores':
        this.aplicarFiltrosAdministradores();
        break;
      case 'contenidos':
        this.aplicarFiltrosContenidos();
        return;
    }

    this.elementosFiltrados = this.filterService.filtrarPorBusqueda(
      this.elementosFiltrados,
      this.searchTerm,
      this.seccionActual
    );
  }

  cambiarSeccion(seccion: string): void {
    if (this.tarjetaEditando) {
      this.cancelarEdicionTarjeta();
    }

    this.seccionActual = seccion;
    this.restablecerFiltros();
  }

  // =============================================================================
  // MÉTODOS DE FILTRADO
  // =============================================================================

  filtrarPorEspecialidad(especialidad: string | null): void {
    if (especialidad === null) {
      this.filtroEspecialidadGestores = [];
    } else {
      const index = this.filtroEspecialidadGestores.indexOf(especialidad);
      if (index > -1) {
        this.filtroEspecialidadGestores.splice(index, 1);
      } else {
        this.filtroEspecialidadGestores.push(especialidad);
      }
    }
    this.aplicarFiltrosGestores();
    this.searchTermValue = '';
  }

  isEspecialidadActive(especialidad: string): boolean {
    return this.filtroEspecialidadGestores.includes(especialidad);
  }

  filtrarPorTagsContenidos(tag: string | null): void {
    if (tag === null) {
      this.filtroTagsContenidos = [];
    } else {
      const index = this.filtroTagsContenidos.indexOf(tag);
      if (index > -1) {
        this.filtroTagsContenidos.splice(index, 1);
      } else {
        this.filtroTagsContenidos.push(tag);
      }
    }
    this.aplicarFiltrosContenidos();
    this.searchTermValue = '';
  }

  isTagActive(tagNombre: string): boolean {
    return this.filtroTagsContenidos.includes(tagNombre);
  }

  filtrarPorTipoContenidoContenidos(tipo: string | null): void {
    this.filtroTipoContenidoContenidos = tipo;
    this.aplicarFiltrosContenidos();
    this.searchTermValue = '';
  }

  filtrarPorDepartamento(departamento: string | null): void {
    if (departamento === null) {
      this.filtroDepartamento = [];
    } else {
      const index = this.filtroDepartamento.indexOf(departamento);
      if (index > -1) {
        this.filtroDepartamento.splice(index, 1);
      } else {
        this.filtroDepartamento.push(departamento);
      }
    }
    this.aplicarFiltrosAdministradores();
    this.searchTermValue = '';
  }

  isDepartamentoActive(departamento: string): boolean {
    return this.filtroDepartamento.includes(departamento);
  }

  filtrarPorTipoContenido(tipo: string | null): void {
    this.filtroTipoContenido = tipo;
    this.aplicarFiltrosGestores();
    this.searchTermValue = '';
  }

  filtrarPorEstadoGestores(bloqueado: boolean | null): void {
    this.filtroEstado = bloqueado;
    this.aplicarFiltrosGestores();
    this.searchTermValue = '';
  }

  filtrarPorEstadoUsuarios(bloqueado: boolean | null): void {
    this.filtroEstado = bloqueado;
    this.aplicarFiltrosUsuarios();
    this.searchTermValue = '';
  }

  filtrarPorEstadoAdministradores(bloqueado: boolean | null): void {
    this.filtroEstado = bloqueado;
    this.aplicarFiltrosAdministradores();
    this.searchTermValue = '';
  }

  aplicarFiltrosUsuarios(): void {
    this.elementosFiltrados = this.filterService.aplicarFiltros(this.userProfile, {
      seccion: 'usuarios',
      filtroVip: this.filtroVip,
      filtroEstado: this.filtroEstado,
    });
  }

  aplicarFiltrosGestores(): void {
    this.elementosFiltrados = this.filterService.aplicarFiltros(this.gestorProfile, {
      seccion: 'gestores',
      filtroEspecialidadGestores: this.filtroEspecialidadGestores,
      filtroTipoContenido: this.filtroTipoContenido,
      filtroEstado: this.filtroEstado,
      filtroValidacion: this.filtroValidacion
    });
  }

  aplicarFiltrosAdministradores(): void {
    this.elementosFiltrados = this.filterService.aplicarFiltros(this.administratorProfile, {
      seccion: 'administradores',
      filtroDepartamento: this.filtroDepartamento,
      filtroEstado: this.filtroEstado
    });
  }

  aplicarFiltrosContenidos(): void {
    this.contenidosFiltrados = this.filterService.aplicarFiltros(this.contenidos, {
      seccion: 'contenidos',
      filtroTipoContenidoContenidos: this.filtroTipoContenidoContenidos,
      filtroTagsContenidos: this.filtroTagsContenidos,
      filtroVisibilidadContenidos: this.filtroVisibilidadContenidos,
      filtroRestriccionEdad: this.filtroRestriccionEdad
    });
  }

  toggleEstado(): void {
    let nuevoEstado: boolean | null;

    if (this.filtroEstado === null) {
      nuevoEstado = true;
    } else if (this.filtroEstado === true) {
      nuevoEstado = false;
    } else {
      nuevoEstado = null;
    }

    this.filtroEstado = nuevoEstado;

    if (this.seccionActual === 'usuarios') {
      this.filtrarPorEstadoUsuarios(nuevoEstado);
    } else if (this.seccionActual === 'gestores') {
      this.filtrarPorEstadoGestores(nuevoEstado);
    } else if (this.seccionActual === 'administradores') {
      this.filtrarPorEstadoAdministradores(nuevoEstado);
    }
    this.searchTermValue = '';
  }

  toggleVip(): void {
    let nuevoEstado: boolean | null;
    if (this.filtroVip === null) {
      nuevoEstado = true;
    } else if (this.filtroVip === true) {
      nuevoEstado = false;
    } else {
      nuevoEstado = null;
    }

    this.filtroVip = nuevoEstado;
    this.aplicarFiltrosUsuarios();
    this.searchTermValue = '';
  }

  toggleValidacion(): void {
    let EstadoValidacion: boolean | null;

    if (this.filtroValidacion === null) {
      EstadoValidacion = true;
    } else if (this.filtroValidacion === true) {
      EstadoValidacion = false;
    } else {
      EstadoValidacion = null;
    }
    
    this.filtroValidacion = EstadoValidacion;
    this.aplicarFiltrosGestores();
    this.searchTermValue = '';
  }

  toggleVisibilidadContenidos(): void {
    let nuevoEstado: boolean | null;

    if (this.filtroVisibilidadContenidos === null) {
      nuevoEstado = true;
    } else if (this.filtroVisibilidadContenidos === true) {
      nuevoEstado = false;
    } else {
      nuevoEstado = null;
    }
    
    this.filtroVisibilidadContenidos = nuevoEstado;
    this.aplicarFiltrosContenidos();
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
    this.aplicarFiltrosContenidos();
  }

  toggleTipoContenido(): void {
    let nuevoTipo: string | null;

    if (this.filtroTipoContenido === null) {
      nuevoTipo = 'AUDIO';
    } else if (this.filtroTipoContenido === 'AUDIO') {
      nuevoTipo = 'VIDEO';
    } else {
      nuevoTipo = null;
    }
    
    this.filtroTipoContenido = nuevoTipo;
    this.aplicarFiltrosGestores();
    this.searchTermValue = '';
  }

  togglePassword(field: string): void {
    if (field === 'password') {
      this.showPassword = this.sharedService.togglePasswordVisibility(this.showPassword);
    } else if (field === 'confirmPassword') {
      this.showConfirmPassword = this.sharedService.togglePasswordVisibility(this.showConfirmPassword);
    }
  }

  restablecerFiltros(): void {
    this.filtroEspecialidadGestores = [];
    this.filtroTipoContenido = null;
    this.isTipoContenido = null;
    this.filtroEstado = null;
    this.filtroValidacion = null;
    this.filtroVip = null;
    this.filtroTagsContenidos = [];
    this.filtroTipoContenidoContenidos = null;
    this.isTipoContenidoContenidos = null;
    this.filtroVisibilidadContenidos = null;
    this.filtroRestriccionEdad = null;
    this.filtroDepartamento = [];
    this.isListView = false;
    
    this.showTagFilters = false;
    this.showEspecialidadFilters = false;
    this.showDepartamentoFilters = false; 

    switch (this.seccionActual) {
      case 'usuarios':
        this.elementosFiltrados = this.userProfile;
        break;
      case 'gestores':
        this.elementosFiltrados = this.gestorProfile;
        break;
      case 'administradores':
        this.elementosFiltrados = this.administratorProfile;
        break;
      case 'contenidos':
        this.contenidosFiltrados = this.contenidos;
        break;
      case 'inicio':
        this.elementosFiltrados = [];
        this.calcularEstadisticasPanel();
        break;
    }
    this.searchTermValue = '';
  }

  // =============================================================================
  // MÉTODOS DE EDICIÓN DE TARJETAS COMPLETAS
  // =============================================================================

  iniciarEdicionTarjeta(elemento: any): void {
    if (this.tarjetaEditando && this.tarjetaEditando !== elemento) {
      return;
    }

    if (this.tarjetaEditando === elemento) {
      return;
    }

    this.submitted = false;

    this.tarjetaEditando = elemento;
    this.datosOriginales = structuredClone(elemento);
    this.editModel = structuredClone(elemento);

    if (elemento.tipo === 'usuario') {
      this.editForm = this.formValidationService.createUserProfileForm();
    } else if (elemento.tipo === 'gestor') {
      this.editForm = this.formValidationService.createGestorProfileForm();
    } else if (elemento.tipo === 'administrador') {
      this.editForm = this.formValidationService.createAdministradorProfileForm();
    }

    if (this.editForm) {
      const copy = { ...this.editModel };
      this.editForm.patchValue(copy);
    }
  }

  iniciarCreacionAdministrador(): void {
    this.submitted = false;
    this.previewImage = undefined;

    const nuevoAdmin = {
      tipo: 'administrador',
      nombre: '',
      primerApellido: '',
      segundoApellido: '',
      alias: '',
      email: '',
      departamento: '',
      fotoPerfil: '',
      password: '',
      confirmarPassword: '',
      bloqueado: false
    };

    this.tarjetaEditando = nuevoAdmin;
    this.datosOriginales = null;
    this.editModel = structuredClone(nuevoAdmin);
    this.editForm = this.formValidationService.createAdministradorRegistrationForm();
  }

  guardarEdicionTarjeta(): void {
    if (this.tarjetaEditando && this.editForm) {
      if (!this.formValidationService.validateFormBeforeSubmit(this.editForm)) {
        this.submitted = true;
        return;
      }

      const formData = this.editForm.value;
      Object.assign(this.tarjetaEditando, formData);

      this.tarjetaEditando.apellidos =
        this.tarjetaEditando.primerApellido + ' ' + this.tarjetaEditando.segundoApellido;

      if (this.datosOriginales == null) {
        this.tarjetaEditando.rol = 'ADMINISTRADOR';
        this.tarjetaEditando.fotoPerfil = this.previewImage ?? 'assets/porDefecto.jpg';

        const admin = {
          nombre: this.tarjetaEditando.nombre,
          apellidos: this.tarjetaEditando.primerApellido + ' ' + this.tarjetaEditando.segundoApellido,
          email: this.tarjetaEditando.email,
          departamento: this.tarjetaEditando.departamento,
          formatoFotoPerfil: this.tarjetaEditando.fotoPerfil,
          fotoPerfil: this.tarjetaEditando.fotoPerfil,
          password: this.tarjetaEditando.password,
          alias: this.tarjetaEditando.alias,
          rol: 'ADMINISTRADOR'
        };

        this.administradorService.crearAdministrador(admin).subscribe({
          next: () => {
            this.notificationService.success(
              'El administrador ha sido creado correctamente.',
              'Administrador creado'
            );
            this.ngOnInit();
            this.cambiarSeccion('administradores');

            this.tarjetaEditando = null;
            this.datosOriginales = null;
            this.editForm = null;
            this.editModel = null;
            this.submitted = false;
            this.previewImage = undefined;
          },
          error: (error) => {
            console.error('Error al crear administrador:', error);
            this.notificationService.error('No se pudo crear el administrador. Inténtalo de nuevo.');
          }
        });
      } else {        
        switch (this.tarjetaEditando.tipo) {
          case 'usuario':
            this.datosAEditar = {
              nombre: this.tarjetaEditando.nombre,
              apellidos: this.tarjetaEditando.apellidos,
              alias: this.tarjetaEditando.alias,
              fechaNacimiento: this.tarjetaEditando.fechaNacimiento,
              fotoPerfil: this.tarjetaEditando.fotoPerfil,
            };
            break;
          case 'gestor':
            this.datosAEditar = {
              nombre: this.tarjetaEditando.nombre,
              apellidos: this.tarjetaEditando.apellidos,
              alias: this.tarjetaEditando.alias,
              aliasCreador: this.tarjetaEditando.alias,
              especialidad: this.tarjetaEditando.especialidad,
              descripcion: this.tarjetaEditando.descripcion,
              fotoPerfil: this.tarjetaEditando.fotoPerfil,
            };
            break;
          case 'administrador':
            this.datosAEditar = {
              nombre: this.tarjetaEditando.nombre,
              apellidos: this.tarjetaEditando.apellidos,
              alias: this.tarjetaEditando.alias,
              fotoPerfil: this.tarjetaEditando.fotoPerfil,
            };
            break;
        }

        if (this.tarjetaEditando.fotoPerfil === 'assets/porDefecto.jpg') {
          this.tarjetaEditando.fotoPerfil = null;
        }

        this.administradorService
          .guardarEdicion(this.tarjetaEditando.idUsuario, this.tarjetaEditando.tipo, this.datosAEditar)
          .subscribe(() => {
            this.tarjetaEditando = null;
            this.datosOriginales = null;
            this.editForm = null;
            this.editModel = null;
            this.submitted = false;
            this.buscar();
          });
      }
    }
  }

  cancelarEdicionTarjeta(): void {
    if (this.tarjetaEditando && this.datosOriginales) {
      Object.assign(this.tarjetaEditando, this.datosOriginales);
    }
    this.tarjetaEditando = null;
    this.datosOriginales = null;
    this.editForm = null;
    this.editModel = null;
    this.submitted = false;
    this.previewImage = undefined;
  }

  estaEditandoTarjeta(elemento: any): boolean {
    return this.tarjetaEditando === elemento;
  }

  // =============================================================================
  // MÉTODOS DE VALIDACIÓN
  // =============================================================================

  isFieldInvalid(fieldName: string): boolean {
    return this.sharedService.isFieldInvalid(this.editForm, fieldName, this.submitted);
  }

  getFieldError(fieldName: string): string {
    return this.sharedService.getFieldError(this.editForm, fieldName);
  }

  // =============================================================================
  // MÉTODOS DE SERVICIO
  // =============================================================================

  toggleBloqueo(elemento: any): void {
    this.administradorService.toggleBloqueo(elemento).subscribe({
      next: () => {
        elemento.bloqueado = !elemento.bloqueado;
        this.notificationService.success('El estado de bloqueo ha sido actualizado.', 'Estado cambiado');
      },
      error: (error) => {
        this.notificationService.error('No se pudo cambiar el estado de bloqueo. Inténtalo de nuevo.');
        console.error('Error al cambiar estado de bloqueo:', error);
      }
    });
  }

  async toggleValidacionGestor(elemento: any): Promise<void> {
    if (elemento.validado) {
      return;
    }

    const nombreGestor =
      `${elemento.nombre || ''} ${elemento.primerApellido || ''} ${elemento.segundoApellido || ''}`.trim() ||
      'este gestor';

    const confirmed = await this.confirmationService.confirm({
      message: `¿Estás seguro de que deseas validar a ${nombreGestor}?\n\nUna vez validado, el gestor tendrá acceso completo al sistema.`,
      title: 'Validar gestor',
      type: 'info',
      confirmText: 'Validar',
      cancelText: 'Cancelar'
    });

    if (!confirmed) {
      return;
    }

    this.administradorService.toggleValidacionGestor(elemento.idUsuario).subscribe({
      next: () => {
        if (!elemento.validado) {
          elemento.validado = true;
          this.notificationService.success('El gestor ha sido validado correctamente.', 'Gestor validado');
        }
      },
      error: (error) => {
        this.notificationService.error('No se pudo validar el gestor. Inténtalo de nuevo.');
        console.error('Error al validar gestor:', error);
      }
    });
  }

  async eliminarElemento(elemento: any): Promise<void> {
    const tipo = elemento?.tipo;
    const nombreElemento =
      `${elemento.nombre || ''} ${elemento.primerApellido || ''} ${elemento.segundoApellido || ''}`.trim() ||
      (tipo === 'administrador' ? 'este administrador' : 'este gestor');
    
    const confirmed = await this.confirmationService.confirm({
      message: `¿Estás seguro de que deseas eliminar a ${nombreElemento}?\n\nEsta acción eliminará toda la información asociada y no se puede deshacer.`,
      title: tipo === 'administrador' ? 'Eliminar administrador' : 'Eliminar gestor',
      type: 'error',
      confirmText: 'Eliminar',
      cancelText: 'Cancelar'
    });

    if (!confirmed) {
      return;
    }

    this.administradorService.eliminarElemento(elemento).subscribe({
      next: () => {
        const mensaje =
          tipo === 'administrador'
            ? 'El administrador ha sido eliminado correctamente.'
            : 'El gestor ha sido eliminado correctamente.';
        const titulo = tipo === 'administrador' ? 'Administrador eliminado' : 'Gestor eliminado';
        this.notificationService.success(mensaje, titulo);
        
        if (tipo === 'gestor') {
          this.gestorProfile = this.gestorProfile.filter(g => g !== elemento);
          this.aplicarFiltrosGestores();
        } 
        if (tipo === 'administrador') {
          this.administratorProfile = this.administratorProfile.filter(a => a !== elemento);
          this.aplicarFiltrosAdministradores();
        }
      },
      error: (error) => {
        const mensajeError =
          tipo === 'administrador'
            ? 'No se pudo eliminar el administrador.'
            : 'No se pudo eliminar el gestor.';
        this.notificationService.error(mensajeError);
        console.error('Error al eliminar elemento:', error);
      }
    });
  }

  // =============================================================================
  // MÉTODOS DE VISTA
  // =============================================================================

  getPasswordRequirements(): readonly string[] {
    return this.sharedService.getPasswordRequirements();
  }

  toggleView(): void {
    this.isListView = this.sharedService.toggleView(this.isListView);
  }

  toggleTagFilters(): void {
    this.showTagFilters = !this.showTagFilters;
  }

  toggleEspecialidadFilters(): void {
    this.showEspecialidadFilters = !this.showEspecialidadFilters;
  }

  toggleDepartamentoFilters(): void {
    this.showDepartamentoFilters = !this.showDepartamentoFilters;
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
}
