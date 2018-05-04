package org.springframework.cloud.gcp.data.store.repository.support;

import com.google.cloud.datastore.DatastoreOptions;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

public class DatastoreRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
        extends RepositoryFactoryBeanSupport<T, S, ID> {

    DatastoreOptions datastoreOptions;

    public DatastoreRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
        this.datastoreOptions = DatastoreOptions.getDefaultInstance();
    }

    public DatastoreRepositoryFactoryBean(
            Class<? extends T> repositoryInterface,
            DatastoreOptions datastoreOptions) {
        super(repositoryInterface);
        this.datastoreOptions = datastoreOptions;
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory() {
        return new DatastoreRepositoryFactory(this.datastoreOptions);
    }
}
