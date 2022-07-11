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

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.hamcrest.Matchers.equalTo;

public class RegisterTests {

    private String randomString = RandomStringUtils.randomAlphanumeric(5);
    private String password = RandomStringUtils.randomNumeric(4);
    private String name = RandomStringUtils.randomAlphabetic(6);

    private String[] mailCompanies = new String[]{"yandex", "mail", "rambler"};
    private int randomMailCompany = new Random().nextInt(mailCompanies.length);
    private String email = randomString + "@" + mailCompanies[randomMailCompany] + ".ru";

    private final static boolean EXPECTED_RESULT_TRUE = true;
    private final static boolean EXPECTED_RESULT_FALSE = false;
    private final static String ALREADY_EXISTS = "User already exists";
    private final static String REQUIRED_FIELDS = "Email, password and name are required fields";
    private final static String DEFAULT_EMAIL = "test-data@yandex.ru";
    private final static String EMPTY_PASSWORD = "";

    Response responseCreateUser;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
    }

    @After
    public void clear() {
        if (responseCreateUser.statusCode() == SC_OK) {
            Login loginObject = createObjectLogin(email, password);
            Response responseLoginCourier = UserClient.sendPostRequestAuthLogin(loginObject);
            LoginResponse loginResponse = deserialization(responseLoginCourier);
            String accessToken = loginResponse.getAccessToken();

            Response responseDeleteCourier = UserClient.sendDeleteRequestAuthUser(accessToken);
            checkExpectedResult(responseDeleteCourier, SC_ACCEPTED, EXPECTED_RESULT_TRUE);
        }
    }

    @Test
    @DisplayName("Создать уникального пользователя")
    public void createNewUser() {
        CreateUser user = createObjectUser(email, password, name);
        responseCreateUser = UserClient.sendPostRequestAuthRegister(user);
        checkExpectedResult(responseCreateUser, SC_OK, EXPECTED_RESULT_TRUE);
    }

    @Test
    @DisplayName("Создать пользователя, который уже зарегистрирован")
    public void createAlreadyExistsUser() {
        CreateUser user = createObjectUser(DEFAULT_EMAIL, password, name);
        responseCreateUser = UserClient.sendPostRequestAuthRegister(user);
        checkErrorMessage(responseCreateUser, SC_FORBIDDEN, EXPECTED_RESULT_FALSE, ALREADY_EXISTS);
    }

    @Test
    @DisplayName("Создать пользователя и не заполнить одно из обязательных полей")
    public void createUserWithoutPassword() {
        CreateUser user = createObjectUser(email, EMPTY_PASSWORD, name);
        responseCreateUser = UserClient.sendPostRequestAuthRegister(user);
        checkErrorMessage(responseCreateUser, SC_FORBIDDEN, EXPECTED_RESULT_FALSE, REQUIRED_FIELDS);
    }


    @Step("Создание объекта пользователь")
    public CreateUser createObjectUser(String email, String password, String name) {
        return new CreateUser(email, password, name);
    }

    @Step("Проверка соответствия ожидаемого результата")
    public void checkExpectedResult(Response response, int statusCode, boolean expectedResult) {
        response.then().assertThat().statusCode(statusCode).and().body("success", equalTo(expectedResult));
    }

    @Step("Проверка соответствия текста ошибки")
    public void checkErrorMessage(Response response, int statusCode, boolean expectedResult, String errorMessage) {
        response.then().assertThat().statusCode(statusCode)
                .and().body("success", equalTo(expectedResult))
                .and().body("message", equalTo(errorMessage));
    }

    @Step("Создание объекта логин")
    public Login createObjectLogin(String email, String password) {
        return new Login(email, password);
    }

    @Step("Десериализация ответа на логин пользователя")
    public LoginResponse deserialization(Response responseLoginUser) {
        return responseLoginUser.as(LoginResponse.class);
    }
}
