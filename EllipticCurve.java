/*
	E(Q) is an elliptic curve defined by
	    C: y² = x³ + Ax + B,
	with discriminant
	    D = 4A³ + 27B² ≠ 0

    Note: A, B are negated in the form 
	    C: y² - x³ - Ax - B
*/

public class EllipticCurve extends ZeroLevelSet {

    long A;
    long B;
    long D;  // the discriminant
    Point basePoint;
    Point movingPoint;
    static final Point zeroPoint = new Point(0.0, Double.MAX_VALUE);
    static final double EC_CURVE_PRECISION_THRESHOLD = .001;

    public EllipticCurve(long A, long B) {
        super(null);
        this.A = A;
        this.B = B;
        D = 4*A*A*A + 27*B*B;
        basePoint = null;
        movingPoint = null;
    }

    public double lhsvalue(double... point) throws Exception {
        if (point.length != 2) throw new Exception("ERROR: EXPECTING_TWO_ARGUMENTS"); 
        double x = point[0];     
        double y = point[1];     
//System.out.println("val(" + x + ", " + y + ")=" + (y*y - x*x*x - A*x - B));
        return y*y - x*x*x - A*x - B;
    }

    public void setA(long _A) {
        A = _A;
        D = 4*A*A*A + 27*B*B;
    }

    public void setB(long _B) {
        B = _B;
        D = 4*A*A*A + 27*B*B;
    }

    // Mathematical coordinate -> point on the elliptic curve 
    public Point makePoint(double x, double y) {
        double rhs = x*x*x + A*x + B;
        return new Point(x, Math.sqrt(rhs));    
    }

    //
    // Elliptic curve addition formula
    // using 64-bit floating point arithmetic
    //
    // Ref: "An Introduction to the Theory of Elliptic Curves", Joseph H. Silverman
    //      http://www.math.brown.edu/~jhs/Presentations/WyomingEllipticCurve.pdf
    //
    public Point sum(Point P, Point Q) {

        if (P.x == Q.x && P.y == Q.y) return times2(P);    

        // check if line PQ is vertical ⟹   P+Q = 0
        if (P.x == Q.x) 
            return zeroPoint;

        // general case: compute line PQ : y = λx + ν, find
        // the reflection of the third intersection

        double l = (Q.y - P.y) / (Q.x - P.x);
        double v = (P.y - l * P.x);

        double x = l*l - P.x - Q.x;
        double y = -(l * x + v);
        return new Point(x, y);
    }

    //
    // Elliptic curve duplication formula
    //
    // Ref: "An Introduction to the Theory of Elliptic Curves", Joseph H. Silverman
    //      http://www.math.brown.edu/~jhs/Presentations/WyomingEllipticCurve.pdf
    //
    public Point times2(Point P) {

        // check vertical tangent ⟹   2P = 0
        if (0.0==P.y)
            return new Point(0.0, Double.MAX_VALUE);

        // the general case: compute the tangent, find
        // the reflection of the third intersection
        double l = (3*P.x*P.x + A) / 2*P.y;
        double v =  P.y - l * P.x;

        double x = l*l - 2*P.x;
        double y = - (l * x + v);
        return new Point(x,y);
    }

    //
    // (n+1)·basePoint = n·basePoint + basePoint
    //
    public Point nextMultiple() {
        if (null==basePoint) {
            return null;
        }
        else if (movingPoint==null) {
            movingPoint = times2(basePoint);
            return movingPoint; 
        }
        else {
            movingPoint = sum(movingPoint, basePoint);
            return movingPoint; 
        }
    }

    //
    // simple unit test
    //
    public static void main(String[] args) {

        EllipticCurve ec = new EllipticCurve(5,3);
        Point p = ec.makePoint(1,3);

        System.out.println(" p = " + p);
        System.out.println("2p = " + ec.nextMultiple());
        System.out.println("3p = " + ec.nextMultiple());
        System.out.println("4p = " + ec.nextMultiple());
        System.out.println("5p = " + ec.nextMultiple());
        System.out.println("6p = " + ec.nextMultiple());
        System.out.println("7p = " + ec.nextMultiple());
        System.out.println("8p = " + ec.nextMultiple());
        System.out.println("9p = " + ec.nextMultiple());

        /* Examples:

            Non-singular case
                y^2 - x^3 + 5*x - 8, P = (1,2)

            2P = (-7/4, -27/8) = (-1.75, -3.375)
            3P = (553/121, -11950/1331) = (4.570, -8.978)
            4P = (45313/11664, -8655103/1259712) = (3.885, 6.871)

            Non-singular case
                y^2 - x^3 - x + 1, P = (2,3)

            Singular case: convergence to the node
                y^2 - x^3 + 3*x - 2
        */
    }

}
