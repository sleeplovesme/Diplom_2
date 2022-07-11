package tests;

import client.OrderClient;
import client.UserClient;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.CreateOrder;
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

public class OrderTests {

    private String randomString = RandomStringUtils.randomAlphanumeric(5);
    private String password = RandomStringUtils.randomNumeric(4);
    private String name = RandomStringUtils.randomAlphabetic(6);

    private String[] mailCompanies = new String[]{"yandex", "mail", "rambler"};
    private int randomMailCompany = new Random().nextInt(mailCompanies.length);
    private String email = randomString + "@" + mailCompanies[randomMailCompany] + ".ru";

    private final static boolean EXPECTED_RESULT_TRUE = true;
    private final static boolean EXPECTED_RESULT_FALSE = false;
    private final static String[] EMPTY_INGREDIENTS = {};
    private final static String[] INGREDIENTS = {"61c0c5a71d1f82001bdaaa6d",
            "61c0c5a71d1f82001bdaaa6f", "61c0c5a71d1f82001bdaaa72"};
    private final static String[] INVALID_INGREDIENTS_HASH = {"123", "000", "777"};
    private final static String EMPTY_ACCESS_TOKEN = "";

    Response responseCreateUser;
    LoginResponse loginResponse;
    Response responseCreateOrder;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
    }

    @After
    public void clear() {
        if (loginResponse != null) {
            Response responseDeleteCourier = UserClient.sendDeleteRequestAuthUser(loginResponse.getAccessToken());
            checkExpectedResult(responseDeleteCourier, SC_ACCEPTED, EXPECTED_RESULT_TRUE);
        }
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и с ингредиентами")
    public void createNewUserAndCreateOrder() {
        CreateUser user = createObjectUser(email, password, name);
        responseCreateUser = UserClient.sendPostRequestAuthRegister(user);
        checkExpectedResult(responseCreateUser, SC_OK, EXPECTED_RESULT_TRUE);

        Login loginObject = createObjectLogin(email, password);
        Response responseLoginCourier = UserClient.sendPostRequestAuthLogin(loginObject);
        loginResponse = deserialization(responseLoginCourier);
        String accessToken = loginResponse.getAccessToken();

        CreateOrder order = createObjectOrder(INGREDIENTS);
        responseCreateOrder = OrderClient.sendPostRequestOrders(accessToken, order);
        checkExpectedResult(responseCreateOrder, SC_OK, EXPECTED_RESULT_TRUE);
    }

    @Test
    @DisplayName("Создание заказа с авторизацией, но без ингредиентов")
    public void createNewUserTryCreateOrderWithoutIngredients() {
        CreateUser user = createObjectUser(email, password, name);
        responseCreateUser = UserClient.sendPostRequestAuthRegister(user);
        checkExpectedResult(responseCreateUser, SC_OK, EXPECTED_RESULT_TRUE);

        Login loginObject = createObjectLogin(email, password);
        Response responseLoginCourier = UserClient.sendPostRequestAuthLogin(loginObject);
        loginResponse = deserialization(responseLoginCourier);
        String accessToken = loginResponse.getAccessToken();

        CreateOrder order = createObjectOrder(EMPTY_INGREDIENTS);
        responseCreateOrder = OrderClient.sendPostRequestOrders(accessToken, order);
        checkExpectedResult(responseCreateOrder, SC_BAD_REQUEST, EXPECTED_RESULT_FALSE);
    }

    @Test
    @DisplayName("Попытка создания заказа без авторизации и без ингредиентов")
    public void tryCreateOrderWithoutAuthorizationAndIngredients() {
        CreateOrder order = createObjectOrder(EMPTY_INGREDIENTS);
        responseCreateOrder = OrderClient.sendPostRequestOrders(EMPTY_ACCESS_TOKEN, order);
        checkExpectedResult(responseCreateOrder, SC_BAD_REQUEST, EXPECTED_RESULT_FALSE);
    }

    @Test
    @DisplayName("Создание заказа без авторизации, но с ингредиентамит")
    public void tryCreateOrderWithoutAuthorizationWithIngredients() {
        CreateOrder order = createObjectOrder(INGREDIENTS);
        responseCreateOrder = OrderClient.sendPostRequestOrders(EMPTY_ACCESS_TOKEN, order);
        checkExpectedResult(responseCreateOrder, SC_OK, EXPECTED_RESULT_TRUE);
    }

    @Test
    @DisplayName("Попытка создания заказа с авторизацией и с неверным хешем ингредиентов")
    public void tryCreateOrderWithAuthorizationAndErrorIngredients() {
        CreateUser user = createObjectUser(email, password, name);
        responseCreateUser = UserClient.sendPostRequestAuthRegister(user);
        checkExpectedResult(responseCreateUser, SC_OK, EXPECTED_RESULT_TRUE);

        Login loginObject = createObjectLogin(email, password);
        Response responseLoginCourier = UserClient.sendPostRequestAuthLogin(loginObject);
        loginResponse = deserialization(responseLoginCourier);
        String accessToken = loginResponse.getAccessToken();

        CreateOrder order = createObjectOrder(INVALID_INGREDIENTS_HASH);
        responseCreateOrder = OrderClient.sendPostRequestOrders(accessToken, order);
        checkStatusCodeWithInvalidHash(responseCreateOrder, SC_INTERNAL_SERVER_ERROR);
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

    @Step("Создание объекта заказ")
    public CreateOrder createObjectOrder(String[] ingredients) {
        return new CreateOrder(ingredients);
    }

    @Step("Проверка соответствия кода ответа при создании заказа с невалидным хэшем")
    public void checkStatusCodeWithInvalidHash(Response response, int statusCode) {
        response.then().assertThat().statusCode(statusCode);
    }
}
