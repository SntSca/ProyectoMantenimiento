package com.esimedia.features.content.http;

import com.esimedia.features.content.entity.Tags;
import com.esimedia.features.content.repository.TagsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagsController {

    private final TagsRepository tagsRepository;

    @GetMapping
    public ResponseEntity<List<Tags>> getAllTags() {
        List<Tags> tags = tagsRepository.findAll();
        return ResponseEntity.ok(tags);
    }
}
