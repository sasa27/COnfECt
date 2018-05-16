package synoptic.main;


import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Random;


import java.io.File;
import synoptic.algorithms.Correlation;
import synoptic.algorithms.TraceSimilarity;
import synoptic.algorithms.CEFSMSimilarity;
//import smile.clustering.XMeans;
import smile.clustering.HierarchicalClustering;
import smile.clustering.linkage.SingleLinkage;
import java.util.Set;
import java.util.stream.Collectors;
import synoptic.model.export.GraphExporter;
import java.util.LinkedHashSet;
import synoptic.main.Utility;

import synoptic.model.event.Event;
import synoptic.model.EventNode;

import synoptic.algorithms.Bisimulation;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.main.options.AbstractOptions;
import synoptic.main.options.KTailsOptions;
import synoptic.main.parser.ParseException;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.export.GraphExportFormatter;
import synoptic.util.InternalSynopticException;

/**
 * Contains the command line entry point to run Synoptic's implementation of the
 * k-tails algorithm.
 */
/**
 * @author ohmann
 */
public class KTailsMain extends AbstractMain {

    /**
     * Return the singleton instance of KTailsMain, first asserting that the
     * instance isn't null.
     */
    public static KTailsMain getInstance() {
        assert (instance != null);
        assert (instance instanceof KTailsMain);
        return (KTailsMain) instance;
    }

