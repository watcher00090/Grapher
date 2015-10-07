import java.util.HashMap;
import java.lang.Integer;

enum State { 
	INIT, WORD, INTEGER, DOUBLE, DONE;
}

//Token Types. UNDEF = pushback token when nothing's been pushed back, token returned when state is unknown.
enum Tok { 
	LPAR, RPAR, PLUS, MINUS, TIMES, DIVIDE, HAT, NUMBER, VARIABLE, FUNCTION, SUMMATION, PRODUCT, COMMA, EOS, UNDEF, INVALID
}

enum Func { 
	Sin, Cos, Tan, Exp, Sqrt, Log, Ln, Undef, Factorial, Arcsin, Arccos, Arctan;	
}

enum Op { 
	Plus, Minus, Times, Divide, Hat;
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

	public boolean isFunc(String s) {
		if (s.equals("sin")) return true;
		if (s.equals("cos")) return true;
		if (s.equals("exp")) return true;
		if (s.equals("tan")) return true;
		if (s.equals("sqrt")) return true;
		if (s.equals("log")) return true; 
        if (s.equals("ln")) return true;
		if (s.equals("factorial")) return true;
		if (s.equals("arcsin")) return true;
        if (s.equals("arccos")) return true;
        if (s.equals("arctan")) return true;
        return false;
	}	

	/**
	 * 
	 * @param s, the input String
	 * @return the Function enum type of String s. If s isn't a function, throw an exception.
	 * @throws Exception
	 */
	Func getFunc(String s) throws Exception { 
		if (s.equals("sin")) return Func.Sin;
		if (s.equals("cos")) return Func.Cos;
		if (s.equals("tan")) return Func.Tan;
		if (s.equals("exp")) return Func.Exp;
		if (s.equals("sqrt")) return Func.Sqrt;
		if (s.equals("log")) return Func.Log;
        if (s.equals("ln")) return Func.Ln;
		if (s.equals("factorial")) return Func.Factorial;
        if (s.equals("arcsin")) return Func.Arcsin;
        if (s.equals("arccos")) return Func.Arccos;
        if (s.equals("arctan")) return Func.Arctan;
		else throw new Exception("Function is undefined");
	}

    public Tok nextToken() { 
        Tok t = nextTokenPrime();
        //System.out.println(t);
        return t;
    }

