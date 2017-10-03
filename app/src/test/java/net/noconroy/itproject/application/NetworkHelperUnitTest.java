package net.noconroy.itproject.application;


/**
 * A set of unit tests for NetworkHelper.
 * Note that many of these unit tests
 * depend on eachother, for example the logout test assumes that login is
 * working, so take note of what tests are being passed and what methods
 * are being called to make sure that another incorrect method isn't
 * accecting the tested method.
 */

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.Assert.assertEquals;



public class NetworkHelperUnitTest {

    // Arbitary variables used to test that methods are working correctly
    private final String test_password = "test_password";
    private final String test_avatar_url = "test_avatar_url";
    private final String test_description = "test_description";
    private final String test_lat = "10";
    private final String test_lon = "15";
    private final String default_lat = "0.0";
    private final String default_lon = "180.0";
    private final String wrong_password = "wrong_password";


    // Arbitary values for when we have to compare different users
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



    // Basic Register test to test if server accepts registering
    @Test
    public void RegisterTest() throws Exception {

        // Register user
        String username = UUID.randomUUID().toString();
        String response = NetworkHelper.Register(username, test_password,
                test_avatar_url, test_description);

        // Test if the server accepts this registering
        if (!isAccepted(response))
            throw new AssertionError(response);
    }


    // Tests that you aren't able to register twice
    @Test
    public void RegisterTwiceTest() throws Exception {

        // Registers a user
        String username = UUID.randomUUID().toString();
        String response = NetworkHelper.Register(username, test_password,
                test_avatar_url, test_description);

        // Tests that the server accepts this registering
        if (!isAccepted(response))
            throw new AssertionError(response);

        // Tries to register the same username again
        response = NetworkHelper.Register(username, test_password,
                test_avatar_url, test_description);

        // Tests that the server does not accept duplicate username registering
        if (isAccepted(response))
            throw new AssertionError(response);
    }


    // Tests that the server accepts basic login
    @Test
    public void LoginTest() throws Exception {

        // Register and login a user
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, test_password, test_avatar_url, test_description);
        String access_token = NetworkHelper.Login(username, test_password);

        // Checks for whitespaces in access_token, which usually occers if the
        // server is offline
        if (access_token.matches(".*\\s+.*"))
            throw new AssertionError(server_offline_error);

