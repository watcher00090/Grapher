import java.util.HashMap;

//Tokenizer states.
enum S { 
	INIT, WORD, INTEGER, DOUBLE, DONE;
}

//Token Types. UNDEF = pushback token when nothing's been pushed back, token returned when state is unknown.
enum T { 
	LPAR, RPAR, PLUS, MINUS, TIMES, DIVIDE, HAT, NUMBER, VARIABLE, FUNCTION, SUMMATION, PRODUCT, COMMA, EOS, UNDEF, INVALID
}

//Built-in functions
enum F { 
	Sin, Cos, Tan, Exp, Sqrt, Log, Undef, Factorial;	
}

//Operators
enum O { 
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
 * If someone else depends on seeing a character, but you've already used it up, you've GOT TO PUSH IT BACK!!!
 * 
 * Distinguishes between functions
 * 1. Built-in
 * 2. Sum or Product(evaluated function, different from the built-in functions). 
 * 
 */
class Tokenizer { 

	char[] chars; 
	int i;
	double numVal; //start as an int. Then changes to double if a period is found.
	String strVal; //stores function or variable names
	S state;		
	T backToken;

	Tokenizer(String in) { 
		numVal = 0.0;
		strVal = "";
		i = 0;
		state = S.INIT;
		backToken = T.UNDEF;
		chars = new char[in.length()+1];
		for (int k=0; k<in.length(); k++){
			chars[k]=in.charAt(k);	//populate the character array
		}
		chars[in.length()] = 0; //end with the null byte character, so the Tokenizer knows when the String is done.
	}

	/**
	 * @param c
	 * @return true if the character is an operator
	 */
	boolean isOperator(char c) { 
		if (c == '+') return true;
		if (c == '-') return true;
		if (c == '*') return true;
		if (c == '/') return true;
		if (c == '^') return true;
		return false;
	}

	/**
	 * @param c
	 * @return the Operator that the character is. Else, return an undefined operator
	 */
	T getOperator(char c) { 
		if (c == '+') return T.PLUS;
		if (c == '-') return T.MINUS;
		if (c == '*') return T.TIMES;
		if (c == '/') return T.DIVIDE;
		if (c == '^') return T.HAT;
		return T.UNDEF;
	}

	/**
	 * 
	 * @param s, the input String
	 * @return true if the String is a built-in function
	 */
	public boolean isFunction(String s) {
		if (s.equals("sin")) return true;
		if (s.equals("cos")) return true;
		if (s.equals("exp")) return true;
		if (s.equals("tan")) return true;
		if (s.equals("sqrt")) return true;
		if (s.equals("log")) return true; 
		if (s.equals("!")) return true;
		return false;
	}	

	/**
	 * 
	 * @param s, the input String
	 * @return the Function enum type of String s. If s isn't a function, throw an exception.
	 * @throws Exception
	 */
	F getFunction(String s) throws Exception { 
		if (s.equals("sin")) return F.Sin;
		if (s.equals("cos")) return F.Cos;
		if (s.equals("tan")) return F.Tan;
		if (s.equals("exp")) return F.Exp;
		if (s.equals("sqrt")) return F.Sqrt;
		if (s.equals("log")) return F.Log;
		if (s.equals("!")) return F.Factorial;
		else throw new Exception("Function is undefined");
	}

