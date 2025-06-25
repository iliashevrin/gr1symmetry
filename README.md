Supplementary files for TOSEM 2025 submission "Leveraging Symmetry in GR(1) Synthesis"

# Repository Contents


## resources
This directory contains tools to run the evaluation and reproduce the results.

1. **specs**: the directory of the benchmark specifications. Each Spectra specification has a token `$PARAM$` that is replaced during the test with the parameter number
2. **specs/mutation info**: this directory contains a csv file for each benchmark specification with data on realizable/unrealizable mutations
3. **syntech**: the directory of the SYNTECH specifications.
4. **realizability_evaluation.jar**: measures GR(1) realizability checking time
5. **cores_evaluation.jar**: measures unrealizable/assumptions core computation time
6. **realizability_evaluation_syntech.jar**: measures GR(1) realizability checking time, suited for SYNTECH specifications (without a configurable `$PARAM$`)
7. **cores_evaluation.jar_syntech**: measures unrealizable/assumptions core computation time, suited for SYNTECH specifications (without a configurable `$PARAM$`)
7. **gr1performancetest.jar**: required jar for the above evaluation jars (runs a single execution of real.check/core.comp. algorithm as a separate process)
8. **gr1performancetest_syntech.jar**: required jar for the above evaluation jars (runs a single execution of real.check/core.comp. algorithm as a separate process), suited for SYNTECH specifications where the symmetry annotation is not present in the file but detected on-the-fly
9. **detection_evaluation.jar**: measures GR(1) detection time
10. **detection_evaluation_syntech.jar**: measures GR(1) detection time, suited for SYNTECH specifications (without a configurable `$PARAM$`)
11. **gr1symmetrydetectiontest.jar**: required jar for the detection jar (runs a single execution of detection as a separate process)
12. **libcudd.so (and cudd.dll)**: compiled version of the CUDD library that should be present in the same direction while running any of the evaluation jars
13. **libz3.so and libz3java.so**: compiled version of Z3 SAT solver library and Java binding that should be present in the same direction while running the detection jar
14. **symmetry_leveraging_results_processor.jar**: prints to console the output of the statistical analysis for the real.check/core.comp results
15. **symmetry_detection_results_processor.jar**: prints to console the output of the statistical analysis for the symmetry detection results
16. **SymmetryEvaluationProcessor.java**: the source code of the symmetry leveraging evaluation result processing script
17. **SymmetryDetectionEvaluationProcessor.java**: the source code of the symmetry detection evaluation result processing script
        
        
## symmetry_leveraging_results
This directory contains running times for the realizability checking and core computation.

1. **realizability checking**: this directory contains CSV files that are the output of realizability_evaluation.jar, for each specification in the specs directory, for 3 versions: Orig, Sym, SymD
2. **cores computation**: this directory contains CSV files that are the output of cores_evaluation.jar, for each specification in the specs directory, for 3 versions: Orig, Sym, SymD
3. **normalized n tables**: this directory contains CSV files that are used to compute the normalized N tables (tables 5 and 6) from the paper

## symmetry_detection_results
This directory contains CSV files that are the output of detection_evaluation.jar, for each specification in the specs directory, for 2 versions: syntax-aware and semantic

## syntech_evaluation_results
This directory contains results for the SYNTECH benchmark evaluation.
1. CSV files that are the output of detection_evaluation_syntech.jar, for each specification in the syntech directory, for 2 versions: syntax-aware and semantic
2. CSV files that are the output of realizability_evaluation_syntech.jar, for each specification in the syntech directory, for 3 versions: Orig, Sym, SymD
3. CSV files that are the output of cores_evaluation_syntech.jar, for each specification in the syntech directory, for 3 versions: Orig, Sym, SymD

## parametric prevalence
This directory contains data for our preliminary research question on the prevalence of parametric specification
1. **LTL-Synth**: directory of the TLSF specs used in the SYNTCOMP competition
2. **ltlsynt_syntcomp_measure.sh and strix_syntcomp_measure.sh**: scripts used to run both tools on the SYNTCOMP specifications
3. **ltlsynt_execution_times.csv and strix_execution_times.csv**: tables with realizability checking runtimes for both tools
4. **final_execution_times.xlsx**: table assembled from the results of both tools including the figures reported in the paper
5. **TLSF from Spectra**: specifications in TLSF that were manually translated from Spectra. Used to show that GR(1) synthesis tools like Spectra perform better on similar specifications than ltlsynt and Strix, which are LTL synthesis tools


# Executing the symmetry leveraging evaluation JARs

Make sure that your session recognizes the CUDD library by running the following command in the terminal before executing the JARs
        
`export LD_LIBRARY_PATH=[PATH_TO_LIBCUDD_SO]`

If running in Windows, make sure that the JAR is in the same directory of cudd.dll

In order to run a test (real. check. or core comp.) for a single specification including all its mutants, run the following command in a terminal

`java -jar [realizability_evaluation.jar | cores_evaluation.jar] <PATH> <MODE> <REPEATS> <PARAMETERS>`

1. **PATH**: path of the specification with a $PARAM$ token
2. **MODE**: has to be one of the following 3 strings, depending on the desired GR(1) real.check/core.comp algorithm:
   1. NO_SYMMETRY                     (Orig)
   2. SYMMETRY                        (Sym)
   3. SYMMETRY_DISABLE_REORDER        (SymD)
