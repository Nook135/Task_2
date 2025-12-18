package order;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class OrderMethods {

    @Step("Создание заказа")
    public ValidatableResponse create(Order order, String accessToken) {
        if (accessToken != null && !accessToken.isEmpty()) {
            return given().log().all()
                    .header("Authorization", accessToken)
                    .contentType(ContentType.JSON)
                    .body(order)
                    .when()
                    .post("/api/orders")
                    .then().log().all();
        } else {
            return given().log().all()
                    .contentType(ContentType.JSON)
                    .body(order)
                    .when()
                    .post("/api/orders")
                    .then().log().all();
        }
    }

    @Step("Создание заказа с ингредиентами")
    public ValidatableResponse createWithIngredients(List<String> ingredients, String accessToken) {
        Order order = new Order(ingredients);
        return create(order, accessToken);
    }

    @Step("Получение ингредиентов")
    public ValidatableResponse getIngredients() {
        return given().log().all()
                .when()
                .get("/api/ingredients")
                .then().log().all();
    }

    @Step("Получение ID ингредиентов")
    public List<String> getIngredientIds() {
        ValidatableResponse response = getIngredients();
        return response.extract().jsonPath().getList("data._id");
    }

    @Step("Проверка успешного создания заказа")
    public void checkOrderCreatedSuccess(ValidatableResponse response) {
        response.assertThat()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", org.hamcrest.Matchers.notNullValue());
    }

    @Step("Проверка ошибки при создании заказа без ингредиентов")
    public void checkNoIngredientsError(ValidatableResponse response) {
        response.assertThat()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Step("Проверка ошибки при неверном хеше ингредиентов")
    public void checkInvalidIngredientHashError(ValidatableResponse response) {
        response.assertThat()
                .statusCode(500); // Согласно документации
    }

    @Step("Получение заказов пользователя")
    public ValidatableResponse getUserOrders(String accessToken) {
        return given().log().all()
                .header("Authorization", accessToken)
                .when()
                .get("/api/orders")
                .then().log().all();
    }

}