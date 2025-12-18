package order;

import java.util.List;

public class Order {
    private List<String> ingredients;

    // Конструктор по умолчанию
    public Order() {}

    // Конструктор с ингредиентами
    public Order(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    // Геттеры и сеттеры
    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }
}