package tests;

import user.User;
import user.UserMethods;
import io.qameta.allure.junit4.DisplayName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.restassured.response.ValidatableResponse;

import static org.hamcrest.Matchers.equalTo;

public class CreateUserTest extends BaseTest {

    private UserMethods userMethods;
    private String accessToken;
    private User testUser;

    @Before
    public void setUp() {
        super.setup();
        userMethods = new UserMethods();
        accessToken = null;
        testUser = null;
    }

    @After
    public void tearDown() {
        // Очистка после теста
        if (accessToken != null && !accessToken.isEmpty()) {
            userMethods.delete(accessToken);
        }
    }

    @Test
    @DisplayName("Создание уникального пользователя")
    public void createUniqueUserSuccessfully() {
        // Arrange
        String uniqueEmail = userMethods.generateUniqueEmail();
        testUser = new User(uniqueEmail, "password123", "Test User");

        // Act
        ValidatableResponse response = userMethods.create(testUser);

        // Assert
        accessToken = userMethods.checkCreated(response);

        // Дополнительные проверки
        response.assertThat()
                .body("success", equalTo(true))
                .body("user.email", equalTo(uniqueEmail))
                .body("user.name", equalTo("Test User"));
    }

    @Test
    @DisplayName("Создание пользователя, который уже зарегистрирован")
    public void createExistingUserShouldFail() {
        // Arrange: сначала создаем пользователя
        String email = userMethods.generateUniqueEmail();
        testUser = new User(email, "password123", "First User");

        ValidatableResponse firstResponse = userMethods.create(testUser);
        accessToken = userMethods.checkCreated(firstResponse);

        // Act: пытаемся создать такого же
        User duplicateUser = new User(email, "password123", "Duplicate User");
        ValidatableResponse duplicateResponse = userMethods.create(duplicateUser);

        // Assert
        userMethods.checkDuplicateError(duplicateResponse);
    }

    @Test
    @DisplayName("Создание пользователя без email")
    public void createUserWithoutEmailShouldFail() {
        // Arrange
        testUser = new User("", "password123", "Test User");

        // Act
        ValidatableResponse response = userMethods.create(testUser);

        // Assert
        userMethods.checkRequiredFieldsError(response);
    }

    @Test
    @DisplayName("Создание пользователя без пароля")
    public void createUserWithoutPasswordShouldFail() {
        // Arrange
        testUser = new User(userMethods.generateUniqueEmail(), "", "Test User");

        // Act
        ValidatableResponse response = userMethods.create(testUser);

        // Assert
        userMethods.checkRequiredFieldsError(response);
    }

    @Test
    @DisplayName("Создание пользователя без имени")
    public void createUserWithoutNameShouldFail() {
        // Arrange
        testUser = new User(userMethods.generateUniqueEmail(), "password123", "");

        // Act
        ValidatableResponse response = userMethods.create(testUser);

        // Assert
        userMethods.checkRequiredFieldsError(response);
    }
}