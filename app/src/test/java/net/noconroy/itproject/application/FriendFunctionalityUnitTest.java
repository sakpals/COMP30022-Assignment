package net.noconroy.itproject.application;

import android.util.Log;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Created by sampadasakpal on 7/10/17.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({NetworkHelper.class})
public class FriendFunctionalityUnitTest {

    private static final String FAILED_ACCEPT_FRIEND_TEST = "ERROR Accept Friend:";

    private static String username1;
    private static String username2;
    private static String user2_profile_desc = "user_2 profile description";
    private static String user1_profile_desc = "user_1 profile description";

    private static String access_token1 = "r24kj32fkjelwjrklwjerlkewjrlwejrlwjeklrjwe";
    private static String access_token2 = "rejkwj4ljklrewrjlewjreiw042830jlkejrlewjrl";
    private static String password1 = "password1_test";
    private static String password2 = "password2_test";

    private static HashMap<String, String> user1_friend_requests;
    private static HashMap<String, String> user2_friend_requests;
    private static String friendship_token1 = "kltklymejyketkrkrejt9erjtkjretjkerjvkt";
    private static String friendship_token2 = "orepworewllekwrjkvjerjwekrjewkjrklvewj";

    private static ArrayList<ArrayList<String>> user1_friends_list;
    private static ArrayList<ArrayList<String>> user2_friends_list;
    private static ArrayList<String> user2_profile;
    private static ArrayList<String> user1_profile;


    @Before
    public void setUp() {

        /* create random usernames*/
        username1 = UUID.randomUUID().toString();
        username2 = UUID.randomUUID().toString();

        PowerMockito.mockStatic(NetworkHelper.class);

        Mockito.when(NetworkHelper.Register(username1, password1, "", "")).thenReturn("201");
        Mockito.when(NetworkHelper.Login(username1, password1)).thenReturn(access_token1);

        Mockito.when(NetworkHelper.Register(username2, password2, "", "")).thenReturn("201");
        Mockito.when(NetworkHelper.Login(username2, password2)).thenReturn(access_token2);

        /* For AddFriendActivity */
        Mockito.when(NetworkHelper.AddFriend(username2, access_token1)).thenReturn("201", "401", "401");
        Mockito.when(NetworkHelper.AddFriend(username1, access_token2)).thenReturn("201", "401", "401");


        /* For FriendRequestsActivity */

        /* getting friend requests */
        user2_friend_requests = new HashMap<>();
        user2_friend_requests.put(username1, friendship_token1);
        Mockito.when(NetworkHelper.GetIncomingFriendRequests(access_token2)).thenReturn(user2_friend_requests);

        user1_friend_requests = new HashMap<>();
        user1_friend_requests.put(username2, friendship_token2);
        Mockito.when(NetworkHelper.GetIncomingFriendRequests(access_token1)).thenReturn(user1_friend_requests);

        /* accepting friend requests */
        Mockito.when(NetworkHelper.AcceptFriend(friendship_token1, access_token2)).thenReturn("201", "404", "400");
        Mockito.when(NetworkHelper.AcceptFriend(friendship_token2, access_token1)).thenReturn("201", "404", "400");

        /* For FriendActivity */
        /* creating user_2 and user_1's profile */
        user2_profile = new ArrayList<>();
        user2_profile.add(username2);
        user2_profile.add(user2_profile_desc);

        user1_profile = new ArrayList<>();
        user1_profile.add(username1);
        user1_profile.add(user1_profile_desc);

        /* adding user_1's profile to user_2's friends list */
        user1_friends_list = new ArrayList<>();
        user1_friends_list.add(user2_profile);

        /* adding user_2's profile to user_1's friends list */
        user2_friends_list = new ArrayList<>();
        user2_friends_list.add(user1_profile);

        Mockito.when(NetworkHelper.GetFriends(access_token1)).thenReturn(user2_friends_list);
        Mockito.when(NetworkHelper.GetFriends(access_token2)).thenReturn(user1_friends_list);

        Mockito.when(NetworkHelper.RemoveFriend(username1, access_token2)).thenReturn("200", "401");
        Mockito.when(NetworkHelper.RemoveFriend(username2, access_token1)).thenReturn("200", "401");
    }


    /* =========================================================================================
     * Tests for AddFriendActivity
     * =========================================================================================*/

    /* checks that access token is being generated */
    @Test
    public void testAccessToken() {
        String user1_access_token = null;
        NetworkHelper.Register(username1, password1, "", "");
        user1_access_token = NetworkHelper.Login(username1, password1);
        assertTrue(user1_access_token != null);

        String user2_access_token = null;
        NetworkHelper.Register(username2, password2, "", "");
        user2_access_token = NetworkHelper.Login(username2, password2);
        assertTrue(user2_access_token != null);
    }

    /* tests to see whether friend request has been send upon adding friend*/
    @Test
    public void testAddFriend() {
        String response1 = NetworkHelper.AddFriend(username1, access_token2);
        assertTrue(response1.equals("201"));

        String response2 = NetworkHelper.AddFriend(username2, access_token1);
        assertTrue(response2.equals("201"));

    }

    /* tests to see whether friend request fails when trying to add the same friend twice */
    @Test
    public void testAddFriendTwice() {
        String response = NetworkHelper.AddFriend(username1, access_token2);
        assertTrue(response.equals("201"));

        String response2 = NetworkHelper.AddFriend(username1, access_token2);
        assertTrue(response2.equals("401"));
    }

    /* tests to see whether friend request fails due to some network error */
    @Test
    public void testNetworkError() {
        Mockito.when(NetworkHelper.AddFriend(username1, access_token2)).thenReturn("500");
        String response = NetworkHelper.AddFriend(username1, access_token2);
        assertTrue(response.equals("500"));

    }

