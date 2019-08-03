# Spring Boot Starter Minio

[![Build Status](https://travis-ci.org/jlefebure/spring-boot-starter-minio.svg?branch=master)](https://travis-ci.org/jlefebure/spring-boot-starter-minio)

Spring Boot Starter which allow to connect to a Minio bucket, to save, get, remove an object. The starter also embed 
metrics and health check for Actuator.  

## Quick start

Just add the dependency to an existing Spring Boot project.

Maven
```xml
<dependency>
    <groupId>com.jlefebure</groupId>
    <artifactId>spring-boot-starter-minio</artifactId>
    <version>1.1</version>
</dependency>
```

Gradle 
```groovy
    implementation 'com.jlefebure:spring-boot-starter-minio:1.1'
```


Then, add the following properties to your `application.properties` file.

```properties
# Minio Host
spring.minio.url=https://play.min.io
# Minio Bucket name for your application
spring.minio.bucket=00000qweqwe
# Minio access key (login)
spring.minio.access-key=###Your accessKey###
# Minio secret key (password)
spring.minio.secret-key=###Your secretKey###
```

The default value are parameterized on the public Minio instance.

You are then ready to start your application. The Minio connection is setup at Spring context initialization. If the 
connection could not be established, your application will not start.

## Fetching data

The starter include an utility bean `MinioService` which allow to request Minio as simply as possible. Exceptions are
wrapped into a single `MinioException`, and the bucket parameter is populated on what have been set in application 
properties.

This quick example is a Spring REST controller allow to list files at the root of the bucket, and download one of them. 

```java
@RestController
@RequestMapping("/files")
public class TestController {

    @Autowired
    private MinioService minioService;


    @GetMapping("/")
    public List<Item> testMinio() throws MinioException {
        return minioService.list();
    }

    @GetMapping("/{object}")
    public void getObject(@PathVariable("object") String object, HttpServletResponse response) throws MinioException, IOException {
        InputStream inputStream = minioService.get(Path.of(object));
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

        // Set the content type and attachment header.
        response.addHeader("Content-disposition", "attachment;filename=" + object);
        response.setContentType(URLConnection.guessContentTypeFromName(object));

        // Copy the stream to the response's output stream.
        IOUtils.copy(inputStream, response.getOutputStream());
        response.flushBuffer();
    }
}
```

You can always use directly the `MinioClient` from the original SDK, which is declared as a bean. Just add :

```java
@Autowired
private MinioClient minioClient;
```

## Notifications

You can handle notifications from the bucket via `MinioClient` instance, or simply by adding a method with `@MinioNotification` at top.
The method must be in a declared Spring bean to be handled.

The following example print "Hello world" each time an object is download from Minio bucket.

```java
    @MinioNotification({"s3:ObjectAccessed:Get"})
    public void handleGet(NotificationInfo notificationInfo) {
        System.out.println("Hello world");
    }
```

To work, your method must have only one parameter of class `NotificationInfo` and return `void`.

## Actuator

The starter add to Actuator some metrics and an health check to give a status on Minio connection.

### Metric

All operations on Minio client add a metric in Spring Actuator. The default metric name is `minio.storage`. This can be 
override by setting the property `spring.minio.metric-name`.

```json
{
  "name": "minio.storage",
  "baseUnit": "seconds",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 1
    },
    {
      "statistic": "TOTAL_TIME",
      "value": 0.175
    },
    {
      "statistic": "MAX",
      "value": 0
    }
  ],
  "availableTags": [
    {
      "tag": "bucket",
      "values": [
        "customer-care-api"
      ]
    },
    {
      "tag": "operation",
      "values": [
        "getObject",
        "listObjects",
        "putObject",
        "removeObject"
      ]
    },
    {
      "tag": "status",
      "values": [
        "ko",
        "ok"
      ]
    }
  ]
}
```

List bucket operation is also monitored on the metric `minio.storage.list.bucket`.

All metrics are compatible with Prometheus scrapping. If you have Prometheus dependency for Actuator, the following 
metrics are availables.

```
minio_storage_seconds_count{bucket="customer-care-api",operation="getObject",status="ok",} 1.0
minio_storage_seconds_sum{bucket="customer-care-api",operation="getObject",status="ok",} 0.175
```

You can then request it via your favorite monitor tool. For example, to get all getObject operations on every buckets :

```
increase(minio_storage_seconds_count{ operation="getObject" }
```

### Health check

An additional health indicator is available to give a status on Minio connection. When the starter has been added to
the project and by calling your management endpoint (default is `/actuator/health`), the following health indicator is 
shown.

```json
{
  "status": "UP",
  "details": {
    "minio": {
      "status": "UP",
      "details": {
        "bucketName": "00000qweqwe"
      }
    }
  }
}
``` 

The health check is done by checking if the bucket parameterized in the application properties exists. Then,

 * If the bucket is deleted after the application has been started, the health status will be 'DOWN'.
 * If the connection could not been established to Minio, the status will be 'DOWN'.