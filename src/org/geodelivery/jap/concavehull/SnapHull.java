package org.geodelivery.jap.concavehull;

import java.util.ArrayList;

import org.geodelivery.jap.core.Transform;

import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.strtree.GeometryItemDistance;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * @author Pimin Konstantin Kefaloukos
 * Java implementation of ST_ConcaveHull in PostGis 2.0
 */
public class SnapHull implements Transform<Geometry, Geometry> {

	@Override
	public Geometry transform(Geometry src) {
		// TODO Auto-generated method stub
		GeometryFactory gf = new GeometryFactory();
		Coordinate[] coordinates = src.getCoordinates();
		ConvexHull ch = new ConvexHull(coordinates, gf);
		Geometry geometry = ch.getConvexHull();
		
		if(geometry instanceof Polygon) {  
			// get the exterior ring
			LineString vexring = ((Polygon)geometry).getExteriorRing();
			int numVertices = (int) Math.min(vexring.getNumPoints()*2, 1000);
			Coordinate[] result = new Coordinate[numVertices]; // upperbound on verts on boundary
			int bindex = 0;
			double seglength = vexring.getLength()/numVertices;
			vexring = segmentize(vexring, seglength);
			// build index of points
			STRtree index = new STRtree();
			for(Coordinate c : coordinates) {
				index.insert(new Envelope(c), c);
			}
			index.build();
			Coordinate previous = null;
			for (Coordinate c : vexring.getCoordinates()) {
				Envelope env = new Envelope(c);
				// insert into index
				index.insert(env, c);
				index.build();
				Coordinate nearest = (Coordinate) index.nearestNeighbour(env, c, new GeometryItemDistance());
				if(nearest != previous) {
					result[bindex++] = nearest;
					previous = nearest;
				}
				// remove from index
				index.remove(env, c);
			}
			Coordinate[] shell = new Coordinate[bindex];
			System.arraycopy(result, 0, shell, 0, bindex);
			return gf.createPolygon(gf.createLinearRing(shell), null);
		}
		else {
			return geometry; // linestring, point or empty
		}
	}

	private LineString segmentize(LineString vexring, double seglength) {
		// TODO Auto-generated method stub
		Coordinate[] vcoords = vexring.getCoordinates();
		GeometryFactory gf = new GeometryFactory();
		ArrayList<Coordinate> ext = new ArrayList<Coordinate>();
		for(int i=0; i<vcoords.length-1; i++) {
			ext.add(vcoords[i]);
			double dist = vcoords[i].distance(vcoords[i+1]);
			double numSegments = Math.ceil(dist / seglength);
			double actLength = dist / numSegments;
			double factor = actLength / dist;
			double vectorX = factor * (vcoords[i+1].x - vcoords[i].x);
			double vectorY = factor * (vcoords[i+1].y - vcoords[i].y);
			Coordinate ins = vcoords[0];
			while(numSegments > 1) {
				ins.x = ins.x + vectorX;
				ins.y = ins.y + vectorY;
				ext.add(ins);
				numSegments--;
			}
			ext.add(vcoords[i+1]);
		}
		return gf.createLinearRing( ext.toArray(new Coordinate[ext.size()]) );
	}

}
