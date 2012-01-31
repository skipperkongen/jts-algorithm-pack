package org.geodelivery.jap;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

/**
 * This algorithm computes the alpha shape of a Geometry. 
 * See http://www.cgal.org/Manual/latest/doc_html/cgal_manual/Alpha_shapes_2/Chapter_main.html
 * for more details.<br/>
 * The code was produced by the Geodelivery project. Geodelivery is the name of an industrial Ph.d. project at Department of Computer Science, 
 * University of Copenhagen (http://www.diku.dk). The industrial partners of the project are Grontmij 
 * (http://www.grontmij.dk) and National Survey and Cadastre (http://www.kms.dk).
 * @author Pimin Konstantin Kefaloukos (kostas@geodelivery.org)
 */
public class AlphaShape extends TriangulationBasedAlgorithm {	
	
	private Geometry _triangles = null;
	
	/**
	 * @param geom The Geometry object to operate on
	 */
	public AlphaShape(Geometry geom) {
		super(geom);
	}
	
	/**
	 * @param alpha radius of circumcirle for triangles to remove from geometry
	 * @return A new geometry, that is the alpha shape of the geometry.
	 */
	public Geometry getAlphaShape(double alpha) {
		
		// compute triangles, if not computed before

		if(_triangles == null) {
			this._triangles = triangulator.getTriangles(new GeometryFactory());
		}

		Geometry[] partition = new Geometry[_triangles.getNumGeometries()];
		int head = 0;
		int tail = partition.length-1;

		// compute circumcircle radius for each triangle, and place in divided
		for(int i=0; i<_triangles.getNumGeometries(); i++) {
			Polygon t = (Polygon) _triangles.getGeometryN(i);
			// set radius as user data
			double r = triangleCircumcirleRadius(t);
			if(r > alpha) {
				// triangles to throw away go in tail
				partition[tail--] = t;				
			}
			else {
				// triangles to keep go in head
				partition[head++] = t;
			}
		}
		Geometry[] keep = new Geometry[head];
		System.arraycopy(partition, 0, keep, 0, head);
		Geometry result = new GeometryFactory().createGeometryCollection(keep).union();
		
		return result;
	}
		
	private double triangleCircumcirleRadius(Polygon triangle) {
		// formula from http://www.mathopenref.com/trianglecircumcircle.html
		Coordinate[] coords = triangle.getCoordinates();
		double a = coords[0].distance(coords[1]);
		double b = coords[1].distance(coords[2]);
		double c = coords[2].distance(coords[0]);
			
		return (a*b*c/(Math.sqrt((a+b+c)*(b+c-a)*(c+a-b)*(a+b-c))));
	}

}
