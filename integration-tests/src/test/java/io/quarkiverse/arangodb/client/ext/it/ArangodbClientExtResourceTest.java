package io.quarkiverse.arangodb.client.ext.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ArangodbClientExtResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/arangodb-client-ext")
                .then()
                .statusCode(200)
                .body(is("Hello arangodb-client-ext"));
    }
}
