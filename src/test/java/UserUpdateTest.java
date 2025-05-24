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

public class UserUpdateTest extends BaseTest {
    Faker faker = new Faker();
    String email;
    String password;
    String name;
    String emailPatch;
    String passwordPatch;
    String namePatch;
    String accessToken;
    Response response;

    BaseHttpClient httpClient = new BaseHttpClient() {};

    @Before
    @Description("Создание нового пользователя и генерация данных для патча")
    public void setData() {
        email = faker.internet().emailAddress();
        password = faker.internet().password();
        name = faker.name().username();

        emailPatch = faker.internet().emailAddress();
        passwordPatch = faker.internet().password();
        namePatch = faker.name().username();

        User user = new User(email, password, name);
        response = httpClient.postRequest(Endpoints.USER_CREATE_POST, user);
        accessToken = response.then().extract().path("accessToken").toString();
    }

    @After
    @Description("Удаление созданного пользователя")
    public void cleanData() {
        if (response != null) {
            int statusCode = response.then().extract().statusCode();
            if (statusCode == 200) {
                httpClient.deleteRequest(Endpoints.ACTIONS_WITH_USER, accessToken);
            }
        }
    }

    @Test
    @DisplayName("Изменение данных пользователя с авторизацией")
    @Description("Проверка статуса 200 и поля 'success': true")
    public void checkUpdateEmailWhenUserIsAuthorized() {
        httpClient.postRequest(Endpoints.USER_LOGIN_POST, new User(email, password));
        Response patchResponse = httpClient.patchRequest(Endpoints.ACTIONS_WITH_USER, new User(emailPatch, password, name), accessToken);
        patchResponse.then().statusCode(200)
                .and()
                .body("user.email", equalTo(emailPatch));
    }

    @Test
    @DisplayName("Изменение данных пользователя с авторизацией")
    @Description("Проверка статуса 200 и поля 'success': true")
    public void checkUpdateNameWhenUserIsAuthorized() {
        httpClient.postRequest(Endpoints.USER_LOGIN_POST, new User(email, password));
        Response patchResponse = httpClient.patchRequest(Endpoints.ACTIONS_WITH_USER, new User(email, password, namePatch), accessToken);
        patchResponse.then().statusCode(200)
                .and()
                .body("user.name", equalTo(namePatch));
    }

    @Test
    @DisplayName("Изменение данных пользователя без авторизации")
    @Description("Проверка статуса 401 и поля 'message': You should be authorised")
    public void checkUpdateDataWhenUserIsUnauthorized() {
        Response patchResponse = httpClient.patchRequest(Endpoints.ACTIONS_WITH_USER, new User(emailPatch, passwordPatch, namePatch), "");
        patchResponse.then().statusCode(401)
                .and()
                .body("success", equalTo(false))
                .body("message", equalTo(Messages.AUTHORIZATION_MESSAGE));
    }
}