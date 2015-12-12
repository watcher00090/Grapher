import java.util.HashMap;
import java.util.Vector;
import java.util.Set;
import java.lang.Integer;

enum State { 
    INIT, WORD, INTEGER, DOUBLE, DONE;
}

//Token Types. UNDEF = pushback token when nothing's been pushed back, token returned when state is unknown.
enum Tok { 
    LPAR, RPAR, PLUS, MINUS, TIMES, DIVIDE, HAT, NUMBER, VARIABLE, 
    FUNCTION, SUMMATION, PRODUCT, REX, 
    EQUALS, COMMA, EOS, UNDEF, INVALID, 
    E, C, G, g, PI;
}

enum Func { 
    Sin, Cos, Tan, Exp, Abs, Sqrt, Log, Ln, Undef, Factorial, Arcsin, Arccos, Arctan;    
}

enum Op { 
    Plus, Minus, Times, Divide, Hat, Undef;

    public String toString() {
        switch(this) {
        case Plus: return "+"; 
        case Minus: return "-";
        case Times: return "*";
        case Divide: return "/";
        case Hat: return "^";
        default: return "INVALID_OPERATOR";
        }
    }

    /*
     * 5  4  3  2  1
     * ^  /  *  -  +
     * 
     * returns: 
     *     -1 if leftOp < rightOp 
     *     0 if leftOp = rightOp
     *     1 if leftOp > rightOp
     */
    public int precedence(Op op) {
        switch (op) {
            case Hat: return 3;
            case Times: return 2;
            case Divide: return 2;
            case Plus: return 1;
            case Minus: return 1;
            default: return 0;
        }    
    }

}

/*
 * 
 * If someone else depends on seeing a character, but you've already used it up, you've GOT TO PUSH IT BACK!!!
 * 
 * Distinguishes between functions
 * 1. Built-in
 * 2. Sum or Product(evaluated function, different from the built-in functions). 
 */

class Tokenizer { 

    String in;
    char[] chars; 
    int i;
    double numVal; //start as an int, changes to double if a period is found.
    String strVal; //stores function or variable names
    State state;        
    Tok backToken;

    Tokenizer(String in) { 
        this.in  = in;
        numVal = 0.0;
        strVal = "";
        i = 0;
        state = State.INIT;
        backToken = Tok.UNDEF;
        chars = new char[in.length()+1];
        for (int k=0; k<in.length(); k++){
            chars[k]=in.charAt(k);    
        }
        chars[in.length()] = 0; //end with the null byte character, so the Tokenizer knows when the String is done.
    }

    boolean isOp(char c) { 
        if (c == '+') return true;
        if (c == '-') return true;
        if (c == '*') return true;
        if (c == '/') return true;
        if (c == '^') return true;
        return false;
    }

    Tok getOp(char c) { 
        if (c == '+') return Tok.PLUS;
        if (c == '-') return Tok.MINUS;
        if (c == '*') return Tok.TIMES;
        if (c == '/') return Tok.DIVIDE;
        if (c == '^') return Tok.HAT;
        return Tok.UNDEF;
    }

    static Op getOp(String s) { 
        if (s.equals("+")) return Op.Plus;
        if (s.equals("-")) return Op.Minus;
        if (s.equals("*")) return Op.Times;
        if (s.equals("/")) return Op.Divide;
        if (s.equals("^")) return Op.Hat;
        return Op.Undef;
    }

    public boolean isFunc(String s) {
        if (s.equals("sin")) return true;
        if (s.equals("cos")) return true;
        if (s.equals("exp")) return true;
        if (s.equals("tan")) return true;
        if (s.equals("sqrt")) return true;
        if (s.equals("log")) return true; 
        if (s.equals("ln")) return true;
        if (s.equals("abs")) return true;
        if (s.equals("factorial")) return true;
        if (s.equals("arcsin")) return true;
        if (s.equals("arccos")) return true;
        if (s.equals("arctan")) return true;
        return false;
    }    

    Func getFunc(String s) throws Exception { 
        if (s.equals("sin")) return Func.Sin;
        if (s.equals("cos")) return Func.Cos;
        if (s.equals("tan")) return Func.Tan;
        if (s.equals("exp")) return Func.Exp;
        if (s.equals("sqrt")) return Func.Sqrt;
        if (s.equals("abs")) return Func.Abs;
        if (s.equals("log")) return Func.Log;
        if (s.equals("ln")) return Func.Ln;
        if (s.equals("factorial")) return Func.Factorial;
        if (s.equals("arcsin")) return Func.Arcsin;
        if (s.equals("arccos")) return Func.Arccos;
        if (s.equals("arctan")) return Func.Arctan;
        throw new Exception("Function is undefined");
    }

