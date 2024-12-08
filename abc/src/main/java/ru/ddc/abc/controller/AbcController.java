package ru.ddc.abc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AbcController {

    @GetMapping
    public ResponseEntity<?> get() {
        return ResponseEntity.ok("Hello Abc World");
    }
}
