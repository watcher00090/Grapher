public class Point {
    
    public double x;
    public double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Point Q) {
        return (x == Q.x && y == Q.y);
    }

    public String toString() {
        return "("+x+","+y+")";
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }

}
