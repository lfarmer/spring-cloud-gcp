/*
 *  Copyright 2018 original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.gcp.data.spanner.core.convert;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.cloud.ByteArray;
import com.google.cloud.Date;
import com.google.cloud.Timestamp;
import com.google.cloud.spanner.AbstractStructReader;
import com.google.cloud.spanner.Struct;
import com.google.cloud.spanner.Type;
import com.google.common.collect.ImmutableMap;

import org.springframework.cloud.gcp.data.spanner.core.mapping.SpannerDataException;
import org.springframework.cloud.gcp.data.spanner.core.mapping.SpannerMappingContext;
import org.springframework.cloud.gcp.data.spanner.core.mapping.SpannerPersistentEntity;
import org.springframework.cloud.gcp.data.spanner.core.mapping.SpannerPersistentProperty;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.convert.EntityInstantiator;
import org.springframework.data.convert.EntityInstantiators;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.PreferredConstructor;
import org.springframework.data.mapping.PropertyHandler;
import org.springframework.data.mapping.model.ParameterValueProvider;
import org.springframework.data.mapping.model.PersistentEntityParameterValueProvider;
import org.springframework.data.mapping.model.PropertyValueProvider;

/**
 * @author Balint Pato
 * @author Chengyuan Zhao
 */
class MappingSpannerReadConverter extends AbstractSpannerCustomConverter
		implements SpannerEntityReader {

	static final Map<Class, BiFunction<Struct, String, List>> readIterableMapping = new ImmutableMap.Builder<Class, BiFunction<Struct, String, List>>()
			.put(Boolean.class, AbstractStructReader::getBooleanList)
			.put(Long.class, AbstractStructReader::getLongList)
			.put(String.class, AbstractStructReader::getStringList)
			.put(Double.class, AbstractStructReader::getDoubleList)
			.put(Timestamp.class, AbstractStructReader::getTimestampList)
			.put(Date.class, AbstractStructReader::getDateList)
			.put(ByteArray.class, AbstractStructReader::getBytesList)
			.build();

	static final Map<Class, BiFunction<Struct, String, ?>> singleItemReadMethodMapping = new ImmutableMap.Builder<Class, BiFunction<Struct, String, ?>>()
			.put(Boolean.class, AbstractStructReader::getBoolean)
			.put(Long.class, AbstractStructReader::getLong)
			.put(String.class, AbstractStructReader::getString)
			.put(Double.class, AbstractStructReader::getDouble)
			.put(Timestamp.class, AbstractStructReader::getTimestamp)
			.put(Date.class, AbstractStructReader::getDate)
			.put(ByteArray.class, AbstractStructReader::getBytes)
			.put(double[].class, AbstractStructReader::getDoubleArray)
			.put(long[].class, AbstractStructReader::getLongArray)
			.put(boolean[].class, AbstractStructReader::getBooleanArray).build();

	private final SpannerMappingContext spannerMappingContext;

	private EntityInstantiators instantiators = new EntityInstantiators();

	MappingSpannerReadConverter(
			SpannerMappingContext spannerMappingContext,
			CustomConversions customConversions) {
		super(customConversions, null);
		this.spannerMappingContext = spannerMappingContext;
	}

	@Override
	public <R> R read(Class<R> type, Struct source) {
		return read(type, source, null, false);
	}

	/**
	 * Reads a single POJO from a Spanner row.
	 * @param type the type of POJO
	 * @param source the Spanner row
	 * @param includeColumns the columns to read. If null then all columns will be read.
	 * @param allowMissingColumns if true, then properties with no corresponding column are
	 * not mapped. If false, then an exception is thrown.
	 * @param <R> the type of the POJO.
	 * @return the POJO
	 */
	public <R> R read(Class<R> type, Struct source, Set<String> includeColumns,
			boolean allowMissingColumns) {
		boolean readAllColumns = includeColumns == null;
		SpannerPersistentEntity<? extends R> persistentEntity = (SpannerPersistentEntity<? extends R>) this.spannerMappingContext
				.getPersistentEntity(type);
		StructAccessor structAccessor = new StructAccessor(source);
		EntityInstantiator instantiator = instantiators.getInstantiatorFor(persistentEntity);
		PreferredConstructor<? extends R, SpannerPersistentProperty> persistenceConstructor = persistentEntity
				.getPersistenceConstructor();

		ParameterValueProvider<SpannerPersistentProperty> provider = getParameterValueProvider(persistentEntity,
				structAccessor);

		R object = instantiator.createInstance(persistentEntity, provider);
		PersistentPropertyAccessor accessor = persistentEntity
				.getPropertyAccessor(object);

		persistentEntity.doWithProperties(
				(PropertyHandler<SpannerPersistentProperty>) spannerPersistentProperty -> {
					if (!shouldSkipProperty(
							structAccessor,
							spannerPersistentProperty,
							includeColumns,
							readAllColumns,
							persistenceConstructor,
							allowMissingColumns)) {
						attemptToReadProperty(structAccessor, spannerPersistentProperty, accessor);
					}

				});
		return object;
	}

	private void attemptToReadProperty(StructAccessor structAccessor,
			SpannerPersistentProperty spannerPersistentProperty,
			PersistentPropertyAccessor accessor) {
		String columnName = spannerPersistentProperty.getColumnName();
		Object value = structAccessor.get(spannerPersistentProperty);
		if (value != null) {
			accessor.setProperty(spannerPersistentProperty, value);
		}
		else {
			throw new SpannerDataException(String.format(
					"The value in column with name %s"
							+ " could not be converted to the corresponding property in the entity."
							+ " The property's type is %s.",
					columnName, spannerPersistentProperty.getType()));
		}
	}

	private <R> boolean shouldSkipProperty(StructAccessor structAccessor,
			SpannerPersistentProperty spannerPersistentProperty, Set<String> includeColumns,
			boolean readAllColumns,
			PreferredConstructor<? extends R, SpannerPersistentProperty> persistenceConstructor,
			boolean allowMissingColumns) {
		String columnName = spannerPersistentProperty.getColumnName();
		boolean notRequiredByPartialRead = !readAllColumns && !includeColumns.contains(columnName);
		boolean isConstructorParameter = persistenceConstructor != null
				&& persistenceConstructor.isConstructorParameter(spannerPersistentProperty);

		return notRequiredByPartialRead
				|| isValidMissingColumn(structAccessor, allowMissingColumns, spannerPersistentProperty.getColumnName())
				|| structAccessor.isNull(columnName)
				|| isConstructorParameter;
	}

	private boolean isValidMissingColumn(StructAccessor structAccessor, boolean allowMissingColumns,
			String columnName) {
		if (!structAccessor.hasColumn(columnName)) {
			if (!allowMissingColumns) {
				throw new SpannerDataException(
						"Unable to read column from Spanner results: "
								+ columnName);
			}
			else {
				return true;
			}
		}
		return false;
	}

	private <R> PersistentEntityParameterValueProvider<SpannerPersistentProperty> getParameterValueProvider(
			SpannerPersistentEntity<? extends R> persistentEntity, StructAccessor structAccessor) {
		return new PersistentEntityParameterValueProvider<>(
				persistentEntity, new PropertyValueProvider<SpannerPersistentProperty>() {
					@Override
					public <T> T getPropertyValue(SpannerPersistentProperty property) {
						return (T) structAccessor.get(property);
					}
				}, null);
	}

	class StructAccessor {

		private Struct struct;

		StructAccessor(Struct struct) {

			this.struct = struct;
		}

		public Object get(SpannerPersistentProperty spannerPersistentProperty) {
			return ConversionUtils.isIterableNonByteArrayType(spannerPersistentProperty.getType())
					? getIterable(spannerPersistentProperty)
					: getSingle(spannerPersistentProperty);
		}

		private Object getIterable(SpannerPersistentProperty spannerPersistentProperty) {
			String columnName = spannerPersistentProperty.getColumnName();
			Class innerType = ConversionUtils.boxIfNeeded(spannerPersistentProperty.getColumnInnerType());

			Object result;

			if (isSpannerSupportedIterableType(innerType)) {
				result = readIterableMapping.get(innerType).apply(struct, columnName);
			}
			else if (isArrayOfStruct(columnName)) {
				List<Struct> iterableValue = struct.getStructList(columnName);
				Iterable convertedIterableValue = (Iterable) StreamSupport.stream(iterableValue.spliterator(), false)
						.map(item -> read(innerType, item))
						.collect(Collectors.toList());

				result = convertedIterableValue;
			}
			else {
				result = getPotentiallyConvertedIterable(columnName, innerType);
			}
			return result;
		}

		private Object getPotentiallyConvertedIterable(String columnName, Class innerType) {
			for (Class sourceType : readIterableMapping.keySet()) {
				if (canConvert(sourceType, innerType)) {
					List iterableValue = readIterableMapping.get(sourceType).apply(struct, columnName);
					return ConversionUtils
							.convertIterable(iterableValue, innerType, MappingSpannerReadConverter.this);
				}
			}
			return null;
		}

		private boolean isSpannerSupportedIterableType(Class innerType) {
			return readIterableMapping.containsKey(innerType);
		}

		private boolean isArrayOfStruct(String columnName) {
			return struct.getColumnType(columnName).getArrayElementType().getCode() == Type.Code.STRUCT;
		}

		private Object getSingle(SpannerPersistentProperty spannerPersistentProperty) {
			String columnName = spannerPersistentProperty.getColumnName();
			Class sourceType = ConversionUtils.SPANNER_COLUMN_TYPE_TO_JAVA_TYPE_MAPPING
					.get(struct.getColumnType(columnName));

			Object result = null;
			Class targetType = spannerPersistentProperty.getType();
			if (sourceType != null && canConvert(sourceType, targetType)) {
				BiFunction readFunction = singleItemReadMethodMapping.get(ConversionUtils.boxIfNeeded(sourceType));
				if (readFunction != null) {
					result = convertIfNeeded(readFunction.apply(struct, columnName), targetType);
				}
			}
			return result;
		}

		public boolean isNull(String columnName) {
			return struct.isNull(columnName);
		}

		public boolean hasColumn(String columnName) {
			return struct.getType().getStructFields().stream()
					.anyMatch(f -> f.getName().equals(columnName));
		}
	}
}
