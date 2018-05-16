package synoptic.algorithms;

import java.io.IOException;
import java.lang.String;
import java.net.URISyntaxException;
import java.util.Set;

import synoptic.algorithms.Correlation;
import synoptic.main.KTailsMain;
import synoptic.main.parser.ParseException;

import org.apache.commons.lang.ArrayUtils;
import java.util.Iterator;

import synoptic.model.EventNode;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
//import java.util.Arrays;
import synoptic.model.event.Event;
import synoptic.model.export.GraphExporter;
public class CEFSMSimilarity {


	
	public static double[][] matrix(PartitionGraph[] pGraph){
    	int size = pGraph.length;
    	double[][] res = new double[size][size];
    	int i = 0;
    	for  (PartitionGraph p1: pGraph) {
    		int j = 0;
    		String CEFSM1 = p1.getNodes().toString();
    		CEFSM1 = clean(CEFSM1);
        	//System.out.println("c : " + CEFSM1);
        	for (PartitionGraph p2: pGraph) {
        		String CEFSM2 = p2.getNodes().toString();
        		CEFSM2 = clean(CEFSM2);
        		res[i][j] = 1 - similarityCoef(CEFSM1, CEFSM2);
        		++j;
        	}
        	++i;
        }
    	return res;
    }
    
	private static String clean(String s) {
    	s = s.replace("INITIAL", "");
    	s = s.replace("TERMINAL", "");
    	s = s.replace(",", "");
    	s = s.replaceAll( "(call_T)[0-9]+" , "");
    	s = s.replaceAll( "(return_T)[0-9]+" , "");
    	s = s.replace(")", ")\n");
    	s = s.replace("P.", "");
    	s = s.replace(".1", "");
    	s = s.replace(" ", "");
    	s = s.replace("[", "");
    	s = s.replace("]", "");
    	return s;
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
    		String[] res = new String[0];
    		int i = 0;
    		while (i < trace.length()){
				String symbol = trace.substring(i, trace.indexOf("(", i));
				//System.out.println("symbol : " + symbol);
			   	if (!ArrayUtils.contains(res, symbol)){
			   		res = Correlation.stringAdd(res, symbol);
			   	} 
			   	i = trace.indexOf("\n", i) + 1;
			}
			return res;
    }
    
