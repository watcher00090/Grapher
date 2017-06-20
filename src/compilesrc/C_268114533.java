public class C_268114533 extends Function {

    double x;

    public C_268114533() {
        super();
    }

    public boolean isBivariate() {
        return false;
    }

    public double value(double... point) {
        x = point[0];
        return (4.0) * (x);
    }

    public static void main(String[] args) {
        C_268114533 func = new C_268114533();
        if (args.length == 1) System.out.println(func.value(Double.parseDouble(args[0])));
        if (args.length == 2) System.out.println(func.value(Double.parseDouble(args[0]),
                                                            Double.parseDouble(args[1])));
    }

}