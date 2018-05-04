package org.springframework.cloud.gcp.data.store.repository.config;

import org.springframework.cloud.gcp.data.store.repository.support.DatastoreRepositoryFactory;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;

public class DatastoreRepositoryConfigurationExtension extends RepositoryConfigurationExtensionSupport {

    @Override
    protected String getModulePrefix() {
        return "datastore";
    }

    @Override
    public String getRepositoryFactoryBeanClassName() {
        return DatastoreRepositoryFactory.class.getName();
    }
}
