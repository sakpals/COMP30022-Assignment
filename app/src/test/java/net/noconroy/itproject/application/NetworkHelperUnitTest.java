package net.noconroy.itproject.application;

import org.junit.Test;

import java.util.ArrayList;
import java.util.UUID;

import static net.noconroy.itproject.application.NetworkHelper.Register;

public class NetworkHelperUnitTest {

    // Arbitary variables used to test that methods are working correctly
    private final String test_password = "test_password";
    private final String test_avatar_url = "test_avatar_url";
    private final String test_description = "test_description";
    private final String test_lat = "10";
    private final String test_lon = "15";
    private final String wrong_password = "wrong_password";


    // Arbitary descriptions for when we have to compare different users
    // attributes against eachother
    private final String test_description1 = "test_description1";
    private final String test_description2 = "test_description2";
    private final String test_description3 = "test_description3";
    private final String test_lat1 = "10";
    private final String test_lon1 = "15";
    private final String test_lat2 = "20";
    private final String test_lon2 = "30";

    // Error messages
    private final String username_already_exists_error = "You're most" +
            "likely trying to register a username that already exists";
    private final String server_offline_error = "The server ist most" +
            "likey offline, please try again later";



    // Basic Register test
    @Test
    public void RegisterTest() throws Exception {
        String username = UUID.randomUUID().toString();
        if (!Register(username, test_password, test_avatar_url, test_description).equals("201"))
            throw new AssertionError();
    }

    // Tests that you aren't able to register twice
    @Test
    public void RegisterTwiceTest() throws Exception {
        String username = UUID.randomUUID().toString();
        if (!Register(username, test_password, test_avatar_url, test_description).equals("201"))
            throw new AssertionError();

        // Try to register again
        if (!Register(username, test_password, test_avatar_url, test_description).equals("500"))
            throw new AssertionError(username_already_exists_error);
    }

    // This test assume RegisterTest is functioning correctly
    // Tests basic Login
    @Test
    public void LoginTest() throws Exception {
        // Register
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, test_password, test_avatar_url, test_description);

        String access_token = NetworkHelper.Login(username, test_password);

