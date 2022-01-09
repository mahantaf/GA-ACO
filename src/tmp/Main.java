//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package tmp;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options.SatSolver;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.Simplifier;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;
import kodkod.ast.Formula;
import kodkod.ast.Relation;
import kodkod.instance.Bounds;
import kodkod.instance.Tuple;
import kodkod.instance.TupleSet;
import kodkod.util.nodes.AnnotatedNode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static int solvingMode = 1;
    public static Main.AnalysisMode analysisMode = null;
    public static int population = 320;
    public static final int numberOfTopFitness = 1;
    public static long startAlloyToKodKod;
    public static long startSolving;
    public static Bounds orignalBoundsGeneratedByAlloySepc;
    public static ArrayList<Choromosome> originPopulation = new ArrayList();
    public static int numberOfImportantRelation;
    public static int numberOfUnmportantRelation;
    public static Bounds adjustedBounds;
    public static boolean isOriginalPopulation = true;
    public static boolean foundSolution = false;
    public static A4Solution unsolvedSolution = null;
    public static Formula fgoal = null;
    public static HashSet<Integer> fixedRelationIndexSet = null;
    public static HashMap<String, Object[]> principleRelationsAndValues;
    public static HashMap<String, Integer> principleRelationsAndTuplesNumber;
    public static ArrayList<PossibilityPair> possibilityAndRelationList = new ArrayList();
    public static ArrayList<String> principleRelationsList;
    public static int principleTuplesNumber = 0;
    public static HashSet<String> relations = new HashSet();
    public static int iterations = 0;
    public static int numberOfTasks = 15;
    public static ExecutorService executor = Executors.newCachedThreadPool();
    public static int lengthOfString = -1;
    public static CountDownLatch latch = null;
    public static Simplifier simplifier = new Simplifier();
    public static int maxSizeOfSubformula = 0;
    public static int maxVarsOfSubformula = 0;
    public static boolean isStartingGA = false;
    public static HashSet<Integer> searchedChromosomeIndex;
    public static double minFitness = 1000000.0D;
    public static double minFitnessOld = 1000000.0D;
    public static String output;
    public static Main.PrintMode printMode;
    public static AnnotatedNode<Formula> annotated;

    // Added by Mahan
    public static A4Options opt = null;
    public static A4Reporter rep = null;
    public static CompModule world = null;
    public static ThreadPoolExecutor executorPool = null;

    public Main() {}

    public static void addLibraryPath(String pathToAdd) throws Exception {
        Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
        usrPathsField.setAccessible(true);
        String[] paths = (String[])((String[])usrPathsField.get((Object)null));
        String[] newPaths = paths;
        int var4 = paths.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            String path = newPaths[var5];
            if (path.equals(pathToAdd)) {
                return;
            }
        }

        newPaths = (String[])Arrays.copyOf(paths, paths.length + 1);
        newPaths[newPaths.length - 1] = pathToAdd;
        usrPathsField.set((Object)null, newPaths);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Parameter wrong");
        } else {

            // -------------- Configuration Begin --------------

            // TODO: Configuration part can all be done a separate function and return sth like ConfigurationClass

            long configTime = System.currentTimeMillis();

            String libraryPath = args[2];

            try {
                addLibraryPath(libraryPath);
            } catch (Exception var41) {
                var41.printStackTrace();
            }

            long start = System.currentTimeMillis();
            String filename = args[0];
            output = args[1];
            System.out.println("File Name : " + filename);
            if (args.length > 3) {
                String modeString = args[3];
                byte var7 = -1;
                switch(modeString.hashCode()) {
                    case -1588782369:
                        if (modeString.equals("mode_random")) {
                            var7 = 2;
                        }
                        break;
                    case -1524939738:
                        if (modeString.equals("mode_thread")) {
                            var7 = 0;
                        }
                        break;
                    case 1734490543:
                        if (modeString.equals("mode_alloy")) {
                            var7 = 1;
                        }
                }

                switch(var7) {
                    case 0:
                        System.out.println("AnalysisMode: GA");
                        analysisMode = Main.AnalysisMode.GAMultiThreadTestMode;
                        break;
                    case 1:
                        System.out.println("AnalysisMode: Alloy");
                        analysisMode = Main.AnalysisMode.AlloyMode;
                        break;
                    case 2:
                        System.out.println("AnalysisMode: Alloy");
                        analysisMode = Main.AnalysisMode.RandomMode;
                        break;
                    default:
                        System.out.println("AnalysisMode: Alloy");
                        analysisMode = Main.AnalysisMode.AlloyMode;
                }
            }

            Main.SelectionAlgorithm selectionAlgorithm = Main.SelectionAlgorithm.UnbiasedSelection;
            String printmode;
            byte var8;
            if (args.length >= 5) {
                printmode = args[4];
                var8 = -1;
                switch(printmode.hashCode()) {
                    case -2023856447:
                        if (printmode.equals("algorithm_unbiased")) {
                            var8 = 0;
                        }
                        break;
                    case 1885220793:
                        if (printmode.equals("algorithm_tournament")) {
                            var8 = 1;
                        }
                }

                switch(var8) {
                    case 0:
                        System.out.println("Selection Algorithm: Unbiased");
                        selectionAlgorithm = Main.SelectionAlgorithm.UnbiasedSelection;
                        break;
                    case 1:
                        System.out.println("Selection Algorithm: Tournament");
                        selectionAlgorithm = Main.SelectionAlgorithm.TournamentSelection;
                        break;
                    default:
                        System.out.println("Selection Algorithm: Unbiased");
                        selectionAlgorithm = Main.SelectionAlgorithm.UnbiasedSelection;
                }
            }

            if (args.length >= 6) {
                printmode = args[5];
                population = Integer.parseInt(printmode);
                System.out.println("Population:" + population);
            }

            if (args.length >= 7) {
                printmode = args[6];
                GeneticAlgorithm.mutationRate = Double.parseDouble(printmode);
                System.out.println("Mutation Rate:" + GeneticAlgorithm.mutationRate);
            }

            if (args.length >= 8) {
                printmode = args[7];
                GeneticAlgorithm.numberOfTournamentSelect = Integer.parseInt(printmode);
                System.out.println("Tounament size:" + GeneticAlgorithm.numberOfTournamentSelect);
            }

            if (args.length >= 9) {
                printmode = args[8];
                numberOfTasks = Integer.parseInt(printmode);
                System.out.println("Number of Tasks:" + numberOfTasks);
            }

            if (args.length >= 10) {
                printmode = args[9];
                var8 = -1;
                switch(printmode.hashCode()) {
                    case -1796020486:
                        if (printmode.equals("printtime")) {
                            var8 = 2;
                        }
                        break;
                    case 143034310:
                        if (printmode.equals("printdebug")) {
                            var8 = 1;
                        }
                        break;
                    case 146191041:
                        if (printmode.equals("printgraph")) {
                            var8 = 0;
                        }
                }

                switch(var8) {
                    case 0:
                        System.out.println("Print Mode: Graph Data");
                        printMode = Main.PrintMode.GraphDataMode;
                        break;
                    case 1:
                        System.out.println("Print Mode: Debug Info");
                        printMode = Main.PrintMode.DebugMode;
                        break;
                    case 2:
                        System.out.println("Print Mode: Time Only");
                        printMode = Main.PrintMode.OnlyTimeMode;
                        break;
                    default:
                        System.out.println("Print Mode: Debug Info");
                        printMode = Main.PrintMode.DebugMode;
                }
            }

            if (args.length >= 11) {
                printmode = args[10];
                GeneticAlgorithm.unbiasedPickBetterRate = Integer.parseInt(printmode);
                System.out.println("Unbiased Selection Pick Better Chromosome Rate :" + GeneticAlgorithm.unbiasedPickBetterRate);
            }

            // -------------- Configuration End --------------

            // -------------- Initialization Begin --------------

            rep = new A4Reporter() {
                public void warning(ErrorWarning msg) {
                    System.out.print("Relevance Warning:\n" + msg.toString().trim() + "\n\n");
                    System.out.flush();
                }
            };
//            CompModule world = null;

            try {
                world = CompUtil.parseEverything_fromFile(rep, null, filename);
            } catch (Err var40) {
                var40.printStackTrace();
            }

            // TODO: It can be done in configuration part
            if (output != null) {
                FileOutputStream oStream = null;
                String outputFile;
                if (printMode == Main.PrintMode.GraphDataMode) {
                    outputFile = output + ".dat";
                } else {
                    outputFile = output + ".txt";
                }

                oStream = new FileOutputStream(outputFile);
                PrintStream f = new PrintStream(oStream, true);
                System.setOut(f);
            }

            // TODO: Opt setup can be done in a separate function
            opt = new A4Options();
            opt.solver = SatSolver.MiniSatProverJNI;
            opt.symmetry = 20;
            opt.skolemDepth = 2;
            opt.noOverflow = true;

            if (printMode != Main.PrintMode.GraphDataMode) {
                System.out.println("Analyzing Alloy Specification:\n" + filename);
            }

            System.out.println("Configuration Time: " + (System.currentTimeMillis() - configTime) + " ms");
            // -------------- Initialization End --------------

            long initializationTime = System.currentTimeMillis();
            Command cmd = (Command)world.getAllCommands().get(0);
            ConstList<Sig> sigsS = world.getAllReachableSigs();

            // TODO: Each Analysis can be done in a separate function
            if (analysisMode == Main.AnalysisMode.AlloyMode) {
                startAlloyToKodKod = System.currentTimeMillis();

                A4Solution sol = TranslateAlloyToKodkod.execute_command_original(rep, sigsS, cmd, opt);
                if (printMode != Main.PrintMode.GraphDataMode) {
                    System.out.println("Solving time after end of solve: " + (System.currentTimeMillis() - startSolving) + " ms");
                }

                int solNumber = 0;
                for(solNumber = solNumber + 1; sol.satisfiable(); ++solNumber) {
                    if (printMode != Main.PrintMode.GraphDataMode) {
                        System.out.println("--" + sol);
                        System.out.print(".\n");
                        System.out.println("--" + sol.eval);
                    }

                    sol = sol.next();
                }

                if (printMode != Main.PrintMode.GraphDataMode) {
                    PrintStream var10000 = System.out;
                    StringBuilder var10001 = (new StringBuilder()).append("Solutions:");
                    --solNumber;
                    var10000.println(var10001.append(solNumber).toString());
                }

                long cTime1 = System.currentTimeMillis() - start;
                if (printMode != Main.PrintMode.GraphDataMode) {
                    System.out.println("Total Time: " + cTime1 + " ms.");
                }

            } else {
                // TODO: Two following if statements can be reduced to one
                if (analysisMode == Main.AnalysisMode.GAReduceSimplifyMode) {
                    unsolvedSolution = TranslateAlloyToKodkod.execute_command(rep, sigsS, cmd, opt);
                    fgoal = unsolvedSolution.prepareSolve(rep, cmd, new Simplifier());
                }

                if (analysisMode == Main.AnalysisMode.GAMultiThreadTestMode || analysisMode == Main.AnalysisMode.RandomMode) {
                    unsolvedSolution = TranslateAlloyToKodkod.execute_command(rep, sigsS, cmd, opt);
                    fgoal = unsolvedSolution.prepareSolve(rep, cmd, new Simplifier());
                }

                // TODO: Pool initialization can be done in initialization step
                executorPool = new ThreadPoolExecutor(
                        numberOfTasks,
                        100,
                        500L,
                        TimeUnit.SECONDS,
                        new LinkedBlockingQueue()
                );
                long GAPreparationTime = System.currentTimeMillis();
                Boolean hasFinishedPrepare = GeneticAlgorithm.prepareUniverseScope();
                System.out.println("GA preparation time: " + (System.currentTimeMillis() - GAPreparationTime) + " ms");
                isStartingGA = true;
                long firstGenerationTime = System.currentTimeMillis();
                if (analysisMode == Main.AnalysisMode.GAMultiThreadTestMode || analysisMode == Main.AnalysisMode.RandomMode) {
                    try {
                        generateOriginalPopulationOfChromosome_multithread(cmd, rep, sigsS, opt, null, executorPool);
                    } catch (Err | InterruptedException | ClassNotFoundException var37) {
                        var37.printStackTrace();
                    }
                }
                System.out.println("First generation time: " + (System.currentTimeMillis() - firstGenerationTime) + " ms");


                int possibility = 0;
                for (String key : principleRelationsAndTuplesNumber.keySet()) {
                    possibility += principleRelationsAndTuplesNumber.get(key);
                    PossibilityPair pair = new PossibilityPair(possibility, key);
                    possibilityAndRelationList.add(pair);
                }

                if (analysisMode != Main.AnalysisMode.RandomMode) {
                    isOriginalPopulation = false;
                }

                int lastUpdated = iterations;
                long startGA;
                if (analysisMode == Main.AnalysisMode.RandomMode) {
                    while(!foundSolution && iterations < 500) {
                        try {
                            generateOriginalPopulationOfChromosome_multithread(cmd, rep, sigsS, opt, null, executorPool);
                        } catch (Err | CloneNotSupportedException | InterruptedException | ClassNotFoundException var33) {
                            var33.printStackTrace();
                        }

                        for (Choromosome choromosome : originPopulation)
                            System.out.println("Fitness :" + choromosome.fitness);
                    }

                    if (foundSolution) {
                        System.out.println("solved");
                    } else {
                        System.out.println("Exceed " + iterations + " iterations");
                    }
                } else {
                    int numOfZeroFitnessChange = 0;
                    long GATime = System.currentTimeMillis();
                    System.out.println("Initialization Time: " + (GATime - initializationTime) + " ms");
                    while(!foundSolution) {
                        startGA = System.currentTimeMillis();
                        ArrayList<Choromosome> populationWithoutSorting = new ArrayList(originPopulation);

                        ArrayList<Choromosome> topFitnessPopulation = new ArrayList();
                        if (printMode == Main.PrintMode.DebugMode) {
                            System.out.println("size ++ " + originPopulation.size());
                        }

                        quickSortPopulation(originPopulation);

                        if (numOfZeroFitnessChange >= 3) {
                            System.out.println("GA is not optimal anymore");
                            System.out.println("Switching to ACO...");
                            break;
                        }

                        double currentFitness = originPopulation.get(0).fitness;

                        if (printMode == Main.PrintMode.DebugMode) {
                            System.out.println(" ------------- ------------- ------------- Iteration " + iterations);
                        }
                        // TODO: Move this part into a separate function (it's just printing)
//                        for(int i = 0; i < originPopulation.size(); ++i) {
//                            Choromosome tmp = (Choromosome)originPopulation.get(i);
//                            if (printMode == Main.PrintMode.DebugMode) {
//                                System.out.println("Iter" + iterations + ":Index" + i + "\n" +
//                                        "fitness-new:" + tmp.fitness + "\nnumber of failed subformulas: " + tmp.failedConstraintsNumber + "\nnumber of failed null relations " + tmp.failedRelationNumber + "\n bounds ");
//                                tmp.printBounds();
//                            }
//
//                            if (i < 5) {
//                            }
//
//                            if (printMode == Main.PrintMode.GraphDataMode) {
//                                if (i == 0) {
//                                    System.out.print(tmp.fitness + ",");
//                                }
//
//                                if (i == population - 1) {
//                                    System.out.println(tmp.fitness);
//                                }
//                            }
//
//                            if (printMode == Main.PrintMode.DebugMode) {
//                                System.out.println(" \n ");
//                            }
//                        }

                        ArrayList<Choromosome> selectedPopulation;
                        if (selectionAlgorithm != Main.SelectionAlgorithm.UnbiasedSelection) {
                            if (selectionAlgorithm != Main.SelectionAlgorithm.TournamentSelection) {
                                selectedPopulation = null;
                                return;
                            }
                            selectedPopulation = GeneticAlgorithm.tournamentSelectPopulation(originPopulation);
                        } else {
                            for(int i = 0; i < 1; ++i) {
                                Choromosome tmp = originPopulation.get(i);
                                populationWithoutSorting.remove(tmp);
                                topFitnessPopulation.add(tmp);
                            }

                            selectedPopulation = GeneticAlgorithm.unbiasedTournamentSelectPopulation(populationWithoutSorting, topFitnessPopulation);
                        }

                        if (printMode == Main.PrintMode.DebugMode) {
                            System.out.println(" ------------- ------------- ------------- ");
                        }

                        long startCrossover = System.currentTimeMillis();
                        ArrayList<Choromosome> crossoverGeneratedPopulation = GeneticAlgorithm.crossover_possibility(selectedPopulation);
                        ArrayList<Choromosome> mutationGeneratedPopulation = GeneticAlgorithm.mutation_tupleLevel(crossoverGeneratedPopulation);
                        long endGA = System.currentTimeMillis() - startGA;
                        if (printMode != Main.PrintMode.GraphDataMode) {
                            System.out.println("End GA Time: " + endGA + "ms");
                        }

                        if (analysisMode == Main.AnalysisMode.GAMultiThreadTestMode) {
                            try {
                                generateOriginalPopulationOfChromosome_multithread(cmd, rep, sigsS, opt, mutationGeneratedPopulation, executorPool);
                            } catch (Err | CloneNotSupportedException | InterruptedException | ClassNotFoundException var29) {
                                var29.printStackTrace();
                            }
                        }

                        double newFitness = getFittestChromosomeFitness();

                        System.out.println("------------ Fitness Data ------------");
                        System.out.println("Current Fitness: " + currentFitness);
                        System.out.println("New Fitness: " + newFitness);
                        System.out.println("Change: " + (currentFitness - newFitness));
                        System.out.println("------------------------");

                        if (currentFitness - newFitness <= 0.05)
                            numOfZeroFitnessChange++;
                        else
                            numOfZeroFitnessChange = 0;

                        if (foundSolution && printMode == Main.PrintMode.DebugMode) {
                            System.out.println("solved");
                        }
                    }
                    System.out.println("GA Time: " + (System.currentTimeMillis() - GATime) + " ms");
                }
                // Added by Mahan
                if (!foundSolution) {
                    long startAntColony = System.currentTimeMillis();
                    AntColonyAlgorithm antColonyAlgorithm = createAntColonyInstanceByChromosomes(unsolvedSolution, originPopulation);

                    for (Choromosome choromosome : originPopulation)
                        antColonyAlgorithm.updatePheromonesByGASolution(choromosome.chromosomeBounds, choromosome.fitness);

//                    System.out.println("Setting best chromosome");
                    antColonyAlgorithm.setBestGASolution(originPopulation.get(0));
                    System.out.println("Starting Optimization");
                    Ant bestAnt = antColonyAlgorithm.startOptimization();
//                    System.out.println("End of Optimization");
                    System.out.println("ACO Time: " + (System.currentTimeMillis() - startAntColony) + " ms");

//                    System.out.println("=================================");
//                    System.out.println("Best Ant:");
//                    System.out.println(bestAnt);
                }


                startGA = System.currentTimeMillis() - start;
                if (printMode != Main.PrintMode.GraphDataMode) {
                    System.out.println("Total Time: " + startGA + " ms.");
                }

                executorPool.shutdown();

                try {
                    executorPool.awaitTermination(9223372036854775807L, TimeUnit.SECONDS);
                } catch (InterruptedException ignored) {}

            }
        }
    }

    public static void quickSortPopulation(ArrayList<Choromosome> population) {
        recursiveQuickSort(population, 0, population.size() - 1);
    }

    private static void recursiveQuickSort(ArrayList<Choromosome> population, int low, int high) {
        if (low < high) {
            int randomMiddle = partition(population, low, high);
            recursiveQuickSort(population, low, randomMiddle - 1);
            recursiveQuickSort(population, randomMiddle + 1, high);
        }
    }

    private static int partition(ArrayList<Choromosome> population, int low, int high) {
        if (printMode == Main.PrintMode.DebugMode) {
//            System.out.println(low + " low|high " + high);
        }

        double pivot = ((Choromosome)population.get(low)).fitness;
        Choromosome piv = (Choromosome)population.get(low);

        while(low < high) {
            while(low < high && ((Choromosome)population.get(high)).fitness >= pivot) {
                --high;
            }

            population.set(low, population.get(high));

            while(low < high && ((Choromosome)population.get(low)).fitness <= pivot) {
                ++low;
            }

            population.set(high, population.get(low));
        }

        population.set(low, piv);
        return low;
    }

    private static void generateOriginalPopulationOfChromosome_multithread(Command cmd, A4Reporter rep, ConstList<Sig> sigsS, A4Options opt, ArrayList<Choromosome> generatedPopulationByGA, ThreadPoolExecutor executorPool) throws Err, CloneNotSupportedException, IOException, InterruptedException, ClassNotFoundException {
        long start = System.currentTimeMillis();
        new ArrayList();
        originPopulation = new ArrayList();

        for(int j = 0; j < population; ++j)
            originPopulation.add(null);

        latch = new CountDownLatch(population);

        for(int iter = 0; iter < population; ++iter) {
            long startIter = System.currentTimeMillis();
            Choromosome generatedChromo;
            if (isOriginalPopulation) {
                generatedChromo = new Choromosome(iter);
            } else {
                generatedChromo = generatedPopulationByGA.get(iter);
            }

            Formula f1 = Formula.and(unsolvedSolution.formulas);
            A4Solution sol1 = new A4Solution(unsolvedSolution, iter);
            Runnable solutionThread = new GetSolutionThread(sol1, cmd, rep, f1, generatedChromo, iter);
            executorPool.execute(solutionThread);
        }

        try {
            latch.await();
        } catch (InterruptedException ignored) {}

        long cTime1 = System.currentTimeMillis() - start;
//        long time1 = System.currentTimeMillis();
        if (printMode != Main.PrintMode.GraphDataMode) {
            System.out.println(" --- Full Iteration Time: " + cTime1 + " ms.  -- " + originPopulation.size());
        }

//        long cTime2 = System.currentTimeMillis() - time1;
//        if (printMode == Main.PrintMode.DebugMode) {
//            System.out.println(" --- Address Chromosomes Data Time: " + cTime2 + " ms.  -- " + originPopulation.size());
//        }

        ++iterations;
    }

    static {
        printMode = Main.PrintMode.DebugMode;
    }


    // Enums:
    public static enum SelectionAlgorithm {
        TournamentSelection("TournamentSelection"),
        UnbiasedSelection("UnbiasedSelection");

        public final String name;

        private SelectionAlgorithm(String label) {
            this.name = label;
        }
    }

    public static enum PrintMode {
        OnlyTimeMode,
        DebugMode,
        GraphDataMode;

        private PrintMode() {
        }
    }

    public static enum AnalysisMode {
        RandomMode,
        AlloyMode,
        GAOriginalMode,
        GAReduceSimplifyMode,
        GAMultiThreadTestMode;

        private AnalysisMode() {
        }
    }

    /*
     * Ant Colony Optimization Methods
     * Added by Mahan Tafreshipour (Nov. 2021)
     */

    public static double getFittestChromosomeFitness() {
        double fitness = 10000000;
        for (Choromosome c : originPopulation) {
            if (c.fitness < fitness)
                fitness = c.fitness;
        }
        return fitness;
    }

    public static void evaluateSingleChromosome(
            Command cmd,
            A4Reporter rep, ConstList<Sig> sigsS,
            A4Options opt,
            Choromosome choromosome,
            ThreadPoolExecutor executorPool
    ) throws Err, CloneNotSupportedException, IOException, InterruptedException, ClassNotFoundException {
        originPopulation = new ArrayList<>();
        for(int j = 0; j < population; ++j)
            originPopulation.add(null);

        latch = new CountDownLatch(1);

        Formula f1 = Formula.and(unsolvedSolution.formulas);
        A4Solution sol1 = new A4Solution(unsolvedSolution, 0);
        Runnable solutionThread = new GetSolutionThread(sol1, cmd, rep, f1, choromosome, 0);
        executorPool.execute(solutionThread);

        try {
            latch.await();
        } catch (InterruptedException ignored) {}
    }

    public static boolean isUnimportant(Relation relation) {
        return  relation.toString().equals("String")   ||
                relation.toString().equals("seq/Int")  ||
                relation.toString().equals("Int/next");
    }

    public static Choromosome translateAntToChromosome(Ant ant, ArrayList<Relation> unimportantRelations, int index) {
        ArrayList<AntNode> antNodes = ant.getNodes();
        Choromosome generatedChromosome = new Choromosome(index);

        Map<Relation, TupleSet> lowerBounds = new LinkedHashMap<>();
        Map<Relation, TupleSet> upperBounds = new LinkedHashMap<>();
        Bounds chromosomeBounds = new Bounds(unsolvedSolution.bounds.universe());

        for (Relation relation : unimportantRelations) {
            lowerBounds.put(relation, unsolvedSolution.bounds.lowers.get(relation));
            upperBounds.put(relation, unsolvedSolution.bounds.uppers.get(relation));
        }

        int tupleSize = 0;
        for (AntNode antNode : antNodes) {
            Relation relation = antNode.relation;
            TupleSet tuples = antNode.tuples;

            lowerBounds.put(relation, tuples);
            upperBounds.put(relation, tuples);

            tupleSize += tuples.size();
        }

        chromosomeBounds.uppers = upperBounds;
        chromosomeBounds.lowers = lowerBounds;

        generatedChromosome.chromosomeBounds = chromosomeBounds;
        generatedChromosome.tupleSize = tupleSize;

        return generatedChromosome;
    }

    private static void evaluatePartialAntsMultiThread(
            Command cmd,
            A4Reporter rep,
            ConstList<Sig> sigsS,
            A4Options opt,
            ArrayList<Choromosome> generatedPopulationByGA,
            ThreadPoolExecutor executorPool
    ) throws Err, CloneNotSupportedException, IOException, InterruptedException, ClassNotFoundException {

        originPopulation = new ArrayList<>();

        for(int j = 0; j < generatedPopulationByGA.size(); ++j)
            originPopulation.add(null);

        latch = new CountDownLatch(generatedPopulationByGA.size());

        for(int iter = 0; iter < generatedPopulationByGA.size(); ++iter) {
            Choromosome generatedChromo = generatedPopulationByGA.get(iter);
            Formula f1 = Formula.and(unsolvedSolution.formulas);
            A4Solution sol1 = new A4Solution(unsolvedSolution, iter);
            Runnable solutionThread = new GetSolutionThread(sol1, cmd, rep, f1, generatedChromo, iter);
            executorPool.execute(solutionThread);
        }

        try {
            latch.await();
        } catch (InterruptedException ignored) {}

        ++iterations;
    }

    public static void evaluateAntPartialSolution(Ant ant, ArrayList<Relation> unimportantRelations) {
        int index = 0;
        Choromosome choromosome = translateAntToChromosome(ant, unimportantRelations, index);

        Command cmd = world.getAllCommands().get(0);
        ConstList<Sig> sigsS = world.getAllReachableSigs();

        try {
            evaluateSingleChromosome(cmd, rep, sigsS, opt, choromosome, executorPool);
        } catch (Err | CloneNotSupportedException | InterruptedException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        Choromosome evaluatedChromosome = originPopulation.get(index);
        ant.fitness = evaluatedChromosome.fitness;
        ant.failedConstraintsNumber = evaluatedChromosome.failedConstraintsNumber;
        ant.totalConstraintsNumber = evaluatedChromosome.totalConstraintsNumber;
        ant.failedRelationNumber = evaluatedChromosome.failedRelationNumber;
    }

    public static void evaluateAntsPartialSolutionsMultiThread(AntColonyAlgorithm antColonyAlgorithmInstance, ArrayList<Ant> ants) {
        ArrayList<Choromosome> antChromosomePopulation = new ArrayList<>();
        int index = 0;
        for (Ant ant : ants) {
            antChromosomePopulation.add(translateAntToChromosome(ant, antColonyAlgorithmInstance.unimportantRelations, index));
            ++index;
        }

        Command cmd = world.getAllCommands().get(0);
        ConstList<Sig> sigsS = world.getAllReachableSigs();

        try {
            evaluatePartialAntsMultiThread(cmd, rep, sigsS, opt, antChromosomePopulation, executorPool);
        } catch (Err | CloneNotSupportedException | InterruptedException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        index = 0;
        for (Ant ant: ants) {
            Choromosome evaluatedChromosome = originPopulation.get(index);

            ant.fitness = evaluatedChromosome.fitness;
            ant.failedConstraintsNumber = evaluatedChromosome.failedConstraintsNumber;
            ant.totalConstraintsNumber = evaluatedChromosome.totalConstraintsNumber;
            ant.failedRelationNumber = evaluatedChromosome.failedRelationNumber;
            ++index;
        }
    }

    public static void evaluateAntsSolutions(AntColonyAlgorithm antColonyAlgorithmInstance, ArrayList<Ant> ants) {
        ArrayList<Choromosome> antChromosomePopulation = new ArrayList<>();

        int index = 0;
        for (Ant ant : ants) {
            antChromosomePopulation.add(translateAntToChromosome(ant, antColonyAlgorithmInstance.unimportantRelations, index));
            ++index;
        }

        Command cmd = world.getAllCommands().get(0);
        ConstList<Sig> sigsS = world.getAllReachableSigs();

        Random random = new Random(System.currentTimeMillis());
        int mutationProbability = random.nextInt(100);

        if (mutationProbability <= 10) {
            System.out.println("Adding Random Mutation");
            antChromosomePopulation = GeneticAlgorithm.mutation_tupleLevel(antChromosomePopulation);
        }

        try {
            generateOriginalPopulationOfChromosome_multithread(cmd, rep, sigsS, opt, antChromosomePopulation, executorPool);
        } catch (Err | CloneNotSupportedException | InterruptedException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        index = 0;
        for (Ant ant: ants) {

            Choromosome evaluatedChromosome = originPopulation.get(index);

            ant.fitness = evaluatedChromosome.fitness;
            ant.failedConstraintsNumber = evaluatedChromosome.failedConstraintsNumber;
            ant.totalConstraintsNumber = evaluatedChromosome.totalConstraintsNumber;
            ant.failedRelationNumber = evaluatedChromosome.failedRelationNumber;

            antColonyAlgorithmInstance.updateNodesByChromosome(evaluatedChromosome.chromosomeBounds);
            ++index;
        }
    }

    public static boolean contain(ArrayList<TupleSet> tupleSets, TupleSet tupleSet) {
        for (TupleSet ts : tupleSets) {
            if (ts.equals(tupleSet))
                return true;
        }
        return false;
    }

    public static AntColonyAlgorithm createAntColonyInstanceByChromosomes(A4Solution notSolvedSolution, ArrayList<Choromosome> choromosomes) {
        HashMap<Relation, ArrayList<TupleSet>> relationTuples = new HashMap<Relation, ArrayList<TupleSet>>();
        ArrayList<Relation> unimportantRelations = new ArrayList<>();

        for (Relation relation : notSolvedSolution.bounds.relations()) {
            if (isUnimportant(relation)) {
                unimportantRelations.add(relation);
            } else {
                ArrayList<TupleSet> tupleSets = new ArrayList<>();
                for (Choromosome choromosome : choromosomes) {
                    TupleSet chromosomeTuples = choromosome.chromosomeBounds.uppers.get(relation);
                    if (!contain(tupleSets, chromosomeTuples))
                        tupleSets.add(chromosomeTuples);
                }
                relationTuples.put(relation, tupleSets);
            }
        }
        AntColonyAlgorithm antColonyAlgorithm = new AntColonyAlgorithm();
        antColonyAlgorithm.setUnimportantRelations(unimportantRelations);
        antColonyAlgorithm.setNotSolvedSolution(notSolvedSolution);
        antColonyAlgorithm.initializeNodes(relationTuples);

        return antColonyAlgorithm;
    }


    /**
     *
     * Not Used Methods:
     */

    private static void swapChromosome(ArrayList<Choromosome> population, int i, int j) {
        Choromosome tmp = (Choromosome)originPopulation.get(j);
        originPopulation.set(j, originPopulation.get(i));
        originPopulation.set(i, tmp);
    }

    private static void generateOriginalPopulationOfChromosome(Command cmd, A4Reporter rep, ConstList<Sig> sigsS, A4Options opt, ArrayList<Choromosome> generatedPopulationByGA) throws Err {
        long start = System.currentTimeMillis();
        originPopulation = new ArrayList();

        for(int iter = 0; iter < population; ++iter) {
            A4Solution sol = null;
            int solNumber = 0;
            if (!isOriginalPopulation) {
                adjustedBounds = ((Choromosome)generatedPopulationByGA.get(iter)).chromosomeBounds;
            }

            solNumber = solNumber + 1;
            startAlloyToKodKod = System.currentTimeMillis();

            try {
                switch(analysisMode) {
                    case AlloyMode:
                    default:
                        break;
                    case GAOriginalMode:
                        sol = TranslateAlloyToKodkod.execute_command_original(rep, sigsS, cmd, opt);
                        break;
                    case GAReduceSimplifyMode:
                        System.out.println("Switch Simplify mode");
                }

                if (printMode != Main.PrintMode.GraphDataMode) {
                    System.out.println(" === " + sol);
                }

                while(sol.satisfiable()) {
                    if (printMode != Main.PrintMode.GraphDataMode) {
                        System.out.println("--" + sol);
                        System.out.print(".\n");
                    }

                    sol = sol.next();
                    ++solNumber;
                }
            } catch (Err var14) {
                var14.printStackTrace();
            }

            while(originPopulation.size() < population) {
                int var10 = population - originPopulation.size();
            }

            if (printMode == Main.PrintMode.DebugMode) {
                PrintStream var10000 = System.out;
                StringBuilder var10001 = (new StringBuilder()).append("Solutions:");
                --solNumber;
                var10000.println(var10001.append(solNumber).append("Orginal Population :").append(originPopulation.size()).toString());
            }

            if (solNumber != 0) {
                if (printMode != Main.PrintMode.GraphDataMode) {
                    System.out.println("--Solved");
                }

                foundSolution = true;
                break;
            }

            long startGATime = System.currentTimeMillis();
            long gaTime = System.currentTimeMillis() - startGATime;
            if (printMode != Main.PrintMode.GraphDataMode) {
                System.out.println("GA Time: " + gaTime + " ms.");
            }

            if (printMode != Main.PrintMode.GraphDataMode) {
                System.out.println("Generate One Chromosome " + (System.currentTimeMillis() - startAlloyToKodKod) + "ms.");
            }
        }

        long cTime1 = System.currentTimeMillis() - start;
        if (printMode != Main.PrintMode.GraphDataMode) {
            System.out.println("Full Iteration Time: " + cTime1 + " ms.");
        }

    }

    public static void updatePheromonesByChromosome(AntColonyAlgorithm antColonyAlgorithm, Choromosome choromosome) {
        antColonyAlgorithm.updatePheromonesByGASolution(choromosome.chromosomeBounds, choromosome.fitness);
    }

    public static AntColonyAlgorithm createAntColonyInstance(A4Solution notSolvedSolution) {

        HashMap<Relation, ArrayList<TupleSet>> relationTuples = new HashMap<Relation, ArrayList<TupleSet>>();
        ArrayList<Relation> unimportantRelations = new ArrayList<>();

        for (Relation r : notSolvedSolution.bounds.relations()) {
            ArrayList<TupleSet> tupleSets = new ArrayList<>();
            if (isUnimportant(r)) {
                unimportantRelations.add(r);
            } else if (notSolvedSolution.bounds.lowers.get(r) == notSolvedSolution.bounds.uppers.get(r)) {
                TupleSet tuples = notSolvedSolution.bounds.lowers.get(r);
                tupleSets.add(tuples);
                relationTuples.put(r, tupleSets);
            } else {
                TupleSet upperTuples = notSolvedSolution.bounds.uppers.get(r);
                TupleSet lowerTuples = notSolvedSolution.bounds.lowers.get(r);
                createAllSubsetOfTupleSet(upperTuples, lowerTuples, tupleSets);
                relationTuples.put(r, tupleSets);
            }
        }
        AntColonyAlgorithm antColonyAlgorithm = new AntColonyAlgorithm();
        antColonyAlgorithm.setUnimportantRelations(unimportantRelations);
        antColonyAlgorithm.initializeNodes(relationTuples);

        return antColonyAlgorithm;
    }

    public static boolean containsAll(TupleSet a, TupleSet b) {
        boolean contains = true;
        for (Tuple tuple : b) {
            if (!a.contains(tuple)) {
                contains = false;
                break;
            }
        }
        return contains;
    }

    public static void createAllSubsetOfTupleSet(TupleSet upperTuples, TupleSet lowerTuples, ArrayList<TupleSet> tupleSets) {
        for (int i = 0; i < (1 << upperTuples.size()); i++) {
            TupleSet tupleSet = new TupleSet(upperTuples.universe(), upperTuples.arity());
            int j = 0;
            for (Tuple tuple : upperTuples) {
                if ((i & (1 << j)) > 0) {
                    tupleSet.add(tuple);
                }
                j++;
            }
            if (containsAll(tupleSet, lowerTuples))
                tupleSets.add(tupleSet);
        }
    }
}
