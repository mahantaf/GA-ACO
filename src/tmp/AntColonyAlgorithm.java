package tmp;

import kodkod.ast.Relation;
import kodkod.instance.Bounds;
import kodkod.instance.TupleSet;
import java.util.*;

public class AntColonyAlgorithm {

    public double pheromoneWeight = 0.45;
    public double heuristicWeight = 0.55;
    public int randomSelectionRatio = 5; // percent
    public int numberOfAnts = 32;
    public double minPheromone = 5;
    public double maxPheromone = 50;
    public double defaultPheromone = 5;
    public double remainderPheromone = 5;
    public double evaporationRate = 0.8;
    public int numberOfIterations = 3;
    public int bestAntIndex = 0;
    public Ant bestAnt;
    public boolean initialAnts = true;
    public ArrayList<Ant> ants = new ArrayList<>();
    public Map<Relation, ArrayList<AntNode>> relationNodes = new LinkedHashMap<>();
    public ArrayList<Relation> unimportantRelations = new ArrayList<>();

    public int totalNumberOfChoice = 0;
    public int numberOfRandomChoice = 0;
    public int numberOfProbabilityChoice = 0;

    public AntColonyAlgorithm() {
        this.bestAnt = null;
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

    public void printAnts() {
        int index = 1;
        for (Ant ant : ants) {
//            System.out.println(index + ") " + ant);
            System.out.println(index + ") ");
            System.out.println("Fitness: " + ant.fitness);
            System.out.println("Failed Constraints: " + ant.failedConstraintsNumber);
            System.out.println("Failed Relations: " + ant.failedRelationNumber);
            System.out.println("=====================");
            ++index;
        }
    }

    public Ant startOptimization() {
        sortRelationsByNodeSize();
        int iter = 0;
        while (iter < numberOfIterations && !Main.foundSolution) {
//        while (iter < numberOfIterations) {
            System.out.println("------------------------- Iteration " + iter + " -------------------------");
//            clearAntsNodes();
//            calculateProbabilities();
            initializeAnts();
            moveAnts();
            evaluateAnts();
            updatePheromones();
            updateBestAnt();
//            printAnts();
            System.out.println(bestAnt);
            ++iter;
            initialAnts = false;
        }
        System.out.println("# Probability Choice: " + numberOfProbabilityChoice);
        System.out.println("# Random Choice: " + numberOfRandomChoice);
        System.out.println("# Total Choice: " + totalNumberOfChoice);

        return bestAnt;
    }

    public Ant getBestAnt() {
        return bestAnt;
    }

    /**
        Sort the list based on relations' number of nodes (Minimum-Domain-Heuristic)
     */
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

    private void calculateProbabilities() {
        for (HashMap.Entry<Relation, ArrayList<AntNode>> entry: relationNodes.entrySet()) {
            double relationNodesPheromoneHeuristicSum = getNodesPheromoneHeuristicSum(entry.getKey());
            for (AntNode node : entry.getValue())
                node.setProbability(getNodeSelectionProbability(node, relationNodesPheromoneHeuristicSum));
        }
    }

    private void moveAnts() {
        System.out.println("Moving Ants...");
        int antNumber = 1;
        for (Ant ant: ants) {
            int relationIndex = 0;
            for (HashMap.Entry<Relation, ArrayList<AntNode>> entry : relationNodes.entrySet()) {

                Relation relation = entry.getKey();
                selectNode(ant, relation, relationIndex);

                ++relationIndex;
            }
            ++antNumber;
        }
    }

    private void evaluateAnts() {
        System.out.println("Evaluate Ants...");
        Main.evaluateAntsSolutions(ants, unimportantRelations);

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

            Random random = new Random(System.currentTimeMillis());
            int randomProbability = random.nextInt(100);

            ArrayList<Integer> nonOptimalNodesIndex = getSpecificRelationNodesIndex(relation, false);

            if (randomProbability <= randomSelectionRatio && nonOptimalNodesIndex.size() > 0) {
                ++numberOfRandomChoice;
                selectedNode = selectNodeRandomly(relation, nonOptimalNodesIndex);
            } else {
                ++numberOfProbabilityChoice;
                setAntNodesProbability(ant, relation, relationIndex);
                selectedNode = selectNodeByProbability(ant, relation);
            }
        }
        ++totalNumberOfChoice;
        ant.setNode(selectedNode, relationIndex);
    }

