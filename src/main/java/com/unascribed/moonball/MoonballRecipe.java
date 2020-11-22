package com.unascribed.moonball;

import java.util.List;

import com.google.gson.JsonObject;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class MoonballRecipe extends ShapedRecipe {

	public MoonballRecipe(Identifier id, String group, int width, int height,
			DefaultedList<Ingredient> ingredients, ItemStack output) {
		super(id, group, width, height, ingredients, output);
	}
	
	public MoonballRecipe(ShapedRecipe toCopy) {
		this(toCopy.getId(), toCopy.getGroup(), toCopy.getWidth(), toCopy.getHeight(), toCopy.getPreviewInputs(), toCopy.getOutput());
	}

	@Override
	public ItemStack craft(CraftingInventory inv) {
		ItemStack result = super.craft(inv);
		List<DyeItem> dyes = Lists.newArrayList();
		for (int i = 0; i < inv.size(); i++) {
			ItemStack ingredient = inv.getStack(i);
			if (ingredient.getItem() instanceof BlockItem) {
				BlockItem bi = (BlockItem)ingredient.getItem();
				Block b = bi.getBlock();
				if (b.isIn(BlockTags.WOOL)) {
					for (DyeColor dc : DyeColor.values()) {
						if (dc.getMaterialColor() == b.getDefaultMaterialColor()) {
							dyes.add(DyeItem.byColor(dc));
							break;
						}
					}
				}
			}
		}
		return DyeableItem.blendAndSetColor(result, dyes);
	}
	
	public static class Serializer extends ShapedRecipe.Serializer {
		
		@Override
		public MoonballRecipe read(Identifier identifier, JsonObject jsonObject) {
			return new MoonballRecipe(super.read(identifier, jsonObject));
		}
		
		@Override
		public MoonballRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
			return new MoonballRecipe(super.read(identifier, packetByteBuf));
		}
		
	}

}
