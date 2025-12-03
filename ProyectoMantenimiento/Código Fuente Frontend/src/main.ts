import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';
import { AppComponent } from './app/app.component';
import { PantallaInicioComponent } from './app/shared/components/pantalla-inicio/pantalla-inicio.component';
import { ResetPasswordComponent } from './app/shared/components/reset-password/reset-password.component';
import { VerifyEmailComponent } from './app/shared/components/verify-email/verify-email.component';
import { QrCodeComponent } from './app/shared/components/qr-code/qr-code.component';
import { InactivityInterceptor } from './app/shared/interceptors/inactivity.interceptor';

const routes = [
  { path: '', component: PantallaInicioComponent },
  { path: 'verify-email', component: VerifyEmailComponent },
  { path: 'third-factor', component: QrCodeComponent },
  { path: 'reset-password/:resetToken', component: ResetPasswordComponent },
  // Lazy loading de rutas standalone (en lugar de módulos)
  {
    path: 'usuario',
    loadChildren: () => import('./app/usuario/usuario.routes').then(m => m.usuarioRoutes)
  },
  {
    path: 'personal',
    loadChildren: () => import('./app/personal/personal.routes').then(m => m.personalRoutes)
  }
  // Agrega más rutas según necesites, apuntando a componentes standalone
];

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes), 
    provideHttpClient(withInterceptorsFromDi()),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: InactivityInterceptor,
      multi: true
    }
  ]
})
  .catch(err => console.error(err));