    private void setAntNodesProbability(Ant ant, Relation relation, int relationIndex) {
        ArrayList<AntNode> relationNode = relationNodes.get(relation);

        // Step 1. Calculate pheromone-heuristic sum
        double pheromoneHeuristicSum = 0;
        for (AntNode node : relationNode) {
            if (node.pheromone > defaultPheromone) {
                Ant tempAnt = new Ant(ant);
                tempAnt.setNode(node, relationIndex);
                Main.evaluateAntPartialSolution(tempAnt, unimportantRelations);
                double nodeHeuristic = (double) 1 / (1 + tempAnt.getDistance());
                pheromoneHeuristicSum +=
                        Math.pow(node.pheromone, pheromoneWeight) *
                                Math.pow(nodeHeuristic, heuristicWeight);
                node.setTempHeuristic(nodeHeuristic);
            }
        }

        // Step 2. Calculate each relation node temp probability
        for (AntNode node : relationNode) {
            if (node.pheromone > defaultPheromone) {
                double probability = Math.pow(node.pheromone, pheromoneWeight) *
                        Math.pow(node.tempHeuristic, heuristicWeight) /
                        pheromoneHeuristicSum;
                node.setProbability(probability);
            }
        }
    }

    private double getNodesPheromoneHeuristicSum(Relation relation) {
        ArrayList<AntNode> relationNode = relationNodes.get(relation);
        double pheromoneHeuristicSum = 0;
        for (AntNode n : relationNode)
            pheromoneHeuristicSum +=
                    Math.pow(n.pheromone, pheromoneWeight) *
                            Math.pow(getNodeHeuristicProbability(n), heuristicWeight);
        return pheromoneHeuristicSum;
    }

    private double getNodeSelectionProbability(AntNode node, double pheromoneHeuristicSum) {
        return Math.pow(node.pheromone, pheromoneWeight) *
                Math.pow(getNodeHeuristicProbability(node), heuristicWeight) /
                pheromoneHeuristicSum;
    }

    private double getNodeHeuristicProbability(AntNode node) {

        // TODO: Decide about how to implement the heuristic

        return 1;
    }

    private ArrayList<Integer> getSpecificRelationNodesIndex(Relation relation, boolean optimal) {
        ArrayList<AntNode> relationNode = relationNodes.get(relation);
        ArrayList<Integer> optimalNodes = new ArrayList<>();

        int index = 0;
        for (AntNode node : relationNode) {
            if (optimal) {
                if (node.pheromone > defaultPheromone)
                    optimalNodes.add(index);
            } else {
                if (node.pheromone <= defaultPheromone)
                    optimalNodes.add(index);
            }
            ++index;
        }
        return optimalNodes;
    }

    private AntNode selectNodeRandomly(Relation relation, ArrayList<Integer> relationNodeIndex) {
        ArrayList<AntNode> relationNode = relationNodes.get(relation);

        Random random = new Random(System.currentTimeMillis());
        int index = random.nextInt(relationNodeIndex.size());

        return relationNode.get(relationNodeIndex.get(index));
    }

    private AntNode selectNodeRandomly(Relation relation) {
        ArrayList<AntNode> relationNode = relationNodes.get(relation);

        Random random = new Random(System.currentTimeMillis());
        int index = random.nextInt(relationNode.size());

        return relationNode.get(index);
    }

    private AntNode selectNodeByProbability(Ant ant, Relation relation) {
        ArrayList<AntNode> relationNode = relationNodes.get(relation);

        Random random = new Random(System.currentTimeMillis());
        double probability = random.nextDouble();
        double cumulativeProbability = 0.0;

        for (AntNode n : relationNode) {
            if (n.pheromone > defaultPheromone) {
                cumulativeProbability += n.probability;
                if (cumulativeProbability >= probability)
                    return n;
            }
        }
        return null;
    }

    private void clearAntsNodes() {
        for (Ant ant: ants)
            ant.clearNodes();
    }
}
