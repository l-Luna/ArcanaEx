package arcana.research;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record Entry(
		Identifier id,
		Category in,
		String name,
		String desc,
		List<EntrySection> sections,
		List<Parent> parents,
		List<Icon> icons,
		List<String> meta,
		int x,
		int y){
	
	public NbtCompound toNbt(){
		NbtCompound compound = new NbtCompound();
		compound.putString("id", id.toString());
		compound.putString("name", name);
		compound.putString("desc", desc);
		compound.putInt("x", x);
		compound.putInt("y", y);
		// TODO: EntrySection
		NbtList parentList = new NbtList();
		for(Parent parent : parents)
			parentList.add(NbtString.of(parent.asString()));
		compound.put("parents", parentList);
		
		NbtList iconList = new NbtList();
		for(Icon icon : icons)
			iconList.add(NbtString.of(icon.asString()));
		compound.put("icons", iconList);
		
		NbtList metaList = new NbtList();
		for(String s : meta)
			metaList.add(NbtString.of(s));
		compound.put("meta", metaList);
		
		return compound;
	}
	
	public static Entry fromNbt(NbtCompound compound, Category in){
		Identifier id = new Identifier(compound.getString("id"));
		String name = compound.getString("name"), desc = compound.getString("desc");
		int x = compound.getInt("x"), y = compound.getInt("y");
		
		List<Parent> parents = new ArrayList<>();
		List<Icon> icons = new ArrayList<>();
		List<String> meta = new ArrayList<>();
		
		for(NbtElement parent : compound.getList("parents", NbtElement.STRING_TYPE))
			parents.add(Parent.parse(parent.asString())); // NbtString overrides it
		for(NbtElement parent : compound.getList("icons", NbtElement.STRING_TYPE))
			icons.add(Icon.fromString(parent.asString()));
		for(NbtElement parent : compound.getList("meta", NbtElement.STRING_TYPE))
			meta.add(parent.asString());
		
		return new Entry(id, in, name, desc, List.of(), parents, icons, meta, x, y);
	}
}
