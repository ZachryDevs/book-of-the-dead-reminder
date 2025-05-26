package com.bodReminder;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("bodReminder")
public interface BodReminderConfig extends Config
{
	String GROUP = "bodReminder";
	@ConfigItem(
		keyName = "ShouldNotify",
		name = "Notify when the Book of the Dead is forgotten",
		description = "Sends a notification when the runes for summoning thralls are in the inventory but the book of the dead is not.",
			position = 1
	)
	default boolean ShouldNotify()
	{
		return true;
	}
	@ConfigItem(
			keyName = "reminderTimeoutSeconds",
			name = "Timeout for notification window",
			description = "The durations in seconds before the reminder disappears",
			position = 2
	)
	default int reminderTimeoutSeconds()
	{
		return 120;
	}
}
