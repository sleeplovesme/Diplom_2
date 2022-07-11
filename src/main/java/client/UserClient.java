package client;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import models.CreateUser;
import models.Login;

import static io.restassured.RestAssured.given;

public class UserClient {

    @Step("Отправка POST запроса на /api/auth/register")
    public static Response sendPostRequestAuthRegister(CreateUser user) {
        return given().contentType(ContentType.JSON).body(user).post("/api/auth/register");
    }

    @Step("Отправка POST запроса на /api/auth/login")
    public static Response sendPostRequestAuthLogin(Login login) {
        return given().contentType(ContentType.JSON).body(login).post("/api/auth/login");
    }

    @Step("Отправка DELETE запроса на /api/auth/user")
    public static Response sendDeleteRequestAuthUser(String accessToken) {
        return given().header("Authorization", accessToken).delete("/api/auth/user");
    }

    @Step("Отправка PATCH запроса на /api/auth/user")
    public static Response sendPatchRequestAuthUser(String accessToken, CreateUser user) {
        return given().header("Authorization", accessToken).contentType(ContentType.JSON)
                .body(user).patch("/api/auth/user");
    }
}
