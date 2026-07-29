package ru.luttsev.studio.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Set;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.info.Info;
import ru.luttsev.studio.core.model.media.MediaType;
import ru.luttsev.studio.core.model.media.MediaTypeName;
import ru.luttsev.studio.core.model.path.HttpMethod;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.path.Paths;
import ru.luttsev.studio.core.model.reference.InlineObject;
import ru.luttsev.studio.core.model.response.ApiResponse;
import ru.luttsev.studio.core.model.response.ResponseKey;
import ru.luttsev.studio.core.model.schema.JsonType;
import ru.luttsev.studio.core.model.schema.SchemaDefinition;
import ru.luttsev.studio.core.model.schema.UriReference;

class OpenApiDocumentTest {

    @Test
    void buildsCompleteDocumentGraph() {
        var userSchema = new SchemaDefinition();
        userSchema.setTypes(Set.of(JsonType.OBJECT));

        var nameSchema = new SchemaDefinition();
        nameSchema.setTypes(Set.of(JsonType.STRING));
        userSchema.getProperties().put("name", nameSchema);
        userSchema.getRequired().add("name");

        var userReference = new SchemaDefinition();
        userReference.setRef(new UriReference("#/components/schemas/User"));

        var responseMediaType = new MediaType();
        responseMediaType.setSchema(userReference);

        var okResponse = new ApiResponse();
        okResponse.setDescription("User found");
        okResponse.getContent().put(
                MediaTypeName.APPLICATION_JSON,
                new InlineObject<>(responseMediaType));

        var getUser = new Operation();
        getUser.setOperationId("getUser");
        getUser.getResponses().put(
                new ResponseKey("200"),
                new InlineObject<>(okResponse));

        var userPath = new PathItem();
        userPath.getOperations().put(HttpMethod.GET, getUser);

        var paths = new Paths();
        paths.getItems().put("/users/{id}", userPath);

        var components = new Components();
        components.getSchemas().put("User", userSchema);

        var info = new Info();
        info.setTitle("Users API");
        info.setVersion("1.0.0");

        var document = new OpenApiDocument();
        document.setOpenApiVersion(OpenApiVersion.V3_1_2);
        document.setInfo(info);
        document.setPaths(paths);
        document.setComponents(components);

        assertEquals(OpenApiVersion.V3_1_2, document.getOpenApiVersion());
        assertSame(
                getUser,
                document.getPaths()
                        .getItems()
                        .get("/users/{id}")
                        .getOperations()
                        .get(HttpMethod.GET));
        assertSame(userSchema, document.getComponents().getSchemas().get("User"));
        assertEquals(
                "User found",
                ((InlineObject<ApiResponse>) getUser.getResponses()
                                .get(new ResponseKey("200")))
                        .value()
                        .getDescription());
    }
}
