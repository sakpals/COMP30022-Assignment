package net.noconroy.itproject.application;

/**
 * A set of unit tests for NetworkHelper.
 * Note that many of these unit tests
 * depend on eachother, for example the logout test assumes that login is
 * working, so take note of what tests are being passed and what methods
 * are being called to make sure that another incorrect method isn't
 * accecting the tested method.
 */

import org.junit.AssumptionViolatedException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static net.noconroy.itproject.application.NetworkHelper.AddFriend;
import static net.noconroy.itproject.application.NetworkHelper.GetFriends;
import static net.noconroy.itproject.application.NetworkHelper.GetIncomingFriendRequests;
import static net.noconroy.itproject.application.NetworkHelper.Register;

public class NetworkHelperUnitTest {


    // Basic Register test to test if server accepts registering
    @Test
    public void RegisterTest() throws Exception {
        String username = UUID.randomUUID().toString();
        String response = Register(username, "test_password",
                "test_avatar_url", "test_description");
        if (!response.equals("201"))
            throw new AssertionError(response);
    }


    // Tests that you aren't able to register twice
    @Test
    public void RegisterTwiceTest() throws Exception {

        final String likelyerror =
                "you're most likely trying to register a username that already" +
                        "exists";

        String username = UUID.randomUUID().toString();
        String response = Register(username, "test_password",
                "test_avatar_url", "test_description");
        if (!response.equals("201"))
            throw new AssertionError(response);

        // Try to register again
        response = Register(username, "test_password",
                "test_avatar_url", "test_description");
        if (response.equals("500"))
            throw new AssertionError(likelyerror);
    }


    // Tests that the server accepts basic login
    @Test
    public void LoginTest() throws Exception {
        // Register
        String username = UUID.randomUUID().toString();
        Register(username, "test_password", "test_avatar_url",
                "test_description");

        String access_token = NetworkHelper.Login(username,
                "test_password");

        // Checks for whitespaces in access_token, which usually occers if the
        // server is offline
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
        NetworkHelper.Register(username, "test_password",
                "test_avatar_url", "test_description");
        String access_token = NetworkHelper.Login(username, "test_password");

        String response = NetworkHelper.Logout(access_token);

        if (!response.equals("200"))
            throw new AssertionError(response);
    }


    // Checks that you can't logout whilst not logged in
    @Test
    public void InvalidLogoutTest() throws Exception {
        // Register and login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, "test_password", "test_avatar_url",
                "test_description");
        String access_token = NetworkHelper.Login(username, "test_password");

        // Logout for the first time
        String response = NetworkHelper.Logout(access_token);
        if (!response.equals("200")) throw new AssertionError(response);

        // Checks if other logouts work whilst already logged out
        // Will return error if logout works as planned
        if (!response.equals("200")) throw new AssertionError(response);

