public class C_1954976237 extends Function {

    double x;

    public C_1954976237() {
        super();
    }

    public boolean isBivariate() {
        return false;
    }

    public double value(double... point) {
        x = point[0];
        return ((4.0) * (x)) + (-15.0);
    }

    public static void main(String[] args) {
        C_1954976237 func = new C_1954976237();
        if (args.length == 1) System.out.println(func.value(Double.parseDouble(args[0])));
        if (args.length == 2) System.out.println(func.value(Double.parseDouble(args[0]),
                                                            Double.parseDouble(args[1])));
    }

}