3. **REPEATS**: number of repeats for every parameter value. We set it to 10 in our experiments
4. **PARAMETERS**: a list of parameters to replace the $PARAM$ token in the specification


Example execution:

`java -jar realizability_evaluation.jar specs/amba.spectra SYMMETRY_DISABLE_REORDER 5 8 9 10`

This will run realizability checking for all the mutants of the AMBA specification file, using the SymD algorithm, repeating 5 times every parameter value, for parameter N of sizes 8, 9, and 10

The result of a realizability checking evaluation is a CSV file in the following name format:	`<NAME>_<MODE>_results.csv`

The result of a cores computation evaluation is a CSV file in the following name format:	`<NAME>_<MODE>_core_results.csv`

The above example command will generate the file amba.spectra_SYMMETRY_DISABLE_REORDER_results.csv


# Symmetry Leveraging Evaluation Results Format

The content of every csv file is a table with the measurement data.

The columns of a realizability checking csv file are:

1. **Spec**: path of the specification
2. **Size**: the value of N
3. **Remove**: the index of the constraint removed to obtain the mutant. The value -1 indicates the original specification
4. The time for every mutant measurement is divided into Prep. (symmetry validation and BDD construction) and Real. Check columns
5. **Total**: the sum of the two values in previous two columns. This is the value that we report in the paper. All values are an average of the configured number of repeats
	  

The columns of a cores computation csv file are:

1. **Spec**: path of the specification.
2. **Size**: the value of N.
3. **Remove**: the index of the constraint removed to obtain the mutant. The value -1 indicates the original specification
4. **Is Real.**: if true then assumptions core is computed for the mutant, is false then unrealizability core
5. **Total**: average time of the configured number of repeats

# Executing the symmetry detection evaluation JARs

Make sure that your session recognizes the CUDD library by running the following command in the terminal before executing the JARs
        
`export LD_LIBRARY_PATH=[PATH_TO_LIBCUDD_SO]`

If running in Windows, make sure that the JAR is in the same directory of cudd.dll
        
Also, make sure your session recognizes the Z3 library by running
        
`export LD_LIBRARY_PATH=[PATH_TO_LIBZ3_SO]`
        
Preferably, both CUDD and Z3 libraries should reside in the same directory

In order to run a single test, run the following command in a terminal

`java -jar detection_evaluation.jar <PATH> <MODE> <REPEATS> <PARAMETERS>`

1. **PATH**: path of the specification with a `$PARAM$` token
2. **MODE**: has to be one of the following 2 strings, depending on the desired detection algorithm:
   1. SYNTAX_AWARE_DETECTION
   2. SEMANTIC_DETECTION
3. **REPEATS**: number of repeats for every parameter value. We set it to 10 in our experiments
4. **PARAMETERS**: a list of parameters to replace the $PARAM$ token in the specification

Example execution:

`java -jar detection_evaluation.jar SYNTAX_AWARE_DETECTION specs/amba.spectra 5 8 9 10`

This will run symmetry detection for AMBA specification file, using the syntax-aware heuristic, repeating 5 times every parameter value, for parameter N of sizes 8, 9, and 10

The result of a detection evaluation is a CSV file in the following name format:	`<NAME>_<MODE>_results.csv`

The above example command will generate the file amba.spectra_SYNTAX_AWARE_DETECTION_results.csv

# Symmetry Detection Evaluation Results Format

The content of every csv file is a table with the measurement data.

The columns of a symmetry detection csv file are:

1. **Spec**: path of the specification
2. **Size**: the value of N
3. **Remove**: the index of the constraint removed to obtain the mutant. The value -1 indicates the original specification
4. **Time**: average time of the configured number of repeats
5. **Best?**: has the detected symmetry the maximum attainable symmetry score?


# Executing the processing JAR

In order to process the result of a single symmetry leveraging evaluation csv file / a directory containing csv files run the following command in a terminal:

`java -jar symmetry_leveraging_results_processor.jar <PATH_TO_RESULTS> <PATH_TO_MUTATION_INFO> <IS_CORE> [SPEC]`
        
1. **PATH_TO_RESULTS**: path to a directory containing csv results, either realizability checking or cores computation
2. **PATH_TO_MUTATION_INFO**: path to a directory containing data on which mutations are realizable and which are not
3. **IS_CORE**: true for core computation results, false for realizability check results
4. **SPEC**: optional name of the specification file. If omitted, then the JAR prints the real/unreal division figures concerning all specifications in the directory, otherwise prints figures for a single specification
	
Example execution:

`java -jar symmetry_leveraging_results_processor.jar "results/realizability checking/" "tests/specs/mutation info/" false genbuf.spectra`
        
This will print processed results for the realizability checking evaluaton data of the GenBuf specification
	
In order to process the result of a single symmetry detection evaluation csv file / a directory containing csv files run the following command in a terminal:

`java -jar symmetry_detection_results_processor.jar <PATH_TO_RESULTS>`
        
1. **PATH_TO_RESULTS**: path to a directory containing detection csv results for a given spec
	
Example execution:

`java -jar symmetry_detection_results_processor.jar "symmetry_detection_results/amba.spectra_SEMANTIC_DETECTION_results.csv"`
        
This will print processed results for the symmetry detection data of the AMBA specification
