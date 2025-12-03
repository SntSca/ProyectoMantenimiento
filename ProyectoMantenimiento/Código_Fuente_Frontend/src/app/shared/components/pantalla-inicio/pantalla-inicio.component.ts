import { Component, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { SharedService, USUARIO_ROUTES, GESTOR_ROUTES, PERSONAL_ROUTES } from '@shared';


@Component({
  selector: 'app-pantalla-inicio',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './pantalla-inicio.component.html',

})
export class PantallaInicioComponent implements OnInit {

  // Constantes de rutas para usar en templates
  public readonly USUARIO_ROUTES = USUARIO_ROUTES;
  public readonly GESTOR_ROUTES = GESTOR_ROUTES;
  public readonly PERSONAL_ROUTES = PERSONAL_ROUTES;

  constructor(private readonly router: Router, private readonly sharedService: SharedService) { }

  ngOnInit(): void {
    this.sharedService.clearSession();
  }
}
