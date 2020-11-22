package com.unascribed.moonball;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity.PickupPermission;
import net.minecraft.item.DyeItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class MoonballItem extends Item implements DyeableItem {

	public MoonballItem(Settings settings) {
		super(settings);
	}

	@Override
	public void onCraft(ItemStack stack, World world, PlayerEntity player) {
		super.onCraft(stack, world, player);
		if (!stack.hasTag()) stack.setTag(new CompoundTag());
		if (!stack.getTag().contains("Seed")) {
			stack.getTag().putInt("Seed", ThreadLocalRandom.current().nextInt());
		}
	}
	
	@Override
	public int getColor(final ItemStack stack) {
		CompoundTag tag = stack.getSubTag("display");
		if (tag != null && tag.contains("color", 99)) {
			return tag.getInt("color");
		}
		return -1;
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		Iterator<Text> iter = tooltip.iterator();
		while (iter.hasNext()) {
			Text t = iter.next();
			if (t instanceof TranslatableText) {
				if (((TranslatableText) t).getKey().equals("item.dyed")) {
					iter.remove();
				}
			}
		}
		tooltip.add(Moonball.getFlavorText(stack.hasTag() ? stack.getTag().getInt("Seed"): Moonball.class.hashCode()));
	}
	
	@Override
	public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
		if (isIn(group)) {
			List<ItemStack> balls = Lists.newArrayList();
			if (group == ItemGroup.COMBAT) {
				for (DyeColor a : DyeColor.values()) {
					ItemStack stack = DyeableItem.blendAndSetColor(new ItemStack(this), Lists.newArrayList(DyeItem.byColor(a)));
					stack.getTag().putInt("Seed", ThreadLocalRandom.current().nextInt());
					balls.add(stack);
				}
			} else {
				for (DyeColor a : DyeColor.values()) {
					for (DyeColor b : DyeColor.values()) {
						ItemStack stack = DyeableItem.blendAndSetColor(new ItemStack(this), Lists.newArrayList(DyeItem.byColor(a), DyeItem.byColor(b)));
						stack.getTag().putInt("Seed", ThreadLocalRandom.current().nextInt());
						balls.add(stack);
					}
				}
			}
			// sort by hue for  a e s t h e t i c s
			balls.sort((a, b) -> {
				double[] hslA = toHsl(getColor(a));
				double[] hslB = toHsl(getColor(b));
				if (Math.abs(hslA[0]-hslB[0]) < 0.005) {
					// hue is similar; compare saturation?
					if (Math.abs(hslA[1]-hslB[1]) < 0.005) {
						// saturation is close; compare lightness
						return Double.compare(hslA[2], hslB[2]);
					}
					return Double.compare(hslA[1], hslB[1]);
				}
				return Double.compare(hslA[0], hslB[0]);
			});
			stacks.addAll(balls);
		}
	}

	// based on chromatism.js
	public static double[] toHsl(int color) {
		double[] rgb = { ((color >> 16) & 0xFF) / 255D, ((color >> 8) & 0xFF) / 255D, ((color >> 0) & 0xFF) / 255D };
		double[] rgbOrdered = Arrays.copyOf(rgb, 3);
		Arrays.sort(rgbOrdered);
		double l = ((rgbOrdered[0] + rgbOrdered[2]) / 2);
		double s, h;
		if (Double.compare(rgbOrdered[0], rgbOrdered[2]) == 0) {
			s = 0;
			h = 0;
		} else {
			if (l >= 50) {
				s = ((rgbOrdered[2] - rgbOrdered[0]) / ((2D - rgbOrdered[2]) - rgbOrdered[0]));
			} else {
				s = ((rgbOrdered[2] - rgbOrdered[0]) / (rgbOrdered[2] + rgbOrdered[0]));
			}
			if (Double.compare(rgbOrdered[2], rgb[0]) == 0) {
				h = ((rgb[1] - rgb[2]) / (rgbOrdered[2] - rgbOrdered[0])) / 6;
			} else if (Double.compare(rgbOrdered[2], rgb[1]) == 0) {
				h = (2 + ((rgb[2] - rgb[0]) / (rgbOrdered[2] - rgbOrdered[0]))) / 6;
			} else {
				h = (4 + ((rgb[0] - rgb[1]) / (rgbOrdered[2] - rgbOrdered[0]))) / 6;
			}
			if (h < 0) {
				h += 1;
			} else if (h > 1) {
				h = h % 1;
			}
		}
		return new double[] { h, s, l };
	}
	
	
	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BOW;
	}
	
	@Override
	public int getMaxUseTime(ItemStack stack) {
		return 72000;
	}

	@Override
	public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
		super.usageTick(world, user, stack, remainingUseTicks);
		if (remainingUseTicks == getMaxUseTime(stack)-10 && world instanceof ServerWorld) {
			for (PlayerEntity ep : world.getPlayers(TargetPredicate.DEFAULT, user, user.getBoundingBox().expand(32))) {
				ep.sendMessage(new TranslatableText("moonball.hud.dangerzone", user.getDisplayName(), Moonball.getFlavorText()).formatted(Formatting.RED, Formatting.ITALIC, Formatting.BOLD), true);
			}
			if (user instanceof PlayerEntity) {
				((PlayerEntity)user).sendMessage(new TranslatableText("moonball.hud.dangerzone.self", Moonball.getFlavorText()).formatted(Formatting.RED, Formatting.ITALIC, Formatting.BOLD), true);
			}
		}
	}

	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int timeLeft) {
		super.onStoppedUsing(stack, world, user, timeLeft);

		int charge = getMaxUseTime(stack) - timeLeft;
		float power = charge / 20.0F;
		power = (power * power + power * 2.0F) / 3.0F;

		if (power > 2.0F) {
			power = 2.0F;
		}

		PlayerEntity player = user instanceof PlayerEntity ? (PlayerEntity) user : null;

		PickupPermission perm;
		ItemStack thrown;
		if (player == null || !player.abilities.creativeMode) {
			thrown = stack.split(1);
			perm = PickupPermission.ALLOWED;
		} else {
			thrown = stack.copy();
			thrown.setCount(1);
			perm = PickupPermission.CREATIVE_ONLY;
		}

		world.playSound(null, user.getPos().x, user.getPos().y, user.getPos().z, Moonball.THROW_SOUND, user.getSoundCategory(), 0.5f,
				0.4f / (RANDOM.nextFloat() * 0.4f + 0.8f));

		if (!world.isClient) {
			MoonballEntity munbah = new MoonballEntity(world, user, thrown);
			munbah.pickupType = perm;
			munbah.setProperties(user, user.pitch, user.yaw, 0, 1.25f * power, 0.5f);
			world.spawnEntity(munbah);
		}

		if (player != null) {
			player.incrementStat(Stats.USED.getOrCreateStat(this));
		}
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		user.setCurrentHand(hand);
		return new TypedActionResult<>(ActionResult.SUCCESS, stack);
	}
	
}