    /**
     * The synoptic.main method to perform the k-tails inference algorithm. See
     * user documentation for an explanation of the options.
     * 
     * @param args
     *            Command-line options
     */
    public static void main(String[] args) throws Exception {
    	/**
    	 * 0 = strict composition
    	 * 1 = weak composition
    	 * 2 = strong composition 
    	 */
    	int composition = 1;
    	System.out.println(args[args.length - 1]);
    	if (args[args.length - 1].equals("strict")) {
    		composition = 0;
    	}
    	else if (args[args.length - 1].equals("strong")) {
    		composition = 2;
    	}
    	double similarity1 = 0.25; // clustering initial traces
    	double similarity2 = 0.4; // clustering CEFSMs
        String[] traces = getTraces(args);
        Correlation.analysis(traces);
        int n = 1;
        String[] newtraces = new String[0];
        File f = new File("COnfECt/trace" + n);
        while (f.exists()){
            newtraces = Correlation.stringAdd(newtraces, ("COnfECt/trace" + n));
            ++n;
            f = new File("COnfECt/trace" + n);
        }
        int k = 1;
        File Tn = new File("COnfECt/T" + k);
        while (Tn.exists()){
            ++k;
            Tn = new File("COnfECt/T" + k);
        }
        /* cluster for T1 */
        int[] clusters = new int[0];
        try {
        	double[][] matrix = TraceSimilarity.matrix(newtraces);
        	SingleLinkage link = new SingleLinkage(matrix);
        	HierarchicalClustering clusterise = new HierarchicalClustering(link);
        	clusters = clusterise.partition(similarity1);
        } catch (IllegalArgumentException e) {
        	clusters = new int[newtraces.length];
        }
        PartitionGraph[] CEFSMs = new PartitionGraph[0];
        int nbOfInitial = 0;
        int bstrict = 0;
        for (int c = 0; c <= maxValue(clusters); ++c) {
        	bstrict += 1;
        	String[] arg = new String[0];
        	for (int i = 0; i < clusters.length; ++i) {
	        	if (clusters[i] == c) {
	        		arg = Correlation.stringAdd(arg, "COnfECt/trace" + (i+1));
	        	}
	        }
	       	arg = Correlation.stringAdd(arg, "-o");
	       	arg = Correlation.stringAdd(arg, "COnfECt/initial" + c);
	       	KTailsMain mainInstance = processArgs(arg);
	        if (mainInstance == null) {
	            return;
	        }
	        try {
	            Locale.setDefault(Locale.US);
	            ChainsTraceGraph traceGraph = mainInstance.parseIntoTraceGraph();
	            if (traceGraph == null) {
	                return;
	            }
	            long startTime = loggerInfoStart(
	                    "Creating initial partition graph");
	            PartitionGraph pGraph = new PartitionGraph(traceGraph, false, null);
	            loggerInfoEnd("Creating partition graph took ", startTime);
	            CEFSMs = arrayAdd(CEFSMs, pGraph);
	            nbOfInitial ++;
	        } catch (ParseException e) {
	            throw e;
	        } catch (Exception e) {
	            throw InternalSynopticException.wrap(e);
	        }
	    }
        /* generate a CEFSM for all Tn file */
        for (int c = 1; c < k; ++c) {
        	String[] arg = {"COnfECt/T"+c,"-o","COnfECt/C"+c};
	       	KTailsMain mainInstance = processArgs(arg);
	        if (mainInstance == null) {
	            return;
	        }
	        try {
	            Locale.setDefault(Locale.US);
	            ChainsTraceGraph traceGraph = mainInstance.parseIntoTraceGraph();
	            if (traceGraph == null) {
	                return;
	            }
	            long startTime = loggerInfoStart(
	                    "Creating initial partition graph");
	            PartitionGraph pGraph = new PartitionGraph(traceGraph, false, null);
	            loggerInfoEnd("Creating partition graph took ", startTime);
	            CEFSMs = arrayAdd(CEFSMs, pGraph);
	        } catch (ParseException e) {
	            throw e;
	        } catch (Exception e) {
	            throw InternalSynopticException.wrap(e);
	        }
	    }        
        PartitionGraph[] finalCEFSMs = new PartitionGraph[0];
        if (composition == 1 || composition == 2) {
        	int[] clusters2 = new int[0];
        	try {
        		double[][] compo = CEFSMSimilarity.matrix(CEFSMs);
        		SingleLinkage link2 = new SingleLinkage(compo);
        		HierarchicalClustering clusterise2 = new HierarchicalClustering(link2);
        		clusters2 = clusterise2.partition(similarity2);
        	} catch (IllegalArgumentException e) {
        		clusters2 = new int[CEFSMs.length];
        	}
        	for (int c = 0; c <= maxValue(clusters2); ++c) {
        		ChainsTraceGraph empty = new ChainsTraceGraph();
        		PartitionGraph Final = new PartitionGraph(empty, false, null);       	
        		for (Partition p : Final.getNodes()) {
        			if (p != Final.getDummyInitialNode()) {
        				p.removeAllEventNodes();
        				Final.removePartition(p);
        			}
        		}
        		for (int i = 0; i < clusters2.length; ++i) {
        			if (clusters2[i] == c) {
        				Final.Compose(CEFSMs[i]); 
        			}	        	
        		}
        		finalCEFSMs = arrayAdd(finalCEFSMs, Final);
        	}
        		for (PartitionGraph tree: finalCEFSMs) {
        			Partition initial = tree.getDummyInitialNode();
        			Set<Partition> goodnodes = tree.getNodes();
        			Set<Partition> nodes = goodnodes.stream().collect(Collectors.toSet());
        			int nb = -1;
        			Set<Partition> retrun = new LinkedHashSet<Partition>();
        			for (Partition p: nodes) {
        				Set<Partition> futurs = p.getAllSuccessors();
        				if (!futurs.isEmpty()) {
	        	    		Partition futur = futurs.iterator().next();
	        				if (p.toString().contains("call") && !futur.toString().contains("return")){
	        					String call = p.toString();
	        					call = call.replace("P.", "");
	            				call = call.substring(call.indexOf("T")+1, call.indexOf("."));
	        					nb = clusters2[(nbOfInitial + Integer.parseInt(call) - 1)] + 1;
	        				}
	        				else if (!p.toString().contains("call") && futur.toString().contains("return")) {
	        					retrun.add(futur);
	        				}
        				}
        			}
        			for (Partition p: nodes) {
        				String call = p.toString();
        				call = call.replace("P.", "");
        				if ((call.length() > 6) && (call.substring(0,6).equals("call_T"))) {
        					call = call.substring(call.indexOf("T")+1, call.indexOf("."));
        					if (composition == 1) {
        						Event label = new Event("call_C" + (clusters2[(nbOfInitial + Integer.parseInt(call) - 1)] + 1));
	        					EventNode label2 = new EventNode(label);
	        					Partition label3 = new Partition(label2);
	        					tree.add(label3);
	        					Bisimulation.merge(tree, label3, p);
        					}        					
        					else if (composition == 2) {
        						if (initial.getAllSuccessors().contains(p)) {
        							Event label = new Event("call_C" + (clusters2[(nbOfInitial + Integer.parseInt(call) - 1)] + 1));
    	        					EventNode label2 = new EventNode(label);
    	        					Partition label3 = new Partition(label2);
    	        					tree.add(label3);
    	        					Bisimulation.merge(tree, label3, p);
        						}
        						else {
        							Event label = new Event("call_C");
    	        					EventNode label2 = new EventNode(label);
    	        					Partition label3 = new Partition(label2);
    	        					tree.add(label3);
    	        					Bisimulation.merge(tree, label3, p);
        						}
        					}
        				}
        			}
        			for (Partition p: nodes) {
        				String call = p.toString();
        				call = call.replace("P.", "");
        				if ((call.length() > 6) && (call.substring(0,8).equals("return_T"))) {
        					call = call.substring(call.indexOf("T")+1, call.indexOf("."));
        					if (composition == 1) {
        						Event label = new Event("return_C" + (clusters2[(nbOfInitial + Integer.parseInt(call) - 1)] + 1));
	        					EventNode label2 = new EventNode(label);
	        					Partition label3 = new Partition(label2);
	        					tree.add(label3);
	        					Bisimulation.merge(tree, label3, p);
	        				}
        					else if (composition == 2 && nb == clusters2[(nbOfInitial + Integer.parseInt(call) - 1)] + 1
        							 && retrun.contains(p)) {
        						Event label = new Event("return_C" + (clusters2[(nbOfInitial + Integer.parseInt(call) - 1)] + 1));
	        					EventNode label2 = new EventNode(label);
	        					Partition label3 = new Partition(label2);
	        					tree.add(label3);
	        					Bisimulation.merge(tree, label3, p);
        					}
        					else if (composition == 2) {
        						Event label = new Event("return_C");
	        					EventNode label2 = new EventNode(label);
	        					Partition label3 = new Partition(label2);
	        					tree.add(label3);
	        					Bisimulation.merge(tree, label3, p);
        					}
        					
        				}
        			}
        	}
        	if (composition == 1) {
        		for (PartitionGraph tree: finalCEFSMs) {
        			tree = CEFSMSimilarity.loopCall(tree);
        		}
        	}
        	if (composition == 2) {
        		for (PartitionGraph tree: finalCEFSMs) {
        			tree = CEFSMSimilarity.suppCall(tree);
        		}
        	}
        }
        else { 
        	finalCEFSMs = CEFSMs;
        }
        /* generate the dot file */
        int i = 1;
        double timeKtail = 0;
        for (PartitionGraph tree: finalCEFSMs) {
        	String[] out = {"*.log", "-o", "COnfECt/final" + i};
        	KTailsMain mainInstance = processArgs(out);
        	long tempsDebut = System.nanoTime(); 
        	runKTails(tree, 2); /* cette fonction ne fait que le merge de Ktail */
        	long tempsFin = System.nanoTime();
        	double seconds = (double) (tempsFin - tempsDebut) / (double) 1000000000; 
        	System.out.println("time K-tail :" + seconds);
        	timeKtail = timeKtail + seconds;
    	    mainInstance.exportGraph(tree);
    	    ++i;
        }
        
        /* put label on transition like real ktail */
        for (i = 1; i <= finalCEFSMs.length; ++i) {
        	Clean.eraseMe("COnfECt/final" + i + ".dot", "COnfECt/postErase" + i + ".dot");        	
        	if (composition == 0) {
        		Clean.labelOnTransitions("COnfECt/postErase" + i + ".dot", "COnfECt/T" + i + ".dot");
        		Clean.makeStrict("COnfECt/T" + i + ".dot", "COnfECt/C" + i + ".dot", bstrict);
        		
            	GraphExporter.generatePngFileFromDotFile("COnfECt/C" + i + ".dot");  
            }
        	else {
        		Clean.labelOnTransitions("COnfECt/postErase" + i + ".dot", "COnfECt/transfinal" + i + ".dot");
        		//GraphExporter.generatePngFileFromDotFile("COnfECt/transfinal" + i + ".dot");  /// tempo
        	}
        	//GraphExporter.generatePngFileFromDotFile("COnfECt/transfinal" + i + ".dot");  */
        	new File("COnfECt/final" + i + ".dot").delete();
        	new File("COnfECt/postErase" + i + ".dot").delete();
        }
        if (composition == 1) {
           	// draw loop on dots file
        	for (i = 1; i <= finalCEFSMs.length; ++i) {
        		Clean.makeLoop("COnfECt/transfinal" + i + ".dot", "COnfECt/C" + i + ".dot");
        		new File("COnfECt/transfinal" + i + ".dot").delete();
        		GraphExporter.generatePngFileFromDotFile("COnfECt/C" + i + ".dot");
        	}
        }   
        if (composition == 2) {
           	// draw loop on dots file 
        	for (i = 1; i <= finalCEFSMs.length; ++i) {
        		Clean.makeLoopStrong("COnfECt/transfinal" + i + ".dot", "COnfECt/C" + i + ".dot");
        		new File("COnfECt/transfinal" + i + ".dot").delete();
        		GraphExporter.generatePngFileFromDotFile("COnfECt/C" + i + ".dot");
        	}
        }
        i = i - 1;
        Utility.getStats(i);     
        
        i = 1;
    	for (PartitionGraph tree: finalCEFSMs) {
    		GraphExporter.generatePngFileFromDotFile("COnfECt/hideCall" + i + ".dot");
    		++i;
    	}
    	
        System.out.println("total time Ktail :" + timeKtail + " seconds");
    	System.out.println("THE END");
    }
    
