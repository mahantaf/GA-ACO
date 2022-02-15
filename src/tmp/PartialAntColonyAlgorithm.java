package tmp;

import kodkod.ast.Relation;
import kodkod.engine.bool.Int;
import kodkod.instance.Bounds;
import kodkod.instance.TupleSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class PartialAntColonyAlgorithm extends AntColonyAlgorithm {

    public int expirationTimeSlots = 3;
    public int relationTuplesWindowSize = 50;
    public static int randomChromosomeNumber = 50;


    public PartialAntColonyAlgorithm() {
        super();
    }

    public void initializeBestAntByBestChromosome() {
        for (Relation relation : relationNodes.keySet()) {
            TupleSet chromosomeTuples = bestGASolution.chromosomeBounds.uppers.get(relation);
            SubAntNode node = (SubAntNode) findNodeByTupleSet(relation, chromosomeTuples);

            bestAnt.addNode(node);
        }
        bestAnt.fitness = bestGASolution.fitness;
        bestAnt.totalConstraintsNumber = bestGASolution.totalConstraintsNumber;
        bestAnt.failedConstraintsNumber = bestGASolution.failedConstraintsNumber;
        bestAnt.failedRelationNumber = bestGASolution.failedRelationNumber;
    }

    public Ant startOptimization() {
        sortRelationsByNodeSize();
        initializeBestAntByBestChromosome();
        int iter = 0;
        while (iter < numberOfIterations && !Main.foundSolution) {
//            sortRelationsByNodeSize();
            long startIteration = System.currentTimeMillis();
            System.out.println("------------------------- Iteration " + iter + " -------------------------");
            printGraphSize();
            initializeAntsByLowerBounds();
//            moveAntsMultiThread();
//            moveAnts();
            moveAntsByProbability();
            evaluateAnts();
            updatePheromones();
            updateBestAnt();
            updateAntNodesUsedTime();
            updateRelationNodes();
            System.out.println(bestAnt);
            ++iter;
            initialAnts = false;
            long endIteration = System.currentTimeMillis() - startIteration;
            System.out.println("End Iteration: " + endIteration + " ms");
        }
        antThreadPool.shutdown();
        try {
            antThreadPool.awaitTermination(9223372036854775807L, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}

        return bestAnt;
    }

    private void moveAntsByProbability() {
        System.out.println("Moving Ants by Probability Start");
        long cTime = System.currentTimeMillis();
        int antIndex = 0;
        for (Ant ant: ants) {
            long cTime2 = System.currentTimeMillis();
            ++antIndex;
            int relationIndex = 0;

            Random random = new Random(System.currentTimeMillis());
            int numOfBestChoice = random.nextInt(numOfRelations);

//            System.out.println("Ant " + antIndex + " is about to choose " + numOfBestChoice + " best choice.");

            ArrayList<Integer> relationChoice = new ArrayList<>();
            int numOfBestChoiceTemp = numOfBestChoice;
            while (numOfBestChoice > 0) {
                int chosenRelationIndex = random.nextInt(numOfRelations);
                if (!relationChoice.contains(chosenRelationIndex)) {
                    relationChoice.add(chosenRelationIndex);
                    numOfBestChoice--;
                }
            }
//            System.out.print("relations: ");
//            for (Integer rc : relationChoice) {
//                System.out.print(rc + " ");
//            }
//            System.out.println();

            for (HashMap.Entry<Relation, ArrayList<AntNode>> entry : relationNodes.entrySet()) {
//                if (numOfBestChoice > 0) {
                if (relationChoice.contains(relationIndex)) {
                    SubAntNode bestAntNode = (SubAntNode) bestAnt.getNode(relationIndex);
                    ant.setNode(bestAntNode, relationIndex);
                    relationIndex++;
                    continue;
                } else {
                    // TODO: Select by probability
                    Relation relation = entry.getKey();
                    selectNode(ant, relation, relationIndex);
                }
                ++relationIndex;
            }
            System.out.println("Ant with " + numOfBestChoiceTemp + "best choice " + antIndex + " moving time: " + (System.currentTimeMillis() - cTime2) + " ms");
            if (Main.foundSolution)
                break;
        }
        System.out.println("Moving Ants By Probability End: " + (System.currentTimeMillis() - cTime) + " ms");
    }

    public void initializeNodes(HashMap<Relation, ArrayList<TupleSet>> relationTuples) {
        for (HashMap.Entry<Relation, ArrayList<TupleSet>> entry: relationTuples.entrySet()) {
            Relation relation = entry.getKey();
            ArrayList<AntNode> nodes = new ArrayList<>();
            for (TupleSet tupleSet : entry.getValue())
                nodes.add(new SubAntNode(relation, tupleSet, defaultPheromone));
            relationNodes.put(relation, nodes);
        }
    }

    public void initializeAntsByLowerBounds() {
        System.out.println("Initialize Lower Bounds Start");
        long cTime = System.currentTimeMillis();
        Ant fullSolutionAnt = new Ant();
        for (Relation r : relationNodes.keySet()) {
            if (!Main.isUnimportant(r)) {
                TupleSet lowerTuples = notSolvedSolution.bounds.lowers.get(r);
                SubAntNode node = (SubAntNode) findNodeByTupleSet(r, lowerTuples);
                SubAntNode setNode;
                if (node == null) {
                    setNode = new SubAntNode(r, lowerTuples, defaultPheromone);
                    relationNodes.get(r).add(setNode);
                } else {
                    setNode = node;
                }
                fullSolutionAnt.addNode(setNode);
            }
        }
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

    public void updateNodesByChromosome(Bounds chromosomeBounds) {
        for (Relation relation : relationNodes.keySet()) {
            TupleSet chromosomeTuples = chromosomeBounds.uppers.get(relation);
            AntNode node = findNodeByTupleSet(relation, chromosomeTuples);

            if (node == null)
//                relationNodes.get(relation).add(new SubAntNode(relation, chromosomeTuples, defaultPheromone));
                relationNodes.get(relation).add(new SubAntNode(relation, chromosomeTuples, defaultPheromone));
        }
    }

    public static void selectNode(Ant ant, Relation relation, int relationIndex) {
        SubAntNode selectedNode;
        if (relationNodes.get(relation).size() == 1) {
            selectedNode = (SubAntNode) relationNodes.get(relation).get(0);
        } else {
            setAntNodeProbabilityMultiThread(ant, relation, relationIndex);
            selectedNode = (SubAntNode) selectNodeByProbability(ant, relation);
        }
        selectedNode.resetUnusedTime();
        ant.setNode(selectedNode, relationIndex);
    }

    protected void updateAntNodesUsedTime() {
        System.out.println("Update Ant Used Time");
        for (HashMap.Entry<Relation, ArrayList<AntNode>> entry: relationNodes.entrySet())
            for (SubAntNode node: (ArrayList<SubAntNode>) ((ArrayList<?>) entry.getValue()))
                node.incUnusedTime();
    }

    protected void updateRelationNodes() {
        System.out.println("Update Relations based on used time");
        for (HashMap.Entry<Relation, ArrayList<AntNode>> entry: relationNodes.entrySet()) {
            int relationNodeLength = entry.getValue().size();
            int deletedNodesCount = 0;
            if (relationNodeLength > relationTuplesWindowSize) {
                ArrayList<SubAntNode> nodes = (ArrayList<SubAntNode>) ((ArrayList<?>) entry.getValue());
                for (int i = 0; i < nodes.size(); ++i) {
                    if (nodes.get(i).getUnusedTime() >= expirationTimeSlots) {
                        nodes.remove(i);
                        deletedNodesCount++;
                        i--;
                    }
                }
            }
        }
    }
}
