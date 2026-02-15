package com.example.tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.*;

public class SimpleGetRequestTest {

    @BeforeClass
    public void setup() {
        // Set base URI for the API
        RestAssured.baseURI = "https://jsonplaceholder.typicode.com";
    }

    @Test(priority = 1, description = "Verify GET request returns 200 status code")
    public void testGetRequestStatusCode() {
        given()
            .log().all()
        .when()
            .get("/posts/1")
        .then()
            .log().all()
            .statusCode(200);
    }

    @Test(priority = 2, description = "Verify GET request returns correct content type")
    public void testGetRequestContentType() {
        given()
            .log().uri()
        .when()
            .get("/posts/1")
        .then()
            .contentType("application/json; charset=utf-8")
            .statusCode(200);
    }

    @Test(priority = 3, description = "Verify GET request response body contains expected fields")
    public void testGetRequestResponseBody() {
        given()
            .log().uri()
        .when()
            .get("/posts/1")
        .then()
            .log().body()
            .statusCode(200)
            .body("userId", equalTo(1))
            .body("id", equalTo(1))
            .body("title", notNullValue())
            .body("body", notNullValue());
    }

    @Test(priority = 4, description = "Verify GET request and extract response data")
    public void testGetRequestAndExtractData() {
        Response response = given()
            .log().uri()
        .when()
            .get("/posts/1")
        .then()
            .statusCode(200)
            .extract().response();

        // Assertions using TestNG
        assertEquals(response.getStatusCode(), 200, "Status code should be 200");
        
        int userId = response.jsonPath().getInt("userId");
        int id = response.jsonPath().getInt("id");
        String title = response.jsonPath().getString("title");
        
        assertTrue(userId > 0, "User ID should be greater than 0");
        assertTrue(id > 0, "ID should be greater than 0");
        assertFalse(title.isEmpty(), "Title should not be empty");
        
        System.out.println("Post ID: " + id);
        System.out.println("User ID: " + userId);
        System.out.println("Title: " + title);
    }

    @Test(priority = 5, description = "Verify GET request for all posts returns list")
    public void testGetAllPosts() {
        given()
            .log().uri()
        .when()
            .get("/posts")
        .then()
            .log().status()
            .statusCode(200)
            .body("size()", greaterThan(0))
            .body("[0].userId", notNullValue())
            .body("[0].id", notNullValue());
    }

    @Test(priority = 6, description = "Verify GET request with query parameters")
    public void testGetRequestWithQueryParams() {
        given()
            .log().all()
            .queryParam("userId", 1)
        .when()
            .get("/posts")
        .then()
            .log().body()
            .statusCode(200)
            .body("size()", greaterThan(0))
            .body("[0].userId", equalTo(1));
    }
}
