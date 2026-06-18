package com.vaultsentinel.client;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.VaultBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.block.vault.VaultSharedData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Watches one specific Ominous Vault while two conditions both hold:
 *   1) the player is holding the Ominous Trial Key, and
 *   2) the player's crosshair is on that vault.
 *
 * Pressing the keybind while both conditions hold "arms" the watch.
 * The watch auto-cancels the instant either condition breaks (key swapped
 * out of hand, or the crosshair leaves the vault).
 *
 * While armed, every client tick it reads the vault block entity's
 * synced display item (VaultSharedData#getDisplayItem) and, the moment
 * that item becomes a Heavy Core, fires an action-bar flash + sound cue.
 * It never sends any click or interact packet on your behalf -- you still
 * have to right-click it yourself.
 */
public class SentinelManager {

	private static final double MAX_REACH = 6.0; // matches vanilla block interaction range closely enough

	private boolean watching = false;
	private BlockPos watchedVaultPos = null;
	private boolean lastTickWasHeavyCore = false;

	/**
	 * @param keyJustPressed true on the tick the arm/disarm keybind was pressed
	 */
	public void tick(MinecraftClient client, boolean keyJustPressed) {
		ClientPlayerEntity player = client.player;
		World world = client.world;

		if (player == null || world == null) {
			disarm();
			return;
		}

		boolean holdingOminousKey = isHoldingOminousKey(player);
		BlockPos aimedVault = getAimedOminousVault(client, player);
		boolean conditionsMet = holdingOminousKey && aimedVault != null;

		if (keyJustPressed) {
			if (conditionsMet) {
				// (Re)arm on whichever vault is currently being aimed at.
				watching = true;
				watchedVaultPos = aimedVault;
				lastTickWasHeavyCore = false;
			} else {
				// Pressed it without meeting conditions -> nothing to arm, make sure we're off.
				disarm();
			}
		}

		if (!watching) {
			return;
		}

		// Cancel the watch the instant either condition breaks, or the player
		// stops aiming at the SAME vault they armed on.
		if (!conditionsMet || aimedVault == null || !aimedVault.equals(watchedVaultPos)) {
			disarm();
			return;
		}

		checkDisplayItem(client, world, watchedVaultPos);
	}

	private void checkDisplayItem(MinecraftClient client, World world, BlockPos vaultPos) {
		BlockEntity be = world.getBlockEntity(vaultPos);
		if (!(be instanceof VaultBlockEntity vaultBe)) {
			disarm();
			return;
		}

		VaultSharedData sharedData = vaultBe.getSharedData();
		if (sharedData == null || !sharedData.hasDisplayItem()) {
			lastTickWasHeavyCore = false;
			return;
		}

		ItemStack displayed = sharedData.getDisplayItem();
		boolean isHeavyCoreNow = !displayed.isEmpty() && displayed.getItem() == Items.HEAVY_CORE;

		// Only fire on the rising edge so it doesn't spam every tick the
		// core happens to stay on screen.
		if (isHeavyCoreNow && !lastTickWasHeavyCore) {
			fireCue(client);
		}
		lastTickWasHeavyCore = isHeavyCoreNow;
	}

	private void fireCue(MinecraftClient client) {
		ClientPlayerEntity player = client.player;
		if (player == null) {
			return;
		}

		player.sendMessage(
				Text.literal("HEAVY CORE!").formatted(Formatting.GOLD, Formatting.BOLD),
				true // action bar, not chat
		);

		client.getSoundManager().play(PositionedSoundInstance.master(
				SoundEvents.ENTITY_PLAYER_LEVELUP, 1.4f, 1.0f
		));
	}

	private boolean isHoldingOminousKey(PlayerEntity player) {
		ItemStack held = player.getMainHandStack();
		return !held.isEmpty() && held.getItem() == Items.OMINOUS_TRIAL_KEY;
	}

	/**
	 * Raycasts from the camera and returns the position of the targeted
	 * block only if it is a Vault block in its "ominous" state; otherwise null.
	 */
	private BlockPos getAimedOminousVault(MinecraftClient client, PlayerEntity player) {
		HitResult hit = client.crosshairTarget;
		if (hit == null || hit.getType() != HitResult.Type.BLOCK) {
			return null;
		}
		if (!(hit instanceof BlockHitResult blockHit)) {
			return null;
		}
		if (player.squaredDistanceTo(
				blockHit.getPos().getX() + 0.5,
				blockHit.getPos().getY() + 0.5,
				blockHit.getPos().getZ() + 0.5
		) > MAX_REACH * MAX_REACH) {
			return null;
		}

		BlockPos pos = blockHit.getBlockPos();
		BlockState state = client.world.getBlockState(pos);
		Block block = state.getBlock();

		if (!(block instanceof VaultBlock)) {
			return null;
		}

		// NOTE: if this line fails to compile against your local mappings,
		// open VaultBlock in your IDE and check the exact name of its
		// "ominous" BooleanProperty (it may differ slightly between
		// Yarn builds) and swap it in here.
		boolean isOminous = state.get(VaultBlock.OMINOUS);
		if (!isOminous) {
			return null;
		}

		return pos;
	}

	private void disarm() {
		watching = false;
		watchedVaultPos = null;
		lastTickWasHeavyCore = false;
	}

	public boolean isWatching() {
		return watching;
	}
}
