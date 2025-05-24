package praktikum;

import io.qameta.allure.Step;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import praktikum.constants.Endpoints;

import static io.restassured.RestAssured.given;

public abstract class BaseHttpClient {
    private RequestSpecification requestSpecification = new RequestSpecBuilder()
            .setBaseUri(Endpoints.BASE_HOST)
            .addHeader("Content-Type","application/json")
            .addFilter(new RequestLoggingFilter())
            .addFilter(new ResponseLoggingFilter())
            .build();
    @Step("Отправка POST запроса")
    public Response postRequest(String endpoint, Object body) {
        return given()
                .spec(requestSpecification)
                .body(body)
                .when()
                .post(endpoint)
                .thenReturn();
    }

    @Step("Отправка PATCH запроса")
    public Response patchRequest(String endpoint, Object body, String token) {
        return given()
                .spec(requestSpecification)
                .header("Authorization", token)
                .body(body)
                .when()
                .patch(endpoint)
                .thenReturn();
    }

    @Step("Отправка DELETE запроса")
    public void deleteRequest(String endpoint, String token) {
        given()
                .spec(requestSpecification)
                .header("Authorization", token)
                .delete(endpoint);
    }

    @Step("Отправка GET запроса")
    public Response getRequest(String endpoint, String token) {
        return given()
                .spec(requestSpecification)
                .header("Authorization", token)
                .get(endpoint)
                .thenReturn();
    }

}