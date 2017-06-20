public class C_m2041472475 extends Function {

    double x;
    double n;

    public C_m2041472475() {
        super();
    }

    public boolean isBivariate() {
        return false;
    }

    public double value(double... point) {
        x = point[0];
        return sum_1();
    }

    public double sum_1() {
        double result = 0;
        double prev = n;
        for (n = 1; n <= 20; n++) {
            result += ((1.0) / (Math.pow(n, x)));
        }
        n = prev;
        return result;
    }

    public static void main(String[] args) {
        C_m2041472475 func = new C_m2041472475();
        if (args.length == 1) System.out.println(func.value(Double.parseDouble(args[0])));
        if (args.length == 2) System.out.println(func.value(Double.parseDouble(args[0]),
                                                            Double.parseDouble(args[1])));
    }

}