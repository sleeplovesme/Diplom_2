package tests;

import client.UserClient;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.CreateUser;
import models.Login;
import models.LoginResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.equalTo;

public class UpdateUserDataTests {

    private String randomString = RandomStringUtils.randomAlphanumeric(5);
    private String password = RandomStringUtils.randomNumeric(4);
    private String name = RandomStringUtils.randomAlphabetic(6);

    private String[] mailCompanies = new String[]{"yandex", "mail", "rambler"};
    private int randomMailCompany = new Random().nextInt(mailCompanies.length);
    private String email = randomString + "@" + mailCompanies[randomMailCompany] + ".ru";

    private final static boolean EXPECTED_RESULT_TRUE = true;
    private final static boolean EXPECTED_RESULT_FALSE = false;
    private final static String EMPTY_ACCESS_TOKEN = "";

    Response responseCreateUser;
    LoginResponse loginResponse;
    Response responseUpdateDataUser;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
    }

    @After
    public void clear() {
        if (responseUpdateDataUser.statusCode() == SC_OK) {
            Response responseDeleteCourier = UserClient.sendDeleteRequestAuthUser(loginResponse.getAccessToken());
            checkExpectedResult(responseDeleteCourier, SC_ACCEPTED, EXPECTED_RESULT_TRUE);
        }
    }

    @Test
    @DisplayName("Изменение данных пользователя с авторизацией")
    public void createNewUserAndUpdateData() {
        CreateUser user = createObjectUser(email, password, name);
        responseCreateUser = UserClient.sendPostRequestAuthRegister(user);
        checkExpectedResult(responseCreateUser, SC_OK, EXPECTED_RESULT_TRUE);

        Login loginObject = createObjectLogin(email, password);
        Response responseLoginCourier = UserClient.sendPostRequestAuthLogin(loginObject);
        loginResponse = deserialization(responseLoginCourier);
        String accessToken = loginResponse.getAccessToken();

        CreateUser newUser = createObjectUser(email, password, name);
        responseUpdateDataUser = UserClient.sendPatchRequestAuthUser(accessToken, newUser);
        checkExpectedResultAfterUpdate(responseUpdateDataUser, SC_OK, EXPECTED_RESULT_TRUE);
    }

    @Test
    @DisplayName("Изменение данных пользователя без авторизации")
    public void updateUserDataWithoutLogin() {
        CreateUser newUser = createObjectUser(email, password, name);
        responseUpdateDataUser = UserClient.sendPatchRequestAuthUser(EMPTY_ACCESS_TOKEN, newUser);
        checkExpectedResultAfterUpdate(responseUpdateDataUser, SC_UNAUTHORIZED, EXPECTED_RESULT_FALSE);
    }

    @Step("Создание объекта пользователь")
    public CreateUser createObjectUser(String email, String password, String name) {
        return new CreateUser(email, password, name);
    }

    @Step("Проверка соответствия ожидаемого результата")
    public void checkExpectedResult(Response response, int statusCode, boolean expectedResult) {
        response.then().assertThat().statusCode(statusCode).and().body("success", equalTo(expectedResult));
    }

    @Step("Создание объекта логин")
    public Login createObjectLogin(String email, String password) {
        return new Login(email, password);
    }

    @Step("Десериализация ответа на логин пользователя")
    public LoginResponse deserialization(Response responseLoginUser) {
        return responseLoginUser.as(LoginResponse.class);
    }

    @Step("Проверка соответствия ожидаемого результата после изменения данных пользователя")
    public void checkExpectedResultAfterUpdate(Response response, int statusCode, boolean expectedResult) {
        response.then().assertThat().statusCode(statusCode).and().body("success", equalTo(expectedResult));
    }
}
