public class C_488294329 extends Function {

    double x;
    double y;

    public C_488294329() {
        super();
    }

    public boolean isBivariate() {
        return true;
    }

    public double value(double... point) {
        x = point[0];
        y = point[1];
        return (1 * (x) * (x)) - (1 * (y) * (y)) + (-7.5);
    }

    public static void main(String[] args) {
        C_488294329 func = new C_488294329();
        if (args.length == 1) System.out.println(func.value(Double.parseDouble(args[0])));
        if (args.length == 2) System.out.println(func.value(Double.parseDouble(args[0]),
                                                            Double.parseDouble(args[1])));
    }

}