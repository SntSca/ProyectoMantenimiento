import { Routes } from '@angular/router';
import { PerfilAdministradorComponent } from './componentes/perfil-administrador/perfil-administrador.component';
import { InicioAdministradorComponent } from './componentes/inicio-administrador/inicio-administrador.component';
import { ListasAdministradorComponent } from './componentes/listas-administrador/listas-administrador.component';
import { AuthGuard } from '../../shared/guards/auth.guard';

export const ADMINISTRADOR_ROUTES = {
  perfil: '/personal/administrador/perfil',
  inicio: '/personal/administrador/inicio',
  listas: '/personal/administrador/listas'
};

export const administradorRoutes: Routes = [
  { path: 'perfil', component: PerfilAdministradorComponent, canActivate: [AuthGuard] },
  { path: 'inicio', component: InicioAdministradorComponent, canActivate: [AuthGuard] },
  { path: 'listas', component: ListasAdministradorComponent, canActivate: [AuthGuard] },
  { path: '', redirectTo: 'inicio', pathMatch: 'full' }
];