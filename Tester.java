public class Tester { 

    public static void testPderiv(String[] args) {
        
    }

    public static void testToString(String[] args) {
        Parser P = new Parser(args[0]);
        Node tree = P.root;
        System.out.println(tree.toString());
    }

    public static void testPrint(String[] args) {
        Parser P = new Parser(args[0]);
        Node tree = P.root;
        tree.print();
    }

    public static void testCodeGen(String[] args) {
        Parser P = new Parser(args[0]);

        System.out.println();        
        P.root.print();
        System.out.println();        

        Compiler.compileFunction(P);

        try {
            Class c = Class.forName(Compiler.getClassName(P));
            Function tmpfunc = (Function) c.newInstance(); 
            if (args.length == 3) {
                System.out.println("func.value(" + args[1] + ", " + args[2] + ") = " + 
                                    tmpfunc.value(Double.parseDouble(args[1]), 
                                                  Double.parseDouble(args[2])));
            }
            else { 
                System.out.println("func.value(" + args[1] + ") = " + tmpfunc.value(Double.parseDouble(args[1])));
            }
        } catch (ClassNotFoundException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        } catch (InstantiationException e4) {
            e4.printStackTrace();
        } catch (SecurityException e5) {
            e5.printStackTrace();
        } catch (Exception e6) {
            e6.printStackTrace();
        } 

    }

    public static void testEquals(String[] args) {
        Parser P0 = new Parser(args[0]);
        Parser P1 = new Parser(args[1]);
        P0.root.print();
        System.out.println();
        P1.root.print();
        System.out.println();
        System.out.println(P0.root.equals(P1.root));
        System.out.println(P1.root.equals(P0.root));
    }

    public static void testHashCode(String[] args) {
        Parser P0 = new Parser(args[0]);
        Parser P1 = new Parser(args[1]);
        System.out.println(P0.root.toString());
        System.out.println(P1.root.toString());
        System.out.println();        
        System.out.println(P0.root.toString().hashCode());
        System.out.println(P1.root.toString().hashCode());
    }

    public static void main(String[] args) { 
        //testTokenizer(args);
        //testOpCompareTo(args);
        //testToString(args);
        //testPrint(args);
        //testDeriv(args);
        //testNewton(args);
        testCodeGen(args);
        //testEquals(args);
        //testHashCode(args);
    }

}
