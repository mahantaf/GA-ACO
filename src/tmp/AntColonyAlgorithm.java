package tmp;

import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import kodkod.ast.Relation;
import kodkod.instance.Bounds;
import kodkod.instance.TupleSet;
import java.util.*;

public class AntColonyAlgorithm {

    public double pheromoneWeight = 0.4;
    public double heuristicWeight = 0.6;
    public int numberOfAnts = 32;
    public double minPheromone = 5;
    public double maxPheromone = 30;
    public double defaultPheromone = 5;
    public double remainderPheromone = 5;
    public double evaporationRate = 0.8;
    public int numberOfIterations = 100;
    public int bestAntIndex = 0;
    public Ant bestAnt;
    public boolean initialAnts = true;
    public ArrayList<Ant> ants = new ArrayList<>();
    public Map<Relation, ArrayList<AntNode>> relationNodes = new LinkedHashMap<>();
    public ArrayList<Relation> unimportantRelations = new ArrayList<>();
    public Choromosome bestGASolution = null;
    A4Solution notSolvedSolution = null;


    public void setNotSolvedSolution(A4Solution notSolvedSolution) {
        this.notSolvedSolution = notSolvedSolution;
    }

    public void setBestGASolution(Choromosome bestGASolution) {
        this.bestGASolution = bestGASolution;
    }

    public AntColonyAlgorithm() {
        this.bestAnt = null;
    }

    public void initializeAntsByBestChromosome() {
        Ant fullSolutionAnt = new Ant();
        for (Relation relation : relationNodes.keySet()) {
            TupleSet chromosomeTuples = bestGASolution.chromosomeBounds.uppers.get(relation);
            AntNode node = findNodeByTupleSet(relation, chromosomeTuples);

            fullSolutionAnt.addNode(node);
        }
        int index = 0;
        for (int i = 0; i < this.numberOfAnts; ++i) {
            if (initialAnts)
                ants.add(new Ant(fullSolutionAnt));
            else
                ants.set(index, new Ant(fullSolutionAnt));
            ++index;
        }
    }

    public void initializeAntsByLowerBounds() {
        System.out.println("Initialize Lower Bounds Start");
        long cTime = System.currentTimeMillis();
        Ant fullSolutionAnt = new Ant();
        for (Relation r : relationNodes.keySet()) {
            if (!Main.isUnimportant(r)) {
                TupleSet lowerTuples = notSolvedSolution.bounds.lowers.get(r);
                AntNode node = findNodeByTupleSet(r, lowerTuples);
                AntNode setNode;
                if (node == null) {
                    setNode = new AntNode(r, lowerTuples, defaultPheromone);
                    relationNodes.get(r).add(setNode);
                } else {
                    setNode = node;
                }
                fullSolutionAnt.addNode(setNode);
            }
        }
//        Main.evaluateAntPartialSolution(fullSolutionAnt, unimportantRelations);
        int index = 0;
        for (int i = 0; i < this.numberOfAnts; ++i) {
            if (initialAnts)
                ants.add(new Ant(fullSolutionAnt));
            else
                ants.set(index, new Ant(fullSolutionAnt));
            ++index;
        }
        System.out.println("Initialize Lower Bounds End: " + (System.currentTimeMillis() - cTime) + " ms");
    }

    public void initializeAnts() {
        Ant fullSolutionAnt = new Ant();

        for (HashMap.Entry<Relation, ArrayList<AntNode>> entry : relationNodes.entrySet())
            fullSolutionAnt.addNode(entry.getValue().get(0));

        int index = 0;
        for (int i = 0; i < this.numberOfAnts; ++i) {
            if (initialAnts)
                ants.add(new Ant(fullSolutionAnt));
            else
                ants.set(index, new Ant(fullSolutionAnt));
            ++index;
        }
    }

    public void initializeNodes(HashMap<Relation, ArrayList<TupleSet>> relationTuples) {
        for (HashMap.Entry<Relation, ArrayList<TupleSet>> entry: relationTuples.entrySet()) {
            Relation relation = entry.getKey();
            ArrayList<AntNode> nodes = new ArrayList<>();
            for (TupleSet tupleSet : entry.getValue())
                nodes.add(new AntNode(relation, tupleSet, defaultPheromone));
            relationNodes.put(relation, nodes);
        }
    }

    public void setUnimportantRelations(ArrayList<Relation> unimportantRelations) {
        this.unimportantRelations = unimportantRelations;
    }

    public void sampleNodes() {
        for (HashMap.Entry<Relation, ArrayList<AntNode>> entry: relationNodes.entrySet()) {
            System.out.println(entry.getValue().get(entry.getValue().size() - 1));
        }
    }

    public void sampleNonInitialNodes() {
        for (HashMap.Entry<Relation, ArrayList<AntNode>> entry: relationNodes.entrySet()) {
            System.out.println("Relation: " +  entry.getKey());
            for (AntNode node : entry.getValue()) {
                if (node.pheromone > defaultPheromone) {
                    System.out.println("Pheromone: " + node.pheromone);
                    TupleSet tupleSet = node.getTuples();
                    System.out.println("Tuple Set:");
                    System.out.println(tupleSet);
                }
            }
            System.out.println("-------------------------------");
        }
    }

