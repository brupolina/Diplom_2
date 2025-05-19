import io.qameta.allure.Description;
import praktikum.BaseHttpClient;
import praktikum.constants.Endpoints;
import praktikum.constants.Messages;
import praktikum.pojo.User;
import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;

public class UserLoginTest extends BaseHttpClient {
    Faker faker = new Faker();
    String email = faker.internet().emailAddress();
    String password = faker.internet().password();
    String emailIncorrect = faker.internet().emailAddress();
    String passwordIncorrect = faker.internet().password();
    String name = faker.name().username();
    Response response;

    @Before
    @Description("Создание нового пользователя")
    public void setData() {
        User user = new User(email, password, name);
        response = postRequest(Endpoints.USER_CREATE_POST, user);
    }
    @After
    @Description("Удаление созданного пользователя")
    public  void cleanData() {
        String accessToken;
        int statusCode = response.then().extract().statusCode();
        if (statusCode == 200) {
            accessToken = response.then().extract().path("accessToken").toString();
            deleteRequest(Endpoints.ACTIONS_WITH_USER, accessToken);
        }
    }

    @Test
    @DisplayName("Авторизация существующего пользователя с обязательными полями")
    @Description("Проверка статуса 200 и поля 'success': true")
    public void checkUserLogin() {
        Response loginResponse = postRequest(Endpoints.USER_LOGIN_POST, new User(email, password));
        loginResponse.then().statusCode(200)
                .and()
                .body("success", equalTo(true));

    }

    @Test
    @DisplayName("Авторизация существующего пользователя с неправильной почтой")
    @Description("Проверка статуса 401 и поля 'message': email or password are incorrect")
    public void checkUserLoginWithIncorrectEmail() {
        Response loginResponse = postRequest(Endpoints.USER_LOGIN_POST, new User(emailIncorrect, password));
        loginResponse.then().statusCode(401)
                .and()
                .body("success", equalTo(false))
                .body("message", equalTo(Messages.incorrectLoginMessage));
    }

    @Test
    @DisplayName("Авторизация существующего пользователя с неправильным паролем")
    @Description("Проверка статуса 401 и поля 'message': email or password are incorrect")
    public void checkUserLoginWithIncorrectPassword() {
        Response loginResponse = postRequest(Endpoints.USER_LOGIN_POST, new User(email, passwordIncorrect));
        loginResponse.then().statusCode(401)
                .and()
                .body("success", equalTo(false))
                .body("message", equalTo(Messages.incorrectLoginMessage));
    }
    @Test
    @DisplayName("Авторизация существующего пользователя без ввода почты")
    @Description("Проверка статуса 401 и поля 'message': email or password are incorrect")
    public void checkUserLoginWithoutEmail() {
        Response loginResponse = postRequest(Endpoints.USER_LOGIN_POST, new User(null, password));
        loginResponse.then().statusCode(401)
                .and()
                .body("success", equalTo(false))
                .body("message", equalTo(Messages.incorrectLoginMessage));
    }

    @Test
    @DisplayName("Авторизация существующего пользователя без ввода пароля")
    @Description("Проверка статуса 401 и поля 'message': email or password are incorrect")
    public void checkUserLoginWithoutPassword() {
        Response loginResponse = postRequest(Endpoints.USER_LOGIN_POST, new User(email, null));
        loginResponse.then().statusCode(401)
                .and()
                .body("success", equalTo(false))
                .body("message", equalTo(Messages.incorrectLoginMessage));
    }

}