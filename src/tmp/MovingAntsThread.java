package tmp;

import kodkod.ast.Relation;

import java.util.ArrayList;
import java.util.HashMap;

public class MovingAntsThread implements Runnable {

    public Ant ant;
    public int index;

    public MovingAntsThread(Ant ant, int index) {
        this.ant = ant;
        this.index = index;
    }

    @Override
    public void run() {
        long cTime2 = System.currentTimeMillis();
        int relationIndex = 0;
        System.out.println("Ant " + index + " is moving");
        for (HashMap.Entry<Relation, ArrayList<AntNode>> entry : AntColonyAlgorithm.relationNodes.entrySet()) {
            Relation relation = entry.getKey();
            PartialAntColonyAlgorithm.selectNode(ant, relation, relationIndex);
            ++relationIndex;
        }
        AntColonyAlgorithm.antLatch.countDown();
        System.out.println("Ant " + index + " moving time: " + (System.currentTimeMillis() - cTime2) + " ms");
    }
}
