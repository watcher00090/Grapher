import java.text.NumberFormat;
import java.awt.*;
import java.awt.Color;
import java.awt.image.BufferStrategy;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

class GraphController
implements ComponentListener, WindowListener, KeyListener, 
           ItemListener, MouseListener, MouseMotionListener, ActionListener {

    Graph graph;
    TextField inputbar;
    TextField xminbar;
    TextField xmaxbar;
    TextField yminbar;
    TextField ymaxbar;
    TextField xincrementbar;
    TextField yincrementbar;
    Button helpbutton;
    Dialog helpdialog;
    Frame frame;

    String str = "";

    static double MOUSE_CURVE_PROXIMITY_FACTOR = 5; //pixels on screen 
    static double DRAG_SENSITIVITY_COEFFICIENT = 1;

    Point target = new Point();

    //for dragging the curve around
    int mx1;
    int my1;

    GraphController(Graph graph, Frame frame, TextField inputbar,TextField xminbar,
                     TextField xmaxbar, TextField yminbar, TextField ymaxbar,
                     TextField xincrementbar, TextField yincrementbar, 
                     Button helpbutton, Dialog helpdialog) {
        this.graph = graph;
        this.frame = frame;
        this.inputbar = inputbar;
        this.xminbar = xminbar;
        this.xmaxbar = xmaxbar;
        this.yminbar = yminbar;
        this.ymaxbar = ymaxbar;
        this.xincrementbar = xincrementbar;
        this.yincrementbar = yincrementbar;
        this.helpbutton = helpbutton;
        this.helpdialog = helpdialog;

        inputbar.setText("x");
        xminbar.setText("-5");
        xmaxbar.setText("5");
        yminbar.setText("-5");
        ymaxbar.setText("5");
        xincrementbar.setText("1");
        yincrementbar.setText("1");
    }

    public void componentResized(ComponentEvent e) { 
        if (e.getSource().equals(frame)) {
            graph.WIDTH = graph.getWidth();
            graph.HEIGHT = graph.getHeight();
            graph.RWIDTH = graph.WIDTH - 2 * graph.HORIZONTAL_BORDER_OFFSET;
            graph.RHEIGHT = graph.HEIGHT - 2 * graph.VERTICAL_BORDER_OFFSET;
        }
        graph.render();
    }

    public void keyPressed(KeyEvent e) {

        if (e.getKeyChar() == KeyEvent.VK_ENTER) {

            if ( !(xminbar.getText().equals("")
                || xmaxbar.getText().equals("")
                || yminbar.getText().equals("")
                || ymaxbar.getText().equals("") 
                || xincrementbar.getText().equals("")
                || yincrementbar.getText().equals("") ) ) { 

                double xmin = Double.parseDouble(xminbar.getText());
                double xmax = Double.parseDouble(xmaxbar.getText());
                double ymin=  Double.parseDouble(yminbar.getText());
                double ymax = Double.parseDouble(ymaxbar.getText());
                double xincrement = Double.parseDouble(xincrementbar.getText());
                double yincrement = Double.parseDouble(yincrementbar.getText());

                graph.updateViewingWindow(xmin, xmax, ymin, ymax);
                graph.setXIncrement(xincrement);
                graph.setYIncrement(yincrement);
            }
            if (inputbar.getText().equals("") ) {
                graph.setFunction(null);
                graph.setCurve(null);
            }
            else { 
                if ( !(str.equals(inputbar.getText())) ) {
                    Parser P = new Parser(inputbar.getText());
                    Function func = new Function(inputbar.getText(), P.root, P.argList);
                    if (func.isMultivariable()) {
                        graph.setFunction(null);
                        graph.setCurve(new Curve(inputbar.getText(), func, func.argList));
System.out.println("Parsed a curve");
                    }
                    else {
                        graph.setCurve(null);
                        graph.setFunction(func);
                    }
                }
            }
            graph.render();
        }
    }
   
    public void mouseMoved(MouseEvent e) {

        int mx = e.getX();
        int my = e.getY();

        if (graph.func != null) {
            double px = graph.renderXToMathematicalX(mx);
            double py = Double.MAX_VALUE;
            try {
                py = graph.func.value(px);
            }
            catch(Exception ex) { ex.printStackTrace(); }

            double diff = Math.abs(graph.mathematicalYToRenderY(py) - my);

            if (diff < MOUSE_CURVE_PROXIMITY_FACTOR) {
                graph.hoveringoverfunction = true;
                graph.setLabeledPointCoordinates(px, py);
            }
            else { graph.hoveringoverfunction = false; }
        }
        graph.render();
    }
    
    public void mousePressed(MouseEvent e) {
         mx1 = e.getX();
         my1 = e.getY();
    }

    public void mouseDragged(MouseEvent e) {
        int mx2 = e.getX();
        int my2 = e.getY();

        double xshift = (graph.renderXToMathematicalX(mx1) - graph.renderXToMathematicalX(mx2));
        xshift *= DRAG_SENSITIVITY_COEFFICIENT;
        double yshift = (graph.renderYToMathematicalY(my1) - graph.renderYToMathematicalY(my2));
        yshift *= DRAG_SENSITIVITY_COEFFICIENT;

        mx1 = mx2;
        my1 = my2;
        graph.updateViewingWindow(xshift, yshift);
        graph.setLabeledPointCoordinates( graph.mathematicalXToRenderX(mx2),
                                          graph.mathematicalYToRenderY(my2) );
        graph.render();
    }

    public void itemStateChanged(ItemEvent e) {
        int state = e.getStateChange();
        if (state == ItemEvent.SELECTED) {
            graph.setTickLabelsShowing(true); 
        }
        else {
            graph.setTickLabelsShowing(false); 
        }
        graph.render();
    }

    public void windowClosing(WindowEvent e) {
        if (e.getSource() == frame) {
            frame.dispose();
            System.exit(0);
        }
        if (e.getSource() == helpdialog) {
            helpdialog.setVisible(false);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == helpbutton) { 
            System.out.println("helpbuttonpressed!");
            helpdialog.setSize(600,500);
            helpdialog.setVisible(true);
        }
    }

    //unimplemented methods
    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void componentHidden(ComponentEvent e) {}
    public void componentMoved(ComponentEvent e) {}
    public void componentShown(ComponentEvent e) {}
}

