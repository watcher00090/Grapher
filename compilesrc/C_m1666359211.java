public class C_m1666359211 extends Function {

    double x;
    double n;

    public C_m1666359211() {
        super();
    }

    public boolean isBivariate() {
        return false;
    }

    public double value(double... point) {
        x = point[0];
        return Math.pow(2.718281828459045, sum_1());
    }

    public double sum_1() {
        double result = 0;
        double prev = n;
        for (n = 1; n <= 4; n++) {
            result += (Math.pow(x, n));
        }
        n = prev;
        return result;
    }

    public static void main(String[] args) {
        C_m1666359211 func = new C_m1666359211();
        if (args.length == 1) System.out.println(func.value(Double.parseDouble(args[0])));
        if (args.length == 2) System.out.println(func.value(Double.parseDouble(args[0]),
                                                            Double.parseDouble(args[1])));
    }

}