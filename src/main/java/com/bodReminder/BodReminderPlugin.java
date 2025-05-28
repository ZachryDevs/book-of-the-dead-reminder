package com.bodReminder;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.runepouch.RunepouchPlugin;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@PluginDescriptor(
	name = "Book of the Dead Reminder"
)
public class BodReminderPlugin extends Plugin
{
	private boolean hasFireRunes;
	private boolean hasBloodRunes;
	private boolean hasCosmicRunes;
	private boolean hasBOTD;

	private BodReminderInfobox infobox;
	private Pattern reminderRegex;
	private Pattern hiderRegex;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private Notifier notifier;

//	@Inject
//	private BodHelperOverlay overlay;

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
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Started Plugin", null);
		}
	}

	@Provides
	BodReminderConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BodReminderConfig.class);
	}

	private static final Map<Integer, Integer> runeReqMap;
	static {
        runeReqMap = Map.of(554, 10, 564, 1, 565, 5);
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		// Check if the closed widget belongs to the bank interface
		if (event.getGroupId() == InterfaceID.BANKMAIN) {
			if (hasBloodRunes && hasCosmicRunes && hasFireRunes) {
				if (!hasBOTD) {
					System.out.println("Throw alert here for forgetting BOTD");
					client.addChatMessage(ChatMessageType.GAMEMESSAGE,"", "Congrats you forgot your BOTD", null);
				} else {
					System.out.println("No need to throw alert, BOTD remembered!");
					client.addChatMessage(ChatMessageType.GAMEMESSAGE,"", "Congrats you remembered your BOTD", null);
				}
			} else {
				System.out.println("No need to throw alert, Runes not fetched");
			}

		}
	}


	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged itemContainerChanged) {
		System.out.println("ItemContainerChanged: " + itemContainerChanged.getContainerId());
		ItemContainer itemContainer = itemContainerChanged.getItemContainer();
		// check 
		if (itemContainer.getId() == InterfaceID.INVENTORY || itemContainer.getId() == InterfaceID.RunePouch.INVENTORY) {
			for (Item item : itemContainer.getItems()) {
				String name;
				switch (item.getId()) {
					case ItemID.FIRERUNE:
						System.out.println("Fire rune quantity changed to: " + item.getQuantity());
                        hasFireRunes = item.getQuantity() == runeReqMap.get(ItemID.FIRERUNE);
						name = "Fire";
						break;
					case ItemID.BLOODRUNE:
						System.out.println("Blood rune quantity changed to: " + item.getQuantity());
						hasBloodRunes = item.getQuantity() == runeReqMap.get(ItemID.BLOODRUNE);
						name = "Blood";
						break;
					case ItemID.COSMICRUNE:
						System.out.println("Cosmic rune quantity changed to: " + item.getQuantity());
						hasCosmicRunes = item.getQuantity() == runeReqMap.get(ItemID.COSMICRUNE);
						name = "Cosmic";
						break;
					case ItemID.BOOK_OF_THE_DEAD:
						System.out.println("Book of the dead quantity changed to: " + item.getQuantity());
						hasBOTD = item.getQuantity() == 1;
						name = "book";
						break;
					default:
						System.out.println("Item ID of non-rune: " + item.getId());
						name = "unknown";
				}

				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",  name + " rune quantity changed to: " + item.getQuantity(), null);
			}
		}
		// Rune pouch ID: 27281
		// soul rune ID: 566
		// fire rune ID: 554
		// blood rune ID: 565
		// cosmic rune ID: 564

	}
}
