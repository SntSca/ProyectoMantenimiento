package com.esimedia.features.lists.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO para actualizar campos principales de una lista de reproducción privada
 * (nombre, descripción) - NO incluye modificación de contenidos ni visibilidad
 * Las listas privadas no tienen campo de visibilidad ya que siempre son privadas
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListaUpdateFieldsPrivadasDTO extends ListaUpdateFieldsDTO {

    // No tiene campo visibilidad - las listas privadas siempre son privadas
}