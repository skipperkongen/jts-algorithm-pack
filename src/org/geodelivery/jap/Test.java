package org.geodelivery.jap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

import org.geodelivery.jap.algorithms.ConcaveHull;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

public class Test {

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
			ConcaveHull ch = new ConcaveHull();
			Geometry result = ch.computeGeometry(g);
			//result = DouglasPeuckerSimplifier.simplify(result, 500);
			//result = result.buffer(250);
			//result = result.buffer(-250);

			System.out.println(result.toText());
			WKTWriter writer = new WKTWriter();
			FileWriter outstream = new FileWriter("data/result.wkt");
			BufferedWriter out = new BufferedWriter(outstream);
			writer.write(result, out);

		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

	}

}