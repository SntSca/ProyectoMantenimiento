package com.esimedia.features.content.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.esimedia.features.content.entity.ValoracionContenido;

import java.util.List;

@Repository
public interface ValoracionContenidoRepository extends MongoRepository<ValoracionContenido, String> {
    
    List<ValoracionContenido> findByIdContenido(String idContenido);
    
    ValoracionContenido findByIdContenidoAndIdUsuario(String idContenido, String idUsuario);
    
    List<ValoracionContenido> findByIdUsuario(String idUsuario);
    
    void deleteByIdUsuario(String idUsuario);
}