    public Tok nextToken() { 
        Tok t = nextTokenContinued();
        //System.out.println(t);
        return t;
    }

    /**
     * Fewer States is desirable. Operators and punctuation are single
     * characters, so there isn't any reason to define separate states for each
     * one. Therefore, when such a character is encountered, the State switches
     * to State.INIT, and a token indicating corresponding to that character is
     * returned.
     * 
     * Every time a character is found that changes the state, i(the pointer
     * that stores where the tokenizer is in the String) is decremented so that
     * the next call to nextToken() so that the character that changed the
     * state isn't skipped over.
     * 
     * When the end of the String is reached, repeated calls to nextToken()
     * return Tok.EOState.
     * 
     * @return the next token in the input String.
     */
    public Tok nextTokenContinued() { 
        if (backToken !=Tok.UNDEF) {    
            Tok result = backToken;      
            backToken = Tok.UNDEF;    
            return result;             
        }

        numVal = 0.0;
        strVal = "";    
        int p = 0;    // stores double decimal point digits.
        state = State.INIT; // State is internal. So when nextToken is called,
                            // the code shouldn't know what state it's in. 

        while (true) { 

            char c = chars[i++]; 

            switch (state) { 
            case INIT: { //Initial scanner state 
                if (Character.isAlphabetic(c)) { 
                    state = State.WORD;
                    strVal+=c;    
                }
                else if (Character.isDigit(c)) { 
                    state = State.INTEGER;
                    numVal = 10*numVal + (c - '0') ; //add the digit to the number
                }
                else if (isOp(c)) return getOp(c);
                else if (c == '(') {
                    return Tok.LPAR;
                }
                else if (c == ')') return Tok.RPAR;
                else if (c == '.') state = State.DOUBLE;
                else if (c == ',') return Tok.COMMA;
                else if (c == '!') return Tok.FUNCTION;
                else if (c == '=') return Tok.EQUALS;
                else if (c == 0) { //DONE must know that we've reached the end.
                    i--;            //allow repeated calls to return Tok.EOS
                    state = State.DONE;    
                }
                else if (Character.isWhitespace(c)) continue; // ignore whitespace between
                                                              // operators or punctuation 
                else return Tok.INVALID;    
                break;
            }
            case INTEGER: { 
                if (Character.isAlphabetic(c)) { 
                    return Tok.INVALID;
                }
                else if (Character.isDigit(c)) { 
                    numVal = 10*numVal + (c -'0');    //add the digit to the number
                }
                else if (isOp(c) || c =='(' || c==')' || c==',' || Character.isWhitespace(c) || c==0) {
                    if (c==0) state = State.DONE;
                    i--; //the next call to nextToken() needs to know the character.
                    return Tok.NUMBER;
                }
                else if (c == '.') {
                    state = State.DOUBLE;
                }
                else {
                    return Tok.INVALID;
                }
                break;
            }
            case DOUBLE: {
                if (Character.isAlphabetic(c)) { 
                    return Tok.INVALID;
                }
                else if (Character.isDigit(c)){ 
                    p++;
                    numVal = numVal + (c -'0')/(Math.pow(10, p));
                }            
                else if (c == '(' || c ==')' || c==',' || Character.isWhitespace(c) || isOp(c) || c==0) {
                    if (c==0) state = State.DONE;
                    i--; 
                    return Tok.NUMBER;
                }
                else { 
                    return Tok.INVALID;
                }
                break;
            }
            case WORD: {
                if (Character.isAlphabetic(c) || Character.isDigit(c)) { 
                    strVal+=c;
                }
                else if (isOp(c) || c=='(' || c==')' || c==',' || Character.isWhitespace(c) || c==0) { 
                    i--;
                    if (c==0) state = State.DONE;
                    if (isFunc(strVal)) return Tok.FUNCTION;
                    if (strVal.equals( "sum" )) return Tok.SUMMATION;
                    if (strVal.equals( "prod" )) return Tok.PRODUCT;
                    if (strVal.equals( "rex" )) return Tok.REX;
                    if (strVal.equals( "e" )) return Tok.E;
                    if (strVal.equals( "pi")) return Tok.PI;
                    if (strVal.equals( "c")) return Tok.C;
                    if (strVal.equals( "G")) return Tok.G;
                    if (strVal.equals( "g")) return Tok.g;
                    return Tok.VARIABLE;
                }
                else {
                    return Tok.INVALID;
                }
                break;
            }
            case DONE: return Tok.EOS; 
            default: return Tok.UNDEF; //undefined state error.
            }
        }    
    }

}

