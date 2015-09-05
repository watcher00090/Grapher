/**
 * The following contains the code for a String Tokenizer, Recursive-Descent parser, and 2D-scalable OldGraphing applet. 
 * 
 * Currently, the user must specify the variable in the HashMap created in main, and the String
 * containing the function as a parameter of the Tokenizer created in main.
 * 
 * Features to be added later:
 * Linear tickmarks on the y-axis.
 * An input bar: so the user can change the function and and it's domain end-points without
 * having to edit numbers directly into the code.
 * A Recursive-Descent parser generator that takes a grammer as a parameter and updates a file with the code for the parser.
 * Differentiable functions?
 * Vectors? 
 * Regions?
 * Complex Numbers?
 * Tools to calculate the critical points and inflection points?
 * 3D graphing?
 * 
 * @author: James Pedersen
 */
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashMap;

class Pair { 
	double x;
	double y;
	Pair( double x, double y) { 
		this.x = x;
		this.y = y;
	}
	public String toString() { return "(" + x + ", " + y + ")"; }
}

/**
 * General-purpose listener for the Frame and Canvas
 *
 */
class MyListener implements WindowListener, ComponentListener {
	OldGraph graph;
	Graphics G;
	
	//initialize
	MyListener(OldGraph graph, Graphics G) { 
		this.graph = graph;
		this.G = G;
	}
	
	//necessary to repaint the resized canvas
	public void componentResized(ComponentEvent e) {
		graph.paint(G);
 	}
	
	//if the window is closed, we're done
	public void windowClosing(WindowEvent e) {
		System.exit(0);
	}
	
	//unimplemented methods
	public void componentMoved(ComponentEvent e) {	}
	public void componentShown(ComponentEvent e) {}
	public void componentHidden(ComponentEvent e) {}
	public void windowOpened(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {} 
}

/**
 * To initialize the OldGraph, the function and its minimum and maximum 
 * domain values, its argList, and the frame all come from outside. Where the 
 * x and y axes meet on a regular graph is just an arbitrary point, which is why
 * the drawn axes of the graph meet at the mathematical center of the function,
 * with coordinates as the average of the largest and smallest domain values
 * and the average of the largest and smallest range values. 
 */
public class OldGraph extends Canvas {
	
	Frame frame; 
	Node func;//the function
	HashMap<String, Double> argList;
	ArrayList<Pair> points = new ArrayList<Pair>(); //the array of points to be rendered
	
	double xrange;
	double yrange;
	Pair origin;//the mathematical center of the function
	double Dmax;//the lowest value in the function's domain
	double Dmin;//the highest value in the function's domain
	double Rmax; //the lowest value in the function's range
	double Rmin;//the highest value in the function's domain
	private static double EPSILON = 1.0 * Math.pow(10, -3); 
	int BORDER_OFFSET = 2;
	
	OldGraph(Node func, HashMap<String, Double> argList, double Dmin, double Dmax) {
		super(); //initialization
		this.func = func; 
		frame = new Frame();
		this.argList = argList;
		this.Dmin = Dmin;
		this.Dmax = Dmax;
		
		frame.setSize(600,600);
		frame.setVisible(true);
		Graphics G = frame.getGraphics(); //frame automatically provides a graphics pallet. 
		 
		Rmin = Double.MAX_VALUE;	// Whatever's smaller becomes Rmin
		Rmax = Double.MIN_VALUE;	//Whatever's larger becomes Rmax
		
		xrange = Math.abs(Dmax - Dmin);	//used when scaling
		
		double n = xrange / getN(); 
		for (double i = Dmin; i<=Dmax+n; i+=n) { 
			argList.replace("x", new Double(i));
			if (Math.abs(i) > EPSILON ) { //prevent miniscule points from skewing the polyline
				try { 
					double val = func.eval(argList);
					if (val > Rmax) Rmax = val;
					if (val < Rmin) Rmin = val;
					Pair Point = new Pair(i, val); 
System.out.println("(x, y) = " + "(" + Point.x + ", " + Point.y + ")");
					points.add(Point);
				}
				catch(Exception e) { 
					e.printStackTrace();
				}
			}
		}
		yrange = Math.abs(Rmax - Rmin); //used in scaling
		origin = new Pair((Dmin + Dmax )/ 2, (Rmin + Rmax)/2); //mathematical average of the function
System.out.println("(Dmin, Dmax) = (" + Dmin + "," + Dmax + ")");
System.out.println("(Rmin, Rmax) = (" + Rmin + "," + Rmax + ")");
System.out.println("(origin.x, origin.y) = (" + origin.x + ", " + origin.y + ")");
				
		MyListener L = new MyListener(this, G);
		frame.addComponentListener(L); //register the resize listener with the graph
		frame.addWindowListener(L); //register the windowlistener with the frame
		
		frame.add(this); //put the graph into the frame
		
		paint(G); //paint the graph
	}
	
