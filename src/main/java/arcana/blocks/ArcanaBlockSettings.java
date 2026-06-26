package arcana.blocks;

import arcana.ArcanaRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;

// TODO: use AW instead
// TODO: once the block codecs are actually used, The Horrors ensue
public class ArcanaBlockSettings extends AbstractBlock.Settings{
	
	private boolean dropsSelf = false;
	private BlockLayer renderLayer = null;
	private TagKey<Block> toolTag = null;
	private ArcanaRegistry.Tab group = null;
	
	protected ArcanaBlockSettings(MapColor color){
		super();
		mapColor(color);
	}
	
	public static ArcanaBlockSettings of(Material material){
		ArcanaBlockSettings settings = new ArcanaBlockSettings(material.color());
		settings.sounds(material.sounds()).pistonBehavior(material.crushable() ? PistonBehavior.DESTROY : PistonBehavior.NORMAL);
		return settings;
	}
	
	public static ArcanaBlockSettings of(Material material, MapColor color){
		ArcanaBlockSettings settings = of(material);
		settings.mapColor(color);
		return settings;
	}
	
	//
	
	public ArcanaBlockSettings dropsSelf(){
		dropsSelf = true;
		return this;
	}
	
	public ArcanaBlockSettings renderLayer(BlockLayer layer){
		renderLayer = layer;
		return this;
	}
	
	public ArcanaBlockSettings usesTool(TagKey<Block> toolTag){
		this.toolTag = toolTag;
		return this;
	}
	
	public ArcanaBlockSettings requiresTool(TagKey<Block> toolTag){
		this.toolTag = toolTag;
		requiresTool();
		return this;
	}
	
	public ArcanaBlockSettings group(ArcanaRegistry.Tab group){
		this.group = group;
		return this;
	}
	
	public ArcanaBlockSettings strength(float strength){
		super.strength(strength);
		return this;
	}
	
	public ArcanaBlockSettings strength(float strength, float resistance){
		super.strength(strength, resistance);
		return this;
	}
	
	public ArcanaBlockSettings sounds(BlockSoundGroup soundGroup){
		super.sounds(soundGroup);
		return this;
	}
	
	public ArcanaBlockSettings luminance(int lumi){
		luminance(__ -> lumi);
		return this;
	}
	
	//
	
	public BlockLayer getRenderLayer(){
		return renderLayer;
	}
	
	public boolean getDropsSelf(){
		return dropsSelf;
	}
	
	public TagKey<Block> getToolTag(){
		return toolTag;
	}
	
	public ArcanaRegistry.Tab getGroup(){
		return group;
	}
	
	//
	
	public enum BlockLayer{
		OPAQUE,
		TRANSLUCENT,
		CUTOUT
	}
}