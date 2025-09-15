package arcana.research;

import arcana.util.NbtUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public record Entry(
		Identifier id,
		Category category,
		String name,
		String desc,
		List<EntrySection> sections,
		List<Parent> parents,
		List<Icon> icons,
		List<String> meta,
		List<Addendum> addenda,
		int x,
		int y
){
	
	public Stream<Pin> getAllPins(World world){
		return sections().stream().flatMap(section -> section.pins(sections.indexOf(section), world, this));
	}
	
	public int warping(){
		return getIntMeta("warping");
	}
	
	public int getIntMeta(String prefix){
		for(String s : meta)
			if(s.startsWith(prefix + ":"))
				try{
					return Integer.parseInt(s.substring((prefix + ":").length()));
				}catch(NumberFormatException ignored){}
		return 0;
	}
	
	public NbtCompound toNbt(){
		NbtCompound compound = new NbtCompound();
		compound.putString("id", id.toString());
		compound.putString("name", name);
		compound.putString("desc", desc);
		compound.putInt("x", x);
		compound.putInt("y", y);
		
		NbtList sectionList = new NbtList();
		for(EntrySection section : sections)
			sectionList.add(section.getPassData());
		compound.put("sections", sectionList);
		
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
		
		compound.put("addenda", addenda.stream().map(Addendum::toNbt).collect(NbtUtil.toNbtList()));
		
		return compound;
	}
	
	public static Entry fromNbt(NbtCompound compound, Category in){
		Identifier id = new Identifier(compound.getString("id"));
		String name = compound.getString("name"), desc = compound.getString("desc");
		int x = compound.getInt("x"), y = compound.getInt("y");
		
		List<EntrySection> sections = NbtUtil.readList(compound, "sections", EntrySection::deserialize);
		
		List<Parent> parents = new ArrayList<>();
		for(NbtElement parent : compound.getList("parents", NbtElement.STRING_TYPE))
			parents.add(Parent.parse(parent.asString())); // NbtString overrides it
		
		List<Icon> icons = new ArrayList<>();
		for(NbtElement parent : compound.getList("icons", NbtElement.STRING_TYPE))
			icons.add(Icon.fromString(parent.asString()));
		
		List<String> meta = new ArrayList<>();
		for(NbtElement parent : compound.getList("meta", NbtElement.STRING_TYPE))
			meta.add(parent.asString());
		
		List<Addendum> addenda = new ArrayList<>();
		Entry entry = new Entry(id, in, name, desc, sections, parents, icons, meta, addenda, x, y);
		
		for(NbtElement nbt : compound.getList("addenda", NbtElement.COMPOUND_TYPE))
			addenda.add(Addendum.fromNbt((NbtCompound)nbt, entry));
		
		return entry;
	}
	
	// recursive equality with parent category
	public boolean equals(Object obj){
		return obj instanceof Entry e && e.id().equals(id());
	}
	
	public int hashCode(){
		return id().hashCode();
	}
	
	public String toString(){
		return id.toString();
	}
}