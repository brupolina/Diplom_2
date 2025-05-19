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

public class GetUsersOrderTest extends BaseHttpClient {
    Faker faker = new Faker();
    String email = faker.internet().emailAddress();
    String password = faker.internet().password();
    String name = faker.name().username();
    String accessToken;
    Response response;

    @Before
    @Description("Создание нового пользователя")
    public void setData() {
        User user = new User(email, password, name);
        response = postRequest(Endpoints.USER_CREATE_POST, user);
        accessToken = response.then().extract().path("accessToken").toString();
    }
    @After
    @Description("Удаление созданного пользователя")
    public  void cleanData() {
        int statusCode = response.then().extract().statusCode();
        if (statusCode == 200) {
            deleteRequest(Endpoints.ACTIONS_WITH_USER, accessToken);
        }
    }

    @Test
    @DisplayName("Получение заказа авторизированного пользователя")
    @Description("Проверка статуса 200 и поля 'success': true")
    public void checkOrderCreationWithAuthorization() {
        List<String> listAvailableIngredients = getRequest(Endpoints.INGREDIENTS_LIST_GET, "").then().extract().path("data._id");
        Order order = new Order(listAvailableIngredients.subList(0, 1));
        postRequest(Endpoints.ORDER_CREATE_POST, order);
        Response orderGetOrderResponse = getRequest(Endpoints.USERS_ORDER_GET, accessToken);
        orderGetOrderResponse.then().statusCode(200)
                .and()
                .body("success", equalTo(true));

    }

    @Test
    @DisplayName("Получение заказа неавторизированного пользователя")
    @Description("Проверка статуса 401 и поля 'message': You should be authorised")
    public void checkOrderCreationWithoutAuthorization() {
        Response orderGetOrderResponse = getRequest(Endpoints.USERS_ORDER_GET, "");
        orderGetOrderResponse.then().statusCode(401)
                .and()
                .body("success", equalTo(false))
                .body("message", equalTo(Messages.authorizationMessage));
    }

}