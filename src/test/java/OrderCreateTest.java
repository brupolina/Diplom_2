import io.qameta.allure.Description;
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

public class OrderCreateTest extends BaseHttpClient {
    Faker faker = new Faker();
    String email = faker.internet().emailAddress();
    String password = faker.internet().password();
    String name = faker.name().username();
    List<String> listAvailableIngredients;
    List<String> listUnavailableIngredients = List.of("60d3b41abdacab0026a00c8","609646e4dc916e00276b000");
    Response response;

    @Before
    @Description("Создание нового пользователя")
    public void setData() {
        User user = new User(email, password, name);
        response = postRequest(Endpoints.USER_CREATE_POST, user);
        listAvailableIngredients = getRequest(Endpoints.INGREDIENTS_LIST_GET, "").then().extract().path("data._id");
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
    @DisplayName("Создание заказа авторизированным пользователем")
    @Description("Проверка статуса 200 и поля 'success': true")
    public void checkOrderCreationWithAuthorization() {
        Order order = new Order(listAvailableIngredients.subList(0, 1));
        postRequest(Endpoints.USER_LOGIN_POST, new User(email, password));
        Response orderResponse = postRequest(Endpoints.ORDER_CREATE_POST, order);
        orderResponse.then().statusCode(200)
                .and()
                .body("success", equalTo(true));

    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    @Description("Проверка статуса 401 и поля 'message': You should be authorised")
    public void checkOrderCreationWithoutAuthorization() {
        Order order = new Order(listAvailableIngredients.subList(0, 1));
        Response orderResponse = postRequest(Endpoints.ORDER_CREATE_POST, order);
        orderResponse.then().statusCode(401)
                .and()
                .body("success", equalTo(false))
                .body("message", equalTo(Messages.authorizationMessage));
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов авторизированным пользователем")
    @Description("Проверка статуса 400 и поля 'message': Ingredient ids must be provided")
    public void checkOrderCreationWithoutIngredients() {
        Order order = new Order(null);
        postRequest(Endpoints.USER_LOGIN_POST, new User(email, password));
        Response orderResponse = postRequest(Endpoints.ORDER_CREATE_POST, order);
        orderResponse.then().statusCode(400)
                .and()
                .body("success", equalTo(false))
                .body("message", equalTo(Messages.ingredientsMessage));
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов авторизированным пользователем")
    @Description("Проверка статуса 500 и поля 'success': false")
    public void checkOrderCreationWithWrongIngredients() {
        Order order = new Order(listUnavailableIngredients);
        postRequest(Endpoints.USER_LOGIN_POST, new User(email, password));
        Response orderResponse = postRequest(Endpoints.ORDER_CREATE_POST, order);
        orderResponse.then().statusCode(500);
    }
}