package org.springframework.cloud.gcp.data.store.repository.config;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.cloud.gcp.data.store.repository.support.DatastoreRepositoryFactoryBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.repository.config.DefaultRepositoryBaseClass;
import org.springframework.data.repository.query.QueryLookupStrategy;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(DatastoreRepositoriesRegistrar.class)
public @interface EnableDatastoreRepositories {

    /**
     * Alias for the {@link #basePackages()} attribute. Allows for more concise annotation
     * declarations e.g.: {@code @EnableDatastoreRepositories("org.my.pkg")} instead
     * of {@code @EnableDatastoreRepositories(basePackages="org.my.pkg")}.
     */
    String[] value() default {};

    /**
     * Base packages to scan for annotated components. {@link #value()} is an alias for
     * (and mutually exclusive with) this attribute. Use {@link #basePackageClasses()} for
     * a type-safe alternative to String-based package names.
     */
    String[] basePackages() default {};

    /**
     * Type-safe alternative to {@link #basePackages()} for specifying the packages to
     * scan for annotated components. The package of each class specified will be scanned.
     * Consider creating a special no-op marker class or interface in each package that
     * serves no purpose other than being referenced by this attribute.
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * Specifies which types are eligible for component scanning. Further narrows the set
     * of candidate components from everything in {@link #basePackages()} to everything in
     * the base packages that matches the given filter or filters.
     */
    ComponentScan.Filter[] includeFilters() default {};

    /**
     * Specifies which types are not eligible for component scanning.
     */
    ComponentScan.Filter[] excludeFilters() default {};

    /**
     * Returns the postfix to be used when looking up custom repository implementations.
     * Defaults to {@literal Impl}. So for a repository named {@code PersonRepository} the
     * corresponding implementation class will be looked up scanning for
     * {@code PersonRepositoryImpl}.
     *
     * @return
     */
    String repositoryImplementationPostfix() default "Impl";

    /**
     * Configures the location of where to find the Spring Data named queries properties
     * file.
     *
     * @return
     */
    String namedQueriesLocation() default "";

    /**
     * Returns the key of the {@link QueryLookupStrategy} to be used for lookup queries
     * for query methods. Defaults to {@link QueryLookupStrategy.Key#CREATE_IF_NOT_FOUND}.
     *
     * @return
     */
    QueryLookupStrategy.Key queryLookupStrategy() default QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND;

    /**
     * Returns the {@link FactoryBean} class to be used for each repository instance.
     * Defaults to {@link DatastoreRepositoryFactoryBean}.
     *
     * @return
     */
    Class<?> repositoryFactoryBeanClass() default DatastoreRepositoryFactoryBean.class;

    /**
     * Configure the repository base class to be used to create repository proxies for
     * this particular configuration.
     *
     * @return
     */
    Class<?> repositoryBaseClass() default DefaultRepositoryBaseClass.class;
}
