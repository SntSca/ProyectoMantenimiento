import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { SharedService } from '../services/shared.services';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private readonly sharedService: SharedService, private readonly router: Router) {}

  canActivate(): boolean {
    const token = sessionStorage.getItem('token') ?? sessionStorage.getItem('authToken');
    if (token) {
      // Verificar si el token es válido
      try {
        const decoded: any = this.sharedService.decodeJWT(token);
        if (decoded && decoded.exp > Date.now() / 1000) {
          return true;
        }
      } catch (error) {
        console.error('Token inválido:', error);
      }
    }
    // Si no hay token o es inválido, redirigir al inicio
    this.router.navigate(['/']);
    return false;
  }
}