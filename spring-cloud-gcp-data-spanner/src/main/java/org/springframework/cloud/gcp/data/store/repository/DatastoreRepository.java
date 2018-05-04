package org.springframework.cloud.gcp.data.store.repository;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import org.springframework.data.repository.CrudRepository;

import java.io.Serializable;

public interface DatastoreRepository<T, ID extends Serializable> extends CrudRepository<T, ID> {

    Iterable<T> query(Query<Entity> query);
}
