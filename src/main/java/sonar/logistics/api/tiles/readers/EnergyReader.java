package sonar.logistics.api.tiles.readers;

/**all the modes used by the Fluid Reader*/
public class EnergyReader {
	
	public static enum Modes {
		STORAGES, STORAGE, TOTAL;
		
		public String getDescription() {
			switch (this) {
			case STORAGES:
				return "All connected storages";
			case STORAGE:
				return "The selected storage";
			case TOTAL:
				return "A total of all the storages";
			default:
				return "ERROR";
			}
		}

		public String getName() {
			switch (this) {
			case STORAGES:
				return "Storages";
			case STORAGE:
				return "Storage";
			case TOTAL:
				return "Total";
			default:
				return "ERROR";
			}
		}
	}
	
	public static enum SortingType {
		STORED, CAPACITY, INPUT, TYPE, NAME;
		
		public String getTypeName() {
			switch(this){
			case CAPACITY:
				return "Energy Capacity";
			case INPUT:
				return "Energy Input";
			case NAME:
				return "Block Name";
			case STORED:
				return "Energy Stored";
			case TYPE:
				return "Energy Type";
			default:
				return "ERROR";
			
			}
		}
	}
}
