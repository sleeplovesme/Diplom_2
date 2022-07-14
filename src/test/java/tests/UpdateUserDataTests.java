package tests;

import clients.*;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.apache.http.HttpStatus.*;

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
            ChecksClient.checkExpectedResult(responseDeleteCourier, SC_ACCEPTED, EXPECTED_RESULT_TRUE);
        }
    }

    @Test
    @DisplayName("Изменение данных пользователя с авторизацией")
    public void createNewUserAndUpdateData() {
        CreateUser user = UserClient.createObjectUser(email, password, name);
        responseCreateUser = UserClient.sendPostRequestAuthRegister(user);
        ChecksClient.checkExpectedResult(responseCreateUser, SC_OK, EXPECTED_RESULT_TRUE);

        Login loginObject = LoginClient.createObjectLogin(email, password);
        Response responseLoginCourier = UserClient.sendPostRequestAuthLogin(loginObject);
        loginResponse = LoginClient.deserialization(responseLoginCourier);
        String accessToken = loginResponse.getAccessToken();

        CreateUser newUser = UserClient.createObjectUser(email, password, name);
        responseUpdateDataUser = UserClient.sendPatchRequestAuthUser(accessToken, newUser);
        ChecksClient.checkExpectedResultAfterUpdate(responseUpdateDataUser, SC_OK, EXPECTED_RESULT_TRUE);
    }

    @Test
    @DisplayName("Изменение данных пользователя без авторизации")
    public void updateUserDataWithoutLogin() {
        CreateUser newUser = UserClient.createObjectUser(email, password, name);
        responseUpdateDataUser = UserClient.sendPatchRequestAuthUser(EMPTY_ACCESS_TOKEN, newUser);
        ChecksClient.checkExpectedResultAfterUpdate(responseUpdateDataUser, SC_UNAUTHORIZED, EXPECTED_RESULT_FALSE);
    }
}
