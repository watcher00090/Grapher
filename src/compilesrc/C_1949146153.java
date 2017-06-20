public class C_1949146153 extends Function {

    double x;

    public C_1949146153() {
        super();
    }

    public boolean isBivariate() {
        return false;
    }

    public double value(double... point) {
        x = point[0];
        return Math.pow(2.718281828459045, x);
    }

    public static void main(String[] args) {
        C_1949146153 func = new C_1949146153();
        if (args.length == 1) System.out.println(func.value(Double.parseDouble(args[0])));
        if (args.length == 2) System.out.println(func.value(Double.parseDouble(args[0]),
                                                            Double.parseDouble(args[1])));
    }

}