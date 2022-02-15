//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package tmp;

import kodkod.ast.Relation;
import kodkod.instance.Bounds;
import kodkod.instance.Tuple;
import kodkod.instance.TupleFactory.IntTuple;
import kodkod.instance.TupleSet;
import tmp.Main.PrintMode;

import java.util.*;

public class GeneticAlgorithm {
    public static int numberOfTournamentSelect = 3;
    public static double mutationRate = 0.08D;
    public static int deleteRelationMutationRate = 10;
    public static int changeRelationMutationRate = 30;
    public static int addRelationMutationRate = 30;
    public static int oneTupleMutationRate;
    public static int unbiasedPickBetterRate;
    public static HashMap<String, Integer> atomsNameAndScope;

    public GeneticAlgorithm() {
    }

    public static ArrayList<Choromosome> rankingSelectPopulation(ArrayList<Choromosome> population) {
        ArrayList<Choromosome> selectedPopulation = new ArrayList(population.subList(0, population.size() / 2));
        return selectedPopulation;
    }

    public static boolean prepareUniverseScope() {
        boolean prepareSuccess = true;
        String nowIteratedAtomName = "";
        int atomIndex = 0;

        for(int i = 0; i < Main.unsolvedSolution.bounds.universe().size(); ++i) {
            String atomName = Main.unsolvedSolution.bounds.universe().atom(i).toString();
            String[] atomNameComponents = atomName.split("\\$");
            String key = atomNameComponents[0];
            if (key.equals(nowIteratedAtomName)) {
                String snd = atomNameComponents[1];
                atomIndex = Integer.parseInt(snd);
            } else {
                if (!nowIteratedAtomName.equals("")) {
                    atomsNameAndScope.put(nowIteratedAtomName, atomIndex);
                }

                nowIteratedAtomName = key;
                atomIndex = 0;
            }
        }

        atomsNameAndScope.put(nowIteratedAtomName, atomIndex);
        return prepareSuccess;
    }

    public static ArrayList<Choromosome> tournamentSelectPopulation(ArrayList<Choromosome> population) {
        ArrayList<Choromosome> selectedPopulation = new ArrayList();
        selectedPopulation.add(population.get(0));
        selectedPopulation.add(population.get(1));
        selectedPopulation.add(population.get(2));
        selectedPopulation.add(population.get(3));
        int selectedNumber = 0;

        HashSet tournamentSet;
        for(Random rand = new Random(System.currentTimeMillis()); selectedNumber < Main.population / 2 - 4; tournamentSet = null) {
            tournamentSet = new HashSet();
            double min = 0.0D;
            int minIndex = 0;

            for(int k = 0; k < numberOfTournamentSelect; ++k) {
                int selectedChromosomeIndex = rand.nextInt(Main.population);
                tournamentSet.add(selectedChromosomeIndex);
                if (min == 0.0D || min > ((Choromosome)population.get(selectedChromosomeIndex)).fitness) {
                    min = ((Choromosome)population.get(selectedChromosomeIndex)).fitness;
                    minIndex = selectedChromosomeIndex;
                }
            }

            Choromosome selectedChromo = (Choromosome)population.get(minIndex);
            selectedPopulation.add(selectedChromo);
            ++selectedNumber;
        }

        return selectedPopulation;
    }

    public static ArrayList<Choromosome> unbiasedTournamentSelectPopulation(ArrayList<Choromosome> randomSortedPopulation, ArrayList<Choromosome> topFitnessPopulation) {
        ArrayList<Choromosome> selectedPopulation = new ArrayList(topFitnessPopulation);
        ArrayList<Choromosome> secRandomSortedPopulation = new ArrayList(randomSortedPopulation);
        Random rnd = new Random(System.currentTimeMillis());
        Collections.shuffle(randomSortedPopulation, rnd);

        for(int i = 0; i < Main.population / 2 - 1; ++i) {
            int selectionPos = 1 + rnd.nextInt(100);
            boolean isPickBetterOne = selectionPos < unbiasedPickBetterRate;
            if (((Choromosome)secRandomSortedPopulation.get(i)).fitness < ((Choromosome)randomSortedPopulation.get(i)).fitness) {
                if (isPickBetterOne) {
                    selectedPopulation.add(secRandomSortedPopulation.get(i));
                } else {
                    selectedPopulation.add(randomSortedPopulation.get(i));
                }
            } else if (isPickBetterOne) {
                selectedPopulation.add(randomSortedPopulation.get(i));
            } else {
                selectedPopulation.add(secRandomSortedPopulation.get(i));
            }
        }

        return selectedPopulation;
    }

    public static ArrayList<Choromosome> crossover_possibility(ArrayList<Choromosome> selectedPopulation) {
        ArrayList<Choromosome> crossoveredPopulation = new ArrayList(selectedPopulation);
        Random randomGenerator = new Random(System.currentTimeMillis());
//        int crossoverProbability = true;
//        int CrossoverProbability = true;
        int i = 0;

        while(i + 2 < selectedPopulation.size()) {
            Choromosome originFirst = (Choromosome)selectedPopulation.get(i);
            Choromosome originSecond = (Choromosome)selectedPopulation.get(i + 2);
            ArrayList<Choromosome> pairParents = new ArrayList();
            pairParents.add(originFirst);
            pairParents.add(originSecond);
            ArrayList<Choromosome> pairOffsprings = null;
            pairOffsprings = crossover_pair_dummy(pairParents, randomGenerator);
            crossoveredPopulation.addAll(pairOffsprings);
            if (i % 4 == 0) {
                ++i;
            } else {
                i += 3;
            }
        }

        return crossoveredPopulation;
    }

    public static ArrayList<Choromosome> crossover_everypoint_relationLevel(ArrayList<Choromosome> pairParents) {
        ArrayList<Choromosome> pairOffspring = new ArrayList();
        Choromosome generatedFirstChromo = new Choromosome();
        Choromosome generatedSecondChromo = new Choromosome();
        Choromosome originFirst = (Choromosome)pairParents.get(0);
        Choromosome originSecond = (Choromosome)pairParents.get(1);
        Bounds chromosomeBoundsFirst = new Bounds(originFirst.chromosomeBounds.universe());
        Bounds chromosomeBoundsSec = new Bounds(originSecond.chromosomeBounds.universe());
        Map<Relation, TupleSet> firstLowerBounds = new LinkedHashMap();
        Map<Relation, TupleSet> firstUpperBounds = new LinkedHashMap();
        Map<Relation, TupleSet> secLowerBounds = new LinkedHashMap();
        Map<Relation, TupleSet> secUpperBounds = new LinkedHashMap();
        int crossedTimes = 0;
        Iterator var13 = originFirst.chromosomeBounds.relations().iterator();

        while(var13.hasNext()) {
            Relation r = (Relation)var13.next();
            String relationString = r.toString();
            String actualRelation;
            if (relationString.contains(" ")) {
                actualRelation = relationString.split(" ")[0] + "." + relationString.split(" ")[1];
            } else {
                actualRelation = relationString;
            }

            if (!Main.principleRelationsAndTuplesNumber.containsKey(actualRelation)) {
                firstLowerBounds.put(r, originFirst.chromosomeBounds.lowers.get(r));
                firstUpperBounds.put(r, originFirst.chromosomeBounds.uppers.get(r));
                secLowerBounds.put(r, originSecond.chromosomeBounds.lowers.get(r));
                secUpperBounds.put(r, originSecond.chromosomeBounds.uppers.get(r));
            } else {
                if (crossedTimes % 2 == 0) {
                    firstLowerBounds.put(r, originSecond.chromosomeBounds.lowers.get(r));
                    firstUpperBounds.put(r, originSecond.chromosomeBounds.uppers.get(r));
                    secLowerBounds.put(r, originFirst.chromosomeBounds.lowers.get(r));
                    secUpperBounds.put(r, originFirst.chromosomeBounds.uppers.get(r));
                } else {
                    firstLowerBounds.put(r, originFirst.chromosomeBounds.lowers.get(r));
                    firstUpperBounds.put(r, originFirst.chromosomeBounds.uppers.get(r));
                    secLowerBounds.put(r, originSecond.chromosomeBounds.lowers.get(r));
                    secUpperBounds.put(r, originSecond.chromosomeBounds.uppers.get(r));
                }

                ++crossedTimes;
            }
        }

        chromosomeBoundsFirst.lowers = firstLowerBounds;
        chromosomeBoundsFirst.uppers = firstUpperBounds;
        chromosomeBoundsSec.lowers = secLowerBounds;
        chromosomeBoundsSec.uppers = secUpperBounds;
        generatedFirstChromo.chromosomeBounds = chromosomeBoundsFirst;
        generatedSecondChromo.chromosomeBounds = chromosomeBoundsSec;
        pairOffspring.add(generatedFirstChromo);
        pairOffspring.add(generatedSecondChromo);
        return pairOffspring;
    }

    public static ArrayList<Choromosome> crossover_pair_dummy(ArrayList<Choromosome> pairParents, Random randomGenerator) {
        new ArrayList();
        int prob = randomGenerator.nextInt(100);
        if (prob < 50) {
            return crossover_tupleLevel_twopoints(pairParents, randomGenerator);
        } else {
            return prob < 0 ? crossover_everypoint_relationLevel(pairParents) : pairParents;
        }
    }

