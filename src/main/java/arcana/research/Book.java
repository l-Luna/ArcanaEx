package arcana.research;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// immutable data carriers, now with mutable data!
public record Book(Identifier id, Map<Identifier, Category> categories){
	
	public List<Category> categoryList(){
		return new ArrayList<>(categories.values());
	}
	
	public NbtCompound toNbt(){
		var compound = new NbtCompound();
		compound.putString("id", id.toString());
		NbtList categoriesList = new NbtList();
		for(Category cat : categories.values())
			categoriesList.add(cat.toNbt());
		compound.put("categories", categoriesList);
		return compound;
	}
	
	public static Book fromNbt(NbtCompound compound){
		Identifier id = new Identifier(compound.getString("id"));
		Map<Identifier, Category> categories = new LinkedHashMap<>();
		var book = new Book(id, categories);
		// the book object needs to exist before constructing the categories
		for(NbtElement catElement : compound.getList("categories", NbtElement.COMPOUND_TYPE)){
			NbtCompound catCompound = (NbtCompound)catElement;
			Category category = Category.fromNbt(catCompound, book);
			categories.put(category.id(), category);
		}
		return book;
	}
}