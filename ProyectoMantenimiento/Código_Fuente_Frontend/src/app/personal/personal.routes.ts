import { Routes } from '@angular/router';
import { RecuperarPasswordPersonalComponent } from './componentes/recuperar-password/recuperar-password.component';

export const PERSONAL_ROUTES = {
  login: '/personal/login',
  gestor: '/personal/gestor',
  administrador: '/personal/administrador',
  recuperarPassword: '/personal/recuperar-password'
};

export const personalRoutes: Routes = [
  {
    path: 'gestor',
    loadChildren: () => import('./gestor/gestor.routes').then(m => m.gestorRoutes)
  },
  {
    path: 'administrador',
    loadChildren: () => import('./administrador/administrador.routes').then(m => m.administradorRoutes)
  },
  { path: 'login', loadComponent: () => import('./componentes/login/login-personal.component').then(m => m.LoginPersonalComponent) },
  { path: 'recuperar-password', component: RecuperarPasswordPersonalComponent },
  // Redirección por defecto a login
  { path: '', redirectTo: 'login', pathMatch: 'full' }
];