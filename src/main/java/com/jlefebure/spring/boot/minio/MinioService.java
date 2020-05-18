/*
 * Copyright Jordan LEFEBURE © 2019.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.jlefebure.spring.boot.minio;


import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.PutObjectOptions;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


/**
 * Service class to interact with Minio bucket. This class is register as a bean and use the properties defined in {@link MinioConfigurationProperties}.
 * All methods return an {@link com.jlefebure.spring.boot.minio.MinioException} which wrap the Minio SDK exception.
 * The bucket name is provided with the one defined in the configuration properties.
 *
 * @author Jordan LEFEBURE
 *
 *
 * This service adapetd with minio sdk 7.0.x
 * @author Mostafa Jalambadani
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
     */
    public List<Item> list() {
        Iterable<Result<Item>> myObjects = minioClient.listObjects(configurationProperties.getBucket(), "", false);
        return getItems(myObjects);
    }

    /**
     * List all objects at root of the bucket
     *
     * @return List of items
     * @throws com.jlefebure.spring.boot.minio.MinioException if an error occur while fetch list
     */
    public List<Item> fullList() throws com.jlefebure.spring.boot.minio.MinioException {
        try {
            Iterable<Result<Item>> myObjects = minioClient.listObjects(configurationProperties.getBucket());
            return getItems(myObjects);
        } catch (  XmlParserException e) {
            throw new com.jlefebure.spring.boot.minio.MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * List all objects with the prefix given in parameter for the bucket.
     * Simulate a folder hierarchy. Objects within folders (i.e. all objects which match the pattern {@code {prefix}/{objectName}/...}) are not returned
     *
     * @param path Prefix of seeked list of object
     * @return List of items
     */
    public List<Item> list(Path path) {

        Iterable<Result<Item>> myObjects = minioClient.listObjects(configurationProperties.getBucket(), path.toString(), false);
        return getItems(myObjects);
    }

    /**
     * List all objects with the prefix given in parameter for the bucket
     * <p>
     * All objects, even those which are in a folder are returned.
     *
     * @param path Prefix of seeked list of object
     * @return List of items
     */
    public List<Item> getFullList(Path path) throws MinioException {
        try {
            Iterable<Result<Item>> myObjects = minioClient.listObjects(configurationProperties.getBucket(), path.toString());
            return getItems(myObjects);
        } catch (XmlParserException e) {
            throw new com.jlefebure.spring.boot.minio.MinioException("Error while fetching files in Minio", e);
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
                        XmlParserException |
                        InvalidResponseException|
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
     * @throws com.jlefebure.spring.boot.minio.MinioException if an error occur while fetch object
     */
    public InputStream get(Path path) throws com.jlefebure.spring.boot.minio.MinioException {
        try {
            return minioClient.getObject(configurationProperties.getBucket(), path.toString());
        } catch (XmlParserException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException  | ErrorResponseException | InternalException  | InvalidResponseException e) {
            throw new com.jlefebure.spring.boot.minio.MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Get metadata of an object from Minio
     *
     * @param path Path with prefix to the object. Object name must be included.
     * @return Metadata of the  object
     * @throws com.jlefebure.spring.boot.minio.MinioException if an error occur while fetching object metadatas
     */
    public ObjectStat getMetadata(Path path) throws com.jlefebure.spring.boot.minio.MinioException {
        try {
            return minioClient.statObject(configurationProperties.getBucket(), path.toString());
        } catch (XmlParserException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException |  ErrorResponseException | InternalException  | InvalidResponseException e) {
            throw new com.jlefebure.spring.boot.minio.MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Get metadata for multiples objects from Minio
     *
     * @param paths Paths of all objects with prefix. Objects names must be included.
     * @return A map where all paths are keys and metadatas are values
     */
    public Map<Path, ObjectStat> getMetadata(Iterable<Path> paths) {
        return StreamSupport.stream(paths.spliterator(), false)
            .map(path -> {
                try {
                    return new HashMap.SimpleEntry<>(path, minioClient.statObject(configurationProperties.getBucket(), path.toString()));
                } catch (InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException |  XmlParserException | ErrorResponseException | InternalException | InvalidResponseException  e) {
                    throw new MinioFetchException("Error while parsing list of objects", e);
                }
            })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Get a file from Minio, and save it in the {@code fileName} file
     *
     * @param source   Path with prefix to the object. Object name must be included.
     * @param fileName Filename
     * @throws com.jlefebure.spring.boot.minio.MinioException if an error occur while fetch object
     */
    public void getAndSave(Path source, String fileName) throws com.jlefebure.spring.boot.minio.MinioException {
        try {
            minioClient.getObject(configurationProperties.getBucket(), source.toString(), fileName);
        } catch (XmlParserException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException |  ErrorResponseException | InternalException  | InvalidResponseException e) {
            throw new com.jlefebure.spring.boot.minio.MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Upload a file to Minio
     *
     * @param source      Path with prefix to the object. Object name must be included.
     * @param file        File as an inputstream
     * @param headers     Additional headers to put on the file. The map MUST be mutable. All custom headers will start with 'x-amz-meta-' prefix when fetched with {@code getMetadata()} method.
     * @throws com.jlefebure.spring.boot.minio.MinioException if an error occur while uploading object
     */
    public void upload(Path source, InputStream file, Map<String, String> headers) throws
        com.jlefebure.spring.boot.minio.MinioException {
        try {
            PutObjectOptions options = new PutObjectOptions(file.available(), -1);
            options.setHeaders(headers);
            minioClient.putObject(configurationProperties.getBucket(), source.toString(), file, options);
        } catch (XmlParserException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException |  ErrorResponseException | InternalException  | InvalidResponseException e) {
            throw new com.jlefebure.spring.boot.minio.MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Upload a file to Minio
     *
     * @param source      Path with prefix to the object. Object name must be included.
     * @param file        File as an inputstream
     * @throws com.jlefebure.spring.boot.minio.MinioException if an error occur while uploading object
     */
    public void upload(Path source, InputStream file) throws
        com.jlefebure.spring.boot.minio.MinioException {
        try {
            minioClient.putObject(configurationProperties.getBucket(), source.toString(), file, new PutObjectOptions(file.available(),-1));
        } catch (XmlParserException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException |  ErrorResponseException | InternalException  | InvalidResponseException e) {
            throw new com.jlefebure.spring.boot.minio.MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Upload a file to Minio
     *
     * @param source      Path with prefix to the object. Object name must be included.
     * @param file        File as an inputstream
     * @param contentType MIME type for the object
     * @param headers     Additional headers to put on the file. The map MUST be mutable
     * @throws com.jlefebure.spring.boot.minio.MinioException if an error occur while uploading object
     */
    public void upload(Path source, InputStream file, String contentType, Map<String, String> headers) throws
        com.jlefebure.spring.boot.minio.MinioException {
        try {
            PutObjectOptions options = new PutObjectOptions(file.available(), -1);
            options.setContentType(contentType);
            options.setHeaders(headers);
            minioClient.putObject(configurationProperties.getBucket(), source.toString(), file, options);
        } catch (XmlParserException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException |  ErrorResponseException | InternalException  | InvalidResponseException e) {
            throw new com.jlefebure.spring.boot.minio.MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Upload a file to Minio
     *
     * @param source      Path with prefix to the object. Object name must be included.
     * @param file        File as an inputstream
     * @param contentType MIME type for the object
     * @throws com.jlefebure.spring.boot.minio.MinioException if an error occur while uploading object
     */
    public void upload(Path source, InputStream file, String contentType) throws
        com.jlefebure.spring.boot.minio.MinioException {
        try {
            PutObjectOptions options = new PutObjectOptions(file.available(), -1);
            options.setContentType(contentType);
            minioClient.putObject(configurationProperties.getBucket(), source.toString(), file, options);
        } catch (XmlParserException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException |  ErrorResponseException | InternalException  | InvalidResponseException e) {
            throw new com.jlefebure.spring.boot.minio.MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Upload a file to Minio
     * upload file bigger than Xmx size
     * @param source      Path with prefix to the object. Object name must be included.
     * @param file        File as an Filename
     * @throws com.jlefebure.spring.boot.minio.MinioException if an error occur while uploading object
     */
    public void upload(Path source, File file,int partSize) throws
            com.jlefebure.spring.boot.minio.MinioException {
        try {
            minioClient.putObject(configurationProperties.getBucket(), source.toString(), file.getAbsolutePath(),new PutObjectOptions(Files.size(file.toPath()),partSize));
        } catch (XmlParserException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException |  ErrorResponseException | InternalException  | InvalidResponseException e) {
            throw new com.jlefebure.spring.boot.minio.MinioException("Error while fetching files in Minio", e);
        }
    }


    /**
     * Remove a file to Minio
     *
     * @param source Path with prefix to the object. Object name must be included.
     * @throws com.jlefebure.spring.boot.minio.MinioException if an error occur while removing object
     */
    public void remove(Path source) throws com.jlefebure.spring.boot.minio.MinioException {
        try {
            minioClient.removeObject(configurationProperties.getBucket(), source.toString());
        } catch (XmlParserException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException |  ErrorResponseException | InternalException  | InvalidResponseException e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

}
