import java.util.HashMap;
import java.util.Vector;

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
     |  Param '(' Number ':' Number ', ' Number ')'

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
                termlist.add(term, s);
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
            return (new FuncNode(Func.getFunc(funcName), argExpr)).reduce();
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
            
            Node start = power();//number expected
            if (!(start instanceof NumNode))
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting start value, a number");
            
            if (str.nextToken() != Tok.COMMA) //number expected
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting comma");
            
            Node limit = power();
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
            
            Node start = power();
            if (!(start instanceof NumNode))
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting start value, a number");
            
            if (str.nextToken() != Tok.COMMA) 
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting comma");
            
            Node limit = power();
            if (!(limit instanceof NumNode))
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting limit value, a number");
            
            if (str.nextToken() != Tok.RPAR) 
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting right paren");
            
            return (new ProdNode(argExpr, var.toString(), (int) start.eval(), (int) limit.eval())).reduce();
        }
        if (token == Tok.REX) {
            //expect: '(' Number ')'

            if (str.nextToken() != Tok.LPAR) 
                throw new Exception( "INVALID_FUNCTION_SYNTAX: expecting left paren");

            Node argExpr = expr();
            if (str.nextToken() != Tok.COMMA) 
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting comma");

            Node limit = power(); //expecting number
            if (!(limit instanceof NumNode))
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting limit value, a number");

            if (str.nextToken() != Tok.RPAR) 
                throw new Exception("INVALID_FUNCTION_SYNTAX: expecting right paren");

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
        return null; 
    }    

}
