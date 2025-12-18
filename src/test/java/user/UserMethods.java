package user;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class UserMethods {

    @Step("Создание пользователя")
    public ValidatableResponse create(User user) {
        return given().log().all()
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .post("/api/auth/register")
                .then().log().all();
    }

    @Step("Удаление пользователя")
    public ValidatableResponse delete(String accessToken) {
        return given().log().all()
                .header("Authorization", accessToken)
                .when()
                .delete("/api/auth/user")
                .then().log().all();
    }

    @Step("Проверка успешного создания пользователя")
    public String checkCreated(ValidatableResponse createResponse) {
        return createResponse
                .assertThat()
                .statusCode(200)
                .extract()
                .path("accessToken");
    }

    @Step("Проверка ошибки при создании дубликата пользователя")
    public void checkDuplicateError(ValidatableResponse response) {
        response.assertThat()
                .statusCode(403)
                .body("message", equalTo("User already exists"));
    }

    @Step("Проверка ошибки при отсутствии обязательных полей")
    public void checkRequiredFieldsError(ValidatableResponse response) {
        response.assertThat()
                .statusCode(403)
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Step("Генерация уникального email")
    public String generateUniqueEmail() {
        return "testuser_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    // методы для проверки изменения данных пользователя
    @Step("Обновление данных пользователя")
    public ValidatableResponse update(User user, String accessToken) {
        return given().log().all()
                .header("Authorization", accessToken)
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .patch("/api/auth/user")
                .then().log().all();
    }

    @Step("Обновление данных пользователя без авторизации")
    public ValidatableResponse updateWithoutAuth(User user) {
        return given().log().all()
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .patch("/api/auth/user")
                .then().log().all();
    }

    @Step("Получение данных пользователя")
    public ValidatableResponse getUser(String accessToken) {
        return given().log().all()
                .header("Authorization", accessToken)
                .when()
                .get("/api/auth/user")
                .then().log().all();
    }

    @Step("Проверка успешного обновления данных")
    public void checkUpdateSuccess(ValidatableResponse response, String expectedEmail, String expectedName) {
        response.assertThat()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(expectedEmail))
                .body("user.name", equalTo(expectedName));
    }

    @Step("Проверка ошибки при обновлении без авторизации")
    public void checkUnauthorizedError(ValidatableResponse response) {
        response.assertThat()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Step("Проверка ошибки при обновлении на существующий email")
    public void checkEmailAlreadyExistsError(ValidatableResponse response) {
        response.assertThat()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User with such email already exists"));
    }
}
