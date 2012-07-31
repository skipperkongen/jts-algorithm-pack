package org.geodelivery.jap.concavehull;

import java.util.ArrayList;
import java.util.List;

import org.geodelivery.jap.core.Transform;

import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;

/**
 * @author Pimin Konstantin Kefaloukos
 * Java implementation of ST_ConcaveHull in PostGis 2.0
 */
public class SnapHull implements Transform<Geometry, Geometry> {
	
	public final static double START_RANGE_RELATIVE = 0.03;
	public final static int MAX_HULL_POINTS = 2000;
	public final static int HULL_SEGMENT_FACTOR = 4;
	//public final static double BUFFER_PCT = 0.05;
	
	@Override
	public Geometry transform(Geometry src) {
		// TODO Auto-generated method stub
		GeometryFactory gf = new GeometryFactory();
		Envelope srcEnv = src.getEnvelopeInternal();
		double srcDim = (srcEnv.getHeight() + srcEnv.getWidth()) / 2;
		double startRange = START_RANGE_RELATIVE * srcDim;
		Coordinate[] coordinates = src.getCoordinates();
		ConvexHull ch = new ConvexHull(coordinates, gf);
		Geometry geometry = ch.getConvexHull();
		//return geometry;
		if(geometry instanceof Polygon) {  
			// get the exterior ring
			LineString vexring = ((Polygon)geometry).getExteriorRing();
			int numVertices = (int) Math.min(vexring.getNumPoints()*HULL_SEGMENT_FACTOR, MAX_HULL_POINTS);
			double seglength = vexring.getLength()/numVertices;
			vexring = segmentize(vexring, seglength);
			Coordinate[] result = new Coordinate[vexring.getNumPoints()]; // upperbound on verts on boundary
			int bindex = 0;
			//return vexring;
			// build index of points
			STRtree index = new STRtree();
			for(Coordinate c : coordinates) {
				index.insert(new Envelope(c), c);
			}
			index.build();
			Coordinate previous = null;
			for (Coordinate c : vexring.getCoordinates()) {
				// This proceduce creates invalid polygons. Find better solution.
				Coordinate nearest = findNearest(c, startRange, srcDim, index);
				if(nearest != previous) {
					result[bindex++] = nearest;
					previous = nearest;
				}
			}
			Coordinate[] shell = new Coordinate[bindex];
			System.arraycopy(result, 0, shell, 0, bindex);
			Geometry p = gf.createPolygon(gf.createLinearRing(shell), null);
			//p.buffer(srcDim*BUFFER_PCT);
			//p.buffer(-10);
			if(!p.isValid()) {
				DouglasPeuckerSimplifier simp = new DouglasPeuckerSimplifier(p);
				p = simp.getResultGeometry();
			}
			System.out.println("Valid: " + p.isValid());
			return p;
		}
		else {
			return geometry; // linestring, point or empty
		}
	}

	private Coordinate findNearest(Coordinate qc, double range, double maxRange, STRtree index) {
		while(range < maxRange) {
			Envelope searchEnv = new Envelope(qc.x - range, qc.x + range, qc.y - range, qc.y + range);
			@SuppressWarnings("rawtypes")
			List hits = index.query(searchEnv);
			if(hits.isEmpty()) {
				range *= 2;
			}
			else {
				Coordinate best = null;
				double bestDist = Double.MAX_VALUE;
					
				for(Object obj : hits) {
					Coordinate hit = (Coordinate) obj;
					double dist = qc.distance(hit);
					if(dist < bestDist) {
						bestDist = dist;
						best = hit;
					}
				}
				return best;
			}
		}
		return null;
	}

	private LineString segmentize(LineString vexring, double seglength) {
		// TODO Auto-generated method stub
		Coordinate[] vcoords = vexring.getCoordinates();
		GeometryFactory gf = new GeometryFactory();
		ArrayList<Coordinate> ext = new ArrayList<Coordinate>();
		for(int i=0; i<vcoords.length-1; i++) {
			ext.add(vcoords[i]);
			// start debug
			double dist = vcoords[i].distance(vcoords[i+1]); // OK
			double numSegments = Math.ceil(dist / seglength); // OK
			double actLength = dist / numSegments; // OK
			double factor = actLength / dist; 
			double vectorX = factor * (vcoords[i+1].x - vcoords[i].x);
			double vectorY = factor * (vcoords[i+1].y - vcoords[i].y);
			Coordinate ins = vcoords[i];
			while(numSegments > 1) {
				ins = new Coordinate(ins.x + vectorX, ins.y + vectorY);
				ext.add(ins);
				numSegments--;
			}
			// end debug
			ext.add(vcoords[i+1]);
		}
		return gf.createLinearRing( ext.toArray(new Coordinate[ext.size()]) );
	}

}
