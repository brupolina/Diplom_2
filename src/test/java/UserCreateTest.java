import io.qameta.allure.Description;
import praktikum.BaseTest;
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

public class UserCreateTest extends BaseTest {
    Faker faker = new Faker();
    String email;
    String password;
    String name;
    Response response;

    BaseHttpClient httpClient = new BaseHttpClient() {};

    @Before
    @Description("Генерация уникальных данных пользователя перед тестом")
    public void setUp() {
        email = faker.internet().emailAddress();
        password = faker.internet().password();
        name = faker.name().username();
    }

    @After
    @Description("Удаление созданного пользователя")
    public void cleanData() {
        if (response != null) {
            int statusCode = response.then().extract().statusCode();
            if (statusCode == 200) {
                String accessToken = response.then().extract().path("accessToken").toString();
                httpClient.deleteRequest(Endpoints.ACTIONS_WITH_USER, accessToken);
            }
        }
    }

    @Test
    @DisplayName("Создание уникального пользователя")
    @Description("Проверка статуса 200 и поля 'success': true")
    public void checkUserRegistration() {
        User user = new User(email, password, name);
        response = httpClient.postRequest(Endpoints.USER_CREATE_POST, user);
        response.then().statusCode(200)
                .and()
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Повторное создание уже существующего пользователя")
    @Description("Проверка статуса 403 и поля 'message': User already exists")
    public void checkUserRegistrationAlreadyRegistered() {
        User user = new User(email, password, name);
        response = httpClient.postRequest(Endpoints.USER_CREATE_POST, user);

        Response responseSecond = httpClient.postRequest(Endpoints.USER_CREATE_POST, user);
        responseSecond.then().statusCode(403)
                .and()
                .body("success", equalTo(false))
                .body("message", equalTo(Messages.EXISTING_USER_MESSAGE));
    }

    @Test
    @DisplayName("Создание уникального пользователя без поля email")
    @Description("Проверка статуса 403 и поля 'message': Email, password and name are required fields")
    public void checkUserRegistrationWithoutEmail() {
        User user = new User(null, password, name);
        response = httpClient.postRequest(Endpoints.USER_CREATE_POST, user);
        response.then().statusCode(403)
                .and()
                .body("success", equalTo(false))
                .body("message", equalTo(Messages.REQUIRED_FIELD_MESSAGE));
    }

    @Test
    @DisplayName("Создание уникального пользователя без поля password")
    @Description("Проверка статуса 403 и поля 'message': Email, password and name are required fields")
    public void checkUserRegistrationWithoutPassword() {
        User user = new User(email, null, name);
        response = httpClient.postRequest(Endpoints.USER_CREATE_POST, user);
        response.then().statusCode(403)
                .and()
                .body("success", equalTo(false))
                .body("message", equalTo(Messages.REQUIRED_FIELD_MESSAGE));
    }

    @Test
    @DisplayName("Создание уникального пользователя без поля name")
    @Description("Проверка статуса 403 и поля 'message': Email, password and name are required fields")
    public void checkUserRegistrationWithoutName() {
        User user = new User(email, password, null);
        response = httpClient.postRequest(Endpoints.USER_CREATE_POST, user);
        response.then().statusCode(403)
                .and()
                .body("success", equalTo(false))
                .body("message", equalTo(Messages.REQUIRED_FIELD_MESSAGE));
    }
}