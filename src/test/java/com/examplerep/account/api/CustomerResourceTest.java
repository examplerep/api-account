package com.examplerep.account.api;

import com.examplerep.account.Roles;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * With {@code %test} profile, OIDC is disabled; {@link TestSecurity} supplies roles so RBAC is still
 * exercised. Against a real Keycloak, use access tokens with realm roles {@link Roles#CUSTOMER_READ}
 * / {@link Roles#CUSTOMER_WRITE} (or a {@code groups} claim if configured).
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerResourceTest {

    private static final long SEED_BUSINESS_ID = 1000000000000000000L;
    private static final long SEED_INDIVIDUAL_ID = 2000000000000000000L;

    @Test
    @Order(1)
    void list_withoutToken_returns401() {
        given().when().get("/api/customers").then().statusCode(401);
    }

    @Test
    @Order(2)
    @TestSecurity(user = "test", roles = Roles.CUSTOMER_WRITE)
    void list_withWriteOnlyToken_returns403() {
        given().when().get("/api/customers").then().statusCode(403);
    }

    @Test
    @Order(3)
    @TestSecurity(user = "test", roles = Roles.CUSTOMER_READ)
    void list_withReadToken_returns200() {
        given()
                .when()
                .get("/api/customers")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(2));
    }

    @Test
    @Order(4)
    @TestSecurity(user = "test", roles = Roles.CUSTOMER_READ)
    void getSeedIndividual_withReadToken_returns200() {
        given()
                .when()
                .get("/api/customers/" + SEED_INDIVIDUAL_ID)
                .then()
                .statusCode(200)
                .body("customer.customerId", equalTo(SEED_INDIVIDUAL_ID))
                .body("individual.firstName", equalTo("Test"))
                .body("individual.lastName", equalTo("Individual"));
    }

    @Test
    @Order(5)
    @TestSecurity(user = "test", roles = Roles.CUSTOMER_READ)
    void getSeedBusiness_bodyHasLegalName() {
        given()
                .when()
                .get("/api/customers/" + SEED_BUSINESS_ID)
                .then()
                .statusCode(200)
                .body("customer.customerId", equalTo(SEED_BUSINESS_ID))
                .body("business.legalName", equalTo("Acme Example Corp"));
    }

    @Test
    @Order(6)
    @TestSecurity(user = "test", roles = Roles.CUSTOMER_READ)
    void get_missing_returns404() {
        given().when().get("/api/customers/1").then().statusCode(404);
    }

    @Test
    @Order(7)
    void post_withoutToken_returns401() {
        given()
                .contentType(ContentType.JSON)
                .body(
                        """
                        {
                          "customerType": "INDIVIDUAL",
                          "individual": {
                            "firstName": "Api",
                            "lastName": "Test"
                          }
                        }
                        """)
                .when()
                .post("/api/customers")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(8)
    @TestSecurity(user = "test", roles = Roles.CUSTOMER_READ)
    void post_withReadOnly_returns403() {
        given()
                .contentType(ContentType.JSON)
                .body(
                        """
                        {
                          "customerType": "INDIVIDUAL",
                          "individual": {
                            "firstName": "Api",
                            "lastName": "Test"
                          }
                        }
                        """)
                .when()
                .post("/api/customers")
                .then()
                .statusCode(403);
    }

    @Test
    @Order(9)
    @TestSecurity(user = "test", roles = {Roles.CUSTOMER_READ, Roles.CUSTOMER_WRITE})
    void post_createIndividual_thenGet_thenDelete() {
        long id =
                given()
                        .contentType(ContentType.JSON)
                        .body(
                                """
                                {
                                  "customerType": "INDIVIDUAL",
                                  "individual": {
                                    "firstName": "Rest",
                                    "middleName": null,
                                    "lastName": "Assured",
                                    "suffix": null
                                  }
                                }
                                """)
                        .when()
                        .post("/api/customers")
                        .then()
                        .statusCode(201)
                        .header("Location", notNullValue())
                        .body("customer.customerType", equalTo("INDIVIDUAL"))
                        .body("customer.name", equalTo("Rest Assured"))
                        .extract()
                        .path("customer.customerId");

        given()
                .when()
                .get("/api/customers/" + id)
                .then()
                .statusCode(200)
                .body("individual.firstName", equalTo("Rest"));

        given().when().delete("/api/customers/" + id).then().statusCode(204);
    }

    @Test
    @Order(10)
    @TestSecurity(user = "test", roles = {Roles.CUSTOMER_READ, Roles.CUSTOMER_WRITE})
    void put_updateIndividual_mismatchPayload_returns400() {
        long id =
                ((Number)
                                given()
                                        .contentType(ContentType.JSON)
                                        .body(
                                                """
                                                {
                                                  "customerType": "INDIVIDUAL",
                                                  "individual": {
                                                    "firstName": "X",
                                                    "lastName": "Y"
                                                  }
                                                }
                                                """)
                                        .when()
                                        .post("/api/customers")
                                        .then()
                                        .statusCode(201)
                                        .extract()
                                        .path("customer.customerId"))
                        .longValue();

        given()
                .contentType(ContentType.JSON)
                .body("{\"business\": {\"legalName\": \"Only Biz\"}}")
                .when()
                .put("/api/customers/" + id)
                .then()
                .statusCode(400);

        given().when().delete("/api/customers/" + id).then().statusCode(204);
    }

    @Test
    @Order(11)
    @TestSecurity(user = "test", roles = {Roles.CUSTOMER_READ, Roles.CUSTOMER_WRITE})
    void put_validIndividual_returns200() {
        long id =
                ((Number)
                                given()
                                        .contentType(ContentType.JSON)
                                        .body(
                                                """
                                                {
                                                  "customerType": "INDIVIDUAL",
                                                  "individual": {
                                                    "firstName": "A",
                                                    "lastName": "B"
                                                  }
                                                }
                                                """)
                                        .when()
                                        .post("/api/customers")
                                        .then()
                                        .statusCode(201)
                                        .extract()
                                        .path("customer.customerId"))
                        .longValue();

        given()
                .contentType(ContentType.JSON)
                .body(
                        """
                        {
                          "individual": {
                            "firstName": "Up",
                            "middleName": null,
                            "lastName": "Dated",
                            "suffix": "Sr"
                          }
                        }
                        """)
                .when()
                .put("/api/customers/" + id)
                .then()
                .statusCode(200)
                .body("customer.name", equalTo("Up Dated, Sr"));

        given().when().delete("/api/customers/" + id).then().statusCode(204);
    }

    @Test
    @Order(12)
    @TestSecurity(user = "test", roles = {Roles.CUSTOMER_READ, Roles.CUSTOMER_WRITE})
    void delete_unknown_returns404() {
        given().when().delete("/api/customers/1").then().statusCode(404);
    }
}
