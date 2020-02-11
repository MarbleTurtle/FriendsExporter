package com.FriendsExporter;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class FriendsExporterPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(FriendsExporterPlugin.class);
		RuneLite.main(args);
	}
}