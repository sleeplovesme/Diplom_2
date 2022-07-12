package tests;

import clients.*;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.Login;
import models.LoginResponse;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;

public class GetOrdersTests {

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
        Login loginObject = LoginClient.createObjectLogin(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        Response responseLoginCourier = UserClient.sendPostRequestAuthLogin(loginObject);
        LoginResponse loginResponse = LoginClient.deserialization(responseLoginCourier);
        String accessToken = loginResponse.getAccessToken();

        Response responseGetOrder = OrderClient.sendGetRequestOrders(accessToken);
        ChecksClient.checkExpectedResult(responseGetOrder, SC_OK, EXPECTED_RESULT_TRUE);
    }

    @Test
    @DisplayName("Получение заказов неавторизованным пользователем")
    public void tryGetOrdersUnauthorizedUser() {
        Response responseGetOrder = OrderClient.sendGetRequestOrders(EMPTY_ACCESS_TOKEN);
        ChecksClient.checkExpectedResult(responseGetOrder, SC_UNAUTHORIZED, EXPECTED_RESULT_FALSE);
    }
}
