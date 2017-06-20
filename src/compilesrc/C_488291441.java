public class C_488291441 extends Function {

    double x;
    double y;

    public C_488291441() {
        super();
    }

    public boolean isBivariate() {
        return true;
    }

    public double value(double... point) {
        x = point[0];
        y = point[1];
        return (1 * (x) * (x)) - (1 * (y) * (y)) + (-4.0);
    }

    public static void main(String[] args) {
        C_488291441 func = new C_488291441();
        if (args.length == 1) System.out.println(func.value(Double.parseDouble(args[0])));
        if (args.length == 2) System.out.println(func.value(Double.parseDouble(args[0]),
                                                            Double.parseDouble(args[1])));
    }

}