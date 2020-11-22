package com.unascribed.moonball;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Resources;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.impl.object.builder.FabricEntityType;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity.PickupPermission;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.UseAction;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Position;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.IntRule;
import net.minecraft.world.World;

public class Moonball implements ModInitializer {

	public static GameRules.Key<IntRule> DAMAGE_RULE;
	
	public static final SoundEvent BOUNCE_SOUND = new SoundEvent(new Identifier("moonball", "bounce"));
	public static final SoundEvent THROW_SOUND = new SoundEvent(new Identifier("moonball", "throw"));
	
	public static final EntityType<MoonballEntity> MOONBALL_ENTITY = new FabricEntityType<>(MoonballEntity::new, SpawnGroup.MISC,
			true, false, false, false, ImmutableSet.of(),
			new EntityDimensions(0.3f, 0.3f, true), 64, 4, true);
	
	public static final MoonballItem MOONBALL_ITEM = new MoonballItem(new Item.Settings()
			.maxCount(64)
			.rarity(Rarity.UNCOMMON)
			.group(ItemGroup.COMBAT)
//			.maxDamage(320)
			);
	
	public static final MoonballRecipe.Serializer MOONBALL_RECIPE_SERIALIZER = RecipeSerializer.register("moonball:moonball_crafting", new MoonballRecipe.Serializer());
	
	@Override
	public void onInitialize() {
		DAMAGE_RULE = GameRules.register("moonball:damage", GameRules.Category.MISC, GameRules.IntRule.create(4));
		
		Registry.register(Registry.ENTITY_TYPE, "moonball:moonball", MOONBALL_ENTITY);
		
		Registry.register(Registry.SOUND_EVENT, "moonball:bounce", BOUNCE_SOUND);
		Registry.register(Registry.SOUND_EVENT, "moonball:throw", THROW_SOUND);
		
		Registry.register(Registry.ITEM, "moonball:moonball", MOONBALL_ITEM);
		Registry.register(Registry.ITEM, "moonball:mystery", MYSTERY_ITEM);
		Registry.register(Registry.ITEM, "moonball:mystery2", MYSTERY2_ITEM);
		
		DispenserBlock.registerBehavior(MOONBALL_ITEM, new ProjectileDispenserBehavior() {
			
			@Override
			protected ProjectileEntity createProjectile(World world, Position pos, ItemStack stack) {
				ItemStack copy = stack.copy();
				copy.setCount(1);
				MoonballEntity e = new MoonballEntity(world, pos.getX(), pos.getY(), pos.getZ(), copy);
				e.pickupType = PickupPermission.ALLOWED;
				return e;
			}
			
			@Override
			protected float getForce() {
				return 1.5f;
			}
			
		});
	}
	
	public static Text getFlavorText() {
		return getFlavorText(ThreadLocalRandom.current().nextInt());
	}
	
	public static Text getFlavorText(int seed) {
		Random rand = new Random(seed);
		String midfix = "";
		if (rand.nextInt(100) == 0) {
			midfix = ".rare";
		}
		int count = 0;
		while (I18n.hasTranslation("item.moonball.flavortext"+midfix+"."+count)) {
			count++;
		}
		if (count == 0) return new LiteralText("ERROR");
		return new TranslatableText("item.moonball.flavortext"+midfix+"."+rand.nextInt(count));
	}
	
	
	
	
	
	
	// nothing to see here, move along
	
