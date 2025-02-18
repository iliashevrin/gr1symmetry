package processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import org.apache.commons.math3.stat.regression.SimpleRegression;

public class SymmetryEvaluationProcessor {
	
	public static boolean core;
	public static String resultsPath;
	public static String mutationInfoPath;
	
	public static int mutationsNum;
	public static List<Integer> parameters;
	public static Set<Integer> slowMutations = new HashSet<>();
	public static int totalDataPoints;
	
	public static Set<Integer> realMutations = new HashSet<>();
	
	public static List<Double> symNoSymBases = new ArrayList<>(); 
	public static List<Double> symDRDNoSymBases = new ArrayList<>();
	public static List<Double> symNoSymAverageRatios = new ArrayList<>(); 
	public static List<Double> symDRDNoSymAverageRatios = new ArrayList<>(); 
	public static List<Double> symNoSymMaxRatios = new ArrayList<>();
	public static List<Double> symDRDNoSymMaxRatios = new ArrayList<>();
	
	public static int noSymTimeouts = 0;
	public static int symTimeouts = 0;
	public static int symDRDTimeouts = 0;
	
	public static final double TIMEOUT = -1;
	public static final int SLOW_THRESHOLD = 60; // In seconds
	
	public static final int BEST_PERCENTAGE = 95;
	
	public static Set<String> getSpecsList() {
		
		Set<String> allSpecs = new HashSet<>();
       
		for (File file : new File(resultsPath).listFiles()) {
			if (file.isFile() && file.getName().contains(".spectra")) {
				allSpecs.add(file.getName().substring(0, file.getName().indexOf(".spectra")) + ".spectra");
            }
        }

		return allSpecs;
	}
	
	public static void computeSingleSpec(String spec) {
		
		slowMutations = new HashSet<>();
		realMutations = new HashSet<>();
		
		symNoSymBases = new ArrayList<>(); 
		symDRDNoSymBases = new ArrayList<>();
		symNoSymAverageRatios = new ArrayList<>(); 
		symDRDNoSymAverageRatios = new ArrayList<>(); 
		symNoSymMaxRatios = new ArrayList<>();
		symDRDNoSymMaxRatios = new ArrayList<>();
		
		noSymTimeouts = 0;
		symTimeouts = 0;
		symDRDTimeouts = 0;
		
		String fullName = resultsPath + File.separator + spec;
		
		parseRealMutations(String.format("%s%s_mutations.csv", mutationInfoPath, spec));
		
		String noSym = String.format("%s_NO_SYMMETRY_%sresults.csv", fullName, core ? "core_" : "");
		String sym = String.format("%s_SYMMETRY_%sresults.csv", fullName, core ? "core_" : "");
		String symDRD = String.format("%s_SYMMETRY_DISABLE_REORDER_%sresults.csv", fullName, core ? "core_" : "");

		Map<Integer, List<Double>> noSymResults = parseResults(noSym);
		Map<Integer, List<Double>> symResults = parseResults(sym);
		Map<Integer, List<Double>> symDRDResults = parseResults(symDRD);
		
		Map<Integer, List<Double>> symNoSymRatio = new HashMap<>();
		Map<Integer, List<Double>> symDRDNoSymRatio = new HashMap<>();
		
		parameters = noSymResults.keySet().stream().sorted().toList();
		mutationsNum = noSymResults.get(parameters.get(0)).size();
		

		
		totalDataPoints = mutationsNum * parameters.size();
		
		for (Integer parameter : parameters) {
			
			symNoSymRatio.put(parameter, new ArrayList<>());
			symDRDNoSymRatio.put(parameter, new ArrayList<>());
			
			// i is the mutation variant index
			for (int i = 0; i < mutationsNum; i++) {
				
				Double currNoSym = noSymResults.get(parameter).get(i);
				Double currSym = symResults.get(parameter).get(i);
				Double currSymDRD = symDRDResults.get(parameter).get(i);
				
				if (currNoSym == TIMEOUT) {
					noSymTimeouts++;
				}
				
				if (currSym == TIMEOUT) {
					symTimeouts++;
				}
				
				if (currSymDRD == TIMEOUT) {
					symDRDTimeouts++;
				}
				
				if (currNoSym != TIMEOUT) {
					
					if (currSym != TIMEOUT) {
						symNoSymRatio.get(parameter).add(currSym / currNoSym);
					} else {
						symNoSymRatio.get(parameter).add(Double.POSITIVE_INFINITY);
					}
					if (currSymDRD != TIMEOUT) {
						symDRDNoSymRatio.get(parameter).add(currSymDRD / currNoSym);
					} else {
						symDRDNoSymRatio.get(parameter).add(Double.POSITIVE_INFINITY);
					}

				} else {

					if (currSym != TIMEOUT) {
						symNoSymRatio.get(parameter).add(0d);
					} else {
						symNoSymRatio.get(parameter).add(TIMEOUT);
					}
					if (currSymDRD != TIMEOUT) {
						symDRDNoSymRatio.get(parameter).add(0d);
					} else {
						symDRDNoSymRatio.get(parameter).add(TIMEOUT);
					}
				}
				
				if (currNoSym > SLOW_THRESHOLD || currNoSym == TIMEOUT) {
					slowMutations.add(i);
				}
			}
		}
		
		computeStatistics(symNoSymRatio, symNoSymBases, symNoSymAverageRatios, symNoSymMaxRatios);
		computeStatistics(symDRDNoSymRatio, symDRDNoSymBases, symDRDNoSymAverageRatios, symDRDNoSymMaxRatios);
		
	}

