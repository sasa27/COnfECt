package synoptic.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Utility {
	
	private static String stat = "COnfECt/stat"; // nom du fichier contenant les stats
	
	private static int totalS = 0;
	
	private static int totalT = 0;
	
	
	
	private static void getNbStates(String file) {
		try {
			
			BufferedReader br = new BufferedReader(new FileReader(file));
			BufferedWriter bw = new BufferedWriter(new FileWriter(stat, true));
			String line = br.readLine(); // skip the first line	
			line = br.readLine();
			int compteur = 0;
			while (!line.contains("->")){
				compteur ++;
				line = br.readLine();
			}
			br.close();
			bw.write(file + " :" + compteur + " states");
			bw.close();
			totalS = totalS + compteur;
			//System.out.println(file + " :" + compteur + " states in the model");
		} catch (FileNotFoundException e){
    		System.out.println("file not found " + e);
    	} catch (IOException e){
    		System.out.println("error " + e);
    	}
	}
	
	
	private static void getNbTransitions(String file) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			BufferedWriter bw = new BufferedWriter(new FileWriter(stat, true));
			String line = br.readLine();	
			int compteur = 0;
			while (!line.contains("->")){
				line = br.readLine();
			}
			while (!line.contains("}")) {
				compteur ++;
				line = br.readLine();
			}
			br.close();
			bw.write(", and " + compteur + " transitions\n");
			bw.close();
			totalT = totalT + compteur;
			//System.out.println(file + " :" + compteur + " transitions in the model");
		} catch (FileNotFoundException e){
    		System.out.println("file not found " + e);
    	} catch (IOException e){
    		System.out.println("error " + e);
    	}
	}
	
	private static void getNbFile() {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(stat));	
			int k = 1;
			File Tn = new File("COnfECt/T" + k);
	        while (Tn.exists()){
	            ++k;
	            Tn = new File("COnfECt/T" + k);
	        }
	        
	        int k2 = 1;
			Tn = new File("COnfECt/trace" + k2);
	        while (Tn.exists()){
	            ++k2;
	            Tn = new File("COnfECt/trace" + k2);
	        }
	        
	        int C = 1;
	        Tn = new File("COnfECt/C" + C + ".dot");
	        while (Tn.exists()){
	            ++C; 
	            Tn = new File("COnfECt/C" + C + ".dot");
	        }
	        C = C - 1;
	        k = k + k2 - 2;
			bw.write( k + " trace files in Strace, and " + C + " dot file generated\n\n" );
			bw.close();
			//System.out.println(file + " :" + compteur + " transitions in the model");
		} catch (FileNotFoundException e){
    		System.out.println("file not found " + e);
    	} catch (IOException e){
    		System.out.println("error " + e);
    	}
	}
	
	private static void getTotal() {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(stat, true));
			bw.write(totalS + " states in total and " + totalT + " transitions\n" );
			bw.close();			
		} catch (FileNotFoundException e){
	    	System.out.println("file not found " + e);
	    } catch (IOException e){
	    	System.out.println("error " + e);
	    }	
	}
	
	public static void getStats(int nbFile) {
		getNbFile();
		for (int i = 1; i <= nbFile; ++i) {
			String file = "COnfECt/C" + i + ".dot";
			getNbStates(file);
			getNbTransitions(file);
			String hide = "COnfECt/hideCall" + i + ".dot";
			Clean.hideCall(file, hide); //if strong or weak strategy
			//Clean.hideCallStrict(file, hide); //if strict strategy
			getNbStates(hide);
			getNbTransitions(hide);
			getTotal();
			//new File(hide).delete();
		}
		getTotal();
		
	}
	
}