/**
 * A node has either: 
 * 1. A left and a right child
 * 2. An argument Node but no left or right child (a function)
 * 3. No argument and no left or right child (a leaf node)
 * 
 * The method eval(argList) takes a HashMap that is passed in from outside
 * which maps variable names to values. This HashMap serves as the
 * evaluation context. This is much more efficient for evaluating
 * multivariable functions(such as the sums and products) than the
 * iterate-and-replace method, which requires creating, storing, and
 * looping through a new Node tree upon every iteration.
 * 
 */
abstract class Node {
    Node left; 
    Node right;
    static final int NUM_SPACES = 2;

    abstract double eval(HashMap<String, Double> argList) throws Exception; 
    abstract Node reduce() throws Exception; 
    abstract Node pderiv(String var) throws Exception;
    abstract void print(int depth);

    public double eval() { return 0; }
    public Op getOp() { return Op.Undef; }

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

    public void add(int sgn, Node term) {
        signv.add(new Integer(sgn));
        termv.add(term);
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
            result.add(1, new NumNode(1));
            return result;
        }
        for (int i = 0; i<termv.size(); i++) {
            result.add(signv.get(i).intValue(), termv.get(i).pderiv(var));
        }
        return result;
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
            if (signv.get(0) == -1) return (new OpNode(Op.Times, new NumNode(-1), termv.get(0))).reduce(); 
            else return termv.get(0).reduce();
        } 
        //double constsum = 0;
        System.out.println("returning this");
        return this;
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
            if (l.eval() == 1 && op == Op.Times) return r;
        }
        else if (!(l instanceof NumNode) && r instanceof NumNode) {
            if (op == Op.Times && r.eval() == 1) return l;
            else if (op == Op.Divide && r.eval() == 1) return l;
        }
        return new OpNode(op, l, r); //the OpNode is irreducible 
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
    
    public double eval(HashMap<String, Double> argList) throws Exception {
        Double val = argList.get(name); //gets the value corresponding to the variable.
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
        this.val = val; 
        left = null;
        right = null;
    }

    public void print(int depth) { 
        Node.printSpaces(depth);
        System.out.print(val);    
    }

    public String toString() { return Double.toString(val); }

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

    public FuncNode( Func name, Node argExpr ) { 
        this.name = name;
        this.argExpr = argExpr;
        left = null;
        right = null;
    }   

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
                                       new NumNode(1),
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
    /*    if (argExpr instanceof NumNode) { 
            return new NumNode(argExpr.eval(argExpr.val));
        } */
        return this;
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
    
    public void print(int depth) {
        Node.printSpaces(depth);
        System.out.println("sum(" + var + ", start = " + start + ", limit = " + limit + ")");
        argExpr.print(depth+1);
    }

    public String toString() {
        return "sum("+ argExpr.toString() + ", " + var + ", " + start + ", " + limit + ")"; 
    }

    public double eval(HashMap<String, Double> argList) throws Exception {
        Double prev = argList.get( var );
        double accum = 0;
        for (int i=start; i<=limit; i++) {
            argList.replace( var, new Double(i) );
            accum += argExpr.eval(argList);
            if (prev != null) argList.replace(var, prev );
        }
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
    
    ProdNode( Node argExpr, String var, int start, int limit ) {
        this.argExpr = argExpr;
        this.var = var;
        this.start = start;
        this.limit = limit;
    }
    
    public void print(int depth) {
        Node.printSpaces(depth);
        System.out.println("prod(" + var + ", start = " + start + ", limit = " + limit + ")");
        argExpr.print(depth+1);
    }

    public String toString() {
        return "prod("+ argExpr.toString() + ", " + var + ", " + start + ", " + limit + ")"; 
    }

    public double eval(HashMap<String, Double> argList) throws Exception {
        double accum = 1;
        Double prev = argList.get( var );
        for (int i=start; i<=limit; i++) {
            argList.replace( var, new Double(i) );
            accum *= argExpr.eval(argList);
            if (prev != null) argList.replace(var, prev);
        }
        return accum;
    }

    public Node pderiv(String var) throws Exception { return null; }

    public Node reduce() throws Exception {
        return this;
    }

    public int size() { return 1; }
}

