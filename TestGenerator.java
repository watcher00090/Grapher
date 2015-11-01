
public class TestGenerator {

    String number() {
        return new Double(100*Math.random()).toString();
    }

    String function() {
        double d = Math.random();
        if (d<0.1) return "sin";
        if (d<0.2) return "cos";
        if (d<0.3) return "tan";
        if (d<0.4) return "log";
        if (d<0.5) return "ln";
        if (d<0.6) return "exp";
        if (d<0.7) return "abs";
        if (d<0.8) return "arcsin";
        if (d<0.9) return "arccos";
        return "arctan";  
    }

    String factor() {
        String result = "";
        double d = Math.random();
        if (d<0.33)
            result = number();
        else if (d<0.66)
            result = "x";
        else if (d<0.90)
            result = function() + "(x)";
        else
            result = function() + "(" + expr() + ")";
       return result;
   }

   String term() {
       String result = factor();
       int n = (int)(3*Math.random());
       for (int i=0; i<n; i++) {
           String s = (Math.random() < 0.5 ? "*" : "*");
           result += s;
           result += factor();
       }
       return result;
   }

   String expr() {
       String result = term();
       int n = (int)(3*Math.random());
       for (int i=0; i<n; i++) {
           String s = (Math.random() < 0.5 ? "-" : "+");
           result += s;
           result += term();
       }
       return result;
   }

   public static void main(String[] args) {
       int primes[] = {
         2,     3,     5,     7,    11,    13,    17,    19,    23,    29,
        31,    37,    41,    43,    47,    53,    59,    61,    67,    71,
        73,    79,    83,    89,    97,   101,   103,   107,   109,   113,
       127,   131,   137,   139,   149,   151,   157,   163,   167,   173,
       179,   181,   191,   193,   197,   199,   211,   223,   227,   229,
       233,   239,   241,   251,   257,   263,   269,   271,   277,   281,
       283,   293,   307,   311,   313,   317,   331,   337,   347,   349,
       353,   359,   367,   373,   379,   383,   389,   397,   401,   409,
       419,   421,   431,   433,   439,   443,   449,   457,   461,   463,
       467,   479,   487,   491,   499,   503,   509,   521,   523,   541
       };
          
      // basic stress testing: simple polynomials
      String result = "";
          for (int i=0; i<20; i++) {
          result += " + cos("+primes[i]+"*x)";
          System.out.println(result);
      }
      result = "";
      for (int i=0; i<40; i++) {
          String s = (Math.random() < 0.5 ? " + " : " - ");
          result += (""+s+primes[i]+"*x^"+i);
          System.out.println(result);
      }

      // now some random expressions
      TestGenerator g = new TestGenerator();
      for (int i=0; i<10; i++) {
          System.out.println( g.expr() );
      }
    }

 }
