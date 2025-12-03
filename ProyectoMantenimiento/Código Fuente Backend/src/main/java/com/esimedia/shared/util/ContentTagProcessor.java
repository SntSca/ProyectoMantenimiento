package com.esimedia.shared.util;

import java.util.List;
import java.util.function.BiFunction;

import com.esimedia.features.content.entity.Tags;
import com.esimedia.features.content.repository.TagsRepository;

/**
 * Procesador genérico para tags de contenido multimedia.
 * Permite reutilizar la lógica de procesamiento de tags para diferentes tipos de contenido.
 */
public class ContentTagProcessor {

    private final TagsRepository tagRepository;
    private final BiFunction<String, String, Object> tagRelationFactory;

    /**
     * Constructor que recibe el repositorio de tags y una fábrica para crear relaciones contenido-tag.
     * @param tagRepository Repositorio para buscar/crear tags.
     * @param tagRelationFactory Función que crea la relación (contenidoId, tagId) -> relación entity.
     */
    public ContentTagProcessor(TagsRepository tagRepository, BiFunction<String, String, Object> tagRelationFactory) {
        this.tagRepository = tagRepository;
        this.tagRelationFactory = tagRelationFactory;
    }

    /**
     * Procesa una lista de nombres de tags para un contenido específico.
     * Busca o crea cada tag y crea la relación correspondiente.
     * @param contenidoId ID del contenido.
     * @param tagNames Lista de nombres de tags.
     * @param relationSaver Función para guardar la relación (debe manejar el tipo específico).
     */
    public void processContentTags(String contenidoId, List<String> tagNames, java.util.function.Consumer<Object> relationSaver) {

        for (String tagName : tagNames) {
            // Buscar o crear tag
            Tags tag = tagRepository.findByNombre(tagName.trim())
                    .orElseGet(() -> {
                        Tags newTag = new Tags(tagName.trim());
                        return tagRepository.save(newTag);
                    });

            // Crear relación contenido-tag
            Object relation = tagRelationFactory.apply(contenidoId, tag.getIdTag());
            relationSaver.accept(relation);
        }
    }

    /**
     * Actualiza las tags de un contenido específico.
     * Elimina relaciones para tags que ya no están, añade nuevas relaciones para tags nuevos.
     * @param contenidoId ID del contenido.
     * @param newTagNames Lista de nombres de tags nuevos.
     * @param currentTagIdsGetter Función que devuelve la lista de tagIds actuales.
     * @param relationDeleter Función que elimina la relación para un tagId dado.
     * @param relationSaver Función para guardar nuevas relaciones.
     */
    public void updateContentTags(String contenidoId, List<String> newTagNames, 
                                  java.util.function.Supplier<List<String>> currentTagIdsGetter,
                                  java.util.function.Consumer<String> relationDeleter,
                                  java.util.function.Consumer<Object> relationSaver) {
        if (newTagNames == null) {
            newTagNames = List.of();
        } 

        // Obtener tagIds actuales
        List<String> currentTagIds = currentTagIdsGetter.get();

        // Mapear nuevos tagNames a tagIds, creando tags si no existen
        List<String> newTagIds = newTagNames.stream()
            .map(tagName -> tagRepository.findByNombre(tagName.trim())
                    .orElseGet(() -> {
                        Tags newTag = new Tags(tagName.trim());
                        return tagRepository.save(newTag);
                    }))
            .map(Tags::getIdTag)
            .toList();

        // Tags a eliminar: actuales que no están en nuevos
        List<String> tagsToRemove = currentTagIds.stream()
            .filter(tagId -> !newTagIds.contains(tagId))
            .toList();

        // Tags a añadir: nuevos que no están en actuales
        List<String> tagsToAdd = newTagIds.stream()
            .filter(tagId -> !currentTagIds.contains(tagId))
            .toList();

        // Eliminar relaciones obsoletas
        tagsToRemove.forEach(relationDeleter);

        // Añadir nuevas relaciones
        tagsToAdd.forEach(tagId -> {
            Object relation = tagRelationFactory.apply(contenidoId, tagId);
            relationSaver.accept(relation);
        });
    }
}