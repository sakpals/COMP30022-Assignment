package net.noconroy.itproject.application;

import android.text.Editable;
import android.widget.EditText;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import java.util.UUID;
import android.text.Editable;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import net.noconroy.itproject.application.AddFriendActivity.AddUserAsFriendTask;


import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.eq;

/**
 * Created by sampadasakpal on 24/9/17.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({NetworkHelper.class, AddUserAsFriendTask.class})
public class AddFriendUnitTest {

    private static String username1;
    private static String username2;
    private static String access_token1 = "r24kj32fkjelwjrklwjerlkewjrlwejrlwjeklrjwe";
    private static String access_token2 = "rejkwj4ljklrewrjlewjreiw042830jlkejrlewjrl";
    private static String password1 = "password1_test";
    private static String password2 = "password2_test";

    private String response;

    private AddFriendActivity addFriendActivity = Mockito.spy(new AddFriendActivity());

    private EditText findUser = Mockito.mock(EditText.class);

    private AddUserAsFriendTask user1_task = null;

    @Before
    public void setUp() {

        /* create random usernames*/
        username1 = UUID.randomUUID().toString();
        username2 = UUID.randomUUID().toString();

        PowerMockito.mockStatic(NetworkHelper.class);
        user1_task = PowerMockito.mock(AddUserAsFriendTask.class);

        Mockito.when(NetworkHelper.Register(username1, password1, "", "")).thenReturn("201");
        Mockito.when(NetworkHelper.Login(username1, password1)).thenReturn(access_token1);

        Mockito.when(NetworkHelper.Register(username2, password2, "", "")).thenReturn("201");
        Mockito.when(NetworkHelper.Login(username2, password2)).thenReturn(access_token2);

        Mockito.when(NetworkHelper.AddFriend(username2, access_token1)).thenReturn("201");
        Mockito.when(NetworkHelper.AddFriend(username1, access_token2)).thenReturn("201");

        try {
            PowerMockito.whenNew(AddUserAsFriendTask.class).withArguments(username2, access_token1).thenReturn(user1_task);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

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


}
