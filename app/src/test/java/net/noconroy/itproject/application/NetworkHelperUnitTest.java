package net.noconroy.itproject.application;

import org.junit.AssumptionViolatedException;
import org.junit.Test;

import java.util.UUID;

import static net.noconroy.itproject.application.NetworkHelper.Register;

public class NetworkHelperUnitTest {

    // TODO: Check tht the username is valid - check if white space works

    // Basic Register test
    @Test
    public void RegisterTest() throws Exception {
        String username = UUID.randomUUID().toString();
        if (!Register(username, "test_password", "test_avatar_url", "test_description").equals("201"))
            throw new AssertionError();
    }

    // Tests that you aren't able to register twice
    @Test
    public void RegisterTwiceTest() throws Exception {
        String username = UUID.randomUUID().toString();
        if (!Register(username, "test_password", "test_avatar_url", "test_description").equals("201"))
            throw new AssertionError();

        // Try to register again
        if (!Register(username, "test_password", "test_avatar_url", "test_description").equals("500"))
            throw new AssertionError("You're most likely trying to register a username that already exists");
    }

    // This test assume RegisterTest is functioning correctly
    // Tests basic Login
    @Test
    public void LoginTest() throws Exception {
        // Register
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, "test_password", "test_avatar_url", "test_description");

        String access_token = NetworkHelper.Login(username, "test_password");

        // Checks for whitespaces in access_token, which usually occers if the server is offline
        if (access_token.matches(".*\\s+.*"))
            throw new AssertionError("Most likely server offline");
        if (!access_token.matches(".*[a-z].*"))
            throw new AssumptionViolatedException("Most likely HTTP error");
    }

    // Tests below here assume Register and Login works
    // Tests basic Logout
    @Test
    public void LogoutTest() throws Exception {
        // Register and login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, "test_password", "test_avatar_url", "test_description");
        String access_token = NetworkHelper.Login(username, "test_password");

        if (!NetworkHelper.Logout(access_token, username).equals("200")) throw new AssertionError();
    }

    // Checks that you can't logout whilst not logged in
    @Test
    public void InvalidLogoutTest() throws Exception {
        // Register and login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, "test_password", "test_avatar_url", "test_description");
        String access_token = NetworkHelper.Login(username, "test_password");

        // Logout for the first time
        if (!NetworkHelper.Logout(access_token, username).equals("200")) throw new AssertionError();

        // Checks if other logouts work whilst already logged out
        // Will return error if logout works as planned
        if (NetworkHelper.Logout(access_token, username).equals("200")) throw new AssertionError();

        // Will return an error if the server doesn't correctly return internal error (500) when
        // logout is attempted again
        if (!NetworkHelper.Logout(access_token, username).equals("500")) throw new AssertionError();
    }

    @Test
    public void UpdateProfileTest() throws Exception {
        // Register and Login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, "test_password", "test_avatar_url", "test_description");
        String access_token = NetworkHelper.Login(username, "test_password");

        if (!NetworkHelper.UpdateProfile(username, "test_password", "test_avatar_url", "test_description", access_token).equals("200"))
            throw new AssertionError();
    }

    // Checks that the http GET request is successful
    @Test
    public void GetProfileBasic() throws Exception {
        // Register and Login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, "test_password", "test_avatar_url", "test_description");
        String access_token = NetworkHelper.Login(username, "test_password");

        if (!NetworkHelper.GetProfile(username, access_token).equals("test_description")) throw new AssertionError();

    }

    // Checks that updating and getting the profile gives a correct output
    // Currently only works for getting description, not avatar url
    @Test
    public void GetProfileAfterUpdate() throws Exception {
        // Register and Login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, "test_password", "test_avatar_url", "test_description");
        String access_token = NetworkHelper.Login(username, "test_password");

        NetworkHelper.UpdateProfile(username, "test_password", "test_avatar_url", "test_description2", access_token);

        if (!NetworkHelper.GetProfile(username, access_token).equals("test_description2")) throw new AssertionError();
    }

}


















