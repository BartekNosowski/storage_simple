package com.example.storage.controller;

import com.example.storage.model.LocalFile;
import com.example.storage.service.LocalFileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
//@RequestMapping nie jest potrzebne bo mam wpisany servlet w app properties
public class LocalFileController {

    private LocalFileService localFileService;

    public LocalFileController(LocalFileService localFileService) {
        this.localFileService = localFileService;
    }
    @GetMapping("/files")
    // getFiles serwuje ścieżki do pobrania plików
    public List<LocalFile> getFiles(){
        return localFileService.getFiles();
    }
    // {} zmienna ścieżki, pathvariable
    @GetMapping("/files/download/{filename}")
    // getFile pobiera plik z dysku twardego
    public ResponseEntity<?> getFile(@PathVariable String filename){
        return localFileService.getFile(filename);
    }

    @GetMapping("/files/delete/{filename}") // tu powinien być @DeleteMapping
    // getFile usuwa plik z dysku twardego
    public ResponseEntity<?> deleteFile(@PathVariable String filename){
        return localFileService.deleteFile(filename);
    }


}
