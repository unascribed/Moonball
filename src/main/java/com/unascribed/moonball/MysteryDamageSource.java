package com.unascribed.moonball;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.Text;

public class MysteryDamageSource extends DamageSource {

	public static final MysteryDamageSource INSTANCE = new MysteryDamageSource();
	
	public MysteryDamageSource() {
		super("moonball.mystery2");
		setBypassesArmor();
		setUnblockable();
		setOutOfWorld();
	}
	
	@Override
	public Text getDeathMessage(LivingEntity entity) {
		return entity.getDisplayName().copy().append(Moonball.squiggle("UbxhxFB7NbSaldciN6RbmxX9ccgVdw=="));
	}
	
}
