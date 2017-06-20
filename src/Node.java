import java.util.HashMap;
import java.util.Vector;
import java.lang.Integer;
import java.lang.Double;
import java.io.IOException;
import java.io.StringWriter;
import java.io.File;
import java.io.FileWriter;

public abstract class Node {

    Node left; 
    Node right;

    abstract double eval(HashMap<String, Double> argList) throws Exception; 
    abstract Node reduce() throws Exception; 
    abstract Node pderiv(String var) throws Exception;
    abstract void codeGen(StringWriter str, CompilerContext context);
    abstract void print(int depth);

    public boolean equals(Node node) {
        if (this.toString().hashCode() == node.toString().hashCode()) return true;
        return false;
    }

    public double eval() { return 0; }
    public Op getOp() { return Op.Undef; }
    public int getStart() { return Integer.MAX_VALUE; }
    public int getLimit() { return Integer.MIN_VALUE; }
    public String getName() { return null; }
    public Node getArgExpr() { return null; }
    public String getVar() { return null; }
    public Vector<Node> getTermv() { return null; }
    public Vector<Integer> getSignv() { return null; }

    // checks if current node has strucure: 
    //     c * var ^ pow, where c can be any NumNode and var ^ pow is an OpNode.
    //      
    // cases currently being recognized:
    // 
    //      *         *         *         *       ^        ^ 
    //     / \       / \       / \       / \     / \      / \
    //    ^   c     ^   c     c   ^     c   ^   var pow pow var
    //   / \       / \           / \       / \
    //  var pow  pow var       var pow   pow var
    //
    //      additional cases if (pow = 0)
    //          c
    //
    //      additional cases if (pow = 1)
    //           *       *       var      
    //          / \     / \
    //        var  c   c   var
    //
    public boolean isUnivariateMonomial(VarNode var, int pow) {
        //additional cases
        if (pow == 0) {
            if (this instanceof NumNode) return true;
        }
        if (pow == 1) {
            if (equals(var)) return true;
            if (getOp() == Op.Times && left != null && right != null) {
                if (left.equals(var) && right instanceof NumNode && right.eval() != 0) return true;       
                if (right.equals(var) && left instanceof NumNode && left.eval() != 0) return true;       
            }
        }
        //general case
        NumNode pownode = new NumNode(pow);
        if (getOp() == Op.Hat && left != null && right != null) {
            if (left.equals(var) && right.equals(pownode)) return true;
            if (left.equals(pownode) && right.equals(var)) return true;
        }
        if (getOp() == Op.Times && left != null && right != null) {
            if (left.getOp() == Op.Hat && right instanceof NumNode && right.eval() != 0 
                && left.left != null && left.right != null) {
                if (left.left.equals(var) && left.right.equals(pownode)) return true;
                if (left.left.equals(pownode) && left.right.equals(var)) return true;
            }
            if (right.getOp() == Op.Hat && left instanceof NumNode && left.eval() != 0 
                && right.left != null && right.right != null) {
                if (right.left.equals(var) && right.right.equals(pownode)) return true;
                if (right.left.equals(pownode) && right.right.equals(var)) return true;
            }
        }
        return false;
    }

    // Returns the univariate monomial's coefficient
    //
    // Univariate monomial forms currently being recognized:
    // 
    //      *         *         *         *       ^        ^      c     var   *       *
    //     / \       / \       / \       / \     / \      / \                / \     / \ 
    //    ^   c     ^   c     c   ^     c   ^   var pow pow var            var  c   c  var 
    //   / \       / \           / \       / \
    //  var pow  pow var       var pow   pow var   
    //
    public static double getMonCoeff(Node mon) {
        if (mon instanceof NumNode) return mon.eval();
        if (mon instanceof VarNode) return 1;
        if (mon.getOp() == Op.Hat) return 1;
        if (mon.getOp() == Op.Times) {
            if (mon.left instanceof NumNode) return mon.left.eval();
            if (mon.right instanceof NumNode) return mon.right.eval();
        }
        return Double.MAX_VALUE;
    }   

