package com.github.jlefebure.minio;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@ConfigurationProperties("spring.minio")
public class MinioConfigurationProperties {
    /**
     * URL for Minio instance
     */
    private String url = "https://play.min.io";

    /**
     * Access key (login) on Minio
     */
    private String accessKey = "Q3AM3UQ867SPQQA43P2F";

    /**
     * Secret key (password) on Minio
     */
    private String secretKey = "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG";

    /**
     * Set the connection to secured (HTTPS).
     */
    private boolean secure = false;

    /**
     * Bucket name for the application. Default value is the application name as defined in application.properties. The bucket must already exists on Minio.
     */
    @Value("${spring.application.name}")
    private String bucket;

    /**
     * URL for Minio instance
     */
    private String metricName = "minio.storage";

    private Duration connectTimeout = Duration.ofSeconds(10);
    private Duration writeTimeout = Duration.ofSeconds(60);
    private Duration readTimeout = Duration.ofSeconds(10);

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }
}
