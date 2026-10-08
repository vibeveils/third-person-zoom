package dev.thirdpersonzoom;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.sdl.SDLScancode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

import dev.thirdpersonzoom.config.ZoomConfig;

public final class ThirdPersonZoom implements ClientModInitializer {
	public static final String MOD_ID = "thirdpersonzoom";
	public static final Logger LOGGER = LoggerFactory.getLogger("Third Person Zoom");

	public static KeyMapping zoomKey;

	private CameraType lastCameraType;

	@Override
	public void onInitializeClient() {
		ZoomConfig.load();
		ZoomState.reset();

		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));
		zoomKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.thirdpersonzoom.zoom",
				InputConstants.Type.KEYBOARD,
				SDLScancode.SDL_SCANCODE_LALT,
				category
		));

		ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
	}

	private void onEndTick(Minecraft client) {
		CameraType current = client.options.getCameraType();

		if (current != lastCameraType) {
			boolean enteringThirdPerson = !current.isFirstPerson()
					&& (lastCameraType == null || lastCameraType.isFirstPerson());

			if (enteringThirdPerson && ZoomConfig.get().resetOnPerspectiveChange) {
				ZoomState.reset();
			}

			lastCameraType = current;
		}
	}

	/** True when the scroll wheel should zoom instead of doing its normal job. */
	public static boolean isZoomScrollActive(Minecraft client) {
		return zoomKey != null
				&& zoomKey.isDown()
				&& client.player != null
				&& client.gui.screen() == null
				&& !client.options.getCameraType().isFirstPerson();
	}
}