	public static void main(String[] args) {
		
		resultsPath = args[0];
		mutationInfoPath = args[1];
		core = Boolean.parseBoolean(args[2]);
		
		if (args.length == 3) {
			
			int realOrigTimeouts = 0;
			int realSymTimeouts = 0;
			int realSymDRDTimeouts = 0;
			int unrealOrigTimeouts = 0;
			int unrealSymTimeouts = 0;
			int unrealSymDRDTimeouts = 0;
			
			int realSymNoSymGoodBase = 0;
			int realSymDRDNoSymGoodBase = 0;
			int unrealSymNoSymGoodBase = 0;
			int unrealSymDRDNoSymGoodBase = 0;
			
			int realSymNoSymGoodRatio = 0;
			int realSymDRDNoSymGoodRatio = 0;
			int unrealSymNoSymGoodRatio = 0;
			int unrealSymDRDNoSymGoodRatio = 0;
			
			List<Double> realSymNoSymAvgRatios = new ArrayList<>();
			List<Double> realSymDRDNoSymAvgRatios = new ArrayList<>();
			List<Double> unrealSymNoSymAvgRatios = new ArrayList<>();
			List<Double> unrealSymDRDNoSymAvgRatios = new ArrayList<>();
			
			double realSymNoSymAvgRatio = 0;
			double realSymDRDNoSymAvgRatio = 0;
			double unrealSymNoSymAvgRatio = 0;
			double unrealSymDRDNoSymAvgRatio = 0;
			
			int allReal = 0;
			int allUnreal = 0;
			
			int allRealVariants = 0;
			int allUnrealVariants = 0;
			
			Set<String> allSpecs = getSpecsList();
			
			for (String spec : allSpecs) {
				
				computeSingleSpec(spec);
				
				Set<Integer> unreal = new HashSet<>(IntStream.range(0, mutationsNum).filter(i -> !realMutations.contains(i)).boxed().toList());
				
				allReal += realMutations.size();
				allUnreal += unreal.size();
				
				allRealVariants += realMutations.size() * parameters.size();
				allUnrealVariants += unreal.size() * parameters.size();
				
				double[] realNumbers = getNumbers(realMutations, symNoSymBases, symNoSymAverageRatios, symNoSymMaxRatios);
				double[] unrealNumbers = getNumbers(unreal, symNoSymBases, symNoSymAverageRatios, symNoSymMaxRatios);
				
				realSymNoSymAvgRatios.addAll(realMutations.stream().map(i -> symNoSymAverageRatios.get(i)).toList());
				realSymNoSymGoodBase += realNumbers[1];
				realSymNoSymGoodRatio += realNumbers[2];
				
				unrealSymNoSymAvgRatios.addAll(unreal.stream().map(i -> symNoSymAverageRatios.get(i)).toList());
				unrealSymNoSymGoodBase += unrealNumbers[1];
				unrealSymNoSymGoodRatio += unrealNumbers[2];
				
				realOrigTimeouts += noSymTimeouts;
				realSymTimeouts += symTimeouts;
				realSymDRDTimeouts += symDRDTimeouts;
				
				
				realNumbers = getNumbers(realMutations, symDRDNoSymBases, symDRDNoSymAverageRatios, symDRDNoSymMaxRatios);
				unrealNumbers = getNumbers(unreal, symDRDNoSymBases, symDRDNoSymAverageRatios, symDRDNoSymMaxRatios);
				
				realSymDRDNoSymAvgRatios.addAll(realMutations.stream().map(i -> symDRDNoSymAverageRatios.get(i)).toList());
				realSymDRDNoSymGoodBase += realNumbers[1];
				realSymDRDNoSymGoodRatio += realNumbers[2];

				unrealSymDRDNoSymAvgRatios.addAll(unreal.stream().map(i -> symDRDNoSymAverageRatios.get(i)).toList());
				unrealSymDRDNoSymGoodBase += unrealNumbers[1];
				unrealSymDRDNoSymGoodRatio += unrealNumbers[2];
				
				unrealOrigTimeouts += noSymTimeouts;
				unrealSymTimeouts += symTimeouts;
				unrealSymDRDTimeouts += symDRDTimeouts;
				
			}
			
			realSymNoSymAvgRatio = avgKeepBest(realSymNoSymAvgRatios, BEST_PERCENTAGE);
			unrealSymNoSymAvgRatio = avgKeepBest(unrealSymNoSymAvgRatios, BEST_PERCENTAGE);
			
			realSymDRDNoSymAvgRatio = avgKeepBest(realSymDRDNoSymAvgRatios, BEST_PERCENTAGE);
			unrealSymDRDNoSymAvgRatio = avgKeepBest(unrealSymDRDNoSymAvgRatios, BEST_PERCENTAGE);
			
			System.out.println("Real");
			System.out.println("------");
			
			System.out.println("Real. Orig timeouts  " + prettyPrintPercentage(realOrigTimeouts, allRealVariants));
			System.out.println("Real. Sym timeouts  " + prettyPrintPercentage(realSymTimeouts, allRealVariants));
			System.out.println("Real. SymDRD timeouts  " + prettyPrintPercentage(realSymDRDTimeouts, allRealVariants));
			
			System.out.println("Real. Sym base<1  " + prettyPrintPercentage(realSymNoSymGoodBase, allReal));
			System.out.println("Real. SymDRD base<1  " + prettyPrintPercentage(realSymDRDNoSymGoodBase, allReal));
			
			System.out.println("Real. Sym max ratio<1  " + prettyPrintPercentage(realSymNoSymGoodRatio, allReal));
			System.out.println("Real. SymDRD max ratio<1  " + prettyPrintPercentage(realSymDRDNoSymGoodRatio, allReal));
			
			System.out.println("Real. Sym avg ratio  " + prettyPrint(realSymNoSymAvgRatio));
			System.out.println("Real. SymDRD avg ratio  " + prettyPrint(realSymDRDNoSymAvgRatio));
			
			System.out.println();
			System.out.println("Unreal");
			System.out.println("------");
			
			System.out.println("Unreal. Orig timeouts  " + prettyPrintPercentage(unrealOrigTimeouts, allUnrealVariants));
			System.out.println("Unreal. Sym timeouts  " + prettyPrintPercentage(unrealSymTimeouts, allUnrealVariants));
			System.out.println("Unreal. SymDRD timeouts  " + prettyPrintPercentage(unrealSymDRDTimeouts, allUnrealVariants));
			
			System.out.println("Unreal. Sym base<1  " + prettyPrintPercentage(unrealSymNoSymGoodBase, allUnreal));
			System.out.println("Unreal. SymDRD base<1  " + prettyPrintPercentage(unrealSymDRDNoSymGoodBase, allUnreal));
			
			System.out.println("Unreal. Sym max ratio<1  " + prettyPrintPercentage(unrealSymNoSymGoodRatio, allUnreal));
			System.out.println("Unreal. SymDRD max ratio<1  " + prettyPrintPercentage(unrealSymDRDNoSymGoodRatio, allUnreal));
			
			System.out.println("Unreal. Sym avg ratio  " + prettyPrint(unrealSymNoSymAvgRatio));
			System.out.println("Unreal. SymDRD avg ratio  " + prettyPrint(unrealSymDRDNoSymAvgRatio));
			
		} else {
			
			String specName = args[3];
			
			computeSingleSpec(specName);
			
			System.out.println(specName);
			System.out.println("");
			System.out.println("Slowest mutations 		= " + prettyPrintPercentage(slowMutations.size(), mutationsNum));
			System.out.println("NoSym finished 			= " + prettyPrintPercentage(totalDataPoints-noSymTimeouts, totalDataPoints));
			System.out.println("");
			
			System.out.println("Sym / NoSym");
			System.out.println("===========");
			printStatistics(symNoSymBases, symNoSymAverageRatios, symNoSymMaxRatios, symTimeouts);
			System.out.println("");
			
			System.out.println("SymDRD / NoSym");
			System.out.println("==============");
			printStatistics(symDRDNoSymBases, symDRDNoSymAverageRatios, symDRDNoSymMaxRatios, symDRDTimeouts);
			System.out.println("");
		}
		

	}
	
