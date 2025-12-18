package tests;

import io.qameta.allure.junit4.DisplayName;
import login.LoginMethods;
import login.LoginMethods.TestUserData;
import order.OrderMethods;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import user.UserMethods;
import io.restassured.response.ValidatableResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;

public class CreateOrderTest extends BaseTest {

    private UserMethods userMethods;
    private LoginMethods loginMethods;
    private OrderMethods orderMethods;
    private TestUserData testUserData;
    private String accessToken;
    private List<String> validIngredientIds;

    @Before
    public void setUp() {
        super.setup();
        userMethods = new UserMethods();
        loginMethods = new LoginMethods();
        orderMethods = new OrderMethods();

        // Создаем тестового пользователя
        testUserData = loginMethods.createTestUser(userMethods);
        accessToken = testUserData.accessToken;

        // Получаем реальные ID ингредиентов с сервера
        validIngredientIds = orderMethods.getIngredientIds();

        System.out.println("Получено ингредиентов: " + validIngredientIds.size());
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
    @DisplayName("Создание заказа с авторизацией и ингредиентами")
    public void createOrderWithAuthAndIngredients() {
        // Проверяем, что есть доступные ингредиенты
        if (validIngredientIds.isEmpty()) {
            System.out.println("Нет доступных ингредиентов, тест пропущен");
            return;
        }

        // Arrange: берем первые 2 ингредиента
        List<String> ingredients = validIngredientIds.subList(
                0, Math.min(2, validIngredientIds.size())
        );

        // Act
        ValidatableResponse response = orderMethods.createWithIngredients(ingredients, accessToken);

        // Assert
        orderMethods.checkOrderCreatedSuccess(response);
    }

    @Test
    @DisplayName("Создание заказа без авторизации с ингредиентами")
    public void createOrderWithoutAuthWithIngredients() {
        if (validIngredientIds.isEmpty()) {
            System.out.println("Нет доступных ингредиентов, тест пропущен");
            return;
        }

        // Arrange
        List<String> ingredients = validIngredientIds.subList(
                0, Math.min(2, validIngredientIds.size())
        );

        // Act: передаем null вместо токена
        ValidatableResponse response = orderMethods.createWithIngredients(ingredients, null);

        // Assert: заказ должен создаваться и без авторизации
        orderMethods.checkOrderCreatedSuccess(response);
    }

    @Test
    @DisplayName("Создание заказа с авторизацией без ингредиентов")
    public void createOrderWithAuthWithoutIngredients() {
        // Arrange: пустой список ингредиентов
        List<String> ingredients = Collections.emptyList();

        // Act
        ValidatableResponse response = orderMethods.createWithIngredients(ingredients, accessToken);

        // Assert: должна быть ошибка 400
        orderMethods.checkNoIngredientsError(response);
    }

    @Test
    @DisplayName("Создание заказа без авторизации и без ингредиентов")
    public void createOrderWithoutAuthAndIngredients() {
        // Arrange
        List<String> ingredients = Collections.emptyList();

        // Act
        ValidatableResponse response = orderMethods.createWithIngredients(ingredients, null);

        // Assert
        orderMethods.checkNoIngredientsError(response);
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиентов")
    public void createOrderWithInvalidIngredientHash() {
        // Arrange: невалидные ID ингредиентов
        List<String> invalidIngredients = Arrays.asList(
                "invalid_hash_123",
                "another_invalid_456"
        );

        // Act
        ValidatableResponse response = orderMethods.createWithIngredients(invalidIngredients, accessToken);

        // Assert: согласно документации - 500 ошибка
        orderMethods.checkInvalidIngredientHashError(response);
    }

}