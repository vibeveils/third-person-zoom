package dev.thirdpersonzoom;

import dev.thirdpersonzoom.config.ZoomConfig;

/** Holds the current and target camera distance. */
public final class ZoomState {
	/** Distance the player asked for. */
	private static double target = ZoomConfig.VANILLA_DISTANCE;
	/** Distance actually used this frame (eases toward target when smooth zoom is on). */
	private static double current = ZoomConfig.VANILLA_DISTANCE;
	private static long lastFrameNanos = -1L;

	private ZoomState() {
	}

	public static void reset() {
		target = ZoomConfig.get().defaultDistance;
		current = target;
	}

	/** Applies one scroll event. Positive amount = scroll up = zoom in (unless inverted). */
	public static void onScroll(double amount) {
		ZoomConfig config = ZoomConfig.get();
		if (config.invertScroll) {
			amount = -amount;
		}

		// Multiplicative steps feel the same whether you're 3 or 300 blocks out.
		double factor = 1.0 + config.scrollStepPercent / 100.0;
		target = target * Math.pow(factor, -amount);
		target = Math.max(ZoomConfig.MIN_DISTANCE, Math.min(config.maxDistance, target));

		if (!config.smoothZoom) {
			current = target;
		}
	}

	/** Called once per rendered frame from the camera mixin. */
	public static double getDistanceForFrame() {
		ZoomConfig config = ZoomConfig.get();
		// The max may have been lowered in the options screen.
		target = Math.min(target, config.maxDistance);

		long now = System.nanoTime();
		double dt = lastFrameNanos < 0 ? 0.0 : Math.min(0.25, (now - lastFrameNanos) / 1.0e9);
		lastFrameNanos = now;

		if (config.smoothZoom) {
			// Frame-rate independent exponential ease (~95% of the way in 0.25 s).
			double alpha = 1.0 - Math.exp(-12.0 * dt);
			current += (target - current) * alpha;
			if (Math.abs(target - current) < 0.001) {
				current = target;
			}
		} else {
			current = target;
		}

		return current;
	}
}
