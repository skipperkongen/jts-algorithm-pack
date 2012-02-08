package org.geodelivery.jap.command;

import java.util.HashMap;
import java.util.Stack;

public class Options {

	private HashMap<String, Stack<String>> _map = new HashMap<String, Stack<String>>();

	// command line parameter
	// Jap -h | -m method args... [-o output_file] input_file 
	Options(String[] args) {
		
		String currentOption = null;
		String lastArgument = null;
		String lastOption = null;

		for(String s : args) {
			
			if(s.startsWith("-")) {
				_map.put(s, new Stack<String>());
				currentOption = s;
				lastOption = s;
			}
			else {
				_map.get(currentOption).add(s);
				currentOption = null;
			}
			lastArgument = s;
		}
		// remove positional argument from last optional argument)
		_map.get(lastOption).pop();
		_map.put("input_file", new Stack<String>());
		_map.get("input_file").push(lastArgument);
	}
	
	/**
	 * @param parameter
	 * @return
	 */
	private String[] getValues(String parameter) {
		if(!_map.containsKey(parameter)) {
			return null;
		}
		else {
			Stack<String> stack = _map.get(parameter);
			String[] values = new String[stack.size()];
			for(int i=0; i<stack.size(); i++) {
				values[i] = stack.get(i);
			}
			return values;
		}
	}
	
	/**
	 * @return
	 */
	public boolean helpRequested() {
		return _map.containsKey("-h");
	}
	
	/**
	 * @return
	 */
	public String getInputFile() {
		return getValues("input_file")[0];
	}

	/**
	 * @return
	 */
	public String getOutputFile() {
		String[] values = getValues("-o");
		if(values == null || values.length == 0) {
			return null;
		}
		return values[0];
	}
	
	public String getAlgorithm() {
		String[] values = getValues("-m");
		if(values == null || values.length == 0) {
			return null;
		}
		return values[0];
	}
	
	public String[] getMethodArgs() {
		String[] values = getValues("-m");
		if(values == null || values.length < 2 ) {
			return null;
		}
		// return all but first element
		String[] result = new String[values.length-1];
		System.arraycopy(values, 1, result, 0, values.length-1);
		return result;
	}
}
