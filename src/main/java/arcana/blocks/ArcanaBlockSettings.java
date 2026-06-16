package arcana.blocks;

import arcana.ArcanaRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.registry.tag.TagKey;

// TODO: use AW instead
public class ArcanaBlockSettings extends FabricBlockSettings{
	
	private boolean dropsSelf = false;
	private BlockLayer renderLayer = null;
	private TagKey<Block> toolTag = null;
	private ArcanaRegistry.Tab group = null;
	
	protected ArcanaBlockSettings(MapColor color){
		super();
		((AbstractBlock.Settings)this).mapColor(color);
	}
	
	public static ArcanaBlockSettings of(Material material){
		return new ArcanaBlockSettings(MapColor.CYAN /*material.getColor()*/);
	}
	
	public static ArcanaBlockSettings of(Material material, MapColor color){
		return new ArcanaBlockSettings(color);
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