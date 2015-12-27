public enum Func { 
    Sin, Cos, Tan, Exp, Abs, Sqrt, Log, Ln, Undef, Factorial, Arcsin, Arccos, Arctan;    

    public static boolean isFunc(String s) {
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

    public static Func getFunc(String s) throws Exception { 
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

}