        // Indicates the server has responded with a HTTP error, and not a
        // username
        if (!access_token.matches(".*[a-z].*"))
            throw new AssertionError(access_token);
    }


    // Tests below here assume Register and Login works
    // Tests basic Logout
    @Test
    public void LogoutTest() throws Exception {

        // Register and login a user
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, test_password, test_avatar_url, test_description);
        String access_token = NetworkHelper.Login(username, test_password);

        // Logout the user
        String response = NetworkHelper.Logout(access_token);

        // Tests that the server accepted this logout
        if (!isAccepted(response))
            throw new AssertionError(response);
    }


    // Checks that you can't logout whilst not logged in
    @Test
    public void InvalidLogoutTest() throws Exception {

        // Register and login a user
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, test_password, test_avatar_url, test_description);
        String access_token = NetworkHelper.Login(username, test_password);

        // Logout for the first time
        String response = NetworkHelper.Logout(access_token);
        if (!isAccepted(response)) throw new AssertionError(response);

        // Logout for a second time, and get the servers response to this
        response = NetworkHelper.Logout(access_token);

        // Checks if logout works whilst already logged out
        // Will return error if logout works as planned
        if (isAccepted(response)) throw new AssertionError(response);
    }


    // Checks that the server accepts updating your own profile
    @Test
    public void UpdateProfileTest() throws Exception {

        // Register and login a user
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, test_password, test_avatar_url,
                test_description);
        String access_token = NetworkHelper.Login(username, test_password);

        // Get the server's response to updating a profile
        String response = NetworkHelper.UpdateProfile(username, test_password,
                test_avatar_url, test_description, access_token);

        // Checks that the servers response indicates that it accepted this
        // request
        if (!isAccepted(response))
            throw new AssertionError(response);
    }


    // Checks getting a user's own profile works correctly
    @Test
    public void GetProfileBasicTest() throws Exception {

        // Register and login user
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, test_password, test_avatar_url,
                test_description);
        String access_token = NetworkHelper.Login(username, test_password);

        // Get the profile of the user
        String response = NetworkHelper.GetProfile(username, access_token);

        // Checks that this profile is the correct profile
        assertEquals(response, test_description);
    }


    // Checks that updating and getting the profile gives a correct output
    // Currently only works for getting description, not avatar url
    @Test
    public void GetProfileAfterUpdateTest() throws Exception {

        // Register and login user first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, test_password, test_avatar_url,
                test_description);
        String access_token = NetworkHelper.Login(username, test_password);

        // The user updates its own profile
        NetworkHelper.UpdateProfile(username, test_password,
                test_avatar_url, test_description2, access_token);

        // The user gets its own profile description (that has recently been
        // updates
        String response = NetworkHelper.GetProfile(username, access_token);

        // Asserts that this update of teh profile was successful
        assertEquals(response, test_description2);
    }


    // Tests that a user can get another users profile
    @Test
    public void GetAnotherUsersProfileTest() throws Exception {

        // Register and login user 1
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, test_password, test_avatar_url,
                test_description1);
        String access_token1 = NetworkHelper.Login(username1, test_password);

        // Register user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, test_password, test_avatar_url,
                test_description2);

        // User 1 tried to get the profile description of user 2
        String response = NetworkHelper.GetProfile(username2, access_token1);

        // Assert that the correct description attained was the correct
        // description
        assertEquals(response, test_description2);
    }


    // Tests that a user cannot update another users profile
    @Test
    public void UpdateAnotherUsersProfileTest() throws Exception {

        // Register and login user 1
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, test_password, test_avatar_url,
                test_description1);
        String access_token1 = NetworkHelper.Login(username1, test_password);

        // Register and login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, test_password, test_avatar_url,
                test_description2);
        String access_token2 = NetworkHelper.Login(username2, test_password);

        // User 2 tries to update user 1's profile
        String response = NetworkHelper.UpdateProfile(username1,
                wrong_password, test_avatar_url, test_description2, access_token2);

        // Throw error if the server accepts this
        if (isAccepted(response)) throw new AssertionError(response);
    }


    // Tests that the server accepts request to update users own location
    @Test
    public void UpdateLocationTest() throws Exception {

        // Register and login user
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, test_password, test_avatar_url,
                test_description);
        String access_token = NetworkHelper.Login(username, test_password);

        // The user updates its own location
        String response = NetworkHelper.UpdateLocation(username, test_lat, test_lon,
                access_token);

        // Throws exception if the server doesn't accept this location update
        if (!isAccepted(response))
            throw new AssertionError(response);
    }


    // Test to make sure server returns error if invalid lat/lon inputted
    @Test
    public void InvalidUpdateLocationTest() throws Exception {

        // Register and login user
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, test_password, test_avatar_url,
                test_description);
        String access_token = NetworkHelper.Login(username, test_password);

        // TODO: iterate through all illegal characters which will be contained in a constant array

        String response = NetworkHelper.UpdateLocation(username, "??", "15",
                access_token);
        if (isAccepted(response))
            throw new AssertionError(response);

        response = NetworkHelper.UpdateLocation(username, "sdad", "15",
                access_token);
        if (!response.equals("400")) throw new AssertionError(response);

        response = NetworkHelper.UpdateLocation(username, "10", "??",
                access_token);
        if (!response.equals("400")) throw new AssertionError(response);

        response = NetworkHelper.UpdateLocation(username, "10", "sdad",
                access_token);
        if (!response.equals("400")) throw new AssertionError(response);

        response = NetworkHelper.UpdateLocation(username, " ", "sdad",
                access_token);
        if (!response.equals("400")) throw new AssertionError(response);

        response = NetworkHelper.UpdateLocation(username, "10", " ",
                access_token);
        if (!response.equals("400")) throw new AssertionError(response);
    }


    // Obviously assumes update location works, as that needs to be called in order
    // to initialize the the location. Only functions for default direction, distance
    // being returned so far
    @Test
    public void GetLocationTest() throws Exception {

        // Register and login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, test_password, test_avatar_url,
                test_description);
        String access_token = NetworkHelper.Login(username, test_password);

        // Updates the users own location
        String response = NetworkHelper.UpdateLocation(username, test_lat, test_lon,
                access_token);

        // Tests that the location correctly updates to the defult values
        if (!NetworkHelper.RetrieveLocation(username, access_token)[0].equals(default_lat))
            throw new AssertionError(response);
        if (!NetworkHelper.RetrieveLocation(username, access_token)[1].equals(default_lon))
            throw new AssertionError(response);
    }


    // Tests that you cannot update the location of another use
    @Test
    public void UpdateOthersLocationTest() throws Exception {

        // Register and login user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, test_password, test_avatar_url,
                test_description2);
        String access_token1 = NetworkHelper.Login(username1, test_password);

        // Register and Login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, test_password, test_avatar_url,
                test_description2);
        String access_token2 = NetworkHelper.Login(username2, test_password);

        // User 1 updates its own location
        NetworkHelper.UpdateLocation(username1, test_lat1, test_lon1, access_token1);

        // User 2 tried to update location of user 1. Will throw error if the
        // server allows this to occur
        String response = NetworkHelper.UpdateLocation(username1, test_lat2, test_lon2,
                access_token2);
        if (isAccepted(response)) throw new AssertionError(response);

    }


    // Tests that server accepts friend request
    @Test
    public void FriendRequestTest() throws Exception {

        // Register user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, test_password, test_avatar_url, test_description);

        // Register and login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, test_password, test_avatar_url, test_description);
        String access_token2 = NetworkHelper.Login(username2, test_password);

        // User 2 sends a friend request to user 1
        String response = NetworkHelper.AddFriend(username1, access_token2);

        // Checks that the server accepted this request
        if (!isAccepted(response)) throw new AssertionError(response);
    }


    // Tests that you can't send multiple friend requests to a single user from
    // the same user
    @Test
    public void DoubleFriendRequestTest() throws Exception {

        // Register and login user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, test_password, test_avatar_url,
                test_description2);
        String access_token1 = NetworkHelper.Login(username1, test_password);

        // Register and login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, test_password, test_avatar_url,
                test_description2);
        String access_token2 = NetworkHelper.Login(username1, test_password);

        // User 2 sends friend request to user 1
        NetworkHelper.AddFriend(username1, access_token2);

        // User 2 sends second friend request to user 1
        String response = NetworkHelper.AddFriend(username1, access_token2);

        // Checks that the server doesn't accept the second friend request
        if (isAccepted(response)) throw new AssertionError(response);
    }


    // Tests that the server accepts accepting a friend request
    @Test
    public void AcceptFriendTest() throws Exception {

        // Register and login user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, test_password, test_avatar_url,
                test_description2);
        String access_token1 = NetworkHelper.Login(username1, test_password);

        // Register and login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, test_password, test_avatar_url,
                test_description2);
        String access_token2 = NetworkHelper.Login(username2, test_password);

        // User 2 sends friend request to user 1
        NetworkHelper.AddFriend(username1, access_token2);

        // Get a list of user 1's incoming friend requests
        HashMap<String, String> out = NetworkHelper.GetIncomingFriendRequests(access_token1);
        String token = out.get(username2);

        // User 1 accepts friend request
        NetworkHelper.AcceptFriend(token, access_token1);

        // Checks that user 1 now has user 2 as a friend
        if (!NetworkHelper.GetFriends(access_token1).get(0).get(0)
            .equals(username2))
            throw new AssertionError();

        // Checks that user 2 now has user 1 as a friend
        if (!NetworkHelper.GetFriends(access_token1).get(0).get(0)
                .equals(username2))
            throw new AssertionError();
    }


    // Tests only user that friend request was sent to can accept it
    @Test
    public void UnauthorisedFriendRequestAcceptTest() throws Exception {

        // Register and login user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, test_password, test_avatar_url,
                test_description2);
        String access_token1 = NetworkHelper.Login(username1, test_password);

        // Register and login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, test_password, test_avatar_url,
                test_description2);
        String access_token2 = NetworkHelper.Login(username2, test_password);

        // User 2 sends friend request to user 1
        NetworkHelper.AddFriend(username1, access_token2);

        // User 1 gets incoming friend requests
        HashMap<String, String> user1map =
                NetworkHelper.GetIncomingFriendRequests(access_token1);

        // Gets the friendship token associated with user 2
        String user1_friendship_token = user1map.get(username2);

        // User 2 tries to accept the friend request that himself sent
        String response = NetworkHelper.AcceptFriend(user1_friendship_token,
                access_token2);

        // Checks that the server rejects user 2 accepting friend request,
        // as only the target user should be able to accept the friend request
        if (isAccepted(response)) throw new AssertionError(response);
    }


    // Tests getting a users friends list
    @Test
    public void FriendsListTest() throws Exception {

        // Register and login user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, test_password, test_avatar_url,
                test_description1);
        String access_token1 = NetworkHelper.Login(username1, test_password);

        // Register and login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, test_password, test_avatar_url,
                test_description2);
        String access_token2 = NetworkHelper.Login(username2, test_password);

        // Register and login user 3
        String username3 = UUID.randomUUID().toString();
        NetworkHelper.Register(username3, test_password, test_avatar_url,
                test_description3);
        String access_token3 = NetworkHelper.Login(username3, test_password);

        // User 1 sends friend request to user 2
        NetworkHelper.AddFriend(username2, access_token1);

        // User 1 sends friend request to user 3
        NetworkHelper.AddFriend(username3, access_token1);;

        // User 2 gets incoming friend requests
        HashMap<String, String> user2map =
                NetworkHelper.GetIncomingFriendRequests(access_token2);

        // Gets the friendship token associated with user 1
        String user2_friendship_token = user2map.get(username1);

        // User 3 gets incoming friend requests
        HashMap<String, String> user3map =
                NetworkHelper.GetIncomingFriendRequests(access_token3);

        // Gets the friendship token associated with user 1
        String user3_friendship_token = user3map.get(username1);

        // User 2 accepts friend request of user 1
        NetworkHelper.AcceptFriend(user2_friendship_token, access_token2);

        // User 3 accepts friend request of user 1
        NetworkHelper.AcceptFriend(user3_friendship_token, access_token3);

        // User 1 gets gets a list of his friends and des
        ArrayList<ArrayList<String>> out = NetworkHelper.GetFriends(access_token1);

        if (out.size() != 2) throw new AssertionError();
        if (!out.get(0).get(0).equals(username2)) throw new AssertionError();
        if (!out.get(0).get(1).equals(test_description2)) throw new AssertionError();
        if (!out.get(1).get(0).equals(username3)) throw new AssertionError();
        if (!out.get(1).get(1).equals(test_description3)) throw new AssertionError();
    }


    // Test that removing a friend works
    @Test
    public void RemoveFriendTest() throws Exception {
        // Register user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, test_password, test_avatar_url,
                test_description);
        String access_token1 = NetworkHelper.Login(username1, test_password);

        // Register and Login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, test_password, test_avatar_url,
                test_description);
        String access_token2 = NetworkHelper.Login(username2, test_password);

        // User 2 sends friend request to user 1
        NetworkHelper.AddFriend(username1, access_token2);

        // User 1 gets incoming friend requests
        HashMap<String, String> user1map =
                NetworkHelper.GetIncomingFriendRequests(access_token1);

        // Gets the friendship token associated with user 2
        String user1_friendship_token = user1map.get(username2);

        // User 1 accepts friend request of user 2
        NetworkHelper.AcceptFriend(user1_friendship_token, access_token1);

        // User 1 removes user 2
        String response = NetworkHelper.RemoveFriend(username2,
                access_token1);

        // Check that both users now have no friends
        if (NetworkHelper.GetFriends(access_token1).size() != 0)
            throw new AssertionError(response);

        if (NetworkHelper.GetFriends(access_token2).size() != 0)
            throw new AssertionError(response);
    }


    // Cheks that a user can successfully view their incoming friend requests
    @Test
    public void GetIncomingFriendRequestsTest() throws Exception {
        // Register and Login user 1 first
        String username1 = UUID.randomUUID().toString();

        NetworkHelper.Register(username1, test_password, test_avatar_url,
                test_description2);
        String access_token1 = NetworkHelper.Login(username1, test_password);

        // Register and Login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, test_password, test_avatar_url,
                test_description2);
        String access_token2 = NetworkHelper.Login(username2, test_password);

        // Checks that there are no friend requests present before
        // they are sent
        if (NetworkHelper.GetIncomingFriendRequests(access_token2) != null)
            throw new AssertionError();
        if (NetworkHelper.GetIncomingFriendRequests(access_token1) != null)
            throw new AssertionError();

        // User 2 sends friend request to user 1
        NetworkHelper.AddFriend(username1, access_token2);

        // Get user 1's incoming friend requests
        HashMap<String, String> out = NetworkHelper.GetIncomingFriendRequests(access_token1);

        // Checks that user 2's friend request is received by user 1
        if (!out.keySet().contains(username2)) throw new AssertionError();
    }


    // Checks a users outgoing friend requests
    @Test
    public void GetOutgoingFriendRequestsTest() throws Exception {
        // Register user 1 first
        String username1 = UUID.randomUUID().toString();

        NetworkHelper.Register(username1, test_password, test_avatar_url,
                test_description2);
        String access_token1 = NetworkHelper.Login(username1, test_password);

        // Register and Login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, test_password, test_avatar_url,
                test_description2);
        String access_token2 = NetworkHelper.Login(username2, test_password);

        // Makes sure users don't have any outgoing friend requests before
        // before any are sent
        if (NetworkHelper.GetOutgoingFriendRequests(access_token2) != null)
            new AssertionError();
        if (NetworkHelper.GetOutgoingFriendRequests(access_token1) != null)
            throw new AssertionError();

        // User 2 sends friend request to user 1
        NetworkHelper.AddFriend(username1, access_token2);

        // Check that the outgoing friend request is there
        ArrayList<ArrayList<String>> out = NetworkHelper
                .GetOutgoingFriendRequests(access_token2);
        if (!out.get(0).get(0).toString().equals(username1))
            throw new AssertionError();
    }



    /**
     * Helper method that determines  if the http response from the server
     * indicates a success. Helpful as a range of http responses starting
     * with '2' all indicate success.
     *
     * @param httpResponse: A string of the http response received from the
     *                      server
     * @return If the message was successful
     */
    private Boolean isAccepted(String httpResponse){
        return httpResponse.charAt(0) == '2';
    }
}

















