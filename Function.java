import java.util.HashMap;
import java.util.ArrayList;
import java.awt.Point;

public class Function {

    Node tree;
    HashMap<String, Double> argList;
    static double CONTINUITY_INCREMENT = .00000001;
    static double CONTINUITY_MARGIN_OF_ERROR = .1;

    public Function(Node tree, HashMap<String, Double> argList) {
        this.tree = tree;
        this.argList = argList;
    }

    public void print() {
        tree.print();
    }

    public double value(double... point) throws Exception {
        if (point.length == 1) {
            argList.replace("x", point[0]);   
        }
        else if (point.length == 2) {
            argList.replace("x", point[0]); 
            argList.replace("y", point[1]);
        }
        else if (point.length > 2) throw new Exception("TOO_MANY_ARGUMENTS");
        return tree.eval(argList);
    }
    
    public void printArgList() {
        for (String var : argList.keySet()) {
            System.out.println("(" + var + ", " + argList.get(var).doubleValue() + ")");
        }
    }

    public void updateParam() {
        argList.replace("param", argList.get("param") + 1);  
    }

    public boolean isContinuous(double p) throws Exception {
        double v = 0;
        try { 
            v = value(p);
            for (double l = p - 5 * CONTINUITY_INCREMENT; l < p; l += CONTINUITY_INCREMENT) {
                double fy = value(l); 
                //System.out.println("x="+x+", fy="+fy+", v="+v);
                if ( Math.abs(v - fy) > CONTINUITY_MARGIN_OF_ERROR ) return false;
            }
            for (double r = p + 5 * CONTINUITY_INCREMENT; r > p; r -= CONTINUITY_INCREMENT) {
                double fy = value(r); 
                if ( Math.abs(v - fy) > CONTINUITY_MARGIN_OF_ERROR ) return false;
            }
        }
        catch (Exception e) { 
            System.out.println("Exception at p = " + p);
            e.printStackTrace();
            if (e.getMessage().equals("division by 0")) return false; 
        }
        return true;
    }

    public boolean isBivariate() {
        if (argList.containsKey("x") && argList.containsKey("y")) return true;
        return false;
    }

    public static double newton(Node func, Node deriv, String var, 
                                HashMap<String, Double> argList, double x0, double threshold) {
        try {
            while ( Math.abs(func.eval(argList)) > threshold && deriv.eval(argList) > threshold) { 
                argList.replace(var, x0);
                x0 = x0 - func.eval(argList) / deriv.eval(argList);
            }
            return x0;
        }
        catch(Exception e) { 
            System.out.println(e.toString()+"at x0="+x0);
            return x0; 
        }
    }

    public static void testNewton(String[] args) {
        Parser P = new Parser(args[0]);
        Node func = P.root;
        try { 
            Node deriv = P.root.pderiv("x");
            HashMap<String, Double> argList = P.argList;
            double x0 = Double.parseDouble(args[1]);
            double threshold = Double.parseDouble(args[2]);
            System.out.println("newton(func, deriv, "+x0+", "+threshold+") = "+
                                newton(func, deriv, "x", argList, x0, threshold));
        }
        catch (Exception e) { 
            e.printStackTrace();
        }
    }

    public static void testFunction(String[] args) {
        Parser P = new Parser(args[0]);
        Function func = new Function(P.root, P.argList);
        System.out.println();
        func.print();
        System.out.println();
        System.out.println("Bi-Variate: " + func.isBivariate());
        System.out.println();
        if (func.isBivariate()) {
            try { 
                double x = Double.parseDouble(args[1]);
                double y = Double.parseDouble(args[2]);
                System.out.println("f(" + x + "," + y + ") = " + 
                    func.value(x, y)
                                  ); 
            }
            catch (Exception e) { e.printStackTrace(); }
        }
        else { 
            try { 
                System.out.println("f(" + args[1] + ") = " + func.value(Double.parseDouble(args[1])));    
            }
            catch (Exception e) { e.printStackTrace(); }
        }
        System.out.println();
    }

    public static void testIsContinuous(String[] args) {
        Parser P = new Parser(args[0]);
        Function func = new Function(P.root, P.argList);
        func.print();
        Double val = Double.parseDouble(args[1]);
        try { 
            System.out.println("isContinuous(" + val +
                               ") = " + func.isContinuous(val)
                              ); 
        }
        catch (Exception e) { e.printStackTrace(); } 
    }

    public static void main(String[] args) {
        testFunction(args);
        //testIsContinuous(args);
        //testNewton(args);
    }
    
}

//f(x, y) = 0;
class ZeroLevelSet {

    Function func;
    HashMap<String, Double> argList;
    
    public ZeroLevelSet(Function func, HashMap<String, Double> argList) {
        this.func = func;
        this.argList = argList;
    }

    public double funcValue(double... point) throws Exception {
        return func.value(point); 
    }

}
