public class Function {

    String in
    Node tree;
    HashMap<String, Double> argList;

    public Function(String in, Node func, HashMap<String, Double> argList) {
        this.in = in;
        this.func = func;
        this.argList = argList;
    }

    public double value(double... point) throws Exception {
        if (point.length != argList.size()) throw new Exception("ERROR: INVALID_NUMBER_OF_ARGUMENTS");
        if (argList.size() == 1) {
            if (argList.containsKey("x")) argList.replace("x", point[0]);   
            else if (argList.containsKey("y")) argList.replace("y", point[0]);
        }
        else if (argList.size() == 2) {
            argList.replace("x", point[0]); 
            argList.replace("y", point[1]);
        }
        return tree.eval(argList);
    }

    public boolean isContinuous(double p, String var) {
        if (argList.size() == 1) {
            double v = 0;
            try { 
                v = this.value(p);
                for (double d = p - 5 * CONTINUITY_INCREMENT; d < p; d += CONTINUITY_INCREMENT) {
                    try { 
                        argList.replace(var, d);
                        double fy = tree.eval(argList); 
                        //System.out.println("x="+x+", fy="+fy+", v="+v);
                        if ( Math.abs(v - fy) > MARGIN_OF_ERROR ) return false;
                    }
                    catch (Exception e) { e.printStackTrace(); }
                }
                for (double d = p + 5 * CONTINUITY_INCREMENT; d > p; d -= CONTINUITY_INCREMENT) {
                    try { 
                        argList.replace(var, d);
                        double fy = tree.eval(argList); 
                        if ( Math.abs(v - fy) > MARGIN_OF_ERROR ) return false;
                    }
                    catch (Exception e) { e.printStackTrace(); }
                }
            }
            catch (Exception e) { if ( e.getMessage().equals("division by 0") ) return false; }
            return true;
        }
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
        Tokenizer T = new Tokenizer(args[0]);
        Parser P = new Parser(T);
        Node func = P.root;
        Node deriv = P.root.deriv("x");
        HashMap<String, Double> argList = P.argList;
        double x0 = Double.parseDouble(args[1]);
        double threshold = Double.parseDouble(args[2]);
        System.out.println("newton(func, deriv, "+x0+", "+threshold+") = "+
                            Curve.newton(func, deriv, "x", argList, x0, threshold));
        
    }
}

//f(x, y) = 0;
class Curve extends AlgebraicObject {

    String in;
    Function func;
    HashMap<String, Double> argList
    Function pderivx;
    Function pderivy;
    
    public Curve(String in, Function func, HashMap<String, Double> argList) {
        this.in = in;
        this.func = func;
        this.argList = argList;
        Node pderivx = func.pderiv("x");
        Node pderivy = func.pderiv("y");
        this.pderivx = new Function(pderivx.toString(), pderivx, argList);   
        this.pderivy = new Function(pderivy.toString(), pderivy, argList);   
    }

    public ArrayList<Point> findSolutions(double start, double end, String var, double val, double threshold) {
        double increment = Math.abs(end - start) / 1000;
        double x0 = start;
        double x1 = start;
        for (int k = 1; k <= 1000; k++) {
            x0 = x1;
            x1 = start + k * increment; 
            if (x0 * x1 < 0) { 
                coords.add( (x0 + x1)/2 );
            }

        }
    }

}


/*
    ArrayList<Point> points = new ArrayList<Point>();
    Vector<Double> startingpoints = new ArrayList<Double>();
    argList.replace(var, val);
    int n = 1000;
    double d1 = start;
    double d2 = start;
    double range = Math.abs(end - start);
    for (int i=1; i<=1000; i++) {
        d1 = d2;
        d2 = start + i * range / n;
        if (curve.eval(d1, argList) * 
            curve.eval(d2, argList) < 0) startingpoints.add((d1+d2)/2); 
    }
    for (double d : startingpoints) {
        argList.replace(var, d);
        double coord = 0;
        if (var.equals("x")) {
            argList.replace("x", d);
            coord = newton(curve, pderivx, y, d, argList, threshold);
        }
        else if (var.equals("y")) {
            argList.replace("y", d);
            coord = newton(curve, pderivx, x, d, argList, threshold);
        }
        double coord = newton(curve, d, 1e-8);
    }
*/
