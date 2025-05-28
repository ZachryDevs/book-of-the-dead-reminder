package com.bodReminder;

import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.InfoBox;

import java.awt.*;

public class BodReminderInfobox  extends InfoBox {

    public BodReminderInfobox(Plugin plugin) {
        super(null, plugin);
    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public Color getTextColor() {
        return null;
    }

    @Override
    public String getTooltip() {
        return "You forgot your Book of the Dead!";
    }

}
