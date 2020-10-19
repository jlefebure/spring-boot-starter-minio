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

package com.jlefebure.spring.boot.minio.notification;

import io.minio.messages.NotificationRecords;
import org.springframework.scheduling.annotation.Async;

import java.lang.annotation.*;


/**
 * Add a listener to the Minio bucket, which handle the events given in the {@code value} parameter.
 * The annotated method should have a parameter {@link NotificationRecords} and return {@code void}.
 */

@Async
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MinioNotification {

    /**
     * All events that the method handler should receive. Values defined
     * in <a href="https://docs.min.io/docs/minio-bucket-notification-guide.html">Minio documentation</a> are allowed.
     */
    String[] value();

    /**
     * Prefix of the items that should be handled
     */
    String prefix() default "";

    /**
     * Suffix of the items that should be handled
     */
    String suffix() default "";

}
