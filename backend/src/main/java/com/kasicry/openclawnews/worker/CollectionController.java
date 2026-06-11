package com.kasicry.openclawnews.worker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/news")
public class CollectionController {

    private final CollectionService collectionService;
    private final boolean collectEnabled;

    public CollectionController(
            CollectionService collectionService,
            @Value("${operations.collect-enabled:false}") boolean collectEnabled
    ) {
        this.collectionService = collectionService;
        this.collectEnabled = collectEnabled;
    }

    @PostMapping("/collect")
    public WorkerCollectResponse collect(@Valid @RequestBody WorkerCollectRequest request) {
        if (!collectEnabled) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Collection operation requires explicit approval and configuration"
            );
        }
        return collectionService.collectAndSave(request);
    }
}