//Riemann Explicit Formula as a sum of cosines
class RexNode extends Node { 
    
    Node argExpr;
    int limit;
     
     // source : Andrew Odlyzko [http://www.dtc.umn.edu/~Odlyzko/zeta_tables/index.html]
    public final double[] zeta_zeros = {
        14.134725142,  21.022039639,  25.010857580,  30.424876126,  32.935061588,  //  10 
        37.586178159,  40.918719012,  43.327073281,  48.005150881,  49.773832478,

        52.970321478,  56.446247697,  59.347044003,  60.831778525,  65.112544048,  //  20
        67.079810529,  69.546401711,  72.067157674,  75.704690699,  77.144840069,

        79.337375020,  82.910380854,  84.735492981,  87.425274613,  88.809111208,  //  30
        92.491899271,  94.651344041,  95.870634228,  98.831194218, 101.317851006,

        103.725538040, 105.446623052, 107.168611184, 111.029535543, 111.874659177,  //  40
        114.320220915, 116.226680321, 118.790782866, 121.370125002, 122.946829294,

        124.256818554, 127.516683880, 129.578704200, 131.087688531, 133.497737203,  //  50
        134.756509753, 138.116042055, 139.736208952, 141.123707404, 143.111845808,

        146.000982487, 147.422765343, 150.053520421, 150.925257612, 153.024693811,  //  60
        156.112909294, 157.597591818, 158.849988171, 161.188964138, 163.030709687,

        165.537069188, 167.184439978, 169.094515416, 169.911976479, 173.411536520,  //  70
        174.754191523, 176.441434298, 178.377407776, 179.916484020, 182.207078484,

        184.874467848, 185.598783678, 187.228922584, 189.416158656, 192.026656361,  //  80
        193.079726604, 195.265396680, 196.876481841, 198.015309676, 201.264751944,

        202.493594514, 204.189671803, 205.394697202, 207.906258888, 209.576509717,  //  90
        211.690862595, 213.347919360, 214.547044783, 216.169538508, 219.067596349,

        220.714918839, 221.430705555, 224.007000255, 224.983324670, 227.421444280,  // 100
        229.337413306, 231.250188700, 231.987235253, 233.693404179, 236.524229666,

        237.769820481, 239.555477573, 241.049157796, 242.823271934, 244.070898497,  // 110
        247.136990075, 248.101990060, 249.573689645, 251.014947795, 253.069986748,

        255.306256455, 256.380713694, 258.610439492, 259.874406990, 260.805084505,  // 120
        263.573893905, 265.557851839, 266.614973782, 267.921915083, 269.970449024,

        271.494055642, 273.459609188, 275.587492649, 276.452049503, 278.250743530,  // 130
        279.229250928, 282.465114765, 283.211185733, 284.835963981, 286.667445363,

        287.911920501, 289.579854929, 291.846291329, 293.558434139, 294.965369619,  // 140
        295.573254879, 297.979277062, 299.840326054, 301.649325462, 302.696749590,

        304.864371341, 305.728912602, 307.219496128, 310.109463147, 311.165141530,  // 150
        312.427801181, 313.985285731, 315.475616089, 317.734805942, 318.853104256,

        321.160134309, 322.144558672, 323.466969558, 324.862866052, 327.443901262,  // 160
        329.033071680, 329.953239728, 331.474467583, 333.645378525, 334.211354833,

        336.841850428, 338.339992851, 339.858216725, 341.042261111, 342.054877510,  // 170
        344.661702940, 346.347870566, 347.272677584, 349.316260871, 350.408419349,

        351.878649025, 353.488900489, 356.017574977, 357.151302252, 357.952685102,  // 180
        359.743754953, 361.289361696, 363.331330579, 364.736024114, 366.212710288,

        367.993575482, 368.968438096, 370.050919212, 373.061928372, 373.864873911,  // 190
        375.825912767, 376.324092231, 378.436680250, 379.872975347, 381.484468617,

        383.443529450, 384.956116815, 385.861300846, 387.222890222, 388.846128354,  // 200
        391.456083564, 392.245083340, 393.427743844, 395.582870011, 396.381854223,

        397.918736210, 399.985119876, 401.839228601, 402.861917764, 404.236441800,  // 210
        405.134387460, 407.581460387, 408.947245502, 410.513869193, 411.972267804,

        413.262736070, 415.018809755, 415.455214996, 418.387705790, 419.861364818,  // 220
        420.643827625, 422.076710059, 423.716579627, 425.069882494, 427.208825084,

        428.127914077, 430.328745431, 431.301306931, 432.138641735, 433.889218481,  // 230
        436.161006433, 437.581698168, 438.621738656, 439.918442214, 441.683199201,

        442.904546303, 444.319336278, 446.860622696, 447.441704194, 449.148545685,  // 240
        450.126945780, 451.403308445, 453.986737807, 454.974683769, 456.328426689,

        457.903893064, 459.513415281, 460.087944422, 462.065367275, 464.057286911,  // 250
        465.671539211, 466.570286931, 467.439046210, 469.536004559, 470.773655478,

        472.799174662, 473.835232345, 475.600339369, 476.769015237, 478.075263767,  // 260
        478.942181535, 481.830339376, 482.834782791, 483.851427212, 485.539148129,

        486.528718262, 488.380567090, 489.661761578, 491.398821594, 493.314441582,  // 270
        493.957997805, 495.358828822, 496.429696216, 498.580782430, 500.309084942,

        501.604446965, 502.276270327, 504.499773313, 505.415231742, 506.464152710,  // 280
        508.800700336, 510.264227944, 511.562289700, 512.623144531, 513.668985555,

        515.435057167, 517.589668572, 518.234223148, 520.106310412, 521.525193449,  // 290
        522.456696178, 523.960530892, 525.077385687, 527.903641601, 528.406213852,

        529.806226319, 530.866917884, 532.688183028, 533.779630754, 535.664314076,  // 300
        537.069759083, 538.428526176, 540.213166376, 540.631390247, 541.847437121

    };
    
