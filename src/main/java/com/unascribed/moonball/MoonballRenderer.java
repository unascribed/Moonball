package com.unascribed.moonball;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

public class MoonballRenderer extends EntityRenderer<MoonballEntity> {

	private static final Identifier MOONBALL_BASE = new Identifier("moonball", "textures/entity/moonball_base.png");
	private static final Identifier MOONBALL_OVERLAY = new Identifier("moonball", "textures/entity/moonball_overlay.png");
	
	protected MoonballRenderer(EntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}
	
	@Override
	public void render(MoonballEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		int color = Moonball.MOONBALL_ITEM.getColor(entity.getStack());
		int r = (color >> 16)&0xFF;
		int g = (color >>  8)&0xFF;
		int b = (color >>  0)&0xFF;
		int a = 255;
		
		int overlay = OverlayTexture.getUv(0, false);
		
		
		Box bb = entity.getBoundingBox();
		
		float s = (float)Math.max(Math.max(bb.getXLength(), bb.getYLength()), bb.getZLength());
		s /= 2;
		
		matrices.push();
		matrices.translate(0, s, 0);
		matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(yaw));
		matrices.scale(s, s, s);
		cube(matrices, light, r, g, b, a, overlay, vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(MOONBALL_BASE)));
		cube(matrices, light, 255, 255, 255, 255, overlay, vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(MOONBALL_OVERLAY)));
		matrices.pop();
		
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
	}

	private void cube(MatrixStack matrices, int light, int r, int g, int b, int a, int overlay, VertexConsumer vc) {
		Matrix4f model = matrices.peek().getModel();
		Matrix3f normal = matrices.peek().getNormal();
		
		vc.vertex(model, -1, -1, -1).color(r, g, b, a).texture(0, 0).overlay(overlay).light(light).normal(normal, -1,  0,  0).next();
		vc.vertex(model, -1,  1, -1).color(r, g, b, a).texture(1, 0).overlay(overlay).light(light).normal(normal, -1,  0,  0).next();
		vc.vertex(model, -1,  1,  1).color(r, g, b, a).texture(1, 1).overlay(overlay).light(light).normal(normal, -1,  0,  0).next();
		vc.vertex(model, -1, -1,  1).color(r, g, b, a).texture(0, 1).overlay(overlay).light(light).normal(normal, -1,  0,  0).next();
		
		vc.vertex(model,  1, -1, -1).color(r, g, b, a).texture(0, 0).overlay(overlay).light(light).normal(normal, -1,  0,  0).next();
		vc.vertex(model,  1,  1, -1).color(r, g, b, a).texture(1, 0).overlay(overlay).light(light).normal(normal, -1,  0,  0).next();
		vc.vertex(model,  1,  1,  1).color(r, g, b, a).texture(1, 1).overlay(overlay).light(light).normal(normal, -1,  0,  0).next();
		vc.vertex(model,  1, -1,  1).color(r, g, b, a).texture(0, 1).overlay(overlay).light(light).normal(normal, -1,  0,  0).next();
		
		vc.vertex(model, -1, -1, -1).color(r, g, b, a).texture(0, 0).overlay(overlay).light(light).normal(normal,  0, -1,  0).next();
		vc.vertex(model,  1, -1, -1).color(r, g, b, a).texture(1, 0).overlay(overlay).light(light).normal(normal,  0, -1,  0).next();
		vc.vertex(model,  1, -1,  1).color(r, g, b, a).texture(1, 1).overlay(overlay).light(light).normal(normal,  0, -1,  0).next();
		vc.vertex(model, -1, -1,  1).color(r, g, b, a).texture(0, 1).overlay(overlay).light(light).normal(normal,  0, -1,  0).next();
		
		vc.vertex(model, -1,  1, -1).color(r, g, b, a).texture(0, 0).overlay(overlay).light(light).normal(normal,  0,  1,  0).next();
		vc.vertex(model,  1,  1, -1).color(r, g, b, a).texture(1, 0).overlay(overlay).light(light).normal(normal,  0,  1,  0).next();
		vc.vertex(model,  1,  1,  1).color(r, g, b, a).texture(1, 1).overlay(overlay).light(light).normal(normal,  0,  1,  0).next();
		vc.vertex(model, -1,  1,  1).color(r, g, b, a).texture(0, 1).overlay(overlay).light(light).normal(normal,  0,  1,  0).next();
		
		vc.vertex(model, -1, -1, -1).color(r, g, b, a).texture(0, 0).overlay(overlay).light(light).normal(normal,  0,  0, -1).next();
		vc.vertex(model,  1, -1, -1).color(r, g, b, a).texture(1, 0).overlay(overlay).light(light).normal(normal,  0,  0, -1).next();
		vc.vertex(model,  1,  1, -1).color(r, g, b, a).texture(1, 1).overlay(overlay).light(light).normal(normal,  0,  0, -1).next();
		vc.vertex(model, -1,  1, -1).color(r, g, b, a).texture(0, 1).overlay(overlay).light(light).normal(normal,  0,  0, -1).next();
		
		vc.vertex(model, -1, -1,  1).color(r, g, b, a).texture(0, 0).overlay(overlay).light(light).normal(normal,  0,  0,  1).next();
		vc.vertex(model,  1, -1,  1).color(r, g, b, a).texture(1, 0).overlay(overlay).light(light).normal(normal,  0,  0,  1).next();
		vc.vertex(model,  1,  1,  1).color(r, g, b, a).texture(1, 1).overlay(overlay).light(light).normal(normal,  0,  0,  1).next();
		vc.vertex(model, -1,  1,  1).color(r, g, b, a).texture(0, 1).overlay(overlay).light(light).normal(normal,  0,  0,  1).next();
	}
	
	@Override
	public Identifier getTexture(MoonballEntity entity) {
		return null;
	}

}
