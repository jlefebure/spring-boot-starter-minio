package com.github.jlefebure.minio;


/**
 * Runtime exception thrown when an error occur while fetching a list of objects.
 */
public class MinioFetchException extends RuntimeException{
    public MinioFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
