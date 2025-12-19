package site.zvolcan.kuClans.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class MessageUtil {

    public static Component translateColors(String message) {
        if (message.contains("<") && message.contains(">")) {
            return MiniMessage.miniMessage().deserialize(message);
        } else {
            return LegacyComponentSerializer.legacySection().deserialize(message);
        }
    }

}
