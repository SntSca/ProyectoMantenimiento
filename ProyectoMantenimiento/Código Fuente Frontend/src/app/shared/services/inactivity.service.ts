import { Injectable, NgZone } from '@angular/core';
import { Router } from '@angular/router';
import { fromEvent, merge, Subscription } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
import { SharedService, SessionInfo } from './shared.services';

@Injectable({
  providedIn: 'root'
})
export class InactivityService {

  /**
   * ===============================
   *  CONFIGURACIÓN DE INACTIVIDAD (MODO PRUEBAS EN SEGUNDOS)
   * ===============================
   */

  // Cierre por inactividad: 20 segundos SIN actividad
  private readonly INACTIVITY_LOGOUT_TIME = 15 * 60 * 1000; // 20 s

  // Aviso por inactividad: a los 10 segundos sin actividad
  private readonly INACTIVITY_WARNING_TIME = 14 * 60 * 1000; // 10 s

  // Tiempo que tiene el usuario para responder al aviso: 5 segundos
  private readonly WARNING_TIMEOUT = 60 * 1000; // 5 s

  /**
   * ===============================
   *  TIMERS / SUBSCRIPCIONES
   * ===============================
   */

  // Timers de inactividad
  private inactivityWarningTimer: any;
  private inactivityLogoutTimer: any;
  private inactivityWarningCountdownTimer: any;

  // Timers de timeout absoluto (basados en info del backend)
  private absoluteWarningTimer: any;
  private absoluteLogoutTimer: any;
  private absoluteWarningCountdownTimer: any;

  // Eventos de actividad (ratón, teclado, scroll...)
  private activityEventsSub?: Subscription;

  private monitoring = false;

  constructor(
    private readonly zone: NgZone,
    private readonly router: Router,
    private readonly sharedService: SharedService
  ) {}

  /**
   * Inicia el control de inactividad y de tiempo máximo de sesión.
   * Se llama desde app.component cuando detectas que hay usuario logueado.
   */
  start(): void {
    if (this.monitoring) {
      return;
    }
    this.monitoring = true;

    this.clearAllTimers();
    this.listenToUserActivity();
    this.startInactivityTimers();

    // Pedimos al backend la info de sesión absoluta (loginTime / expirationTime)
    this.sharedService.getSessionInfo().subscribe({
      next: (info: SessionInfo) => {
        this.startAbsoluteSessionTimersFromBackend(info);
      },
      error: () => {
        console.warn('No se pudo obtener la información de sesión absoluta.');
      }
    });
  }

  stop(): void {
    this.monitoring = false;
    this.clearAllTimers();
    this.unsubscribeFromUserActivity();
  }

  /**
   * Reset de actividad desde:
   *  - eventos del usuario (ratón/teclado…)
   *  - peticiones HTTP (InactivityInterceptor)
   */
  resetActivity(): void {
    if (!this.monitoring) {
      return;
    }

    this.clearInactivityTimers();
    this.startInactivityTimers();
    // OJO: los timers de sesión absoluta NO se reinician -> los manda el back
  }

  /**
   * ===============================
   *  CONTROL DE EVENTOS DE ACTIVIDAD
   * ===============================
   */

  private listenToUserActivity(): void {
    this.zone.runOutsideAngular(() => {
      const mouseMove$ = fromEvent(window, 'mousemove');
      const keyDown$ = fromEvent(window, 'keydown');
      const click$ = fromEvent(window, 'click');
      const scroll$ = fromEvent(window, 'scroll');
      const touch$ = fromEvent(window, 'touchstart');

      const activity$ = merge(mouseMove$, keyDown$, click$, scroll$, touch$).pipe(
        debounceTime(500)
      );

      this.activityEventsSub = activity$.subscribe(() => {
        // Cualquier actividad del usuario resetea los timers de INACTIVIDAD
        this.zone.run(() => this.resetActivity());
      });
    });
  }

  private unsubscribeFromUserActivity(): void {
    if (this.activityEventsSub) {
      this.activityEventsSub.unsubscribe();
      this.activityEventsSub = undefined;
    }
  }

  /**
   * ===============================
   *  TIMERS DE INACTIVIDAD (10 / 20 SEGUNDOS)
   * ===============================
   */

  private startInactivityTimers(): void {
    // Aviso de inactividad a los 10 segundos
    this.inactivityWarningTimer = setTimeout(() => {
      this.showInactivityWarning();
    }, this.INACTIVITY_WARNING_TIME);

    // Cierre de sesión por seguridad a los 20 segundos
    this.inactivityLogoutTimer = setTimeout(() => {
      this.forceLogoutForInactivity();
    }, this.INACTIVITY_LOGOUT_TIME);
  }

