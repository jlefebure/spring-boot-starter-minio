/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.github.jlefebure.minio;


import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


/**
 * Service class to interact with Minio bucket. This class is register as a bean and use the properties defined in {@link MinioConfigurationProperties}.
 * All methods return an {@link com.github.jlefebure.minio.MinioException} which wrap the Minio SDK exception.
 * The bucket name is provided with the one defined in the configuration properties.
 */
@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfigurationProperties configurationProperties;

    /**
     * List all objects at root of the bucket
     *
     * @return List of items
     * @throws com.github.jlefebure.minio.MinioException if an error occur while fetch list
     */
    public List<Item> list() throws com.github.jlefebure.minio.MinioException {
        try {
            Iterable<Result<Item>> myObjects = minioClient.listObjects(configurationProperties.getBucket());
            return getItems(myObjects);
        } catch (XmlPullParserException e) {
            throw new com.github.jlefebure.minio.MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * List all objects with the prefix given in parameter for the bucket
     *
     * @return List of items
     * @throws com.github.jlefebure.minio.MinioException if an error occur while fetch list
     */
    public List<Item> list(Path path) throws com.github.jlefebure.minio.MinioException {
        try {
            Iterable<Result<Item>> myObjects = minioClient.listObjects(configurationProperties.getBucket(), path.toString());
            return getItems(myObjects);
        } catch (XmlPullParserException e) {
            throw new com.github.jlefebure.minio.MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Utility method which map results to items and return a list
     * 
     * @param myObjects Iterable of results
     * @return List of items
     */
    private List<Item> getItems(Iterable<Result<Item>> myObjects) {
        return StreamSupport
                .stream(myObjects.spliterator(), true)
                .map(itemResult -> {
                    try {
                        return itemResult.get();
                    } catch (InvalidBucketNameException |
                            NoSuchAlgorithmException |
                            InsufficientDataException |
                            IOException |
                            InvalidKeyException |
                            NoResponseException |
                            XmlPullParserException |
                            ErrorResponseException |
                            InternalException e) {
                        throw new MinioFetchException("Error while parsing list of objects", e);
                    }
                })
                .collect(Collectors.toList());
    }


    /**
     * Get an object from Minio
     *
     * @param path Path with prefix to the object. Object name must be included.
     * @return The object as an InputStream
     * @throws com.github.jlefebure.minio.MinioException if an error occur while fetch object
     */
    public InputStream get(Path path) throws com.github.jlefebure.minio.MinioException {
        try {
            return minioClient.getObject(configurationProperties.getBucket(), path.toString());
        } catch (XmlPullParserException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException e) {
            throw new com.github.jlefebure.minio.MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Get a file from Minio, and save it in the {@code fileName} file
     *
     * @param source Path with prefix to the object. Object name must be included.
     * @param fileName Filename
     * @throws com.github.jlefebure.minio.MinioException if an error occur while fetch object
     */
    public void getAndSave(Path source, String fileName) throws com.github.jlefebure.minio.MinioException {
        try {
            minioClient.getObject(configurationProperties.getBucket(), source.toString(), fileName);
        } catch (XmlPullParserException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException e) {
            throw new com.github.jlefebure.minio.MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Upload a file to Minio
     * @param source Path with prefix to the object. Object name must be included.
     * @param file File as an inputstream
     * @param contentType MIME type for the object
     * @throws com.github.jlefebure.minio.MinioException if an error occur while uploading object
     */
    public void upload(Path source, InputStream file, ContentType contentType) throws com.github.jlefebure.minio.MinioException {
        try {
            minioClient.putObject(configurationProperties.getBucket(), source.toString(), file, contentType.getMimeType());
        } catch (XmlPullParserException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException e) {
            throw new com.github.jlefebure.minio.MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Upload a file to Minio
     * @param source Path with prefix to the object. Object name must be included.
     * @param file File as an inputstream
     * @param contentType MIME type for the object
     * @throws com.github.jlefebure.minio.MinioException if an error occur while uploading object
     */
    public void upload(Path source, InputStream file, String contentType) throws com.github.jlefebure.minio.MinioException {
        try {
            minioClient.putObject(configurationProperties.getBucket(), source.toString(), file, contentType);
        } catch (XmlPullParserException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException e) {
            throw new com.github.jlefebure.minio.MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Remove a file to Minio
     *
     * @param source Path with prefix to the object. Object name must be included.
     * @throws com.github.jlefebure.minio.MinioException if an error occur while uploading object
     */
    public void remove(Path source) throws com.github.jlefebure.minio.MinioException {
        try {
            minioClient.removeObject(configurationProperties.getBucket(), source.toString());
        } catch (XmlPullParserException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

}
