package com.unascribed.moonball.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.moonball.Moonball;
import com.unascribed.moonball.MoonballEntity;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
	@Shadow private ClientWorld world;

	// Set the staged entity type
	// Stolen from https://github.com/Queerbric/queerbrics/blob/master/src/main/java/io/github/queerbric/queerbrics/mixin/ClientPlayNetworkHandlerMixin.java
	@Inject(method = "onEntitySpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/EntitySpawnS2CPacket;getX()D"))
	private void handleEntitySpawn(EntitySpawnS2CPacket pkt, CallbackInfo ci) {
		Entity toSpawn = null;
		double x = pkt.getX();
		double y = pkt.getY();
		double z = pkt.getZ();
		if (pkt.getEntityTypeId() == Moonball.MOONBALL_ENTITY) {
			toSpawn = new MoonballEntity(this.world, pkt.getX(), pkt.getY(), pkt.getZ(), ItemStack.EMPTY);
		}

		if (toSpawn != null) {
			// Copied from end of ClientPlayNetworkHandler#onEntitySpawn
			// TODO: someday we'll be able to just add on to the if-chain, or have proper API
			toSpawn.updateTrackedPosition(x, y, z);
			toSpawn.refreshPositionAfterTeleport(x, y, z);
			toSpawn.pitch = pkt.getPitch() * 360 / 256.0F;
			toSpawn.yaw = pkt.getYaw() * 360 / 256.0F;
			toSpawn.setEntityId(pkt.getId());
			toSpawn.setUuid(pkt.getUuid());
			this.world.addEntity(pkt.getId(), toSpawn);
		}
	}
}
