package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class MachineLearningResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .header("Content-Type", "application/json")
          .body(10.0)
          .when().post("/samples")
          .then()
             .statusCode(204);
    }

}