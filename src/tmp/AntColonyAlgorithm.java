package tmp;

import kodkod.ast.Relation;
import kodkod.instance.Bounds;
import kodkod.instance.TupleSet;
import java.util.*;

public class AntColonyAlgorithm {

    public double pheromoneWeight = 1;
    public double heuristicWeight = 0;
    public int numberOfAnts = 32;
    public double minPheromone = 1;
    public double maxPheromone = 50;
    public double defaultPheromone = 5;
    public double remainderPheromone = 10;
    public double evaporationRate = 0.8;
    public int numberOfIterations = 30;
    public int bestAntIndex = 0;
    public Ant bestAnt;
    public ArrayList<Ant> ants = new ArrayList<>();
    public Map<Relation, ArrayList<AntNode>> relationNodes = new HashMap<>();

    public ArrayList<Relation> unimportantRelations = new ArrayList<>();

    public AntColonyAlgorithm() {
        for (int i = 0; i < this.numberOfAnts; ++i)
            this.ants.add(new Ant());
        this.bestAnt = null;
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

    public Ant startOptimization() {
        sortRelationsByNodeSize();
        int iter = 0;
        while (iter < numberOfIterations && !Main.foundSolution) {
            System.out.println("Iteration: " + iter);
            clearAntsNodes();
            calculateProbabilities();
            moveAnts();
            evaluateAnts();
            updatePheromones();
            updateBestAnt();
            System.out.println(bestAnt);
            ++iter;
        }
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
        for (Ant ant: ants) {
            for (HashMap.Entry<Relation, ArrayList<AntNode>> entry : relationNodes.entrySet()) {
                Relation relation = entry.getKey();
                selectNode(ant, relation);
            }
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

    private void selectNode(Ant ant, Relation relation) {
        AntNode selectedNode = selectNodeByProbability(ant, relation);
        if (selectedNode == null)
            selectedNode = selectNodeRandomly(relation);
        ant.addNode(selectedNode);
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
