import { Routes } from '@angular/router';
import { RegistroGestorComponent } from './componentes/registro-gestor/registro-gestor.component';
import { PerfilGestorComponent } from './componentes/perfil-gestor/perfil-gestor.component';
import { InicioGestorComponent } from './componentes/inicio-gestor/inicio-gestor.component';
import { SubirContenidoComponent } from './componentes/subir-contenido/subir-contenido.component';
import { CrearListaPublicaComponent } from './componentes/crear-lista-publica/crear-lista-publica.component';
import { ListasGestorComponent } from './componentes/listas-gestor/listas-gestor.component';
import { AuthGuard } from '../../shared/guards/auth.guard';

export const GESTOR_ROUTES = {
  registro: '/personal/gestor/registro',
  perfil: '/personal/gestor/perfil',
  inicio: '/personal/gestor/inicio',
  subirContenido: '/personal/gestor/subir-contenido',
  crearListaPublica: '/personal/gestor/crear-lista',
  editarListaPublica: '/personal/gestor/editar-lista',
  listas: '/personal/gestor/listas'
};

export const gestorRoutes: Routes = [
  { path: 'registro', component: RegistroGestorComponent },
  { path: 'perfil', component: PerfilGestorComponent, canActivate: [AuthGuard] },
  { path: 'inicio', component: InicioGestorComponent, canActivate: [AuthGuard] },
  { path: 'subir-contenido', component: SubirContenidoComponent, canActivate: [AuthGuard] },
  { path: 'crear-lista', component: CrearListaPublicaComponent, canActivate: [AuthGuard] },
  { path: 'editar-lista/:id', component: CrearListaPublicaComponent, canActivate: [AuthGuard] },
  { path: 'listas', component: ListasGestorComponent, canActivate: [AuthGuard] },
  { path: '', redirectTo: 'inicio', pathMatch: 'full' }
];