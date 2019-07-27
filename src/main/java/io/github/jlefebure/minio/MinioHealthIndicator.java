package io.github.jlefebure.minio;

import io.minio.MinioClient;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


/**
 * Set the Minio health indicator on Actuator.
 */
@Component
public class MinioHealthIndicator implements HealthIndicator {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfigurationProperties minioConfigurationProperties;


    @Override
    public Health health() {
        if (minioClient == null) {
            return Health.down().build();
        }

        try {
            if (minioClient.bucketExists(minioConfigurationProperties.getBucket())) {
                return Health.up()
                        .withDetail("bucketName", minioConfigurationProperties.getBucket())
                        .build();
            } else {
                return Health.down()
                        .withDetail("bucketName", minioConfigurationProperties.getBucket())
                        .build();
            }
        } catch (InvalidBucketNameException | IOException | NoSuchAlgorithmException | InsufficientDataException | InvalidKeyException | NoResponseException | XmlPullParserException | ErrorResponseException | InternalException e) {
            return Health.down(e)
                    .withDetail("bucketName", minioConfigurationProperties.getBucket())
                    .build();
        }
    }
}