public class Graph extends Canvas {

    Frame frame = new Frame();
    TextField inputbar = new TextField();
    TextField xminbar = new TextField(); 
    TextField xmaxbar = new TextField();
    TextField yminbar = new TextField();
    TextField ymaxbar = new TextField();
    TextField xincrementbar = new TextField();
    TextField yincrementbar = new TextField();
    Label xminbarlabel = new Label();
    Label xmaxbarlabel = new Label();
    Label yminbarlabel = new Label();
    Label ymaxbarlabel = new Label();
    Label xincrementbarlabel = new Label();
    Label yincrementbarlabel = new Label();
    Label tickmarklabelscheckboxlabel = new Label();
    Checkbox tickmarklabelscheckbox = new Checkbox();
    Checkbox derivcheckbox = new Checkbox();
    Button helpbutton = new Button();
    Dialog helpdialog = new Dialog(frame);
    TextArea helptext = new TextArea();

    BufferStrategy strategy;

    int WIDTH; //canvas width
    int HEIGHT; //canvas height

    int RWIDTH; //render area width
    int RHEIGHT; //render area height

    boolean hoveringoverfunction = false;
    boolean ticklabelsenabled = false;
    int targetRadius = 5;
    double lx;
    double ly;

    //initialized prior to the input of a func so that the axes can be drawn
    double xmin = -15; 
    double xmax =  15;
    double ymin = -15;
    double ymax =  15;
    double xrange = Math.abs(xmax - xmin);
    double yrange = Math.abs(ymax - ymin);
    double cx = 0;
    double cy = 0;
    double xincrement = 1;
    double yincrement = 1;

    Function func = null;
    Curve curve = null;

    ArrayList<ArrayList<Integer>> xpoints = new ArrayList<ArrayList<Integer>>();
    ArrayList<ArrayList<Integer>> ypoints = new ArrayList<ArrayList<Integer>>();

    static int NUM_POINTS = 10000;
    static double CURVE_TRACING_THRESHOLD = .01;

    static final int TARGET_STRING_HORIZONTAL_OFFSET = 10;
    static final int TARGET_STRING_VERTICAL_OFFSET = 10;
    static final int HORIZONTAL_BORDER_OFFSET = 25;
    static final int VERTICAL_BORDER_OFFSET = 10;
    static final int HORIZONTAL_AXIS_LABELS_HORIZONTAL_OFFSET = -10;
    static final int VERTICAL_AXIS_LABELS_VERTICAL_OFFSET = 5;

