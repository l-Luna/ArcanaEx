package arcana.research;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public record Category(
		Identifier id,
		Map<Identifier, Entry> entries,
		Icon icon,
		Identifier bg,
		Identifier requirement,
		String name,
		Book book){
	
	public NbtCompound toNbt(){
		NbtCompound compound = new NbtCompound();
		compound.putString("id", id.toString());
		compound.putString("icon", icon.asString());
		compound.putString("bg", bg.toString());
		if(requirement != null)
			compound.putString("requirement", requirement.toString());
		compound.putString("name", name);
		
		NbtList list = new NbtList();
		for(Entry entry : entries.values())
			list.add(entry.toNbt());
		compound.put("entries", list);
		
		return compound;
	}
	
	public static Category fromNbt(NbtCompound nbt, Book in){
		Identifier id = new Identifier(nbt.getString("id"));
		Icon icon = Icon.fromString(nbt.getString("icon"));
		Identifier bg = new Identifier(nbt.getString("bg"));
		Identifier requirement = nbt.contains("requirement") ? new Identifier(nbt.getString("requirement")) : null;
		String name = nbt.getString("name");
		
		Map<Identifier, Entry> entries = new LinkedHashMap<>();
		Category category = new Category(id, entries, icon, bg, requirement, name, in);
		
		for(NbtElement entryElem : nbt.getList("entries", NbtElement.COMPOUND_TYPE)){
			Entry entry = Entry.fromNbt((NbtCompound)entryElem, category);
			entries.put(entry.id(), entry);
		}
		
		return category;
	}
}