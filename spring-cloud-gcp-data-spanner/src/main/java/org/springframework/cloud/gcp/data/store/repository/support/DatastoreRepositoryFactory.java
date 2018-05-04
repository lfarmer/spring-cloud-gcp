package org.springframework.cloud.gcp.data.store.repository.support;

import com.google.cloud.datastore.DatastoreOptions;
import org.springframework.cloud.gcp.data.store.core.mapping.DatastoreEntityInformation;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

public class DatastoreRepositoryFactory extends RepositoryFactorySupport {

    private DatastoreOptions datastoreOptions = DatastoreOptions.getDefaultInstance();

    public DatastoreRepositoryFactory(DatastoreOptions datastoreOptions) {
        this.datastoreOptions = datastoreOptions;
    }

    @Override
    public <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
        return new DatastoreEntityInformation<T, ID>(domainClass);
    }

    @Override
    protected Object getTargetRepository(RepositoryInformation information) {
        EntityInformation<?, Serializable> entityInformation = getEntityInformation(
                information.getDomainType());
        return getTargetRepositoryViaReflection(information, entityInformation,
                this.datastoreOptions);
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return SimpleDatastoreRepository.class;
    }
}
