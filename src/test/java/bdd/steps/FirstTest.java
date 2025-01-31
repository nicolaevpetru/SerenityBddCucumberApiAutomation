package bdd.steps;

import io.cucumber.java.en.Given;

public class FirstTest {

    @Given("the user is testing the first test")
    public void the_user_is_testing_the_first_test() {
        System.out.println("First test");
    }
}