	 /**
	  * Fewer States is desirable. Operators and punctuation are single characters, so there isn't any reason 
	  * to define separate states for each one. Therefore, any other state encounters one, the State switches to State.INIT,
	  * where a token indicating corresponding to that character is returned. Every time a character is hit that changes
	  * the state, i is decremented so that the next call to nextToken() so that the character that changed the state
	  * isn't skipped over.
	  * 
	  * When the end of the String is reached, repeated calls to nextToken() return Tok.EOState. 
	  * 
	  * @return the next token in the input String.
	  */
	public Tok nextTokenPrime() { 

		if (backToken !=Tok.UNDEF) {	
			Tok result = backToken;  	
			backToken = Tok.UNDEF;	
			return result; 			
		}

		numVal = 0.0;
		strVal = "";	
		int p = 0;		//stores double decimal point digits.
		state = State.INIT; //State is internal. So when nextToken is called, the computer shouldn't know what state it's in. 

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
				else if (c == 0) { //DONE must know that we've reached the end.
					i--;			//allow repeated calls to return Tok.EOS
					state = State.DONE;	
				}
				else if (Character.isWhitespace(c)) continue; //whitespace between operators or punctuation is insignificant
				else return Tok.INVALID;	
				break;
			}
			case INTEGER: { 
				if (Character.isAlphabetic(c)) { 
//System.out.println("ERROR: S=NUMBER, c=letter");
					return Tok.INVALID;
				}
				else if (Character.isDigit(c)) { 
					numVal = 10*numVal + (c -'0'); //>0	//add digit to the number
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
//System.out.println("ERROR 003");	
				}
				else if (Character.isDigit(c)){ 
					p++;
					numVal = numVal + (c -'0')/(Math.pow(10, p)); //<0
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
					if (strVal.equals( "Sum" )) return Tok.SUMMATION;
					if (strVal.equals( "Prod" )) return Tok.PRODUCT;
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
 * The method eval() takes a HashMap that is passed in from outside that maps variable names to 
 * values. To evaluate the Node at a specific value, configure in the HashMap each of the values corresponding 
 * to the variables of that node, then pass the HashMap to eval. This is much more efficient 
 * for evaluating multivariable functions(such as the sums and products) than the iterate-and-replace method, which requires creating, 
 * storing, and looping through a new Node tree upon every iteration.
 * 
 */
abstract class Node {
	Node left; 
	Node right;
	
	abstract double eval( HashMap<String, Double> args ) throws Exception; 
	
	double eval() throws Exception {
		throw new Exception( "Not implemented" );
	}

    void print() {
        print(0);
        System.out.println();
    }
	
	void print(int depth) { 
		printSpaces(depth);
		System.out.println(this.toString());
		if (left !=null) left.print(depth+1);
		if (right !=null) right.print(depth+1);
    }

	protected static void printSpaces(int depth) {
		for (int i=0; i<2*depth; i++) { 
			System.out.print(" ");
		}
	}	
		
}

class OpNode extends Node { 
	Op op;

	public String toString() { return op.toString(); }
	
	public double eval( HashMap<String, Double> args ) throws Exception { 		
		switch(op) { 
			case Plus: return left.eval( args ) + right.eval( args );
			case Minus: return left.eval( args ) - right.eval( args );
			case Times: return left.eval( args ) * right.eval( args );
			case Divide: {
				if (left.eval( args ) == 0) throw new Exception("division by 0");
					return left.eval( args ) / right.eval( args );
				}
			case Hat: return Math.pow(left.eval( args ), right.eval( args ));
			default: throw new Exception("invalid operator");
		}
	}

	public OpNode(Op op, Node left, Node right) {
		this.op = op;
		this.left = left;
		this.right = right;
	}

}

class VarNode extends Node { 
	String name;

	public String toString() { return name; }
	
	public double eval( HashMap<String, Double> args ) throws Exception {
		Double dd = args.get( name ); //gets the value corresponding to the Node's variable.
		if (dd==null) { 
			throw new Exception( "VarNode has no value mapping" );
		}
        return dd.doubleValue();	
	}
	
	VarNode(String name) { 
		this.name = name; 
		left = null;
		right = null; 
	}

}

class NumNode extends Node { 
	double val;

	public String toString() { return Double.toString(val); }
	public double eval( HashMap<String, Double> args ) throws Exception { return val; }
	public double eval() throws Exception { return val; }

	NumNode(double val) { 
		this.val = val; 
		left = null;
		right = null;
	}
}

class FuncNode extends Node { 
	Func name;
	Node argExpr;

	void printFuncSpaces() { 
		for (int i=0; i<name.toString().length(); i++) System.out.print(" ");
	}
		
	void print(int d) { //overides print(d) in node.
		printSpaces(d);
		System.out.println(name + "[");
		argExpr.print(d+1);
		printFuncSpaces();
		printSpaces(d);
		System.out.println("]");
	}
		
	public double eval( HashMap<String, Double> args ) throws Exception {
		double d = argExpr.eval( args );
		switch (name) { 
		case Sin: return Math.sin( d );
		case Cos: return Math.cos( d );
		case Tan: return Math.tan( d );
		case Exp: return Math.pow( Math.E, d );
		case Sqrt: return Math.sqrt( d );
		case Log: return Math.log10( d );			
        case Ln: return Math.log( d );
		case Arcsin: return Math.asin( d );
        case Arccos: return Math.acos( d );
        case Arctan: return Math.atan( d );
        default: throw new Exception( "UNRECOGNIZED_FUNCTION" ); 
		}
	}

	public FuncNode( Func name, Node argExpr ) { 
		this.name = name;
		this.argExpr = argExpr;
		left = null;
		right = null;
	}	
}

class SumNode extends Node { 
	
	Node argExpr;
	String var;
	int start;	
	int limit;
	
	SumNode( Node argExpr, String var, int start, int limit ) {
		this.argExpr = argExpr;
		this.var = var;
		this.start = start;
		this.limit = limit;
	}
	
	public double eval( HashMap<String, Double> args ) throws Exception {
		double accum = 0;
		for (int i=start; i<=limit; i++) {
			if (null == args.replace( var, new Double(i) ))
				throw new Exception( "Summation parameter missing in arg list" );
			accum += argExpr.eval( args );
		}
		return accum;
    }
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
	
	public double eval( HashMap<String, Double> args ) throws Exception {
		double accum = 0;
		for (int i=start; i<=limit; i++) {
			if (null == args.replace( var, new Double(i) ))
				throw new Exception( "Summation parameter missing in arg list" );
			accum *= argExpr.eval( args );
		}
		return accum;
    }
}

/**
Expr :== '-' Term 
     :== Term
     :== Term +- Expr
    
Term :== '-' Factor
     :== Power
     :== Power /* Term

Power :== '-' Factor
      :== Factor
      :== Factor ^ Power

Factor :== Number
           Variable
           '(' Expr ')'
            Function '(' Expr ')'
            Summation '(' Expr ',' Variable ',' Number ',' Number ')'
            Product '(' Expr ',' Variable ',' Number ',' Number ')'
    
**/
class Parser { 

    Tokenizer str;
    Node root;
    static char[] chars = {
    'x', '1', '+', '-', '*', '/', '(', ')', '^'
    };    


    public Parser(Tokenizer str) { 
        this.str = str;
        try { 
            root = expr();
        }
        catch(Exception e) { 
            e.printStackTrace();
        }
    }
    
    public void pushBack(Tok token) {
     //   System.out.println("            PUSHBACK_REQUEST: " + token);
        str.backToken = token;
    }

    public void pushBack(String s, Tok token) {
    //    System.out.println("            PUSHBACK_REQUEST: " + s + ", " +  token);
        str.backToken = token;
    }
    
    public Node expr() throws Exception { 
//System.out.println("expr -->");
        Node left; 
        Tok t = str.nextToken(); 
        if (t == Tok.MINUS) {
            left = new OpNode(Op.Times, new NumNode(-1), term() ); 
        }
        else if (t == Tok.INVALID) throw new Exception("INVALID_TOKEN"); //nongramatical: Quickly catches invalid tokens
        else {
            pushBack("e", t);
            left = term();
        }
        Tok token = str.nextToken();  
		if (token == Tok.PLUS) return new OpNode(Op.Plus, left, expr()); 
        else if (token == Tok.MINUS) return new OpNode(Op.Minus, left, expr());	
        else { 
            pushBack("e", token);
            return left; 
	    }
    }
	

	/**
	 * Consumes everything up to a times or divide, as according to the grammar.
	 * @return An operator node with operator type times or divide, or a node that is a power()
	 */
	public Node term() throws Exception { 
//System.out.println("  term -->");
        Node left; 
        Tok t = str.nextToken(); 
        if (t == Tok.MINUS) {
            left = new OpNode(Op.Times, new NumNode(-1), power() ); 
        }
        else {
            pushBack("t", t);
            left = power();
        }
		Tok token = str.nextToken();
		if (token == Tok.TIMES) return new OpNode(Op.Times, left, term());
		else if (token == Tok.DIVIDE) return new OpNode(Op.Divide, left, term());
		else {
			pushBack("t", token); 
			return left;
		}
	}

	public Node power() throws Exception { 
//System.out.println("    power-->");
        Node left; 
        Tok t = str.nextToken(); 
        if (t == Tok.MINUS) {
            left = new OpNode(Op.Times, new NumNode(-1), factor() ); 
        }
        else {
            pushBack("p", t);
            left = factor();
        }
		Tok token = str.nextToken();
		if (token == Tok.HAT) return new OpNode(Op.Hat, left, power());
		else {
			pushBack("p", token); //nextToken could be a +,-,*,/
			return left;
		}
	}
	
	public Node factor() throws Exception { 
//System.out.println("      factor -->");
        Tok token = str.nextToken();
		if (token == Tok.VARIABLE) return new VarNode(str.strVal);
		if (token == Tok.NUMBER) return new NumNode(str.numVal);	
		if (token == Tok.FUNCTION) { 
			String funcName = str.strVal;
			if (str.nextToken() != Tok.LPAR)
				throw new Exception( "INVALID_FUNCTION_SYNTAX: expecting left paren");
			Node argExpr = expr();	//consume everything up to the right-parenthesis, which was pushed back in expr().
			if (str.nextToken() != Tok.RPAR) 
				throw new Exception("INVALID_FUNCTION_SYNTAX: expecting right paren");
			
			return new FuncNode( str.getFunc( funcName ), argExpr );
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
			
			return new SumNode( argExpr, var.toString(), (int)start.eval(), (int)limit.eval() );
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
			
			return new ProdNode( argExpr, var.toString(), (int)start.eval(), (int)limit.eval() );
		}
		if (token == Tok.LPAR) {	//Parenthesis without a function 
            Node interior = expr();	 //Expr inside '(' and ') 
			Tok extra = str.nextToken();
			if (extra == Tok.RPAR) return interior; //consumes everything up to the next ')'.
			throw new Exception("UNBALANCED_PARENTHESIS"); //no rightparens makes input invalid.
            /* We would pushBack here if we had more tokens. Yet the only tokens we can get here now are EOS, INVALID, and UNDEF */
		}
        //nextToken() design saves need for pushing back EOS
        return null; 
	}	

	public static String genRandomTest(int length) throws Exception { 
        String str = "";    
        for (int i=0; i<length; i++) { 
            int pos = (int) (chars.length * Math.random());
            str += chars[pos];
        }
        return str;
    }

} 

public class Function { 

    Node func;
    String s;
    HashMap<String, Double> argList = new HashMap<String, Double>();
    static double CONTINUITY_INCREMENT = .000001;
    static double MARGIN_OF_ERROR = .01;

    public Function(String s) { 
        Tokenizer T = new Tokenizer(s);
        Parser P = new Parser(T);
        this.func = P.root;
        this.s = s;
        argList.put("x", Double.MIN_VALUE );
    }

    public void print() { 
        System.out.println(s);
    }

    public double value(double x) throws Exception {
        argList.replace("x", x);
        return func.eval(argList);
    }

   public boolean isContinuous(double x) {
        double v;
        try { 
            v = value(x);
            for (double d = x - 5 * CONTINUITY_INCREMENT; d < x; d += CONTINUITY_INCREMENT) {
                try { 
                    double fy = value(d); 
                    //System.out.println("x="+x+", fy="+fy+", v="+v);
                    if ( Math.abs(v - fy) > MARGIN_OF_ERROR ) return false;
                }
                catch (Exception e) { e.printStackTrace(); }
            }
            for (double d = x + 5 * CONTINUITY_INCREMENT; d > x; d -= CONTINUITY_INCREMENT) {
                try { 
                    double fy = value(d); 
                    if ( Math.abs(v - fy) > MARGIN_OF_ERROR ) return false;
                }
                catch (Exception e) { e.printStackTrace(); }
            }
        }
        catch (Exception e) { if ( e.getMessage().equals("division by 0") ) return false; }
        return true;
   }

    public static void main(String[] args) { 
            /* 
            if (args.length < 2) {
                System.out.println("ERROR: expecting 2 arguments");
                return;
            }	        
            int num = Integer.parseInt(args[0]);
            int len = Integer.parseInt(args[1]); 
            for (int i=0; i<num; i++) { 
                String s = "";
                try { 
                    s = genRandomTest(len);
                    System.out.println(s);
                    Tokenizer T = new Tokenizer(s);
                    Parser P = new Parser(T);
                    P.root.print(); 
                    
                }       
                catch(Exception e) { 
                    e.printStackTrace();
                }
           }
           */
         	
            /* 
            while (true) {
            Tok t = T.nextToken(); 
			//if (Character.isWhitespace(0)) System.out.println("the null character is a white space");
			System.out.print(t);
			if (t == Tok.VARIABLE || t ==Tok.FUNCTION) System.out.print(", " + T.strVal);
			if (t == Tok.NUMBER) System.out.print(", " + T.numVal);
			if (t == Tok.EOS) { 
                System.out.println();
                break; 
            }
                System.out.println();
            }
            */

            /*
            try {
			    HashMap<String, Double> argList = new HashMap<String, Double>();
			    argList.put( "x", 2.0 );
			    argList.put( "n", 0.0);
			System.out.println(P.root.eval( argList ));
		    } catch (Exception e) {
			    e.printStackTrace();
	 	    } 
            */

            Function f = new Function(args[0]);
            System.out.println(f.isContinuous(Double.parseDouble(args[1])));
    }

}

