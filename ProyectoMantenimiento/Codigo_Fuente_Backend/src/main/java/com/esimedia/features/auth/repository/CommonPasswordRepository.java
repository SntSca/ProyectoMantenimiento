package com.esimedia.features.auth.repository;

import com.esimedia.features.auth.entity.CommonPassword;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para acceder a las contraseñas comunes hasheadas.
 */
@Repository
public interface CommonPasswordRepository extends MongoRepository<CommonPassword, String> {

    /**
     * Busca una contraseña común por su hash.
     * @param hash El hash SHA-256 de la contraseña
     * @return Optional con la CommonPassword si existe
     */
    Optional<CommonPassword> findByHash(String hash);

    /**
     * Verifica si existe una contraseña común con el hash dado.
     * @param hash El hash SHA-256 de la contraseña
     * @return true si existe, false en caso contrario
     */
    boolean existsByHash(String hash);
}