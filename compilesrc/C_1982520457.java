public class C_1982520457 extends Function {

    double x;
    double y;

    public C_1982520457() {
        super();
    }

    public boolean isBivariate() {
        return true;
    }

    public double value(double... point) {
        x = point[0];
        y = point[1];
        return (1 * (x) * (x) * (x) * (x) * (x) * (x)) - (1 * (y) * (y) * (y) * (y)) + (Math.pow(x, y)) + (-1.0);
    }

    public static void main(String[] args) {
        C_1982520457 func = new C_1982520457();
        if (args.length == 1) System.out.println(func.value(Double.parseDouble(args[0])));
        if (args.length == 2) System.out.println(func.value(Double.parseDouble(args[0]),
                                                            Double.parseDouble(args[1])));
    }

}