    /* added, get the max of the array */
    private static int maxValue(int[] number) {
        int max = number[0];
        for (int ktr = 0; ktr < number.length; ktr++) {
            if (number[ktr] > max) {
                max = number[ktr];
            }
        }
        return max;
    }

    /* add a pGraph in the Array */
    public static PartitionGraph[] arrayAdd(PartitionGraph[] originalArray, PartitionGraph newItem)
	{
  		int currentSize = originalArray.length;
  		int newSize = currentSize + 1;
	    PartitionGraph[] tempArray = new PartitionGraph[ newSize ];
 	    for (int i=0; i < currentSize; i++){
    	    tempArray[i] = originalArray [i];
   		}
  		tempArray[newSize - 1] = newItem;
    	return tempArray;   
	}
    
    /* added, get list of traces */
    private static String[] getTraces(String[] args){
        String[] traces = new String[0];
        for (String arg: args){
            int end = arg.length();
            if ( end > 3 && arg.substring(end - 4, end).equals(".log")){
                traces = Correlation.stringAdd(traces, arg);
            }
        }
        return traces;
    }
        
    /**
     * Parses the set of arguments to the program, to set up static state in
     * KTailsMain. This state includes everything necessary to run Synoptic --
     * input log files, regular expressions, etc. Returns null if there is a
     * problem with the parsed options.
     * 
     * @param args
     *            Command line arguments that specify how Synoptic should
     *            behave.
     * @return
     * @throws IOException
     * @throws URISyntaxException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws ParseException
     */
    public static KTailsMain processArgs(String[] args)
            throws IOException, URISyntaxException, IllegalArgumentException,
            IllegalAccessException, ParseException {
        // Parse and process command line options
        AbstractOptions options = new KTailsOptions(args).toAbstractOptions();
        GraphExportFormatter graphExportFormatter = processArgs(options);
        if (graphExportFormatter == null) {
            return null;
        }

        // Construct and return main object
        KTailsMain newMain = new KTailsMain(options, graphExportFormatter);
        return newMain;
    }

