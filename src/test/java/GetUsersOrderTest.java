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

public class GetUsersOrderTest extends BaseTest {
    Faker faker = new Faker();
    String email;
    String password;
    String name;
    String accessToken;
    Response response;

    // Создаем экземпляр клиента для отправки запросов
    BaseHttpClient httpClient = new BaseHttpClient() {};

    @Before
    @Description("Создание нового пользователя")
    public void setData() {
        email = faker.internet().emailAddress();
        password = faker.internet().password();
        name = faker.name().username();

        User user = new User(email, password, name);
        response = httpClient.postRequest(Endpoints.USER_CREATE_POST, user);
        accessToken = response.then().extract().path("accessToken").toString();
    }

    @After
    @Description("Удаление созданного пользователя")
    public void cleanData() {
        int statusCode = response.then().extract().statusCode();
        if (statusCode == 200) {
            httpClient.deleteRequest(Endpoints.ACTIONS_WITH_USER, accessToken);
        }
    }

    @Test
    @DisplayName("Получение заказа авторизированного пользователя")
    @Description("Проверка статуса 200 и поля 'success': true")
    public void checkOrderCreationWithAuthorization() {
        List<String> listAvailableIngredients = httpClient.getRequest(Endpoints.INGREDIENTS_LIST_GET, "")
                .then().extract().path("data._id");
        Order order = new Order(listAvailableIngredients.subList(0, 1));
        httpClient.postRequest(Endpoints.ORDER_CREATE_POST, order);
        Response orderGetOrderResponse = httpClient.getRequest(Endpoints.USERS_ORDER_GET, accessToken);
        orderGetOrderResponse.then().statusCode(200)
                .and()
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Получение заказа неавторизированного пользователя")
    @Description("Проверка статуса 401 и поля 'message': You should be authorised")
    public void checkOrderCreationWithoutAuthorization() {
        Response orderGetOrderResponse = httpClient.getRequest(Endpoints.USERS_ORDER_GET, "");
        orderGetOrderResponse.then().statusCode(401)
                .and()
                .body("success", equalTo(false))
                .body("message", equalTo(Messages.AUTHORIZATION_MESSAGE));
    }
}