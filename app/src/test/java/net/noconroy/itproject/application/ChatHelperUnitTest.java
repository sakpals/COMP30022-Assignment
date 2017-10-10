package net.noconroy.itproject.application;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class ChatHelperUnitTest {

    // Everything here assumes all methods in NetworkHelper work correctly

    private static String username;
    private static String username2;
    private static String access_token;
    private static String access_token2;

    // Arbitary variables used to test that methods are working correctly
    private final String test_password = "test_password";
    private final String test_avatar_url = "test_avatar_url";
    private final String test_description = "test_description";
    private final String test_message = "test_message";


    @Before
    public void AccountSetup() {
        username = UUID.randomUUID().toString();
        username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username, test_password, test_avatar_url, test_description);
        NetworkHelper.Register(username2, test_password, test_avatar_url, test_description);
        access_token = NetworkHelper.Login(username, test_password);
        access_token2 = NetworkHelper.Login(username2, test_password);
    }

    @Test
    public void CreateChannelTest() {
        String channel_name = UUID.randomUUID().toString();
        String response = ChatHelper.CreateChannel(channel_name, access_token, true);
        assertEquals(response, "201");
    }

    @Test
    public void DeleteChannelTest() {
        String channel_name = UUID.randomUUID().toString();
        ChatHelper.CreateChannel(channel_name, access_token, true);
        String response = ChatHelper.DeleteChannel(channel_name, access_token);
        assertEquals(response, "200");
    }

    @Test
    public void SubscribeChannelTest() {
        String channel_name = UUID.randomUUID().toString();
        ChatHelper.CreateChannel(channel_name, access_token, true);
        String response = ChatHelper.SubscribeChannel(channel_name, access_token2);
        assertEquals(response, "200");
    }

    @Test
    public void LeaveChannelTest() {
        String channel_name = UUID.randomUUID().toString();
        ChatHelper.CreateChannel(channel_name, access_token, true);
        ChatHelper.SubscribeChannel(channel_name, access_token2);
        String response = ChatHelper.LeaveChannel(channel_name, access_token2);
        assertEquals(response, "200");
    }

    @Test
    public void MessageChannelTest() {
        String channel_name = UUID.randomUUID().toString();
        ChatHelper.CreateChannel(channel_name, access_token, true);
        ChatHelper.SubscribeChannel(channel_name, access_token2);
        if (!ChatHelper.MessageChannel(channel_name, username2, "test_message", access_token2).equals("200"))
            throw new AssertionError();
    }

    @Test
    public void GetAllMessagesTest() {
        String channel_name = UUID.randomUUID().toString();
        ChatHelper.CreateChannel(channel_name, access_token, true);
        ChatHelper.SubscribeChannel(channel_name, access_token2);
        ChatHelper.MessageChannel(channel_name, username2, "test_message", access_token2);
        String msg = ChatHelper.GetAllMessages(channel_name, access_token);
        if (msg.isEmpty())
            throw new AssertionError();
        System.out.println(msg);
    }
/*
    @Test
    public void ListenChannelsTest() {
        String channel_name = UUID.randomUUID().toString();
        ChatHelper.CreateChannel(channel_name, access_token, true);
        ChatHelper.SubscribeChannel(channel_name, access_token2);
        ChatHelper.ListenChannels(access_token2);
    }
*/
}

