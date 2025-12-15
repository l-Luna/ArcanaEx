package arcana.commands;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.util.Identifier;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class ArcanaCommands{
	
	public static void register(){
		CommandRegistrationCallback.EVENT.register(NodeCommand::register);
		CommandRegistrationCallback.EVENT.register(ResearchCommand::register);
		CommandRegistrationCallback.EVENT.register(WarpCommand::register);
	}
	
	// similar to versions from CommandSource, but work correctly for other namespaces
	public static CompletableFuture<Suggestions> suggestIdentifiers(Stream<Identifier> candidates, SuggestionsBuilder builder){
		return suggestIdentifiers(candidates::iterator, builder);
	}
	
	public static CompletableFuture<Suggestions> suggestIdentifiers(Iterable<Identifier> candidates, SuggestionsBuilder builder){
		forEachMatching(candidates, builder.getRemaining().toLowerCase(Locale.ROOT), id -> builder.suggest(id.toString()));
		return builder.buildFuture();
	}
	
	private static void forEachMatching(Iterable<Identifier> candidates, String remaining, Consumer<Identifier> act){
		for(Identifier id : candidates)
			if(id.toString().contains(remaining))
				act.accept(id);
	}
}