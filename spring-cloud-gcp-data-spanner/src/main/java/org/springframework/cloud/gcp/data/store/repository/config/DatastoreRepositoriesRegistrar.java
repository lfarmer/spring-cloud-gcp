package org.springframework.cloud.gcp.data.store.repository.config;

import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

public class DatastoreRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableDatastoreRepositories.class;
    }

    @Override
    protected RepositoryConfigurationExtension getExtension() {
        return new DatastoreRepositoryConfigurationExtension();
    }
}
