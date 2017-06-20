public class ZeroLevelSet {

    Function func;
    
    public ZeroLevelSet(Function func) {
        this.func = func;
    }    

    public double lhsvalue(double... point) throws Exception {
        return func.value(point);
    }

}
