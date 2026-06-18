package com.vaultsentinel;

import net.fabricmc.api.ModInitializer;

public class VaultSentinel implements ModInitializer {
	public static final String MOD_ID = "vaultsentinel";

	@Override
	public void onInitialize() {
		// No server-side logic. Everything this mod does (raycasting, reading the
		// vault's client-synced display item, playing a cue) is client-only,
		// since it's just a heads-up assist, not anything that changes game state.
	}
}
