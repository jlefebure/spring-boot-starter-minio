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


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.jlefebure.spring.boot.minio.notification.MinioNotification;

import io.minio.CloseableIterator;
import io.minio.ListenBucketNotificationArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.NotificationRecords;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AutoConfiguration(after = {MinioConfiguration.class, MinioMetricConfiguration.class})
@RequiredArgsConstructor
@Slf4j
public class MinioNotificationConfiguration implements ApplicationContextAware {

    private final MinioClient minioClient;
    private final MinioConfigurationProperties minioConfigurationProperties;

    private List<Thread> handlers = new ArrayList<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        for (String beanName : applicationContext.getBeanDefinitionNames()) {

            // ignore self
            if("minioNotificationConfiguration".equals(beanName)) {
                continue;
            }

            Object obj = applicationContext.getBean(beanName);

            Class<?> objClz = obj.getClass();
            if (org.springframework.aop.support.AopUtils.isAopProxy(obj)) {
                objClz = org.springframework.aop.support.AopUtils.getTargetClass(obj);
            }

            for (Method m : objClz.getDeclaredMethods()) {
                if (m.isAnnotationPresent(MinioNotification.class)) {
                    //Check if has NotificationInfo parameter only
                    if (m.getParameterCount() != 1) {
                        throw new IllegalArgumentException("Minio notification handler should have only one NotificationInfo parameter");
                    }

                    if (m.getParameterTypes()[0] != NotificationRecords.class) {
                        throw new IllegalArgumentException("Parameter should be instance of NotificationRecords");
                    }

                    MinioNotification annotation = m.getAnnotation(MinioNotification.class);

                    //Then registering method handler
                    Thread handler = new Thread(() -> {
                        for (; ; ) {
                            try {
                                log.info("Registering Minio handler on {} with notification {}", m.getName(), Arrays.toString(annotation.value()));
                                ListenBucketNotificationArgs args = ListenBucketNotificationArgs.builder()
                                        .bucket(minioConfigurationProperties.getBucket())
                                        .prefix(annotation.prefix())
                                        .suffix(annotation.suffix())
                                        .events(annotation.value())
                                        .build();
                                try(CloseableIterator<Result<NotificationRecords>> list = minioClient.listenBucketNotification(args)){
                                    while(list.hasNext()){
                                        NotificationRecords info = list.next().get();
                                        try {
                                            log.debug("Receive notification for method {}", m.getName());
                                            m.invoke(obj, info);
                                        } catch (IllegalAccessException | InvocationTargetException e) {
                                            log.error("Error while handling notification for method {} with notification {}", m.getName(), Arrays.toString(annotation.value()), e);
                                        }
                                    }
                                };
                            } catch (Exception e) {
                                log.error("Error while registering notification for method {} with notification {}", m.getName(), Arrays.toString(annotation.value()), e);
                                throw new IllegalStateException("Cannot register handler", e);
                            }
                        }
                    });
                    handler.start();
                    handlers.add(handler);
                }
            }
        }
    }
}