    public static ArrayList<Choromosome> crossover_pair_relationLevel(ArrayList<Choromosome> pairParents, Random randomGenerator) {
        ArrayList<Choromosome> pairOffspring = new ArrayList();
        Choromosome generatedFirstChromo = new Choromosome();
        Choromosome generatedSecondChromo = new Choromosome();
        Choromosome originFirst = (Choromosome)pairParents.get(0);
        Choromosome originSecond = (Choromosome)pairParents.get(1);
        Bounds chromosomeBoundsFirst = new Bounds(originFirst.chromosomeBounds.universe());
        Bounds chromosomeBoundsSec = new Bounds(originSecond.chromosomeBounds.universe());
        Map<Relation, TupleSet> firstLowerBounds = new LinkedHashMap();
        Map<Relation, TupleSet> firstUpperBounds = new LinkedHashMap();
        Map<Relation, TupleSet> secLowerBounds = new LinkedHashMap();
        Map<Relation, TupleSet> secUpperBounds = new LinkedHashMap();
        int j = 0;
        int randomPoint1 = Main.numberOfImportantRelation / 4 + randomGenerator.nextInt(Main.numberOfImportantRelation * 3 / 4 - Main.numberOfImportantRelation / 4 + 1) + 1;

        int randomPoint2;
        for(randomPoint2 = Main.numberOfImportantRelation / 4 + randomGenerator.nextInt(Main.numberOfImportantRelation * 3 / 4 - Main.numberOfImportantRelation / 4 + 1) + 1; randomPoint2 == randomPoint1; randomPoint2 = Main.numberOfImportantRelation / 4 + randomGenerator.nextInt(Main.numberOfImportantRelation * 3 / 4 - Main.numberOfImportantRelation / 4 + 1) + 1) {
        }

        int smallerPoint;
        int largerPoint;
        if (randomPoint1 < randomPoint2) {
            smallerPoint = randomPoint1;
            largerPoint = randomPoint2;
        } else {
            smallerPoint = randomPoint2;
            largerPoint = randomPoint1;
        }

        int crossPoint1 = Main.numberOfUnmportantRelation - 1 + smallerPoint;
        int crossPoint2 = Main.numberOfUnmportantRelation - 1 + largerPoint;

        for(Iterator var20 = originFirst.chromosomeBounds.relations().iterator(); var20.hasNext(); ++j) {
            Relation r = (Relation)var20.next();
            String relationString = r.toString();
            String actualRelation;
            if (relationString.contains(" ")) {
                actualRelation = relationString.split(" ")[0] + "." + relationString.split(" ")[1];
            } else {
                actualRelation = relationString;
            }

            if (!Main.principleRelationsAndTuplesNumber.containsKey(actualRelation)) {
                firstLowerBounds.put(r, originFirst.chromosomeBounds.lowers.get(r));
                firstUpperBounds.put(r, originFirst.chromosomeBounds.uppers.get(r));
                secLowerBounds.put(r, originSecond.chromosomeBounds.lowers.get(r));
                secUpperBounds.put(r, originSecond.chromosomeBounds.uppers.get(r));
            } else if (j >= crossPoint1 && j <= crossPoint2) {
                firstLowerBounds.put(r, originSecond.chromosomeBounds.lowers.get(r));
                firstUpperBounds.put(r, originSecond.chromosomeBounds.uppers.get(r));
                secLowerBounds.put(r, originFirst.chromosomeBounds.lowers.get(r));
                secUpperBounds.put(r, originFirst.chromosomeBounds.uppers.get(r));
            } else {
                firstLowerBounds.put(r, originFirst.chromosomeBounds.lowers.get(r));
                firstUpperBounds.put(r, originFirst.chromosomeBounds.uppers.get(r));
                secLowerBounds.put(r, originSecond.chromosomeBounds.lowers.get(r));
                secUpperBounds.put(r, originSecond.chromosomeBounds.uppers.get(r));
            }
        }

        chromosomeBoundsFirst.lowers = firstLowerBounds;
        chromosomeBoundsFirst.uppers = firstUpperBounds;
        chromosomeBoundsSec.lowers = secLowerBounds;
        chromosomeBoundsSec.uppers = secUpperBounds;
        generatedFirstChromo.chromosomeBounds = chromosomeBoundsFirst;
        generatedSecondChromo.chromosomeBounds = chromosomeBoundsSec;
        generatedFirstChromo.fitness = -1.0D;
        generatedSecondChromo.fitness = -1.0D;
        pairOffspring.add(generatedFirstChromo);
        pairOffspring.add(generatedSecondChromo);
        return pairOffspring;
    }

    public static ArrayList<Choromosome> crossover(ArrayList<Choromosome> selectedPopulation) {
        ArrayList<Choromosome> crossoveredPopulation = new ArrayList(selectedPopulation);
        Random rand = new Random(System.currentTimeMillis());

        for(int i = 0; i < selectedPopulation.size(); i += 2) {
            if (i + 1 >= selectedPopulation.size()) {
                System.out.println(" Odd Size Population");
                crossoveredPopulation.add(selectedPopulation.get(i));
            } else {
                Choromosome generatedFirstChromo = new Choromosome();
                Choromosome generatedSecondChromo = new Choromosome();
                Choromosome originFirst = (Choromosome)selectedPopulation.get(i);
                Choromosome originSecond = (Choromosome)selectedPopulation.get(i + 1);
                Bounds chromosomeBoundsFirst = new Bounds(originFirst.chromosomeBounds.universe());
                Bounds chromosomeBoundsSec = new Bounds(originSecond.chromosomeBounds.universe());
                Map<Relation, TupleSet> firstLowerBounds = new LinkedHashMap();
                Map<Relation, TupleSet> firstUpperBounds = new LinkedHashMap();
                Map<Relation, TupleSet> secLowerBounds = new LinkedHashMap();
                Map<Relation, TupleSet> secUpperBounds = new LinkedHashMap();
                int j = 0;
                int randomPoint1 = Main.numberOfImportantRelation / 4 + rand.nextInt(Main.numberOfImportantRelation * 3 / 4 - Main.numberOfImportantRelation / 4 + 1) + 1;

                int randomPoint2;
                for(randomPoint2 = Main.numberOfImportantRelation / 4 + rand.nextInt(Main.numberOfImportantRelation * 3 / 4 - Main.numberOfImportantRelation / 4 + 1) + 1; randomPoint2 == randomPoint1; randomPoint2 = Main.numberOfImportantRelation / 4 + rand.nextInt(Main.numberOfImportantRelation * 3 / 4 - Main.numberOfImportantRelation / 4 + 1) + 1) {
                }

                int smallerPoint;
                int largerPoint;
                if (randomPoint1 < randomPoint2) {
                    smallerPoint = randomPoint1;
                    largerPoint = randomPoint2;
                } else {
                    smallerPoint = randomPoint2;
                    largerPoint = randomPoint1;
                }

                int crossPoint1 = Main.numberOfUnmportantRelation - 1 + smallerPoint;
                int crossPoint2 = Main.numberOfUnmportantRelation - 1 + largerPoint;
                int crossPortion = rand.nextInt(3);

                for(Iterator var22 = originFirst.chromosomeBounds.relations().iterator(); var22.hasNext(); ++j) {
                    Relation r = (Relation)var22.next();
                    if (j < Main.numberOfUnmportantRelation) {
                        firstLowerBounds.put(r, originFirst.chromosomeBounds.lowers.get(r));
                        firstUpperBounds.put(r, originFirst.chromosomeBounds.uppers.get(r));
                        secLowerBounds.put(r, originSecond.chromosomeBounds.lowers.get(r));
                        secUpperBounds.put(r, originSecond.chromosomeBounds.uppers.get(r));
                    } else if (j >= crossPoint1 && j <= crossPoint2) {
                        firstLowerBounds.put(r, originSecond.chromosomeBounds.lowers.get(r));
                        firstUpperBounds.put(r, originSecond.chromosomeBounds.uppers.get(r));
                        secLowerBounds.put(r, originFirst.chromosomeBounds.lowers.get(r));
                        secUpperBounds.put(r, originFirst.chromosomeBounds.uppers.get(r));
                    } else {
                        firstLowerBounds.put(r, originFirst.chromosomeBounds.lowers.get(r));
                        firstUpperBounds.put(r, originFirst.chromosomeBounds.uppers.get(r));
                        secLowerBounds.put(r, originSecond.chromosomeBounds.lowers.get(r));
                        secUpperBounds.put(r, originSecond.chromosomeBounds.uppers.get(r));
                    }
                }

                chromosomeBoundsFirst.lowers = firstLowerBounds;
                chromosomeBoundsFirst.uppers = firstUpperBounds;
                chromosomeBoundsSec.lowers = secLowerBounds;
                chromosomeBoundsSec.uppers = secUpperBounds;
                generatedFirstChromo.chromosomeBounds = chromosomeBoundsFirst;
                generatedSecondChromo.chromosomeBounds = chromosomeBoundsSec;
                crossoveredPopulation.add(generatedFirstChromo);
                crossoveredPopulation.add(generatedSecondChromo);
            }
        }

        return crossoveredPopulation;
    }

