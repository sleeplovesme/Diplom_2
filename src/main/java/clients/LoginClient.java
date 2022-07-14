package clients;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import models.Login;
import models.LoginResponse;

public class LoginClient {

    @Step("Создание объекта логин")
    public static Login createObjectLogin(String email, String password) {
        return new Login(email, password);
    }

    @Step("Десериализация ответа на логин пользователя")
    public static LoginResponse deserialization(Response responseLoginUser) {
        return responseLoginUser.as(LoginResponse.class);
    }
}