    /* tests to see whether friend request fails due to user (being searched for) not existing  */
    @Test
    public void testFriendSearchError() {
        Mockito.when(NetworkHelper.AddFriend(username1, access_token2)).thenReturn("404");
        String response = NetworkHelper.AddFriend(username1, access_token2);
        assertTrue(response.equals("404"));

    }


     /* =========================================================================================
     * Tests for FriendRequestsActivity
     *
     * =========================================================================================*/

    /* tests to see whether incoming friend requests returns a request or not */
    @Test
    public void testIncomingFriendRequests() {
        /* user2 checking their friend requests */
        HashMap<String, String> requests = NetworkHelper.GetIncomingFriendRequests(access_token2);
        assertTrue(requests.size() != 0);
        assertTrue(requests.get(username1).equals(friendship_token1));

        /* user1 checking their friend requests */
        HashMap<String, String> requests2 = NetworkHelper.GetIncomingFriendRequests(access_token1);
        assertTrue(requests2.size() != 0);
        assertTrue(requests2.get(username2).equals(friendship_token2));
    }

    /* tests to see whether friend requests can be accepted, and the changes made to incoming friend requests hashmap*/
    @Test
    public void testAcceptFriendRequest() {
        /* user2 accepting user 1's friend request */
        String response = NetworkHelper.AcceptFriend(friendship_token1, access_token2);
        assertTrue(response.equals("201"));

        try {
            removeFriendRequest(user2_friend_requests, friendship_token1);
            assertTrue(user2_friend_requests.size() == 0);
        }
        catch (Exception e) {
            Log.e(FAILED_ACCEPT_FRIEND_TEST, "The friend you're trying to accept doesn't exist in your requests");
        }

        /* user1 accepting user 2's friend request */
        String response2 = NetworkHelper.AcceptFriend(friendship_token2, access_token1);
        assertTrue(response2.equals("201"));

        try {
            removeFriendRequest(user1_friend_requests, friendship_token2);
            assertTrue(user1_friend_requests.size() == 0);
        }
        catch (Exception e) {
            Log.e(FAILED_ACCEPT_FRIEND_TEST, "The friend you're trying to accept doesn't exist in your requests");
        }

    }

    /* checks that user cannot accept the same friend request twice */
    @Test
    public void testAcceptFriendRequestTwice() {

        String response;
        response = NetworkHelper.AcceptFriend(friendship_token1, access_token2);
        assertTrue(response.equals("201"));

        try {
            removeFriendRequest(user2_friend_requests, friendship_token1);
            assertTrue(user2_friend_requests.size() == 0);
        }
        catch (Exception e) {
            Log.e(FAILED_ACCEPT_FRIEND_TEST, "The friend you're trying to accept doesn't exist in your requests");
        }

        response = NetworkHelper.AcceptFriend(friendship_token1, access_token2);
        assertTrue(response.equals("404"));

    }

    /* Checks that user cannot accept request from a user that is already their friend.
    * NOTE: should never come to this, but good to check */
    @Test
    public void testAlreadyFriendsAndTryingToAccept() {
        Mockito.when(NetworkHelper.AcceptFriend(friendship_token1, access_token2)).thenReturn("400");
        String response = NetworkHelper.AcceptFriend(friendship_token1, access_token2);
        assertTrue(response.equals("400"));
    }

    /* tests to see whether accept friend request fails due to some network error */
    @Test
    public void testAcceptRequestNetworkError() {
        Mockito.when(NetworkHelper.AcceptFriend(friendship_token1, access_token2)).thenReturn("500");
        String response = NetworkHelper.AcceptFriend(friendship_token1, access_token2);
        assertTrue(response.equals("500"));
    }

    /**
     * Removes friend request from hash map
     * @param requests hashmap to remove friend request from
     * @param friendship_token friendship token to identify which friend to remove
     * @throws Exception in case of trying to remove friend that isn't in the hashmap (e.g. already been removed)
     */
    public void removeFriendRequest(HashMap<String, String> requests, String friendship_token) throws Exception {

        if(friendship_token == friendship_token1) {
            requests.remove(username1);
        }
        else {
            requests.remove(username2);
        }
    }

    /* =========================================================================================
     * Tests for FriendActivity
     * NOTE: this only simulates what is to be expected from FriendActivity.
     * =========================================================================================*/


    /* Checks that a user is able to retrieve their friends list */
    @Test
    public void testGetFriendsList() {
        assertTrue(NetworkHelper.GetFriends(access_token1).equals(user2_friends_list));
        assertTrue(NetworkHelper.GetFriends(access_token2).equals(user1_friends_list));
    }

    /* Checks to see that once a user removes a friend, then that friend is no longer in
    * their friends list.
    * NOTE: in this case each user (and for the purpose of this test, user_1) has only one
    * friend. */
    @Test
    public void testRemoveFriendFromFriendsList() {
        String response = NetworkHelper.RemoveFriend(username2, access_token1);
        assertTrue(response.equals("200"));

        removeFriendFromList(user1_friends_list, username2);
        String response2 = NetworkHelper.RemoveFriend(username2, access_token1);
        assertTrue(response2.equals("401"));
        assertTrue(user1_friends_list.size() == 0);


    }

    /**
     * Removes a friend from the user's friends list
     * @param list user's friends list
     * @param username the username of the friend to be removed
     */
    public void removeFriendFromList(ArrayList<ArrayList<String>> list, String username) {
        for(int i=0; i < list.size(); i ++) {
            if(list.get(i).contains(username)) {
                list.remove(i);
            }
        }
    }

}