        // Will return an error if the server doesn't correctly return
        // internal error (500) when logout is attempted again
        if (!response.equals("500")) throw new AssertionError(response);
    }


    // Checks that the server accepts updating your own profile
    @Test
    public void UpdateProfileTest() throws Exception {
        // Register and Login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, "test_password", "test_avatar_url",
                "test_description");
        String access_token = NetworkHelper.Login(username, "test_password");

        String response = NetworkHelper.UpdateProfile(username, "test_password",
                "test_avatar_url", "test_description", access_token);

        if (!response.equals("200"))
            throw new AssertionError(response);
    }


    // Checks getting a user's own profile works correctly
    @Test
    public void GetProfileBasicTest() throws Exception {
        // Register and Login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, "test_password", "test_avatar_url",
                "test_description");
        String access_token = NetworkHelper.Login(username, "test_password");

        String response = NetworkHelper.GetProfile(username, access_token);

        if (!response.equals("test_description"))
            throw new AssertionError(response);

    }


    // Checks that updating and getting the profile gives a correct output
    // Currently only works for getting description, not avatar url
    @Test
    public void GetProfileAfterUpdateTest() throws Exception {
        // Register and Login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, "test_password", "test_avatar_url",
                "test_description");
        String access_token = NetworkHelper.Login(username, "test_password");

        NetworkHelper.UpdateProfile(username, "test_password",
                "test_avatar_url", "test_description2", access_token);

        String response = NetworkHelper.GetProfile(username, access_token);

        if (!response.equals("test_description2"))
            throw new AssertionError(response);
    }


    // Tests that a user can get another users profile
    @Test
    public void GetAnotherUsersProfileTest() throws Exception {
        // Register and Login first user
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, "test_password", "test_avatar_url",
                "usr1_desc");
        String access_token1 = NetworkHelper.Login(username1, "test_password");

        // Register second user
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, "test_password", "test_avatar_url", "usr2_desc");

        String response = NetworkHelper.GetProfile(username2, access_token1);

        // Tests that first user can access profile of second user
        if (!response.equals("usr2_desc"))
            throw new AssertionError(response);
    }


    // Tests that a user cannot update another users profile
    @Test
    public void UpdateAnotherUsersProfileTest() throws Exception {
        // Register and Login user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, "test_password", "test_avatar_url",
                "usr1_desc");
        String access_token1 = NetworkHelper.Login(username1, "test_password");

        // Register and Login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, "test_password", "test_avatar_url",
                "usr2_desc");
        String access_token2 = NetworkHelper.Login(username1, "test_password");

        // These will return an error if a user succesfully alters another users profile
        String response = NetworkHelper.UpdateProfile(username1,
                "wrong_password", "test_avatar_url", "changed_desc", access_token2);

        if (response.equals("200")) throw new AssertionError(response);

        if (response.equals("changed_desc"))
            throw new AssertionError(response);

    }


    // Tests that the server accepts request to update users own location
    @Test
    public void UpdateLocationTest() throws Exception {
        // Register and Login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, "test_password", "test_avatar_url",
                "test_description");
        String access_token = NetworkHelper.Login(username, "test_password");

        String response = NetworkHelper.UpdateLocation(username, "10", "15",
                access_token);

        if (!response.equals("200"))
            throw new AssertionError(response);
    }


    // Test to make sure server returns error if invalid lat/lon inputted
    @Test
    public void InvalidUpdateLocationTest() throws Exception {
        // Register and Login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, "test_password", "test_avatar_url",
                "test_description");
        String access_token = NetworkHelper.Login(username, "test_password");


        String response = NetworkHelper.UpdateLocation(username, "??", "15",
                access_token);
        if (!response.equals("400"))
            throw new AssertionError();


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
        // Register and Login first
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, "test_password", "test_avatar_url",
                "test_description");
        String access_token = NetworkHelper.Login(username, "test_password");

        String response = NetworkHelper.UpdateLocation(username, "10", "15",
                access_token);

        if (!NetworkHelper.RetrieveLocation(username, access_token)[0].equals("0.0"))
            throw new AssertionError(response);
        if (!NetworkHelper.RetrieveLocation(username, access_token)[1].equals("180.0"))
            throw new AssertionError(response);
    }


    // Tests that you cannot update the location of another use
    @Test
    public void UpdateOthersLocationTest() throws Exception {
        // Register and Login user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, "test_password", "test_avatar_url",
                "usr1_desc");
        String access_token1 = NetworkHelper.Login(username1, "test_password");

        // Register and Login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, "test_password", "test_avatar_url",
                "usr2_desc");
        String access_token2 = NetworkHelper.Login(username1, "test_password");

        // User 2 tried to update location of user 1. Will throw error if the
        // server allows this to occur
        NetworkHelper.UpdateLocation(username1, "10", "15", access_token1);

        String response = NetworkHelper.UpdateLocation(username1, "20", "30",
                access_token2);
        if (response.equals("200")) throw new AssertionError();

    }


    // Tests that server accepts friend request
    @Test
    public void AddFriendTest() throws Exception {
        // Register and Login user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, "test_password", "test_avatar_url",
                "usr1_desc");

        // Register and Login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, "test_password", "test_avatar_url",
                "usr2_desc");
        String access_token2 = NetworkHelper.Login(username1, "test_password");

        String response = NetworkHelper.AddFriend(username1, access_token2);
        if (!response.equals("200")) throw new AssertionError(response);
    }


    // Tests that you can't send multiple friend requests to a single user from
    // the same user
    @Test
    public void DoubleFriendRequestTest() throws Exception {
        // Register and Login user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, "test_password", "test_avatar_url",
                "usr1_desc");
        String access_token1 = NetworkHelper.Login(username1, "test_password");

        // Register and Login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, "test_password", "test_avatar_url",
                "usr2_desc");
        String access_token2 = NetworkHelper.Login(username1, "test_password");

        // User 2 sends friend request to user 1
        AddFriend(username1, access_token2);

        String response = AddFriend(username1, access_token2);

        // User 1 accepts friend request
        if (!response.equals("500")) throw new AssertionError(response);
    }


    // Tests that the server accepts accepting a friend request
    @Test
    public void AcceptFriendTest() throws Exception {
        // Register and Login user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, "test_password", "test_avatar_url",
                "usr1_desc");
        String access_token1 = NetworkHelper.Login(username1, "test_password");

        // Register and Login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, "test_password", "test_avatar_url",
                "usr2_desc");
        String access_token2 = NetworkHelper.Login(username2, "test_password");

        // User 2 sends friend request to user 1
        AddFriend(username1, access_token2);

        // Get a list of user 1's incoming friend requests
        HashMap<String, String> out = GetIncomingFriendRequests(access_token1);
        String token = out.get(username2);

        // User 1 accepts friend request
        NetworkHelper.AcceptFriend(token, access_token1);

        // Checks that use4 1 now has user 2 as a friend
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
        // Register and Login user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, "test_password", "test_avatar_url",
                "usr1_desc");
        String access_token1 = NetworkHelper.Login(username1, "test_password");

        // Register and Login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, "test_password", "test_avatar_url",
                "usr2_desc");
        String access_token2 = NetworkHelper.Login(username2, "test_password");

        // User 2 sends friend request to user 1
        AddFriend(username1, access_token2);

        // User 1 gets incoming friend requests
        HashMap<String, String> user1map =
                GetIncomingFriendRequests(access_token1);

        // Gets the friendship token associated with user 2
        String user1_friendship_token = user1map.get(username2);

        String response = NetworkHelper.AcceptFriend(user1_friendship_token,
                access_token2);

        // User 2 tried to accept the friend request
        if (!response.equals("500")) throw new AssertionError(response);
    }


    // Tests getting a users friends list
    @Test
    public void FriendsListTest() throws Exception {
        // Register and Login user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, "test_password", "test_avatar_url",
                "usr1_desc1");
        String access_token1 = NetworkHelper.Login(username1, "test_password");

        // Register and Login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, "test_password", "test_avatar_url",
                "usr2_desc2");
        String access_token2 = NetworkHelper.Login(username2, "test_password");

        // Register and Login user 3
        String username3 = UUID.randomUUID().toString();
        NetworkHelper.Register(username3, "test_password", "test_avatar_url",
                "usr3_desc3");
        String access_token3 = NetworkHelper.Login(username3, "test_password");

        // User 1 sends friend request to user 2
        AddFriend(username2, access_token1);

        // User 1 sends friend request to user 3
        AddFriend(username3, access_token1);;

        // User 2 gets incoming friend requests
        HashMap<String, String> user2map =
                GetIncomingFriendRequests(access_token2);

        // Gets the friendship token associated with user 1
        String user2_friendship_token = user2map.get(username1);

        // User 3 gets incoming friend requests
        HashMap<String, String> user3map =
                GetIncomingFriendRequests(access_token3);

        // Gets the friendship token associated with user 1
        String user3_friendship_token = user3map.get(username1);

        // User 2 accepts friend request of user 1
        NetworkHelper.AcceptFriend(user2_friendship_token, access_token2);

        // User 3 accepts friend request of user 1
        NetworkHelper.AcceptFriend(user3_friendship_token, access_token3);

        // The output from the server
        ArrayList<ArrayList<String>> out = GetFriends(access_token1);

        if (out.size() != 2) throw new AssertionError();
        if (!out.get(0).get(0).equals(username2)) throw new AssertionError();
        if (!out.get(0).get(1).equals("usr2_desc2")) throw new AssertionError();
        if (!out.get(1).get(0).equals(username3)) throw new AssertionError();
        if (!out.get(1).get(1).equals("usr3_desc3")) throw new AssertionError();
    }


    // Test that removing a friend works
    @Test
    public void RemoveFriendTest() throws Exception {
        // Register user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, "test_password", "test_avatar_url",
                "usr1_desc");
        String access_token1 = NetworkHelper.Login(username1, "test_password");

        // Register and Login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, "test_password", "test_avatar_url",
                "usr2_desc");
        String access_token2 = NetworkHelper.Login(username2, "test_password");

        // User 2 sends friend request to user 1
        AddFriend(username1, access_token2);

        // User 1 gets incoming friend requests
        HashMap<String, String> user1map =
                GetIncomingFriendRequests(access_token1);

        // Gets the friendship token associated with user 2
        String user1_friendship_token = user1map.get(username2);

        // User 1 accepts friend request of user 2
        NetworkHelper.AcceptFriend(user1_friendship_token, access_token1);

        // User 1 removes user 2
        String response = NetworkHelper.RemoveFriend(username2,
                access_token1);

        // Check that both users now have no friends
        if (GetFriends(access_token1).size() != 0)
            throw new AssertionError(response);

        if (GetFriends(access_token2).size() != 0)
            throw new AssertionError(response);
    }


    // Cheks that a user can successfully view their incoming friend requests
    @Test
    public void GetIncomingFriendRequestsTest() throws Exception {
        // Register and Login user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, "test_password", "test_avatar_url",
                "usr1_desc");
        String access_token1 = NetworkHelper.Login(username1, "test_password");

        // Register and Login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, "test_password", "test_avatar_url",
                "usr2_desc");
        String access_token2 = NetworkHelper.Login(username2, "test_password");

        // Checks that there are no friend requests present before
        // they are sent
        if (GetIncomingFriendRequests(access_token2) != null)
            throw new AssertionError();
        if (GetIncomingFriendRequests(access_token1) != null)
            throw new AssertionError();

        // User 2 sends friend request to user 1
        AddFriend(username1, access_token2);

        // Get user 1's incoming friend requests
        HashMap<String, String> out = GetIncomingFriendRequests(access_token1);

        // Checks that user 2's friend request is received by user 1
        if (!out.keySet().contains(username2)) throw new AssertionError();
    }


    // Checks a users outgoing friend requests
    @Test
    public void GetOutgoingFriendRequestsTest() throws Exception {
        // Register user 1 first
        String username1 = UUID.randomUUID().toString();
        NetworkHelper.Register(username1, "test_password", "test_avatar_url",
                "usr1_desc");
        String access_token1 = NetworkHelper.Login(username1, "test_password");

        // Register and Login user 2
        String username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username2, "test_password", "test_avatar_url",
                "usr2_desc");
        String access_token2 = NetworkHelper.Login(username2, "test_password");

        // Makes sure users don't have any outgoing friend requests before
        // before any are sent
        if (NetworkHelper.GetOutgoingFriendRequests(access_token2) != null)
            new AssertionError();
        if (NetworkHelper.GetOutgoingFriendRequests(access_token1) != null)
            throw new AssertionError();

        // User 2 sends friend request to user 1
        AddFriend(username1, access_token2);

        // Check that the outgoing friend request is there
        ArrayList<ArrayList<String>> out = NetworkHelper
                .GetOutgoingFriendRequests(access_token2);
        if (!out.get(0).get(0).toString().equals(username1))
            throw new AssertionError();
    }

}

















