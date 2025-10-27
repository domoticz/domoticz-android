package com.stfalcon.chatkit.messages;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import nl.hnogames.domoticz.containers.NotificationInfo;

public class MessagesListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private String senderId;
    private MessageHolders holdersConfig;
    private List<NotificationInfo> items = new ArrayList<>();
    private RecyclerView recyclerView;

    private static final int VIEW_TYPE_INCOMING = 1;
    private static final int VIEW_TYPE_OUTCOMING = 2;

    public MessagesListAdapter(String senderId, MessageHolders holdersConfig, Object payload) {
        this.senderId = senderId;
        this.holdersConfig = holdersConfig;
    }

    @Override
    public int getItemViewType(int position) {
        NotificationInfo message = items.get(position);
        String userId = message.getUser() != null ? message.getUser().getId() : null;
        if (userId != null && userId.equals(senderId))
            return VIEW_TYPE_OUTCOMING;
        return VIEW_TYPE_INCOMING;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        try {
            if (viewType == VIEW_TYPE_INCOMING && holdersConfig.getIncomingLayout() != 0) {
                View v = inflater.inflate(holdersConfig.getIncomingLayout(), parent, false);
                Class<?> holderCls = holdersConfig.getIncomingClass();
                if (holderCls != null) {
                    Constructor<?> c = holderCls.getConstructor(View.class, Object.class);
                    return (RecyclerView.ViewHolder) c.newInstance(v, null);
                }
                return new SimpleHolder(v);
            } else if (viewType == VIEW_TYPE_OUTCOMING && holdersConfig.getOutcomingLayout() != 0) {
                View v = inflater.inflate(holdersConfig.getOutcomingLayout(), parent, false);
                Class<?> holderCls = holdersConfig.getOutcomingClass();
                if (holderCls != null) {
                    Constructor<?> c = holderCls.getConstructor(View.class, Object.class);
                    return (RecyclerView.ViewHolder) c.newInstance(v, null);
                }
                return new SimpleHolder(v);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // fallback
        View v = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        return new SimpleHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NotificationInfo message = items.get(position);
        if (holder instanceof com.stfalcon.chatkit.messages.MessageHolders.IncomingTextMessageViewHolder) {
            try {
                ((com.stfalcon.chatkit.messages.MessageHolders.IncomingTextMessageViewHolder) holder).onBind(message);
            } catch (Exception ignored) {
            }
        } else if (holder instanceof com.stfalcon.chatkit.messages.MessageHolders.OutcomingTextMessageViewHolder) {
            try {
                ((com.stfalcon.chatkit.messages.MessageHolders.OutcomingTextMessageViewHolder) holder).onBind(message);
            } catch (Exception ignored) {
            }
        } else if (holder instanceof SimpleHolder) {
            ((SimpleHolder) holder).bind(message.getText());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addToEnd(List<NotificationInfo> messages, boolean scroll) {
        if (messages != null) {
            int pos = items.size();
            this.items.addAll(messages);
            notifyItemRangeInserted(pos, messages.size());
            if (scroll && recyclerView != null) {
                recyclerView.scrollToPosition(items.size() - 1);
            }
        }
    }

    public void addToStart(NotificationInfo message, boolean scroll) {
        if (message != null) {
            this.items.add(0, message);
            notifyItemInserted(0);
            if (scroll && recyclerView != null) {
                recyclerView.scrollToPosition(0);
            }
        }
    }

    public List<NotificationInfo> getItems() {
        return items;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    private static class SimpleHolder extends RecyclerView.ViewHolder {
        public SimpleHolder(@NonNull View itemView) {
            super(itemView);
        }

        void bind(String text) {
            try {
                android.widget.TextView tv = itemView.findViewById(android.R.id.text1);
                tv.setText(text);
            } catch (Exception ignored) {
            }
        }
    }
}
