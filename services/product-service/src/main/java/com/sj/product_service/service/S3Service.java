package com.sj.product_service.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface S3Service {

    /**
     * Uploads a file to an S3 bucket and returns the object's key.
     *
     * @param bucketName The name of the S3 bucket.
     * @param key The key (path and filename) for the new object in S3.
     * @param file The MultipartFile to upload.
     * @return The S3 object key of the uploaded file.
     * @throws IOException If an I/O error occurs during file processing.
     */
    String uploadFile(String bucketName, String key, MultipartFile file) throws IOException;

    /**
     * Constructs the full public or CDN URL for an S3 object.
     *
     * @param key The S3 object key.
     * @return The fully qualified URL.
     */
}