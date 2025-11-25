package com.esimedia.features.auth.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.esimedia.features.auth.entity.Administrador;

public interface AdminRepository extends MongoRepository<Administrador, String> {
    Optional<Administrador> findByemail(String email);
    Optional<Administrador> findByalias(String alias);

    boolean existsByEmail(String email);
    boolean existsByAlias(String alias);
}
