import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { SharedService } from './shared/services/shared.services';
import { ApiConfigService } from './shared/services/api-config.service';
import { InactivityService } from './shared/services/inactivity.service';
import { filter } from 'rxjs/operators';
import { AuthService } from './shared/services/auth.service'; // ajusta la ruta si es distinta

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  standalone: true,
  imports: [RouterModule],
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'ESIMedia-Frontend';

  constructor(
    private readonly sharedService: SharedService,
    private readonly apiConfig: ApiConfigService,
    private readonly inactivityService: InactivityService,
    private readonly router: Router,
    private readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    // restoreSession returns a Promise; use finally so ngOnInit remains synchronous (void) per OnInit
    this.authService.restoreSession().finally(() => {
      this.checkAuthAndStartInactivity();

      this.router.events
        .pipe(filter((event) => event instanceof NavigationEnd))
        .subscribe(() => {
          this.inactivityService.resetActivity();
          this.checkAuthAndStartInactivity();
        });
    });
  }

  ngOnDestroy(): void {
    this.inactivityService.stop();
  }

  /**
   * Verifica si hay un usuario autenticado y activa el servicio de inactividad
   */
  private checkAuthAndStartInactivity(): void {
    const hasUser = sessionStorage.getItem('user_data');
    const hasGestor = sessionStorage.getItem('gestor_data');
    const hasAdmin = sessionStorage.getItem('admin_data');
    const hasToken = sessionStorage.getItem('token');

    // Solo activar el servicio si hay un usuario autenticado
    if ((hasUser || hasGestor || hasAdmin) && hasToken) {
      this.inactivityService.start();
    } else {
      this.inactivityService.stop();
    }
  }

  // ❌ Eliminamos el beforeunload que hacía logout en cada refresco
}
