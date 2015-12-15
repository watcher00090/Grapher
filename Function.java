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
        if (point.length != argList.size() && argList.size() != 0) {
            throw new Exception("ERROR: INVALID_NUMBER_OF_ARGUMENTS");
        }
        if (argList.size() == 1) {
            if (argList.containsKey("x")) argList.replace("x", point[0]);   
            else if (argList.containsKey("y")) argList.replace("y", point[0]);
        }
        else if (argList.size() == 2) {
            argList.replace("x", point[0]); 
            argList.replace("y", point[1]);
        }
        else if (argList.size() > 2) throw new Exception("ERROR: ARGLIST_TOO_BIG");
        Node result = tree.eval(argList);
        if (result instanceof NumNode) return result.eval();
        throw new Exception("ERROR: TREE.EVAL()_RETURNED_EXPRESSION_BUT_VALUE_RETURNS_A_DOUBLE");
    }
    
    public void printArgList() {
        for (String var : argList.keySet()) {
            System.out.println("(" + var + ", " + argList.get(var).doubleValue() + ")");
        }
    }

    public boolean isContinuous(double p) throws Exception {
        if (argList.size() <= 1) {
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
        throw new Exception("ERROR: MULTIVARIATE CONTINUITY NOT YET IMPLEMENTED");
    }

    public boolean isMultivariable() {
        if (argList.size() > 1) return true;
        return false;
    }

    public boolean isFunctionOfX() {
        if (argList.size() == 0) return true; //treating constants as functions of x
        else if (argList.size() == 1 && argList.containsKey("x")) return true;
        return false;
    }

    public boolean isFunctionOfY() {
        if (argList.size() == 1 && argList.containsKey("y")) return true;
        return false;
    }

    public static double newton(Function func, Function deriv, 
                                double x0, double threshold) {
        try {
            while ( Math.abs(func.value(x0)) > threshold && deriv.value(x0) > threshold) { 
                x0 = x0 - func.value(x0) / deriv.value(x0);
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
        Function func = new Function(P.root, P.argList);
        try { 
            Function deriv = new Function(P.root.pderiv("x"), P.argList);
            double x0 = Double.parseDouble(args[1]);
            double threshold = Double.parseDouble(args[2]);
            System.out.println("newton(func, deriv, "+x0+", "+threshold+") = "+
                                newton(func, deriv, x0, threshold));
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
        System.out.println("Multivariable: " + func.isMultivariable());
        System.out.println();
        if (func.isMultivariable()) {
            try { 
                double x = Double.parseDouble(args[1]);
                double y = Double.parseDouble(args[2]);
                System.out.println("f(" + x + ", " + y + ") = " + 
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

    public double lhsvalue(double... point) throws Exception {
        return func.value(point); 
    }

    public ArrayList<Point> findPoints(double xmin, double xmax) {
        return null;     
    }

}