	public static double[] getNumbers(Set<Integer> indexes, List<Double> bases, List<Double> averageRatios, List<Double> maxRatios) {
		
		List<Double> filteredBases = indexes.stream().map(i -> bases.get(i)).toList();
		List<Double> filteredAverageRatios = indexes.stream().map(i -> averageRatios.get(i)).toList();
		List<Double> filteredMaxRatios = indexes.stream().map(i -> maxRatios.get(i)).toList();
		
		return getNumbers(filteredBases, filteredAverageRatios, filteredMaxRatios);
	}
	
	public static double[] getNumbers(List<Double> bases, List<Double> averageRatios, List<Double> maxRatios) {
		
		double meanRatio = avgKeepBest(averageRatios, BEST_PERCENTAGE);
		long goodBases = bases.stream().filter(b -> b < 1).count();
		long goodMax = maxRatios.stream().filter(r -> r < 1 && r != TIMEOUT).count();
		
		return new double[] {meanRatio, (double) goodBases, (double) goodMax};
	}
	
	public static void printStatistics(double[] numbers, List<Double> bases, List<Double> averageRatios, List<Double> maxRatios) {

	
		
		System.out.println("avg ratio mean 			= " + prettyPrint(numbers[0]));
		System.out.println("base<1 ratio 			= " + prettyPrintPercentage((long) numbers[1], bases.size()));
		System.out.println("max<1 ratio 			= " + prettyPrintPercentage((long) numbers[2], maxRatios.size()));
		System.out.println("");
	}
	
