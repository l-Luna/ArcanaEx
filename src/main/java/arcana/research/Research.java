package arcana.research;

import arcana.Networking;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public final class Research{
	
	public static final Map<Identifier, Book> books = new LinkedHashMap<>();
	public static final Map<Identifier, Puzzle> puzzles = new LinkedHashMap<>();
	
	public static void setup(){
		EntrySection.setup();
		Requirement.setup();
		Networking.setup();
		Puzzle.setup();
	}
	
	public static Book getBook(Identifier book){
		return books.get(book);
	}
	
	public static Category getCategory(Identifier category){
		return streamCategories().filter(x -> x.id().equals(category)).findFirst().orElse(null);
	}
	
	public static Entry getEntry(Identifier entry){
		return streamEntries().filter(x -> x.id().equals(entry)).findFirst().orElse(null);
	}
	
	public static Puzzle getPuzzle(Identifier puzzle){
		return puzzles.get(puzzle);
	}
	
	public static Stream<Book> streamBooks(){
		return books.values().stream();
	}
	
	public static Stream<Category> streamCategories(){
		return streamBooks().flatMap(x -> x.categories().stream());
	}
	
	public static Stream<Entry> streamEntries(){
		return streamCategories().flatMap(x -> x.entries().stream());
	}
	
	public static Stream<Puzzle> streamPuzzles(){
		return puzzles.values().stream();
	}
	
	public static Stream<Pair<Entry, Parent>> streamChildrenOf(Entry parent){
		return streamEntries()
				.map(x -> new Pair<>(x, x.parents().stream().filter(p -> p.id().equals(parent.id())).toList()))
				.filter(x -> x.getRight().size() > 0)
				.map(x -> new Pair<>(x.getLeft(), x.getRight().get(0)));
	}
}