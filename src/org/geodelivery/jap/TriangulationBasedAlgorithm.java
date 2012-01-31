package org.geodelivery.jap;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.operation.linemerge.LineMergeGraph;
import com.vividsolutions.jts.planargraph.PlanarGraph;
import com.vividsolutions.jts.triangulate.DelaunayTriangulationBuilder;

public abstract class TriangulationBasedAlgorithm {

	private Geometry _triangles = null;
	private Geometry _edges = null;
	private LineMergeGraph _graph;
	private DelaunayTriangulationBuilder _triangulator;
	private Geometry _geometry;

	
	public TriangulationBasedAlgorithm(Geometry geom) {
		super();
		_triangulator = new DelaunayTriangulationBuilder();
		_triangulator.setSites(geom);
		_geometry = geom;
	}
	
	protected Geometry getOriginalGeometry() {
		return _geometry;
	}
	
	protected Geometry getTriangles() {
		if(_triangles == null) {
			_triangles = _triangulator.getTriangles(new GeometryFactory());
		}
		return _triangles;
	}
	
	protected Geometry getEdges() {
		if(_edges == null) {
			_edges = _triangulator.getEdges(new GeometryFactory());
		}
		return _edges;
	}
	
	protected PlanarGraph getGraph() {
		if(_graph == null) {
			_graph = new LineMergeGraph();
			Geometry edges = getEdges();
			for(int i=0; i<edges.getNumGeometries(); i++) {
				LineString ls = (LineString) edges.getGeometryN(i);
				_graph.addEdge(ls);
			}
		}
		return _graph;
	}
}