	public static void printStatistics(List<Double> bases, List<Double> averageRatios, List<Double> maxRatios, long timeouts) {
		
		System.out.println("All Mutations");
		System.out.println("=============");
		
		double[] numbers = getNumbers(bases, averageRatios, maxRatios);
		printStatistics(numbers, bases, averageRatios, maxRatios);
		
		
		System.out.println("Slowest Mutations");
		System.out.println("=================");
		
		numbers = getNumbers(slowMutations, bases, averageRatios, maxRatios);
		printStatistics(numbers, bases, averageRatios, maxRatios);
		
		System.out.println("Finished 			= " + prettyPrintPercentage(totalDataPoints-timeouts, totalDataPoints));
	}
	
	
	public static String prettyPrint(double val) {
		if (val > 1) {
			return (">1");
		}
		return String.format("%.2f", val);
	}
	
	public static String prettyPrintPercentage(long nom, int denom) {
		return String.format("%.2f%% (%d / %d)", ((double) nom / denom) * 100, nom, denom);
	}
	
	public static void computeStatistics(Map<Integer, List<Double>> ratio, List<Double> base, List<Double> avg, List<Double> max) {
		
		for (int i = 0; i < mutationsNum; i++) {
			
			List<Double> values = new ArrayList<>();
			
			for (Integer parameter : parameters) {
				values.add(ratio.get(parameter).get(i));	
			}
			
			base.add(getBase(values));
			avg.add(getStatistics(values)[1]);
			max.add(getMax(values));
		}
	}
	
