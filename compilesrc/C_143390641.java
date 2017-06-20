public class C_143390641 extends Function {

    double x;
    double y;

    public C_143390641() {
        super();
    }

    public boolean isBivariate() {
        return true;
    }

    public double value(double... point) {
        x = point[0];
        y = point[1];
        return (Math.pow(x, x)) - (Math.pow(y, y)) - (1 * (x) * (x));
    }

    public static void main(String[] args) {
        C_143390641 func = new C_143390641();
        if (args.length == 1) System.out.println(func.value(Double.parseDouble(args[0])));
        if (args.length == 2) System.out.println(func.value(Double.parseDouble(args[0]),
                                                            Double.parseDouble(args[1])));
    }

}