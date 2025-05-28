package com.bodReminder;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.inject.Inject;
import java.awt.*;

public class BodReminderOverlay extends OverlayPanel {

    private final BodReminderConfig config;
    private final Client client;

    private final String LONG_TEXT = "You need to retrieve your Book of the Dead!";
    private final String SHORT_TEXT = "Book of the Dead";

    @Inject
    private BodReminderOverlay(Client client, BodReminderConfig config) {
        this.client = client;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();

        switch (config.reminderStyle()) {
            case LONG_TEXT:
                panelComponent.getChildren().add((LineComponent.builder())
                        .left(LONG_TEXT)
                        .build());

                panelComponent.setPreferredSize(new Dimension(graphics.getFontMetrics().stringWidth(LONG_TEXT) - 20, 0));
                break;
            case SHORT_TEXT:
                panelComponent.getChildren().add((LineComponent.builder())
                        .left(SHORT_TEXT)
                        .build());
                panelComponent.setPreferredSize(new Dimension(graphics.getFontMetrics().stringWidth(SHORT_TEXT) + 10, 0));
                break;
            case CUSTOM_TEXT:
                panelComponent.getChildren().add((LineComponent.builder())
                        .left(config.customText())
                        .build());
                break;
        }

        if (config.shouldFlash()) {
            if (client.getGameCycle() % 40 >= 20)
            {
                panelComponent.setBackgroundColor(config.flashColor1());
            } else
            {
                panelComponent.setBackgroundColor(config.flashColor2());
            }
        } else {
            panelComponent.setBackgroundColor(config.flashColor1());
        }

        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);

        if (config.reminderStyle() == BodReminderStyle.CUSTOM_TEXT) {
            return super.render(graphics);
        } else {
            return panelComponent.render(graphics);
        }
    }

}