    /**
     * Constructor that simply stores parameters in fields and initializes the
     * pseudo RNG.
     * 
     * @param opts
     *            Processed options from the command line
     * @param graphExportFormatter
     *            Graph export formatter for outputting the model
     */
    public KTailsMain(AbstractOptions opts,
            GraphExportFormatter graphExportFormatter) {
        setUpLogging(opts);

        /*if (AbstractMain.instance != null) {
            throw new RuntimeException(
                    "Cannot create multiple instance of singleton synoptic.main.AbstractMain");
        }*/
        this.options = opts;
        this.graphExportFormatter = graphExportFormatter;
        this.random = new Random(opts.randomSeed);
        logger.info("Using random seed: " + opts.randomSeed);
        AbstractMain.instance = this;
    }

    @Override
    public TemporalInvariantSet mineTOInvariants(
            boolean useTransitiveClosureMining, ChainsTraceGraph traceGraph) {
        return new TemporalInvariantSet();
    }

    /**
     * Run the k-tails algorithm on {@code pGraph} using k-equivalence for the
     * specified {@code k} value
     * 
     * @param pGraph
     * @param k
     */
    private static void runKTails(PartitionGraph pGraph, int k) {
        long startTime = loggerInfoStart("Running k-tails...");
        Bisimulation.mergePartitions(pGraph, null, k);
        loggerInfoEnd("K-tails took ", startTime);
    }
            
}