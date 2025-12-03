import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ListasComponent } from '../../../shared/components/listas/listas.component';
import { USUARIO_ROUTES } from '../../usuario.routes';

@Component({
  selector: 'app-listas-usuario',
  standalone: true,
  imports: [ListasComponent],
  templateUrl: './listas-usuario.component.html'
})
export class ListasUsuarioComponent implements OnInit {
  public readonly USUARIO_ROUTES = USUARIO_ROUTES;
  tipoLista: 'publicas' | 'privadas' = 'privadas'; // Por defecto muestra privadas

  constructor(private readonly route: ActivatedRoute) {}

  ngOnInit(): void {
    // Leer el query parameter 'tipo' para determinar qué listas mostrar
    this.route.queryParams.subscribe(params => {
      this.tipoLista = params['tipo'] === 'publicas' ? 'publicas' : 'privadas';
    });
  }
}
