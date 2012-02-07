package org.geodelivery.jap.charact;

import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.operation.linemerge.LineMergeGraph;
import com.vividsolutions.jts.planargraph.PlanarGraph;
import com.vividsolutions.jts.triangulate.DelaunayTriangulationBuilder;
import com.vividsolutions.jts.triangulate.quadedge.QuadEdgeSubdivision;

public abstract class Characterizer {
	
	private DelaunayTriangulationBuilder _triangulator;
	private Geometry _geometry;

	public Characterizer(Geometry geom) {
		super();
		_triangulator = new DelaunayTriangulationBuilder();
		_triangulator.setSites(geom);
		_geometry = geom;
	}

	protected Geometry getOriginalGeometry() {
		return _geometry;
	}

	protected Geometry getTriangles() {
		return _triangulator.getTriangles(new GeometryFactory());
	}

	protected Geometry getEdges() {
		long t0 = System.currentTimeMillis();
		Geometry edges = _triangulator.getEdges(new GeometryFactory());
		System.out.println("getEdges():" + (System.currentTimeMillis() - t0));
		return edges;
	}

	protected Geometry getConvexHull() {
		ConvexHull hull = new ConvexHull(_geometry);
		return hull.getConvexHull();
	}

	protected PlanarGraph getGraph() {


		Geometry edges = getEdges();

		long t0 = System.currentTimeMillis();
		LineMergeGraph graph = new LineMergeGraph();
		for (int i = 0; i < edges.getNumGeometries(); i++) {
			LineString ls = (LineString) edges.getGeometryN(i);
			graph.addEdge(ls);
		}
		System.out.println("getGraph():" + (System.currentTimeMillis() - t0));
		return graph;
	}

	protected QuadEdgeSubdivision getQuadEdgeSubDivision() {
		return _triangulator.getSubdivision();
	}
	
}
