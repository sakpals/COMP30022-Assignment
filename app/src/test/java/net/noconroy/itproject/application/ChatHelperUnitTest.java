package net.noconroy.itproject.application;

import com.google.gson.Gson;

import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

public class ChatHelperUnitTest {

    // Everything here assumes all methods in NetworkHelper work correctly

    private static String username;
    private static String username2;
    private static String access_token;
    private static String access_token2;

    @Before
    public void AccountSetup() {
        username = UUID.randomUUID().toString();
        username2 = UUID.randomUUID().toString();
        NetworkHelper.Register(username, "test_password", "test_avatar_url", "test_description");
        NetworkHelper.Register(username2, "test_password", "test_avatar_url", "test_description");
        access_token = NetworkHelper.Login(username, "test_password");
        access_token2 = NetworkHelper.Login(username2, "test_password");
    }

    @Test
    public void CreateChannelTest() {
        String channel_name = UUID.randomUUID().toString();
        if (!ChatHelper.CreateChannel(channel_name, access_token).equals("201"))
            throw new AssertionError();
    }

    @Test
    public void DeleteChannelTest() {
        String channel_name = UUID.randomUUID().toString();
        ChatHelper.CreateChannel(channel_name, access_token);
        if (!ChatHelper.DeleteChannel(channel_name, access_token).equals("200"))
            throw new AssertionError();
    }

    @Test
    public void SubscribeChannelTest() {
        String channel_name = UUID.randomUUID().toString();
        ChatHelper.CreateChannel(channel_name, access_token);
        if (!ChatHelper.SubscribeChannel(channel_name, access_token2).equals("200"))
            throw new AssertionError();
    }

    @Test
    public void LeaveChannelTest() {
        String channel_name = UUID.randomUUID().toString();
        ChatHelper.CreateChannel(channel_name, access_token);
        ChatHelper.SubscribeChannel(channel_name, access_token2);
        if (!ChatHelper.LeaveChannel(channel_name, access_token2).equals("200"))
            throw new AssertionError();
    }

    @Test
    public void MessageChannelTest() {
        String channel_name = UUID.randomUUID().toString();
        ChatHelper.CreateChannel(channel_name, access_token);
        ChatHelper.SubscribeChannel(channel_name, access_token2);
        if (!ChatHelper.MessageChannel(channel_name, "test_message", access_token2).equals("200"))
            throw new AssertionError();
    }

    @Test
    public void ListenChannelsTest() {
        String channel_name = UUID.randomUUID().toString();
        ChatHelper.CreateChannel(channel_name, access_token);
        ChatHelper.SubscribeChannel(channel_name, access_token2);
        ChatHelper.ListenChannels(access_token2);
    }
}


















