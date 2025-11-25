import { Component } from '@angular/core';
import { ListasComponent } from '../../../../shared/components/listas/listas.component';
import { ADMINISTRADOR_ROUTES } from '../../administrador.routes';

@Component({
  selector: 'app-listas-administrador',
  standalone: true,
  imports: [ListasComponent],
  templateUrl: './listas-administrador.component.html'
})
export class ListasAdministradorComponent {
  public readonly ADMINISTRADOR_ROUTES = ADMINISTRADOR_ROUTES;
}
