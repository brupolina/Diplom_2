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

public class UserUpdateTest extends BaseHttpClient {
    Faker faker = new Faker();
    String email = faker.internet().emailAddress();
    String password = faker.internet().password();
    String name = faker.name().username();
    String emailPatch = faker.internet().emailAddress();
    String passwordPatch = faker.internet().password();
    String namePatch= faker.name().username();
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
    @DisplayName("Изменение данных пользователя с авторизацией")
    @Description("Проверка статуса 200 и поля 'success': true")
    public void checkUpdateEmailWhenUserIsAuthorized() {
        postRequest(Endpoints.USER_LOGIN_POST, new User(email, password));
        Response patchResponse = patchRequest(Endpoints.ACTIONS_WITH_USER,new User(emailPatch, password, name) ,accessToken);
        patchResponse.then().statusCode(200)
                .and()
                .body("user.email", equalTo(emailPatch));

    }

    @Test
    @DisplayName("Изменение данных пользователя с авторизацией")
    @Description("Проверка статуса 200 и поля 'success': true")
    public void checkUpdateNameWhenUserIsAuthorized() {
        postRequest(Endpoints.USER_LOGIN_POST, new User(email, password));
        Response patchResponse = patchRequest(Endpoints.ACTIONS_WITH_USER,new User(email, password, namePatch) ,accessToken);
        patchResponse.then().statusCode(200)
                .and()
                .body("user.name", equalTo(namePatch));

    }

    @Test
    @DisplayName("Изменение данных пользователя без авторизации")
    @Description("Проверка статуса 401 и поля 'message': You should be authorised")
    public void checkUpdateDataWhenUserIsUnauthorized() {
        Response patchResponse = patchRequest(Endpoints.ACTIONS_WITH_USER,new User(emailPatch, passwordPatch, namePatch) ,"");
        patchResponse.then().statusCode(401)
                .and()
                .body("success", equalTo(false))
                .body("message", equalTo(Messages.authorizationMessage));
    }

}