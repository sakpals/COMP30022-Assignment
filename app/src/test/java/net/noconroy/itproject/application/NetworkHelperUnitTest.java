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
    public void GetProfileBasicTest() throws Exception {
        // Register and Login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, "test_password", "test_avatar_url", "test_description");
        String access_token = NetworkHelper.Login(username, "test_password");

        if (!NetworkHelper.GetProfile(username, access_token).equals("test_description")) throw new AssertionError();

    }

    // Checks that updating and getting the profile gives a correct output
    // Currently only works for getting description, not avatar url
    @Test
    public void GetProfileAfterUpdateTest() throws Exception {
        // Register and Login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, "test_password", "test_avatar_url", "test_description");
        String access_token = NetworkHelper.Login(username, "test_password");

        NetworkHelper.UpdateProfile(username, "test_password", "test_avatar_url", "test_description2", access_token);

        if (!NetworkHelper.GetProfile(username, access_token).equals("test_description2"))
            throw new AssertionError();
    }

    // Tests that a user can get another users profile
    @Test
    public void GetAnotherUsersProfileTest() throws Exception {
        // Register and Login first user
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, "test_password", "test_avatar_url", "usr1_desc");
        String access_token1 = NetworkHelper.Login(username1, "test_password");

        // Register second user
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, "test_password", "test_avatar_url", "usr2_desc");

        // Tests that first user can access profile of second user
        if (!NetworkHelper.GetProfile(username2, access_token1).equals("usr2_desc"))
            throw new AssertionError();
    }

    // Tests that a user cannot update another users profile
    @Test
    public void UpdateAnotherUsersProfileTest() throws Exception {
        // Register and Login first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, "test_password", "test_avatar_url", "usr1_desc");
        String access_token1 = NetworkHelper.Login(username1, "test_password");

        // Register and Login first
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, "test_password", "test_avatar_url", "usr2_desc");
        String access_token2 = NetworkHelper.Login(username1, "test_password");

        // Thesewill return an error if a user succesfully alters another users profile
        if (NetworkHelper.UpdateProfile(username1,
                "wrong_password", "test_avatar_url", "changed_desc", access_token2).equals("200"))
            throw new AssertionError();

        if (NetworkHelper.GetProfile(username1, access_token1).equals("changed_desc"))
            throw new AssertionError();

    }

    // Tests that the server accepts the request
    @Test
    public void UpdateLocationTest() throws Exception {
        // Register and Login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, "test_password", "test_avatar_url", "test_description");
        String access_token = NetworkHelper.Login(username, "test_password");

        if (!NetworkHelper.UpdateLocation(username, "10", "15", access_token).equals("200"))
            throw new AssertionError();
    }

    // Test to make sure server returns error if invalid lat/lon inputted
    @Test
    public void InvalidUpdateLocationTest() throws Exception {
        // Register and Login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, "test_password", "test_avatar_url", "test_description");
        String access_token = NetworkHelper.Login(username, "test_password");

        if (!NetworkHelper.UpdateLocation(username, "??", "15", access_token).equals("400"))
            throw new AssertionError();

        if (!NetworkHelper.UpdateLocation(username, "sdad", "15", access_token).equals("400"))
            throw new AssertionError();

        if (!NetworkHelper.UpdateLocation(username, "10", "??", access_token).equals("400"))
            throw new AssertionError();

        if (!NetworkHelper.UpdateLocation(username, "10", "sdad", access_token).equals("400"))
            throw new AssertionError();

        if (!NetworkHelper.UpdateLocation(username, " ", "sdad", access_token).equals("400"))
            throw new AssertionError();

        if (!NetworkHelper.UpdateLocation(username, "10", " ", access_token).equals("400"))
            throw new AssertionError();
    }

    // Obviously assumes update location works, as that needs to be called in order
    // to initialize the the location. Only functions for default direction, distance
    // being returned so far
    @Test
    public void GetLocationTest() throws Exception {
        // Register and Login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, "test_password", "test_avatar_url", "test_description");
        String access_token = NetworkHelper.Login(username, "test_password");

        NetworkHelper.UpdateLocation(username, "10", "15", access_token).equals("400");

        if (!NetworkHelper.RetrieveLocation(username, access_token)[0].equals("0.0"))
            throw new AssertionError();
        if (!NetworkHelper.RetrieveLocation(username, access_token)[1].equals("180.0"))
            throw new AssertionError();
    }
}


















