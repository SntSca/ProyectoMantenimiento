package com.esimedia.features.auth.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.esimedia.features.auth.entity.UsuarioNormal;

@Repository
public interface UsuarioNormalRepository extends MongoRepository<UsuarioNormal, String> {
    
    // Buscar usuario por email (único)
    Optional<UsuarioNormal> findByemail(String email);
    
    // Buscar usuario por alias
    Optional<UsuarioNormal> findByalias(String alias);
    
    // Buscar usuarios por rol
    java.util.List<UsuarioNormal> findByrol(String rol);
    
    // Verificar si existe un email
    boolean existsByemail(String email);
    
    // Verificar si existe un alias
    boolean existsByalias(String alias);
}