    public static ArrayList<Choromosome> crossover_tupleLevel_twopoints(ArrayList<Choromosome> selectedPopulation, Random randomGenerator) {
        ArrayList<Choromosome> crossoveredPopulation = new ArrayList(selectedPopulation);

        for(int i = 0; i < selectedPopulation.size(); i += 2) {
            if (i + 1 >= selectedPopulation.size()) {
                System.out.println(" Odd Size Population");
                crossoveredPopulation.add(selectedPopulation.get(i));
            } else {
                Choromosome generatedFirstChromo = new Choromosome();
                Choromosome generatedSecondChromo = new Choromosome();
                Choromosome originFirst = (Choromosome)selectedPopulation.get(i);
                Choromosome originSecond = (Choromosome)selectedPopulation.get(i + 1);
                Bounds chromosomeBoundsFirst = new Bounds(originFirst.chromosomeBounds.universe());
                Bounds chromosomeBoundsSec = new Bounds(originSecond.chromosomeBounds.universe());
                Map<Relation, TupleSet> firstLowerBounds = new LinkedHashMap();
                Map<Relation, TupleSet> firstUpperBounds = new LinkedHashMap();
                Map<Relation, TupleSet> secLowerBounds = new LinkedHashMap();
                Map<Relation, TupleSet> secUpperBounds = new LinkedHashMap();
                int randomPoint1 = 1 + Main.principleTuplesNumber / 6 + randomGenerator.nextInt(2 * Main.principleTuplesNumber / 3);

                int randomPoint2;
                for(randomPoint2 = 1 + Main.principleTuplesNumber / 6 + randomGenerator.nextInt(2 * Main.principleTuplesNumber / 3); randomPoint2 == randomPoint1; randomPoint2 = 1 + Main.principleTuplesNumber / 6 + randomGenerator.nextInt(2 * Main.principleTuplesNumber / 3)) {
                }

                int smallerPoint;
                int largerPoint;
                if (randomPoint1 < randomPoint2) {
                    smallerPoint = randomPoint1;
                    largerPoint = randomPoint2;
                } else {
                    smallerPoint = randomPoint2;
                    largerPoint = randomPoint1;
                }

                SelectedRelation crossRelation1 = findSelectedTupleWithRandomNumber(smallerPoint);
                SelectedRelation crossRelation2 = findSelectedTupleWithRandomNumber(largerPoint);
                int fisrtTupleIndex = crossRelation1.tupleNumber - 1;
                int secondTupleIndex = crossRelation2.tupleNumber - 1;
                Object[] tuplesOfFirstSelectedRelation = (Object[])Main.principleRelationsAndValues.get(crossRelation1.relation);
                Object[] tuplesOfSecondSelectedRelation = (Object[])Main.principleRelationsAndValues.get(crossRelation2.relation);
                IntTuple selectedFirstTuple = (IntTuple)tuplesOfFirstSelectedRelation[fisrtTupleIndex];
                IntTuple selectedSecondTuple = (IntTuple)tuplesOfSecondSelectedRelation[secondTupleIndex];
                int typeOfRelation = 1;
                Iterator var27 = originFirst.chromosomeBounds.relations().iterator();

                while(var27.hasNext()) {
                    Relation r = (Relation)var27.next();
                    String relationString = r.toString();
                    String actualRelation;
                    if (relationString.contains(" ")) {
                        actualRelation = relationString.split(" ")[0] + "." + relationString.split(" ")[1];
                    } else {
                        actualRelation = relationString;
                    }

                    TupleSet tupleSetForFirstLowerBounds;
                    TupleSet tupleSetForSecondLowerBounds;
                    Iterator itr;
                    boolean arriveSelectedTuple;
                    Tuple tuple;
                    if (typeOfRelation == 1) {
                        if (crossRelation1.relation.equals(crossRelation2.relation)) {
                            if (actualRelation.equals(crossRelation1.relation)) {
                                tupleSetForFirstLowerBounds = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
                                tupleSetForSecondLowerBounds = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
                                itr = ((TupleSet)Main.unsolvedSolution.bounds.uppers.get(r)).iterator();
                                arriveSelectedTuple = false;
                                boolean arriveSecondSelectedTuple = false;

                                while(itr.hasNext()) {
//                                    Tuple tuple = (Tuple)itr.next();
                                    tuple = (Tuple)itr.next();
                                    if (!arriveSelectedTuple) {
                                        if (((TupleSet)originFirst.chromosomeBounds.lowers.get(r)).contains(tuple)) {
                                            tupleSetForFirstLowerBounds.add(tuple);
                                        }

                                        if (((TupleSet)originSecond.chromosomeBounds.lowers.get(r)).contains(tuple)) {
                                            tupleSetForSecondLowerBounds.add(tuple);
                                        }

                                        if (tuple.equals(selectedFirstTuple)) {
                                            arriveSelectedTuple = true;
                                        }
                                    } else if (!arriveSecondSelectedTuple) {
                                        if (((TupleSet)originFirst.chromosomeBounds.lowers.get(r)).contains(tuple)) {
                                            tupleSetForSecondLowerBounds.add(tuple);
                                        }

                                        if (((TupleSet)originSecond.chromosomeBounds.lowers.get(r)).contains(tuple)) {
                                            tupleSetForFirstLowerBounds.add(tuple);
                                        }

                                        if (tuple.equals(selectedSecondTuple)) {
                                            arriveSecondSelectedTuple = true;
                                        }
                                    } else {
                                        if (((TupleSet)originFirst.chromosomeBounds.lowers.get(r)).contains(tuple)) {
                                            tupleSetForFirstLowerBounds.add(tuple);
                                        }

                                        if (((TupleSet)originSecond.chromosomeBounds.lowers.get(r)).contains(tuple)) {
                                            tupleSetForSecondLowerBounds.add(tuple);
                                        }
                                    }
                                }

                                typeOfRelation = 3;
                            } else {
                                firstLowerBounds.put(r, originFirst.chromosomeBounds.lowers.get(r));
                                firstUpperBounds.put(r, originFirst.chromosomeBounds.uppers.get(r));
                                secLowerBounds.put(r, originSecond.chromosomeBounds.lowers.get(r));
                                secUpperBounds.put(r, originSecond.chromosomeBounds.uppers.get(r));
                            }
                        } else if (actualRelation.equals(crossRelation1.relation)) {
                            tupleSetForFirstLowerBounds = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
                            tupleSetForSecondLowerBounds = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
                            itr = ((TupleSet)Main.unsolvedSolution.bounds.uppers.get(r)).iterator();
                            arriveSelectedTuple = false;

                            while(itr.hasNext()) {
                                tuple = (Tuple)itr.next();
                                if (!arriveSelectedTuple) {
                                    if (((TupleSet)originFirst.chromosomeBounds.lowers.get(r)).contains(tuple)) {
                                        tupleSetForFirstLowerBounds.add(tuple);
                                    }

                                    if (((TupleSet)originSecond.chromosomeBounds.lowers.get(r)).contains(tuple)) {
                                        tupleSetForSecondLowerBounds.add(tuple);
                                    }

                                    if (tuple.equals(selectedFirstTuple)) {
                                        arriveSelectedTuple = true;
                                    }
                                } else {
                                    if (((TupleSet)originFirst.chromosomeBounds.lowers.get(r)).contains(tuple)) {
                                        tupleSetForSecondLowerBounds.add(tuple);
                                    }

                                    if (((TupleSet)originSecond.chromosomeBounds.lowers.get(r)).contains(tuple)) {
                                        tupleSetForFirstLowerBounds.add(tuple);
                                    }
                                }
                            }

                            firstLowerBounds.put(r, tupleSetForFirstLowerBounds);
                            firstUpperBounds.put(r, tupleSetForFirstLowerBounds);
                            secLowerBounds.put(r, tupleSetForSecondLowerBounds);
                            secUpperBounds.put(r, tupleSetForSecondLowerBounds);
                            typeOfRelation = 2;
                        } else {
                            firstLowerBounds.put(r, originFirst.chromosomeBounds.lowers.get(r));
                            firstUpperBounds.put(r, originFirst.chromosomeBounds.uppers.get(r));
                            secLowerBounds.put(r, originSecond.chromosomeBounds.lowers.get(r));
                            secUpperBounds.put(r, originSecond.chromosomeBounds.uppers.get(r));
                        }
                    }

                    if (typeOfRelation == 2 && !crossRelation1.relation.equals(crossRelation2.relation)) {
                        if (actualRelation.equals(crossRelation2.relation)) {
                            tupleSetForFirstLowerBounds = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
                            tupleSetForSecondLowerBounds = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
                            itr = ((TupleSet)Main.unsolvedSolution.bounds.uppers.get(r)).iterator();
                            arriveSelectedTuple = false;

                            while(itr.hasNext()) {
                                tuple = (Tuple)itr.next();
                                if (!arriveSelectedTuple) {
                                    if (((TupleSet)originFirst.chromosomeBounds.lowers.get(r)).contains(tuple)) {
                                        tupleSetForSecondLowerBounds.add(tuple);
                                    }

                                    if (((TupleSet)originSecond.chromosomeBounds.lowers.get(r)).contains(tuple)) {
                                        tupleSetForFirstLowerBounds.add(tuple);
                                    }

                                    if (tuple.equals(selectedSecondTuple)) {
                                        arriveSelectedTuple = true;
                                    }
                                } else {
                                    if (((TupleSet)originFirst.chromosomeBounds.lowers.get(r)).contains(tuple)) {
                                        tupleSetForFirstLowerBounds.add(tuple);
                                    }

                                    if (((TupleSet)originSecond.chromosomeBounds.lowers.get(r)).contains(tuple)) {
                                        tupleSetForSecondLowerBounds.add(tuple);
                                    }
                                }
                            }

                            firstLowerBounds.put(r, tupleSetForFirstLowerBounds);
                            firstUpperBounds.put(r, tupleSetForFirstLowerBounds);
                            secLowerBounds.put(r, tupleSetForSecondLowerBounds);
                            secUpperBounds.put(r, tupleSetForSecondLowerBounds);
                            typeOfRelation = 3;
                        } else {
                            firstLowerBounds.put(r, originSecond.chromosomeBounds.lowers.get(r));
                            firstUpperBounds.put(r, originSecond.chromosomeBounds.uppers.get(r));
                            secLowerBounds.put(r, originFirst.chromosomeBounds.lowers.get(r));
                            secUpperBounds.put(r, originFirst.chromosomeBounds.uppers.get(r));
                        }
                    }

                    if (typeOfRelation == 3) {
                        firstLowerBounds.put(r, originSecond.chromosomeBounds.lowers.get(r));
                        firstUpperBounds.put(r, originSecond.chromosomeBounds.uppers.get(r));
                        secLowerBounds.put(r, originFirst.chromosomeBounds.lowers.get(r));
                        secUpperBounds.put(r, originFirst.chromosomeBounds.uppers.get(r));
                    }
                }

                chromosomeBoundsFirst.lowers = firstLowerBounds;
                chromosomeBoundsFirst.uppers = firstUpperBounds;
                chromosomeBoundsSec.lowers = secLowerBounds;
                chromosomeBoundsSec.uppers = secUpperBounds;
                generatedFirstChromo.chromosomeBounds = chromosomeBoundsFirst;
                generatedSecondChromo.chromosomeBounds = chromosomeBoundsSec;
                crossoveredPopulation.add(generatedFirstChromo);
                crossoveredPopulation.add(generatedSecondChromo);
            }
        }

        return crossoveredPopulation;
    }

