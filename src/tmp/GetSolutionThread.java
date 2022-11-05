//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package tmp;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import kodkod.ast.Formula;

import java.io.IOException;
import java.util.ArrayList;

public class GetSolutionThread implements Runnable {
    private A4Solution solution;
    private Command cmd;
    private A4Reporter rep;
    Formula fgoal;
    Choromosome generatedChromo;
    int taskIndex;
    ArrayList<Choromosome> generatedPopulationByGA;
    ArrayList<Formula> formulas;
    ArrayList<A4Solution> solutionArrayList;

    public GetSolutionThread(A4Solution solution, Command command, A4Reporter reporter, int index, ArrayList<Choromosome> generatedPopulation, ArrayList<Formula> formulas) {
        this.solution = solution;
        this.solutionArrayList = this.solutionArrayList;
        this.cmd = command;
        this.rep = reporter;
        this.taskIndex = index;
        this.generatedPopulationByGA = generatedPopulation;
        this.formulas = formulas;
    }

    public GetSolutionThread(A4Solution solution, Command command, A4Reporter reporter, Formula formula, Choromosome chromo, int index) {
        this.solution = solution;
        this.cmd = command;
        this.rep = reporter;
        this.fgoal = formula;
        this.generatedChromo = chromo;
        this.taskIndex = index;
    }

    public void run() {
        try {
            long startT = System.currentTimeMillis();
            int solNumber = 0;
            A4Solution sol = this.solution.solve(this.rep, this.cmd, this.fgoal, this.generatedChromo);
            this.generatedChromo.getFitness();
            if (sol.satisfiable()) {
//                System.out.println("--" + sol + " --" + this.generatedChromo.originalIndex);
//                System.out.print(".\n");
//                this.generatedChromo.printBounds();
                ++solNumber;
                sol.writeXML(Main.output + ".xml");
                System.out.println("Solutions:" + solNumber);
            }

            if (solNumber > 0) {
//                System.out.println("Solved : " + this.generatedChromo.originalIndex);
                Main.foundSolution = true;
            }
            Main.originPopulation.set(this.taskIndex, this.generatedChromo);
            long time1 = System.currentTimeMillis() - startT;
            Main.latch.countDown();
        } catch (Err | IOException var7) {
            var7.printStackTrace();
        }
    }
}
