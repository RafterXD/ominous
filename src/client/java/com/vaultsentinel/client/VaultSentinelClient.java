package com.vaultsentinel.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Util;

public class VaultSentinelClient implements ClientModInitializer {

	private static KeyBinding ARM_KEY;
	private static final SentinelManager MANAGER = new SentinelManager();

	@Override
	public void onInitializeClient() {
		ARM_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.vaultsentinel.arm",
				InputUtil.Type.KEYSYM,
				InputConstants.KEY_G, // default bind, change freely in Controls menu
				"category.vaultsentinel.main"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			boolean keyJustPressed = ARM_KEY.wasPressed();
			MANAGER.tick(client, keyJustPressed);
		});
	}

	public static SentinelManager getManager() {
		return MANAGER;
	}
}