    int HORIZONTAL_AXIS_LABELS_VERTICAL_OFFSET;
    int VERTICAL_AXIS_LABELS_HORIZONTAL_OFFSET;

    static final int TICKMARK_SIZE = 10; //in pixels
    static final int TICKMARK_LABELS_CHARACTER_HEIGHT = 8;
    static final int CHARACTER_WIDTH = 6;

    static final Color labeledpointcolor = Color.BLACK;

    NumberFormat nf = NumberFormat.getInstance();
    GraphController G;

    static String helpmessage = 
        "Simple java 2D Grapher\n\n"+
        "Can currently graph:\n"+
        "   1) Functions of x\n"+
        "   2) Summation and product expressions.\n\n"+

        "For example,\n"+
        "   exp(-x^2/2)/sqrt(2*pi)\n"+
        "   ln(cos(.6535*x))\n"+
        "   sum(1/n^x,n,1,20)\n\n"+

        "Syntax for sum and product expressions:\n"+
        "   sum(expr, index variable, start index, limit index )\n"+
        "   prod(expr, index variable, start index, limit index )\n\n"+

        "We also have one special form which is a compiled representation\n"+
        "of the Riemann explicit formula for the func concentrated on prime powers:\n"+
        "   1 - 2*sum(cos(ln(x)*zeta_zero(n)), n, 1, 200) - 1/x(x^2-1)\n\n"+
        
        "The following constant expressions are recognized:\n"+
        "   e, pi, g, G, c\n\n" + 

        "The following built-in funcs are recognized:\n"+
        "   sin, cos, exp, tan, sqrt, log, ln, abs, factorial, arcsin, arccos, arctan\n\n";

    public Graph() {

        super();

        assembleLayout();

        WIDTH = this.getWidth();
        HEIGHT = this.getHeight();
        RWIDTH = WIDTH - 2 * HORIZONTAL_BORDER_OFFSET;
        RHEIGHT = HEIGHT - 2 * VERTICAL_BORDER_OFFSET;

        this.setIgnoreRepaint(true);
        this.createBufferStrategy(2);
        strategy = this.getBufferStrategy();

        G = new GraphController(this, frame, inputbar, xminbar,
                                xmaxbar, yminbar, ymaxbar, xincrementbar, 
                                yincrementbar, helpbutton, helpdialog);

        inputbar.addKeyListener(G);
        xminbar.addKeyListener(G);
        xmaxbar.addKeyListener(G);
        yminbar.addKeyListener(G);
        ymaxbar.addKeyListener(G);
        xincrementbar.addKeyListener(G);
        yincrementbar.addKeyListener(G);
        tickmarklabelscheckbox.addItemListener(G);
        frame.addComponentListener(G);
        this.addMouseMotionListener(G);
        this.addMouseListener(G);
        helpbutton.addActionListener(G);
        helpdialog.addWindowListener(G);
        frame.addWindowListener(G);

        render();
    }

