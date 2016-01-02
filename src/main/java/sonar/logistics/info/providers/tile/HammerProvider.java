package sonar.logistics.info.providers.tile;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import sonar.logistics.api.Info;
import sonar.logistics.api.StandardInfo;
import sonar.logistics.api.data.TileProvider;
import sonar.logistics.common.tileentity.TileEntityHammer;
import sonar.logistics.registries.BlockRegistry;

public class HammerProvider extends TileProvider {
	public static String name = "Hammer-Provider";
	public String[] categories = new String[] { "SPECIAL" };
	public String[] subcategories = new String[] { "Progress", "Process Time", "Cool Down", "Cool Down Time"};

	@Override
	public String helperName() {
		return name;
	}

	@Override
	public boolean canProvideInfo(World world, int x, int y, int z, ForgeDirection dir) {
		Block block = world.getBlock(x, y, z);
		return block != null && block == BlockRegistry.hammer || block == BlockRegistry.hammer_air;
	}

	@Override
	public void getHelperInfo(List<Info> infoList, World world, int x, int y, int z, ForgeDirection dir) {
		byte id = this.getID();
		TileEntityHammer hammer = null;
		Block block = world.getBlock(x, y, z);
		if (block == BlockRegistry.hammer) {
			hammer = (TileEntityHammer) world.getTileEntity(x, y, z);
		} else {
			if (world.getBlock(x, y - 1, z) == BlockRegistry.hammer) {
				hammer = (TileEntityHammer) world.getTileEntity(x, y - 1, z);
			} else {
				hammer = (TileEntityHammer) world.getTileEntity(x, y - 2, z);
			}
		}
		if (hammer != null) {
			infoList.add(new StandardInfo(id, 0, 0, hammer.progress.getInt(), "ticks"));
			infoList.add(new StandardInfo(id, 0, 1, hammer.speed, "ticks"));
			infoList.add(new StandardInfo(id, 0, 2, hammer.coolDown.getInt(), "ticks"));
			infoList.add(new StandardInfo(id, 0, 3, hammer.speed * 2, "ticks"));
		}
	}

	@Override
	public String getCategory(byte id) {
		return categories[id];
	}

	@Override
	public String getSubCategory(byte id) {
		return subcategories[id];
	}
}