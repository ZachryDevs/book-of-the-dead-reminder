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

class RunePouchContents {
	int fireRunes;
	int bloodRunes;
	int cosmicRunes;

	RunePouchContents(int fireRunes, int bloodRunes, int cosmicRunes) {
		this.fireRunes = fireRunes;
		this.bloodRunes = bloodRunes;
		this.cosmicRunes = cosmicRunes;
	}
}

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

	private static final int INVENTORY_ID = 93;
	private static final int DIVINE_RUNE_POUCH_ID = 27281;

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
        runeReqMap = Map.of(ItemID.FIRERUNE, 10, ItemID.COSMICRUNE, 1, ItemID.BLOODRUNE, 5);
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event) {
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

	private RunePouchContents checkRunePouch(ItemContainer itemContainer) {
		System.out.println("Checking runepouch...");

		for (Item item : itemContainer.getItems()) {
			int itemId = item.getId();
			if (itemId <= 0) continue;

			if (isRunePouchVariant(itemId)) {
				System.out.println("Pouch identified as ID: " + itemId);
				Map<Integer, Integer> pouchContents = getRunePouchContents(itemId);
				if (pouchContents == null) {
					System.out.println("No readable params for item: " + itemId);
					continue;
				}

				int fire = pouchContents.getOrDefault(ItemID.FIRERUNE, 0);
				int blood = pouchContents.getOrDefault(ItemID.BLOODRUNE, 0);
				int cosmic = pouchContents.getOrDefault(ItemID.COSMICRUNE, 0);

				return new RunePouchContents(fire, blood, cosmic);
			} else {
				System.out.println("not a rune pouch");
			}
		}

		return new RunePouchContents(0, 0, 0);
	}

	private Map<Integer, Integer> getRunePouchContents(int itemId) {
		ItemComposition comp = client.getItemDefinition(itemId);
		IterableHashTable<Node> rawParams = comp.getParams();

		if (rawParams == null) return null;

		Map<Integer, Integer> runeMap = new HashMap<>();

		for (Node node : rawParams) {
			if (node instanceof IntegerNode) {
				int key = (int) node.getHash();
				int value = ((IntegerNode) node).getValue();

				runeMap.put(key, value);
			}
		}

		Map<Integer, Integer> extractedRunes = new HashMap<>();
		for (int i = 0; i < 4; i++) {
			int runeIdKey = 1 + i * 2;
			int amountKey = runeIdKey + 1;

			if (runeMap.containsKey(runeIdKey) && runeMap.containsKey(amountKey)) {
				int runeId = runeMap.get(runeIdKey);
				int amount = runeMap.get(amountKey);

				if (runeId > 0 && amount > 0) {
					extractedRunes.put(runeId, amount);
				}
			}
		}

		return extractedRunes;
	}



	private boolean isRunePouchVariant(int itemId) {
		System.out.println("Checking if id is runepouch: " + itemId);
		// 12791 - rune pouch normal
		// 24416 - rune pouch locked
		// 27281 - Divine rune pouch
		// 27509 - Divine runep ouch locked
		// 30692 - rune pouch (Castle wars) - Not for evaluation just record
		if (itemId == 12791 || itemId == 24416 || itemId == 27281 || itemId == 27509) {
			return true;
		}
		return false;
	}




	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged itemContainerChanged) {
		ItemContainer itemContainer = itemContainerChanged.getItemContainer();
		if (itemContainer.getId() == INVENTORY_ID) {
			int fireRunesInPouch = 0, bloodRunesInPouch = 0, cosmicRunesInPouch = 0;
			int fireRunesInInv = 0, bloodRunesInInv = 0, cosmicRunesInInv = 0;

			 RunePouchContents runePouchContents = checkRunePouch(itemContainer);
			 fireRunesInPouch += runePouchContents.fireRunes;
			 bloodRunesInPouch += runePouchContents.bloodRunes;
			 cosmicRunesInPouch += runePouchContents.cosmicRunes;

			System.out.println("Fire Runes from runepouch: " + fireRunesInPouch);
			System.out.println("Blood Runes from pouch: " + bloodRunesInPouch);
			System.out.println("Cosmic Runes from pouch: " + cosmicRunesInPouch);

			if (itemContainer.contains(ItemID.FIRERUNE)) {
				fireRunesInInv += itemContainer.count(ItemID.FIRERUNE);
			}
			if (itemContainer.contains(ItemID.BLOODRUNE)) {
				bloodRunesInInv += itemContainer.count(ItemID.BLOODRUNE);
			}
			if (itemContainer.contains(ItemID.COSMICRUNE)) {
				cosmicRunesInInv += itemContainer.count(ItemID.COSMICRUNE);
			}

			hasFireRunes = fireRunesInInv + fireRunesInPouch >= runeReqMap.get(ItemID.FIRERUNE);
			hasBloodRunes = bloodRunesInInv + bloodRunesInPouch >= runeReqMap.get(ItemID.BLOODRUNE);
			hasCosmicRunes = cosmicRunesInInv + cosmicRunesInPouch >= runeReqMap.get(ItemID.COSMICRUNE);
			hasBOTD = itemContainer.count(ItemID.BOOK_OF_THE_DEAD) >= 1;

			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", " Fire Rune quantity changed to: " + (fireRunesInInv + fireRunesInPouch), null);
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", " Blood Rune quantity changed to: " + (bloodRunesInInv + bloodRunesInPouch), null);
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", " Cosmic Rune quantity changed to: " + (cosmicRunesInInv + cosmicRunesInPouch), null);
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", " Book of the dead quantity changed to: " + itemContainer.count(ItemID.BOOK_OF_THE_DEAD), null);

		}
		// Rune pouch ID: 27281
		// soul rune ID: 566
		// fire rune ID: 554
		// blood rune ID: 565
		// cosmic rune ID: 564

	}
}