    static SelectedRelation findSelectedTupleWithRandomNumber(int randomNumber) {
        String selectedRelationName = null;
        int selectedNumber = -1;
        int base = 0;

        for(int k = 0; k < Main.possibilityAndRelationList.size(); ++k) {
            PossibilityPair pair = (PossibilityPair)Main.possibilityAndRelationList.get(k);
            if (k != 0) {
                base = ((PossibilityPair)Main.possibilityAndRelationList.get(k - 1)).possibility;
            }

            if (randomNumber <= pair.possibility) {
                selectedRelationName = pair.relation;
                selectedNumber = randomNumber - base;
                break;
            }
        }

        SelectedRelation selectedRelation = new SelectedRelation(selectedNumber, selectedRelationName, randomNumber);
        return selectedRelation;
    }

    public static ArrayList<Choromosome> crossover_tupleLevel(ArrayList<Choromosome> selectedPopulation) {
        ArrayList<Choromosome> crossoveredPopulation = new ArrayList(selectedPopulation);
        Random randomGenerator = new Random(System.currentTimeMillis());

        for(int i = 0; i < selectedPopulation.size(); i += 2) {
            if (i + 1 >= selectedPopulation.size()) {
                System.out.println(" Odd Size Population");
                crossoveredPopulation.add(selectedPopulation.get(i));
            } else {
                Choromosome generatedFirstChromo = new Choromosome();
                Choromosome generatedSecondChromo = new Choromosome();
                Choromosome originFirst = (Choromosome)selectedPopulation.get(i);
                Choromosome originSecond = (Choromosome)selectedPopulation.get(i + 1);
                Bounds chromosomeBoundsFirst = new Bounds(originFirst.chromosomeBounds.universe());
                Bounds chromosomeBoundsSec = new Bounds(originSecond.chromosomeBounds.universe());
                Map<Relation, TupleSet> firstLowerBounds = new LinkedHashMap();
                Map<Relation, TupleSet> firstUpperBounds = new LinkedHashMap();
                Map<Relation, TupleSet> secLowerBounds = new LinkedHashMap();
                Map<Relation, TupleSet> secUpperBounds = new LinkedHashMap();
                int randomNumber = 1 + Main.principleTuplesNumber / 6 + randomGenerator.nextInt(2 * Main.principleTuplesNumber / 3);
                String selectedRelationName = null;
//                int selectedNumber = true;
                SelectedRelation mutateRelation = findSelectedTupleWithRandomNumber(randomNumber);
                int selectedNumber = mutateRelation.tupleNumber;
                selectedRelationName = mutateRelation.relation;
                int tupleIndex = selectedNumber - 1;
                Object[] tuplesOfSelectedRelation = (Object[])Main.principleRelationsAndValues.get(selectedRelationName);
                IntTuple selectedTuple = (IntTuple)tuplesOfSelectedRelation[tupleIndex];
                boolean arrivedSelectedRelation = false;
                int j = 0;
                Iterator var23 = originFirst.chromosomeBounds.relations().iterator();

                while(var23.hasNext()) {
                    Relation r = (Relation)var23.next();
                    ++j;
                    String relationString = r.toString();
                    String actualRelation;
                    if (relationString.contains(" ")) {
                        actualRelation = relationString.split(" ")[0] + "." + relationString.split(" ")[1];
                    } else {
                        actualRelation = relationString;
                    }

                    if (arrivedSelectedRelation) {
                        firstLowerBounds.put(r, originSecond.chromosomeBounds.lowers.get(r));
                        firstUpperBounds.put(r, originSecond.chromosomeBounds.uppers.get(r));
                        secLowerBounds.put(r, originFirst.chromosomeBounds.lowers.get(r));
                        secUpperBounds.put(r, originFirst.chromosomeBounds.uppers.get(r));
                    } else if (!actualRelation.equals(selectedRelationName)) {
                        firstLowerBounds.put(r, originFirst.chromosomeBounds.lowers.get(r));
                        firstUpperBounds.put(r, originFirst.chromosomeBounds.uppers.get(r));
                        secLowerBounds.put(r, originSecond.chromosomeBounds.lowers.get(r));
                        secUpperBounds.put(r, originSecond.chromosomeBounds.uppers.get(r));
                    } else {
                        TupleSet tupleSetForFirstLowerBounds = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
                        TupleSet tupleSetForSecondLowerBounds = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
                        Iterator<Tuple> itr = ((TupleSet)Main.unsolvedSolution.bounds.uppers.get(r)).iterator();
                        boolean arriveSelectedTuple = false;

                        while(itr.hasNext()) {
                            Tuple tuple = (Tuple)itr.next();
                            if (!arriveSelectedTuple) {
                                if (((TupleSet)originFirst.chromosomeBounds.lowers.get(r)).contains(tuple)) {
                                    tupleSetForFirstLowerBounds.add(tuple);
                                }

                                if (((TupleSet)originSecond.chromosomeBounds.lowers.get(r)).contains(tuple)) {
                                    tupleSetForSecondLowerBounds.add(tuple);
                                }

                                if (tuple.equals(selectedTuple)) {
                                    arriveSelectedTuple = true;
                                }
                            } else {
                                if (((TupleSet)originFirst.chromosomeBounds.lowers.get(r)).contains(tuple)) {
                                    tupleSetForSecondLowerBounds.add(tuple);
                                }

                                if (((TupleSet)originSecond.chromosomeBounds.lowers.get(r)).contains(tuple)) {
                                    tupleSetForFirstLowerBounds.add(tuple);
                                }
                            }
                        }

                        firstLowerBounds.put(r, tupleSetForFirstLowerBounds);
                        firstUpperBounds.put(r, tupleSetForFirstLowerBounds);
                        secLowerBounds.put(r, tupleSetForSecondLowerBounds);
                        secUpperBounds.put(r, tupleSetForSecondLowerBounds);
                        arrivedSelectedRelation = true;
                    }
                }

                chromosomeBoundsFirst.lowers = firstLowerBounds;
                chromosomeBoundsFirst.uppers = firstUpperBounds;
                chromosomeBoundsSec.lowers = secLowerBounds;
                chromosomeBoundsSec.uppers = secUpperBounds;
                generatedFirstChromo.chromosomeBounds = chromosomeBoundsFirst;
                generatedSecondChromo.chromosomeBounds = chromosomeBoundsSec;
                crossoveredPopulation.add(generatedFirstChromo);
                crossoveredPopulation.add(generatedSecondChromo);
            }
        }

        return crossoveredPopulation;
    }

    public static ArrayList<Choromosome> mutation_tupleLevel(ArrayList<Choromosome> crossoverPopulation) {
        ArrayList<Choromosome> mutationPopulation = new ArrayList();
        mutationPopulation.addAll(crossoverPopulation);
        Random randomGenerator = new Random(System.currentTimeMillis());
        double x = (double)crossoverPopulation.size() * 0.03D * (double)Main.relations.size();
        if (Main.printMode == PrintMode.DebugMode) {
//            System.out.println("x number of mutation " + x);
        }

        int totalNumberOfImportantRelations = Main.principleTuplesNumber * crossoverPopulation.size();
        Double tmp = (double)totalNumberOfImportantRelations * mutationRate;
        int numberOfMutation = tmp.intValue();
        numberOfMutation = (int)(mutationRate * (double)crossoverPopulation.size());
        if (Main.printMode == PrintMode.DebugMode) {
            System.out.println(" numberOfMutation " + numberOfMutation);
        }

        int j = 0;

        for(int i = 0; i < mutationPopulation.size(); ++i) {
            Choromosome chromo = (Choromosome)mutationPopulation.get(i);
            HashSet<String> principleRelations = new HashSet(Main.principleRelationsList);
            Iterator var12 = Main.unsolvedSolution.bounds.relations().iterator();

            while(var12.hasNext()) {
                Relation r = (Relation)var12.next();
                String relationString = r.toString();
                String actualRelation;
                if (relationString.contains(" ")) {
                    actualRelation = relationString.split(" ")[0] + "." + relationString.split(" ")[1];
                } else {
                    actualRelation = relationString;
                }

                if (principleRelations.contains(actualRelation) && chromo.chromosomeBounds.upperBound(r).size() == 0) {
                    chromo.nullRelations.add(actualRelation);
                }
            }
        }

        while(j < numberOfMutation) {
            GeneticAlgorithm.MutationMode mode = selecteMutationMode(randomGenerator);
            int selectedChromoIndex = randomSelectChromosome(randomGenerator, mutationPopulation.size() - 1, mode) + 1;
            Choromosome selectedChromo = (Choromosome)mutationPopulation.get(selectedChromoIndex);
            Choromosome mutatedChromo = mutateSelectedChromosome_2D(selectedChromo, randomGenerator, mode);
            if (mutatedChromo.chromosomeBounds != null) {
                mutationPopulation.set(selectedChromoIndex, mutatedChromo);
                ++j;
            }
        }

        return mutationPopulation;
    }