    private void assembleLayout() {

        frame.setLayout(new GridBagLayout());
        GridBagConstraints oc = new GridBagConstraints();

        oc.fill = GridBagConstraints.BOTH;
        oc.gridx = 0;
        oc.gridy = 0;
        oc.weightx = 1.0; 
        oc.weighty = 1.0;
        frame.add(this, oc);

        Container bottom = new Container();
        oc.fill = GridBagConstraints.HORIZONTAL;
        oc.gridx = 0;
        oc.gridy = 1;
        oc.weightx = 1.0;   //request any extra horizontal space
        oc.weighty = 0;
        bottom.setPreferredSize(new Dimension(640,80));
        frame.add(bottom, oc);

        //bottom layout; 
        GridBagConstraints bc = new GridBagConstraints();
        bottom.setLayout(new GridBagLayout());
        bc.ipadx = 10;
        bc.ipady = 0;

        bc.gridx = 0;
        bc.gridy = 0;
        bc.fill = GridBagConstraints.HORIZONTAL;
        bc.weightx = 1.0; //request any extra horiz space
        bottom.add(inputbar, bc);

        Container tablecontainer = new Container();
        bc.gridx = 0;
        bc.gridy = 1;
        bc.gridwidth = 6;
        bc.gridheight = 2;
        bc.weightx = 0.0;
        bc.fill = GridBagConstraints.NONE; 
        bc.anchor = GridBagConstraints.WEST;
        bottom.add(tablecontainer, bc);
        tablecontainer.setPreferredSize(new Dimension(590, 40));

        //bottom table layout
        GridBagConstraints ic = new GridBagConstraints();
        tablecontainer.setLayout(new GridBagLayout());

        xminbarlabel.setText("xmin=");
        ic.gridx = 0;
        ic.gridy = 0;
        tablecontainer.add(xminbarlabel, ic);
        ic.gridx = 1;
        ic.gridy = 0;
        tablecontainer.add(xminbar, ic);
        xminbarlabel.setPreferredSize(new Dimension(50,20));
        xminbar.setPreferredSize(new Dimension(100,20));
        xminbar.setMinimumSize(new Dimension(100,20));

        xmaxbarlabel.setText("xmax=");
        ic.gridx = 2;
        ic.gridy = 0;
        tablecontainer.add(xmaxbarlabel, ic);
        ic.gridx = 3;
        ic.gridy = 0;
        tablecontainer.add(xmaxbar, ic);
        xmaxbarlabel.setPreferredSize(new Dimension(50,20));
        xmaxbar.setPreferredSize(new Dimension(100,20));
        xmaxbar.setMinimumSize(new Dimension(100,20));

        xincrementbarlabel.setText("xincrement=");
        ic.gridx = 4;
        ic.gridy = 0;
        tablecontainer.add(xincrementbarlabel, ic);
        ic.gridx = 5;
        ic.gridy = 0;
        tablecontainer.add(xincrementbar, ic);
        xincrementbarlabel.setPreferredSize(new Dimension(100,20));
        xincrementbar.setPreferredSize(new Dimension(100,20));
        xincrementbar.setMinimumSize(new Dimension(100,20));

        tickmarklabelscheckboxlabel.setText("tickmarklabels:");
        ic.gridx = 6;
        ic.gridy = 0;
        tablecontainer.add(tickmarklabelscheckboxlabel, ic);
        
        ic.gridx = 7;
        ic.gridy = 0;
        tablecontainer.add(tickmarklabelscheckbox, ic);

        yminbarlabel.setText("ymin=");
        ic.gridx = 0;
        ic.gridy = 1;
        tablecontainer.add(yminbarlabel, ic);
        ic.gridx = 1;
        ic.gridy = 1;
        tablecontainer.add(yminbar, ic);
        yminbarlabel.setPreferredSize(new Dimension(50,20));
        yminbar.setPreferredSize(new Dimension(100,20));
        yminbar.setMinimumSize(new Dimension(100,20));

        ymaxbarlabel.setText("ymax=");
        ic.gridx = 2;
        ic.gridy = 1;
        tablecontainer.add(ymaxbarlabel, ic);
        ic.gridx = 3; ic.gridy = 1; 
        tablecontainer.add(ymaxbar, ic); 
        ymaxbarlabel.setPreferredSize(new Dimension(50,20));
        ymaxbar.setPreferredSize(new Dimension(100,20));
        ymaxbar.setMinimumSize(new Dimension(100,20));

        yincrementbarlabel.setText("yincrement=");
        ic.gridx = 4;
        ic.gridy = 1;
        tablecontainer.add(yincrementbarlabel, ic);
        ic.gridx = 5;
        ic.gridy = 1;
        tablecontainer.add(yincrementbar, ic);
        yincrementbarlabel.setPreferredSize(new Dimension(100,20));
        yincrementbar.setPreferredSize(new Dimension(100,20));
        yincrementbar.setMinimumSize(new Dimension(100,20));

        helpbutton.setLabel("help");
        ic.gridx = 5;
        ic.gridy = 1;
        ic.gridwidth = 3;
        ic.insets = new Insets(0, 30, 0, 0);
        tablecontainer.add(helpbutton, ic);
        helpbutton.setPreferredSize(new Dimension(50,20));
        helpbutton.setMinimumSize(new Dimension(50,20));
        ic.gridwidth = 1;

        helptext.insert(helpmessage, 0);
        helpdialog.add(helptext);

        frame.setSize(800,800);
        frame.setVisible(true);

    }

