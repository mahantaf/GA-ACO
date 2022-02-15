package tmp;

import java.util.ArrayList;

public class Ant {

    public ArrayList<AntNode> nodes = new ArrayList<>();
    public double fitness;
    public int failedConstraintsNumber;
    public int totalConstraintsNumber;
    public int failedRelationNumber;


    public Ant(Ant ant) {
        this.nodes = new ArrayList<>(ant.nodes);
        this.fitness = ant.fitness;
        this.failedConstraintsNumber = ant.failedConstraintsNumber;
        this.failedRelationNumber = ant.failedRelationNumber;
        this.totalConstraintsNumber = ant.totalConstraintsNumber;
    }

    public int getDistance() {
        return this.failedRelationNumber + this.failedConstraintsNumber;
    }

    public Ant() {

        // TODO: Change these initial numbers to more logical ones

        this.failedConstraintsNumber = 1000;
        this.failedRelationNumber = 1000;
        this.fitness = 1000000.0D;
    }

    public double getFitness() {
        if (this.fitness == -1.0D) {
            this.fitness = (double)(0.45F * (float)this.failedConstraintsNumber / (float)this.totalConstraintsNumber + 0.55F * (float)this.failedRelationNumber);
            if (this.fitness == 0.0D) {
                this.fitness = Main.minFitness;
            }

            if (this.fitness < Main.minFitness) {
                Main.minFitness = this.fitness;
            }
        }
        return this.fitness;
    }

    public void setNode(AntNode node, int index) {
        nodes.set(index, node);
    }

    public AntNode getNode(int index) {
        return nodes.get(index);
    }

    public void addNode(AntNode node) {
        nodes.add(node);
    }

    public void clearNodes() {
        nodes.clear();
    }

    public ArrayList<AntNode> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        StringBuilder printed = new StringBuilder("Ant: ").append(fitness).append("\n");
        printed.append("Number of failed constraints: ").append(failedConstraintsNumber).append("\n");
        printed.append("Number of failed relations: ").append(failedRelationNumber).append("\n");
        // TODO: Uncomment following lines later
        for (AntNode node : nodes)
            printed.append(node.relation).append(" : ").append(node.tuples.toString()).append("\n");
        return printed.toString();
    }
}
