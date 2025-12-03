package com.esimedia.features.auth.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entidad que representa una contraseña común hasheada en la base de datos.
 * Se utiliza para verificar contraseñas débiles durante el registro.
 */
@Document(collection = "common_passwords")
public class CommonPassword {

    @Id
    private String id;

    private String hash;

    // Constructor vacío para MongoDB
    public CommonPassword() {}

    public CommonPassword(String hash) {
        this.hash = hash;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        return "CommonPassword{" +
                "id='" + id + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }
}