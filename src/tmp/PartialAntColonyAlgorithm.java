package tmp;

import kodkod.ast.Relation;
import kodkod.instance.Bounds;
import kodkod.instance.TupleSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class PartialAntColonyAlgorithm extends AntColonyAlgorithm {

    public int expirationTimeSlots = 3;
    public int relationTuplesWindowSize = 30;


    public PartialAntColonyAlgorithm() {
        super();
    }

    public Ant startOptimization() {
        sortRelationsByNodeSize();
        int iter = 0;
        while (iter < numberOfIterations && !Main.foundSolution) {
            long startIteration = System.currentTimeMillis();
            System.out.println("------------------------- Iteration " + iter + " -------------------------");
            printGraphSize();
            initializeAntsByLowerBounds();
//            moveAntsMultiThread();
            moveAnts();
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
                relationNodes.get(relation).add(new SubAntNode(relation, chromosomeTuples, defaultPheromone));
        }
    }

    public static void selectNode(Ant ant, Relation relation, int relationIndex) {
        SubAntNode selectedNode;
        if (relationNodes.get(relation).size() == 1) {
            selectedNode = (SubAntNode) relationNodes.get(relation).get(0);
        } else {
//            setAntNodesProbability(ant, relation, relationIndex);
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
