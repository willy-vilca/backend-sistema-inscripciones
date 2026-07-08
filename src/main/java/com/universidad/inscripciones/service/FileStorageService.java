package com.universidad.inscripciones.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final Path uploadRoot;

    public FileStorageService(@Value("${app.upload-dir}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file, String folder, String prefix) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            Path targetFolder = uploadRoot.resolve(folder).normalize();
            Files.createDirectories(targetFolder);

            String original = StringUtils.cleanPath(file.getOriginalFilename() == null
                    ? "archivo"
                    : file.getOriginalFilename());
            String extension = "";
            int dotIndex = original.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = original.substring(dotIndex);
            }

            String safePrefix = prefix.replaceAll("[^a-zA-Z0-9-_]", "_");
            String filename = safePrefix + "-" + UUID.randomUUID() + extension;
            Path target = targetFolder.resolve(filename).normalize();

            if (!target.startsWith(targetFolder)) {
                throw new IllegalArgumentException("Nombre de archivo no valido.");
            }

            file.transferTo(target);
            return uploadRoot.relativize(target).toString().replace("\\", "/");
        } catch (IOException ex) {
            throw new IllegalArgumentException("No se pudo guardar el archivo " + file.getOriginalFilename(), ex);
        }
    }
}
