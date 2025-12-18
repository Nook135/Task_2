package login;

public class Login {
    private String email;
    private String password;

    // Конструктор по умолчанию
    public Login() {}

    // Конструктор с полями
    public Login(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Геттеры и сеттеры
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}