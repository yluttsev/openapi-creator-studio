package ru.luttsev.studio.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.luttsev.studio.core.document.OpenApiDocumentFactory;
import ru.luttsev.studio.core.validation.DocumentValidator;
import ru.luttsev.studio.openapi.importing.DefaultOpenApiImporter;
import ru.luttsev.studio.openapi.importing.OpenApiImporter;
import ru.luttsev.studio.openapi.version.OpenApiVersionAdapterRegistry;
import ru.luttsev.studio.openapi.version.OpenApiVersionAdapters;

@Configuration(proxyBeanMethods = false)
public class ApplicationComponentConfiguration {

    @Bean
    OpenApiDocumentFactory openApiDocumentFactory() {
        return new OpenApiDocumentFactory();
    }

    @Bean
    OpenApiImporter openApiImporter() {
        return new DefaultOpenApiImporter();
    }

    @Bean
    OpenApiVersionAdapterRegistry openApiVersionAdapterRegistry() {
        return OpenApiVersionAdapters.defaults();
    }

    @Bean
    DocumentValidator documentValidator() {
        return new DocumentValidator();
    }
}
