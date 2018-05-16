package synoptic.algorithms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.String;
import java.util.Arrays;

public class Correlation {

	private static double seuil = 0.5;

    public static void analysis(String[] traces){
    	try{
    		/* TODO create COnfECt folder */
			int i = 1;
    		for (String trace: traces){
    			String[] sequences = new String[0];
    			BufferedReader br = new BufferedReader(new FileReader(trace));
    			String prec = br.readLine();
    			String sigma = prec + "\n";
				String line;
				String name;
				while ((line = br.readLine()) != null){
   					if (coefficient(traces, prec, line) >= seuil){
   						sigma = sigma + line + "\n";
					}
   					else {
   						sequences = stringAdd(sequences, sigma);
   						sigma = line + "\n";
   					}
   					prec = line;
				}
				br.close();
				sequences = stringAdd(sequences, sigma);
				name = "COnfECt/trace" + i;
				extract(traces, sequences, name);
				++i;
			}			
    	} catch (FileNotFoundException e){
    		System.out.println("file not found " + e);
    	} catch (IOException e){
    		System.out.println("error " + e);
    	}

    }

    public static String[] stringAdd(String[] originalArray, String newItem)
	{
  		int currentSize = originalArray.length;
  		int newSize = currentSize + 1;
	    String[] tempArray = new String[ newSize ];
 	    for (int i=0; i < currentSize; i++){
    	    tempArray[i] = originalArray [i];
   		}
  		tempArray[newSize - 1] = newItem;
    	return tempArray;   
	}

    public static void extract(String[] traces, String[] sequences, String file){
		try{
			
			int n = 1;
			int id = 0;
			int k = sequences.length;
			//boolean nothingtodo = true;
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(file)));
			String Tn = file.substring(file.indexOf("T") + 1);
			/*if (!file.contains("trace")){
				bw.write("call_T" + Tn + "\n");
			}*/
			bw.write(sequences[0]);
			while (id < k - 1){
				File f = new File("COnfECt/T" + n);
				while (f.exists()){
					//System.out.println("T" + n + " existe deja");
					++n;
					f = new File("COnfECt/T" + n);
				}
				String name = "COnfECt/T" + n;
				/* create the file */
				BufferedWriter creation = new BufferedWriter(new FileWriter(f));
				creation.close();
				boolean find = false;
				String sigma1 = getLast(sequences[id]); 
				String sigma2;
				int i;
				for (i = id + 1; i < k; ++i){
					sigma2 = getFirst(sequences[i]);
					if (coefficient(traces, sigma1, sigma2) >= seuil){
						find = true;
						break;
					}
				}
				/*bw.write("call_T" + n + "\n"
					 + "return_T" + n + "\n"); */
				if (find){
					extract(traces, Arrays.copyOfRange(sequences, id + 1, i), name);
					bw.write(sequences[i]);
					id = i;
				}
				else {
					extract(traces, Arrays.copyOfRange(sequences, id + 1, k), name);
					id = k;
				}
			}
			/*if (!file.contains("trace")){
				bw.write("return_T" + Tn + "\n");
			}*/
			bw.close();
		} catch (FileNotFoundException e){
    		System.out.println("file not found " + e);
    	} catch (IOException e){
    		System.out.println("error " + e);
    	}

    }



    /* get the first event of a sequence */
    private static String getFirst(String sequence){
    	int end = sequence.indexOf("\n");
    	String res = sequence.substring(0, end);
    	return res;
    }

    /* get the last event of a sequence */
    private static String getLast(String sequence){
    	int i = sequence.length() - 2;
    	while ( i > 0 && sequence.charAt(i) != '\n'){
    		i -= 1;
    	}
    	String res = sequence.substring(i, sequence.length() - 1);
    	if (res.indexOf("\n") != -1){
    		res = res.substring(1, res.length());
    	}
    	return res;
    }

    /* get the correlation coefficient between two events */ 
    private static float coefficient(String[] traces, String event1, String event2) {
    	float freq1 = 0;
    	float freq2 = 0;
    	float freq12 = 0;
    	String symbol1 = event1.substring(0, event1.indexOf("("));
    	String symbol2 = event2.substring(0, event2.indexOf("(")); 
    	try{
    		for (String trace: traces){
    		//File f = new File(trace);
    			
    			BufferedReader br = new BufferedReader(new FileReader(trace));
    			String prec = null;
				String line;
				while ((line = br.readLine()) != null){
					//System.out.println(line);
					line = line.substring(0, line.indexOf("("));
   					if (line.equals(symbol1)){
   						freq1++;
   					}
   					if(prec != null && line.equals(symbol2)){
   						if (prec.equals(symbol1)){
							freq12++;
						}
   						freq2++;
   					}

   					prec = line;
				}
				br.close();
			}
			//float corr = ((freq12/freq1) + (freq12/freq2))/2; 
    		float corr = Math.max((freq12/freq1), (freq12/freq2));
   			//System.out.println(symbol1 + symbol2 + " ont pour corr : " + corr );
			return corr;
    	} catch (FileNotFoundException e){
    		System.out.println("file not found " + e);
    	} catch (IOException e){
    		System.out.println("error " + e);
    	}
    	return 0;
    }



}