//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package tmp;

import java.util.HashSet;
import java.util.Iterator;
import kodkod.ast.Relation;
import kodkod.instance.Bounds;

public class Choromosome {
    public Boolean isSatisfiable = false;
    public int totalConstraintsNumber = 0;
    public int failedConstraintsNumber;
    public int failedFieldNumber;
    public int tupleSize;
    public int failedRelationNumber;
    public double fitness;
    public int originalIndex;
    public HashSet<String> nullRelations = new HashSet();
    public int nullRelationsNumber = 0;
    public Bounds chromosomeBounds;
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public void getFitness() {
        if (this.fitness == -1.0D) {
            this.fitness = (double)(0.45F * (float)this.failedConstraintsNumber / (float)this.totalConstraintsNumber + 0.55F * (float)this.failedRelationNumber);
            if (this.fitness == 0.0D) {
                this.fitness = Main.minFitness;
            }

            if (this.fitness < Main.minFitness) {
                Main.minFitness = this.fitness;
            }
        }

    }

    public Choromosome() {
        this.originalIndex = -1;
        this.fitness = -1.0D;
        this.tupleSize = 0;
    }

    public Choromosome(int index) {
        this.originalIndex = index;
        this.fitness = -1.0D;
        this.tupleSize = 0;
    }

    public String toString() {
        return this.fitness + "," + this.failedConstraintsNumber + "," + this.totalConstraintsNumber + "," + this.failedRelationNumber + "," + Main.relations.size() + "," + (this.isSatisfiable ? "Valid " : "Invalid ");
    }

    public void printBounds() {
        Iterator var1 = this.chromosomeBounds.relations().iterator();

        while(var1.hasNext()) {
            Relation r = (Relation)var1.next();
            System.out.println(r.toString() +
                    " | " +
                    this.chromosomeBounds.upperBound(r).size() +
                    ":" +
                    this.chromosomeBounds.upperBound(r));
        }

    }

    public String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();

        while(count-- != 0) {
            int character = (int)(Math.random() * (double)"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".length());
            builder.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".charAt(character));
        }

        return builder.toString();
    }
}
