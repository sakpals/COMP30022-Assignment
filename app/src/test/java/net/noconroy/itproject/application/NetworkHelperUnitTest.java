package net.noconroy.itproject.application;

import org.junit.Test;

import java.util.UUID;

import static net.noconroy.itproject.application.NetworkHelper.Register;

public class NetworkHelperUnitTest {

    // TODO: Check tht the username is valid - check if white space works

    @Test
    public void RegisterTest() throws Exception {
        String username = UUID.randomUUID().toString();
        if (!Register(username, "test_password", "test_avatar_url", "test_description").equals("201"))
            throw new AssertionError();
    }

    // This test assume RegisterTest is functioning correctly
    @Test
    public void LoginTest() throws Exception {
        final String Error500 = "This error tends to usually mean you're try" +
                "to login with a username that doesn't exist in this in this" +
                "context ";

        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, "test_password", "test_avatar_url", "test_description");

        String access_token = NetworkHelper.Login(username, "test_password");
        if (access_token.equals("401")) throw new AssertionError();
        if (access_token.equals("500")) throw new AssertionError(Error500);
    }

    // Tests below here assume Register and Login works
    // TODO: Something wrong with my handling of access_token
    @Test
    public void LogoutTest() throws Exception {
        // Register and login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, "test_password", "test_avatar_url", "test_description");
        String access_token = NetworkHelper.Login(username, "test_password");

        if (!NetworkHelper.Logout(access_token).equals("200")) throw new AssertionError();
    }

    @Test
    public void UpdateProfileTest() throws Exception {
        // Register and Login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, "test_password", "test_avatar_url", "test_description");
        String access_token = NetworkHelper.Login(username, "test_password");

        if (!NetworkHelper.UpdateProfile(username, "test_password", "test_avatar_url", "test_description", access_token).equals("204"))
            throw new AssertionError();
    }

}