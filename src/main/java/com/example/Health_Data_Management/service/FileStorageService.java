package com.example.Health_Data_Management.service;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(
            @Value("${file.upload-dir:uploads/reports}")
            String uploadDir) {

        try {

            this.fileStorageLocation =
                    Paths.get(uploadDir)
                            .toAbsolutePath()
                            .normalize();

            Files.createDirectories(
                    this.fileStorageLocation
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Could not create upload directory",
                    e
            );
        }
    }


    // ---------------------------------------------------------
    // STORE FILE
    // ---------------------------------------------------------

    public String storeFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {

            throw new RuntimeException(
                    "Cannot upload an empty file"
            );
        }

        String originalFileName =
                StringUtils.cleanPath(
                        file.getOriginalFilename()
                );

        if (originalFileName == null ||
                originalFileName.isBlank()) {

            throw new RuntimeException(
                    "Invalid file name"
            );
        }


        // Prevent unsafe file names
        if (originalFileName.contains("..")) {

            throw new RuntimeException(
                    "Invalid file name: "
                            + originalFileName
            );
        }


        // Generate unique file name
        String extension = "";

        int lastDot =
                originalFileName.lastIndexOf(".");

        if (lastDot >= 0) {
            extension =
                    originalFileName.substring(lastDot);
        }

        String newFileName =
                UUID.randomUUID()
                        .toString()
                        + extension;


        try {

            Path targetLocation =
                    fileStorageLocation
                            .resolve(newFileName)
                            .normalize();


            // Make sure file stays inside upload directory
            if (!targetLocation.startsWith(
                    fileStorageLocation)) {

                throw new RuntimeException(
                        "Invalid file path"
                );
            }


            Files.copy(
                    file.getInputStream(),
                    targetLocation,
                    StandardCopyOption.REPLACE_EXISTING
            );


            return newFileName;

        } catch (IOException e) {

            throw new RuntimeException(
                    "Could not store file "
                            + originalFileName,
                    e
            );
        }
    }


    // ---------------------------------------------------------
    // DELETE FILE
    // ---------------------------------------------------------

    public void deleteFile(String fileName) {

        if (fileName == null ||
                fileName.isBlank()) {

            return;
        }

        try {

            Path filePath =
                    fileStorageLocation
                            .resolve(fileName)
                            .normalize();

            if (!filePath.startsWith(
                    fileStorageLocation)) {

                throw new RuntimeException(
                        "Invalid file path"
                );
            }

            Files.deleteIfExists(filePath);

        } catch (IOException e) {

            throw new RuntimeException(
                    "Could not delete file "
                            + fileName,
                    e
            );
        }
    }


    // ---------------------------------------------------------
    // GET FILE PATH
    // ---------------------------------------------------------

    public Path getFilePath(String fileName) {

        if (fileName == null ||
                fileName.isBlank()) {

            throw new RuntimeException(
                    "File name cannot be empty"
            );
        }

        Path filePath =
                fileStorageLocation
                        .resolve(fileName)
                        .normalize();

        if (!filePath.startsWith(
                fileStorageLocation)) {

            throw new RuntimeException(
                    "Invalid file path"
            );
        }

        return filePath;
    }
}