	public static boolean isValid(Double val) {
		return val > 0 && !val.equals(Double.POSITIVE_INFINITY) && !val.equals(Double.NaN);
	}
	
	public static double avgKeepBest(List<Double> values, int percentage) {
		
		List<Double> temp = new ArrayList<>(values.stream().filter(v -> isValid(v)).toList());
		
		temp.sort(Double::compare);
		
		int count = (temp.size() * percentage) / 100;
		
		return getStatistics(temp.subList(0, count))[1];
	}
	
	public static double[] getStatistics(List<Double> values) {
		
		long noTimeout = values.stream().filter(v -> isValid(v)).count();
		double sum = 0;
		for (Double value : values) {
			if (isValid(value)) {
				sum += value;
			}
		}
		
		double mean = sum / noTimeout;
		double sum2 = 0;
		for (Double value : values) {
			if (isValid(value)) {
				sum2 += Math.pow(value - mean, 2);
			}
		}
		
		double std = Math.sqrt(sum2 / noTimeout);
		return new double[] {sum,mean,std};
	}
	
	public static double getMax(List<Double> values) {
		double max = 0;
		for (Double value : values) {
			if (value > max) {
				max = value;
			}
		}
		return max;
	}
	
	public static double getBase(List<Double> values) {

        SimpleRegression s = new SimpleRegression();

		for (int i = 0; i < parameters.size(); i++) {
			if (isValid(values.get(i))) {
				s.addData(parameters.get(i), Math.log(values.get(i)));
			}
		}

        return Math.exp(s.getSlope()); // e^c = m
	}
	
	public static boolean isError(String value) {
		
		if ("true".equals(value.strip()) || "false".equals(value.strip())) {
			return false;
		}
		
		try {
			Double.parseDouble(value);
			return false;
		} catch (NumberFormatException e) {
			return true;
		}
	}
	
	public static void parseRealMutations(String fileName) {
		
		boolean firstLine = true;
		String line = "";
		int index = 0;
		
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			
            while ((line = br.readLine()) != null) {
            	
            	// First line is the header line, skip it
            	if (firstLine) {
            		firstLine = false;
            		continue;
            	}
            	
            	
                String[] values = line.split(",");
                
                if (Boolean.parseBoolean(values[0].strip())) {
                	realMutations.add(index);
                }
                
                index++;
                
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public static Map<Integer, List<Double>> parseResults(String fileName) {
		
		// The integer is the N parameter
		Map<Integer, List<Double>> results = new HashMap<>();
		
		boolean firstLine = true;
		String line = "";
		
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			
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
                }
                
                // Timeout or other error
                if (isError(values[3])) {
                	results.get(N).add(TIMEOUT);
                } else {
                    // Core time for core comp. or total time for real. check.
                    results.get(N).add(Double.parseDouble(values[core ? 4 : 5].strip()));
                }
                
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		return results;
	}

}
