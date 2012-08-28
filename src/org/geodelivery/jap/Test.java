package org.geodelivery.jap;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.geodelivery.jap.concavehull.ConcaveHull;
import org.geodelivery.jap.concavehull.SnapHull;
import org.geodelivery.jap.core.Transform;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

public class Test {
	
	public static void main(String[] args) throws Exception {
		// Open the file that is the first
		// command line parameter
		FileInputStream fstream = new FileInputStream("data/punkter.wkt");
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		WKTReader wktReader = new WKTReader();
		Geometry g = wktReader.read(br);

		HashMap<String, Geometry> results = new HashMap<String, Geometry>();
		results.put("snaphull", runAlgorithm(new SnapHull(), g));
		results.put("concavehull", runAlgorithm(new ConcaveHull(0.7),g));

		for(String key : results.keySet()) {
			System.out.println(key + ": \n" + results.get(key).toText());
		}		
	}

	private static Geometry runAlgorithm(Transform<Geometry, Geometry> algorithm, Geometry g) {
		// TODO Auto-generated method stub
		long t0 = System.currentTimeMillis();
		Geometry result = algorithm.transform(g);
		long t1 = System.currentTimeMillis();
		System.out.println("Processing time: " + (t1-t0) + " ms");
		return result;
	}
}