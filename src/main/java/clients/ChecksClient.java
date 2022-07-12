package clients;

import io.qameta.allure.Step;
import io.restassured.response.Response;

import static org.hamcrest.Matchers.equalTo;

public class ChecksClient {

    @Step("Проверка соответствия ожидаемого результата")
    public static void checkExpectedResult(Response response, int statusCode, boolean expectedResult) {
        response.then().assertThat().statusCode(statusCode).and().body("success", equalTo(expectedResult));
    }

    @Step("Проверка соответствия текста ошибки")
    public static void checkErrorMessage(Response response, int statusCode, boolean expectedResult, String errorMessage) {
        response.then().assertThat().statusCode(statusCode)
                .and().body("success", equalTo(expectedResult))
                .and().body("message", equalTo(errorMessage));
    }

    @Step("Проверка соответствия ожидаемого результата после изменения данных пользователя")
    public static void checkExpectedResultAfterUpdate(Response response, int statusCode, boolean expectedResult) {
        response.then().assertThat().statusCode(statusCode).and().body("success", equalTo(expectedResult));
    }
}
