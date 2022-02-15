package tmp;

import kodkod.ast.Relation;
import kodkod.instance.TupleSet;

public class SubAntNode extends AntNode {

    public int unusedTime = 0;

    public SubAntNode(Relation relation, TupleSet tuples, double pheromone) {
        super(relation, tuples, pheromone);
    }

    public int getUnusedTime() {
        return unusedTime;
    }

    public void resetUnusedTime() {
        this.unusedTime = 0;
    }

    public void incUnusedTime() {
        this.unusedTime++;
    }
}