    public RexNode(Node argExpr, int limit) {
        this.argExpr = argExpr;
        this.limit = (limit > 300 ? 300 : limit);
        this.left = null;
        this.right = null;
    }

    public void print(int depth) {
        Node.printSpaces(depth); 
        System.out.println("rex(limit = " + limit);
        argExpr.print(depth+1);
    }

    public String toString(String var) {
        return "rex(" + argExpr.toString() + ", " + limit + ")";
    }

    public Node pderiv(String var) throws Exception { return null; }

    public double eval(HashMap<String, Double> argList) throws Exception {
    // source : David Mumford's blog [http://www.dam.brown.edu/people/mumford/blog/2014/RiemannZeta.html]
        double x = argExpr.eval( argList);
        double result = 0.0;
        for (int i=0; i<limit; i++) {
            result += Math.cos( Math.log(x) * zeta_zeros[i] );
        }
        return (1 - 2*result/Math.sqrt(x) - 1 / (x*x*x-x));
    }

    public Node reduce() throws Exception {
        return this;
    }

    public int size() { return 1; }
}

/*
The Grammar

Expr ::= 
         Term ± Term ± ...
     |   -Term ± Term ± ...
    
Term ::= 
        '-' Power
     |  Power
     |  Power * Term
     |  Power / Term

Power ::= 
        '-' Factor
     |  Factor
     |  Factor ^ Power

Factor ::= 
        Number
     |  Variable
     |  '(' Expr ')'
     |  Function '(' Expr ')'
     |  Summation '(' Expr ',' Variable ',' Number ',' Number ')'
     |  Product '(' Expr ',' Variable ',' Number ',' Number ')'
     |  Rex '(' Number ')'

Number ::= 
        [0-9]\+
     |  [0-9]\+ '.' [0-9]\+

Function ::= 
        'sin'
     |  'cos' 
     |  'tan' 
     |  'exp' 
     |  'abs' 
     |  'sqrt' 
     |  'log' 
     |  'ln' 
     |  'factorial' 
     |  'arcsin' 
     |  'arccos' 
     |  'arctan' 

Variable ::=
        [a-z]\+  (excluding pi,g,c,e,G)
    
*/

public class Parser { 

    String in;
    Tokenizer str;
    Node root;
    HashMap<String, Double> argList;

    public Parser(String in) { 
        this.in = in;
        str = new Tokenizer(in);
        argList = new HashMap<String, Double>();
        try { 
            root = expr();
        }
        catch(Exception e) { 
            e.printStackTrace();
        }
    }
    
