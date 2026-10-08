package dev.thirdpersonzoom.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;

import dev.thirdpersonzoom.ThirdPersonZoom;
import dev.thirdpersonzoom.ZoomState;

/** While the zoom key is held in third person, the scroll wheel zooms instead of changing hotbar slot. */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void thirdpersonzoom$onScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
		if (vertical != 0.0 && ThirdPersonZoom.isZoomScrollActive(this.minecraft)) {
			ZoomState.onScroll(vertical);
			ci.cancel();
		}
	}
}
