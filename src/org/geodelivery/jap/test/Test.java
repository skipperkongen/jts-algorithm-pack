package org.geodelivery.jap.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

import org.geodelivery.jap.AlphaShape;

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
			AlphaShape alpha = new AlphaShape(g);
			Geometry result = alpha.getAlphaShape(1000);
			WKTWriter writer = new WKTWriter();
			FileWriter outstream = new FileWriter("data/result.wkt");
			BufferedWriter out = new BufferedWriter(outstream);
			writer.writeFormatted(result, out);

		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

	}

}