    //  det  = 4*A^3 + 27*B^2
    //  the termlist must have either 3 or 4 terms!
    //
    //  if the termlist has 3 terms, it must have form: 
    //      y^2 - x^3 ± b*x = 0;  OR  
    //      y^2 - x^3 ± c = 0;    
    //
    //  if the termlist has 4 terms, it must have form:
    //      y^2 - x^3 ± b*x ± c = 0; OR
    public static boolean isEC(Node root) {
        try { 
            Vector<Node> termv = root.getTermv();
            Vector<Integer> signv = root.getSignv();
            int length = termv.size();
            if (length <= 2) return false;
            if (length > 4) return false;
            Node t1 = termv.get(0); 
            if (    !(t1.isUnivariateMonomial(new VarNode("y"), 2)) 
                 || getMonCoeff(t1) != 1
                 || signv.get(0).intValue() == -1) return false;
            Node t2 = termv.get(1); 
            if (    !(t2.isUnivariateMonomial(new VarNode("x"), 3)) 
                 || getMonCoeff(t2) != 1
                 || signv.get(1).intValue() == 1) return false;
            if (length == 3) {
                Node t3 = termv.get(2); 
                if (    !(t3.isUnivariateMonomial(new VarNode("x"), 1)) 
                     && !(t3.isUnivariateMonomial(new VarNode("x"), 0)) ) return false;
                double d = getMonCoeff(t3);
                if (d == 0) return false;
                if (Math.round(d) != d) return false;
            }
            if (length == 4) {
                Node t3 = termv.get(2); 
                if (!(t3.isUnivariateMonomial(new VarNode("x"), 1))) return false;
                Node t4 = termv.get(3); 
                if (!(t4.isUnivariateMonomial(new VarNode("x"), 0))) return false;
                double A = getMonCoeff(t3) * signv.get(2).intValue();
                double B = getMonCoeff(t4) * signv.get(3).intValue();
                if (Math.round(A) != A || Math.round(B) != B) return false; //not integer coefficients
                if (4*A*A*A + 27*B*B == 0) return false;
            }
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    // ret[0] = a
    // ret[1] = b
    public static long[] getECparams(Node ec) {
        if (!isEC(ec)) return null;
        Vector<Node> termv = ec.getTermv();
        Vector<Integer> signv = ec.getSignv();
        int length = termv.size();
        long[] params = new long[2];
        if (length == 3) {
            Node t3 = termv.get(2); 
            int sgn = signv.get(2).intValue();
            if (t3.isUnivariateMonomial(new VarNode("x"), 1)) {
                params[0] = Math.round(getMonCoeff(t3) * sgn * -1);
                params[1] = 0;
            }
            else {
                params[0] = 0;
                params[1] = Math.round(getMonCoeff(t3) * sgn * -1);
            }
        }
        if (length == 4) {
            Node t3 = termv.get(2); 
            Node t4 = termv.get(3); 
            params[0] = Math.round(getMonCoeff(t3) * signv.get(2).intValue() * -1);
            params[1] = Math.round(getMonCoeff(t4) * signv.get(3).intValue() * -1);
        }
        return params;
    }
    
    public void print() { 
        this.print(0);
        System.out.println();
    }

    public static void printSpaces(int depth) {
        for (int i=0; i<depth; i++) System.out.print(" ");
    }

}

class TermList extends Node {

    Vector<Integer> signv;
    Vector<Node> termv;

    public TermList() {
        left = null;
        right = null; 
        signv = new Vector<Integer>();
        termv = new Vector<Node>();
    }

    public TermList(Vector<Node> termv, Vector<Integer> signv) {
       this.termv = termv;
       this.signv = signv; 
    }

    public Vector<Node> getTermv() { return termv; }
    public Vector<Integer> getSignv() { return signv; }

    public void print(int depth) {
        Node.printSpaces(depth);
        System.out.println("termlist");
        for (int i=0; i<termv.size(); i++) {
            Node.printSpaces(depth+1);
            if (signv.get(i) == 1) System.out.print("+"); 
            else if (signv.get(i)== -1) System.out.print("-"); 
            System.out.println();
            termv.get(i).print(depth+2);
            if (i != termv.size() - 1) System.out.println();
        }
    }

    public void add(Node term, int sgn) {
        if (term instanceof TermList) { //merge nested termlists
            for (int i = 0; i < term.getTermv().size(); i++) {
                termv.add(term.getTermv().get(i));
                signv.add(term.getSignv().get(i) * sgn);
            }
        }
        else { 
            termv.add(term);
            signv.add(new Integer(sgn));
        }    
    }

    public double eval(HashMap<String, Double> argList) throws Exception {
        double result = 0.0;
        for (int i=0; i<termv.size(); i++) {
            double d = termv.get(i).eval(argList);
            if (signv.get(i).intValue() == -1) d *= -1;
            result += d;
        }
        return result;
    }

    public Node pderiv(String var) throws Exception { 
        TermList result = new TermList();
        if (termv.size() == 0) {
            result.add(new NumNode(1), 1);
            return result;
        }
        for (int i = 0; i<termv.size(); i++) {
            result.add(termv.get(i).pderiv(var), signv.get(i).intValue());
        }
        return result;
    }

    public void codeGen(StringWriter str, CompilerContext context) {
        for (int i = 0; i < termv.size(); i++)  {
            if (signv.get(i).intValue() == -1) str.write(" - (");
            else 
                if (i == 0) str.write("(");
                else str.write(" + (");
            termv.get(i).codeGen(str, context);
                str.write(")");
        } 
    }

    public String toString() {
        String result = "";
        boolean init = true;
        for (int i=0; i<termv.size(); i++)  {
            if (signv.get(i) == -1) { 
                result += "-(" + termv.get(i).toString() + ")"; 
            }    
            else { 
                if (!init) {
                    result += "+";
                }
                init = false;
                result += termv.get(i);
            }
        }
        return result;
    }

    public Node reduce() throws Exception {
        if (termv.size() == 0) throw new Exception("ERROR: INVALID_SYMANTICS");
        if (termv.size() == 1) { 
            if (signv.get(0) == -1) return (new OpNode(Op.Times, 
                                                       new NumNode(-1), 
                                                       termv.get(0))
                                           ).reduce(); 
            else return termv.get(0).reduce();
        } 
        TermList list = new TermList();
        double constsum = 0;
        for (int i=0; i<termv.size(); i++) {
            Node t = termv.get(i).reduce();
            if (t instanceof NumNode) constsum += t.eval() * signv.get(i);
            else list.add(t, signv.get(i));
        }
        if (constsum != 0) list.add(new NumNode(constsum), 1);
        else if (constsum == 0 && list.termv.size() == 0) 
            list.add(new NumNode(constsum), 1);
        if (list.termv.size() == 1) { 
            if (list.signv.get(0) == -1) 
                return (new OpNode(Op.Times, 
                                   new NumNode(-1), 
                                   list.termv.get(0))
                       ).reduce(); 
            else return list.termv.get(0).reduce();
        } 
        return list;
    }

}

class OpNode extends Node { 
    Op op;

    public OpNode(Op op, Node left, Node right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }

    public void print(int depth) {
        Node.printSpaces(depth);
        System.out.println(op.toString());
        left.print(depth+1);
        System.out.println();
        right.print(depth+1);
    }

    public String toString() { 
        return left.toString() + op.toString() + "(" + right.toString() + ")";
    }

    public void codeGen(StringWriter str, CompilerContext context) {
        if (op == Op.Hat) {
            if (   right instanceof NumNode 
                && right.eval() >= 0
                && Math.floor(right.eval()) == right.eval() ) {
 
                str.write(String.valueOf(1));
                for (int i = 1; i <= right.eval(); i++) {
                    str.write(" * (");
                    left.codeGen(str, context);
                    str.write(")");
                } 

            }
            else {
                str.write("Math.pow(");
                left.codeGen(str, context);
                str.write(", "); 
                right.codeGen(str, context);
                str.write(")"); 
            }
        }
        else { 
            str.write("("); 
            left.codeGen(str, context);
            str.write(") " + op.toString() + " (");
            right.codeGen(str, context);
            str.write(")");
        }
    }

    public double eval(HashMap<String, Double> argList) throws Exception {         
        switch(op) { 
            case Plus: return left.eval(argList) + right.eval(argList);
            case Minus: return left.eval(argList) - right.eval(argList);
            case Times: return left.eval(argList) * right.eval(argList);
            case Divide: {
                if (right.eval(argList) == 0) throw new Exception("division by 0");
                    return left.eval(argList) / right.eval(argList);
                }
            case Hat: return Math.pow(left.eval(argList), right.eval(argList));
            default: throw new Exception("invalid operator");
        }
    }

    public Node pderiv(String var) throws Exception {
        switch(op) { 
            case Plus: return (new OpNode(Op.Plus, left.pderiv(var), right.pderiv(var))).reduce();
            case Minus: return (new OpNode(Op.Minus, left.pderiv(var), right.pderiv(var))).reduce();
            case Times: { 
                return (new OpNode(Op.Plus,
                                   new OpNode(Op.Times, left.pderiv(var), right),
                                   new OpNode(Op.Times, left, right.pderiv(var))
                                  )
                       ).reduce();
            }
            case Divide: {
                Node numerator = (new OpNode(Op.Minus, 
                                               new OpNode(Op.Times, left.pderiv(var), right), 
                                               new OpNode(Op.Times, left, right.pderiv(var))
                                              )
                                   ).reduce();
                Node denominator = new OpNode(Op.Hat, right, new NumNode(2)); 
                return (new OpNode(Op.Divide, numerator, denominator)).reduce();
            }
            case Hat: { 
                if ( (left instanceof NumNode) && (right instanceof NumNode) ) return new NumNode(0);
                if (left instanceof NumNode) { // n ^ g(x)
                    return (new OpNode(Op.Times, 
                                       new OpNode(Op.Times, 
                                                  new NumNode(Math.log(left.eval())), 
                                                  right.pderiv(var)), 
                                       this
                                      )
                           ).reduce(); 
                }
                if (right instanceof NumNode) { //f(x) ^ n 
                    return (new OpNode(Op.Times, 
                                       new OpNode(Op.Times, right, left.pderiv(var)), 
                                       new OpNode(Op.Hat, left, new NumNode(right.eval()-1))
                                      )
                           ).reduce();  
                }
                else { //f(x) ^ g(x)
                    return (new OpNode(Op.Plus,
                                       new OpNode(Op.Times, 
                                                  new OpNode(Op.Times, left.pderiv(var), right), 
                                                  new OpNode(Op.Hat, 
                                                             left, 
                                                             new OpNode(Op.Minus, 
                                                                        right,
                                                                        new NumNode(1)
                                                                       ) 
                                                            )
                                                 ),
                                       new OpNode(Op.Times, 
                                                  new OpNode(Op.Times, 
                                                             new FuncNode(Func.Ln, left), 
                                                             right.pderiv(var)
                                                            ), 
                                                  this
                                                 )
                                     )
                           ).reduce();
                }
            }
            default: return null;
        }
    }

    public Op getOp() { return this.op; }

    public Node reduce() throws Exception {
        Node l = left.reduce();
        Node r = right.reduce();
        if (l instanceof NumNode && r instanceof NumNode) { 
            switch(op) { 
                case Plus: return new NumNode(l.eval() + r.eval());
                case Minus: return new NumNode(l.eval() - r.eval());
                case Times: return new NumNode(l.eval() * r.eval());
                case Divide: return new NumNode(l.eval() / r.eval());
                case Hat: return new NumNode(Math.pow(l.eval(), r.eval()));
                default: throw new Exception("invalid operator"); 
            }
        }
        else if (l instanceof NumNode && !(r instanceof NumNode)) {
            if (l.eval() == 0 && op == Op.Times && r instanceof VarNode) 
                return new NumNode(0);
            if (l.eval() == 0 && op == Op.Plus) return r;
            if (l.eval() == 1 && op == Op.Times) return r;
        }
        else if (!(l instanceof NumNode) && r instanceof NumNode) {
            if (l instanceof VarNode && op == Op.Times && r.eval() == 0) 
                return new NumNode(0); 
            if (op == Op.Plus && r.eval() == 0) return l;
            if (op == Op.Times && r.eval() == 1) return l;
            if (op == Op.Divide && r.eval() == 1) return l; if (op == Op.Hat && r.eval() == 1) return l;
        }
        return new OpNode(op, l, r); //irreducible 
    }

}

class VarNode extends Node { 

    String name;

    public VarNode(String name) { 
        this.name = name; 
        left = null;
        right = null; 
    }

    public void print(int depth) {
        Node.printSpaces(depth);
        System.out.print(name);
    }

    public String toString() { return name; }
    public String getName() { return name; }
    
    public void codeGen(StringWriter str, CompilerContext context) {
        str.write(name);
    }

    public double eval(HashMap<String, Double> argList) throws Exception {
        Double val = argList.get(name); 
        if (val == null) {  
            throw new Exception( "Variable " + name + " has no value mapping" );
        }
        return val.doubleValue();    
    }
    
    public Node pderiv(String var) throws Exception {
        if (name.equals(var)) return new NumNode(1);
        else return new NumNode(0);
    }
    
    public Node reduce() throws Exception {
        return this;
    }

}

class NumNode extends Node { 

    double val;

    public NumNode(double val) { 
        this.val = (double) val; 
        left = null;
        right = null;
    }

    public void print(int depth) { 
        Node.printSpaces(depth);
        System.out.print(val);    
    }

    public String toString() { return Double.toString(val); }

    public void codeGen(StringWriter str, CompilerContext context) {
        str.write(String.valueOf(val));
    }

    public double eval() { return val; }
    public double eval(HashMap<String, Double> argList) throws Exception { return val; }

    public Node pderiv(String var) throws Exception {
        return new NumNode(0);
    }

    public Node reduce() throws Exception {
        return this;
    }   

}

class FuncNode extends Node { 

    Func name;
    Node argExpr;

    public FuncNode(Func name, Node argExpr) { 
        this.name = name;
        this.argExpr = argExpr;
        left = null;
        right = null;
    }   

    public String getName() { return name.toString(); }
    public Node getArgExpr() { return argExpr; }

    public void print(int depth) {
        Node.printSpaces(depth);
        System.out.println(name.toString()+"[");
        argExpr.print(depth+1);
        System.out.println();
        Node.printSpaces(depth);
        System.out.print("]");
    }

    void printFuncSpaces() { 
        for (int i=0; i<name.toString().length(); i++) System.out.print(" ");
    }

    public void codeGen(StringWriter str, CompilerContext context) {
        if (name == Func.Factorial) 
            str.write(name.toString().toLowerCase() + "(");
        else if (name == Func.Ln)
            str.write("Math.log(");
        else if (name == Func.Log)
            str.write("Math.log10(");
        else if (name == Func.Arcsin)
            str.write("Math.asin(");
        else if (name == Func.Arccos)
            str.write("Math.acos(");
        else if (name == Func.Arctan)
            str.write("Math.atan(");
        else 
            str.write("Math." + name.toString().toLowerCase() + "(");
        argExpr.codeGen(str, context);
        str.write(")");
    }
        
    public double eval(HashMap<String, Double> argList) throws Exception {
        double d = argExpr.eval(argList);
        switch (name) { 
            case Sin: return Math.sin( d );
            case Cos: return Math.cos( d );
            case Tan: return Math.tan( d );
            case Exp: return Math.pow( Math.E, d );
            case Sqrt: return Math.sqrt( d );
            case Abs: return Math.abs(d) ;
            case Log: return Math.log10( d );            
            case Ln: return Math.log( d );
            case Arcsin: return Math.asin( d );
            case Arccos: return Math.acos( d );
            case Arctan: return Math.atan( d );
            case Factorial: 
                if ( d != Math.floor(d) ) 
                    throw new Exception("FACTORIAL ON A NON-NATURAL");
                if (d < 0) 
                    throw new Exception("FACTORIAL ON A NEGATIVE INTEGER");
                 double result = 1.0;
                 while (d != 0) {
                    result = result * d; 
                    d--;
                 }
                return result;
            default: throw new Exception( "UNRECOGNIZED_FUNCTION" ); 
        }
    }

    public double eval(double d) throws Exception {
        switch (name) { 
            case Sin: return Math.sin( d );
            case Cos: return Math.cos( d );
            case Tan: return Math.tan( d );
            case Exp: return Math.pow( Math.E, d );
            case Sqrt: return Math.sqrt( d );
            case Abs: return Math.abs(d) ;
            case Log: return Math.log10( d );            
            case Ln: return Math.log( d );
            case Arcsin: return Math.asin( d );
            case Arccos: return Math.acos( d );
            case Arctan: return Math.atan( d );
            case Factorial: 
                if ( d != Math.floor(d) ) 
                    throw new Exception("FACTORIAL ON A NON-NATURAL");
                if (d < 0) 
                    throw new Exception("FACTORIAL ON A NEGATIVE INTEGER");
                 double result = 1.0;
                 while (d != 0) {
                    result = result * d; 
                    d--;
                 }
                 return result;
            default: throw new Exception( "UNRECOGNIZED_FUNCTION" ); 
        }
    }

    public Node pderiv(String var) throws Exception {
        switch (name) {
            case Sin: return new OpNode(Op.Times, new FuncNode(Func.Cos, argExpr), argExpr.pderiv(var)); 
            case Cos: return new OpNode(Op.Times, 
                                        new OpNode(Op.Times, new NumNode(-1), argExpr.pderiv(var)),
                                        new FuncNode(Func.Sin, argExpr)
                                       ); 
            case Tan: return new OpNode(Op.Divide, argExpr.pderiv(var), 
                                        new OpNode(Op.Hat, new FuncNode(Func.Cos, argExpr), new NumNode(2))
                                       ); 
            case Exp: return new OpNode(Op.Times, this, argExpr.pderiv(var)); 
            case Sqrt: return new OpNode(Op.Divide,
                                         argExpr.pderiv(var), 
                                         new OpNode(Op.Times, 
                                                    new NumNode(2), 
                                                    new FuncNode(Func.Sqrt, argExpr)
                                                   )
                                        ); 
            case Ln: return new OpNode(Op.Divide,
                                       argExpr.pderiv(var),
                                       argExpr
                                      );
            case Abs: return null;    
            default: return null;
        }
    }

    public String toString() {
        if (name == Func.Factorial) { 
            return "[" + argExpr.toString() + "]!";
        }
        return name + "[" + argExpr.toString() + "]";
    }

    public Node reduce() throws Exception {
        Node ae = argExpr.reduce();
        if (ae instanceof NumNode) { 
            return new NumNode(eval(ae.eval()));
        } 
        return new FuncNode(name, ae);
    }    

}

class SumNode extends Node { 
    
    Node argExpr;
    String var;
    int start;    
    int limit;
    
    SumNode(Node argExpr, String var, int start, int limit) {
        this.argExpr = argExpr;
        this.var = var;
        this.start = start;
        this.limit = limit;
    }
    
    public String getVar() { return var; } 
    public int getStart() { return start; } 
    public int getLimit() { return limit; } 
    public Node getArgExpr() { return argExpr; }

    public void print(int depth) {
        Node.printSpaces(depth);
        System.out.println("sum(" + var + ", start = " + start + ", limit = " + limit + ")");
        argExpr.print(depth+1);
    }

    public String toString() {
        return "sum("+ argExpr.toString() + ", " + var + ", " + start + ", " + limit + ")"; 
    }

    public void codeGen(StringWriter str, CompilerContext context) {
        if (context.nodev.contains(this) == false) {
//            context.print();
 //           System.out.println(this.toString());
            context.add(this);
        }
        str.write(context.namev.get(context.nodev.indexOf(this)) + "()"); 
    }

    public double eval(HashMap<String, Double> argList) throws Exception {
        Double prev = argList.get(var);
        double accum = 0;
        for (int i=start; i<=limit; i++) {
            argList.replace(var, new Double(i));
            accum += argExpr.eval(argList);
        }
        argList.replace(var, prev);
        return accum;
    }

    public Node pderiv(String var) throws Exception { return null; }

    public Node reduce() throws Exception { return this; }

}

class ProdNode extends Node { 
    
    Node argExpr;
    String var;
    int start;    
    int limit;
    
    ProdNode(Node argExpr, String var, int start, int limit) {
        this.argExpr = argExpr;
        this.var = var;
        this.start = start;
        this.limit = limit;
    }

    public String getVar() { return var; } 
    public int getStart() { return start; } 
    public int getLimit() { return limit; } 
    public Node getArgExpr() { return argExpr; }
    
    public void print(int depth) {
        Node.printSpaces(depth);
        System.out.println("prod(" + var + ", start = " + start + ", limit = " + limit + ")");
        argExpr.print(depth+1);
    }

    public String toString() {
        return "prod("+ argExpr.toString() + ", " + var + ", " + start + ", " + limit + ")"; 
    }

    public void codeGen(StringWriter str, CompilerContext context) {
        if (!(context.nodev.contains(this))) context.add(this);
        str.write(context.namev.get(context.nodev.indexOf(this)) + "()"); 
    }

    public double eval(HashMap<String, Double> argList) throws Exception {
        double accum = 1;
        Double prev = argList.get(var);
        for (int i=start; i<=limit; i++) {
            argList.replace(var, new Double(i));
            accum *= argExpr.eval(argList);
        }
        argList.replace(var, prev);
        return accum;
    }

    public Node pderiv(String var) throws Exception { return null; }

    public Node reduce() throws Exception {
        return this;
    }

}

//Riemann Explicit Formula as a sum of cosines
class RexNode extends Node { 
    
    Node argExpr;
    int limit;
     
    public RexNode(Node argExpr, int limit) {
        this.argExpr = argExpr;
        this.limit = (limit > 300 ? 300 : limit);
        this.left = null;
        this.right = null;
    }

    public int getLimit() { return limit; } 
    public Node getArgExpr() { return argExpr; }

    public void print(int depth) {
        Node.printSpaces(depth); 
        System.out.println("rex(limit = " + limit + ")");
        argExpr.print(depth+1);
    }

    public String toString() {
        return "rex(" + argExpr.toString() + ", " + this.limit + ")";
    }

    public void codeGen(StringWriter str, CompilerContext context) {
        if (context.nodev.contains(this) == false) context.add(this);
        str.write(context.namev.get(context.nodev.indexOf(this)) + "()"); 
    }

    public Node pderiv(String var) throws Exception { return null; }

    public double eval(HashMap<String, Double> argList) throws Exception {
    // source : David Mumford's blog [http://www.dam.brown.edu/people/mumford/blog/2014/RiemannZeta.html]
        double x = argExpr.eval(argList);
        double result = 0.0;
        for (int i=0; i<limit; i++) {
            result += Math.cos( Math.log(x) * Function.zeta_zeros[i] );
        }
        return (1 - 2*result/Math.sqrt(x) - 1 / (x*x*x-x));
    }

    public Node reduce() throws Exception {
        return this;
    }

}

