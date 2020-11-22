package com.unascribed.moonball;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;

public class MoonballClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		MinecraftClient mc = MinecraftClient.getInstance();
		EntityRendererRegistry.INSTANCE.register(Moonball.MOONBALL_ENTITY, (erd, ctx) -> new MoonballRenderer(erd));
		
		mc.renderTaskQueue.add(() -> {
			mc.itemColors.register((stack, layer) -> {
				if (layer == 0) {
					return Moonball.MOONBALL_ITEM.getColor(stack);
				}
				return -1;
			}, Moonball.MOONBALL_ITEM);
		});
	}

}
