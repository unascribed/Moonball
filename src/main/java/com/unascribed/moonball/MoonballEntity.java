package com.unascribed.moonball;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class MoonballEntity extends PersistentProjectileEntity {

	private static final TrackedData<ItemStack> STACK = DataTracker.registerData(MoonballEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
	
	// Add a 1-tick delay when bouncing so that new heading is considered fully for new collisions
	// This prevents clipping in tight corners
	private boolean bouncing;
	
	private boolean done = false;

	private Vec3d lastVelocity = Vec3d.ZERO;
	private Vec3d newVelocity;

	// As a fallback, limit the total number of bounces (should only be reached if stuck inside a block)
	private int bounceCount;

	public MoonballEntity(EntityType<? extends MoonballEntity> type, World world) {
		super(type, world);
	}

	public MoonballEntity(World world, LivingEntity thrower, ItemStack stack) {
		super(Moonball.MOONBALL_ENTITY, thrower, world);
		setStack(stack);
	}
	
	public MoonballEntity(World world, double x, double y, double z, ItemStack stack) {
		super(Moonball.MOONBALL_ENTITY, x, y, z, world);
		setStack(stack);
	}
	
	@Override
	protected void initDataTracker() {
		super.initDataTracker();
		dataTracker.startTracking(STACK, ItemStack.EMPTY);
	}

	@Override
	public void tick() {
		lastVelocity = getVelocity();
		if (bouncing) {
			bouncing = false;
			setVelocity(newVelocity);
		}

		super.tick();
		setVelocity(getVelocity().add(0, -0.03, 0));

		if (bounceCount > 50 && !world.isClient) {
			finish();
		}

		setVelocity(getVelocity().multiply(0.99));
	}
	
	@Override
	public boolean hasNoGravity() {
		return true;
	}
	
	public int getColor() {
		return Moonball.MOONBALL_ITEM.getColor(getStack());
	}
	
	public ItemStack getStack() {
		return getDataTracker().get(STACK);
	}
	
	public void setStack(ItemStack stack) {
		getDataTracker().set(STACK, stack);
	}
	
	@Override
	protected void onCollision(HitResult result) {
		super.onCollision(result);
		if (result.getType() == Type.MISS) return;
		double velSq = lastVelocity.lengthSquared();
		if (!world.isClient) {
			world.playSound(null, result.getPos().x, result.getPos().y, result.getPos().z,
					Moonball.BOUNCE_SOUND, SoundCategory.HOSTILE, (float) (0.75f * velSq), 0.5f);
		}
	}
	
	@Override
	protected void onEntityHit(EntityHitResult result) {
		DamageSource dmg = new MoonballDamageSource("moonball", this, this.getOwner());
		if (!world.isClient) {
			double velSq = getVelocity().lengthSquared();
			if (result.getEntity().damage(dmg, (float) (world.getGameRules().getInt(Moonball.DAMAGE_RULE) * velSq))) {
				damage((int)Math.ceil(velSq));
				finish();
			}
		}
	}
	
	@Override
	protected void onBlockHit(BlockHitResult result) {
		double velSq = getVelocity().lengthSquared();
		World world = getEntityWorld();
		BlockPos hitLoc = result.getBlockPos();
		if (hitLoc == null) {
			return;
		}
		
		if (done) return;
		
		damage((int)Math.ceil(velSq));
		
		BlockState hit = world.getBlockState(hitLoc);
		float hardness = hit.getHardness(world, hitLoc);
		if (hardness >= 0 && hardness < velSq * 0.5) {
			if (!world.isClient) {
				world.breakBlock(hitLoc, true, this);
			}
			setVelocity(getVelocity().multiply(1 - (hardness/2)));
			if (velSq > hardness*3) {
				// if we are going so fast that this block was soft, don't bother bouncing
				return;
			}
		}
		
		if (!hit.getCollisionShape(world, hitLoc).isEmpty()) {
			// Despawn if going too slow
			if (velSq < 0.1) {
				if (!world.isClient) {
					finish();
					return;
				}
			}
			
			// High angles of incidence (Near 180 degrees) can cause the ball to get stuck in a wall
			// To fix this, I add some "ghost" velocity in the direction normal to the wall
			Vec3d motionVec = getVelocity();
			Vec3i dirVec = result.getSide().getVector();
			Vec3d bounceVec = new Vec3d(dirVec.getX(), dirVec.getY(), dirVec.getZ());
			motionVec.multiply(bounceVec);
			// This is a very shallow bounce, so give it some extra "kick"
			if (motionVec.lengthSquared() < 0.001) {
				motionVec.multiply(4);
				setVelocity(getVelocity().add(motionVec));
			}

			bounceCount++;
			bouncing = true;
			newVelocity = getVelocity();
			
			switch (result.getSide().getAxis()) {
				case X:
					newVelocity = new Vec3d(-newVelocity.x, newVelocity.y, newVelocity.z);
					break;
				case Y:
					newVelocity = new Vec3d(newVelocity.x, -newVelocity.y, newVelocity.z);
					break;
				case Z:
					newVelocity = new Vec3d(newVelocity.x, newVelocity.y, -newVelocity.z);
					break;
			}
			
			setVelocity(Vec3d.ZERO);
		}
	}

	public void finish() {
//		remove();
//		if (!world.isClient && pickupType == PickupPermission.ALLOWED) {
//			world.spawnEntity(new ItemEntity(world, getPos().x, getPos().y, getPos().z, getStack()));
//		}
		done = true;
		setVelocity(Vec3d.ZERO);
	}

	@Override
	public boolean shouldRenderName() {
		return hasCustomName();
	}
	
	@Override
	public boolean hasCustomName() {
		return getStack().hasCustomName();
	}
	
	@Override
	public Text getCustomName() {
		return getStack().getName();
	}
	
	private void damage(int amt) {
		if (amt <= 0) return;
//		ServerPlayerEntity thrower = getOwner() instanceof ServerPlayerEntity ? (ServerPlayerEntity)getOwner() : null;
//		ItemStack stack = getStack();
//		if (stack.damage(amt, random, thrower)) {
//			stack.decrement(1);
//			stack.setDamage(0);
//			if (thrower != null) {
//				thrower.incrementStat(Stats.BROKEN.getOrCreateStat(getStack().getItem()));
//			}
//		} else/if (random.nextBoolean()) {
//			int rgb = Moonball.MOONBALL_ITEM.getColor(stack);
//			int r = (rgb>>16)&0xFF;
//			int g = (rgb>> 8)&0xFF;
//			int b = (rgb>> 0)&0xFF;
//			if (r < 255) r++;
//			if (g < 255) g++;
//			if (b < 255) b++;
//			Moonball.MOONBALL_ITEM.setColor(stack, (r<<16)|(g<<8)|b);
//		}
//		setStack(stack);
	}
	
	@Override
	public void writeCustomDataToTag(CompoundTag tag) {
		super.writeCustomDataToTag(tag);
		tag.putBoolean("Done", done);
		tag.put("Stack", getStack().toTag(new CompoundTag()));
		tag.putInt("Bounces", bounceCount);
	}

	@Override
	public void readCustomDataFromTag(CompoundTag tag) {
		super.readCustomDataFromTag(tag);
		this.done = tag.getBoolean("Done");
		setStack(ItemStack.fromTag(tag.getCompound("Stack")));
		this.bounceCount = tag.getInt("Bounces");
	}

	@Override
	protected ItemStack asItemStack() {
		return getStack();
	}

}
