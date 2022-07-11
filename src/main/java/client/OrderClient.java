package client;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import models.CreateOrder;

import static io.restassured.RestAssured.given;

public class OrderClient {

    @Step("Отправка POST запроса на api/orders")
    public static Response sendPostRequestOrders(String accessToken, CreateOrder ingredients) {
        return given().header("Authorization", accessToken).contentType(ContentType.JSON)
                .body(ingredients).post("/api/orders");
    }

    @Step("Отправка GET запроса на api/orders")
    public static Response sendGetRequestOrders(String accessToken) {
        return given().header("Authorization", accessToken).contentType(ContentType.JSON).get("/api/orders");
    }
}
