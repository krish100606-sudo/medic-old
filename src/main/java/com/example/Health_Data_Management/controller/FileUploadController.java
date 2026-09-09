package com.example.Health_Data_Management.controller;


import com.example.Health_Data_Management.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    public FileUploadController(
            FileStorageService fileStorageService) {

        this.fileStorageService = fileStorageService;
    }


    // ---------------------------------------------------------
    // UPLOAD FILE
    // POST /api/files/upload
    // ---------------------------------------------------------

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file) {

        String fileName =
                fileStorageService.storeFile(file);

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "message",
                "File uploaded successfully"
        );

        response.put(
                "fileName",
                fileName
        );

        response.put(
                "originalFileName",
                file.getOriginalFilename()
        );

        response.put(
                "fileType",
                file.getContentType()
        );

        response.put(
                "fileSize",
                String.valueOf(file.getSize())
        );

        return ResponseEntity.ok(response);
    }


    // ---------------------------------------------------------
    // DELETE FILE
    // DELETE /api/files/{fileName}
    // ---------------------------------------------------------

    @DeleteMapping("/{fileName}")
    public ResponseEntity<String> deleteFile(
            @PathVariable String fileName) {

        fileStorageService.deleteFile(fileName);

        return ResponseEntity.ok(
                "File deleted successfully"
        );
    }
}
