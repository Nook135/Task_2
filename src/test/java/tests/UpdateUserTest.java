package tests;

import io.qameta.allure.junit4.DisplayName;
import login.LoginMethods;
import login.LoginMethods.TestUserData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import user.User;
import user.UserMethods;
import io.restassured.response.ValidatableResponse;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class UpdateUserTest extends BaseTest {

    private UserMethods userMethods;
    private LoginMethods loginMethods;
    private TestUserData originalUserData;
    private String accessToken; // Токен оригинального пользователя

    @Before
    public void setUp() {
        super.setup();
        userMethods = new UserMethods();
        loginMethods = new LoginMethods();

        // Создаем тестового пользователя
        originalUserData = loginMethods.createTestUser(userMethods);
        accessToken = originalUserData.accessToken;
        System.out.println("Создан тестовый пользователь: " + originalUserData.email);
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
    @DisplayName("Изменение email с авторизацией")
    public void updateEmailWithAuth() {
        // Arrange: готовим новые данные
        String newEmail = "updated_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        User updatedUser = new User(newEmail, originalUserData.password, originalUserData.name);

        // Act: обновляем данные
        ValidatableResponse response = userMethods.update(updatedUser, accessToken);

        // Assert: проверяем успешное обновление
        userMethods.checkUpdateSuccess(response, newEmail, originalUserData.name);

        // Дополнительная проверка: получаем обновленные данные
        ValidatableResponse getUserResponse = userMethods.getUser(accessToken);
        getUserResponse.assertThat()
                .statusCode(200)
                .body("user.email", equalTo(newEmail));
    }

    @Test
    @DisplayName("Изменение имени с авторизацией")
    public void updateNameWithAuth() {
        // Arrange
        String newName = "Updated Name " + UUID.randomUUID().toString().substring(0, 4);
        User updatedUser = new User(originalUserData.email, originalUserData.password, newName);

        // Act
        ValidatableResponse response = userMethods.update(updatedUser, accessToken);

        // Assert
        userMethods.checkUpdateSuccess(response, originalUserData.email, newName);
    }

    @Test
    @DisplayName("Изменение пароля с авторизацией")
    public void updatePasswordWithAuth() {
        // Arrange
        String newPassword = "newpassword123";
        User updatedUser = new User(originalUserData.email, newPassword, originalUserData.name);

        // Act
        ValidatableResponse response = userMethods.update(updatedUser, accessToken);

        // Assert
        userMethods.checkUpdateSuccess(response, originalUserData.email, originalUserData.name);

        // Проверяем, что с новым паролем можно залогиниться
        ValidatableResponse loginResponse = loginMethods.login(originalUserData.email, newPassword);
        loginResponse.assertThat()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Изменение всех полей с авторизацией")
    public void updateAllFieldsWithAuth() {
        // Arrange
        String newEmail = "allfields_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        String newPassword = "newpass123";
        String newName = "All Fields User";
        User updatedUser = new User(newEmail, newPassword, newName);

        // Act
        ValidatableResponse response = userMethods.update(updatedUser, accessToken);

        // Assert
        userMethods.checkUpdateSuccess(response, newEmail, newName);

    }

    @Test
    @DisplayName("Изменение данных без авторизации")
    public void updateUserWithoutAuth() {
        // Arrange
        String newEmail = "unauth_update@example.com";
        User updatedUser = new User(newEmail, "newpassword", "Unauthorized Update");

        // Act
        ValidatableResponse response = userMethods.updateWithoutAuth(updatedUser);

        // Assert
        userMethods.checkUnauthorizedError(response);
    }

}