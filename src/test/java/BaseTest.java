package praktikum;

import io.restassured.RestAssured;
import org.junit.BeforeClass;
import praktikum.constants.Endpoints;

public abstract class BaseTest {

    @BeforeClass
    public static void globalSetUp() {
        RestAssured.baseURI = Endpoints.BASE_HOST;
    }
}