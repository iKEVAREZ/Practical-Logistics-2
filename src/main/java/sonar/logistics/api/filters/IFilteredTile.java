package sonar.logistics.api.filters;

import sonar.core.helpers.InventoryHelper.IInventoryFilter;
import sonar.core.listener.PlayerListener;
import sonar.logistics.api.tiles.IChannelledTile;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.network.sync.SyncFilterList;

public interface IFilteredTile extends IChannelledTile, ILogicListenable<PlayerListener>, IInventoryFilter {

	public SyncFilterList getFilters();
}
