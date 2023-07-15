package com.FriendsExporter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("friendsexporter")
public interface FriendsExporterConfig extends Config
{
	@ConfigItem(
		keyName = "lineStart",
		name = "Line Start",
		description = "What format to add at the beginning of a line.",
		position = 1
	)
	default LineLeads lineLeads() {return LineLeads.None;}

	@ConfigItem(
		keyName = "prev",
		name = "Include Previous Names",
		description = "Shows the previous name if available.",
		position = 2
	)
	default boolean includePrevName() {return true;}

	@ConfigItem(
		keyName = "note",
		name = "Include Note",
		description = "Includes note from Friend Notes plugin on Friends List export.",
		position = 3
	)
	default boolean includeNote() {return true;}

	@ConfigItem(
		keyName = "unrank",
		name = "Show Unranked Players",
		description = "Shows players that do not have a rank but are still friends in ranks export.",
		position = 4
	)
	default boolean showUnranked() {return false;}

	@ConfigItem(
		keyName = "separator",
		name = "Separator",
		description = "Separator between fields for a player.",
		position = 5
	)
	default String getSeparator() {return " - ";}
}
