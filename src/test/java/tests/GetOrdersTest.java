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

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class GetOrdersTest extends BaseTest {

    private UserMethods userMethods;
    private LoginMethods loginMethods;
    private OrderMethods orderMethods;
    private TestUserData testUserData;
    private String accessToken;
    private List<String> ingredientIds;

    @Before
    public void setUp() {
        super.setup();
        userMethods = new UserMethods();
        loginMethods = new LoginMethods();
        orderMethods = new OrderMethods();

        // Создаем тестового пользователя
        testUserData = loginMethods.createTestUser(userMethods);
        accessToken = testUserData.accessToken;

        // Получаем ID ингредиентов для создания заказов
        ingredientIds = orderMethods.getIngredientIds();

        System.out.println("Пользователь создан: " + testUserData.email);
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
    @DisplayName("Получение заказов авторизованного пользователя")
    public void getOrdersForUserWithOrders() {
        // Arrange: сначала создаем заказ
        if (!ingredientIds.isEmpty()) {
            List<String> ingredients = ingredientIds.subList(
                    0, Math.min(2, ingredientIds.size())
            );
            ValidatableResponse createResponse = orderMethods.createWithIngredients(ingredients, accessToken);
            createResponse.assertThat().statusCode(200);
            System.out.println("Тестовый заказ создан");
        }

        // Act: получаем заказы
        ValidatableResponse response = orderMethods.getUserOrders(accessToken);

        // Assert
        response.assertThat()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("orders", notNullValue());

        // Проверяем, что в ответе есть массив заказов
        List<?> orders = response.extract().jsonPath().getList("orders");
        System.out.println("Заказов после создания: " + orders.size());

    }

    @Test
    @DisplayName("Получение заказов неавторизованного пользователя")
    public void getOrdersForUnauthorizedUser() {
        // Act: пытаемся получить заказы без токена
        ValidatableResponse response = given()
                .when()
                .get("/api/orders")
                .then().log().all();

        // Assert: должна быть ошибка 401
        response.assertThat()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }
}