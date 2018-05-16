package synoptic.algorithms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.String;
import synoptic.algorithms.Correlation;
import org.apache.commons.lang.ArrayUtils;
import java.lang.Math;

//import java.util.Set;

public class TraceSimilarity {


    /* calculate distance matrix */
    public static double[][] matrix(String[] traces){
    	int size = traces.length;
    	double[][] res = new double[size][size];
    	int i = 0;
    	for (String t1: traces){
    		int j = 0;
    		for (String t2: traces){
    			//System.out.println("1: " + t1);
    			//System.out.println("2: " + t2);
    			res[i][j] = 1 - similarityCoef(t1, t2);
    			++j;
    		}
    		++i;
    	}
    	return res;
    }
    
    private static double similarityCoef(String t1, String t2){
    	String[] symbol1 = parseSymbol(t1);
    	String[] symbol2 = parseSymbol(t2);
    	String[] symbol12 = new String[0];
    	String[] parameters1 = parseParameters(t1);
		String[] parameters2 = parseParameters(t2);
		String[] parameters12 = new String[0];
		for (String s: symbol1){
			if (ArrayUtils.contains(symbol2, s)){
				symbol12 = Correlation.stringAdd(symbol12, s);
			}
		}
		for (String p: parameters1){
			if (ArrayUtils.contains(parameters2, p)){
				parameters12 = Correlation.stringAdd(parameters12, p);
			}
		}
		//System.out.println("s1:" + Arrays.toString(symbol1) + ", s2:" + Arrays.toString(symbol2) + ", s12:" + Arrays.toString(symbol12) + ", p1:" + Arrays.toString(parameters1) + ", p2:" + Arrays.toString(parameters2) + ", p12:" + Arrays.toString(parameters12));
		double ressymbol = ((double) symbol12.length / (double) Math.min(symbol1.length, symbol2.length));
		double resparameters = ((double) parameters12.length / (double) Math.min(parameters1.length, parameters2.length));
		double res = (ressymbol + resparameters) / 2;
		return res;
    }


    private static String[] parseSymbol(String trace){
    	try {
    		BufferedReader br = new BufferedReader(new FileReader(trace));
    		String[] res = new String[0];
    		String line;
    		while ((line = br.readLine()) != null){
    			if (!(line.contains("call_") || line.contains("return_"))) { 
    				String symbol = line.substring(0, line.indexOf("("));
    				if (!ArrayUtils.contains(res, symbol)){
    					res = Correlation.stringAdd(res, symbol);
    				}
			   	} 
			}
			br.close();
			return res;
    	} catch (FileNotFoundException e){
    		System.out.println("file not found " + e);
    		return null;
    	} catch (IOException e){
    		System.out.println("error " + e);
    		return null;
    	}
    }

    private static String[] parseParameters(String trace){
    	try {
    		BufferedReader br = new BufferedReader(new FileReader(trace));
    		String[] res = new String[0];
    		String line;
		   	while ((line = br.readLine()) != null){
				String tmp = line.replace("(", ";");
				tmp = tmp.replace(")", ";");
				String[] parameters = tmp.split(";");
				int i = 0;
				for (String p: parameters){
					if ( i != 0 && !ArrayUtils.contains(res, p) &&
					   ( p.length() < 5 || !p.substring(0,5).equals("CEFSM"))){ 
		   				res = Correlation.stringAdd(res, p);
					}
					++i;
				} 
			}
    		br.close();
    		return res;
  		} catch (FileNotFoundException e){
    		System.out.println("file not found " + e);
    		return null;
    	} catch (IOException e){
    		System.out.println("error " + e);
    		return null;
    	}
    }

}