	 /**
	  * Fewer States is desirable. Operators and punctuation are single characters, so there isn't any reason 
	  * to define separate states for each one. Therefore, any other state encounters one, the State switches to S.INIT,
	  * where a token indicating corresponding to that character is returned. Every time a character is hit that changes
	  * the state, i is decremented so that the next call to nextToken() so that the character that changed the state
	  * isn't skipped over.
	  * 
	  * When the end of the String is reached, repeated calls to nextToken() return T.EOS. 
	  * 
	  * @return the next token in the input String.
	  */
	public T nextToken() { 

		if (backToken !=T.UNDEF) {	//if a token has been pushed back BY THE PARSER,  
			T result = backToken;  	
			backToken = T.UNDEF;	//remove the backtoken
			return result; 			//return the backtoken
		}

		numVal = 0.0;	//if the token is a double, numVal is taken immediately after nextToken() is called by the parser, so no need to store numVal between iterations.		
		strVal = "";	//if the token is a String, strVal is taken immediately after nextToken() is called by the parser, so no need to store strVal between iterations.	
		int p = 0;		//stores double decimal point.
		state = S.INIT; //State is internal. So when nextToken is called, the computer shouldn't know what state it's in. 

		while (true) { 

			char c = chars[i++]; //Get the next character. first assign, then increment

//System.out.println(i);
//System.out.println("state = " + state );
//System.out.println("state = " + state);
//System.out.println("c = " + c);

			switch (state) { 
			case INIT: { //Initial state of the scanner.
				if (Character.isAlphabetic(c)) { 
					state = S.WORD;
					strVal+=c;	//add the letter to the word
				}
				else if (Character.isDigit(c)) { 
					state = S.INTEGER;
					numVal = 10*numVal + (c - '0') ; //add the digit to the number
//System.out.println("numVal = " + numVal);
				}
				else if (isOperator(c)) return getOperator(c);
				else if (c == '(') return T.LPAR;
				else if (c == ')') return T.RPAR;
				else if (c == '.') state = S.DOUBLE;
				else if (c == ',') return T.COMMA;
				else if (c == '!') return T.FUNCTION;
				else if (c == 0) { //DONE must know that we've reached the end.
					i--;			//allow repeated calls to return T.EOS
					state = S.DONE;	
				}
				else if (Character.isWhitespace(c)) continue; //whitespace between operators or punctuation is insignificant
				else return T.INVALID;	
				break;
			}
			case INTEGER: { 
				if (Character.isAlphabetic(c)) { 
//System.out.println("ERROR: S=NUMBER, c=letter");
					return T.INVALID;
				}
				else if (Character.isDigit(c)) { 
					numVal = 10*numVal + (c -'0'); //>0	//add digit to the number
				}
				else if (isOperator(c) || c =='(' || c==')' || c==',' || Character.isWhitespace(c) || c==0) {
					if (c==0) state = S.DONE;
					i--; //the next call to nextToken() needs to know the character.
					return T.NUMBER;
				}
				else if (c == '.') {
					state = S.DOUBLE;
				}
				else {
					return T.INVALID;
				}
				break;
			}
			case DOUBLE: {
				if (Character.isAlphabetic(c)) { 
					return T.INVALID;
//System.out.println("ERROR 003");	
				}
				else if (Character.isDigit(c)){ 
					p++;
					numVal = numVal + (c -'0')/(Math.pow(10, p)); //<0
				}			
				else if (c == '(' || c ==')' || c==',' || Character.isWhitespace(c) || isOperator(c) || c==0) {
					if (c==0) state = S.DONE;
					i--; 
					return T.NUMBER;
				}
				else { 
					return T.INVALID;
				}
				break;
			}
			case WORD: {
				if (Character.isAlphabetic(c) || Character.isDigit(c)) { 
					strVal+=c;
				}
				else if (isOperator(c) || c=='(' || c==')' || c==',' || Character.isWhitespace(c) || c==0) { 
					i--;
					if (c==0) state = S.DONE;
					if (isFunction(strVal)) return T.FUNCTION;
					if (strVal.equals( "Sum" )) return T.SUMMATION;
					if (strVal.equals( "Prod" )) return T.PRODUCT;
					return T.VARIABLE;
				}
				else {
					return T.INVALID;
				}
				break;
			}
			case DONE: return T.EOS; 
			default: return T.UNDEF; //undefined state error.
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

/**
 * Operator node
 * 
 *
 */
class OpNode extends Node { 
	O op;

	public String toString() { return op.toString(); }
	
	/**
	 * Evaluates recursively.
	 */
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

	//constructor
	public OpNode(O op, Node left, Node right) {
		this.op = op;
		this.left = left;
		this.right = right;
	}

}

/**
 * Variable node
 * 
 */
class VarNode extends Node { 
	String name;

	public String toString() { return name; }
	
	public double eval( HashMap<String, Double> args ) throws Exception {
		Double dd = args.get( name ); //gets the value corresponding to the Node's variable.
		if (dd==null)	//if the variable name isn't in the hashmap...
			throw new Exception( "VarNode has no value mapping" );
		return dd.doubleValue();	
	}
	
	VarNode(String name) { 
		this.name = name; 
		left = null;
		right = null; 
	}

}

/**
 * Number node
 * 
 */
class NumNode extends Node{ 
	double val;

	public String toString() { return Double.toString(val); }
	public double eval( HashMap<String, Double> args ) throws Exception { return val; }
	public double eval() throws Exception { return val; }

	NumNode(double val) { 
		this.val = val; 
		left = null; //leaf node, has no left or right children.
		right = null;
	}
}

/**
 * Function node. The argument of the function can itself be an expression, like sin(5x^2 + 5)
 *
 */
class FuncNode extends Node { 
	F name;
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
		default: throw new Exception( "UNRECOGNIZED_FUNCTION" ); 
		}
	}

	public FuncNode( F type, Node argExpr ) { 
		this.name = type;
		this.argExpr = argExpr;
		left = null;
		right = null;
	}	
}

/**
 * Represents a sum. Representing sums and products as nodes allows for
 * for expressions of sums and products, such as sum(x,n,0,1) + 5. 
 *
 */
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
		double accum = 1.0;
		for (int i=start; i<=limit; i++) {
			if (null == args.replace( var, new Double(i) ))
				throw new Exception( "Summation parameter missing in arg list" );
			accum *= argExpr.eval( args );
		}
		return accum;
	}
}


/**
 * 
 * The Grammar:
 * Expr ::= Term
 *        | Term + Expr
 *        | Term - Expr

 * Term ::= Power
 *        | Power * Term
 *        | Power / Term
 *        
 * Power ::= Factor	
 * 			 Factor ^ Power
 * 
 * Factor ::= Variable
 *       	| Number
 *        	| '(' Expr ')'
 *        	| FUNCTION_NAME '(' Expr ')' 
 *       	| 'Sum' '(' Expr ',' Var ',' Number ',' Number ')' 
 *      	| 'Prod' '(' Expr ',' Var ',' Number ',' Number ')' 
 *  
 */
public class Parser { 