    public static GeneticAlgorithm.MutationMode selecteMutationMode(Random randomGenerator) {
        GeneticAlgorithm.MutationMode mode = null;
        int mutationTypePossibility = 1 + randomGenerator.nextInt(100);
        if (mutationTypePossibility > 0 && mutationTypePossibility <= deleteRelationMutationRate) {
            mode = GeneticAlgorithm.MutationMode.DeleteRelationMode;
        } else if (mutationTypePossibility > deleteRelationMutationRate && mutationTypePossibility <= deleteRelationMutationRate + changeRelationMutationRate) {
            mode = GeneticAlgorithm.MutationMode.ChangeRelationMode;
        } else if (mutationTypePossibility > deleteRelationMutationRate + changeRelationMutationRate && mutationTypePossibility <= deleteRelationMutationRate + changeRelationMutationRate + addRelationMutationRate) {
            mode = GeneticAlgorithm.MutationMode.AddRelationMode;
        } else {
            mode = GeneticAlgorithm.MutationMode.OneTupleMode;
        }

        return mode;
    }

    public static Choromosome mutateSelectedChromosome_2D(Choromosome selectedChromo, Random randomGenerator, GeneticAlgorithm.MutationMode mode) {
        Choromosome mutationChromo = new Choromosome();
        Bounds originBounds = selectedChromo.chromosomeBounds;
        Bounds mutationBounds = null;
        switch(mode) {
            case OneRelationMode:
                mutationBounds = mutateBoundsWithOneRelation(originBounds, randomGenerator);
                break;
            case DeleteRelationMode:
                mutationBounds = mutateBounds_DeleteRelation(selectedChromo, originBounds, randomGenerator);
                break;
            case ChangeRelationMode:
                mutationBounds = mutateBounds_ChangeRelation(originBounds, randomGenerator);
                break;
            case AddRelationMode:
                mutationBounds = mutateBounds_AddRelation(selectedChromo, originBounds, randomGenerator);
                break;
            case OneTupleMode:
                mutationBounds = mutateBoundsWithOneTuple_2D(originBounds, randomGenerator);
        }

        mutationChromo.chromosomeBounds = mutationBounds;
        mutationChromo.fitness = -1.0D;
        return mutationChromo;
    }

    public static Bounds mutateBoundsWithNewBounds(Bounds originBounds, Random randomGenerator) {
        Bounds mutationBounds = new Bounds(Main.unsolvedSolution.bounds.universe());
        Map<Relation, TupleSet> lowerBounds = new LinkedHashMap();
        Map<Relation, TupleSet> upperBounds = new LinkedHashMap();
        Iterator var10 = Main.unsolvedSolution.bounds.relations().iterator();

        while(true) {
            while(var10.hasNext()) {
                Relation r = (Relation)var10.next();
                TupleSet tupleSetForLowerBounds = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
                Object[] upperBoundsArray = Main.unsolvedSolution.bounds.upperBound(r).toArray();
                String relationString = r.toString();
                String actualRelation;
                if (relationString.contains(" ")) {
                    actualRelation = relationString.split(" ")[0] + "." + relationString.split(" ")[1];
                } else {
                    actualRelation = relationString;
                }

                TupleSet emptyTupleSet = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
                if (!Main.principleRelationsAndTuplesNumber.containsKey(actualRelation)) {
                    lowerBounds.put(r, Main.unsolvedSolution.bounds.lowers.get(r));
                    upperBounds.put(r, Main.unsolvedSolution.bounds.uppers.get(r));
                } else {
                    int tuplesNumber = randomGenerator.nextInt(upperBoundsArray.length + 1);
                    Set tupleIndexSet = new HashSet();
                    if (tuplesNumber == 0) {
                        tupleSetForLowerBounds = emptyTupleSet;
                    }

                    while(tuplesNumber != 0) {
                        int nextTupleIndex;
                        do {
                            nextTupleIndex = randomGenerator.nextInt(upperBoundsArray.length);
                        } while(tupleIndexSet.contains(nextTupleIndex));

                        tupleIndexSet.add(nextTupleIndex);
                        --tuplesNumber;
                    }

                    Iterator itr = tupleIndexSet.iterator();

                    while(itr.hasNext()) {
                        IntTuple tInst = (IntTuple)upperBoundsArray[(Integer)itr.next()];
                        tupleSetForLowerBounds.add(tInst);
                    }

                    lowerBounds.put(r, tupleSetForLowerBounds);
                    upperBounds.put(r, tupleSetForLowerBounds);
                }
            }

            mutationBounds.uppers = upperBounds;
            mutationBounds.lowers = lowerBounds;
            return mutationBounds;
        }
    }