    public void setTickLabelsShowing(boolean ticklabelsenabled) {
        this.ticklabelsenabled = ticklabelsenabled;
    }

    public void setLabeledPointCoordinates(double lx, double ly) {
        this.lx = lx;
        this.ly = ly;
    }

    public void setXIncrement(double xincrement) { this.xincrement = xincrement; }
    public void setYIncrement(double yincrement) { this.yincrement = yincrement; }

    public void setFunction(Function func) { this.func = func; }
    public void setCurve(Curve curve) { this.curve = curve; }

    public void updateViewingWindow(double xmin, double xmax, double ymin, double ymax) {
        this.xmin = xmin;
        this.xmax = xmax;
        this.ymin = ymin;
        this.ymax = ymax;
        cx = (xmin + xmax)/2;
        cy = (ymin + ymax)/2;
        xrange = Math.abs(xmax - xmin);
        yrange = Math.abs(ymax - ymin);
    }

    public void updateViewingWindow(double xshift, double yshift) { 
        xmin = xmin + xshift;
        xmax = xmax + xshift;
        ymin = ymin + yshift;
        ymax = ymax + yshift;
        cx = (xmin + xmax)/2;
        cy = (ymin + ymax)/2;
        xrange = Math.abs(xmax - xmin);
        yrange = Math.abs(ymax - ymin);
    }

    public void render() {
        do { 
            do {
                Graphics g = strategy.getDrawGraphics();
                paint(g);
                g.dispose();
                try { 
                    Thread.sleep(1);
                }
                catch (Exception e) { 
                    e.printStackTrace();
                }
            }
            while (strategy.contentsRestored());
            strategy.show();
        }
        while (strategy.contentsLost());
    }