    public void printGraphSize() {
        System.out.println("------------------");
        for (HashMap.Entry<Relation, ArrayList<AntNode>> entry: relationNodes.entrySet())
            System.out.println("Relation: " + entry.getKey() + " size: " + entry.getValue().size());
        System.out.println("------------------");
    }

    public void printAnts() {
        int index = 1;
        for (Ant ant : ants) {
            System.out.println(index + ") ");
            System.out.println(ant);
//            System.out.println("Fitness: " + ant.fitness);
//            System.out.println("Failed Constraints: " + ant.failedConstraintsNumber);
//            System.out.println("Failed Relations: " + ant.failedRelationNumber);
            System.out.println("=====================");
            ++index;
        }
    }

    public Ant startOptimization() {
        sortRelationsByNodeSize();
        int iter = 0;
        while (iter < numberOfIterations && !Main.foundSolution) {
            long startIteration = System.currentTimeMillis();
            System.out.println("------------------------- Iteration " + iter + " -------------------------");
            printGraphSize();
//            initializeAnts();
//            initializeAntsByBestChromosome();
            initializeAntsByLowerBounds();
            moveAnts();
            evaluateAnts();
            updatePheromones();
            updateBestAnt();
//            printAnts();
            System.out.println(bestAnt);
            ++iter;
            initialAnts = false;
            long endIteration = System.currentTimeMillis() - startIteration;
            System.out.println("End Iteration: " + endIteration + " ms");
        }

        return bestAnt;
    }

    public void updateNodesByChromosome(Bounds chromosomeBounds) {
        for (Relation relation : relationNodes.keySet()) {
            TupleSet chromosomeTuples = chromosomeBounds.uppers.get(relation);
            AntNode node = findNodeByTupleSet(relation, chromosomeTuples);

            if (node == null)
                relationNodes.get(relation).add(new AntNode(relation, chromosomeTuples, defaultPheromone));
        }
    }

    private void sortRelationsByNodeSize() {
        List<Map.Entry<Relation, ArrayList<AntNode>>> list =
                new LinkedList<Map.Entry<Relation, ArrayList<AntNode>>>(relationNodes.entrySet());
        list.sort(new Comparator<Map.Entry<Relation, ArrayList<AntNode>>>() {
            @Override
            public int compare(Map.Entry<Relation, ArrayList<AntNode>> o1, Map.Entry<Relation, ArrayList<AntNode>> o2) {
                return o1.getValue().size() - o2.getValue().size();
            }
        });

        relationNodes.clear();

        for (Map.Entry<Relation, ArrayList<AntNode>> entry : list)
            relationNodes.put(entry.getKey(), entry.getValue());
    }

    private void moveAnts() {
        System.out.println("Moving Ants Start");
        long cTime = System.currentTimeMillis();
        int index = 0;
        for (Ant ant: ants) {
            index++;
            long cTime2 = System.currentTimeMillis();
            int relationIndex = 0;
            for (HashMap.Entry<Relation, ArrayList<AntNode>> entry : relationNodes.entrySet()) {
                long cTime3 = System.currentTimeMillis();
                Relation relation = entry.getKey();
                selectNode(ant, relation, relationIndex);
                System.out.println(
                        "Ant " + index + " selected relation node: " + relation +
                                " size " + relationNodes.get(relation).size() + ". Time: " +
                                (System.currentTimeMillis() - cTime3) + " ms"
                );
                ++relationIndex;
            }
            System.out.println("Ant " + index + " moving time: " + (System.currentTimeMillis() - cTime2) + " ms");
            if (Main.foundSolution)
                break;
        }
        System.out.println("Moving Ants End: " + (System.currentTimeMillis() - cTime) + " ms");
    }

    private void evaluateAnts() {
        System.out.println("Evaluate Ants Begin");
        long cTime = System.currentTimeMillis();
        Main.evaluateAntsSolutions(this, ants);
        System.out.println("Evaluate Ants End: " + (System.currentTimeMillis() - cTime) + " ms");

    }

    private AntNode findNodeByTupleSet(Relation relation, TupleSet tupleSet) {
        ArrayList<AntNode> relationNode = relationNodes.get(relation);
        for (AntNode node : relationNode) {
            if (node.tuples.equals(tupleSet))
                return node;
        }
        return null;
    }

    public void updatePheromonesByGASolution(Bounds chromosomeBounds, double fitness) {
        for (Relation relation : relationNodes.keySet()) {

            TupleSet chromosomeTuples = chromosomeBounds.uppers.get(relation);
            AntNode node = findNodeByTupleSet(relation, chromosomeTuples);

            if (node == null) {
                System.out.println("Null Node Exception");
                continue;
            }

            double contribution = remainderPheromone / fitness;
//            node.setPheromone(node.pheromone + contribution);
            node.setPheromone(Math.min(node.pheromone + contribution, maxPheromone));
        }
    }

