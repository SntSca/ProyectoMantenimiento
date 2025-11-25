package com.esimedia.features.content.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.esimedia.features.content.entity.ContenidosAudio;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContenidosAudioRepository extends MongoRepository<ContenidosAudio, String> {
    
    // Buscar por título
    Optional<ContenidosAudio> findByTitulo(String titulo);
    
    // Buscar por creador
    List<ContenidosAudio> findByIdCreador(String idCreador);
    
    // Buscar por VIP
    List<ContenidosAudio> findByEsVIP(boolean esVIP);
    
    // Verificar si existe por título
    boolean existsByTitulo(String titulo);
    

}