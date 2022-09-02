package arcana.research;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

// immutable data carriers, now with mutable data!
public record Book(Identifier id, List<Category> categories){
	
	public NbtCompound toNbt(){
		var compound = new NbtCompound();
		compound.putString("id", id.toString());
		NbtList categoriesList = new NbtList();
		for(Category cat : categories)
			categoriesList.add(cat.toNbt());
		compound.put("categories", categoriesList);
		return compound;
	}
	
	public static Book fromNbt(NbtCompound compound){
		Identifier id = new Identifier(compound.getString("id"));
		List<Category> categories = new ArrayList<>();
		var book = new Book(id, categories);
		// the book object needs to exist before constructing the categories
		for(NbtElement catElement : compound.getList("categories", NbtElement.COMPOUND_TYPE)){
			NbtCompound catCompound = (NbtCompound)catElement;
			categories.add(Category.fromNbt(catCompound, book));
		}
		return book;
	}
	
	// recursive equality with contained categories
	public boolean equals(Object obj){
		return obj instanceof Book book && book.id().equals(id());
	}
	
	public int hashCode(){
		return id().hashCode();
	}
}