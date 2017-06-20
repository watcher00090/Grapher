public class C_m434488458 extends Function {

    double x;
    double y;

    public C_m434488458() {
        super();
    }

    public boolean isBivariate() {
        return true;
    }

    public double value(double... point) {
        x = point[0];
        y = point[1];
        return (Math.pow(y, Math.tan(x))) - (Math.cos(y)) + (Math.pow(x, y));
    }

    public static void main(String[] args) {
        C_m434488458 func = new C_m434488458();
        if (args.length == 1) System.out.println(func.value(Double.parseDouble(args[0])));
        if (args.length == 2) System.out.println(func.value(Double.parseDouble(args[0]),
                                                            Double.parseDouble(args[1])));
    }

}