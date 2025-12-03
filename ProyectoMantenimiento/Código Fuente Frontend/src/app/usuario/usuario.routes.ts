import { Routes } from '@angular/router';
import { LoginUsuarioComponent } from './componentes/login/login-usuario.component';
import { RegistroUsuarioComponent } from './componentes/registro-usuario/registro-usuario.component';
import { PerfilUsuarioComponent } from './componentes/perfil-usuario/perfil-usuario.component';
import { InicioUsuarioComponent } from './componentes/inicio-usuario/inicio-usuario.component';
import { VisualizarContenidoComponent } from './componentes/visualizar-contenido/visualizar-contenido.component';
import { RecuperarPasswordUsuarioComponent } from './componentes/recuperar-password/recuperar-password.component';
import { CrearListaPrivadaComponent } from './componentes/crear-lista-privada/crear-lista-privada.component';
import { ListasUsuarioComponent } from './componentes/listas-usuario/listas-usuario.component';
import { AuthGuard } from '../shared/guards/auth.guard';

export const USUARIO_ROUTES = {
  login: '/usuario/login',
  registro: '/usuario/registro',
  perfil: '/usuario/perfil',
  inicio: '/usuario/inicio',
  visualizar: '/usuario/visualizar',
  recuperarPassword: '/usuario/recuperar-password',
  crearListaPrivada: '/usuario/crear-lista',
  editarListaPrivada: '/usuario/editar-lista',
  listas: '/usuario/listas'
};

export const usuarioRoutes: Routes = [
  { path: 'login', component: LoginUsuarioComponent },
  { path: 'registro', component: RegistroUsuarioComponent },
  { path: 'perfil', component: PerfilUsuarioComponent, canActivate: [AuthGuard] },
  { path: 'inicio', component: InicioUsuarioComponent, canActivate: [AuthGuard] },
  { path: 'visualizar/:tipo/:id', component: VisualizarContenidoComponent, canActivate: [AuthGuard] },
  { path: 'recuperar-password', component: RecuperarPasswordUsuarioComponent },
  { path: 'crear-lista', component: CrearListaPrivadaComponent, canActivate: [AuthGuard] },
  { path: 'editar-lista/:id', component: CrearListaPrivadaComponent, canActivate: [AuthGuard] },
  { path: 'listas', component: ListasUsuarioComponent, canActivate: [AuthGuard] },
  // Redirección por defecto al inicio
  { path: '', redirectTo: 'inicio', pathMatch: 'full' }
];