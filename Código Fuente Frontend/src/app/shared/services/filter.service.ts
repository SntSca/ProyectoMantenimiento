import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class FilterService {

  constructor() { }

  /**
   * Filtra elementos por búsqueda en campos específicos según la sección
   */
  filtrarPorBusqueda(base: any[], termino: string, seccion: string): any[] {
    if (!termino.trim()) {
      return base;
    }
    
    switch (seccion) {
      case 'usuarios':
        return base.filter((user: any) =>
          user.nombre.toLowerCase().includes(termino.toLowerCase()) ||
          user.primerApellido.toLowerCase().includes(termino.toLowerCase()) ||
          user.segundoApellido?.toLowerCase().includes(termino.toLowerCase()) ||
          user.email.toLowerCase().includes(termino.toLowerCase()) ||
          user.alias.toLowerCase().includes(termino.toLowerCase())
        );
      case 'gestores':
        return base.filter((gestor: any) =>
          gestor.nombre.toLowerCase().includes(termino.toLowerCase()) ||
          gestor.primerApellido.toLowerCase().includes(termino.toLowerCase()) ||
          gestor.segundoApellido?.toLowerCase().includes(termino.toLowerCase()) ||
          gestor.email.toLowerCase().includes(termino.toLowerCase()) ||
          gestor.alias.toLowerCase().includes(termino.toLowerCase())
        );
      case 'administradores':
        return base.filter((admin: any) =>
          admin.nombre.toLowerCase().includes(termino.toLowerCase()) ||
          admin.primerApellido.toLowerCase().includes(termino.toLowerCase()) ||
          admin.segundoApellido?.toLowerCase().includes(termino.toLowerCase()) ||
          admin.email.toLowerCase().includes(termino.toLowerCase()) ||
          admin.alias.toLowerCase().includes(termino.toLowerCase()) 
        );
      default:
        return base.filter((contenido: any) =>
          contenido.titulo.toLowerCase().includes(termino.toLowerCase())
        );
    }
  }

  /**
   * Aplica filtros a una lista base
   */
  aplicarFiltros(base: any[], filtros: any): any[] {
    let filtrados = base;

    // Aplicar filtros específicos
    if (filtros.filtroVip != null) {
      filtrados = filtrados.filter((user: any) => user.usuarioVip === filtros.filtroVip);
    }

    if (filtros.filtroEstado != null) {
      filtrados = filtrados.filter((item: any) => item.bloqueado === filtros.filtroEstado);
    }

    if (filtros.filtroValidacion != null) {
      filtrados = filtrados.filter((gestor: any) => gestor.validado === filtros.filtroValidacion);
    }

    if (filtros.filtroEspecialidadGestores) {
      filtrados = filtrados.filter((gestor: any) => gestor.especialidad === filtros.filtroEspecialidadGestores);
    }

    if (filtros.filtroTipoContenido) {
        filtrados = filtrados.filter((gestor: any) => gestor.tipoContenido === filtros.filtroTipoContenido);
    }
    
    if (filtros.filtroDepartamento) {
      filtrados = filtrados.filter((admin: any) => admin.departamento === filtros.filtroDepartamento);
    }

    if (filtros.filtroTagsContenidos && filtros.filtroTagsContenidos.length > 0) {
      filtrados = filtrados.filter((contenido: any) =>
        filtros.filtroTagsContenidos.some((tag: string) => contenido.tags?.includes(tag))
      );
    }

    if (filtros.filtroTipoContenidoContenidos) {
      filtrados = filtrados.filter((contenido: any) => contenido.tipo === filtros.filtroTipoContenidoContenidos);
    }

    if (filtros.filtroRestriccionEdad != null) {
      filtrados = filtrados.filter((contenido: any) => contenido.restriccionEdad <= filtros.filtroRestriccionEdad);
    }

    if(filtros.filtroVisibilidadContenidos != null) {
      filtrados = filtrados.filter((contenido: any) => contenido.visibilidad === filtros.filtroVisibilidadContenidos);
    }

    return filtrados;
  }
}