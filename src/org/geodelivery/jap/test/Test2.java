package org.geodelivery.jap.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

import org.geodelivery.jap.charact.AlphaShape;
import org.geodelivery.jap.charact.ConcaveHull;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

public class Test2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream("data/punkter.wkt");
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			WKTReader wktReader = new WKTReader();
			Geometry g = wktReader.read(br);
			long t0 = System.currentTimeMillis();
			ConcaveHull ch = new ConcaveHull(g);
			Geometry result = ch.getConcaveHull(1);
			System.out.println(System.currentTimeMillis() - t0);
//			System.out.println(result.toText());
//			WKTWriter writer = new WKTWriter();
//			FileWriter outstream = new FileWriter("data/result.wkt");
//			BufferedWriter out = new BufferedWriter(outstream);
//			writer.write(result, out);

		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

	}

}
