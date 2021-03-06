package sonar.logistics.connections.handlers;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.logistics.PL2Config;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.info.handlers.IEntityInfoProvider;
import sonar.logistics.api.info.handlers.ITileInfoProvider;
import sonar.logistics.api.networks.IEntityMonitorHandler;
import sonar.logistics.api.networks.INetworkListChannels;
import sonar.logistics.api.networks.ITileMonitorHandler;
import sonar.logistics.api.register.LogicPath;
import sonar.logistics.api.register.TileHandlerMethod;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.EntityConnection;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.connections.channels.InfoNetworkChannels;
import sonar.logistics.info.LogicInfoRegistry;

public class InfoNetworkHandler extends ListNetworkHandler<IProvidableInfo> implements ITileMonitorHandler<IProvidableInfo, InfoNetworkChannels>, IEntityMonitorHandler<IProvidableInfo, InfoNetworkChannels> {
	
	public static InfoNetworkHandler INSTANCE = new InfoNetworkHandler();

	@Override
	public Class<? extends INetworkListChannels> getChannelsType(){
		return InfoNetworkChannels.class;	
	}

	@Override
	public MonitoredList<IProvidableInfo> updateInfo(InfoNetworkChannels channels, MonitoredList<IProvidableInfo> newList, MonitoredList<IProvidableInfo> previousList, BlockConnection connection) {
		EnumFacing face = connection.face.getOpposite();
		World world = connection.coords.getWorld();
		IBlockState state = connection.coords.getBlockState(world);
		BlockPos pos = connection.coords.getBlockPos();
		Block block = connection.coords.getBlock(world);
		TileEntity tile = connection.coords.getTileEntity(world);
		LogicInfoRegistry.INSTANCE.getTileInfo(newList, face, world, state, pos, face, block, tile);

		for (ITileInfoProvider handler : LogicInfoRegistry.INSTANCE.tileProviders) {
			if (handler.canProvide(world, state, pos, face, tile, block)) {
				TileHandlerMethod method = new TileHandlerMethod(handler);
				LogicPath path = new LogicPath();
				path.setStart(method);
				handler.provide(LogicInfoRegistry.INSTANCE, newList, path, null, world, state, pos, face, block, tile);
			}
		}
		return newList;
	}

	@Override
	public MonitoredList<IProvidableInfo> updateInfo(InfoNetworkChannels channels, MonitoredList<IProvidableInfo> newList, MonitoredList<IProvidableInfo> previousList, EntityConnection connection) {
		Entity entity = connection.entity;
		World world = entity.getEntityWorld();
		LogicInfoRegistry.INSTANCE.getEntityInfo(newList, entity);
		for (IEntityInfoProvider handler : LogicInfoRegistry.INSTANCE.entityProviders) {
			if (handler.canProvide(world, entity)) {
				handler.provide(newList, world, entity);
			}
		}
		return newList;
	}

	@Override
	public int updateRate() {
		return PL2Config.infoUpdate;
	}
}
