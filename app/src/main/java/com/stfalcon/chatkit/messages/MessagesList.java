package com.stfalcon.chatkit.messages;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MessagesList extends FrameLayout {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    public MessagesList(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MessagesList(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MessagesList(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        recyclerView = new RecyclerView(context);
        recyclerView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        addView(recyclerView);
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        this.adapter = adapter;
        recyclerView.setAdapter(adapter);
        if (adapter instanceof com.stfalcon.chatkit.messages.MessagesListAdapter) {
            ((com.stfalcon.chatkit.messages.MessagesListAdapter) adapter).setRecyclerView(recyclerView);
        }
    }

    public RecyclerView.Adapter getAdapter() {
        return adapter;
    }
}