    public void pushBack(Tok token) {
        System.out.println("            PUSHBACK_REQUEST: " + token);
        str.backToken = token;
    }

    public void pushBack(String s, Tok token) {
        System.out.println("            PUSHBACK_REQUEST: " + s + ", " +  token);
        str.backToken = token;
    }
   
    /* 
    public Node equ() throws Exception {
    System.out.println("equ -->");
        Node lhs = expr(); 
        Tok t = str.nextToken();    
        if (t == Tok.EQUALS) {
        Node rhs = expr(); 
            Tok t1 = str.nextToken();
            if (t1 == Tok.EQUALS) {
                throw new Exception("INVALID_EQUATION_SYNTAX");
            }
            pushBack("equ", t1);
            return Node.reduceOpNode(new OpNode(Op.Minus, lhs, rhs));
        }
        else {
            pushBack("equ", t);
            return lhs; 
        }
    }
    */

    public Node expr() throws Exception { 
    System.out.println("expr -->");
        TermList termlist = new TermList();
        int s = 1;
        boolean firstterm = true;
        while (true) { 
            Tok t = str.nextToken();
            switch (t) {
                case EOS: 
                    return termlist.reduce();
                case INVALID: 
                    System.out.println("expr: invalid token");
                    throw new Exception("INVALID_TOKEN");
                case PLUS: s=1; break;
                case MINUS: s=-1; break;
                default: 
                    pushBack("e", t);
                    s = +1;
                    break;
            }
            Node term = term();
            if (term != null) {
                termlist.add(s, term);
            }
            else break; 
        }
        return termlist.reduce();
    }

     // Consumes everything up to a times or divide, as according to the grammar.
    public Node term() throws Exception { 
System.out.println("  term -->");
        Node left; 
        Tok t = str.nextToken(); 
        if (t == Tok.MINUS) {
            left = new OpNode(Op.Times, new NumNode(-1), power()); 
        }
        else {
            pushBack("t", t);
            left = power();
        }
        Tok token = str.nextToken();
        if (token == Tok.TIMES) return (new OpNode(Op.Times, left, term())).reduce();
        else if (token == Tok.DIVIDE) return (new OpNode(Op.Divide, left, term())).reduce();
        else {
            pushBack("t", token); 
            if (left != null) return left.reduce();
            return left;
        }
    }

    public Node power() throws Exception { 
System.out.println("    power-->");
        Node left; 
        Tok t = str.nextToken(); 
        if (t == Tok.MINUS) {
            left = new OpNode(Op.Times, new NumNode(-1), factor()); 
        }
        else {
            pushBack("p", t);
            left = factor();
        }
        Tok token = str.nextToken();
        if (token == Tok.HAT) return (new OpNode(Op.Hat, left, power())).reduce();
        else {
            pushBack("p", token); 
            if (left != null) return left.reduce();
            return left;
        }
    }
    
