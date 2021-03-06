package sonar.logistics.connections;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import sonar.logistics.api.filters.ITransferFilteredTile;
import sonar.logistics.api.networks.INetworkHandler;
import sonar.logistics.api.networks.INetworkListener;
import sonar.logistics.api.tiles.nodes.INode;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.api.wireless.IDataReceiver;
import sonar.logistics.connections.channels.TransferNetworkChannels;
import sonar.logistics.managers.WirelessManager;

public abstract class CacheHandler<T> {

	public static final CacheHandler<IDataReceiver> RECEIVERS = new CacheHandler<IDataReceiver>(IDataReceiver.class) {

		@Override
		public void onConnectionAdded(LogisticsNetwork network, IDataReceiver connection) {			
			connection.onNetworkConnect(network); //if we don't do this they effectively would have no network
			WirelessManager.addReceiver(connection);						
		}

		@Override
		public void onConnectionRemoved(LogisticsNetwork network, IDataReceiver connection) {
			connection.onNetworkDisconnect(network);
			WirelessManager.removeReceiver(connection);	
		}
		
		@Override
		public void update(LogisticsNetwork network, List<IDataReceiver> connections) {
			network.markUpdate(NetworkUpdate.SUB_NETWORKS, NetworkUpdate.GLOBAL, NetworkUpdate.HANDLER_CHANNELS);
		}
	};

	public static final CacheHandler<IDataEmitter> EMITTERS = new CacheHandler<IDataEmitter>(IDataEmitter.class) {

		public void onConnectionAdded(LogisticsNetwork network, IDataEmitter emitter) {
			emitter.onNetworkConnect(network); //if we don't do this they effectively would have no network
			WirelessManager.addEmitter(emitter);						
		}

		public void onConnectionRemoved(LogisticsNetwork network, IDataEmitter emitter) {
			emitter.onNetworkDisconnect(network);
			WirelessManager.removeEmitter(emitter);
		}
	};

	public static final CacheHandler<IListReader> READER = new CacheHandler<IListReader>(IListReader.class) {

		public void onConnectionAdded(LogisticsNetwork network, IListReader reader) {
			List<INetworkHandler> handlers = reader.getValidHandlers();
			for (INetworkHandler handler : handlers) {
				network.getOrCreateNetworkChannels(handler.getChannelsType()).addConnection(this, reader);
			}
		}

		public void onConnectionRemoved(LogisticsNetwork network, IListReader reader) {
			List<INetworkHandler> handlers = reader.getValidHandlers();
			for (INetworkHandler handler : handlers) {
				network.getOrCreateNetworkChannels(handler.getChannelsType()).removeConnection(this, reader);
			}
		}
	};

	public static final CacheHandler<INetworkListener> TILE = new CacheHandler<INetworkListener>(INetworkListener.class) {

		@Override
		public void onConnectionAdded(LogisticsNetwork network, INetworkListener connection) {
			connection.onNetworkConnect(network);
		}

		@Override
		public void onConnectionRemoved(LogisticsNetwork network, INetworkListener connection) {
			connection.onNetworkDisconnect(network);
		}
	};

	public static final CacheHandler<ITransferFilteredTile> TRANSFER_NODES = new CacheHandler<ITransferFilteredTile>(ITransferFilteredTile.class) {

		public void onConnectionAdded(LogisticsNetwork network, ITransferFilteredTile reader) {
			network.getOrCreateNetworkChannels(TransferNetworkChannels.class).addConnection(this, reader);
		}

		public void onConnectionRemoved(LogisticsNetwork network, ITransferFilteredTile reader) {
			network.getOrCreateNetworkChannels(TransferNetworkChannels.class).removeConnection(this, reader);
		}

	};

	public static final CacheHandler<INode> NODES = new CacheHandler<INode>(INode.class) {

		@Override
		public void update(LogisticsNetwork network, List<INode> connections) {
			network.markUpdate(NetworkUpdate.LOCAL, NetworkUpdate.GLOBAL, NetworkUpdate.HANDLER_CHANNELS);
		}

	};

	public static final CacheHandler<IEntityNode> ENTITY_NODES = new CacheHandler<IEntityNode>(IEntityNode.class) {

		@Override
		public void update(LogisticsNetwork network, List<IEntityNode> connections) {
			network.markUpdate(NetworkUpdate.LOCAL, NetworkUpdate.GLOBAL, NetworkUpdate.HANDLER_CHANNELS);
		}

	};

	public static final ArrayList<CacheHandler> handlers = Lists.newArrayList(RECEIVERS, EMITTERS, READER, TILE, NODES, ENTITY_NODES, TRANSFER_NODES);// RECEIVERS and EMITTERS should always come first so connected networks are considered by NODEs and TRANSFER NODEs

	public Class<T> clazz;

	public CacheHandler(Class<T> clazz) {
		this.clazz = clazz;
	}

	public void update(LogisticsNetwork network, List<T> connections) {}

	public void onConnectionAdded(LogisticsNetwork network, T connection) {}

	public void onConnectionRemoved(LogisticsNetwork network, T connection) {}

	public static ArrayList<CacheHandler> getValidCaches(INetworkListener tile) {
		ArrayList<CacheHandler> valid = Lists.newArrayList();
		for (CacheHandler handler : CacheHandler.handlers) {
			if (handler.clazz.isInstance(tile)) {
				valid.add(handler);
			}
		}
		return valid;
	}
}