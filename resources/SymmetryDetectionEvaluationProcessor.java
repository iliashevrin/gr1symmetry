package processor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymmetryDetectionEvaluationProcessor extends SymmetryEvaluationProcessor {

	public static final double INVALID = -1;
	
	public static void main(String[] args) {
		

		// The integer is the N parameter
		Map<Integer, List<Double>> results = new HashMap<>();
		Map<Integer, Integer> best = new HashMap<>();
		Map<Integer, Integer> timeouts = new HashMap<>();
		
		boolean firstLine = true;
		String line = "";
		
		try (BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
			
            while ((line = br.readLine()) != null) {
            	
            	// First line is the header line, skip it
            	if (firstLine) {
            		firstLine = false;
            		continue;
            	}
            	
                String[] values = line.split(",");
                
                int N = Integer.parseInt(values[1].strip());
                
                if (!results.containsKey(N)) {
                	results.put(N, new ArrayList<>());
                	best.put(N, 0);
                	timeouts.put(N, 0);
                }
                
                // Timeout or other error
                if (isError(values[3])) {
                	if (values[3].contains("TimeoutException")) {
                		results.get(N).add(600d);
                		timeouts.put(N, timeouts.get(N) + 1);
                	}
                	results.get(N).add(INVALID);
                } else {
                    results.get(N).add(Double.parseDouble(values[3].strip()));
                    
                    best.put(N, best.get(N) + (int)Double.parseDouble(values[4]));
                }
                
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		System.out.println("Results for " + args[0]);
		
		for (Integer N : results.keySet()) {
			
			Double avg = avgKeepBest(results.get(N), 100);
			if (avg.equals(Double.NaN)) {
				System.out.println("Not a symmetry " + N);
				continue;
			}
			if (avg == 600d) {
				avg = Double.NaN;
			}
			System.out.println("Average of " + N + " = " + String.format("%.2f", avg));
			System.out.println("Timeouts of " + N + " = " + timeouts.get(N));
			System.out.println("Number of early termination " + N + " = " + best.get(N));
		}

	}


}
