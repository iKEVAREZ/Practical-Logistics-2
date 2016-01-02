package sonar.logistics.registries;

import net.minecraft.block.Block;
import sonar.core.common.block.SonarBlockTip;
import sonar.logistics.Logistics;
import sonar.logistics.common.blocks.*;
import sonar.logistics.common.tileentity.*;
import cpw.mods.fml.common.registry.GameRegistry;

public class BlockRegistry extends Logistics {

	public static Block displayScreen, node, entityNode, infoReader, dataCable, dataReceiver, dataEmitter, inventoryReader, redstoneSignaller_on, redstoneSignaller_off, holographicDisplay, dataModifier, infoCreator,sapphire_ore, hammer, hammer_air;

	public static void registerBlocks() {

		hammer = new BlockHammer().setBlockName("Hammer").setHardness(1.0F).setCreativeTab(Logistics.creativeTab).setResistance(20.0F).setBlockTextureName(modid + ":" + "hammer_break");
		GameRegistry.registerBlock(hammer, SonarBlockTip.class, "Hammer");
		GameRegistry.registerTileEntity(TileEntityHammer.class, "Hammer");	

		hammer_air = new BlockHammerAir().setBlockName("Hammer_Air").setHardness(1.0F).setResistance(20.0F).setBlockTextureName(modid + ":" + "hammer_break");
		GameRegistry.registerBlock(hammer_air, SonarBlockTip.class, "Hammer_Air");
		
		sapphire_ore = new BlockSapphireOre().setBlockName("SapphireOre").setHardness(3.0F).setCreativeTab(Logistics.creativeTab).setResistance(5.0F).setBlockTextureName(modid + ":" + "sapphire_ore");
		GameRegistry.registerBlock(sapphire_ore, "SapphireOre");


		displayScreen = new DisplayScreen().setBlockName("DisplayScreen").setHardness(0.2F).setResistance(20.0F).setBlockTextureName(modid + ":" + "infoBlock_normal");
		GameRegistry.registerBlock(displayScreen, SonarBlockTip.class, "DisplayScreen");
		GameRegistry.registerTileEntity(TileEntityDisplayScreen.class, "DisplayScreen");		

		dataCable = new BlockDataCable().setBlockName("DataCable").setHardness(0.1F).setCreativeTab(Logistics.creativeTab).setResistance(20.0F).setBlockTextureName(modid + ":" + "data_cable");
		GameRegistry.registerBlock(dataCable, SonarBlockTip.class, "DataCable");
		GameRegistry.registerTileEntity(TileEntityDataCable.class, "DataCable");	
		
		node = new BlockNode().setBlockName("Node").setHardness(1.0F).setResistance(20.0F).setCreativeTab(Logistics.creativeTab).setBlockTextureName(modid + ":" + "data_cable");
		GameRegistry.registerBlock(node, SonarBlockTip.class, "Node");
		GameRegistry.registerTileEntity(TileEntityBlockNode.class, "Node");

		entityNode = new BlockEntityNode().setBlockName("EntityNode").setHardness(1.0F).setResistance(20.0F).setCreativeTab(Logistics.creativeTab).setBlockTextureName(modid + ":" + "data_cable");
		GameRegistry.registerBlock(entityNode, SonarBlockTip.class, "EntityNode");
		GameRegistry.registerTileEntity(TileEntityEntityNode.class, "EntityNode");

		infoReader = new BlockInfoReader().setBlockName("InfoReader").setHardness(1.0F).setResistance(20.0F).setCreativeTab(Logistics.creativeTab).setBlockTextureName(modid + ":" + "data_cable");
		GameRegistry.registerBlock(infoReader, SonarBlockTip.class, "InfoReader");
		GameRegistry.registerTileEntity(TileEntityInfoReader.class, "InfoReader");

		inventoryReader = new BlockInventoryReader().setBlockName("InventoryReader").setHardness(1.0F).setCreativeTab(Logistics.creativeTab).setResistance(20.0F).setBlockTextureName(modid + ":" + "data_cable");
		GameRegistry.registerBlock(inventoryReader, SonarBlockTip.class, "InventoryReader");
		GameRegistry.registerTileEntity(TileEntityInventoryReader.class, "InventoryReader");
				
		dataReceiver = new BlockDataReceiver().setBlockName("DataReceiver").setHardness(1.0F).setCreativeTab(Logistics.creativeTab).setResistance(20.0F).setBlockTextureName(modid + ":" + "data_cable");
		GameRegistry.registerBlock(dataReceiver, SonarBlockTip.class, "DataReceiver");
		GameRegistry.registerTileEntity(TileEntityDataReceiver.class, "DataReceiver");		

		dataEmitter = new BlockDataEmitter().setBlockName("DataEmitter").setHardness(1.0F).setCreativeTab(Logistics.creativeTab).setResistance(20.0F).setBlockTextureName(modid + ":" + "data_cable");
		GameRegistry.registerBlock(dataEmitter, SonarBlockTip.class, "DataEmitter");
		GameRegistry.registerTileEntity(TileEntityDataEmitter.class, "DataEmitter");		
		
		redstoneSignaller_off = new BlockRedstoneSignaller(false).setBlockName("RedstoneSignaller").setHardness(0.1F).setCreativeTab(Logistics.creativeTab).setResistance(20.0F).setBlockTextureName(modid + ":" + "data_cable");
		redstoneSignaller_on = new BlockRedstoneSignaller(true).setBlockName("RedstoneSignaller").setHardness(0.1F).setLightLevel(0.5F).setResistance(20.0F).setBlockTextureName(modid + ":" + "data_cable");
		GameRegistry.registerBlock(redstoneSignaller_on, SonarBlockTip.class, "RedstoneSignaller_ON");
		GameRegistry.registerBlock(redstoneSignaller_off, SonarBlockTip.class, "RedstoneSignaller_OFF");
		GameRegistry.registerTileEntity(TileEntityRedstoneSignaller.class, "RedstoneSignaller");

		dataModifier = new BlockDataModifier().setBlockName("DataModifier").setHardness(1.0F).setCreativeTab(Logistics.creativeTab).setResistance(20.0F).setBlockTextureName(modid + ":" + "data_cable");
		GameRegistry.registerBlock(dataModifier, SonarBlockTip.class, "DataModifier");
		GameRegistry.registerTileEntity(TileEntityDataModifier.class, "DataModifier");	

		infoCreator = new BlockInfoCreator().setBlockName("InfoCreator").setHardness(1.0F).setCreativeTab(Logistics.creativeTab).setResistance(20.0F).setBlockTextureName(modid + ":" + "data_cable");
		GameRegistry.registerBlock(infoCreator, SonarBlockTip.class, "InfoCreator");
		GameRegistry.registerTileEntity(TileEntityInfoCreator.class, "InfoCreator");	
		
		holographicDisplay = new BlockHolographicDisplay().setBlockName("HolographicDisplay").setHardness(1.0F).setCreativeTab(Logistics.creativeTab).setResistance(20.0F).setBlockTextureName(modid + ":" + "data_cable");
		GameRegistry.registerBlock(holographicDisplay, SonarBlockTip.class, "HolographicDisplay");
		GameRegistry.registerTileEntity(TileEntityHolographicDisplay.class, "HolographicDisplay");	


	}

}