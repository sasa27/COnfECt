package synoptic.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
//import java.util.Arrays;
import java.util.Arrays;

import synoptic.algorithms.Correlation;

public class Clean {
	
	
	  /* TODO make loop from composition */
	public static void labelOnTransitions(String name, String output) {
		String[] labels = new String[0];
		String[] labelList = getLabels(name);
		int c = 0;
		for (int i = 0; i < labelList.length; ++i) {
			int labelSource = Integer.parseInt(labelList[i].substring(2, labelList[i].indexOf("[") - 1));
			while (labelSource != c) {
				labels = Correlation.stringAdd(labels, " ");
				++c;
			}
			labels = Correlation.stringAdd(labels, labelList[i]);
			++c;
			
		}
		String[] transitions = getTransitions(name);
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(output));
			bw.write("digraph G {\n");
			int initial = 0;
			for (int i = 0; i < labels.length; ++i) {
				if (labels[i].contains("INITIAL")){
					initial = i;
					break;
				}
			}
			
			int[] newInit = new int[0];
			for (String edge: transitions) {
				int source = Integer.parseInt(edge.substring(0, edge.indexOf("-")));
				int destination = Integer.parseInt(edge.substring(edge.indexOf(">") + 1, edge.indexOf(" ")));
				if (source == initial) {
					newInit = Add(newInit, destination);
				}
			}
			for (String node: labelList) {
				int i = Integer.parseInt(node.substring(2, node.indexOf("[") - 1));
				if (labels[i].contains("TERMINAL")) {
					bw.write("  " + i + " [label=\"" + "TERMINAL" + "\",shape=diamond];\n");
				}
				else if (labels[i].contains("INITIAL")) {
					bw.write("  " + i + " [label=\"" + "INITIAL" + "\",shape=box];\n");
				}
				else if (!isInArray(newInit,i)){
					bw.write("  " + i + " [label=\"" + i + "\"];\n");
				}
			}
			String[] inFile = new String[0];
			for (String edge: transitions) {
				int source = Integer.parseInt(edge.substring(0, edge.indexOf("-")));
				int destination = Integer.parseInt(edge.substring(edge.indexOf(">") + 1, edge.indexOf(" ")));
				int begin = labels[source].indexOf("\"") + 1;
				int end =  labels[source].indexOf("\"", begin);
				String label =  labels[source].substring(begin, end);
				int newsource = source;
				String newEdge;
				if (source == initial) {
					continue;
				}
				if (isInArray(newInit, source)) {
					newsource = initial;
				}
				if (isInArray(newInit, destination)) {
					destination = initial;
				}
				newEdge = newsource + "->" + destination + " ";
				String line = newEdge + "[label=\"" + label + "\"];\n";
				if (!isInArray(inFile, line) && (newsource != initial || destination != initial || !label.contains("call_C"))) {
					bw.write(line);
					inFile = Correlation.stringAdd(inFile, line);
				}
			}
			bw.write("}\n");
			bw.close();
		} catch (IOException e){
    		System.out.println("error " + e);
    	}	
	}
	
	public static void eraseMe(String name, String output) {
		try {
			String[] transitions = getTransitions(name);
			String[] nodes = getLabels(name);
			BufferedWriter bw = new BufferedWriter(new FileWriter(output));
			bw.write("digraph G {\n");
			int[] erase = new int[0];
			for (String node: nodes) {
				int i = Integer.parseInt(node.substring(0, node.indexOf("[") - 1).trim());
				if (node.toString().contains("*ERASEME*")) {
					erase = Add(erase, i);
					continue;
				}
				else {
					bw.write(node + "\n");				
				}
			}
			for (String edge: transitions) {
				int source = Integer.parseInt(edge.substring(0, edge.indexOf("-")));
				int destination = Integer.parseInt(edge.substring(edge.indexOf(">") + 1, edge.indexOf(" ")));
				if (isInArray(erase, source) || isInArray(erase, destination)) {
					continue;
				}
				bw.write(edge + "\n");
			}
			bw.write("}\n");
			bw.close();
		} catch (FileNotFoundException e){
    		System.out.println("file not found " + e);
    	} catch (IOException e){
    		System.out.println("error " + e);
    	}
	}

	public static void makeLoop(String name, String output) {
		try {
			String[] transitions = getTransitions(name);
			String[] nodes = getLabels(name);
			BufferedWriter bw = new BufferedWriter(new FileWriter(output));
			bw.write("digraph G {\n");
			int[] call = new int[0];
			int[] ret = new int[0];
			int[] retTerm = new int[0];
			int initial = 0;
			String[] put = new String[0];
			String[] alreadyIn = new String[0];
			for (String node: nodes) {
				int i = Integer.parseInt(node.substring(0, node.indexOf("[") - 1).trim());
				if (node.contains("INITIAL")) {
					initial = i;
					break;
				}
			}
			for (String edge: transitions) {
				int source = Integer.parseInt(edge.substring(0, edge.indexOf("-")));
				int dest = Integer.parseInt(edge.substring(edge.indexOf(">") + 1, edge.indexOf(" ")));
				int beginLabel = edge.indexOf("\"") + 1;
				int endLabel = edge.indexOf("\"", beginLabel);
				String label = edge.substring(beginLabel, endLabel);
				if (source != initial && label.length() > 5 && label.substring(0, 5).equals("call_")) {
					call = Add(call, source);
					ret = Add(ret, dest);
				}	
			}
			for (String edge: transitions) {
				
				int source = Integer.parseInt(edge.substring(0, edge.indexOf("-")));
				int dest = Integer.parseInt(edge.substring(edge.indexOf(">") + 1, edge.indexOf(" ")));
				int beginLabel = edge.indexOf("\"") + 1;
				int endLabel = edge.indexOf("\"", beginLabel);
				String label = edge.substring(beginLabel, endLabel);
				
				if (label.length() > 5 && label.substring(0, 6).equals("return") && isInArray(alreadyIn, label + dest) && isInArray(ret, source)) {
					call = Add(call, source);
				}	
				else if (label.length() > 5 && label.substring(0, 6).equals("return") && isInArray(ret, source)) {
					alreadyIn = Correlation.stringAdd(alreadyIn, label + dest);
				}
				else if (label.contains("return_C") && !isInArray(ret, source)) {
					retTerm = Add(retTerm, source);
				}
			}
			String[] retrun = new String[0];
			for (String edge: transitions) {
				int source = Integer.parseInt(edge.substring(0, edge.indexOf("-")));
				int dest = Integer.parseInt(edge.substring(edge.indexOf(">") + 1, edge.indexOf(" ")));
				int beginLabel = edge.indexOf("\"") + 1;
				int endLabel = edge.indexOf("\"", beginLabel);
				String label = edge.substring(beginLabel, endLabel);
				if (isInArray(retTerm, dest) && isInArray(retrun, label)) {
					call = Add(call, source);
				}
				else if (isInArray(retTerm, dest) && label.contains("return")) {
					retrun = Correlation.stringAdd(retrun, label);
				}
			}
			for (String node: nodes) {
				int i = Integer.parseInt(node.substring(0, node.indexOf("[") - 1).trim());
				if (!isInArray(call, i) && !isInArray(retTerm, i)) {
					bw.write(node + "\n");				
				}
			}
			if (retTerm.length > 0) {
				bw.write("  " + retTerm[0] + " [label=\"" + retTerm[0] + "\"];\n");
			}
			for (String edge: transitions) {
				int source = Integer.parseInt(edge.substring(0, edge.indexOf("-")));
				int dest = Integer.parseInt(edge.substring(edge.indexOf(">") + 1, edge.indexOf(" ")));
				int beginLabel = edge.indexOf("\"") + 1;
				int endLabel = edge.indexOf("\"", beginLabel);
				String label = edge.substring(beginLabel, endLabel);
				String label2 = "";
				if (label.contains("return_")){
					label2 = label.substring(label.indexOf("_") + 1);
				}
				if (isInArray(retTerm, source)) {
					source = retTerm[0];
				}
				if (isInArray(retTerm, dest)) {
					dest = retTerm[0];
				}
				String t = source + "->" + dest + "  [label=\"" + label + "\"];\n";
				
				if (!isInArray(put, t) && !isInArray(call, source) && !isInArray(call, dest)) {
					bw.write(t);
					put = Correlation.stringAdd(put, t);
					if (isInArray(ret, source)) {
						bw.write(dest + "->" + source + "  [label=\"call_" + label2 + "\"];\n");
					}
				}
			}			
			bw.write("}\n");
			bw.close();
		} catch (FileNotFoundException e){
    		System.out.println("file not found " + e);
    	} catch (IOException e){
    		System.out.println("error " + e);
    	}
	}
	
	public static void makeLoopStrong(String name, String output) {
		try {
			String[] transitions = getTransitions(name);
			String[] nodes = getLabels(name);
			BufferedWriter bw = new BufferedWriter(new FileWriter(output));
			bw.write("digraph G {\n");
			int[] call = new int[0];
			int[] ret = new int[0];
			int[] retTerm = new int[0];
			int initial = -1;
			String[] put = new String[0];
			String[] alreadyIn = new String[0];
			for (String node: nodes) {
				int i = Integer.parseInt(node.substring(0, node.indexOf("[") - 1).trim());
				if (node.contains("INITIAL")) {
					initial = i;
					break;
				}
			}
			for (String edge: transitions) {
				int source = Integer.parseInt(edge.substring(0, edge.indexOf("-")));
				int dest = Integer.parseInt(edge.substring(edge.indexOf(">") + 1, edge.indexOf(" ")));
				int beginLabel = edge.indexOf("\"") + 1;
				int endLabel = edge.indexOf("\"", beginLabel);
				String label = edge.substring(beginLabel, endLabel); 
				if (label.equals("call_C")) {
					call = Add(call, source);
					ret = Add(ret, dest);
				}
			}
			for (String node: nodes) {
				int i = Integer.parseInt(node.substring(0, node.indexOf("[") - 1).trim());
				if (!isInArray(call, i) && !isInArray(retTerm, i) || i == initial) {
					bw.write(node + "\n");		
				}
			}
			if (retTerm.length > 0) {
				bw.write("  " + retTerm[0] + " [label=\"" + retTerm[0] + "\"];\n");
			}
			for (String edge: transitions) {
				int source = Integer.parseInt(edge.substring(0, edge.indexOf("-")));
				int dest = Integer.parseInt(edge.substring(edge.indexOf(">") + 1, edge.indexOf(" ")));
				int beginLabel = edge.indexOf("\"") + 1;
				int endLabel = edge.indexOf("\"", beginLabel);
				String label = edge.substring(beginLabel, endLabel);
				
				String t = source + "->" + dest + "  [label=\"" + label + "\"];\n";
				if ((!isInArray(call, source) || source == initial) && (!isInArray(call, dest) || dest == initial) && !label.equals("call_C") ) {
					bw.write(t);
					put = Correlation.stringAdd(put, t);
					if (label.equals("return_C")) {
						bw.write(dest + "->" + source + "  [label=\"call_C\"];\n");
					}
				}
			}
			bw.write("}\n");
			bw.close();
		} catch (FileNotFoundException e){
    		System.out.println("file not found " + e);
    	} catch (IOException e){
    		System.out.println("error " + e);
    	}
	}
	
	public static boolean isInArray(int[] arr, int targetValue) {
		for(int s: arr){
			if(s == targetValue) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isInArray(String[] arr, String targetValue) {
		for(String s: arr){
			if(s.equals(targetValue)) {
				return true;
			}
		}
		return false;
	}
	
	public static int[] Add(int[] originalArray, int newItem)
	{
  		int currentSize = originalArray.length;
  		int newSize = currentSize + 1;
	    int[] tempArray = new int[ newSize ];
 	    for (int i=0; i < currentSize; i++){
    	    tempArray[i] = originalArray [i];
   		}
  		tempArray[newSize - 1] = newItem;
    	return tempArray;   
	}
	
	public static String[] getLabels(String name) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(name));
			String line = br.readLine(); // skip the first line	
			line = br.readLine();
			String[] labels = new String[0];
			while (!line.contains("->")){
				labels = Correlation.stringAdd(labels, line);
				line = br.readLine();
			}
			br.close();
			return labels;
		} catch (FileNotFoundException e){
    		System.out.println("file not found " + e);
    		return null;
    	} catch (IOException e){
    		System.out.println("error " + e);
    		return null;
    	}
	}
		
	public static String[] getTransitions(String name) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(name));
			String line = br.readLine();
			// skip the first line		
			String[] transitions = new String[0];
			while (!line.contains("->")){
				line = br.readLine();
			}
			while (line != null && !line.contains("}") ) {
				transitions = Correlation.stringAdd(transitions, line);
				line = br.readLine();
			}
			br.close();
			return transitions;
		} catch (FileNotFoundException e){
    		System.out.println("file not found " + e);
    		return null;
    	} catch (IOException e){
    		System.out.println("error " + e);
    		return null;
    	}
	}
		
	public static void hideCall(String name, String output) {
		try {
			String[] transitions = getTransitions(name);
			String[] nodes = getLabels(name);
			BufferedWriter bw = new BufferedWriter(new FileWriter(output));
			bw.write("digraph G {\n");
			int[] call = new int[0];
			int[] newInit = new int[0];
			int initial = -1;
			int[] finals = new int[0];
			int[] newfinals = new int[0];
			String[] newTransitions = new String[0];
			String[] toModif = new String[0];
			for (String node: nodes) {
				int i = Integer.parseInt(node.substring(0, node.indexOf("[") - 1).trim());
				if (node.contains("INITIAL")) {
					initial = i;
				}
				else if (node.contains("TERMINAL")) {
					finals = Add(finals, i);
				}
			}		
			System.out.println(Arrays.toString(finals));
			for (String edge: transitions) {
				int source = Integer.parseInt(edge.substring(0, edge.indexOf("-")));
				int dest = Integer.parseInt(edge.substring(edge.indexOf(">") + 1, edge.indexOf(" ")));
				int beginLabel = edge.indexOf("\"") + 1;
				int endLabel = edge.indexOf("\"", beginLabel);
				String label = edge.substring(beginLabel, endLabel); 
				if (label.contains("call") && source != initial) {
					call = Add(call, dest);
				}
				else if (label.contains("call") && source == initial) {
					newInit = Add(newInit, dest);
				}
				else if (label.contains("return") && isInArray(finals, dest)) {
					newfinals = Add(newfinals, source);
					//call = Add(call, source);
					newTransitions = Correlation.stringAdd(newTransitions, source + "->" + dest);
				}
			}
			System.out.println(Arrays.toString(newTransitions));
			System.out.println("new init :" + Arrays.toString(newInit));
			for (String node: nodes) {
				int i = Integer.parseInt(node.substring(0, node.indexOf("[") - 1).trim());
				if (!isInArray(call, i) && !isInArray(newInit, i) && !isInArray(newfinals, i)) {
					bw.write(node + "\n");		
				}
			}
			for (String edge: transitions) {
				int source = Integer.parseInt(edge.substring(0, edge.indexOf("-")));
				int dest = Integer.parseInt(edge.substring(edge.indexOf(">") + 1, edge.indexOf(" ")));
				int beginLabel = edge.indexOf("\"") + 1;
				int endLabel = edge.indexOf("\"", beginLabel);
				String label = edge.substring(beginLabel, endLabel);
				if (isInArray(newInit, source) && !label.contains("call_C") && !label.contains("return") && isInArray(newfinals, dest)) {
					if (isInArray(newInit, dest)) {
						dest = initial;
					}
					toModif = Correlation.stringAdd(toModif, initial + "->" + dest + "  [label=\"" + label + "\"]\n");
				}				
				else if (isInArray(newInit, source) && !label.contains("call_C") && !label.contains("return")) {
					if (isInArray(newInit, dest)) {
						dest = initial;
					}
					bw.write( initial + "->" + dest + "  [label=\"" + label + "\"]\n");
				}
				else if (isInArray(newfinals, dest)) {
					if (isInArray(newInit, dest)) {
						edge = source + "->" + initial + "  [label=\"" + label + "\"]\n";
					}
					toModif = Correlation.stringAdd(toModif, edge);
				}
				else if ((!isInArray(call, source) && !isInArray(call, dest)) && !label.contains("call_C") && !label.contains("return")) {
					if (isInArray(newInit, dest)) {
						edge = source + "->" + initial + "  [label=\"" + label + "\"]\n";
					}
					bw.write(edge + "\n");
				}
			}
			for (String edge: toModif) {
				int source = Integer.parseInt(edge.substring(0, edge.indexOf("-")));
				int dest = Integer.parseInt(edge.substring(edge.indexOf(">") + 1, edge.indexOf(" ")));
				int beginLabel = edge.indexOf("\"") + 1;
				int endLabel = edge.indexOf("\"", beginLabel);
				String label = edge.substring(beginLabel, endLabel);
				if (isInArray(newfinals, dest) && !isInArray(call, dest) && !label.contains("call_C") && !label.contains("return")) {
					System.out.println("here");
					int i=-1;
					for (String t: newTransitions) {
						int s = Integer.parseInt(t.substring(0, t.indexOf("-")));
						int d = Integer.parseInt(t.substring(t.indexOf(">") + 1));
						if (dest == s) {
							i = d;
							break;
						}
					}
					bw.write( source + "->" + i + "  [label=\"nnnnn" + label + "\"]\n");
				}
			}
			bw.write("}\n");
			bw.close();
		} catch (FileNotFoundException e){
    		System.out.println("file not found " + e);
    	} catch (IOException e){
    		System.out.println("error " + e);
    	}
	}
			
			
	
}
