package org.geodelivery.jap.charact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.operation.linemerge.LineMergeGraph;
import com.vividsolutions.jts.planargraph.DirectedEdge;
import com.vividsolutions.jts.planargraph.Edge;
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

	protected LineMergeGraph getGraph() {


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
	
	protected PlanarGraph getMinimumSpanningTree() {
		int treeId = Integer.MIN_VALUE;
		
		PlanarGraph graph = getGraph();

		ArrayList<Edge> sorted = new ArrayList<Edge>(graph.getEdges());
		
		Collections.sort(sorted, new Comparator<Edge>() {

			@Override
			public int compare(Edge e0, Edge e1) {
				if(e0.getData() == null) {
					Object[] data = new Object[3];
					data[0] = null;
					data[1] = null;
					data[2] = null;
					DirectedEdge dirEdge = e0.getDirEdge(0);
					dirEdge.getFromNode();
					e0.setData(data);
				}
				// TODO: Implement
				return 0;
			}
			
		});
		// sort edges by length
		for(Edge edge : sorted) {
			DirectedEdge dirEdge = edge.getDirEdge(0);
			dirEdge.getFromNode();
		}
		return null;
	}
}
