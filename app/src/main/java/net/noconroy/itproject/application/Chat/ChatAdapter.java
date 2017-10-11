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
import net.noconroy.itproject.application.models.Message;

import java.util.ArrayList;

/**
 * Created by Mattias on 5/10/2017.
 * Basic functionality for ChatAdapter was sourced from:
 *      https://www.codeproject.com/Tips/897826/Designing-Android-Chat-Bubble-Chat-UI
 */

public class ChatAdapter extends BaseAdapter {

    private ArrayList<Message> chatMessages;
    private Activity activity;

    public ChatAdapter(Activity _activity) {
        activity = _activity;
        chatMessages = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return chatMessages.size();
    }

    @Override
    public Message getItem(int i) {
        return chatMessages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        Message chatMessage = getItem(i);
        LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (view == null) {
            view = vi.inflate(R.layout.activity_chat_message, null);
            holder = createViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder)view.getTag();
        }

        setMessagePositioning(holder, chatMessage.isMe());
        holder.message.setText(ChatHelper.getText(chatMessage));
        holder.date.setText(chatMessage.server_time);

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

    public void Add(Message chatMessage) {
        chatMessages.add(chatMessage);
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
