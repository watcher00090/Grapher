import java.io.IOException;
import java.io.StringWriter;
import java.io.File;
import java.io.FileWriter;

public class Compiler {

    public static void compileFunction(Parser P) {
        StringWriter str = new StringWriter();
        CompilerContext context = new CompilerContext();
        boolean isBivariate = P.argList.containsKey("y") ? true : false;
        String classname = getClassName(P);  

System.out.println("classname="+classname);
        
        if ((new File("bin/" + classname + ".class")).exists() == true) return; 

System.out.println("compiling new file!");

        str.write( "public class " + classname + " extends Function {\n\n");

        for (String var : P.argList.keySet()) {
        str.write( "    double " + var + ";\n");     
        }
        str.write( "\n");     
        str.write( "    public " + classname + "() {\n"
                  +"        super();\n"
                  +"    }\n\n"
                    
                  +"    public boolean isBivariate() {\n"
                  +"        return " + isBivariate + ";\n"
                  +"    }\n\n"

                  +"    public double value(double... point) {\n");

        if (P.argList.containsKey("x")) {
        str.write( "        x = point[0];\n");
        }
        if (P.argList.containsKey("y")) {
        str.write( "        y = point[1];\n");
        }

        str.write( "        return ");

        P.root.codeGen(str, context);

        str.write(";\n");
        str.write("    }\n\n");
        
        while (context.nodev.size() != 0) { 

System.out.println("got here"); 

            Node node = context.nodev.remove(0);
            String name = context.namev.remove(0);
    
                str.write( "    public double " + name + "() {\n");
    
                int startbit = node instanceof ProdNode? 1 : 0;
                str.write( "        double result = " + startbit + ";\n");

            if (node instanceof RexNode) {
                str.write( "        double v = "); 
                          node.getArgExpr().codeGen(str, context);                  
                str.write( ";\n"
                          +"        for (int rex_index_var = 0; " 
                                      + "rex_index_var < " + node.getLimit() + "; " 
                                      + "rex_index_var++) {\n"
                          +"            result += Math.cos( Math.log(v) * Function.zeta_zeros[rex_index_var] );\n"
                          +"        }\n"
                          +"        return (1 - 2*result/Math.sqrt(v) - 1 / (v*v*v-v));\n"
                          +"    }\n\n");
            }

            else {
                str.write( "        double prev = " + node.getVar() + ";\n");
                char opbit = node instanceof SumNode ? '+' : '*';
                str.write(
                           "        for (" + node.getVar() + " = " + node.getStart() + "; " 
                                           + node.getVar() +" <= " + node.getLimit() + "; "
                                           + node.getVar() + "++) {\n"
                          +"            result " + opbit + "= ("); 
    
                          node.getArgExpr().codeGen(str, context);

                str.write( ");\n"
                          +"        }\n"
                          +"        "+node.getVar() + " = prev;\n"
                          +"        return result;\n"
                          +"    }\n\n"
                         );
            } 

        }

        str.write( "    public static void main(String[] args) {\n"
                  +"        " + classname + " func = new " + classname + "();\n"
                  +"        if (args.length == 1) System.out.println(func.value(Double.parseDouble(args[0])));\n"
                  +"        if (args.length == 2) System.out.println(func.value(Double.parseDouble(args[0]),\n" 
                  +"                                                            Double.parseDouble(args[1])));\n"
                  +"    }\n\n"
                  +"}"
                 );

        try { 
            //File compilesrc = new File("compilesrc");
            //compilesrc.mkdir();
            //compilesrc.deleteOnExit();

            File tmpfile = new File("compilesrc/" + classname + ".java");
            //tmpfile.deleteOnExit();

            FileWriter fr = new FileWriter(tmpfile); 
            fr.write(str.toString());
            fr.close();
        }
        catch (NullPointerException e1) {
            e1.printStackTrace();
        }
		catch (IOException e2) {
			e2.printStackTrace();	
		}
       
        try {
            String[] cmdArray = new String[4];
            cmdArray[0] = "javac";
            cmdArray[1] = "-d";
            cmdArray[2] = "bin"; 
            cmdArray[3] = "compilesrc/" + classname + ".java";
            Process process = Runtime.getRuntime().exec(cmdArray, null);
            process.waitFor();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getClassName(Parser P) {
        String exprStr = P.root.toString();
        return   exprStr.hashCode() < 0 
               ? "C_m" + String.valueOf(Math.abs(exprStr.hashCode()))
               : "C_" + String.valueOf(exprStr.hashCode());
    }

}
