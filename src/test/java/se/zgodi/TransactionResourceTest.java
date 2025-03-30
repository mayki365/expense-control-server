package se.zgodi;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static io.restassured.RestAssured.given;

@QuarkusTest
class TransactionResourceTest {
    @Test
    void testHelloEndpoint() {
        var body = given()
                .contentType("application/json")
                .when().get("/transaction")
                .then()
                .statusCode(200)
                .extract().as(ArrayList.class);
        assertTrue(body.size()>-1);
    }

}