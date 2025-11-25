import { Component } from '@angular/core';
import { ListasComponent } from '../../../../shared/components/listas/listas.component';
import { GESTOR_ROUTES } from '../../gestor.routes';

@Component({
  selector: 'app-listas-gestor',
  standalone: true,
  imports: [ListasComponent],
  templateUrl: './listas-gestor.component.html'
})
export class ListasGestorComponent {
  public readonly GESTOR_ROUTES = GESTOR_ROUTES;
}
