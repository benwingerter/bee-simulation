package BeeSimulation.lib;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import BeeSimulation.agents.Hive;
import repast.simphony.context.Context;
import repast.simphony.data2.NonAggregateDataSource;
import repast.simphony.util.ContextUtils;

public class CollectionRateDataSource implements NonAggregateDataSource {
	
	Map<Long, int[]> map = new HashMap<>();

	@Override
	public String getId() {
		return "Nectar Collection Rate";
	}

	@Override
	public Class<Double> getDataType() {
		return Double.class;
	}

	@Override
	public Class<Hive> getSourceType() {
		return Hive.class;
	}

	@Override
	public Object get(Object obj) {
		// TODO Auto-generated method stub
		var hive = (Hive) obj;
		long pop = hive.beeCount();
		if(map.containsKey(pop)) {
//			int[] value = {1,2};
//			map.put(pop, value);
		} else {
			int[] value = {1,2};
			map.put(pop, value);
		}
		
		return 3;
	}

}
