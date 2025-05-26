package com.bodReminder;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

import java.util.regex.Pattern;

@Slf4j
@PluginDescriptor(
	name = "Book of the Dead Reminder"
)
public class BodReminderPlugin extends Plugin
{
	private BodReminderInfobox infobox;
	private Pattern reminderRegex;
	private Pattern hiderRegex;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private Notifier notifier;

	@Inject
	private BodHelperOverlay overlay;

	@Inject
	private Client client;

	@Inject
	private BodReminderConfig config;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Book of the Dead Reminder started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Book of the Dead Reminder stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
	}

	@Provides
	BodReminderConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BodReminderConfig.class);
	}
}
