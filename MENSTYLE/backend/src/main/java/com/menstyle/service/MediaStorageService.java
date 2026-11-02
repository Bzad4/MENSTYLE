package com.menstyle.service;

import com.menstyle.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.*;
import java.util.*;

@Service
public class MediaStorageService {
    private static final long MAX_BYTES = 5 * 1024 * 1024;
    private final Path directory;
    public MediaStorageService(@Value("${menstyle.media.upload-dir:uploads}") String directory) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }
    public String storeImage(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > MAX_BYTES) throw new BadRequestException("Image must be between 1 byte and 5 MB");
        String contentType = Optional.ofNullable(file.getContentType()).orElse("");
        if (!Set.of("image/jpeg", "image/png", "image/webp").contains(contentType)) throw new BadRequestException("Only JPEG, PNG and WebP images are allowed");
        try (InputStream input = file.getInputStream()) {
            byte[] header = input.readNBytes(12);
            if (!validSignature(contentType, header)) throw new BadRequestException("Image content does not match its MIME type");
            Files.createDirectories(directory);
            String extension = contentType.equals("image/png") ? ".png" : contentType.equals("image/webp") ? ".webp" : ".jpg";
            String filename = UUID.randomUUID() + extension;
            Path target = directory.resolve(filename).normalize();
            if (!target.getParent().equals(directory)) throw new BadRequestException("Invalid image path");
            file.transferTo(target);
            return "/media/" + filename;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not store image", exception);
        }
    }
    private boolean validSignature(String type, byte[] h) {
        if (type.equals("image/png")) return h.length >= 8 && h[0] == (byte) 0x89 && h[1] == 0x50 && h[2] == 0x4E && h[3] == 0x47;
        if (type.equals("image/webp")) return h.length >= 12 && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F' && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P';
        return h.length >= 3 && h[0] == (byte) 0xFF && h[1] == (byte) 0xD8 && h[2] == (byte) 0xFF;
    }
}