    public Node factor() throws Exception { 
System.out.println("      factor -->");
        Tok token = str.nextToken();
        if (token == Tok.VARIABLE) {
            argList.put(str.strVal, null);
            return (new VarNode(str.strVal)).reduce(); //single variables and constants are irreducible 
        }
        if (token == Tok.NUMBER) return new NumNode(str.numVal);    
        if (token == Tok.E) return new NumNode(Math.E);
        if (token == Tok.PI) return new NumNode(Math.PI);
        if (token == Tok.G) return new NumNode(6.67408E-11);
        if (token == Tok.g) return new NumNode(9.8);
        if (token == Tok.FUNCTION) { 
            String funcName = str.strVal;
            if (str.nextToken() != Tok.LPAR)
                throw new Exception( "INVALID_FUNCTION_SYNTAX: expecting left paren");
            Node argExpr = expr();    //consumes everything up to the right-parenthesis, which was pushed back in expr().
            if (str.nextToken() != Tok.RPAR) 
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting right paren");
            return (new FuncNode(str.getFunc(funcName), argExpr)).reduce();
        }
        if (token == Tok.SUMMATION) { // expect:  '(' Expr ',' Var ',' Number ',' Number ')' 
            
            if (str.nextToken() != Tok.LPAR)
                throw new Exception( "INVALID_FUNCTION_SYNTAX: expecting left paren");
            
            Node argExpr = expr();
            if (str.nextToken() != Tok.COMMA) 
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting comma");
            
            Node var = factor(); //variable expected
            if (!(var instanceof VarNode)) 
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting var name");
            
            if (str.nextToken() != Tok.COMMA) 
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting comma");
            
            Node start = factor();//number expected
            if (!(start instanceof NumNode))
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting start value, a number");
            
            if (str.nextToken() != Tok.COMMA) //number expected
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting comma");
            
            Node limit = factor();
            if (!(limit instanceof NumNode))
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting limit value, a number");
            
            if (str.nextToken() != Tok.RPAR) 
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting right paren");
            
            return (new SumNode(argExpr, var.toString(), (int) start.eval(), (int) limit.eval())).reduce();
        }
        if (token == Tok.PRODUCT) {
            // expect:  '(' Expr ',' Var ',' Number ',' Number ')' 
            
            if (str.nextToken() != Tok.LPAR)
                throw new Exception( "INVALID_FUNCTION_SYNTAX: expecting left paren");
            
            Node argExpr = expr();
            
            if (str.nextToken() != Tok.COMMA) 
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting comma");
            
            Node var = factor();
            if (!(var instanceof VarNode))
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting var name");
            
            if (str.nextToken() != Tok.COMMA) 
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting comma");
            
            Node start = factor();
            if (!(start instanceof NumNode))
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting start value, a number");
            
            if (str.nextToken() != Tok.COMMA) 
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting comma");
            
            Node limit = factor();
            if (!(limit instanceof NumNode))
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting limit value, a number");
            
            if (str.nextToken() != Tok.RPAR) 
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting right paren");
            
            return (new ProdNode(argExpr,var.toString(), (int) start.eval(), (int) limit.eval())).reduce();
        }
        if (token == Tok.REX) {
            //expect: '(' Number ')'

            if (str.nextToken() != Tok.LPAR) 
                throw new Exception( "INVALID_FUNCTION_SYNTAX: expecting left paren");

            Node argExpr = expr();
            if (str.nextToken() != Tok.COMMA) 
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting comma");

            Node limit = factor(); //expecting number
            if (!(limit instanceof NumNode))
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting limit value, a number");

            return new RexNode(argExpr, (int)limit.eval() );
        }
        if (token == Tok.LPAR) {    //Parenthesis without a function 
            Node interior = expr();     //Expr inside '(' and ') 
            System.out.println("got here");
            Tok extra = str.nextToken();
            if (extra == Tok.RPAR) return interior.reduce(); //consumes everything up to the next ')'.
            throw new Exception("UNBALANCED_PARENTHESIS"); //no rightparens makes input invalid.
        }
        pushBack("f", token);
        //nextToken() design saves need for pushing back EOS
        return null; 
    }    

    public static String generateFuzzTestString(int length) { 
        char[] chars = {
            'x', '1', '+', '-', '*', '/', '(', ')', '^'
        };    
        String str = "";    
        for (int i=0; i<length; i++) { 
            int pos = (int) (chars.length * Math.random());
            str += chars[pos];
        }
        return str;
    }

    public static void fuzzTestParser(String[] args) {
        if (args.length < 2) {
            System.out.println("ERROR: expecting 2 arguments");
            return;
        }            
        int num = Integer.parseInt(args[0]);
        int len = Integer.parseInt(args[1]); 
        for (int i=0; i<num; i++) { 
            String s = "";
            try { 
                s = generateFuzzTestString(len);
                System.out.println(s);
                Parser P = new Parser(s);
                System.out.println(P.root.toString()); 
            }       
            catch(Exception e) { 
                e.printStackTrace();
            }
        }
    }

    public static void testParser(String[] args) {
        Parser P = new Parser(args[0]);
        System.out.println(P.root.toString());
    }

    
    public static void testTokenizer(String[] args) {
        for (int i=0; i<args.length; i++) {
            Tokenizer T = new Tokenizer(args[i]); 
            while (true) {
                Tok t = T.nextToken(); 
                System.out.print(t);
                if (t == Tok.VARIABLE || t ==Tok.FUNCTION) System.out.print(", " + T.strVal);
                if (t == Tok.NUMBER) System.out.print(", " + T.numVal);
                if (t == Tok.EOS) { 
                    System.out.println();
                    break; 
                }
                System.out.println();
            }
        }
    }


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

    public static void main(String[] args) { 
        // testTokenizer(args);
        //testFunction(args);
        //testOpCompareTo(args);
        //testToString(args);
        testPrint(args);
        //testDeriv(args);
        //testNewton(args);
    }

}
