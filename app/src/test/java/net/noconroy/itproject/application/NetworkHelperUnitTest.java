package net.noconroy.itproject.application;

import org.junit.Test;

import java.util.UUID;

public class NetworkHelperUnitTest {

    @Test
    public void RegisterTest() throws Exception {
        String username = UUID.randomUUID().toString();
        if (!NetworkHelper.Register(username, "test_password", "test_avatar_url", "test_description").equals("201"))
            throw new AssertionError();
    }

    @Test
    public void LoginTest() throws Exception {
        String access_token = NetworkHelper.Login("test_username", "test_password");
        if (access_token.equals("401")) throw new AssertionError();
    }

    // Tests below here assume Login works
    // TODO: Something wrong with my handling of access_token
    @Test
    public void LogoutTest() throws Exception {
        // Log in first
        String access_token = NetworkHelper.Login("test_username", "test_password");
        if (!NetworkHelper.Logout(access_token).equals("200")) throw new AssertionError();
    }

    @Test
    public void UpdateProfileTest() throws Exception {
        // Log in first
        String access_token = NetworkHelper.Login("test_username", "test_password");
        if (!NetworkHelper.UpdateProfile("test_username", "test_password", "test_avatar_url", "test_description", access_token).equals("204"))
            throw new AssertionError();
    }
}