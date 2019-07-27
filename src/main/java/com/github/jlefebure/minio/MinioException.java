package com.github.jlefebure.minio;


/**
 * Wrapper exception for all Minio errors that occurs while fetching, removing, uploading an object to Minio.
 */
public class MinioException extends Exception {
    public MinioException(String message, Throwable cause) {
        super(message, cause);
    }
}
