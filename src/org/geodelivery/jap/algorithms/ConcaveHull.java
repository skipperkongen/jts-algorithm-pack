package org.geodelivery.jap.algorithms;

import org.geodelivery.jap.GeometryToGeometry;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.planargraph.DirectedEdge;
import com.vividsolutions.jts.planargraph.DirectedEdgeStar;
import com.vividsolutions.jts.planargraph.Edge;
import com.vividsolutions.jts.planargraph.Node;
import com.vividsolutions.jts.planargraph.PlanarGraph;

/**
 * Concave hull algorithm by Pimin Konstantin Kefaloukos and 
 * Elias Lšfgren.
 * Future optimizations: store successor edge instead of successor node in exposed nodes setData()
 * @author Pimin Konstantin Kefaloukos
 */
public class ConcaveHull implements GeometryToGeometry {
	
	// useful links:
	// http://en.wikipedia.org/wiki/File:Degree-Radian_Conversion.svg	
	public ConcaveHull() {
		super();
	}
	
	/**
	 * @param compression
	 * @return
	 */
	@Override
	public Geometry computeGeometry(Geometry geom) {
		
		int compression = 42; // decide a good value for this one
		int numExposed = 0;
		
		// marked node means "exposed"
		// marked edge means "deleted"
		DelaunayGraph delaunay = new DelaunayGraph();
		PlanarGraph graph = delaunay.computeGraph(geom);

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
		successorEdge = nextEdgeCW(inversePredecessorEdge); // find next edge and perimeter node
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
			numExposed++; // update number of exposed nodes
			from.setData(successorEdge); // set successor

			// move "from" to new position
			inversePredecessorEdge = successorEdge.getEdge().getDirEdge(to);
			from = to;
			successorEdge = nextEdgeCW(inversePredecessorEdge); // find next edge and perimeter node
		} 
		while(from != start);
		
		// now a full perimeter has been marked
		// begin deletion
		// jump to longest perimeter edge
		successorEdge = longestEdge;
		start = successorEdge.getFromNode();
		from = start;

		while(compression > 0) {

			boolean hasDeleted = false;

			// do a lap around the perimeter
			// delete edges that are already exposed, but not edges that become exposed
			do {
				from = successorEdge.getFromNode();
				to = successorEdge.getToNode();

				// compute triangle edges
				DirectedEdge triangleEdge1 = nextEdgeCW(successorEdge);
				Node triangleNode = triangleEdge1.getToNode();
				DirectedEdge inv = triangleEdge1.getEdge().getDirEdge(triangleNode);
				DirectedEdge triangleEdge2 = nextEdgeCW(inv);

				// can successor edge be deleted?
				// rule 1: from and to have degree > 2
				// rule 2: opposite node in CW triangle is not exposed
				boolean rule1 = getDegree(successorEdge.getFromNode()) > 2 
				&& getDegree(successorEdge.getToNode()) > 2;
				boolean rule2 = !triangleNode.isMarked();
				
				if(rule1 && rule2) {
					// delete edge and replace with two triangle edges
					from.setData(triangleEdge1);
					triangleNode.setData(triangleEdge2);
					markDeleted(successorEdge);
					triangleNode.setMarked(true);
					numExposed++;
					hasDeleted = true;
				}
				successorEdge = (DirectedEdge) to.getData();
				from = to;
				
			} while (from != start);
			
			if(!hasDeleted) {
				break;
			}
			
			compression--;
		}
		
		Geometry result = toPolygon(start, numExposed);
		return result;
	}
	
	/**
	 * Ignore edges marked as deleted when measuring degree of node
	 * @param node
	 * @return
	 */
	private int getDegree(Node node) {
		int degree = 0;
		for(Object obj : node.getOutEdges().getEdges()) {
			DirectedEdge e = (DirectedEdge) obj;
			degree += e.isMarked() ? 0 : 1;
		}
		return degree;
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
	 * "Robot-arm" perimeter crawling step.
	 * Ignores edges in edge star marked as deleted
	 * @param node
	 * @param inverseAngle
	 * @return
	 */
	private DirectedEdge nextEdgeCW(DirectedEdge outEdge) {
		// pick the out-edge of node, that is closest in clock-wise direction from inverseAngle
		Node node = outEdge.getFromNode();
		DirectedEdgeStar star = node.getOutEdges();
		DirectedEdge successor = star.getNextCWEdge(outEdge);
		while(successor.isMarked()) {
			outEdge = successor;
			successor = star.getNextCWEdge(outEdge);
		}
		return successor;
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
	
	private Geometry toPolygon(Node node, int numExposed) {
		// the "data" of each perimeter node is a successor edge
		// number of exposed nodes recorded in _numExposed
		Coordinate[] shell = new Coordinate[numExposed+1];
		// crawl perimeter
		for(int i=0; i<numExposed; i++) {
			shell[i] = node.getCoordinate();
			DirectedEdge successorEdge = (DirectedEdge) node.getData();
			node = successorEdge.getToNode();
		}
		shell[numExposed] = shell[0];
		GeometryFactory fact = new GeometryFactory();
		// return just the concave hull
		return fact.createPolygon(fact.createLinearRing(shell), null);
	}
}
