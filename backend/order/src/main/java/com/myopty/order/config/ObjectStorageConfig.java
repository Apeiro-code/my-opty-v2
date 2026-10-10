package com.myopty.order.config;

import com.myopty.order.service.FileSystemObjectStore;
import com.myopty.order.service.ObjectStore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the object store. The bean is the seam a MinIO implementation replaces:
 * the properties and the service depend on the {@link ObjectStore} interface, so
 * swapping the backend is this one method, not a hunt through callers.
 */
@Configuration
@EnableConfigurationProperties(ObjectStorageProperties.class)
public class ObjectStorageConfig {

    @Bean
    ObjectStore objectStore(ObjectStorageProperties properties) {
        return new FileSystemObjectStore(properties.getRoot());
    }
}
