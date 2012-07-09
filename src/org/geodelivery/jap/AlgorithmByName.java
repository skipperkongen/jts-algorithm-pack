package org.geodelivery.jap;

import org.geodelivery.jap.algorithms.ConcaveHull;

public class AlgorithmByName {

	public static GeometryToGeometry getGeometryToGeometryByName(String name) {
		if(name.equalsIgnoreCase("concavehull")){
			return new ConcaveHull();
		}
		else {
			return null;
		}
	}
}
