package dev.thirdpersonzoom.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import dev.thirdpersonzoom.ZoomState;
import dev.thirdpersonzoom.config.ZoomConfig;

/**
 * In third person, vanilla pulls the camera back with {@code move(-distance, 0, 0)} inside
 * {@code Camera.alignWithEntity}. We replace that distance with our own (and do our own
 * wall-collision check so it works at any range).
 */
@Mixin(Camera.class)
public abstract class CameraMixin {
	@Shadow
	private Level level;

	@Shadow
	private Entity entity;

	@Shadow
	private @Final Vector3f forwards;

	@WrapOperation(
			method = "alignWithEntity",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;move(FFF)V", ordinal = 0)
	)
	private void thirdpersonzoom$applyZoom(Camera camera, float zoom, float dy, float dx, Operation<Void> original) {
		// Only touch the third-person pull-back (negative zoom); leave anything else alone.
		if (zoom >= 0.0F || this.entity == null || this.level == null) {
			original.call(camera, zoom, dy, dx);
			return;
		}

		double distance = ZoomState.getDistanceForFrame() * thirdpersonzoom$scaleFor(this.entity);

		if (ZoomConfig.get().cameraCollision) {
			distance = thirdpersonzoom$clipDistance(distance);
		}

		original.call(camera, (float) -distance, dy, dx);
	}

	/** Respect entity scale and any camera-distance attribute changes the game applies. */
	@Unique
	private static double thirdpersonzoom$scaleFor(Entity entity) {
		if (entity instanceof LivingEntity living) {
			double attributeRatio = living.getAttributeValue(Attributes.CAMERA_DISTANCE) / ZoomConfig.VANILLA_DISTANCE;
			if (!(attributeRatio > 0.0)) {
				attributeRatio = 1.0;
			}
			return living.getScale() * attributeRatio;
		}
		return 1.0;
	}

	/** Same idea as vanilla: cast 8 slightly offset rays backwards and stop at the nearest block. */
	@Unique
	private double thirdpersonzoom$clipDistance(double distance) {
		Vec3 eye = ((Camera) (Object) this).position();
		double bx = -this.forwards.x() * distance;
		double by = -this.forwards.y() * distance;
		double bz = -this.forwards.z() * distance;

		for (int i = 0; i < 8; i++) {
			double ox = ((i & 1) * 2 - 1) * 0.1;
			double oy = ((i >> 1 & 1) * 2 - 1) * 0.1;
			double oz = ((i >> 2 & 1) * 2 - 1) * 0.1;

			Vec3 from = eye.add(ox, oy, oz);
			Vec3 to = from.add(bx, by, bz);

			BlockHitResult hit = this.level.clip(new ClipContext(from, to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, this.entity));
			if (hit.getType() != HitResult.Type.MISS) {
				double hitDistance = hit.getLocation().distanceTo(eye);
				if (hitDistance < distance) {
					distance = hitDistance;
				}
			}
		}

		return distance;
	}
}
