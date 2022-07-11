package tests;

import client.*;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.Login;
import models.LoginResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.Matchers.equalTo;

public class GetOrdersTests {

    private String randomString = RandomStringUtils.randomAlphanumeric(5);
    private String password = RandomStringUtils.randomNumeric(4);

    private String[] mailCompanies = new String[]{"yandex", "mail", "rambler"};
    private int randomMailCompany = new Random().nextInt(mailCompanies.length);
    private String email = randomString + "@" + mailCompanies[randomMailCompany] + ".ru";

    private final static boolean EXPECTED_RESULT_TRUE = true;
    private final static boolean EXPECTED_RESULT_FALSE = false;
    private final static String DEFAULT_EMAIL = "test-data@yandex.ru";
    private final static String DEFAULT_PASSWORD = "password";
    private final static String EMPTY_ACCESS_TOKEN = "";

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
    }

    @Test
    @DisplayName("Получение заказов авторизованным пользователем")
    public void getOrdersAuthorizedUser() {
        Login loginObject = createObjectLogin(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        Response responseLoginCourier = UserClient.sendPostRequestAuthLogin(loginObject);
        LoginResponse loginResponse = deserialization(responseLoginCourier);
        String accessToken = loginResponse.getAccessToken();

        Response responseGetOrder = OrderClient.sendGetRequestOrders(accessToken);
        checkExpectedResult(responseGetOrder, SC_OK, EXPECTED_RESULT_TRUE);
    }

    @Test
    @DisplayName("Получение заказов неавторизованным пользователем")
    public void tryGetOrdersUnauthorizedUser() {
        Response responseGetOrder = OrderClient.sendGetRequestOrders(EMPTY_ACCESS_TOKEN);
        checkExpectedResult(responseGetOrder, SC_UNAUTHORIZED, EXPECTED_RESULT_FALSE);
    }

    @Step("Создание объекта логин")
    public Login createObjectLogin(String email, String password) {
        return new Login(email, password);
    }

    @Step("Десериализация ответа на логин пользователя")
    public LoginResponse deserialization(Response responseLoginUser) {
        return responseLoginUser.as(LoginResponse.class);
    }

    @Step("Проверка соответствия ожидаемого результата")
    public void checkExpectedResult(Response response, int statusCode, boolean expectedResult) {
        response.then().assertThat().statusCode(statusCode).and().body("success", equalTo(expectedResult));
    }
}
