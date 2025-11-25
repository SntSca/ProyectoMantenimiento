import { Injectable } from '@angular/core';
import { HttpEvent, HttpInterceptor, HttpHandler, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { InactivityService } from '../services/inactivity.service';

/**
 * ===========================
 * INTERCEPTOR DE ACTIVIDAD HTTP
 * ===========================
 * 
 * Intercepta todas las peticiones HTTP para detectar actividad del usuario
 * y resetear el temporizador de inactividad.
 */
@Injectable()
export class InactivityInterceptor implements HttpInterceptor {
  
  constructor(private inactivityService: InactivityService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Resetear el temporizador de inactividad en cada petición HTTP
    return next.handle(req).pipe(
      tap(() => {
        this.inactivityService.resetActivity();
      })
    );
  }
}
