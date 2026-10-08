package dev.thirdpersonzoom.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import dev.thirdpersonzoom.config.ZoomConfigScreen;

/** Adds the options screen to Mod Menu. Only loaded when Mod Menu is installed. */
public class ModMenuCompat implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ZoomConfigScreen::new;
	}
}
