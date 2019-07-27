package com.github.jlefebure.minio;


import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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

@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfigurationProperties configurationProperties;

    /**
     * List all files at root path of the bucket
     *
     * @return List of all items at the root path of the bucket
     * @throws MinioException if an error occur while fetch list
     */
    public List<Item> list() throws MinioException {
        try {
            Iterable<Result<Item>> myObjects = minioClient.listObjects(configurationProperties.getBucket());
            return getItems(myObjects);
        } catch (XmlPullParserException e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * List all files at root path of the bucket
     *
     * @return List of all items at the root path of the bucket
     * @throws MinioException if an error occur while fetch list
     */
    public List<Item> list(Path path) throws MinioException {
        try {
            Iterable<Result<Item>> myObjects = minioClient.listObjects(configurationProperties.getBucket(), path.toString());
            return getItems(myObjects);
        } catch (XmlPullParserException e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

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
     * Get an object from Minio with the full path to the object
     *
     * @param path Path with all prefixes to the object
     * @return The object as an InputStream
     * @throws MinioException
     */
    public InputStream get(Path path) throws MinioException {
        try {
            return minioClient.getObject(configurationProperties.getBucket(), path.toString());
        } catch (XmlPullParserException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Get a file from Minio, and save it in the {@code fileName} file
     *
     * @param source
     * @param fileName
     * @throws MinioException
     */
    public void getAndSave(Path source, String fileName) throws MinioException {
        try {
            minioClient.getObject(configurationProperties.getBucket(), source.toString(), fileName);
        } catch (XmlPullParserException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Save a file to Minio
     *
     * @param source
     * @throws MinioException
     */
    public void put(Path source, InputStream file, ContentType contentType) throws MinioException {
        try {
            minioClient.putObject(configurationProperties.getBucket(), source.toString(), file, contentType.getMimeType());
        } catch (XmlPullParserException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Save a file to Minio
     *
     * @param source
     * @throws MinioException
     */
    public void put(Path source, InputStream file, String contentType) throws MinioException {
        try {
            minioClient.putObject(configurationProperties.getBucket(), source.toString(), file, contentType);
        } catch (XmlPullParserException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Remove a file to Minio
     *
     * @param source
     * @throws MinioException
     */
    public void remove(Path source) throws MinioException {
        try {
            minioClient.removeObject(configurationProperties.getBucket(), source.toString());
        } catch (XmlPullParserException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

}
