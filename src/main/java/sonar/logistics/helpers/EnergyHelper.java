package sonar.logistics.helpers;

import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import sonar.core.SonarCore;
import sonar.core.api.energy.EnergyType;
import sonar.core.api.energy.ISonarEnergyHandler;
import sonar.core.api.energy.StoredEnergyStack;
import sonar.core.helpers.SonarHelper;
import sonar.core.utils.SortingDirection;
import sonar.logistics.api.tiles.readers.EnergyReader.SortingType;
import sonar.logistics.api.wrappers.EnergyWrapper;
import sonar.logistics.info.types.MonitoredEnergyStack;

public class EnergyHelper extends EnergyWrapper {

	public List<ISonarEnergyHandler> getProviders(EnergyType type) {
		List<ISonarEnergyHandler> providers = Lists.newArrayList();
		List<ISonarEnergyHandler> handlers = SonarCore.energyHandlers;
		for (ISonarEnergyHandler provider : handlers) {
			if (provider.getProvidedType().getName().equals(type.getName())) {
				providers.add(provider);
			}
		}
		return providers;
	}

	public static void sortEnergyList(List<MonitoredEnergyStack> info, final SortingDirection dir, SortingType type) {
		info.sort(new Comparator<MonitoredEnergyStack>() {
			public int compare(MonitoredEnergyStack str1, MonitoredEnergyStack str2) {
				StoredEnergyStack item1 = str1.getEnergyStack(), item2 = str2.getEnergyStack();
				switch (type) {
				case CAPACITY:
					return SonarHelper.compareWithDirection(item1.capacity, item2.capacity, dir);
				case INPUT:
					return SonarHelper.compareWithDirection(item1.input, item2.input, dir);
				case NAME:
					String modid1 = str1.getMonitoredCoords().getUnlocalizedName();
					String modid2 = str2.getMonitoredCoords().getUnlocalizedName();
					return SonarHelper.compareStringsWithDirection(modid1, modid2, dir);
				case STORED:
					return SonarHelper.compareWithDirection(item1.stored, item2.stored, dir);
				case TYPE:
					return SonarHelper.compareStringsWithDirection(item1.energyType.getName(), item2.energyType.getName(), dir);
				}
				return 0;
			}
		});
	}
}
