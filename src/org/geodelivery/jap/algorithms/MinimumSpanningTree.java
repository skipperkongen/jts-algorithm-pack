package org.geodelivery.jap.algorithms;

import org.geodelivery.jap.GeometryToGraph;
import org.geodelivery.jap.GraphToGraph;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.operation.linemerge.LineMergeGraph;
import com.vividsolutions.jts.planargraph.Edge;
import com.vividsolutions.jts.planargraph.PlanarGraph;

public class MinimumSpanningTree implements GeometryToGraph, GraphToGraph {

	@Override
	public PlanarGraph computeGraph(Geometry geom) {
		DelaunayGraph dg = new DelaunayGraph();
		PlanarGraph pg = dg.computeGraph(geom);
		return computeGraph(pg);


	}

	@Override
	public PlanarGraph computeGraph(PlanarGraph graph) {
		SimpleWeightedGraph<Coordinate, DefaultWeightedEdge> swg = 
				new SimpleWeightedGraph<Coordinate, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		// Add edges from delaunay graph
		for(Object obj : graph.getEdges()) {
			Edge edge = (Edge) obj;
			Coordinate c1 = edge.getDirEdge(0).getFromNode().getCoordinate();
			Coordinate c2 = edge.getDirEdge(0).getToNode().getCoordinate();
			swg.addVertex(c1);
			swg.addVertex(c2);
			double weight = c1.distance(c2);
			DefaultWeightedEdge wedge = swg.addEdge(c1, c2);
			swg.setEdgeWeight(wedge, weight);			
		}
		KruskalMinimumSpanningTree<Coordinate, DefaultWeightedEdge> kruskal = 
				new KruskalMinimumSpanningTree<Coordinate, DefaultWeightedEdge>( swg );

		// Copy to JTS graph (LineMergeGraph)
		LineMergeGraph lmg = new LineMergeGraph();
		GeometryFactory fact = new GeometryFactory();

		for (DefaultWeightedEdge edge : kruskal.getEdgeSet()) {
			Coordinate c1 = swg.getEdgeSource(edge);
			Coordinate c2 = swg.getEdgeTarget(edge);
			Coordinate[] cs = new Coordinate[] {c1, c2};
			lmg.addEdge(fact.createLineString(cs));
		}
		return lmg;
	}
	

}
