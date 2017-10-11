package net.noconroy.itproject.application.Chat;

import android.app.Activity;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.noconroy.itproject.application.NetworkHelper;
import net.noconroy.itproject.application.callbacks.EmptyCallback;
import net.noconroy.itproject.application.callbacks.NetworkCallback;
import net.noconroy.itproject.application.models.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Created by James on 8/09/2017.
 */

public final class ChatHelper {

    private final static String CHAT_MESSAGE_TYPE = "m.message.text";
    private final static String CHAT_MESSAGE_DATA = "text";

    private HashMap<String, ArrayList<Message>> messages = new HashMap<>();
    private Activity activity = null;

    public void sendMessage(String username, final String message, final NetworkCallback<Message> cb) {

        JsonObject msg = new JsonObject();
        msg.addProperty(CHAT_MESSAGE_DATA, message);
        NetworkHelper.ChannelMessage("user_" + username, CHAT_MESSAGE_TYPE, msg, new NetworkCallback<Message>(Message.class, null) {
            @Override
            public void onSuccess(Message new_message) {
                AddMessage(new_message);
                cb.onSuccess(new_message);
            }

            @Override
            public void onFailure(Failure f) {
                cb.onFailure(f);
            }
        });
    }

    private ArrayList<NetworkHelper.Receiver> receivers = new ArrayList<>();

    public void addReceiver(NetworkHelper.Receiver r) {
        receivers.add(r);
    }

    public void removeReceiver(NetworkHelper.Receiver r) {
        receivers.remove(r);
    }


    private void AddMessage(final Message message) {
        ArrayList<Message> user_messages = messages.get(message.user);
        if(user_messages == null) user_messages = new ArrayList<>();
        user_messages.add(message);
        messages.put(message.user, user_messages);

        Runnable receiverRunnable = new Runnable() {
            @Override
            public void run() {
                for(NetworkHelper.Receiver r : receivers) {
                    r.process(message);
                }
            }
        };

        if(activity == null) {
            receiverRunnable.run();
        } else {
            activity.runOnUiThread(receiverRunnable);
        }
    }

    private NetworkHelper.Receiver receiver = new NetworkHelper.Receiver() {
        @Override
        public void process(Message message) {
            AddMessage(message);
        }
    };

    public ChatHelper() {
        NetworkHelper.ChannelAddListener(CHAT_MESSAGE_TYPE, receiver);
        NetworkHelper.ChannelListen();
    }

    public void loadUserMessages(String friend, String name) {
        NetworkCallback<NetworkHelper.Messages> cb = new NetworkCallback<NetworkHelper.Messages>(NetworkHelper.Messages.class, null) {
            @Override
            public void onSuccess(NetworkHelper.Messages object) {
                for (Message m: object.messages) {
                    if(m.type.equals(CHAT_MESSAGE_TYPE))
                        AddMessage(m);
                }
            }

            @Override
            public void onFailure(Failure f) {

            }
        };

        NetworkHelper.ChannelMessages("user_"+name, cb);
        NetworkHelper.ChannelMessages("user_"+friend, cb);
    }

    public void stop() {
        NetworkHelper.ChannelRemoveListener(CHAT_MESSAGE_TYPE);
        NetworkHelper.ChannelStopListen();
    }

    public static String getText(Message m) {
        return m.data.get(CHAT_MESSAGE_DATA).getAsString();
    }
}
