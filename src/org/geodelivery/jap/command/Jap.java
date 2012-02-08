package org.geodelivery.jap.command;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.geodelivery.jap.charact.ConcaveHull;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * @author Pimin Konstantin Kefaloukos
 *
 */
public class Jap {

	/**
	 * @param args
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ParseException, IOException, IllegalArgumentException {
		// Open the file that is the first
		// command line parameter
		// Jap -h | -m method_name -a argument(s) [-o output_file] input_file
		if(args.length == 0) {
			printUsageAndExit();
		}
		
		Options options = new Options(args);
		if(options.helpRequested()) {
			printUsageAndExit();
		}
		
		// check mandatory args present
		if(options.getAlgorithm() == null || options.getInputFile() == null) {
			printUsageAndExit();
		}

		// Read input_file
		FileInputStream fstream = new FileInputStream(options.getInputFile());
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		WKTReader wktReader = new WKTReader();
		Geometry g = wktReader.read(br);
		in.close();

		Geometry result;
		if (options.getAlgorithm() == "concavehull") {
			String[] methargs = options.getMethodArgs();
			int compressionLevel = methargs != null ? Integer.parseInt(methargs[0]) : 1; // default
			ConcaveHull ch = new ConcaveHull(g);
			result = ch.getConcaveHull(compressionLevel);
		}
		else {
			System.err.println("Unknown algorithm: " + options.getAlgorithm());
			result = null;
			printUsageAndExit();
		}
		
//		long t0 = System.currentTimeMillis();
//		result = DouglasPeuckerSimplifier.simplify(result, 100);
//		result = result.buffer(1000);
//		result = result.buffer(-750);
//		result = DouglasPeuckerSimplifier.simplify(result, 100);
//		System.out.println(System.currentTimeMillis() - t0);
		if(options.getOutputFile() == null) {
			System.out.println(result.toText());
		}
		else {
			WKTWriter writer = new WKTWriter();
			FileWriter outstream = new FileWriter("data/result.wkt");
			BufferedWriter out = new BufferedWriter(outstream);
			writer.write(result, out);
			out.close();
		}
	}

	private static void printUsageAndExit() {
		System.out.println("Usage: java -jar Jap.jar -m method [args...] [-o output_file] input_file\n\n"+
				"input_file is a text-file containing a geometry as WKT. This can be any geometry type.\n\n"+
				"-h, print this help text.\n" +
				"-l, list supported algorithms.\n" +
				"-o, output is written to file as a WKT geometry. If -o omitted result is printed to standard out.\n"+
				"-m, method or algorithm to apply to input geometry, with space separated arguments, e.g: concavehull 2\n\n" +
				"Example: java -jar Jap.jar -m concavehull 2 -o geometry.txt input.txt");
		System.exit(2);
	}

}
