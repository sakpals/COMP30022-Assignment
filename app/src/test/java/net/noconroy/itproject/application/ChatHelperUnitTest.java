package net.noconroy.itproject.application;

import com.google.gson.Gson;

import org.junit.AssumptionViolatedException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

public class ChatHelperUnitTest {

    // Everything here assumes all methods in NetworkHelper work correctly

    private static String username;
    private static String access_token;
    private static String channel_name;

    @BeforeClass
    public void AccountSetup() {
        String username = UUID.randomUUID().toString();
        NetworkHelper.Register(username, "test_password", "test_avatar_url", "test_description");
        String access_token = NetworkHelper.Login(username, "test_password");
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
        if (!ChatHelper.SubscribeChannel(channel_name, access_token).equals("200"))
            throw new AssertionError();
    }

    @Test
    public void LeaveChannelTest() {
        String channel_name = UUID.randomUUID().toString();
        ChatHelper.CreateChannel(channel_name, access_token);
        ChatHelper.SubscribeChannel(channel_name, access_token);
        if (!ChatHelper.LeaveChannel(channel_name, access_token).equals("200"))
            throw new AssertionError();
    }

    @Test
    public void MessageChannelTest() {
        Gson test_message = new Gson();
        // TODO: build test message
        String channel_name = UUID.randomUUID().toString();
        ChatHelper.CreateChannel(channel_name, access_token);
        ChatHelper.SubscribeChannel(channel_name, access_token);
        if (!ChatHelper.MessageChannel(channel_name, test_message, access_token).equals("200"))
            throw new AssertionError();
    }

    @Test
    public void PollChannelsTest() {
        String channel_name = UUID.randomUUID().toString();
        ChatHelper.CreateChannel(channel_name, access_token);
        ChatHelper.SubscribeChannel(channel_name, access_token);
        if (!ChatHelper.PollChannels(access_token).equals("200"))
            throw new AssertionError();
    }
}


















