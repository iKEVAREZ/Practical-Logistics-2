package sonar.logistics.logic.comparators;

import java.util.List;

import sonar.core.api.IRegistryObject;
import sonar.logistics.api.tiles.signaller.LogicOperator;
import sonar.logistics.api.tiles.signaller.LogicState;

public interface ILogicComparator<T> extends IRegistryObject {

	public LogicState getLogicState(LogicOperator operator, T info, T object);

	public List<LogicOperator> getValidOperators();
	
	public boolean isValidObject(Object obj);

}
