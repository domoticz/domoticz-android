package nl.hnogames.domoticz.helpers;

import android.text.Html;
import android.view.View;
import com.stfalcon.chatkit.messages.MessageHolders;
import nl.hnogames.domoticz.containers.NotificationInfo;

public class CustomOutcomingMessageViewHolder extends MessageHolders.OutcomingTextMessageViewHolder<NotificationInfo> {

    public CustomOutcomingMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
    }

    @Override
    public void onBind(NotificationInfo message) {
        super.onBind(message);
        text.setText(Html.fromHtml("<b>" + message.getTitle() + "</b>" +  "<br />" +
                "<small>"+message.getText() + "</small>"));
    }
}