    private static String[] parseParameters(String trace){
    		String[] res = new String[0];
			String tmp = trace.replace("(", ";");
			tmp = tmp.replace(")", ";");
			String[] parameters = tmp.split(";");
			for (String p: parameters){
				if ( p.charAt(0) != '\n' && !ArrayUtils.contains(res, p)){
		   			res = Correlation.stringAdd(res, p);
				}
			} 
			//System.out.println("param : " + Arrays.toString(res));
    		return res;
    }
    
    
    public static PartitionGraph loopCall(PartitionGraph tree) {
    	Set<Partition> nodestmp = tree.getNodes();
    	Partition[] nodes = nodestmp.toArray(new Partition[nodestmp.size()]);
    	for (Partition node: nodes) {
    		if (!node.isInitial() && !node.isTerminal()) {
	    		Set<Partition> futurs = node.getAllSuccessors();
	    		Partition futur = futurs.iterator().next();
	    		Set<EventNode> eventNodes = node.getEventNodes(); 
	    		EventNode event = eventNodes.iterator().next();
	    		if (node.compareTo(futur) != 0 && futur.toString().contains("call_")) {
	    			Partition pcall = futur;
	    			Set<EventNode> eventCall = pcall.getEventNodes(); 
	    			EventNode call = eventCall.iterator().next();
	    			futurs = pcall.getAllSuccessors();
	    			Partition pret = futurs.iterator().next();
	    			if (node.compareTo(pret) != 0 ) {
		    			Set<EventNode> eventRet = pret.getEventNodes(); 
		    			EventNode ret = eventRet.iterator().next();
		    			futurs = pret.getAllSuccessors();
		    			Partition pend = futurs.iterator().next();
		    			Set<EventNode> eventEnd = pend.getEventNodes(); 
		    			EventNode end = eventEnd.iterator().next();
			    		Event label = new Event(pcall.toString() + "*ERASEME*");
						EventNode label2 = new EventNode(label);
						Partition label3 = new Partition(label2);
						tree.add(label3);
						end.addTransition(label2, "NOLOOP");
						label2.addTransition(end, "NOLOOP");
						ret.addTransition(call, "NOLOOP");
						event.addTransition(end, "NOLOOP");
	    			}
	    		}
	    	}
    	}
    	return tree;
    }

    
    public static PartitionGraph suppCall(PartitionGraph tree) throws IllegalArgumentException, IllegalAccessException, IOException, URISyntaxException, ParseException {
    	Set<Partition> nodestmp = tree.getNodes();
    	Partition[] nodes = nodestmp.toArray(new Partition[nodestmp.size()]);
    	for (Partition node: nodes) {
    		if (/*!node.isInitial() && */!node.isTerminal() && !node.toString().equals("UNINIT.P.call_C.0")) {
	    		Set<Partition> futurs = node.getAllSuccessors();
	    		//System.out.println("p : " + node.toString());
	    		Partition futur = futurs.iterator().next();
	    		Set<EventNode> eventNodes = node.getEventNodes(); 
	    		EventNode event = eventNodes.iterator().next();
	    		if (!node.isInitial() && node.compareTo(futur) != 0 && futur.toString().contains("call_") && !node.toString().contains("return_")) {
		    			Partition pcall = futur;// futur est le call
		    			//System.out.println("call :" + pcall.toString());
		    			Set<EventNode> eventCall = pcall.getEventNodes(); 
		    			EventNode call = eventCall.iterator().next();
		    			futurs = pcall.getAllSuccessors(); //futurs est le return
		    			//System.out.println("futur : " + futurs.toString());
		    			Partition pret = futurs.iterator().next();
		    			if (node.compareTo(pret) != 0 ) {
			    			Set<EventNode> eventRet = pret.getEventNodes(); 
			    			EventNode ret = eventRet.iterator().next();
			    			futurs = pret.getAllSuccessors(); //juste apres le return
			    			Partition pend = futurs.iterator().next();
			    			Set<EventNode> eventEnd = pend.getEventNodes(); 
			    			EventNode end = eventEnd.iterator().next();
				    		Event label = new Event("call_C");
							EventNode label2 = new EventNode(label);
							Partition label3 = new Partition(label2);
							tree.add(label3);
							Bisimulation.merge(tree, label3, pcall);
							//end.addTransition(label2, "NOLOOP");
							//label2.addTransition(end, "NOLOOP");
							call.addTransition(end, "NOLOOP");
							ret.addTransition(call, "NOLOOP");
							event.addTransition(end, "NOLOOP");
							
	    			/*Partition pcall = futur;
	    			Set<EventNode> eventCall = pcall.getEventNodes(); 
	    			EventNode call = eventCall.iterator().next();
	    			futurs = pcall.getAllSuccessors();
	    			System.out.println(pcall + " " + futurs);
	    			Partition pret = futurs.iterator().next();
	    			if (node.compareTo(pret) != 0) {
		    			Set<EventNode> eventRet = pret.getEventNodes(); 
		    			EventNode ret = eventRet.iterator().next();
		    			futurs = pret.getAllSuccessors();
		    			try {
			    			Iterator<Partition> it = futurs.iterator();
			    			Partition pend = it.next();
			    			while (pend.toString().contains("call")){
	    						pend = it.next();
	    						//futurends.iterator().remove();
	    					}
			    			
			    			Set<EventNode> eventEnd = pend.getEventNodes(); 
			    			EventNode end = eventEnd.iterator().next();
			    			//Set<Partition> futurs2 = pend.getAllSuccessors();
			    			Event label = new Event("ncall_C");
							EventNode call2 = new EventNode(label);
							Partition call3 = new Partition(call2);
							tree.add(call3);
							Bisimulation.merge(tree, call3, pcall);
							call.addTransition(end, "NOLOOP");
							ret.addTransition(call, "NOLOOP");
							event.addTransition(end, "NOLOOP");
		    			}catch (java.util.NoSuchElementException e){
		    				String[] out = {"*.log", "-o", "COnfECt/bug"};//////////
		            		KTailsMain mainInstance = KTailsMain.processArgs(out);///////////////
		    				mainInstance.exportGraph(tree);////////////////
		            	    GraphExporter.generatePngFileFromDotFile("COnfECt/bug.dot");  /// tempo
		    			}*/
	    			}
	    		}
    		}
    	}
    	for (Partition node: nodes) {
    		if (/*!node.isInitial() && */!node.isTerminal() && !node.toString().equals("UNINIT.P.call_C.0")) {
	    		Set<Partition> futurs = node.getAllSuccessors();
	    		//System.out.println("p : " + node.toString());
	    		Partition futur = futurs.iterator().next();
	    		Set<EventNode> eventNodes = node.getEventNodes(); 
	    		EventNode event = eventNodes.iterator().next();
	    		if (!node.toString().contains("call_C.") && !node.toString().contains("return_C.") && !futur.toString().contains("call_C.")){
	    			Set<EventNode> eventFutur = futur.getEventNodes(); 
	    			EventNode end = eventFutur.iterator().next();
	    			//Set<Partition> futurs2 = futur.getAllSuccessors();
	    			Event call = new Event("call_C");
					EventNode call2 = new EventNode(call);
					Partition call3 = new Partition(call2);
					tree.add(call3);
					Event retrun = new Event("return_C");
					EventNode retrun2 = new EventNode(retrun);
					Partition retrun3 = new Partition(retrun2);
					tree.add(retrun3);
					call2.addTransition(end, "NOLOOP");
					event.addTransition(call2, "CALL");
					call2.addTransition(retrun2, "CALL");
					retrun2.addTransition(call2, "CALL");
					retrun2.addTransition(end, "CALL");
					
	    		}
	    	}
    	}
    	return tree;
    }
    

    /* add a pGraph in the Array */
    public static Partition[] arrayAdd(Partition[] originalArray, Partition newItem)
	{
  		int currentSize = originalArray.length;
  		int newSize = currentSize + 1;
	    Partition[] tempArray = new Partition[ newSize ];
 	    for (int i=0; i < currentSize; i++){
    	    tempArray[i] = originalArray [i];
   		}
  		tempArray[newSize - 1] = newItem;
    	return tempArray;   
	}
    
    
}
