package com.FriendsExporter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("friendsexporter")
public interface FriendsExporterConfig extends Config
{
	@ConfigItem(
			keyName = "lineStart",
			name = "New entry",
			description = "What to add before each player.",
			position = 1
	)
	default LineLeads Lineleads() { return LineLeads.None;}
	@ConfigItem(
		keyName = "separator",
		name = "Name Separator",
		description = "Separator between current and previous names.",
		position = 2
	)
	default String Separator()
	{
		return "-";
	}
	@ConfigItem(
			keyName = "unrank",
			name = "Show unranked players",
			description = "Shows players that do not have a rank but are still friends in ranks export.",
			position = 3
	)
	default boolean showUnranked()
	{
		return false;
	}
	@ConfigItem(
			keyName = "line",
			name = "Separate lines",
			description = "Separates entrys with new lines.",
			position = 4
	)
	default boolean newLine()	{return false;}
}
