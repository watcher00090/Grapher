import java.util.HashMap;
import java.util.Scanner;
import java.awt.event.KeyEvent;

public class Tester { 

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

    public static void testPderiv(String[] args) {
        
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

    public static void testIsContinuous(String[] args) {
        Parser P = new Parser(args[0]);
        NonCompiledFunction func = new NonCompiledFunction(P.root, P.argList);
        func.print();
        Double val = Double.parseDouble(args[1]);
        try { 
            System.out.println("isContinuous(" + val +
                               ") = " + func.isContinuous(val)
                              ); 
        }
        catch (Exception e) { e.printStackTrace(); } 
    }

    public static void testNonCompiledFunction(String[] args) {
        Parser P = new Parser(args[0]);
        NonCompiledFunction func = new NonCompiledFunction(P.root, P.argList);
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

    public static void testNewton(String[] args) {
        Parser P = new Parser(args[0]);
        Node func = P.root;
        try { 
            Node deriv = P.root.pderiv("x");
            HashMap<String, Double> argList = P.argList;
            double x0 = Double.parseDouble(args[1]);
            double threshold = Double.parseDouble(args[2]);
            System.out.println("newton(func, deriv, "+x0+", "+threshold+") = "+
                                NonCompiledFunction.newton(func, deriv, "x", argList, x0, threshold));
        }
        catch (Exception e) { 
            e.printStackTrace();
        }
    }

    public void testGraphCanvas(String[] args) {
        GraphCanvas g = new GraphCanvas();
        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()) {
            System.out.println("> ");
            String text = sc.nextLine();
            g.inputbar.setText(text);
            g.G.keyPressed( new KeyEvent(g,
                            KeyEvent.KEY_PRESSED,
                            System.currentTimeMillis(),
                            0,
                            KeyEvent.VK_ENTER,
                            '\n' ));
            try { 
                Thread.sleep(500);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
         }
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
        //testIsContinuous(args);
        //testNonCompiledFunction(args);
        //testNewton(args);
    }

}