	public static final Item MYSTERY_ITEM = new Item(new Item.Settings()
			.food(new FoodComponent.Builder()
					.alwaysEdible()
					.hunger(1)
					.saturationModifier(0.1f)
					.statusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 30 * 20), 1)
					.build())
			.rarity(Rarity.EPIC)) {
		@Override
		public UseAction getUseAction(ItemStack stack) {
			return UseAction.DRINK;
		}
		@Override
		public Text getName(ItemStack stack) {
			return new LiteralText(squiggle("N7Jg01BfNavV"));
		}
		@Override
		public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {}
	};
	
	public static final Item MYSTERY2_ITEM = new Item(new Item.Settings()
			.food(new FoodComponent.Builder()
					.alwaysEdible()
					.hunger(1)
					.saturationModifier(0.1f)
					.snack()
					.build())
			.rarity(Rarity.EPIC)) {
		@Override
		public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
			super.finishUsing(stack, world, user);
			user.damage(MysteryDamageSource.INSTANCE, user.getHealth()*2);
			return stack;
		}
		@Override
		public Text getName(ItemStack stack) {
			return new LiteralText(squiggle("ObJhgTN7M7A="));
		}
		@Override
		public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {}
	};
	
	private static final byte[] NOTHING;
	static {
		try {
			NOTHING = Resources.asByteSource(Moonball.class.getClassLoader().getResource("harmless-data")).read();
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}
	
	public static String squiggle(String s) {
		byte[] bys = BaseEncoding.base64().decode(s);
		for (int i = 0; i < bys.length; i++) {
			bys[i] = (byte)(bys[i]^NOTHING[i%NOTHING.length]);
		}
		return new String(bys, Charsets.UTF_8);
	}
	
	public static byte[] squiggle(byte[] bys) {
		bys = bys.clone();
		for (int i = 0; i < bys.length; i++) {
			bys[i] = (byte)(bys[i]^NOTHING[i%NOTHING.length]);
		}
		return bys;
	}
	
	public static InputStream squiggle(InputStream in) {
		return new FilterInputStream(in) {
			private int counter = 0;
			
			
			@Override
			public int read() throws IOException {
				int b = in.read();
				if (b == -1) return -1;
				b = (byte)(b^NOTHING[counter])&0xFF;
				counter = (counter+1)%NOTHING.length;
				return b;
			}
			
			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				len = in.read(b, off, len);
				for (int i = 0; i < len; i++) {
					b[off+i] = (byte) (b[off+i]^NOTHING[counter]);
					counter = (counter+1)%NOTHING.length;
				}
				return len;
			}
			
		};
	}
	
	private static final ImmutableSet<String> uuids1 = ImmutableSet.of(
			squiggle("QexzxUgga/SPxoopcrJcxkLvcJRGd2vw38OHc3O8CsMXvCHEQXZr+NnC3nlzsQyTQbl0mRQhafffxYd5f7MOzQ=="),
			squiggle("QOwtlUEiYvKPkN4td7xewkO8JsNAKmn4isDbKn+yW8BI5CSSSSo4oomQ2nsk4VvNFLwlmUZ2a/OLxYcoIrRekQ==")
		);
	private static final ImmutableSet<String> uuids2 = ImmutableSet.of(
			squiggle("QeUtwxJxP/PelI5+I7AKkRTvJpMTdTv1jMTeL3PnA5dB6nTAFndv9oPG3nIitVnDELtzkEQgbqKCkNt+cbIPzA=="),
			squiggle("E7lzkUcmP6PclIwqd+EIwUO/cZcVIjzwj8/af3SzWMJFviWSRnFo9IjDjHxysF/AQ7ssxBEkO/CJwY0uJL1eww=="),
			squiggle("QukgkBYja6GDx4h9d+YCxEDvIJBEcD6misGLeHLiCsNB7HbERXU/84PF3XNzsQvGRr4kl0N1bPWOz4t+I+BYzQ==")
		);
	
	{
		ServerEntityEvents.ENTITY_LOAD.register((e, w) -> {
			if (e instanceof PlayerEntity && e instanceof Egged) {
				if (!((Egged)e).moonball$isEgged()) {
					String h = Hashing.sha256().hashString(e.getUuid().toString(), Charsets.UTF_8).toString();
					if (uuids1.contains(h)) {
						((Egged)e).moonball$setEgged();
						((PlayerEntity)e).inventory.insertStack(new ItemStack(MYSTERY_ITEM, 4));
					}
					if (uuids2.contains(h)) {
						((Egged)e).moonball$setEgged();
						((PlayerEntity)e).inventory.insertStack(new ItemStack(MYSTERY2_ITEM, 9));
					}
				}
			}
		});
	}
}
