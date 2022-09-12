package arcana.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.tag.TagKey;

public class ArcanaBlockSettings extends FabricBlockSettings{
	
	private boolean dropsSelf = false;
	private BlockLayer renderLayer = null;
	private TagKey<Block> toolTag = null;
	
	protected ArcanaBlockSettings(Material material, MapColor color){
		super(material, color);
	}
	
	protected ArcanaBlockSettings(AbstractBlock.Settings settings){
		super(settings);
		if(settings instanceof ArcanaBlockSettings abs){
			dropsSelf = abs.dropsSelf;
			renderLayer = abs.renderLayer;
			toolTag = abs.toolTag;
		}
	}
	
	public static ArcanaBlockSettings of(Material material){
		return new ArcanaBlockSettings(material, material.getColor());
	}
	
	public static ArcanaBlockSettings of(Material material, MapColor color){
		return new ArcanaBlockSettings(material, color);
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
	
	//
	
	public enum BlockLayer{
		OPAQUE,
		TRANSLUCENT,
		CUTOUT
	}
}