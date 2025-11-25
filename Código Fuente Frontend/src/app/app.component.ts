import { Component, HostListener, OnInit, OnDestroy } from '@angular/core';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { SharedService } from './shared/services/shared.services';
import { ApiConfigService } from './shared/services/api-config.service';
import { InactivityService } from './shared/services/inactivity.service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  standalone: true,
  imports: [RouterModule],
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'ESIMedia-Frontend';

  constructor(
    private sharedService: SharedService,
    private apiConfig: ApiConfigService,
    private inactivityService: InactivityService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Verificar si el usuario está autenticado y activar el servicio de inactividad
    this.checkAuthAndStartInactivity();

    // Monitorear cambios de ruta para detectar actividad
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.inactivityService.resetActivity();
      this.checkAuthAndStartInactivity();
    });
  }

  ngOnDestroy(): void {
    this.inactivityService.stop();
  }

  /**
   * Verifica si hay un usuario autenticado y activa el servicio de inactividad
   */
  private checkAuthAndStartInactivity(): void {
    const hasUser = localStorage.getItem('user_data');
    const hasGestor = localStorage.getItem('gestor_data');
    const hasAdmin = localStorage.getItem('admin_data');
    const hasToken = localStorage.getItem('token');

    // Solo activar el servicio si hay un usuario autenticado
    if ((hasUser || hasGestor || hasAdmin) && hasToken) {
      this.inactivityService.start();
    } else {
      this.inactivityService.stop();
    }
  }



  @HostListener('window:beforeunload', ['$event'])
  onBeforeUnload(event: Event) {
    const token = localStorage.getItem('token');
    if (token) {
      const url = this.apiConfig.getUsersAuthenticationUrl('logout');
      // Usar sendBeacon para enviar logout de forma confiable al cerrar el navegador
      const data = JSON.stringify({});
      navigator.sendBeacon(url, data);
      // Limpiar localStorage inmediatamente
      this.sharedService.clearSession();
    }
  }
}
