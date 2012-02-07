package org.geodelivery.jap.charact;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.operation.linemerge.LineMergeGraph;
import com.vividsolutions.jts.planargraph.DirectedEdge;
import com.vividsolutions.jts.planargraph.DirectedEdgeStar;
import com.vividsolutions.jts.planargraph.Edge;
import com.vividsolutions.jts.planargraph.Node;
import com.vividsolutions.jts.planargraph.PlanarGraph;

/**
 * Concave hull algorithm by Pimin Konstantin Kefaloukos and 
 * Elias Lšfgren
 * @author Pimin Konstantin Kefaloukos
 *
 */
/**
 * @author kostas
 *
 */
public class ConcaveHull extends Characterizer {
	
	// useful links:
	// http://en.wikipedia.org/wiki/File:Degree-Radian_Conversion.svg
	
	private int _numExposed;
	
	public ConcaveHull(Geometry geom) {
		super(geom);
	}
	
	/**
	 * @param compression
	 * @return
	 */
	public Geometry getConcaveHull(int compression) {

		
		// marked node means "exposed"
		// marked edge means "deleted"


		LineMergeGraph graph = getGraph();

		long t0 = System.currentTimeMillis();
		// Variables
		Node start, from, to;
		DirectedEdge successorEdge, inversePredecessorEdge, longestEdge;
		double longest;

		// Initialize
		start = findStart(graph); // use "go west" algorithm
		longestEdge = null; // will be updated in loop 4 sure!
		longest = Double.MIN_VALUE;
		from = start; // first node of perimeter
	
		
		// Special fake node/ege, to trick DirectedEdgeStar.getNextCWEdge
		Node fakeNode = new Node(new Coordinate(start.getCoordinate().x - 10, start.getCoordinate().y));
		DirectedEdge fakeEdge = new DirectedEdge(start, fakeNode, fakeNode.getCoordinate(), true);
		start.addOutEdge(fakeEdge); // edge pointing "west", used to trick the nextEdgeCW function

		inversePredecessorEdge = fakeEdge;
		successorEdge = nextEdgeCW(from, inversePredecessorEdge); // find next edge and perimeter node

		// no longer need fakeEdge, delete it again, phew, the hoops you have to jump through sometimes..
		start.remove(fakeEdge);

		// do full clock wise roundtrip of perimeter
		// mark nodes and find longest edge
		do {

			// find new "to" node
			to = successorEdge.getToNode();

			// update stuff 
			double len = from.getCoordinate().distance(to.getCoordinate());
			if(len > longest) {
				longest = len;
				longestEdge = successorEdge;
			}
			to.setMarked(true);
			_numExposed++; // update number of exposed nodes
			from.setData(to); // set successor

			// move "from" to new position
			inversePredecessorEdge = successorEdge.getEdge().getDirEdge(to);
			from = to;
			successorEdge = nextEdgeCW(from, inversePredecessorEdge); // find next edge and perimeter node
		} 
		while(from != start);
		
		// now a full perimeter has been marked
		// begin deletion
		// jump to longest perimeter edge
		successorEdge = longestEdge;
		from = longestEdge.getFromNode();
		to = (Node) from.getData();
		inversePredecessorEdge = successorEdge.getEdge().getDirEdge(to);

		start = from;
		to = (Node) from.getData();
		
		// TODO: the deletion process
		Geometry result = toPolygon(start);
		System.out.println("getConcaveHull():" + (System.currentTimeMillis() - t0));
		return result;
	}
	
	private void markDeleted(DirectedEdge edge) {
		edge.setMarked(true);
		Edge parent = edge.getEdge();
		if(parent != null) {
			parent.setMarked(true);
			DirectedEdge other = parent.getDirEdge(edge.getToNode());
			if(other != null) {
				other.setMarked(true);
			}
		}
	}

