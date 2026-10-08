package dev.thirdpersonzoom.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

import dev.thirdpersonzoom.ThirdPersonZoom;

/** Simple JSON config stored at config/thirdpersonzoom.json. */
public final class ZoomConfig {
	public static final double MIN_DISTANCE = 1.0;
	/** Hard ceiling for the "max distance" setting. */
	public static final double MAX_DISTANCE_CEILING = 512.0;
	/** Vanilla third-person distance, in blocks. */
	public static final double VANILLA_DISTANCE = 4.0;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("thirdpersonzoom.json");

	private static ZoomConfig instance = new ZoomConfig();

	// ---- settings ----
	/** Distance the camera starts at when entering third person (blocks). */
	public double defaultDistance = VANILLA_DISTANCE;
	/** Furthest you can scroll the camera out (blocks). */
	public double maxDistance = 128.0;
	/** How much each scroll notch changes the distance, as a percentage of the current distance. */
	public double scrollStepPercent = 12.0;
	/** Ease toward the target distance instead of jumping. */
	public boolean smoothZoom = true;
	/** Pull the camera in when a block is between it and the player (vanilla behaviour). */
	public boolean cameraCollision = true;
	/** Go back to the default distance every time you enter third person. */
	public boolean resetOnPerspectiveChange = true;
	/** Swap the scroll direction. */
	public boolean invertScroll = false;

	public static ZoomConfig get() {
		return instance;
	}

	public static void load() {
		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH)) {
				ZoomConfig loaded = GSON.fromJson(reader, ZoomConfig.class);
				if (loaded != null) {
					instance = loaded;
				}
			} catch (Exception e) {
				ThirdPersonZoom.LOGGER.warn("Could not read {}, using defaults", PATH, e);
			}
		}
		instance.sanitize();
		save();
	}

	public static void save() {
		instance.sanitize();
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException e) {
			ThirdPersonZoom.LOGGER.warn("Could not save {}", PATH, e);
		}
	}

	public void sanitize() {
		maxDistance = clamp(maxDistance, 8.0, MAX_DISTANCE_CEILING);
		defaultDistance = clamp(defaultDistance, MIN_DISTANCE, maxDistance);
		scrollStepPercent = clamp(scrollStepPercent, 1.0, 50.0);
	}

	private static double clamp(double v, double min, double max) {
		if (Double.isNaN(v)) {
			return min;
		}
		return Math.max(min, Math.min(max, v));
	}
}