        // Checks for whitespaces in access_token, which usually occers if the server is offline
        if (access_token.matches(".*\\s+.*"))
            throw new AssertionError(server_offline_error);
        // Indicates a HTTP error
        if (!access_token.matches(".*[a-z].*"))
            throw new AssertionError(access_token);
    }

    // Tests below here assume Register and Login works
    // Tests basic Logout
    @Test
    public void LogoutTest() throws Exception {
        // Register and login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, test_password, test_avatar_url, test_description);
        String access_token = NetworkHelper.Login(username, test_password);

        if (!NetworkHelper.Logout(access_token).equals("200")) throw new AssertionError();
    }

    // Checks that you can't logout whilst not logged in
    @Test
    public void InvalidLogoutTest() throws Exception {
        // Register and login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, test_password, test_avatar_url, test_description);
        String access_token = NetworkHelper.Login(username, test_password);

        // Logout for the first time
        if (!NetworkHelper.Logout(access_token).equals("200")) throw new AssertionError();

        // Checks if other logouts work whilst already logged out
        // Will return error if logout works as planned
        if (NetworkHelper.Logout(access_token).equals("200")) throw new AssertionError();

        // Will return an error if the server doesn't correctly return internal error (500) when
        // logout is attempted again
        if (!NetworkHelper.Logout(access_token).equals("500")) throw new AssertionError();
    }

    @Test
    public void UpdateProfileTest() throws Exception {
        // Register and Login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, test_password, test_avatar_url, test_description);
        String access_token = NetworkHelper.Login(username, test_password);

        if (!NetworkHelper.UpdateProfile(username, test_password, test_avatar_url, test_description, access_token).equals("200"))
            throw new AssertionError();
    }

    // Checks that the http GET request is successful
    @Test
    public void GetProfileBasicTest() throws Exception {
        // Register and Login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, test_password, test_avatar_url, test_description);
        String access_token = NetworkHelper.Login(username, test_password);

        if (!NetworkHelper.GetProfile(username, access_token).equals(test_description)) throw new AssertionError();

    }

    // Checks that updating and getting the profile gives a correct output
    // Currently only works for getting description, not avatar url
    @Test
    public void GetProfileAfterUpdateTest() throws Exception {
        // Register and Login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, test_password, test_avatar_url, test_description1);
        String access_token = NetworkHelper.Login(username, test_password);

        NetworkHelper.UpdateProfile(username, test_password, test_avatar_url, test_description2, access_token);

        if (!NetworkHelper.GetProfile(username, access_token).equals(test_description2))
            throw new AssertionError();
    }

    // Tests that a user can get another users profile
    @Test
    public void GetAnotherUsersProfileTest() throws Exception {
        // Register and Login first user
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, test_password, test_avatar_url, test_description1);
        String access_token1 = NetworkHelper.Login(username1, test_password);

        // Register second user
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, test_password, test_avatar_url, test_description2);

        // Tests that first user can access profile of second user
        if (!NetworkHelper.GetProfile(username2, access_token1).equals(test_description2))
            throw new AssertionError();
    }

    // Tests that a user cannot update another users profile
    @Test
    public void UpdateAnotherUsersProfileTest() throws Exception {
        // Register and Login user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, test_password, test_avatar_url, test_description1);
        String access_token1 = NetworkHelper.Login(username1, test_password);

        // Register and Login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, test_password, test_avatar_url, test_description2);
        String access_token2 = NetworkHelper.Login(username1, test_password);

        // These will return an error if a user succesfully alters another users profile
        if (NetworkHelper.UpdateProfile(username1,
                wrong_password, test_avatar_url, test_description1, access_token2).equals("200"))
            throw new AssertionError();

        if (NetworkHelper.GetProfile(username1, access_token1).equals(test_description1))
            throw new AssertionError();

    }

    // Tests that the server accepts the request
    @Test
    public void UpdateLocationTest() throws Exception {
        // Register and Login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, test_password, test_avatar_url, test_description);
        String access_token = NetworkHelper.Login(username, test_password);

        if (!NetworkHelper.UpdateLocation(username, test_lat, test_lon, access_token).equals("200"))
            throw new AssertionError();
    }


    // TODO: iterate through all illegal characters which will be contained in a constant array
    // Test to make sure server returns error if invalid lat/lon inputted
    @Test
    public void InvalidUpdateLocationTest() throws Exception {
        // Register and Login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, test_password, test_avatar_url, test_description);
        String access_token = NetworkHelper.Login(username, test_password);

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
        NetworkHelper.Register(username, test_password, test_avatar_url, test_description);
        String access_token = NetworkHelper.Login(username, test_password);

        NetworkHelper.UpdateLocation(username, test_lat, test_lon, access_token).equals("400");

        if (!NetworkHelper.RetrieveLocation(username, access_token)[0].equals("0.0"))
            throw new AssertionError();
        if (!NetworkHelper.RetrieveLocation(username, access_token)[1].equals("180.0"))
            throw new AssertionError();
    }

    // Tests that you cannot update the location of another use
    @Test
    public void UpdateOthersLocationTest() throws Exception {
        // Register and Login user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, test_password, test_avatar_url, test_description);
        String access_token1 = NetworkHelper.Login(username1, test_password);

        // Register and Login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, test_password, test_avatar_url, test_description);
        String access_token2 = NetworkHelper.Login(username1, test_password);

        // User 2 tried to update location of user 1. Will throw error if the
        // server allows this to occur
        NetworkHelper.UpdateLocation(username1, test_lat1, test_lon1, access_token1);
        if (NetworkHelper.UpdateLocation(username1, test_lat2, test_lon2, access_token2).equals("200"))
            throw new AssertionError();

    }

    // Tests that server accepts friend request
    @Test
    public void AddFriendTest() throws Exception {
        // Register and Login user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, test_password, test_avatar_url, test_description);
        //String access_token1 = NetworkHelper.Login(username1, test_password);

        // Register and Login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, test_password, test_avatar_url, test_description);
        String access_token2 = NetworkHelper.Login(username1, test_password);

        System.out.println(NetworkHelper.AddFriend(username1, access_token2));
    }

    @Test
    public void FriendRequestTest() throws Exception {
        // Register user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, test_password, test_avatar_url, test_description);
        String access_token1 = NetworkHelper.Login(username1, test_password);

        // Register and Login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, test_password, test_avatar_url, test_description);
        String access_token2 = NetworkHelper.Login(username2, test_password);

        System.out.println(NetworkHelper.AddFriend(username1, access_token2));
    }

    // Tests that you can't send multiple friend requests to a single user from
    // the same user
    @Test
    public void DoubleFriendRequestTest() throws Exception {
        // Register user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, test_password, test_avatar_url, test_description);
        String access_token1 = NetworkHelper.Login(username1, test_password);

        // Register and Login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, test_password, test_avatar_url, test_description);
        String access_token2 = NetworkHelper.Login(username1, test_password);

        // User 2 sends friend request to user 1
        NetworkHelper.AddFriend(username1, access_token2);

        // User 1 accepts friend request
        if (!NetworkHelper.AddFriend(username1, access_token2).equals("500"))
            throw new AssertionError();

    }

    // Tests that the server accepts the accpepting of the friend request
    @Test
    public void AcceptFriendTest() throws Exception {
        // Register user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, test_password, test_avatar_url, test_description);
        String access_token1 = NetworkHelper.Login(username1, test_password);

        // Register and Login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, test_password, test_avatar_url, test_description);
        String access_token2 = NetworkHelper.Login(username2, test_password);

        // User 2 sends friend request to user 1
        NetworkHelper.AddFriend(username1, access_token2);

        if (!NetworkHelper.AddFriend(username2, access_token1).equals("201"))
            throw new AssertionError();
    }

    // Tests that other users can't accept friend requests
    @Test
    public void UnauthorisedFriendRequestAcceptTest() throws Exception {
        // Register user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, test_password, test_avatar_url, test_description);
        String access_token1 = NetworkHelper.Login(username1, test_password);

        // Register and Login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, test_password, test_avatar_url, test_description);
        String access_token2 = NetworkHelper.Login(username2, test_password);

        // User 2 sends friend request to user 1
        NetworkHelper.AddFriend(username1, access_token2);

        // User 2 tried to accept the friend request
        if (!NetworkHelper.AcceptFriend(username2, access_token2).equals("500"))
            throw new AssertionError();
    }

    // Tests getting a friends list
    @Test
    public void FriendsListTest() throws Exception {
        // Register and Login user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, test_password, test_avatar_url, test_description1);
        String access_token1 = NetworkHelper.Login(username1, test_password);

        // Register and Login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, test_password, test_avatar_url, test_description2);
        String access_token2 = NetworkHelper.Login(username2, test_password);

        // Register and Login user 3
        String username3 = UUID.randomUUID().toString();
        NetworkHelper.Register(username3, test_password, test_avatar_url, test_description3);
        String access_token3 = NetworkHelper.Login(username3, test_password);

        // User 1 sends friend request to user 2
        NetworkHelper.AddFriend(username2, access_token1);

        // User 1 sends friend request to user 3
        NetworkHelper.AddFriend(username3, access_token1);

        // User 2 accepts friend request of user 1
        NetworkHelper.AcceptFriend(username1, access_token2);

        // User 3 accepts friend request of user 1
        NetworkHelper.AcceptFriend(username1, access_token3);

        // The output from the server
        ArrayList<ArrayList<String>> out = NetworkHelper.GetFriends(access_token1);

        if (out.size() != 2) throw new AssertionError();
        if (!out.get(0).get(0).equals(username2)) throw new AssertionError();
        if (!out.get(0).get(1).equals(test_description2)) throw new AssertionError();
        if (!out.get(1).get(0).equals(username3)) throw new AssertionError();
        if (!out.get(1).get(1).equals(test_description3)) throw new AssertionError();
    }

    @Test
    public void RemoveFriendTest() throws Exception {
        // Register user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, test_password, test_avatar_url, test_description);
        String access_token1 = NetworkHelper.Login(username1, test_password);

        // Register and Login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, test_password, test_avatar_url, test_description);
        String access_token2 = NetworkHelper.Login(username2, test_password);

        // User 2 sends friend request to user 1
        NetworkHelper.AddFriend(username1, access_token2);

        // User 1 accepts user 2's friend request
        NetworkHelper.AddFriend(username2, access_token1);

        // User 1 removes user 2 as a friends
        NetworkHelper.RemoveFriend(username2, access_token1);

        // Check that both user now have no friends
        if (NetworkHelper.GetFriends(access_token1).size() != 0) throw new AssertionError();
        if (NetworkHelper.GetFriends(access_token1).size() != 0) throw new AssertionError();
    }

    // Tests that other users can't accept friend requests
    @Test
    public void GetIncomingFriendRequestsTest() throws Exception {
        // Register user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, test_password, test_avatar_url, test_description);
        String access_token1 = NetworkHelper.Login(username1, test_password);

        // Register and Login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, test_password, test_avatar_url, test_description);
        String access_token2 = NetworkHelper.Login(username2, test_password);

        if (NetworkHelper.GetIncomingFriendRequests(access_token2) != null) throw new AssertionError();
        if (NetworkHelper.GetIncomingFriendRequests(access_token1) != null) throw new AssertionError();

        // User 2 sends friend request to user 1
        NetworkHelper.AddFriend(username1, access_token2);

        // Check that the incoming friend request is there
        ArrayList<ArrayList<String>> out = NetworkHelper.GetIncomingFriendRequests(access_token1);
        if (!out.get(0).get(0).toString().equals(username2)) throw new AssertionError();
    }

    @Test
    public void GetOutgoingFriendRequestsTest() throws Exception {
        // Register user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, test_password, test_avatar_url, test_description);
        String access_token1 = NetworkHelper.Login(username1, test_password);

        // Register and Login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, test_password, test_avatar_url, test_description);
        String access_token2 = NetworkHelper.Login(username2, test_password);

        if (NetworkHelper.GetOutgoingFriendRequests(access_token2) != null) throw new AssertionError();
        if (NetworkHelper.GetOutgoingFriendRequests(access_token1) != null) throw new AssertionError();

        // User 2 sends friend request to user 1
        NetworkHelper.AddFriend(username1, access_token2);

        // Check that the outgoing friend request is there
        ArrayList<ArrayList<String>> out = NetworkHelper.GetOutgoingFriendRequests(access_token2);
        if (!out.get(0).get(0).toString().equals(username1)) throw new AssertionError();
    }

}

















