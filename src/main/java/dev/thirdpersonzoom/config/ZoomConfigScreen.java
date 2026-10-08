package dev.thirdpersonzoom.config;

import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import dev.thirdpersonzoom.ZoomState;

/**
 * Options screen built only from vanilla layouts and buttons, so it needs no config library.
 * Numbers use "-" / "+" buttons; toggles flip when clicked.
 */
public class ZoomConfigScreen extends Screen {
	private static final int LABEL_WIDTH = 200;
	private static final int SMALL = 20;

	private final Screen parent;
	private final ZoomConfig config = ZoomConfig.get();
	private HeaderAndFooterLayout layout;

	public ZoomConfigScreen(Screen parent) {
		super(Component.translatable("thirdpersonzoom.config.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		this.layout = new HeaderAndFooterLayout(this);
		this.layout.addTitleHeader(this.title, this.font);

		LinearLayout body = LinearLayout.vertical().spacing(4);

		body.addChild(numberRow(
				"thirdpersonzoom.config.default_distance",
				() -> config.defaultDistance,
				v -> config.defaultDistance = clamp(v, ZoomConfig.MIN_DISTANCE, config.maxDistance),
				v -> v < 10 ? 0.5 : v < 32 ? 2 : 8,
				v -> String.format("%.1f", v)
		));
		body.addChild(numberRow(
				"thirdpersonzoom.config.max_distance",
				() -> config.maxDistance,
				v -> {
					config.maxDistance = clamp(v, 8, ZoomConfig.MAX_DISTANCE_CEILING);
					config.defaultDistance = Math.min(config.defaultDistance, config.maxDistance);
					return config.maxDistance;
				},
				v -> v < 64 ? 8 : v < 128 ? 16 : 32,
				v -> String.format("%.0f", v)
		));
		body.addChild(numberRow(
				"thirdpersonzoom.config.scroll_step",
				() -> config.scrollStepPercent,
				v -> config.scrollStepPercent = clamp(v, 1, 50),
				v -> 1,
				v -> String.format("%.0f%%", v)
		));

		body.addChild(toggle("thirdpersonzoom.config.smooth_zoom",
				() -> config.smoothZoom, b -> config.smoothZoom = b));
		body.addChild(toggle("thirdpersonzoom.config.camera_collision",
				() -> config.cameraCollision, b -> config.cameraCollision = b));
		body.addChild(toggle("thirdpersonzoom.config.reset_on_perspective",
				() -> config.resetOnPerspectiveChange, b -> config.resetOnPerspectiveChange = b));
		body.addChild(toggle("thirdpersonzoom.config.invert_scroll",
				() -> config.invertScroll, b -> config.invertScroll = b));

		this.layout.addToContents(body);
		this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, (_) -> this.onClose()).width(200).build());
		this.layout.visitWidgets(this::addRenderableWidget);
		this.repositionElements();
	}

	protected void repositionElements() {
		this.layout.arrangeElements();
	}

	@Override
	public void onClose() {
		ZoomConfig.save();
		ZoomState.reset();
		this.minecraft.gui.setScreen(this.parent);
	}

	// ---- row builders ----

	private LinearLayout numberRow(String key, DoubleSupplier getter, DoubleUnaryOperator setter,
			DoubleUnaryOperator stepFor, Function<Double, String> format) {
		LinearLayout row = LinearLayout.horizontal().spacing(4);

		Button label = Button.builder(numberLabel(key, getter.getAsDouble(), format), (_) -> { })
				.width(LABEL_WIDTH).build();
		label.active = false;

		Button minus = Button.builder(Component.literal("-"), (_) -> {
			double v = getter.getAsDouble();
			setter.applyAsDouble(v - stepFor.applyAsDouble(v - 0.0001));
			label.setMessage(numberLabel(key, getter.getAsDouble(), format));
		}).width(SMALL).build();

		Button plus = Button.builder(Component.literal("+"), (_) -> {
			double v = getter.getAsDouble();
			setter.applyAsDouble(v + stepFor.applyAsDouble(v));
			label.setMessage(numberLabel(key, getter.getAsDouble(), format));
		}).width(SMALL).build();

		row.addChild(minus);
		row.addChild(label);
		row.addChild(plus);
		return row;
	}

	private Button toggle(String key, java.util.function.BooleanSupplier getter, java.util.function.Consumer<Boolean> setter) {
		return Button.builder(toggleLabel(key, getter.getAsBoolean()), button -> {
			setter.accept(!getter.getAsBoolean());
			button.setMessage(toggleLabel(key, getter.getAsBoolean()));
		}).width(LABEL_WIDTH + 2 * SMALL + 8).build();
	}

	private static Component numberLabel(String key, double value, Function<Double, String> format) {
		return Component.translatable(key, format.apply(value));
	}

	private static Component toggleLabel(String key, boolean value) {
		return Component.translatable(key).append(": ").append(value ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
	}

	private static double clamp(double v, double min, double max) {
		return Math.max(min, Math.min(max, v));
	}
}
