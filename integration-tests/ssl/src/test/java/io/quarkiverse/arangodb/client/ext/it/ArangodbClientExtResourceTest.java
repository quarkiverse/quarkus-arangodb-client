package io.quarkiverse.arangodb.client.ext.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ArangodbClientExtResourceTest {
    @Test
    public void shouldListDatabases() {
        given()
                .when().get("/arangodb-client-ext/databases")
                .then()
                .statusCode(200)
                .body("$", hasItems("_system"));
    }
}
