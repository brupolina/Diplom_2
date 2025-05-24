import io.qameta.allure.Description;
import praktikum.BaseTest;
import praktikum.BaseHttpClient;
import praktikum.constants.Endpoints;
import praktikum.constants.Messages;
import praktikum.pojo.Order;
import praktikum.pojo.User;
import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;

public class OrderCreateTest extends BaseTest {
    Faker faker = new Faker();
    String email;
    String password;
    String name;
    List<String> listAvailableIngredients;
    List<String> listUnavailableIngredients = List.of("60d3b41abdacab0026a00c8", "609646e4dc916e00276b000");
    Response response;

    BaseHttpClient httpClient = new BaseHttpClient() {};

    @Before
    @Description("Создание нового пользователя")
    public void setData() {
        email = faker.internet().emailAddress();
        password = faker.internet().password();
        name = faker.name().username();

        User user = new User(email, password, name);
        response = httpClient.postRequest(Endpoints.USER_CREATE_POST, user);
        listAvailableIngredients = httpClient.getRequest(Endpoints.INGREDIENTS_LIST_GET, "")
                .then().extract().path("data._id");
    }

    @After
    @Description("Удаление созданного пользователя")
    public void cleanData() {
        int statusCode = response.then().extract().statusCode();
        if (statusCode == 200) {
            String accessToken = response.then().extract().path("accessToken").toString();
            httpClient.deleteRequest(Endpoints.ACTIONS_WITH_USER, accessToken);
        }
    }

    @Test
    @DisplayName("Создание заказа авторизированным пользователем")
    @Description("Проверка статуса 200 и поля 'success': true")
    public void checkOrderCreationWithAuthorization() {
        Order order = new Order(listAvailableIngredients.subList(0, 1));
        httpClient.postRequest(Endpoints.USER_LOGIN_POST, new User(email, password));
        Response orderResponse = httpClient.postRequest(Endpoints.ORDER_CREATE_POST, order);
        orderResponse.then().statusCode(200)
                .and()
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    @Description("Проверка статуса 401 и поля 'message': You should be authorised")
    public void checkOrderCreationWithoutAuthorization() {
        Order order = new Order(listAvailableIngredients.subList(0, 1));
        Response orderResponse = httpClient.postRequest(Endpoints.ORDER_CREATE_POST, order);
        orderResponse.then().statusCode(401)
                .and()
                .body("success", equalTo(false))
                .body("message", equalTo(Messages.AUTHORIZATION_MESSAGE));
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов авторизированным пользователем")
    @Description("Проверка статуса 400 и поля 'message': Ingredient ids must be provided")
    public void checkOrderCreationWithoutIngredients() {
        Order order = new Order(null);
        httpClient.postRequest(Endpoints.USER_LOGIN_POST, new User(email, password));
        Response orderResponse = httpClient.postRequest(Endpoints.ORDER_CREATE_POST, order);
        orderResponse.then().statusCode(400)
                .and()
                .body("success", equalTo(false))
                .body("message", equalTo(Messages.INGREDIENTS_MESSAGE));
    }

    @Test
    @DisplayName("Создание заказа с неверными ингредиентами авторизированным пользователем")
    @Description("Проверка статуса 500 и поля 'success': false")
    public void checkOrderCreationWithWrongIngredients() {
        Order order = new Order(listUnavailableIngredients);
        httpClient.postRequest(Endpoints.USER_LOGIN_POST, new User(email, password));
        Response orderResponse = httpClient.postRequest(Endpoints.ORDER_CREATE_POST, order);
        orderResponse.then().statusCode(500);
    }
}