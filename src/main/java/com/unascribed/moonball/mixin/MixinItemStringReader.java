package com.unascribed.moonball.mixin;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.tag.TagGroup;

@Mixin(ItemStringReader.class)
public class MixinItemStringReader {

	@Inject(at=@At("RETURN"), method= {
			"suggestItem(Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;Lnet/minecraft/tag/TagGroup;)Ljava/util/concurrent/CompletableFuture;",
			"suggestAny(Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;Lnet/minecraft/tag/TagGroup;)Ljava/util/concurrent/CompletableFuture;"
	}, cancellable=true)
	public void suggest(SuggestionsBuilder bldr, TagGroup<?> grp, CallbackInfoReturnable<CompletableFuture<Suggestions>> ci) {
		CompletableFuture<Suggestions> f = new CompletableFuture<>();
		ci.getReturnValue().whenComplete((s, t) -> {
			if (t != null) {
				f.completeExceptionally(t);
			} else {
				Iterator<Suggestion> iter = s.getList().iterator();
				while (iter.hasNext()) {
					Suggestion i = iter.next();
					if (i.getText().contains("moonball:mystery")) {
						iter.remove();
					}
				}
				f.complete(s);
			}
		});
		ci.setReturnValue(f);
	}
	
}
