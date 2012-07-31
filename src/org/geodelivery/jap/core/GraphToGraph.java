package org.geodelivery.jap.core;

import com.vividsolutions.jts.planargraph.PlanarGraph;

public interface GraphToGraph {
	public PlanarGraph computeGraph(PlanarGraph graph);
}
