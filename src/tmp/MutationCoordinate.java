//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package tmp;

public class MutationCoordinate {
    int x;
    int y;

    public MutationCoordinate() {
    }

    public String toString() {
        return this.y + " | " + this.x;
    }

    public boolean equals(Object obj) {
        MutationCoordinate other = (MutationCoordinate)obj;
        return this.x == other.x || this.y == other.y;
    }
}