    private void updatePheromones() {
        System.out.println("Update Pheromones...");
        for (HashMap.Entry<Relation, ArrayList<AntNode>> entry: relationNodes.entrySet()) {
            for (AntNode node : entry.getValue()) {
//                node.setPheromone(node.pheromone * evaporationRate);
                node.setPheromone(Math.max(node.pheromone * evaporationRate, minPheromone));
            }
        }

        for (Ant ant : ants) {
            double contribution = remainderPheromone / ant.fitness;
            for (AntNode node : ant.nodes) {
//                node.setPheromone(node.pheromone + contribution);
                node.setPheromone(Math.min(node.pheromone + contribution, maxPheromone));
            }
        }
    }

    private void updateBestAnt() {
        System.out.println("Update best Ant...");

        if (bestAnt == null) {
            bestAntIndex = 0;
            bestAnt = new Ant(ants.get(0));
        }

        int index = 0;
        double bestFitness = bestAnt.fitness;
        boolean bestChanged = false;
        for (Ant ant : ants) {
            if (ant.fitness < bestFitness) {
                bestChanged = true;
                bestAntIndex = index;
                bestFitness = ant.fitness;
            }
            ++index;
        }
        if (bestChanged)
            bestAnt = new Ant(ants.get(bestAntIndex));
    }

    /**
     * Selection Mechanism:
     *
     * If relation is single node then select that single node
     * With 10% probability select from non-optimal nodes
     * With 90% probability select from optimal nodes by considering pheromone and heuristic
     */
    private void selectNode(Ant ant, Relation relation, int relationIndex) {
        AntNode selectedNode;
        if (relationNodes.get(relation).size() == 1) {
            selectedNode = relationNodes.get(relation).get(0);
        } else {
//            setAntNodesProbability(ant, relation, relationIndex);
            setAntNodeProbabilityMultiThread(ant, relation, relationIndex);
            selectedNode = selectNodeByProbability(ant, relation);
        }
        ant.setNode(selectedNode, relationIndex);
    }

    private void setAntNodeProbabilityMultiThread(Ant ant, Relation relation, int relationIndex) {
        ArrayList<AntNode> relationNode = relationNodes.get(relation);

        ArrayList<Ant> tempAnts = new ArrayList<>();

        for (AntNode node : relationNode) {
            Ant tempAnt = new Ant(ant);
            tempAnt.setNode(node, relationIndex);
            tempAnts.add(tempAnt);
        }

        Main.evaluateAntsPartialSolutionsMultiThread(this, tempAnts);

        double pheromoneHeuristicSum = 0;
        int index = 0;
        for (AntNode node : relationNode) {
            double nodeHeuristic = (double) 1 / (1 + tempAnts.get(index).getDistance());
            pheromoneHeuristicSum +=
                    Math.pow(node.pheromone, pheromoneWeight) *
                            Math.pow(nodeHeuristic, heuristicWeight);
            node.setTempHeuristic(nodeHeuristic);
            index++;
        }

        for (AntNode node : relationNode) {
            double probability = Math.pow(node.pheromone, pheromoneWeight) *
                    Math.pow(node.tempHeuristic, heuristicWeight) /
                    pheromoneHeuristicSum;
            node.setProbability(probability);
        }
    }

    private void setAntNodesProbability(Ant ant, Relation relation, int relationIndex) {
        ArrayList<AntNode> relationNode = relationNodes.get(relation);

        // Step 1. Calculate pheromone-heuristic sum
        double pheromoneHeuristicSum = 0;
        for (AntNode node : relationNode) {
            Ant tempAnt = new Ant(ant);
            tempAnt.setNode(node, relationIndex);
            Main.evaluateAntPartialSolution(tempAnt, unimportantRelations);
            double nodeHeuristic = (double) 1 / (1 + tempAnt.getDistance());
            pheromoneHeuristicSum +=
                    Math.pow(node.pheromone, pheromoneWeight) *
                            Math.pow(nodeHeuristic, heuristicWeight);
            node.setTempHeuristic(nodeHeuristic);
        }

        // Step 2. Calculate each relation node temp probability
        for (AntNode node : relationNode) {
            double probability = Math.pow(node.pheromone, pheromoneWeight) *
                    Math.pow(node.tempHeuristic, heuristicWeight) /
                    pheromoneHeuristicSum;
            node.setProbability(probability);
        }
    }

    private AntNode selectNodeByProbability(Ant ant, Relation relation) {
        ArrayList<AntNode> relationNode = relationNodes.get(relation);

        Random random = new Random(System.currentTimeMillis());
        double probability = random.nextDouble();
        double cumulativeProbability = 0.0;

        for (AntNode n : relationNode) {
            cumulativeProbability += n.probability;
            if (cumulativeProbability >= probability)
                return n;
        }
        return null;
    }

    private void clearAntsNodes() {
        for (Ant ant: ants)
            ant.clearNodes();
    }
}
