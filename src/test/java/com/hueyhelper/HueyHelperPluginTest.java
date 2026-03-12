package com.hueyhelper;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class HueyHelperPluginTest
{
	public static void main(String[] args) throws Exception
	{
		// Load your plugin
		ExternalPluginManager.loadBuiltin(HueyHelperPlugin.class);

		// Launch RuneLite
		RuneLite.main(args);
	}
}