import java.util.Vector;

public class CompilerContext {

    int numSums;
    int numProds;
    int numRexes;

    Vector<String> namev;
    Vector<Node> nodev;

    public CompilerContext() {
        namev = new Vector<String>();
        nodev = new Vector<Node>();
        numSums = 0;
        numProds = 0;
        numRexes = 0;
    }

    public void add(Node n) {
        if (n instanceof SumNode) { 
System.out.println("COMPILER_CONTEXT: ADDING A SUMNODE...");
            numSums++;
            namev.add("sum_"+numSums);
            nodev.add(n);
        }
        if (n instanceof ProdNode) {
System.out.println("COMPILER_CONTEXT: ADDING A PRODNODE...");
            numProds++;
            namev.add("prod_"+numProds);
            nodev.add(n);
        }
        if (n instanceof RexNode) {
System.out.println("COMPILER_CONTEXT: ADDING A REXNODE...");
            numRexes++;
            namev.add("rex_"+numRexes);
            nodev.add(n);
        }
    }

    public void print() {
        for (int i = 0; i < namev.size(); i++) {
            System.out.println("(" + namev.get(i) + ", " + nodev.get(i).toString() + ")");
        }
    }
    
}

