package tests;

import io.qameta.allure.junit4.DisplayName;
import login.LoginMethods;
import login.LoginMethods.TestUserData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import user.UserMethods;
import io.restassured.response.ValidatableResponse;

import static org.hamcrest.Matchers.equalTo;

public class LoginUserTest extends BaseTest {

    private UserMethods userMethods;
    private LoginMethods loginMethods;
    private TestUserData testUserData;
    private String accessToken; // Для очистки

    @Before
    public void setUp() {
        super.setup();
        userMethods = new UserMethods();
        loginMethods = new LoginMethods();
        testUserData = null;
        accessToken = null;
    }

    @After
    public void tearDown() {
        // Очистка после каждого теста
        if (accessToken != null && !accessToken.isEmpty()) {
            userMethods.delete(accessToken);
            System.out.println("Тестовый пользователь удален");
        }
    }

    @Test
    @DisplayName("Логин под существующим пользователем")
    public void loginWithExistingUser() {
        // Arrange: создаем тестового пользователя
        testUserData = loginMethods.createTestUser(userMethods);
        accessToken = testUserData.accessToken; // Сохраняем для очистки

        // Act: логинимся с правильными данными
        ValidatableResponse response = loginMethods.login(
                testUserData.email,
                testUserData.password
        );

        // Assert: проверяем успешный логин
        String loginAccessToken = loginMethods.checkLoginSuccess(response);

        // Дополнительные проверки
        response.assertThat()
                .body("success", equalTo(true))
                .body("user.email", equalTo(testUserData.email))
                .body("user.name", equalTo(testUserData.name));

        System.out.println("AccessToken из логина: " + loginAccessToken);
    }

    @Test
    @DisplayName("Логин с неверным email")
    public void loginWithWrongEmail() {
        // Arrange: создаем тестового пользователя
        testUserData = loginMethods.createTestUser(userMethods);
        accessToken = testUserData.accessToken;

        // Act: логинимся с неверным email
        String wrongEmail = "wrong_" + testUserData.email;
        ValidatableResponse response = loginMethods.login(
                wrongEmail,
                testUserData.password
        );

        // Assert: проверяем ошибку
        loginMethods.checkInvalidCredentialsError(response);
    }

    @Test
    @DisplayName("Логин с неверным паролем")
    public void loginWithWrongPassword() {
        // Arrange: создаем тестового пользователя
        testUserData = loginMethods.createTestUser(userMethods);
        accessToken = testUserData.accessToken;

        // Act: логинимся с неверным паролем
        String wrongPassword = "wrongpassword";
        ValidatableResponse response = loginMethods.login(
                testUserData.email,
                wrongPassword
        );

        // Assert: проверяем ошибку
        loginMethods.checkInvalidCredentialsError(response);
    }

    @Test
    @DisplayName("Логин с неверным email и паролем")
    public void loginWithWrongEmailAndPassword() {
        // Arrange: создаем тестового пользователя
        testUserData = loginMethods.createTestUser(userMethods);
        accessToken = testUserData.accessToken;

        // Act: логинимся с неверными данными
        ValidatableResponse response = loginMethods.login(
                "nonexistent@example.com",
                "wrongpassword"
        );

        // Assert: проверяем ошибку
        loginMethods.checkInvalidCredentialsError(response);
    }

}
