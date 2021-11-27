package tmp;

import kodkod.instance.Tuple;
import kodkod.ast.Relation;
import kodkod.instance.TupleSet;

public class AntNode {

    public Relation relation;
    public TupleSet tuples;
    public int index;
    public double pheromone;
    public double probability = 0.0;
    public double tempHeuristic = 0.0;


    public AntNode(Relation relation, TupleSet tuples, double pheromone) {
        this.relation = relation;
        this.tuples = tuples;
        this.pheromone = pheromone;
    }

    public Relation getRelation() {
        return relation;
    }

    public TupleSet getTuples() {
        return tuples;
    }

    public int getIndex() {
        return index;
    }

    public double getPheromone() {
        return pheromone;
    }

    public void setPheromone(double pheromone) {
        this.pheromone = pheromone;
    }

    public void setTempHeuristic(double heuristic) {
        this.tempHeuristic = heuristic;
    }

    public void setProbability(double probability) { this.probability = probability; }

    public String toString() {
        return "Relation: " + relation + " | " + "Pheromone: " + pheromone + " | " + "Probability: " + probability + "\n" +
                "Tuples: \n" +
                tuples.toString() + "\n";
    }
}