    public static Bounds mutateBoundsWithSeveralRelations_2D(Bounds originBounds, Random randomGenerator) {
        Bounds mutationBounds = new Bounds(Main.unsolvedSolution.bounds.universe());
        Map<Relation, TupleSet> lowerBounds = new LinkedHashMap();
        Map<Relation, TupleSet> upperBounds = new LinkedHashMap();
        HashSet<Integer> mutateRelationsIndexesSet = new HashSet();
        HashSet<String> mutateRelationsSet = new HashSet();
        int sizeOfMutateRelations = 1 + randomGenerator.nextInt(Main.principleRelationsList.size() - 1);
        System.out.println(" Mutate relations number " + sizeOfMutateRelations);

        while(mutateRelationsIndexesSet.size() < sizeOfMutateRelations) {
            int selectedRelationIndex = randomGenerator.nextInt(Main.principleRelationsList.size());
            if (!mutateRelationsIndexesSet.contains(selectedRelationIndex)) {
                mutateRelationsIndexesSet.add(selectedRelationIndex);
                mutateRelationsSet.add(Main.principleRelationsList.get(selectedRelationIndex));
            }
        }

        Iterator var22 = Main.unsolvedSolution.bounds.relations().iterator();

        while(true) {
            while(var22.hasNext()) {
                Relation r = (Relation)var22.next();
                String relationString = r.toString();
                String actualRelation;
                if (relationString.contains(" ")) {
                    actualRelation = relationString.split(" ")[0] + "." + relationString.split(" ")[1];
                } else {
                    actualRelation = relationString;
                }

                if (!mutateRelationsSet.contains(actualRelation)) {
                    lowerBounds.put(r, originBounds.lowers.get(r));
                    upperBounds.put(r, originBounds.uppers.get(r));
                } else {
                    Object[] upperBoundsArray = ((TupleSet)Main.unsolvedSolution.bounds.uppers.get(r)).toArray();
                    new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());

                    TupleSet tupleSetForLowerBounds;
                    do {
                        tupleSetForLowerBounds = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
                        Random randomTupleNumber = new Random(System.currentTimeMillis());
                        int tuplesNumber = randomTupleNumber.nextInt(upperBoundsArray.length + 1);

                        HashSet tupleIndexSet;
                        for(tupleIndexSet = new HashSet(); tuplesNumber != 0; --tuplesNumber) {
                            int nextTupleIndex;
                            do {
                                nextTupleIndex = randomGenerator.nextInt(upperBoundsArray.length);
                            } while(tupleIndexSet.contains(nextTupleIndex));

                            tupleIndexSet.add(nextTupleIndex);
                        }

                        Iterator<Integer> itr = tupleIndexSet.iterator();
                        Iterator var20 = tupleIndexSet.iterator();

                        while(var20.hasNext()) {
                            Integer index = (Integer)var20.next();
                            IntTuple tInst = (IntTuple)upperBoundsArray[index];
                            tupleSetForLowerBounds.add(tInst);
                        }
                    } while(((TupleSet)originBounds.lowers.get(r)).equals(tupleSetForLowerBounds));

                    lowerBounds.put(r, tupleSetForLowerBounds);
                    upperBounds.put(r, tupleSetForLowerBounds);
                }
            }

            mutationBounds.uppers = upperBounds;
            mutationBounds.lowers = lowerBounds;
            return mutationBounds;
        }
    }

    public static Bounds mutateBounds_DeleteRelation(Choromosome chromo, Bounds originBounds, Random randomGenerator) {
        if (chromo.nullRelations.size() == Main.principleRelationsList.size()) {
            return null;
        } else {
            Bounds mutationBounds = new Bounds(Main.unsolvedSolution.bounds.universe());
            Map<Relation, TupleSet> lowerBounds = new LinkedHashMap();
            Map<Relation, TupleSet> upperBounds = new LinkedHashMap();
            ArrayList<String> nonnullRelationsList = new ArrayList(Main.principleRelationsList);
            nonnullRelationsList.removeAll(chromo.nullRelations);
            int selectedRelationIndex = randomGenerator.nextInt(nonnullRelationsList.size());
            String selectedRelationName = (String)nonnullRelationsList.get(selectedRelationIndex);
            new Random(System.currentTimeMillis());
            Iterator var10 = Main.unsolvedSolution.bounds.relations().iterator();

            while(var10.hasNext()) {
                Relation r = (Relation)var10.next();
                String relationString = r.toString();
                String actualRelation;
                if (relationString.contains(" ")) {
                    actualRelation = relationString.split(" ")[0] + "." + relationString.split(" ")[1];
                } else {
                    actualRelation = relationString;
                }

                if (!actualRelation.equals(selectedRelationName)) {
                    lowerBounds.put(r, originBounds.lowers.get(r));
                    upperBounds.put(r, originBounds.uppers.get(r));
                } else {
                    TupleSet emptyTupleSet = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
                    lowerBounds.put(r, emptyTupleSet);
                    upperBounds.put(r, emptyTupleSet);
                }
            }

            mutationBounds.uppers = upperBounds;
            mutationBounds.lowers = lowerBounds;
            return mutationBounds;
        }
    }

    public static Bounds mutateBounds_ChangeRelation(Bounds originBounds, Random randomGenerator) {
        long onerelationStart = System.currentTimeMillis();
        Bounds mutationBounds = new Bounds(Main.unsolvedSolution.bounds.universe());
        Map<Relation, TupleSet> lowerBounds = new LinkedHashMap();
        Map<Relation, TupleSet> upperBounds = new LinkedHashMap();
        int selectedRelationIndex = randomGenerator.nextInt(Main.principleRelationsList.size());
        String selectedRelationName = (String)Main.principleRelationsList.get(selectedRelationIndex);
        Random randomTupleNumber = new Random(System.currentTimeMillis());
        Iterator var10 = Main.unsolvedSolution.bounds.relations().iterator();

        while(true) {
            label68:
            while(var10.hasNext()) {
                Relation r = (Relation)var10.next();
                String relationString = r.toString();
                String actualRelation;
                if (relationString.contains(" ")) {
                    actualRelation = relationString.split(" ")[0] + "." + relationString.split(" ")[1];
                } else {
                    actualRelation = relationString;
                }

                if (!actualRelation.equals(selectedRelationName)) {
                    lowerBounds.put(r, originBounds.lowers.get(r));
                    upperBounds.put(r, originBounds.uppers.get(r));
                } else {
                    Object[] upperBoundsArray = ((TupleSet)Main.unsolvedSolution.bounds.uppers.get(r)).toArray();
                    TupleSet emptyTupleSet = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
                    int chance = 5;

                    TupleSet tupleSetForLowerBounds;
                    do {
                        if (chance <= 0) {
                            continue label68;
                        }

                        tupleSetForLowerBounds = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
//                        int tuplesNumber = true;
                        int possibilityForEmptyTupleSet = randomTupleNumber.nextInt(100);
                        int tuplesNumber;
                        if (upperBoundsArray.length > 1) {
                            if (chance == 1) {
                                tuplesNumber = upperBoundsArray.length - ((TupleSet)originBounds.uppers.get(r)).size();
                            } else {
                                tuplesNumber = 1 + randomTupleNumber.nextInt(upperBoundsArray.length);
                            }
                        } else {
                            tuplesNumber = 1;
                        }

                        HashSet<Integer> tupleIndexSet = new HashSet();
                        long startTupleNumber = System.currentTimeMillis();
                        if (tuplesNumber == 0) {
                            tupleSetForLowerBounds = emptyTupleSet;
                        } else {
                            label58:
                            while(true) {
                                if (tuplesNumber == 0) {
                                    Iterator<Integer> itr = tupleIndexSet.iterator();
                                    Iterator var25 = tupleIndexSet.iterator();

                                    while(true) {
                                        if (!var25.hasNext()) {
                                            break label58;
                                        }

                                        Integer index = (Integer)var25.next();
                                        IntTuple tInst = (IntTuple)upperBoundsArray[index];
                                        tupleSetForLowerBounds.add(tInst);
                                    }
                                }

                                int nextTupleIndex;
                                do {
                                    nextTupleIndex = randomGenerator.nextInt(upperBoundsArray.length);
                                } while(tupleIndexSet.contains(nextTupleIndex));

                                tupleIndexSet.add(nextTupleIndex);
                                --tuplesNumber;
                            }
                        }

                        --chance;
                    } while(((TupleSet)originBounds.uppers.get(r)).equals(tupleSetForLowerBounds) && chance != 0);

                    lowerBounds.put(r, tupleSetForLowerBounds);
                    upperBounds.put(r, tupleSetForLowerBounds);
                }
            }

            mutationBounds.uppers = upperBounds;
            mutationBounds.lowers = lowerBounds;
            long onerelationEnd = System.currentTimeMillis() - onerelationStart;
            return mutationBounds;
        }
    }

    public static Bounds mutateBounds_AddRelation(Choromosome chromo, Bounds originBounds, Random randomGenerator) {
        if (chromo.nullRelations.size() == 0) {
            return null;
        } else {
            Bounds mutationBounds = new Bounds(Main.unsolvedSolution.bounds.universe());
            Map<Relation, TupleSet> lowerBounds = new LinkedHashMap();
            Map<Relation, TupleSet> upperBounds = new LinkedHashMap();
            Object[] nullRelationList = chromo.nullRelations.toArray();
            int selectedRelationIndex = randomGenerator.nextInt(nullRelationList.length);
            String selectedRelationName = (String)nullRelationList[selectedRelationIndex];
            Random randomTupleNumber = new Random(System.currentTimeMillis());
            Iterator var10 = Main.unsolvedSolution.bounds.relations().iterator();

            while(true) {
                label72:
                while(var10.hasNext()) {
                    Relation r = (Relation)var10.next();
                    String relationString = r.toString();
                    String actualRelation;
                    if (relationString.contains(" ")) {
                        actualRelation = relationString.split(" ")[0] + "." + relationString.split(" ")[1];
                    } else {
                        actualRelation = relationString;
                    }

                    if (!actualRelation.equals(selectedRelationName)) {
                        lowerBounds.put(r, originBounds.lowers.get(r));
                        upperBounds.put(r, originBounds.uppers.get(r));
                    } else {
                        Object[] upperBoundsArray = ((TupleSet)Main.unsolvedSolution.bounds.uppers.get(r)).toArray();
                        TupleSet emptyTupleSet = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
                        int chance = 5;

                        TupleSet tupleSetForLowerBounds;
                        do {
                            if (chance <= 0) {
                                continue label72;
                            }

                            tupleSetForLowerBounds = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
//                            int tuplesNumber = true;
                            int possibilityForEmptyTupleSet = randomTupleNumber.nextInt(100);
                            int tuplesNumber;
                            if (upperBoundsArray.length > 1) {
                                if (chance == 1) {
                                    tuplesNumber = upperBoundsArray.length - ((TupleSet)originBounds.uppers.get(r)).size();
                                } else {
                                    tuplesNumber = 1 + randomTupleNumber.nextInt(upperBoundsArray.length);
                                }
                            } else {
                                tuplesNumber = 1;
                            }

                            HashSet<Integer> tupleIndexSet = new HashSet();
                            long startTupleNumber = System.currentTimeMillis();
                            if (tuplesNumber == 0) {
                                tupleSetForLowerBounds = emptyTupleSet;
                            } else {
                                label62:
                                while(true) {
                                    if (tuplesNumber == 0) {
                                        Iterator<Integer> itr = tupleIndexSet.iterator();
                                        Iterator var25 = tupleIndexSet.iterator();

                                        while(true) {
                                            if (!var25.hasNext()) {
                                                break label62;
                                            }

                                            Integer index = (Integer)var25.next();
                                            IntTuple tInst = (IntTuple)upperBoundsArray[index];
                                            tupleSetForLowerBounds.add(tInst);
                                        }
                                    }

                                    int nextTupleIndex;
                                    do {
                                        nextTupleIndex = randomGenerator.nextInt(upperBoundsArray.length);
                                    } while(tupleIndexSet.contains(nextTupleIndex));

                                    tupleIndexSet.add(nextTupleIndex);
                                    --tuplesNumber;
                                }
                            }

                            --chance;
                        } while(((TupleSet)originBounds.uppers.get(r)).equals(tupleSetForLowerBounds) && chance != 0);

                        lowerBounds.put(r, tupleSetForLowerBounds);
                        upperBounds.put(r, tupleSetForLowerBounds);
                    }
                }

                mutationBounds.uppers = upperBounds;
                mutationBounds.lowers = lowerBounds;
                return mutationBounds;
            }
        }
    }

    public static Bounds mutateBoundsWithPartOfRelation_2D(Bounds originBounds, Random randomGenerator) {
        Bounds mutationBounds = new Bounds(Main.unsolvedSolution.bounds.universe());
        return mutationBounds;
    }

    public static Bounds mutateBoundsWithSeveralTuple_2D(Bounds originBounds, Random randomGenerator) {
        Bounds mutationBounds = new Bounds(Main.unsolvedSolution.bounds.universe());
        int tupleNumbers = (int)(0.06D * (double)Main.principleTuplesNumber);
        HashMap<String, HashSet<Integer>> selectedRelationHashMap = new HashMap();
        HashSet selectedTupleHashSet = new HashSet();

        while(tupleNumbers > 0) {
            int randomTupleNumber = 1 + randomGenerator.nextInt(Main.principleTuplesNumber);
            SelectedRelation tmp = findSelectedTupleWithRandomNumber(randomTupleNumber);
            if (!selectedTupleHashSet.contains(randomTupleNumber)) {
                selectedTupleHashSet.add(randomTupleNumber);
                if (!selectedRelationHashMap.containsKey(tmp.relation)) {
                    selectedRelationHashMap.put(tmp.relation, new HashSet());
                }

                ((HashSet)selectedRelationHashMap.get(tmp.relation)).add(tmp.tupleNumber - 1);
                --tupleNumbers;
            }
        }

        Map<Relation, TupleSet> lowerBounds = new LinkedHashMap();
        Map<Relation, TupleSet> upperBounds = new LinkedHashMap();
        Iterator var9 = Main.unsolvedSolution.bounds.relations().iterator();

        while(true) {
            while(var9.hasNext()) {
                Relation r = (Relation)var9.next();
                String relationString = r.toString();
                String actualRelation;
                if (relationString.contains(" ")) {
                    actualRelation = relationString.split(" ")[0] + "." + relationString.split(" ")[1];
                } else {
                    actualRelation = relationString;
                }

                if (selectedRelationHashMap.containsKey(actualRelation)) {
                    HashSet<Integer> mutationIndexSet = (HashSet)selectedRelationHashMap.get(actualRelation);
                    TupleSet tupleSetForLowerBounds = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
                    Iterator itr = ((TupleSet)originBounds.lowers.get(r)).iterator();

                    while(itr.hasNext()) {
                        Tuple tuple = (Tuple)itr.next();
                        tupleSetForLowerBounds.add(tuple);
                    }

                    Object[] tuplesOfSelectedRelation = (Object[])Main.principleRelationsAndValues.get(actualRelation);
                    Iterator var16 = mutationIndexSet.iterator();

                    while(var16.hasNext()) {
                        Integer tupleIndex = (Integer)var16.next();
                        IntTuple tInst = (IntTuple)tuplesOfSelectedRelation[tupleIndex];
                        if (tupleSetForLowerBounds.contains(tInst)) {
                            tupleSetForLowerBounds.remove(tInst);
                        } else {
                            tupleSetForLowerBounds.add(tInst);
                        }
                    }

                    lowerBounds.put(r, tupleSetForLowerBounds);
                    upperBounds.put(r, tupleSetForLowerBounds);
                } else {
                    lowerBounds.put(r, originBounds.lowers.get(r));
                    upperBounds.put(r, originBounds.uppers.get(r));
                }
            }

            mutationBounds.uppers = upperBounds;
            mutationBounds.lowers = lowerBounds;
            return mutationBounds;
        }
    }

    public static Bounds mutateBoundsWithOneAtom_2D(Bounds originBounds, Random randomGenerator) {
        System.out.println(" originBounds.universe() " + Main.unsolvedSolution.bounds.universe().size());
        Bounds mutationBounds = new Bounds(Main.unsolvedSolution.bounds.universe());
        int selectedRelationIndex = randomGenerator.nextInt(Main.principleRelationsList.size());
        String selectedRelationName = (String)Main.principleRelationsList.get(selectedRelationIndex);
        Map<Relation, TupleSet> lowerBounds = new LinkedHashMap();
        Map<Relation, TupleSet> upperBounds = new LinkedHashMap();
        Iterator var9 = Main.unsolvedSolution.bounds.relations().iterator();

        while(true) {
            while(var9.hasNext()) {
                Relation r = (Relation)var9.next();
                TupleSet emptyTupleSet = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
                String relationString = r.toString();
                String actualRelation;
                if (relationString.contains(" ")) {
                    actualRelation = relationString.split(" ")[0] + "." + relationString.split(" ")[1];
                } else {
                    actualRelation = relationString;
                }

                if (actualRelation.equals(selectedRelationName)) {
                    TupleSet tupleSetForLowerBounds = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
                    Iterator<Tuple> itr = ((TupleSet)Main.unsolvedSolution.bounds.uppers.get(r)).iterator();
                    if (((TupleSet)originBounds.lowers.get(r)).size() <= 0) {
                        tupleSetForLowerBounds = emptyTupleSet;
                    } else {
                        int selectedTupleIndex = randomGenerator.nextInt(((TupleSet)originBounds.lowers.get(r)).size());
                        Object[] tuplesOfSelectedRelation = ((TupleSet)originBounds.lowers.get(r)).toArray();
                        IntTuple tInst = (IntTuple)tuplesOfSelectedRelation[selectedTupleIndex];
                        int arity = tInst.arity();
                        int selectedAtomIndex = randomGenerator.nextInt(arity);
                        String selectedAtom = tInst.atom(selectedAtomIndex).toString();
                        String[] selectedAtomComponents = selectedAtom.split("\\$");
                        System.out.println("selectedAtom :" + selectedAtom + " |  selectedAtomIndex" + selectedAtomIndex + " | tInst" + tInst);
                        String keyofAtom = selectedAtomComponents[0];
                        String numberWithInAtom = selectedAtomComponents[1];
                        int scopeOfAtomNumber = (Integer)atomsNameAndScope.get(keyofAtom);
                        int generatedNumberWithInAtom;
                        if (scopeOfAtomNumber > 0) {
                            do {
                                generatedNumberWithInAtom = randomGenerator.nextInt(scopeOfAtomNumber + 1);
                            } while(generatedNumberWithInAtom == Integer.parseInt(numberWithInAtom));
                        } else {
                            generatedNumberWithInAtom = 0;
                        }

                        String generatedAtom = keyofAtom + "$" + generatedNumberWithInAtom;
                        String generatedTupleString = tInst.toString().replace(selectedAtom, generatedAtom);
                        boolean isSame = false;
                        if (tInst.toString().equals(generatedTupleString)) {
                            isSame = true;
                        }

                        label60:
                        while(true) {
                            while(true) {
                                if (!itr.hasNext()) {
                                    break label60;
                                }

                                Tuple tuple = (Tuple)itr.next();
                                if (isSame) {
                                    if (((TupleSet)originBounds.lowers.get(r)).toString().contains(tuple.toString()) && !tuple.toString().equals(tInst.toString())) {
                                        tupleSetForLowerBounds.add(tuple);
                                    }
                                } else if (((TupleSet)originBounds.lowers.get(r)).toString().contains(tuple.toString()) && !tuple.toString().equals(tInst.toString()) || tuple.toString().equals(generatedTupleString)) {
                                    tupleSetForLowerBounds.add(tuple);
                                }
                            }
                        }
                    }

                    lowerBounds.put(r, tupleSetForLowerBounds);
                    upperBounds.put(r, tupleSetForLowerBounds);
                } else {
                    lowerBounds.put(r, originBounds.lowers.get(r));
                    upperBounds.put(r, originBounds.uppers.get(r));
                }
            }

            mutationBounds.uppers = upperBounds;
            mutationBounds.lowers = lowerBounds;
            return mutationBounds;
        }
    }

    public static Bounds mutateBoundsWithOneRelation(Bounds originBounds, Random randomGenerator) {
        long onerelationStart = System.currentTimeMillis();
        Bounds mutationBounds = new Bounds(Main.unsolvedSolution.bounds.universe());
        Map<Relation, TupleSet> lowerBounds = new LinkedHashMap();
        Map<Relation, TupleSet> upperBounds = new LinkedHashMap();
        int selectedRelationIndex = randomGenerator.nextInt(Main.principleRelationsList.size());
        String selectedRelationName = (String)Main.principleRelationsList.get(selectedRelationIndex);
        Random randomTupleNumber = new Random(System.currentTimeMillis());
        Iterator var10 = Main.unsolvedSolution.bounds.relations().iterator();

        while(true) {
            label68:
            while(var10.hasNext()) {
                Relation r = (Relation)var10.next();
                String relationString = r.toString();
                String actualRelation;
                if (relationString.contains(" ")) {
                    actualRelation = relationString.split(" ")[0] + "." + relationString.split(" ")[1];
                } else {
                    actualRelation = relationString;
                }

                if (!actualRelation.equals(selectedRelationName)) {
                    lowerBounds.put(r, originBounds.lowers.get(r));
                    upperBounds.put(r, originBounds.uppers.get(r));
                } else {
                    Object[] upperBoundsArray = ((TupleSet)Main.unsolvedSolution.bounds.uppers.get(r)).toArray();
                    TupleSet emptyTupleSet = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
                    int chance = 3;

                    TupleSet tupleSetForLowerBounds;
                    do {
                        if (chance <= 0) {
                            continue label68;
                        }

                        tupleSetForLowerBounds = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
//                        int tuplesNumber = true;
                        int tuplesNumber;
                        if (upperBoundsArray.length > 1) {
                            if (chance == 1) {
                                tuplesNumber = upperBoundsArray.length - ((TupleSet)originBounds.uppers.get(r)).size();
                            } else {
                                tuplesNumber = randomTupleNumber.nextInt(upperBoundsArray.length + 1);
                            }
                        } else {
                            tuplesNumber = 1 - ((TupleSet)originBounds.uppers.get(r)).size();
                        }

                        HashSet<Integer> tupleIndexSet = new HashSet();
                        if (tuplesNumber == 0) {
                            tupleSetForLowerBounds = emptyTupleSet;
                        } else {
                            label58:
                            while(true) {
                                if (tuplesNumber == 0) {
                                    Iterator<Integer> itr = tupleIndexSet.iterator();
                                    Iterator var22 = tupleIndexSet.iterator();

                                    while(true) {
                                        if (!var22.hasNext()) {
                                            break label58;
                                        }

                                        Integer index = (Integer)var22.next();
                                        IntTuple tInst = (IntTuple)upperBoundsArray[index];
                                        tupleSetForLowerBounds.add(tInst);
                                    }
                                }

                                int nextTupleIndex;
                                do {
                                    nextTupleIndex = randomGenerator.nextInt(upperBoundsArray.length);
                                } while(tupleIndexSet.contains(nextTupleIndex));

                                tupleIndexSet.add(nextTupleIndex);
                                --tuplesNumber;
                            }
                        }

                        --chance;
                    } while(((TupleSet)originBounds.uppers.get(r)).equals(tupleSetForLowerBounds) && chance != 0);

                    lowerBounds.put(r, tupleSetForLowerBounds);
                    upperBounds.put(r, tupleSetForLowerBounds);
                }
            }

            mutationBounds.uppers = upperBounds;
            mutationBounds.lowers = lowerBounds;
            long onerelationEnd = System.currentTimeMillis() - onerelationStart;
            return mutationBounds;
        }
    }

    public static Bounds mutateBoundsWithOneTuple_2D(Bounds originBounds, Random randomGenerator) {
        long onetupleStart = System.currentTimeMillis();
        Bounds mutationBounds = new Bounds(Main.unsolvedSolution.bounds.universe());
        int randomNumber = 1 + randomGenerator.nextInt(Main.principleTuplesNumber);
        String selectedRelationName = null;
        int selectedNumber = -1;
        int base = 0;

        int i;
        for(i = 0; i < Main.possibilityAndRelationList.size(); ++i) {
            PossibilityPair pair = (PossibilityPair)Main.possibilityAndRelationList.get(i);
            if (i != 0) {
                base = ((PossibilityPair)Main.possibilityAndRelationList.get(i - 1)).possibility;
            }

            if (randomNumber <= pair.possibility) {
                selectedRelationName = pair.relation;
                selectedNumber = randomNumber - base;
                break;
            }
        }

        i = selectedNumber - 1;
        Object[] tuplesOfSelectedRelation = (Object[])Main.principleRelationsAndValues.get(selectedRelationName);
        IntTuple tInst = (IntTuple)tuplesOfSelectedRelation[i];
        Map<Relation, TupleSet> lowerBounds = new LinkedHashMap();
        Map<Relation, TupleSet> upperBounds = new LinkedHashMap();
        Iterator var15 = Main.unsolvedSolution.bounds.relations().iterator();

        while(true) {
            while(var15.hasNext()) {
                Relation r = (Relation)var15.next();
                String relationString = r.toString();
                String actualRelation;
                if (relationString.contains(" ")) {
                    actualRelation = relationString.split(" ")[0] + "." + relationString.split(" ")[1];
                } else {
                    actualRelation = relationString;
                }

                if (actualRelation.equals(selectedRelationName)) {
                    TupleSet tupleSetForLowerBounds = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
                    Iterator itr = ((TupleSet)originBounds.lowers.get(r)).iterator();

                    while(itr.hasNext()) {
                        Tuple tuple = (Tuple)itr.next();
                        tupleSetForLowerBounds.add(tuple);
                    }

                    if (tupleSetForLowerBounds.contains(tInst)) {
                        tupleSetForLowerBounds.remove(tInst);
                    } else {
                        tupleSetForLowerBounds.add(tInst);
                    }

                    lowerBounds.put(r, tupleSetForLowerBounds);
                    upperBounds.put(r, tupleSetForLowerBounds);
                } else {
                    lowerBounds.put(r, originBounds.lowers.get(r));
                    upperBounds.put(r, originBounds.uppers.get(r));
                }
            }

            mutationBounds.uppers = upperBounds;
            mutationBounds.lowers = lowerBounds;
            long onetupleEnd = System.currentTimeMillis() - onetupleStart;
            return mutationBounds;
        }
    }

    public static Bounds mutateSelectedRelation(String selectedRelationName, Bounds originBounds, Random randomGenerator) {
        Bounds mutationBounds = new Bounds(Main.unsolvedSolution.bounds.universe());
        Object[] tuplesOfSelectedRelation = (Object[])Main.principleRelationsAndValues.get(selectedRelationName);
        int selectedTupleIndex = randomGenerator.nextInt(tuplesOfSelectedRelation.length);
        IntTuple tInst = (IntTuple)tuplesOfSelectedRelation[selectedTupleIndex];
        System.out.println("Selected Relation" + selectedRelationName + " | selected tuple :" + tInst);
        Map<Relation, TupleSet> lowerBounds = new LinkedHashMap();
        Map<Relation, TupleSet> upperBounds = new LinkedHashMap();
        Iterator var10 = Main.unsolvedSolution.bounds.relations().iterator();

        while(true) {
            while(var10.hasNext()) {
                Relation r = (Relation)var10.next();
                String relationString = r.toString();
                String actualRelation;
                if (relationString.contains(" ")) {
                    actualRelation = relationString.split(" ")[0] + "." + relationString.split(" ")[1];
                } else {
                    actualRelation = relationString;
                }

                if (actualRelation.equals(selectedRelationName)) {
                    TupleSet tupleSetForLowerBounds = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
                    Iterator itr = ((TupleSet)originBounds.lowers.get(r)).iterator();

                    while(itr.hasNext()) {
                        Tuple tuple = (Tuple)itr.next();
                        tupleSetForLowerBounds.add(tuple);
                    }

                    if (tupleSetForLowerBounds.contains(tInst)) {
                        tupleSetForLowerBounds.remove(tInst);
                    } else {
                        tupleSetForLowerBounds.add(tInst);
                    }

                    lowerBounds.put(r, tupleSetForLowerBounds);
                    upperBounds.put(r, tupleSetForLowerBounds);
                } else {
                    lowerBounds.put(r, originBounds.lowers.get(r));
                    upperBounds.put(r, originBounds.uppers.get(r));
                }
            }

            mutationBounds.uppers = upperBounds;
            mutationBounds.lowers = lowerBounds;
            return mutationBounds;
        }
    }

    public static int randomSelectChromosome(Random randomGenerator, int size, GeneticAlgorithm.MutationMode mode) {
//        int index = true;
        int index = randomGenerator.nextInt(size);
        return index;
    }

    public static ArrayList<Choromosome> mutation(ArrayList<Choromosome> crossoverPopulation) {
        Random rand = new Random(System.currentTimeMillis());
        int totalNumberOfImportantRelations = Main.numberOfImportantRelation * crossoverPopulation.size();
        Double tmp = (double)totalNumberOfImportantRelations * mutationRate * 1.5D;
        int numberOfMutation = (int)((double)crossoverPopulation.size() * 0.7D);
        System.out.println("Number of mutation " + numberOfMutation);
        Set<MutationCoordinate> coordinateSet = new HashSet();

        for(int k = 0; k < numberOfMutation; ++k) {
            MutationCoordinate coordinate = new MutationCoordinate();

            do {
                coordinate.y = rand.nextInt(crossoverPopulation.size());

                int point;
                do {
                    point = Main.numberOfUnmportantRelation + rand.nextInt(Main.numberOfImportantRelation);
                } while(Main.fixedRelationIndexSet.contains(point));

                coordinate.x = point;
            } while(coordinateSet.contains(coordinate));

            coordinateSet.add(coordinate);
        }

        Object[] coordinateArray = coordinateSet.toArray();
        ArrayList<MutationCoordinate> coordinateArrayList = new ArrayList();
        Object[] var32 = coordinateArray;
        int w = coordinateArray.length;

        for(int var10 = 0; var10 < w; ++var10) {
            Object co = var32[var10];
            MutationCoordinate c = (MutationCoordinate)co;
            coordinateArrayList.add(c);
        }

        ArrayList<Choromosome> mutationPopulation = new ArrayList(crossoverPopulation);

        for(w = 0; w < coordinateArrayList.size(); ++w) {
            Choromosome mutationChromo = new Choromosome();
            int yAxis = ((MutationCoordinate)coordinateArrayList.get(w)).y;
            int mutationPoint = ((MutationCoordinate)coordinateArrayList.get(w)).x;
            Bounds originBounds = ((Choromosome)mutationPopulation.get(yAxis)).chromosomeBounds;
            Bounds mutationBounds = new Bounds(Main.unsolvedSolution.bounds.universe());
            Map<Relation, TupleSet> lowerBounds = new LinkedHashMap();
            Map<Relation, TupleSet> upperBounds = new LinkedHashMap();
            int j = 0;

            for(Iterator var18 = Main.unsolvedSolution.bounds.relations().iterator(); var18.hasNext(); ++j) {
                Relation r = (Relation)var18.next();
                if (j != mutationPoint) {
                    lowerBounds.put(r, originBounds.lowers.get(r));
                    upperBounds.put(r, originBounds.uppers.get(r));
                } else {
                    Object[] upperBoundsArray = ((TupleSet)Main.unsolvedSolution.bounds.uppers.get(r)).toArray();
                    new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());

                    TupleSet tupleSetForLowerBounds;
                    do {
                        tupleSetForLowerBounds = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
                        Random randomTupleNumber = new Random(System.currentTimeMillis());
                        int tuplesNumber = randomTupleNumber.nextInt(upperBoundsArray.length + 1);

                        HashSet tupleIndexSet;
                        for(tupleIndexSet = new HashSet(); tuplesNumber != 0; --tuplesNumber) {
                            int nextTupleIndex;
                            do {
                                nextTupleIndex = rand.nextInt(upperBoundsArray.length);
                            } while(tupleIndexSet.contains(nextTupleIndex));

                            tupleIndexSet.add(nextTupleIndex);
                        }

                        Iterator<Integer> itr = tupleIndexSet.iterator();
                        Iterator var28 = tupleIndexSet.iterator();

                        while(var28.hasNext()) {
                            Integer index = (Integer)var28.next();
                            IntTuple tInst = (IntTuple)upperBoundsArray[index];
                            tupleSetForLowerBounds.add(tInst);
                        }
                    } while(((TupleSet)originBounds.lowers.get(r)).equals(tupleSetForLowerBounds));

                    lowerBounds.put(r, tupleSetForLowerBounds);
                    upperBounds.put(r, tupleSetForLowerBounds);
                }
            }

            mutationBounds.uppers = upperBounds;
            mutationBounds.lowers = lowerBounds;
            mutationChromo.chromosomeBounds = mutationBounds;
            Choromosome oriChromo = (Choromosome)mutationPopulation.get(yAxis);
            mutationPopulation.set(yAxis, mutationChromo);
        }

        return mutationPopulation;
    }

    private static void quickSortCoordinate(ArrayList<MutationCoordinate> coordinates) {
        recursiveQuickSort(coordinates, 0, coordinates.size() - 1);
    }

    private static void recursiveQuickSort(ArrayList<MutationCoordinate> coordinates, int low, int high) {
        if (low < high) {
            int randomMiddle = partition(coordinates, low, high);
            recursiveQuickSort(coordinates, low, randomMiddle - 1);
            recursiveQuickSort(coordinates, randomMiddle + 1, high);
        }

    }

    private static int partition(ArrayList<MutationCoordinate> coordinates, int low, int high) {
        float pivot = (float)((MutationCoordinate)coordinates.get(low)).y;
        System.out.println("partition in ga");
        MutationCoordinate piv = (MutationCoordinate)coordinates.get(low);

        while(low < high) {
            while(low < high && (float)((MutationCoordinate)coordinates.get(high)).y >= pivot) {
                --high;
            }

            coordinates.set(low, coordinates.get(high));

            while(low < high && (float)((MutationCoordinate)coordinates.get(low)).y <= pivot) {
                ++low;
            }

            coordinates.set(high, coordinates.get(low));
        }

        coordinates.set(low, piv);
        return low;
    }

    static {
        oneTupleMutationRate = 100 - deleteRelationMutationRate - changeRelationMutationRate - addRelationMutationRate;
        unbiasedPickBetterRate = 85;
        atomsNameAndScope = new HashMap();
    }

    public static enum MutationMode {
        OneTupleMode,
        OneRelationMode,
        DeleteRelationMode,
        ChangeRelationMode,
        AddRelationMode;

        private MutationMode() {
        }
    }
}
