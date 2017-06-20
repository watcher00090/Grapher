import java.util.HashMap;
import java.util.ArrayList;
import java.awt.Point;

public class NonCompiledFunction extends Function {

    Node tree;
    HashMap<String, Double> argList;

    public NonCompiledFunction(Node tree, HashMap<String, Double> argList) {
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

    public boolean isBivariate() {
        if (argList.containsKey("y")) return true;
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

}
