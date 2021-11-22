import static edu.mit.csail.sdg.alloy4.A4Reporter.NOP;
import static edu.mit.csail.sdg.alloy4compiler.ast.Sig.UNIV;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.Attr;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Decl;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprConstant;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;
import tmp.tmpClass;

public class Main {
    public static void main(String[] args) throws Err {
        ArrayList<tmpClass> a = new ArrayList<tmpClass>();
        ArrayList<tmpClass> b = new ArrayList<tmpClass>();

        a.add(new tmpClass(1));
        a.add(new tmpClass(2));
        a.add(new tmpClass(3));

        b.add(a.get(0));
        b.add(a.get(2));

        for (tmpClass t : a) {
            System.out.println(t.a);
        }
        System.out.println("=================");
        for (tmpClass t : b) {
            System.out.println(t.a);
        }

        b.get(0).a = 4;
        System.out.println();

        for (tmpClass t : a) {
            System.out.println(t.a);
        }
        System.out.println("=================");
        for (tmpClass t : b) {
            System.out.println(t.a);
        }

    }
}