	Tokenizer S; //used to repeatedly call nextToken()
	public Node root; //stores the tree.

	private void pushBack(T token) { S.backToken = token; }	

	public Node getRoot() { return root; }

	Parser(Tokenizer S) { 
		this.S = S;
		try {
			root = expr();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Consumes everything up to a plus or a minus. 
	 * @return An operator node with operator type plus or minus, or a node that is a term()
	 * @throws Exception
	 */
	public Node expr() throws Exception { 
		System.out.println("expr-> ");
		Node left = term(); //Consumes everything up to the + or -. Also, stays disjoint from the larger tree (until below). 
		T token = S.nextToken();  
		if (token == T.PLUS) return new OpNode(O.Plus, left, expr()); // Expr::= Term +- Expr. The OpNode is a plus and has 2 children.
		if (token == T.MINUS) return new OpNode(O.Minus, left, expr());	
		else {
			pushBack(token); //necessary to push back right-parenthesis tokens to end functions created in factor().
			return left; //Expr::=Term
		}
	}

	/**
	 * Consumes everything up to a times or divide, as according to the grammar.
	 * @return An operator node with operator type times or divide, or a node that is a power()
	 */
	public Node term() throws Exception { 
		System.out.println("   term->");
		Node left = power();
		T token = S.nextToken();
		if (token == T.TIMES) return new OpNode(O.Times, left, term());
		if (token == T.DIVIDE) return new OpNode(O.Divide, left, term());
		else {
			pushBack(token); //nextToken could be a +,-
			return left;
		}
	}

	/**
	 * power ::=  factor
	 *          | factor ^ power
	 *          
	 * @return An operator node with operator type power, or a node that is a factor()
	 */
	public Node power() throws Exception { 
		System.out.println("   power->");
		Node left = factor();
		T token = S.nextToken();
		if (token == T.HAT) return new OpNode(O.Hat, left, power());
		else {
			pushBack(token); //nextToken could be a +,-,*,/
			return left;
		}
	}
	
	/**
	 * 
	 * @return A variable, number, or function node.
	 * @throws Exception
	 */
	public Node factor() throws Exception {
System.out.println("      factor->");
		T token = S.nextToken();
		if (token == T.VARIABLE) return new VarNode(S.strVal);
		if (token == T.NUMBER) return new NumNode(S.numVal);	
		if (token == T.FUNCTION) { 
			String funcName = S.strVal;
			if (S.nextToken() != T.LPAR)
				throw new Exception( "INVALID_FUNCTION_SYNTAX: expecting left paren");
			Node argExpr = expr();	//consume everything up to the right-parenthesis, which was pushed back in expr().
			if (S.nextToken() != T.RPAR) 
				throw new Exception("INVALID_FUNCTION_SYNTAX: expecting right paren");
			
			return new FuncNode( S.getFunction( funcName ), argExpr );
		}
		if (token == T.SUMMATION) { // expect:  '(' Expr ',' Var ',' Number ',' Number ')' 
			
			if (S.nextToken() != T.LPAR)
				throw new Exception( "INVALID_FUNCTION_SYNTAX: expecting left paren");
			
			Node argExpr = expr();
			if (S.nextToken() != T.COMMA) 
				throw new Exception("INVALID_FUNCTION_SYNTAX: expecting comma");
			
			Node var = factor(); //variable expected
			if (!(var instanceof VarNode)) 
				throw new Exception("INVALID_FUNCTION_SYNTAX: expecting var name");
			
			if (S.nextToken() != T.COMMA) 
				throw new Exception("INVALID_FUNCTION_SYNTAX: expecting comma");
			
			Node start = factor();//number expected
			if (!(start instanceof NumNode))
				throw new Exception("INVALID_FUNCTION_SYNTAX: expecting start value, a number");
			
			if (S.nextToken() != T.COMMA) //number expected
				throw new Exception("INVALID_FUNCTION_SYNTAX: expecting comma");
			
			Node limit = factor();
			if (!(limit instanceof NumNode))
				throw new Exception("INVALID_FUNCTION_SYNTAX: expecting limit value, a number");
			
			if (S.nextToken() != T.RPAR) 
				throw new Exception("INVALID_FUNCTION_SYNTAX: expecting right paren");
			
			return new SumNode( argExpr, var.toString(), (int)start.eval(), (int)limit.eval() );
		}
		if (token == T.PRODUCT) {
			// expect:  '(' Expr ',' Var ',' Number ',' Number ')' 
			
			if (S.nextToken() != T.LPAR)
				throw new Exception( "INVALID_FUNCTION_SYNTAX: expecting left paren");
			
			Node argExpr = expr();
			
			if (S.nextToken() != T.COMMA) 
				throw new Exception("INVALID_FUNCTION_SYNTAX: expecting comma");
			
			Node var = factor();
			if (!(var instanceof VarNode))
				throw new Exception("INVALID_FUNCTION_SYNTAX: expecting var name");
			
			if (S.nextToken() != T.COMMA) 
				throw new Exception("INVALID_FUNCTION_SYNTAX: expecting comma");
			
			Node start = factor();
			if (!(start instanceof NumNode))
				throw new Exception("INVALID_FUNCTION_SYNTAX: expecting start value, a number");
			
			if (S.nextToken() != T.COMMA) 
				throw new Exception("INVALID_FUNCTION_SYNTAX: expecting comma");
			
			Node limit = factor();
			if (!(limit instanceof NumNode))
				throw new Exception("INVALID_FUNCTION_SYNTAX: expecting limit value, a number");
			
			if (S.nextToken() != T.RPAR) 
				throw new Exception("INVALID_FUNCTION_SYNTAX: expecting right paren");
			
			return new ProdNode( argExpr, var.toString(), (int)start.eval(), (int)limit.eval() );
		}
		if (token == T.LPAR) {	//Parenthesis without a function 
			Node interior = expr();	 //Expr inside '(' and '). 
			T extra = S.nextToken();
			if (extra == T.RPAR) return interior; //should consume and return everything up to the next ')'.
			throw new Exception("unbalanced parenthesis"); //no rightparens makes input invalid. We would pushBack here if we had more tokens. Yet the only tokens we can get here now are EOS, INVALID, and UNDEF
		}  
		return null; //leaf nodes have no children. If token is EOS, we're at the end.
	}	
	
	//can run test code for the Tokenizer and Parser here if something stops working
	public static void main(String[] args) { 
		/*Tokenizer tokenize = new Tokenizer( "Sum(sin(x^n),n,1,10)" );		
		while (true) { 
			//if (Character.isWhitespace(0)) System.out.println("the null character is a white space");
			//	for (char c: tokenize.chars) System.out.print(c + " "); 
			T type = tokenize.nextToken();
			System.out.print(type);
			if (type == T.VARIABLE || type ==T.FUNCTION) System.out.print(", " + tokenize.strVal);
			if (type == T.NUMBER) System.out.print(", " + tokenize.numVal);
			if (type == T.EOS) break; 
			System.out.println();
		}  

		Parser P = new Parser(tokenize);
		P.root.print(0);
		
		try {
			HashMap<String, Double> argList = new HashMap<String, Double>();
			argList.put( "x", 2.0 );
			argList.put( "n", 0.0);
			System.out.println(P.root.eval( argList ));
		} catch (Exception e) {
			e.printStackTrace();
		}
		 */
	}
		
}
