public class C_460432335 extends Function {

    double x;
    double y;

    public C_460432335() {
        super();
    }

    public boolean isBivariate() {
        return true;
    }

    public double value(double... point) {
        x = point[0];
        y = point[1];
        return (1 * (y) * (y)) + (Math.pow(x, y)) - (Math.pow(y, x));
    }

    public static void main(String[] args) {
        C_460432335 func = new C_460432335();
        if (args.length == 1) System.out.println(func.value(Double.parseDouble(args[0])));
        if (args.length == 2) System.out.println(func.value(Double.parseDouble(args[0]),
                                                            Double.parseDouble(args[1])));
    }

}