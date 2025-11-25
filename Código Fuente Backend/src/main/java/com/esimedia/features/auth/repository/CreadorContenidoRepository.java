package com.esimedia.features.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.esimedia.features.auth.enums.TipoContenido;
import com.esimedia.features.auth.entity.CreadorContenido;

@Repository
public interface CreadorContenidoRepository extends MongoRepository<CreadorContenido, String> {
    
    /**
     * Busca un creador de contenido por su alias único
     * @param aliasCreador El alias del creador
     * @return Optional con el creador si existe
     */
    Optional<CreadorContenido> findByAliasCreador(String aliasCreador);
    
    /**
     * Busca creadores de contenido por tipo (VIDEO o AUDIO)
     * @param tipoContenido El tipo de contenido
     * @return Lista de creadores del tipo especificado
     */
    List<CreadorContenido> findByTipoContenido(TipoContenido tipoContenido);
    
    /**
     * Busca creadores de contenido por email (heredado de Usuario)
     * @param email El email del creador
     * @return Optional con el creador si existe
     */
    Optional<CreadorContenido> findByemail(String email);
    
    /**
     * Busca creadores de contenido por alias (heredado de Usuario)
     * @param alias El alias del usuario
     * @return Optional con el creador si existe
     */
    Optional<CreadorContenido> findByAlias(String alias);
    
    /**
     * Busca creadores de contenido que NO están bloqueados
     * @return Lista de creadores activos
     */
    List<CreadorContenido> findByBloqueadoFalse();
    
    /**
     * Busca creadores de contenido que están bloqueados
     * @return Lista de creadores bloqueados
     */
    List<CreadorContenido> findByBloqueadoTrue();
    
    /**
     * Busca creadores activos (no bloqueados) de un tipo específico
     * @param tipoContenido El tipo de contenido
     * @return Lista de creadores activos del tipo especificado
     */
    List<CreadorContenido> findByTipoContenidoAndBloqueadoFalse(TipoContenido tipoContenido);
    
    /**
     * Verifica si existe un creador con el alias especificado
     * @param aliasCreador El alias del creador
     * @return true si existe, false si no
     */
    boolean existsByAliasCreador(String aliasCreador);
    
    /**
     * Verifica si existe un creador con el email especificado
     * @param email El email del creador
     * @return true si existe, false si no
     */
    boolean existsByEmail(String email);
    
    /**
     * Verifica si existe un creador con el alias de usuario especificado
     * @param alias El alias del usuario
     * @return true si existe, false si no
     */
    boolean existsByAlias(String alias);
    
    /**
     * Cuenta el número de creadores por tipo
     * @param tipoContenido El tipo de contenido
     * @return Número de creadores del tipo especificado
     */
    long countByTipoContenido(TipoContenido tipoContenido);
    
    /**
     * Busca creadores por descripción que contenga una palabra clave
     * @param keyword La palabra clave a buscar
     * @return Lista de creadores cuya descripción contiene la palabra clave
     */
    @Query("{'descripcion': {$regex: ?0, $options: 'i'}}")
    List<CreadorContenido> findByDescripcionContainingIgnoreCase(String keyword);
    
    /**
     * Busca creadores por nombre o apellidos que contengan una palabra clave
     * @param keyword La palabra clave a buscar
     * @return Lista de creadores cuyo nombre o apellidos contienen la palabra clave
     */
    @Query("{ $or: [ {'nombre': {$regex: ?0, $options: 'i'}}, {'apellidos': {$regex: ?0, $options: 'i'}} ] }")
    List<CreadorContenido> findByNombreOrApellidosContainingIgnoreCase(String keyword);
    
    /**
     * Busca creadores activos ordenados por fecha de registro (más recientes primero)
     * @return Lista de creadores activos ordenados por fecha de registro descendente
     */
    @Query(value = "{'bloqueado': false}", sort = "{'fechaRegistro': -1}")
    List<CreadorContenido> findActiveCreadoresOrderByFechaRegistroDesc();
    
    /**
     * Busca creadores de un tipo específico ordenados por alias
     * @param tipoContenido El tipo de contenido
     * @return Lista de creadores del tipo especificado ordenados por aliasCreador
     */
    List<CreadorContenido> findByTipoContenidoOrderByAliasCreadorAsc(TipoContenido tipoContenido);
    
    /**
     * Busca creadores de contenido pendientes de validación (validado = false)
     * @return Lista de creadores pendientes de validación
     */
    List<CreadorContenido> findByValidadoFalse();
    
    /**
     * Busca creadores de contenido ya validados (validado = true)
     * @return Lista de creadores validados
     */
    List<CreadorContenido> findByValidadoTrue();
    
    /**
     * Busca creadores de contenido validados y no bloqueados
     * @return Lista de creadores validados y activos
     */
    List<CreadorContenido> findByValidadoTrueAndBloqueadoFalse();
}