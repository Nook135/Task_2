package login;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import user.User;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class LoginMethods {

    @Step("Логин пользователя")
    public ValidatableResponse login(Login login) {
        return given().log().all()
                .contentType(ContentType.JSON)
                .body(login)
                .when()
                .post("/api/auth/login")
                .then().log().all();
    }

    @Step("Логин пользователя с email и паролем")
    public ValidatableResponse login(String email, String password) {
        Login login = new Login(email, password);
        return login(login);
    }

    @Step("Проверка успешного логина")
    public String checkLoginSuccess(ValidatableResponse loginResponse) {
        return loginResponse
                .assertThat()
                .statusCode(200)
                .extract()
                .path("accessToken");
    }

    @Step("Проверка ошибки при неверных credentials")
    public void checkInvalidCredentialsError(ValidatableResponse response) {
        response.assertThat()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Step("Создание тестового пользователя и возвращение данных")
    public TestUserData createTestUser(user.UserMethods userMethods) {
        String email = userMethods.generateUniqueEmail();
        String password = "password123";
        String name = "Test User";

        User user = new User(email, password, name);
        ValidatableResponse createResponse = userMethods.create(user);
        String accessToken = userMethods.checkCreated(createResponse);

        return new TestUserData(email, password, name, accessToken);
    }

    // Вспомогательный класс для хранения данных тестового пользователя
    public static class TestUserData {
        public final String email;
        public final String password;
        public final String name;
        public final String accessToken;

        public TestUserData(String email, String password, String name, String accessToken) {
            this.email = email;
            this.password = password;
            this.name = name;
            this.accessToken = accessToken;
        }
    }
}