package org.geodelivery.jap;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.planargraph.PlanarGraph;

public interface GeometryToGraph {

	public PlanarGraph computeGraph(Geometry geom);
}