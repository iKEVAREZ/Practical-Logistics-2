package sonar.logistics.info.providers;

import java.util.List;

import com.enderio.core.api.common.util.IProgressTile;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.logistics.api.asm.TileInfoProvider;
import sonar.logistics.api.info.ClientNameConstants;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.info.handlers.ITileInfoProvider;
import sonar.logistics.api.info.register.IMasterInfoRegistry;
import sonar.logistics.api.register.LogicPath;
import sonar.logistics.api.register.RegistryType;

@TileInfoProvider(handlerID = "endercore-progress", modid = "endercore")
public class EnderCoreProgressProvider implements ITileInfoProvider {

	@Override
	public boolean canProvide(World world, IBlockState state, BlockPos pos, EnumFacing dir, TileEntity tile, Block block) {
		return tile instanceof IProgressTile;
	}

	@Override
	public void provide(IMasterInfoRegistry registry, List<IProvidableInfo> infoList, LogicPath currentPath, Integer methodCode, World world, IBlockState state, BlockPos pos, EnumFacing dir, Block block, TileEntity tile) {
		IProgressTile progressTile = (IProgressTile) tile;
		registry.buildInfo(infoList, currentPath.dupe(), ClientNameConstants.DEF, "ProcessTime", RegistryType.TILE, (int)(progressTile.getProgress()*100));
		registry.buildInfo(infoList, currentPath.dupe(), ClientNameConstants.DEF, "BaseProcessTime", RegistryType.TILE, (int)(100));		
	}

}
