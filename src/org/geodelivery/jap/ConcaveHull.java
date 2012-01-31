package org.geodelivery.jap;

import com.vividsolutions.jts.geom.Geometry;

public class ConcaveHull extends TriangulationBasedAlgorithm {

	public ConcaveHull(Geometry geom) {
		super(geom);
	}
	
	public enum Variation {
		MST,
		ALL,
		ABS,
		PERCENT
	}

}
