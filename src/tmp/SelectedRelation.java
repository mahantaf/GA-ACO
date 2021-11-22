//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package tmp;

public class SelectedRelation {
    int tupleNumber;
    String relation;
    int indexNumber;

    public SelectedRelation(int number, String relation, int indexNumber) {
        this.relation = relation;
        this.tupleNumber = number;
        this.indexNumber = indexNumber;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof SelectedRelation)) {
            return false;
        } else if (obj == this) {
            return true;
        } else {
            boolean retValue = this.relation.equals(((SelectedRelation)obj).relation) && this.tupleNumber == ((SelectedRelation)obj).tupleNumber;
            return retValue;
        }
    }

    public int hashCode() {
        return this.indexNumber;
    }

    public String toString() {
        return this.relation + ":" + this.tupleNumber;
    }
}
