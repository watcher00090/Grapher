public class C_1826345766 extends Function {

    double x;
    double y;

    public C_1826345766() {
        super();
    }

    public boolean isBivariate() {
        return true;
    }

    public double value(double... point) {
        x = point[0];
        y = point[1];
        return (Math.pow(y, x)) - (Math.pow(x, y));
    }

    public static void main(String[] args) {
        C_1826345766 func = new C_1826345766();
        if (args.length == 1) System.out.println(func.value(Double.parseDouble(args[0])));
        if (args.length == 2) System.out.println(func.value(Double.parseDouble(args[0]),
                                                            Double.parseDouble(args[1])));
    }

}