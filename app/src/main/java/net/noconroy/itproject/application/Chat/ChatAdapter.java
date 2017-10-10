package net.noconroy.itproject.application.Chat;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.noconroy.itproject.application.R;

import java.util.ArrayList;

/**
 * Created by Mattias on 5/10/2017.
 * Basic functionality for ChatAdapter was sourced from:
 *      https://www.codeproject.com/Tips/897826/Designing-Android-Chat-Bubble-Chat-UI
 */

public class ChatAdapter extends BaseAdapter {

    private ArrayList<ChatMessage> chatMessages = null;
    private Activity activity = null;

    public ChatAdapter(Activity activity, ArrayList<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        if (chatMessages != null) {
            return chatMessages.size();
        }
        return 0;
    }

    @Override
    public ChatMessage getItem(int i) {
        if (chatMessages != null) {
            return chatMessages.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        ChatMessage chatMessage = getItem(i);
        LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (view == null) {
            view = vi.inflate(R.layout.activity_chat_message, null);
            holder = createViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder)view.getTag();
        }

        setMessagePositioning(holder, chatMessage.isMe());
        holder.message.setText(chatMessage.getMessage());
        holder.date.setText(chatMessage.getMessageDate());

        return view;
    }

    private void setMessagePositioning(ViewHolder holder, boolean isMe) {

        if (isMe) {
            holder.messageBackground.setBackgroundResource(R.drawable.bubble2);
            holder.content.setGravity(Gravity.RIGHT);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }
        else {
            holder.messageBackground.setBackgroundResource(R.drawable.bubble1);
            holder.content.setGravity(Gravity.LEFT);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        }
    }

    public void add(ChatMessage chatMessage) {
        chatMessages.add(chatMessage);
    }

    public void addAll(ArrayList<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.message = (TextView)v.findViewById(R.id.message);
        holder.content = (LinearLayout)v.findViewById(R.id.content);
        holder.messageBackground = (LinearLayout)v.findViewById(R.id.messageBackground);
        holder.date = (TextView)v.findViewById(R.id.date);
        return holder;
    }

    private static class ViewHolder {
        public TextView message;
        public TextView date;
        public LinearLayout content;
        public LinearLayout messageBackground;
    }
}
