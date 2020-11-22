package com.unascribed.moonball.mixin;

import java.io.IOException;
import java.io.InputStream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.moonball.Moonball;

import net.minecraft.resource.AbstractFileResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

@Mixin(AbstractFileResourcePack.class)
public class MixinAbstractFileResourcePack {
	
	@Inject(at=@At("HEAD"), method="open(Lnet/minecraft/resource/ResourceType;Lnet/minecraft/util/Identifier;)Ljava/io/InputStream;", cancellable=true)
	public void open(ResourceType type, Identifier id, CallbackInfoReturnable<InputStream> ci) throws IOException {
		if (id.getNamespace().equals("moonball") && (id.getPath().endsWith("mystery.png") || id.getPath().endsWith("mystery2.png"))) {
			AbstractFileResourcePack afrp = (AbstractFileResourcePack)(Object)this;
			ci.setReturnValue(Moonball.squiggle(afrp.open(type, new Identifier("moonball", id.getPath().replace(".png", ".dat")))));
		}
	}
	
}