  private clearInactivityTimers(): void {
    clearTimeout(this.inactivityWarningTimer);
    clearTimeout(this.inactivityLogoutTimer);
    clearTimeout(this.inactivityWarningCountdownTimer);
  }

  private showInactivityWarning(): void {
    // Por simplicidad uso window.confirm.
    // Si tienes un diálogo de Angular Material/PrimeNG puedes sustituir esto.
    this.inactivityWarningCountdownTimer = setTimeout(() => {
      this.forceLogoutForInactivity();
    }, this.WARNING_TIMEOUT);

    const continuar = window.confirm(
      'Tu sesión se cerrará en unos segundos por inactividad. ' +
      'Pulsa "Aceptar" para continuar con la sesión.'
    );

    // Si el usuario responde antes de que se cumpla el timeout:
    clearTimeout(this.inactivityWarningCountdownTimer);

    if (continuar) {
      // El usuario quiere seguir -> consideramos que hay actividad nueva
      this.resetActivity();
    } else {
      // Prefiere cerrar o cierra el diálogo -> cerramos sesión ya
      this.forceLogoutForInactivity();
    }
  }

  /**
   * ===============================
   *  TIMERS DE SESIÓN ABSOLUTA (BACK)
   * ===============================
   */

  /**
   * Recibe la info del backend con loginTime y expirationTime
   * y programa:
   *  - aviso 1 minuto antes
   *  - cierre en la fecha de expiración
   *
   * Nota: aquí seguimos usando minutos porque lo manda el backend.
   * Si en el back reduces a segundos para pruebas, esto seguirá funcionando
   * sin tocar nada (expirationTime se adelanta y listo).
   */
  private startAbsoluteSessionTimersFromBackend(info: SessionInfo): void {
    this.clearAbsoluteTimers();

    if (!info?.expirationTime) {
      return;
    }


    const expiration = new Date(info.expirationTime).getTime();
    const now = Date.now();

    let diffMs = expiration - now;
    if (diffMs <= 0) {
      // Ya está expirada según el backend
      this.forceLogoutForAbsoluteTimeout();
      return;
    }

    // Aviso 1 minuto antes
    const warningMs = diffMs - 60 * 1000;

    if (warningMs > 0) {
      this.absoluteWarningTimer = setTimeout(() => {
        this.showAbsoluteTimeoutWarning();
      }, warningMs);
    } else {
      // Si ya queda menos de 1 min, solo programamos el cierre
      console.warn('Queda menos de 1 minuto para la expiración absoluta.');
    }

    // Cierre en el momento exacto de expiración
    this.absoluteLogoutTimer = setTimeout(() => {
      this.forceLogoutForAbsoluteTimeout();
    }, diffMs);
  }

  private clearAbsoluteTimers(): void {
    clearTimeout(this.absoluteWarningTimer);
    clearTimeout(this.absoluteLogoutTimer);
    clearTimeout(this.absoluteWarningCountdownTimer);
  }

  private showAbsoluteTimeoutWarning(): void {
    this.absoluteWarningCountdownTimer = setTimeout(() => {
      this.forceLogoutForAbsoluteTimeout();
    }, 60 * 1000); // 1 minuto

    const continuar = window.confirm(
      'Has estado conectado mucho tiempo. ' +
      'Por seguridad, tu sesión se cerrará en 1 minuto.\n\n' +
      'Pulsa "Aceptar" si quieres cerrar sesión ahora ' +
      'o "Cancelar" para seguir hasta que se cumpla el minuto.'
    );

    clearTimeout(this.absoluteWarningCountdownTimer);

    if (continuar) {
      this.forceLogoutForAbsoluteTimeout();
    } else {
      // Si cancela, dejamos que el timer de expiración absoluta haga el cierre cuando toque
    }
  }

  /**
   * ===============================
   *  LOGOUTS
   * ===============================
   */

  private forceLogoutForInactivity(): void {
    this.doLogout('inactividad (modo pruebas)');
  }

  private forceLogoutForAbsoluteTimeout(): void {
    this.doLogout('tiempo máximo de sesión (backend)');
  }

  private doLogout(reason: string): void {
    this.clearAllTimers();
    this.unsubscribeFromUserActivity();

    this.sharedService.logout().subscribe({
      next: () => {
        this.sharedService.clearSession();
        this.router.navigate(['/login']);
      },
      error: () => {
        this.sharedService.clearSession();
        this.router.navigate(['/login']);
      }
    });

    console.info(`Sesión cerrada por ${reason}`);
  }

  /**
   * Limpia todos los timers (inactividad + absoluto)
   */
  private clearAllTimers(): void {
    this.clearInactivityTimers();
    this.clearAbsoluteTimers();
  }
}