    public void paint(Graphics g) {
        g.clearRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.WHITE);
        g.fillRect(0,0, WIDTH, HEIGHT);
        paintAxesAndTickmarks(g);
        if (func != null) paintFunction(g);
        else if (curve != null) paintCurve(g);
    } 
   
    public void paintAxesAndTickmarks(Graphics g) {
        g.setColor(Color.BLACK);

        int xaxisy = mathematicalYToRenderY(0);
        int horizticky = xaxisy; 

        if ( VERTICAL_BORDER_OFFSET <= xaxisy && xaxisy <= HEIGHT - VERTICAL_BORDER_OFFSET) { 
            g.drawLine(mathematicalXToRenderX(xmin), xaxisy, mathematicalXToRenderX(xmax), xaxisy); 
            HORIZONTAL_AXIS_LABELS_VERTICAL_OFFSET = 20;
        }
        else if ( xaxisy < VERTICAL_BORDER_OFFSET ) {
            horizticky = VERTICAL_BORDER_OFFSET; 
            HORIZONTAL_AXIS_LABELS_VERTICAL_OFFSET = 20;
        }
        else { 
            horizticky = HEIGHT - VERTICAL_BORDER_OFFSET; 
            HORIZONTAL_AXIS_LABELS_VERTICAL_OFFSET = -10;
        }

        for (double d = 0; d >= xmin ; d -= xincrement ) {
            g.drawLine(mathematicalXToRenderX(d), horizticky - TICKMARK_SIZE/2,
                       mathematicalXToRenderX(d), horizticky + TICKMARK_SIZE/2 );
           
            if (ticklabelsenabled) { 
                String tickmarklabel = nf.format(d);
                g.drawString(tickmarklabel,
                             mathematicalXToRenderX(d) + HORIZONTAL_AXIS_LABELS_HORIZONTAL_OFFSET,
                             horizticky + HORIZONTAL_AXIS_LABELS_VERTICAL_OFFSET);
            }
        }

        for (double d = xincrement; d <= xmax ; d += xincrement   ) {
            g.drawLine(mathematicalXToRenderX(d), horizticky - TICKMARK_SIZE/2,
                       mathematicalXToRenderX(d), horizticky + TICKMARK_SIZE/2 );

            if (ticklabelsenabled) {
                String tickmarklabel = nf.format(d);
                g.drawString(tickmarklabel,
                             mathematicalXToRenderX(d),
                             horizticky + HORIZONTAL_AXIS_LABELS_VERTICAL_OFFSET);
            }
        }

        int yaxisx = mathematicalXToRenderX(0);
        int verttickx = yaxisx;
        boolean againstfarleft = false;

        if ( HORIZONTAL_BORDER_OFFSET <= yaxisx && yaxisx <= WIDTH - HORIZONTAL_BORDER_OFFSET) {
            g.drawLine(yaxisx, mathematicalYToRenderY(ymin), yaxisx, mathematicalYToRenderY(ymax));
            VERTICAL_AXIS_LABELS_HORIZONTAL_OFFSET = 15;
        }
        else if ( yaxisx < HORIZONTAL_BORDER_OFFSET) { 
            verttickx = HORIZONTAL_BORDER_OFFSET;
            VERTICAL_AXIS_LABELS_HORIZONTAL_OFFSET = 15;
        }
        else { 
            verttickx = WIDTH - HORIZONTAL_BORDER_OFFSET; 
            VERTICAL_AXIS_LABELS_HORIZONTAL_OFFSET = -15;
            againstfarleft = true; 
        }

        for (double d = -yincrement; d >= ymin ; d -= yincrement  ) {
            g.drawLine( verttickx - TICKMARK_SIZE/2, mathematicalYToRenderY(d),
                   verttickx + TICKMARK_SIZE/2, mathematicalYToRenderY(d) );
            
            if (ticklabelsenabled) {
                String tickmarklabel = nf.format(d);
                int FAR_RIGHT_HORIZONTAL_OFFSET =
                        againstfarleft ? -CHARACTER_WIDTH*tickmarklabel.length() : 0; 

                g.drawString(tickmarklabel,
                             verttickx + VERTICAL_AXIS_LABELS_HORIZONTAL_OFFSET
                                       + FAR_RIGHT_HORIZONTAL_OFFSET,
                             mathematicalYToRenderY(d) + VERTICAL_AXIS_LABELS_VERTICAL_OFFSET);
            }
        }

        for (double d = yincrement; d <= ymax ; d += yincrement   ) {
            g.drawLine(verttickx - TICKMARK_SIZE/2, mathematicalYToRenderY(d),
                   verttickx + TICKMARK_SIZE/2, mathematicalYToRenderY(d) );
            
            if (ticklabelsenabled) {
                String tickmarklabel = nf.format(d);
                int FAR_RIGHT_HORIZONTAL_OFFSET = againstfarleft ? -CHARACTER_WIDTH*tickmarklabel.length() : 0; 
                g.drawString(tickmarklabel,
                             verttickx + VERTICAL_AXIS_LABELS_HORIZONTAL_OFFSET +  
                                         FAR_RIGHT_HORIZONTAL_OFFSET,
                             mathematicalYToRenderY(d) + VERTICAL_AXIS_LABELS_VERTICAL_OFFSET);
            }
        }
    }
    
    public void paintFunction(Graphics g) {

        if (func.isFunctionOfX()) { 

System.out.println("found function of x");

            g.setColor(Color.BLACK);
            ArrayList<Integer> currxpoints = new ArrayList<Integer>();
            ArrayList<Integer> currypoints = new ArrayList<Integer>();

            try {
                for (int i = 0; i <= NUM_POINTS; i++) {

                    double x = xmin + i * xrange / NUM_POINTS;

                    if ( !func.isContinuous(x) ||
                         mathematicalYToRenderY(func.value(x)) <= VERTICAL_BORDER_OFFSET ||
                         mathematicalYToRenderY(func.value(x)) >= HEIGHT - VERTICAL_BORDER_OFFSET ) {
                            g.drawPolyline(toIntArray(currxpoints), toIntArray(currypoints), currxpoints.size());
                            currxpoints.clear();
                            currypoints.clear();
                    }
                    else {
                        currxpoints.add( mathematicalXToRenderX(x) );
                        currypoints.add( mathematicalYToRenderY(func.value(x)) );
                    }
                } 
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            g.drawPolyline(toIntArray(currxpoints), toIntArray(currypoints), currxpoints.size());
            currxpoints.clear();
            currypoints.clear();

        }
        else if (func.isFunctionOfY()) { 
            
//System.out.println("found function of y");
//System.out.println();
//System.out.println("ymin = " + ymin);
//System.out.println("ymax = " + ymax);
//System.out.println("yrange = " + yrange);
 
            g.setColor(Color.BLACK);
            ArrayList<Integer> currxpoints = new ArrayList<Integer>();
            ArrayList<Integer> currypoints = new ArrayList<Integer>();

            try {
                for (int i = 0; i <= NUM_POINTS; i++) {

                    double y = ymin + i * yrange / NUM_POINTS;
            
//System.out.println("y = " + y);
                    
                    if ( !func.isContinuous(y) ||
                         mathematicalXToRenderX(func.value(y)) <= HORIZONTAL_BORDER_OFFSET ||
                         mathematicalXToRenderX(func.value(y)) >= WIDTH - HORIZONTAL_BORDER_OFFSET ) {

                            g.drawPolyline(toIntArray(currxpoints), toIntArray(currypoints), currxpoints.size());
                            currxpoints.clear();
                            currypoints.clear();
                    }
                    else {
                        currypoints.add( mathematicalYToRenderY(y) );
                        currxpoints.add( mathematicalXToRenderX(func.value(y)) );
                    }
                } 
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            g.drawPolyline(toIntArray(currxpoints), toIntArray(currypoints), currxpoints.size());
            currxpoints.clear();
            currypoints.clear();

        }

        if (hoveringoverfunction) {
            g.setColor(labeledpointcolor);

            g.fillOval( mathematicalXToRenderX(lx) - targetRadius,
                        mathematicalYToRenderY(ly) - targetRadius ,
                        2*targetRadius, 2*targetRadius);

            String targetstring = "("+nf.format(lx)+", "+nf.format(ly)+")";
            
            g.drawString(targetstring,
                         mathematicalXToRenderX(lx) + TARGET_STRING_HORIZONTAL_OFFSET, 
                         mathematicalYToRenderY(ly) + TARGET_STRING_VERTICAL_OFFSET);
        }

    }

    public void paintCurve(Graphics g) {
        g.setColor(Color.BLACK);
        try {
            for (int rx = HORIZONTAL_BORDER_OFFSET; rx <= WIDTH - HORIZONTAL_BORDER_OFFSET ; rx++) {
                for (int ry = VERTICAL_BORDER_OFFSET; ry <= HEIGHT - VERTICAL_BORDER_OFFSET ; ry++) {
                    double px = renderXToMathematicalX(rx);
                    double py = renderYToMathematicalY(ry);
//System.out.println("(" + px + ", " + py + ")"); 
//System.out.println("curve.value(" + px + ", " + py + ") = " + curve.value(px, py));
//System.out.println();
                    if (Math.abs(curve.value(px, py)) <= CURVE_TRACING_THRESHOLD) {
                        g.fillOval(rx, ry, 2, 2);
                    }
                }
            }    
        }
        catch (Exception e) { e.printStackTrace(); }
    }
   
   //convert from mathematical coordinates to coordinates on the canvas
    public int mathematicalXToRenderX(double px) {
        int rx = (int) (HORIZONTAL_BORDER_OFFSET + RWIDTH / 2.0 + (px - cx) * (RWIDTH / xrange) );
        return rx;
    }

    public int mathematicalYToRenderY(double py) {
        int ry = (int) (VERTICAL_BORDER_OFFSET + RHEIGHT / 2.0 + (cy - py) * (RHEIGHT / yrange) );
        return ry;
    }

    //convert back
    public double renderYToMathematicalY(int ry) {
        double py = cy - (yrange / RHEIGHT) * (ry - VERTICAL_BORDER_OFFSET - RHEIGHT / 2.0 ); 
        return py;
    }

    public double renderXToMathematicalX(int rx) {
        double px = (xrange / RWIDTH) * (rx - HORIZONTAL_BORDER_OFFSET - RWIDTH / 2.0 ) + cx;
        return px;
    }
    
    //helper
    public int[] toIntArray(ArrayList<Integer> A) {
        int[] ret = new int[A.size()];
        for (int i=0; i<A.size(); i++) {
            ret[i] = A.get(i);
        }
        return ret;
    }

    public static void main(String[] args) {
        Graph g = new Graph();
        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()) {
            System.out.println("> ");
            String text = sc.nextLine();
            g.inputbar.setText(text);
            g.G.keyPressed( new KeyEvent(g,
                            KeyEvent.KEY_PRESSED,
                            System.currentTimeMillis(),
                            0,
                            KeyEvent.VK_ENTER,
                            '\n' ));
            try { 
                Thread.sleep(500);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
         }
     }

}
