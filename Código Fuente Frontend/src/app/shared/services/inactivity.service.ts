import { Injectable, NgZone } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, fromEvent, merge } from 'rxjs';
import { debounceTime, takeUntil } from 'rxjs/operators';
import { ConfirmationService } from './confirmation.service';
import { SharedService } from './shared.services';
import { APP_ROUTES } from '../index';

/**
 * ===========================
 * SERVICIO DE DETECCIÓN DE INACTIVIDAD
 * ===========================
 * 
 * Monitorea la actividad del usuario y gestiona el logout automático
 * por inactividad después de 15 minutos.
 * 
 * Comportamiento:
 * - A los 14 minutos de inactividad, muestra un modal de confirmación
 * - Si no hay respuesta en 1 minuto (15 minutos totales), cierra sesión automáticamente
 * - Detecta: clicks, movimiento del mouse, teclas, scroll, llamadas HTTP
 */
@Injectable({
  providedIn: 'root'
})
export class InactivityService {
  private readonly INACTIVITY_WARNING_TIME = 12 * 60 * 1000; // 12 minutos
  private readonly INACTIVITY_LOGOUT_TIME = 15 * 60 * 1000; // 15 minutos
  private readonly WARNING_TIMEOUT = 1 * 60 * 1000; // 1 minuto para responder

  private inactivityTimer: any;
  private warningTimer: any;
  private isWarningShown = false;
  private destroy$ = new Subject<void>();
  private lastActivityTime: number = Date.now();
  private isActive = false;

  constructor(
    private readonly ngZone: NgZone,
    private readonly router: Router,
    private readonly confirmationService: ConfirmationService,
    private readonly sharedService: SharedService
  ) {}

  /**
   * Inicia el monitoreo de inactividad
   */
  start(): void {
    if (this.isActive) {
      return;
    }

    this.isActive = true;
    this.lastActivityTime = Date.now();
    this.setupActivityListeners();
    this.resetInactivityTimer();
  }

  /**
   * Detiene el monitoreo de inactividad
   */
  stop(): void {
    this.isActive = false;
    this.isWarningShown = false;
    this.clearTimers();
    
    // Cerrar cualquier modal de inactividad abierto
    this.confirmationService.closeInactivityWarnings();
    
    this.destroy$.next();
    this.destroy$.complete();
    this.destroy$ = new Subject<void>();
  }

  /**
   * Reinicia el temporizador de inactividad (llamar cuando hay actividad del usuario)
   */
  resetActivity(): void {
    if (!this.isActive) {
      return;
    }

    this.lastActivityTime = Date.now();
    this.isWarningShown = false;
    this.clearTimers();
    this.resetInactivityTimer();
  }

  /**
   * Configura los listeners de actividad del usuario
   */
  private setupActivityListeners(): void {
    if (typeof document === 'undefined' || typeof window === 'undefined') {
      return;
    }

    // Eventos de actividad del usuario
    const events = [
      'mousedown',
      'mousemove',
      'keydown',
      'scroll',
      'touchstart',
      'click'
    ];

    // Usar NgZone.runOutsideAngular para evitar detección de cambios innecesaria
    this.ngZone.runOutsideAngular(() => {
      const activityObservables = events.map(event => 
        fromEvent(document, event)
      );

      // Combinar todos los eventos y aplicar debounce
      merge(...activityObservables)
        .pipe(
          debounceTime(1000), // Evitar resetear demasiado frecuentemente
          takeUntil(this.destroy$)
        )
        .subscribe(() => {
          this.ngZone.run(() => {
            this.resetActivity();
          });
        });
    });
  }

  /**
   * Reinicia el temporizador de inactividad
   */
  private resetInactivityTimer(): void {
    this.clearTimers();

    // Timer para mostrar advertencia (14 minutos)
    this.inactivityTimer = setTimeout(() => {
      this.showInactivityWarning();
    }, this.INACTIVITY_WARNING_TIME);
  }

  /**
   * Muestra la advertencia de inactividad
   */
  private async showInactivityWarning(): Promise<void> {
    if (this.isWarningShown) {
      return;
    }

    this.isWarningShown = true;

    // Timer para logout automático si no responde (1 minuto adicional)
    this.warningTimer = setTimeout(() => {
      this.performAutoLogout();
    }, this.WARNING_TIMEOUT);

    // Mostrar modal de confirmación
    const confirmed = await this.confirmationService.confirm({
      message: 'Tu sesión está a punto de expirar por inactividad.\n\n¿Deseas continuar con la sesión activa?',
      title: '⏰ Sesión inactiva',
      type: 'warning',
      confirmText: 'Continuar activo',
      cancelText: 'Cerrar sesión',
      isInactivityWarning: true
    });

    // Limpiar el timer de logout automático
    if (this.warningTimer) {
      clearTimeout(this.warningTimer);
      this.warningTimer = null;
    }

    if (confirmed) {
      // Usuario confirmó que sigue activo
      this.isWarningShown = false;
      this.resetActivity();
    } else {
      // Usuario eligió cerrar sesión
      this.performAutoLogout();
    }
  }

  /**
   * Realiza el logout automático por inactividad
   */
  private performAutoLogout(): void {
    this.clearTimers();
    this.stop();

    // Realizar logout y navegar al home
    this.sharedService.logout().subscribe({
      next: () => {
        this.router.navigate([APP_ROUTES.home]);
      },
      error: () => {
        // Incluso si falla, navegar al home
        this.router.navigate([APP_ROUTES.home]);
      }
    });
  }

  /**
   * Limpia todos los temporizadores
   */
  private clearTimers(): void {
    if (this.inactivityTimer) {
      clearTimeout(this.inactivityTimer);
      this.inactivityTimer = null;
    }

    if (this.warningTimer) {
      clearTimeout(this.warningTimer);
      this.warningTimer = null;
    }
  }
}
