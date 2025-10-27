package com.stfalcon.chatkit.messages;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class MessageHolders {

    private Class<?> incomingClass;
    private int incomingLayout;
    private Class<?> outcomingClass;
    private int outcomingLayout;

    public MessageHolders setIncomingTextConfig(Class<?> incomingClass, int layout) {
        this.incomingClass = incomingClass;
        this.incomingLayout = layout;
        return this;
    }

    public MessageHolders setOutcomingTextConfig(Class<?> outcomingClass, int layout) {
        this.outcomingClass = outcomingClass;
        this.outcomingLayout = layout;
        return this;
    }

    public Class<?> getIncomingClass() {
        return incomingClass;
    }

    public int getIncomingLayout() {
        return incomingLayout;
    }

    public Class<?> getOutcomingClass() {
        return outcomingClass;
    }

    public int getOutcomingLayout() {
        return outcomingLayout;
    }

    public static abstract class IncomingTextMessageViewHolder<T> extends RecyclerView.ViewHolder {
        protected TextView text;

        public IncomingTextMessageViewHolder(View itemView, Object payload) {
            super(itemView);
            try {
                this.text = itemView.findViewById(android.R.id.text1);
            } catch (Exception ex) {
                this.text = null;
            }
        }

        public void onBind(T message) {
        }
    }

    public static abstract class OutcomingTextMessageViewHolder<T> extends RecyclerView.ViewHolder {
        protected TextView text;

        public OutcomingTextMessageViewHolder(View itemView, Object payload) {
            super(itemView);
            try {
                this.text = itemView.findViewById(android.R.id.text1);
            } catch (Exception ex) {
                this.text = null;
            }
        }

        public void onBind(T message) {
        }
    }
}
