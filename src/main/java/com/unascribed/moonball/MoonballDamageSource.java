package com.unascribed.moonball;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class MoonballDamageSource extends ProjectileDamageSource {
	
	public MoonballDamageSource(String name, MoonballEntity projectile, Entity attacker) {
		super(name, projectile, attacker);
	}
	
	@Override
	public Text getDeathMessage(LivingEntity entity) {
		if (entity == getAttacker() || getAttacker() == null) {
			return new TranslatableText("death.attack.moonball.self", entity.getDisplayName());
		}
		Text originator = (getAttacker() == null) ? source.getDisplayName() : getAttacker().getDisplayName();
		ItemStack stack = ((MoonballEntity)getSource()).getStack();
		String key = "death.attack.moonball";
		String itemKey = key + ".item";
		if (!stack.isEmpty() && stack.hasCustomName()) {
			return new TranslatableText(itemKey, entity.getDisplayName(), originator, stack.toHoverableText());
		}
		return new TranslatableText(key, entity.getDisplayName(), originator);
	}

}
