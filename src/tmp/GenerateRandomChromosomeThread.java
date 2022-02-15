package tmp;

import kodkod.ast.Relation;
import kodkod.instance.Bounds;
import kodkod.instance.TupleFactory;
import kodkod.instance.TupleSet;

import java.util.*;

public class GenerateRandomChromosomeThread implements Runnable {

    Choromosome generatedChromo;
    int taskIndex;
    
    GenerateRandomChromosomeThread(Choromosome generatedChromo, int taskIndex) {
        this.generatedChromo = generatedChromo;
        this.taskIndex = taskIndex;
    }
    
    @Override
    public void run() {
        Bounds adjustedBounds = null;
        adjustedBounds = new Bounds(Main.unsolvedSolution.bounds.universe());
        Map<Relation, TupleSet> lowerBounds = new LinkedHashMap<>();
        Map<Relation, TupleSet> upperBounds = new LinkedHashMap<>();
        int relationIndex = 0;

        Random lengthGenerator = new Random(System.currentTimeMillis());
        Random rand = new Random(System.currentTimeMillis());

        for(Iterator<Relation> var36 = Main.unsolvedSolution.bounds.relations().iterator(); var36.hasNext(); ++relationIndex) {
            Relation r = (Relation)var36.next();
            TupleSet tupleSetForLowerBounds = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
            Object[] upperBoundsArray = Main.unsolvedSolution.bounds.upperBound(r).toArray();

            TupleSet emptyTupleSet = new TupleSet(Main.unsolvedSolution.bounds.universe(), r.arity());
            boolean isUnimportantRelation = r.toString().equals("String") || r.toString().equals("seq/Int") || r.toString().equals("Int/next");

            if (isUnimportantRelation || Main.unsolvedSolution.bounds.lowers.get(r) == Main.unsolvedSolution.bounds.uppers.get(r)) {
                lowerBounds.put(r, Main.unsolvedSolution.bounds.lowers.get(r));
                upperBounds.put(r, Main.unsolvedSolution.bounds.uppers.get(r));
            } else {
                int tuplesNumber = lengthGenerator.nextInt(upperBoundsArray.length + 1);
                Set<Object> tupleIndexSet = new HashSet<>();
                if (tuplesNumber == 0) {
                    tupleSetForLowerBounds = emptyTupleSet;
                }
                while (tuplesNumber != 0) {
                    int nextTupleIndex;
                    do {
                        nextTupleIndex = rand.nextInt(upperBoundsArray.length);
                    } while(tupleIndexSet.contains(nextTupleIndex));

                    tupleIndexSet.add(nextTupleIndex);
                    --tuplesNumber;
                }

                Iterator<Object> itr = tupleIndexSet.iterator();

                while (itr.hasNext()) {
                    TupleFactory.IntTuple tInst = (TupleFactory.IntTuple)upperBoundsArray[(Integer)itr.next()];
                    tupleSetForLowerBounds.add(tInst);
                }

                lowerBounds.put(r, tupleSetForLowerBounds);
                upperBounds.put(r, tupleSetForLowerBounds);
            }

            adjustedBounds.uppers = upperBounds;
            adjustedBounds.lowers = lowerBounds;
            generatedChromo.chromosomeBounds = adjustedBounds;
        }

        Main.originPopulation.set(this.taskIndex, this.generatedChromo);
        Main.latch.countDown();
    }
}
