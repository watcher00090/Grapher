public class C_m1931171734 extends Function {

    double x;
    double y;

    public C_m1931171734() {
        super();
    }

    public boolean isBivariate() {
        return true;
    }

    public double value(double... point) {
        x = point[0];
        y = point[1];
        return (Math.pow(x, y)) - (Math.pow(y, x)) + (-15.0);
    }

    public static void main(String[] args) {
        C_m1931171734 func = new C_m1931171734();
        if (args.length == 1) System.out.println(func.value(Double.parseDouble(args[0])));
        if (args.length == 2) System.out.println(func.value(Double.parseDouble(args[0]),
                                                            Double.parseDouble(args[1])));
    }

}