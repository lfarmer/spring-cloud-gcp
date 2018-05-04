package org.springframework.cloud.gcp.data.store.core.mapping;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.core.support.AbstractEntityInformation;

import java.lang.reflect.Field;

public class DatastoreEntityInformation<T, ID>
		extends AbstractEntityInformation<T, ID> {

	public DatastoreEntityInformation(Class<T> domainClass) {
		super(domainClass);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ID getId(T entity) {
		Class<?> domainClass = getJavaType();
		while (domainClass != Object.class) {
			for (Field field : domainClass.getDeclaredFields()) {
				if (field.getAnnotation(Id.class) != null) {
					try {
						return (ID) field.get(entity);
					}
					catch (IllegalArgumentException | IllegalAccessException e) {
						BeanWrapper beanWrapper = PropertyAccessorFactory
								.forBeanPropertyAccess(entity);
						return (ID) beanWrapper.getPropertyValue(field.getName());
					}
				}
			}
			domainClass = domainClass.getSuperclass();
		}
		throw new IllegalStateException("id not found");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<ID> getIdType() {
		Class<?> domainClass = getJavaType();
		while (domainClass != Object.class) {
			for (Field field : domainClass.getDeclaredFields()) {
				if (field.getAnnotation(Id.class) != null) {
					return (Class<ID>) field.getType();
				}
			}
			domainClass = domainClass.getSuperclass();
		}
		throw new IllegalStateException("id not found");
	}
}
