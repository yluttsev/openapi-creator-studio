package ru.luttsev.studio.core.navigation;

import ru.luttsev.studio.core.model.Components;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.info.Contact;
import ru.luttsev.studio.core.model.info.ExternalDocumentation;
import ru.luttsev.studio.core.model.info.Info;
import ru.luttsev.studio.core.model.info.License;
import ru.luttsev.studio.core.model.reference.ReferenceObject;
import ru.luttsev.studio.core.model.server.Server;
import ru.luttsev.studio.core.model.server.ServerVariable;
import ru.luttsev.studio.core.model.tag.Tag;

final class RootMappings {

    private RootMappings() {
    }

    static void register(DocumentNodeRegistry registry) {
        registry.register(OpenApiDocument.class, RootMappings::collectDocument);
        registry.register(Components.class, RootMappings::collectComponents);
        registry.register(Info.class, RootMappings::collectInfo);
        registry.register(Contact.class, RootMappings::collectContact);
        registry.register(License.class, RootMappings::collectLicense);
        registry.register(
                ExternalDocumentation.class,
                RootMappings::collectExternalDocumentation);
        registry.register(Server.class, RootMappings::collectServer);
        registry.register(ServerVariable.class, RootMappings::collectServerVariable);
        registry.register(Tag.class, RootMappings::collectTag);
        registry.register(ReferenceObject.class, RootMappings::collectReference);
    }

    private static void collectDocument(
            OpenApiDocument document,
            ChildrenCollector children) {
        children.add("openapi", document.getOpenApiVersion());
        children.add("$self", document.getSelf());
        children.add("info", document.getInfo());
        children.add("jsonSchemaDialect", document.getJsonSchemaDialect());
        children.add("servers", document.getServers());
        children.add("paths", document.getPaths());
        children.add("webhooks", document.getWebhooks());
        children.add("components", document.getComponents());
        children.add("security", document.getSecurity());
        children.add("tags", document.getTags());
        children.add("externalDocs", document.getExternalDocs());
    }

    private static void collectComponents(
            Components components,
            ChildrenCollector children) {
        children.add("schemas", components.getSchemas());
        children.add("responses", components.getResponses());
        children.add("parameters", components.getParameters());
        children.add("examples", components.getExamples());
        children.add("requestBodies", components.getRequestBodies());
        children.add("headers", components.getHeaders());
        children.add("securitySchemes", components.getSecuritySchemes());
        children.add("links", components.getLinks());
        children.add("callbacks", components.getCallbacks());
        children.add("pathItems", components.getPathItems());
        children.add("mediaTypes", components.getMediaTypes());
    }

    private static void collectInfo(Info info, ChildrenCollector children) {
        children.add("title", info.getTitle());
        children.add("summary", info.getSummary());
        children.add("description", info.getDescription());
        children.add("termsOfService", info.getTermsOfService());
        children.add("contact", info.getContact());
        children.add("license", info.getLicense());
        children.add("version", info.getVersion());
    }

    private static void collectContact(Contact contact, ChildrenCollector children) {
        children.add("name", contact.getName());
        children.add("url", contact.getUrl());
        children.add("email", contact.getEmail());
    }

    private static void collectLicense(License license, ChildrenCollector children) {
        children.add("name", license.getName());
        children.add("identifier", license.getIdentifier());
        children.add("url", license.getUrl());
    }

    private static void collectExternalDocumentation(
            ExternalDocumentation documentation,
            ChildrenCollector children) {
        children.add("description", documentation.getDescription());
        children.add("url", documentation.getUrl());
    }

    private static void collectServer(Server server, ChildrenCollector children) {
        children.add("url", server.getUrl());
        children.add("description", server.getDescription());
        children.add("name", server.getName());
        children.add("variables", server.getVariables());
    }

    private static void collectServerVariable(
            ServerVariable variable,
            ChildrenCollector children) {
        children.add("enum", variable.getEnumValues());
        children.add("default", variable.getDefaultValue());
        children.add("description", variable.getDescription());
    }

    private static void collectTag(Tag tag, ChildrenCollector children) {
        children.add("name", tag.getName());
        children.add("summary", tag.getSummary());
        children.add("description", tag.getDescription());
        children.add("externalDocs", tag.getExternalDocs());
        children.add("parent", tag.getParent());
        children.add("kind", tag.getKind());
    }

    private static void collectReference(
            ReferenceObject<?> reference,
            ChildrenCollector children) {
        children.add("$ref", reference.getRef());
        children.add("summary", reference.getSummary());
        children.add("description", reference.getDescription());
    }
}
