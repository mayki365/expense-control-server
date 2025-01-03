package org.acme;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class InvoicesResourceTest {
    @Test
    void testHelloEndpoint() {
        var body = given()
                .contentType("application/json")
                .when().get("/invoices")
                .then()
                .statusCode(200)
                .extract().as(ArrayList.class);
        assertEquals(3, body.size());
    }

}