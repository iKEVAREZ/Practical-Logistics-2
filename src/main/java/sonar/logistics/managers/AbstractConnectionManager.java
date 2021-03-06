package sonar.logistics.managers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Lists;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.core.api.utils.BlockCoords;
import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.api.tiles.IConnectable;
import sonar.logistics.api.tiles.cable.ConnectableType;

public abstract class AbstractConnectionManager<T extends IConnectable> {

	protected Map<Integer, List<T>> connections = new ConcurrentHashMap<Integer, List<T>>();
	private NetworkManager NetworkManager;

	public void removeAll() {
		connections.clear();
	}

	public NetworkManager NetworkManager() {
		if (NetworkManager == null) {
			NetworkManager = PL2.instance.networkManager;
		}
		return NetworkManager;
	}

	public int getNextAvailableID() {
		for (int i = 0; i < connections.size(); i++) {
			if (connections.get(i) == null || connections.get(i).isEmpty() || connections.get(i).size() == 0) {
				return i;
			}
		}
		return connections.size();
	}

	public List<T> getConnections(int registryID) {
		List<T> coords;
		return (registryID == -1 || (coords = connections.get(registryID)) == null) ? Lists.newArrayList() : coords;
	}

	public void addConnections(int registryID, List<T> connections) {
		connections.forEach(connection -> addConnection(registryID, connection, false));
	}

	public int addConnection(T cable) {
		List<Pair<ConnectableType, Integer>> connections = Lists.newArrayList();
		int cableID = cable.getRegistryID();
		int lastSize = -1;
		BlockCoords coords = cable.getCoords();
		World world = coords.getWorld();
		BlockPos pos = coords.getBlockPos();

		for (EnumFacing dir : EnumFacing.values()) {
			if (cable.canConnectOnSide(cable.getRegistryID(), dir, false)) {
				Pair<ConnectableType, Integer> connection = getConnectionType(cable, world, pos, dir, cable.getConnectableType());
				if (connection.a != ConnectableType.NONE && connection.b != -1) {
					List<T> cables = getConnections(connection.b);
					if (cables.size() > lastSize) {
						cableID = connection.b;
						lastSize = cables.size();
					}
					connections.add(connection);
				}
			}
		}
		cableID = cableID == -1 ? getNextAvailableID() : cableID;
		addConnection(cableID, cable, true);
		for (Pair<ConnectableType, Integer> connection : connections) {
			if (connection.b != cableID) {
				connectNetworks(cableID, connection.b);
			}
		}
		return cableID;
	}

	public void addConnection(int registryID, T connection, boolean refreshCache) {
		if (registryID != -1 && connection != null) {
			List<T> network = connections.get(registryID);
			if (network == null) {
				connections.put(registryID, Lists.newArrayList());
				network = connections.get(registryID);
			}
			if (network != null && network.contains(connection))
				return;
			
			connection.setRegistryID(registryID);
			network.add(connection);
			if (refreshCache) {
				onConnectionAdded(registryID, connection);
			}
		}
	}

	public void removeConnection(T connection) {
		int registryID = connection.getRegistryID();
		if (registryID != -1 && connection.getCoords() != null) {
			List<T> allConnections = connections.get(registryID);
			if (allConnections == null) {
				return;
			}
			//remove the connection
			allConnections.remove(connection);
			onConnectionRemoved(registryID, connection);
			
			int newID = getNextAvailableID();
			allConnections = Lists.newArrayList(allConnections); // save all the current cables.
			connections.get(registryID).clear();
			connections.remove(registryID); // clear all cables currently connected

			List<Integer> newNetworks = Lists.newArrayList();
			allConnections.forEach(oldCable -> oldCable.setRegistryID(-1));
			allConnections.forEach(oldCable -> {
				oldCable.addConnection();
				newNetworks.add(oldCable.getRegistryID());
			});
			onNetworksDisconnected(newNetworks);
			//TODO add an after?
			
		}
	}

	public void refreshConnections(T cable) {
		BlockCoords coords = cable.getCoords();
		for (EnumFacing dir : EnumFacing.values()) {
			Pair<ConnectableType, Integer> connection = getConnectionType(cable, coords.getWorld(), coords.getBlockPos(), dir, cable.getConnectableType());
			boolean canConnect = cable.canConnectOnSide(cable.getRegistryID(), dir, false);
			if ((!canConnect && connection.a.canConnect(cable.getConnectableType()))) {
				cable.removeConnection();
				cable.addConnection();
			} else if ((canConnect && connection.a.canConnect(cable.getConnectableType()) && connection.b != cable.getRegistryID())) {
				connectNetworks(cable.getRegistryID(), connection.b);
			}
		}
	}

	public abstract Pair<ConnectableType, Integer> getConnectionType(T source, World world, BlockPos pos, EnumFacing dir, ConnectableType cableType);

	public void connectNetworks(int newID, int secondaryID) {
		List<T> oldConnections = connections.get(secondaryID);
		if (oldConnections == null) {
			return;
		}
		addConnections(newID, oldConnections);
		oldConnections.clear();
		connections.remove(secondaryID);
		this.onNetworksConnected(newID, secondaryID);
	}

	public abstract void onNetworksDisconnected(List<Integer> newNetworks);
	
	public abstract void onNetworksConnected(int newID, int oldID);

	public abstract void onConnectionAdded(int registryID, T added);

	public abstract void onConnectionRemoved(int registryID, T added);

	//public abstract void afterConnectionRemoved(int registryID, T added);

}