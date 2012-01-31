package org.geodelivery.jap;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.triangulate.DelaunayTriangulationBuilder;

public abstract class TriangulationBasedAlgorithm {

	protected DelaunayTriangulationBuilder triangulator;
	protected Geometry geometry;
	
	public TriangulationBasedAlgorithm(Geometry geom) {
		super();
		DelaunayTriangulationBuilder triangulator = new DelaunayTriangulationBuilder();
		triangulator.setSites(geom);
		this.geometry = geom;
	}
}
