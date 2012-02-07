package org.geodelivery.jap.test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.operation.linemerge.LineMergeGraph;
import com.vividsolutions.jts.planargraph.DirectedEdge;
import com.vividsolutions.jts.planargraph.DirectedEdgeStar;
import com.vividsolutions.jts.planargraph.Node;

public class Test3 {
	public static void main(String[] args) {
		Coordinate a = new Coordinate(0,0);
		Coordinate b = new Coordinate(1,1);
		Coordinate c = new Coordinate(2,0);
		Coordinate d = new Coordinate(1,-1);
		GeometryFactory fact = new GeometryFactory();
		LineMergeGraph graph = new LineMergeGraph();
		graph.addEdge(fact.createLineString(new Coordinate[]{a,b}));
		graph.addEdge(fact.createLineString(new Coordinate[]{b,c}));
		graph.addEdge(fact.createLineString(new Coordinate[]{c,a}));
		graph.addEdge(fact.createLineString(new Coordinate[]{a,d}));
		graph.addEdge(fact.createLineString(new Coordinate[]{d,c}));
		Node nA = graph.findNode(a);
		nA.setData("A");
		Node nB = graph.findNode(b);
		nB.setData("B");
		Node nC = graph.findNode(c);
		nC.setData("C");
		Node nD = graph.findNode(d);
		nD.setData("D");
		for(Object objNode : graph.getNodes()) {
			Node node = (Node) objNode;
			System.out.println(node.getData());
			DirectedEdgeStar star = node.getOutEdges();
			for(Object objEdge : star.getEdges()) {
				DirectedEdge de = (DirectedEdge) objEdge;
				double angle = de.getAngle();
				double dist = distCW(Math.PI, angle);
				System.out.println("\t" + de.getFromNode().getData() + " -> " + de.getToNode().getData() + ": " + angle + ", " + dist);
				
			}			
		}

	}
	
	public static double distCW(double radA, double radB) {
		if(radA < radB) {
			return Math.PI * 2 - (radB - radA);
		}
		else {
			return radA - radB;
		}
	}
}
