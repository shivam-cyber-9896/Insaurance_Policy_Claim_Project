package com.monocept.app.service.implementation;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class CloudinaryServiceImple  {

    private final Cloudinary cloudinary;

    

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains("."))
            ? originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase()
            : "";

        String resourceType = switch (extension) {
            case "jpg", "jpeg", "png", "gif", "webp" -> "image";
            case "mp4", "mov", "avi"                 -> "video";
            default                                   -> "raw";
        };

        String publicId = folder + "/" + UUID.randomUUID().toString();

        Map<?, ?> result = cloudinary.uploader().upload(
            file.getBytes(),
            ObjectUtils.asMap(
                "public_id",       publicId,
                "resource_type",   resourceType,
                "access_mode",     "public",
                "format",          extension,    // force the extension
                "use_filename",    false,
                "unique_filename", false
            )
        );
        return (String) result.get("secure_url");
    }
    public void deleteFile(String publicUrl) throws IOException {
        String publicId = extractPublicId(publicUrl);
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    private String extractPublicId(String url) {
        // e.g. https://res.cloudinary.com/demo/image/upload/v123/claims/abc.pdf
        // extracts: claims/abc
        String[] parts = url.split("/upload/");
        String afterUpload = parts[1];
        // strip version segment if present (v1234567/)
        afterUpload = afterUpload.replaceFirst("v[0-9]+/", "");
        // strip file extension
        int dotIndex = afterUpload.lastIndexOf('.');
        return dotIndex != -1 ? afterUpload.substring(0, dotIndex) : afterUpload;
    }
}