	/**
	 * Non-static because it may depend on characteristics of the graph
	 * @return however many points we want on our graph depending on the function and the frame
	 */
	private double getN() {
		// TODO Auto-generated method stub
		return 50;
	}
	
	/**
	 * The center of the screen(which is displayed upon rendering) is the mathematical center of the function,
	 * rather than the origin.
	 * 
	 * To calculate the x-scale:
	 * Imagine a horizontal number line with 5 coordinates positioned from left to right in the order listed: 
	 * the 0 of the frame, the minimum value of the domain, the maximum value of the domain, and the
	 * end of the frame(at coordinate F_WIDTH). For the sake of argument, let the minimum of the 
	 * domain be the negative of the maximum. Now imagine a point at Dmax. To put that point in it's 
	 * proper place(at F_WIDTH, because we want the point with the largest mathematical x-coordinate 
	 * at the end of the frame),we multiply it by F.WIDTH/2 and divide by Dmax. Assume that Dmax >0 and Dmin < 0. But 
	 * here 2*Dmax = | Dmax - Dmin|, giving us an expression for the x-scale factor.
	 * Similar arguments(based on the positional relationship of Dmin to the rendering 0 and Dmax to F_WIDTH) 
	 * can be made to show that the x-scale expression holds when the point's x-coordinate 
	 * is less than 0, and when the end-points of the domain aren't equal, and even when the 
	 * end-points of the domain are both positive
	 * 
	 * To calculate the y-scale: repeat the process to calculate the x-scale except use a vertical number 
	 * line, Rmin and Rmax, and F_HEIGHT. 
	 * 
	 * To compute a point's x-coordinate on the screen, take that point and translate it left(by the x-coordinate of 
	 * the function's mathematical origin, scale it by the x-scale factor, then translate it by the
	 * value of the Frame frame's vertical mid-line. To compute a point's y-coordinate on the screen, translate it down by the y-coordinate
	 * of the function's mathematical origin(the further down the screen the larger the y-coordinates, which is
	 * the reverse of the mathematical coordinate axes), scaled by the y-scaled factor, then translated up 
	 * by the value of the frame's horizontal mid-line. The final step in both cases allows the point of
	 * the mathematical center of the function(if it exists) to lie at the center of the frame.
	 *
	 * The expression for the spacing between the tick-marks is a heuristic:
	 * as the size of the screen increases, the number of tick-marks increases, and vice-versa. The exact number 
	 * of tick-marks on the x or y axis for a given screen size is merely a "nice amount" to the human eye. 
	 * The coordinates of the tick-marks are rounded down to one decimal point to minimize overlapping
	 * between tick-mark labels. The BORDEROFFSET prevents tick-marks from being drawn where we can't see them.
	 * 	
	 */
	public void paint(Graphics g) { 		
		int F_WIDTH = frame.getWidth();
		int F_HEIGHT = frame.getHeight();
		int n = points.size(); //number of points
		int[] Px = new int[n];	
		int[] Py = new int[n];
		
		//another identical version of the xscale = (P.x < origin.x) ? F_WIDTH / (2 * Math.abs(Dmin-origin.x)) : F_WIDTH/(2 * Math.abs(Dmax-origin.x));
		double xscale = F_WIDTH / xrange;
		double yscale = F_HEIGHT / yrange; 
		for (int i=0; i<n; i++) { 
			Pair P = points.get(i);
//System.out.println("(Dmin, Dmax) = (" + Dmin + "," + Dmax + ")");
//System.out.println("(Rmin, Rmax) = (" + Rmin + "," + Rmax + ")");
//System.out.println("(origin.x, origin.y) = (" + origin.x + ", " + origin.y + ")");			
//System.out.println("F_WIDTH = " + F_WIDTH);
//System.out.println("F_HEIGHT = " + F_HEIGHT);		
//System.out.println("xscale = " + xscale);
			double Gx = F_WIDTH/2 + (P.x - origin.x) * xscale; 
			double Gy = F_HEIGHT/2 + (origin.y - P.y) * yscale;			
		//	Gx = (Gx * F_WIDTH) / (2 * Math.abs(Dmax - (Dmax + Dmin) / 2) );
		//	Gy = (Gy * F_HEIGHT) / (2 * Math.abs(Rmax - (Rmax + Rmin) / 2) );
			Px[i] = (int) Gx; 
			Py[i] = (int) Gy;  //rounding
//System.out.println("(x, y) = " + "(" + x + ", " + y + ")");
		} 
		g.clearRect(0, 0, F_WIDTH, F_HEIGHT);
		g.setColor(Color.black);
		g.drawLine(0, F_HEIGHT/2, F_WIDTH, F_HEIGHT/2); //x-axis
		g.drawLine(F_WIDTH/2, 0, F_WIDTH/2, F_HEIGHT); //y-axis
		g.drawString("(" + roundDown(origin.x,1) + ", " + roundDown(origin.y,1) + ")" , F_WIDTH/2+10, F_HEIGHT/2-15);
		
		int xtickmarkspacing = 2400/F_WIDTH+1; //heuristics
		for (int j=BORDER_OFFSET; j<n; j=j+xtickmarkspacing) { 
			g.drawLine(Px[j], F_HEIGHT/2+10, Px[j], F_HEIGHT/2-10);	
			String str = new Double(roundDown(points.get(j).x,1)).toString();
			g.drawString(str, Px[j]-20, F_HEIGHT/2+20);
		} 
	/*	
	 * Tickmark rectangle created on the x-axis has endpoints at: 
	 * F_HEIGHT/2 + 20, F_HEIGHT/2 - 20
	 */
		int ytickmarkspacing = 2400/F_HEIGHT +1;
		for (int k=BORDER_OFFSET; k<n; k=k+ytickmarkspacing) { 
			if (Py[k]>F_HEIGHT/2+20 || Py[k]<F_HEIGHT/2-20) { //avoid x and y axes tickmark and tickmark label collisions
				g.drawLine(F_WIDTH/2 - 10, Py[k], F_WIDTH/2 + 10, Py[k]);
				String str = new Double(roundDown(points.get(k).y,1)).toString();
				g.drawString(str, F_WIDTH/2-50, Py[k]);
			}
		g.drawPolyline(Px, Py, n);
		}
	}
	/**
	 * 
	 * @param x, the double to be rounded, can be positive or negative
	 * @param places, the number of places to round the double to
	 * @return a new double, rounded down to a specified number of places. 
	 */
	public static double roundDown(double x, int places) {
		double frac = x>0 ? x - Math.floor(x) : -1 * (Math.floor(x) + 1 - x); // floor(-1.75) = -2, we want -1, so we add 1.
		double retval = x>0 ? Math.floor(x) : Math.floor(x) + 1; //same as above
		if (places == 1) frac = Math.floor(frac * 10) / 10; //deals with extra zeros in an example such 1 * .1
		else frac = Math.floor(frac * Math.pow(10, places)) * Math.pow(10, -places); //shift forward, append, shift back.
		return retval + frac; //put the whole and fractional parts together.
	}

	//test the program
	public static void main(String[] args) { 
        if (args.length < 1) System.out.println("ERROR: expecting a function");
		Tokenizer token = new Tokenizer(args[0]);
		HashMap<String, Double> argList = new HashMap<String, Double>();
		
		argList.put("x", new Double(Double.MIN_VALUE)); //need to initialize it to something
		
		Parser parser = new Parser(token);
		parser.root.print(0);
		
		OldGraph G = new OldGraph(parser.root, argList, -5, 5 );
		System.out.println();
	}
	
}