	/**
	 * For angle in radians, return inverse angle (+/- Math.PI)
	 * @param angle
	 * @return
	 */
	private double inverse(double angle) {
		if(angle > 0) {
			return angle - Math.PI;
		}
		else {
			return angle + Math.PI;
		}
	}

	
	/**
	 * Distance in radians in clockwise direction
	 * @param radA
	 * @param radB
	 * @return
	 */
	private double distCW(double radA, double radB) {
		if(radA < radB) {
			return Math.PI * 2 - (radB - radA);
		}
		else {
			return radA - radB;
		}
	}
	
	/**
	 * "Robot-arm" perimeter crawling step. Btw, there is a next cw edge in DirectedEdgeStar, but doesn't fit with what we have
	 * @param node
	 * @param inverseAngle
	 * @return
	 */
	private DirectedEdge nextEdgeCW(Node node, DirectedEdge predecessor) {
		// pick the out-edge of node, that is closest in clock-wise direction from inverseAngle
		DirectedEdgeStar star = node.getOutEdges();
		while(true) {
			DirectedEdge successor = star.getNextCWEdge(predecessor);
			// skip edges marked as deleted
			if(!successor.isMarked()) {
				return successor;
			}
			predecessor = successor;
		}
	}
	
	/**
	 * Algorithm for finding an exposed node in a triangulation graph
	 * @param graph
	 * @return
	 */
	private Node findStart(PlanarGraph graph) {
		// going west means going towards smaller values of x
		Node best = (Node) graph.nodeIterator().next();
		double mostWest = best.getCoordinate().x;
		DirectedEdgeStar star = best.getOutEdges();
		// move west for as long as possible
		while(true) {
			boolean improved = false;
			// examine outedges for node more west
			for(Object o : star.getEdges()) {
				DirectedEdge dirEdge = (DirectedEdge) o;
				Node to = dirEdge.getToNode();
				double toX = to.getCoordinate().x;
				if(toX < mostWest) {
					mostWest = toX;
					best = to;
					improved = true;
				}
			}
			if(!improved) {
				break;
			}
		}
		return best;
	}
	
	private Geometry toPolygon(Node node) {
		// the "data" of each perimeter node points to successor node
		// number of exposed nodes recorded in _numExposed
		Coordinate[] shell = new Coordinate[_numExposed+1];
		// crawl perimeter
		for(int i=0; i<_numExposed; i++) {
			shell[i] = node.getCoordinate();
			node = (Node) node.getData();
		}
		shell[_numExposed] = shell[0];
		GeometryFactory fact = new GeometryFactory();
		// return just the concave hull
		return fact.createPolygon(fact.createLinearRing(shell), null);
	}
	
	// TODO: Below
	
	private boolean canDelete(DirectedEdge e) {
		// rule 1: nodes must have 3+ degree
		// rule 2: opposing node must be un-exposed
		return rule1(e.getFromNode()) 
		&& rule1(e.getToNode()) 
		&& rule2(!isExposed(getOpp(e))); 

	}

	// cannot delete edge, if either node has degree 2
	private boolean rule1(Node fromNode) {
		// TODO Auto-generated method stub
		return false;
	}
	
	// cannot delete edge, if opposed node
	private boolean rule2(boolean b) {
		// TODO Auto-generated method stub
		return false;
	}

	private Node getOpp(DirectedEdge e) {
		return null;
	}

	private boolean isExposed(Node n) {
		// if node has data, it is exposed
		if(n.isMarked()) {
			return true;
		}
		// else if it has two exposed out edges 
		int exposedOutEdges = 0;

		DirectedEdgeStar star = n.getOutEdges();
		// count exposed out edges
		for(Object o : star.getEdges()) {
			DirectedEdge dirEdge = (DirectedEdge) o;
			if(isExposed(dirEdge)) {
				exposedOutEdges++;
			}
		}
		return exposedOutEdges == 2;
	}
	
	private boolean isExposed(DirectedEdge e) {
		return false;
	}

}
