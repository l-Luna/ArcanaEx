package arcana.research;

import arcana.Networking;
import net.minecraft.util.Identifier;

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
	
	public static Stream<Book> streamBooks(){
		return books.values().stream();
	}
	
	public static Stream<Category> streamCategories(){
		return streamBooks().flatMap(x -> x.categories().values().stream());
	}
	
	public static Stream<Entry> streamEntries(){
		return streamCategories().flatMap(x -> x.entries().values().stream());
	}
}