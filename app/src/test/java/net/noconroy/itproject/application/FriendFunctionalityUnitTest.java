package net.noconroy.itproject.application;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import static org.junit.Assert.assertTrue;
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

    private static String username1;
    private static String username2;
    private static String access_token1 = "r24kj32fkjelwjrklwjerlkewjrlwejrlwjeklrjwe";
    private static String access_token2 = "rejkwj4ljklrewrjlewjreiw042830jlkejrlewjrl";
    private static String password1 = "password1_test";
    private static String password2 = "password2_test";

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
     * =========================================================================================*/
    // MORE COMING...
}

