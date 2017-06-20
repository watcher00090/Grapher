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
import java.util.ArrayList;
import java.util.HashMap;

class RenderController
implements ComponentListener, WindowListener, KeyListener, 
           ItemListener, MouseListener, MouseMotionListener, ActionListener {

    GraphCanvas graphCanvas;
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
    Checkbox ticklabelscheckbox;
    Checkbox compilecheckbox;
    Checkbox derivcheckbox;
    Checkbox nextmultiplescheckbox;

    String inputString = "";

    static double MOUSE_FUNCTION_PROXIMITY_FACTOR = 5; //pixels on screen 
    static double MOUSE_CURVE_PROXIMITY_FACTOR = .1; //mathematical distance
    static double DRAG_SENSITIVITY_COEFFICIENT = 1;

    Point target = new Point(Double.MAX_VALUE, Double.MIN_VALUE);

    //for dragging the function around
    int mx1;
    int my1;

    boolean nmthreadactive = false;

    public RenderController(GraphCanvas graphCanvas, Frame frame, TextField inputbar, 
                     TextField xminbar, TextField xmaxbar, 
                     TextField yminbar, TextField ymaxbar,
                     TextField xincrementbar, TextField yincrementbar, 
                     Button helpbutton, Dialog helpdialog,
                     Checkbox ticklabelscheckbox, Checkbox compilecheckbox,
                     Checkbox derivcheckbox, Checkbox nextmultiplescheckbox
                     ) {

        this.graphCanvas = graphCanvas;
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
        this.ticklabelscheckbox = ticklabelscheckbox;
        this.compilecheckbox = compilecheckbox;
        this.derivcheckbox = derivcheckbox;
        this.nextmultiplescheckbox = nextmultiplescheckbox;

        inputbar.setText("y^2-x^3+x-1");
        xminbar.setText("-5");
        xmaxbar.setText("5");
        yminbar.setText("-5");
        ymaxbar.setText("5");
        xincrementbar.setText("1");
        yincrementbar.setText("1");

        keyPressed(new KeyEvent(inputbar, KeyEvent.VK_ENTER, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, '\r'));
    }

    public void componentResized(ComponentEvent e) { 
        if (e.getSource().equals(frame)) {
            graphCanvas.WIDTH = graphCanvas.getWidth();
            graphCanvas.HEIGHT = graphCanvas.getHeight();
            graphCanvas.RWIDTH = graphCanvas.WIDTH - 2 * graphCanvas.HORIZONTAL_BORDER_OFFSET;
            graphCanvas.RHEIGHT = graphCanvas.HEIGHT - 2 * graphCanvas.VERTICAL_BORDER_OFFSET;
        }
        graphCanvas.render();
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {

            if (   !xminbar.getText().equals("")
                && !xmaxbar.getText().equals("")
                && !yminbar.getText().equals("")
                && !ymaxbar.getText().equals("") 
                && !xincrementbar.getText().equals("")
                && !yincrementbar.getText().equals("") ) { 

                double xmin = Double.parseDouble(xminbar.getText());
                double xmax = Double.parseDouble(xmaxbar.getText());
                double ymin=  Double.parseDouble(yminbar.getText());
                double ymax = Double.parseDouble(ymaxbar.getText());
                double xincrement = Double.parseDouble(xincrementbar.getText());
                double yincrement = Double.parseDouble(yincrementbar.getText());

                graphCanvas.updateViewingWindow(xmin, xmax, ymin, ymax);
                graphCanvas.setXIncrement(xincrement);
                graphCanvas.setYIncrement(yincrement);

                if (!(graphCanvas.getInputString().equals(inputbar.getText()))) { 
                    graphCanvas.updateRenderObject(inputbar.getText());                
                }
                graphCanvas.render();
            }
        }
    }
   
    public void mouseMoved(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();
        if (graphCanvas.func != null) { 
            double px = graphCanvas.renderXToMathematicalX(mx);
            double py = Double.MAX_VALUE;
            try {
                py = graphCanvas.func.value(px);
            }
            catch(Exception ex) { ex.printStackTrace(); }
            double diff = Math.abs(graphCanvas.mathematicalYToRenderY(py) - my);
            if (diff < MOUSE_FUNCTION_PROXIMITY_FACTOR) {
                graphCanvas.hovering = true;
                graphCanvas.setLabeledPointCoordinates(px, py);
            }
            else graphCanvas.hovering = false; 
        }
        if (graphCanvas.zls != null) { 
            try {
                double px = graphCanvas.renderXToMathematicalX(mx);
                double py = graphCanvas.renderYToMathematicalY(my);
                double val = graphCanvas.zls.lhsvalue(px, py);
                if (Math.abs(val) < MOUSE_CURVE_PROXIMITY_FACTOR) {
                    graphCanvas.hovering = true;
                    graphCanvas.setLabeledPointCoordinates(px, py);
                }
                else graphCanvas.hovering = false; 
            }
            catch(Exception ex) { ex.printStackTrace(); }
        }
        if (graphCanvas.ec != null) { 
            try {
                double px = graphCanvas.renderXToMathematicalX(mx);
                double py = graphCanvas.renderYToMathematicalY(my);
                double val = graphCanvas.ec.lhsvalue(px, py);
                if (Math.abs(val) < MOUSE_CURVE_PROXIMITY_FACTOR) {
                    graphCanvas.hovering = true;
                    graphCanvas.setLabeledPointCoordinates(px, py);
                }
                else graphCanvas.hovering = false; 
            }
            catch(Exception ex) { ex.printStackTrace(); }
        }
        graphCanvas.render();
    }
    
    public void mousePressed(MouseEvent e) {
         mx1 = e.getX();
         my1 = e.getY();
    }

    public void mouseClicked(MouseEvent e) {
         if (graphCanvas.ec != null) {
            double px = graphCanvas.renderXToMathematicalX(e.getX());
            double py = graphCanvas.renderYToMathematicalY(e.getY());
            graphCanvas.ec.basePoint = graphCanvas.ec.makePoint(px, py);
System.out.println("basepoint.x="+graphCanvas.ec.basePoint.x);
System.out.println("basepoint.y="+graphCanvas.ec.basePoint.y);
System.out.println();
            graphCanvas.ec.movingPoint = null;
            graphCanvas.ecpoints.clear();
            graphCanvas.ecpoints.add(graphCanvas.ec.basePoint);
            if (graphCanvas.colorprogression.size() < graphCanvas.ecpoints.size()) graphCanvas.colorprogression.add(graphCanvas.nextColor());
            graphCanvas.render();
         }
    }

    public void mouseDragged(MouseEvent e) {
        int mx2 = e.getX();
        int my2 = e.getY();

        double xshift = (graphCanvas.renderXToMathematicalX(mx1) - graphCanvas.renderXToMathematicalX(mx2));
        xshift *= DRAG_SENSITIVITY_COEFFICIENT;
        double yshift = (graphCanvas.renderYToMathematicalY(my1) - graphCanvas.renderYToMathematicalY(my2));
        yshift *= DRAG_SENSITIVITY_COEFFICIENT;

        mx1 = mx2;
        my1 = my2;

        graphCanvas.updateViewingWindow(xshift, yshift);
        graphCanvas.setLabeledPointCoordinates(graphCanvas.mathematicalXToRenderX(mx2),
                                               graphCanvas.mathematicalYToRenderY(my2));
        graphCanvas.render();
    }

    public void itemStateChanged(ItemEvent e) {
        int state = e.getStateChange();
        if (e.getSource().equals(ticklabelscheckbox)) {
            if (state == ItemEvent.SELECTED) {
                graphCanvas.setTickLabelsShowing(true); 
            }
            else {
                graphCanvas.setTickLabelsShowing(false); 
            }
            graphCanvas.render();
        }
        if (e.getSource().equals(compilecheckbox)) {
            if (state == ItemEvent.SELECTED) graphCanvas.setCompile(true);
            if (state == ItemEvent.DESELECTED) graphCanvas.setCompile(false);
        }
        if (e.getSource().equals(nextmultiplescheckbox)) { 
            if (state == ItemEvent.DESELECTED) {
                nmthreadactive = false;
            }
            if (state == ItemEvent.SELECTED) {
                nmthreadactive = true;
                if (graphCanvas.ec != null && graphCanvas.ec.basePoint != null) { 
                    Thread nmthread = new Thread() {
                        public void run() {
                            while (nmthreadactive) {
                                graphCanvas.ecpoints.add(graphCanvas.ec.nextMultiple());
                                if (graphCanvas.colorprogression.size() < graphCanvas.ecpoints.size()) 
                                    graphCanvas.colorprogression.add(graphCanvas.nextColor());
                                graphCanvas.render();
                                try {
                                    sleep(1000);
                                }
                                catch (InterruptedException e) {}
                            } 
                        }
                    };
                    nmthread.start();
                }
            }
        } 
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

public class GraphCanvas extends Canvas {

    Frame frame = new Frame();
    TextField inputbar = new TextField();
    TextField xminbar = new TextField(); 
    TextField xmaxbar = new TextField();
    TextField yminbar = new TextField();
    TextField ymaxbar = new TextField();
    TextField xincrementbar = new TextField();
    TextField yincrementbar = new TextField();
    Label inputbarlabel = new Label();
    Label xminbarlabel = new Label();
    Label xmaxbarlabel = new Label();
    Label yminbarlabel = new Label();
    Label ymaxbarlabel = new Label();
    Label xincrementbarlabel = new Label();
    Label yincrementbarlabel = new Label();
    Label ticklabelscheckboxlabel = new Label();
    Label compilecheckboxlabel = new Label();
    Label derivcheckboxlabel = new Label();
    Label nextmultiplescheckboxlabel = new Label();
    Checkbox ticklabelscheckbox = new Checkbox();
    Checkbox compilecheckbox = new Checkbox();
    Checkbox derivcheckbox = new Checkbox();
    Checkbox nextmultiplescheckbox = new Checkbox();
    Button helpbutton = new Button();
    Dialog helpdialog = new Dialog(frame);
    TextArea helptext = new TextArea();

    BufferStrategy strategy;

    int WIDTH; //canvas width
    int HEIGHT; //canvas height

    int RWIDTH; //render area width
    int RHEIGHT; //render area height

    boolean hovering = false;
    boolean ticklabelsenabled = false;
    static int targetRadius = 5;
    double lx;
    double ly;

    double xmin;
    double xmax;
    double ymin;
    double ymax;
    double xrange;
    double yrange;
    double cx;
    double cy;
    double xincrement;
    double yincrement;

    Function func = null;
    ZeroLevelSet zls = null; //non-ec ZeroLevelSet            
    EllipticCurve ec = null;            
    ArrayList<Point> ecpoints = new ArrayList<Point>();

    String inputString = "";

    boolean compile = false;

    static int NUM_POINTS = 10000;
    static double CURVE_TRACING_THRESHOLD = .1;

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
    RenderController G;
    int compiledfunctionnumber = 0;

    static String helpmessage = 
        "Simple java 2D Grapher\n\n"+
        "Can currently graph:\n"+
        "   1) Functions of x\n"+
        "   2) Summation and product expressions\n"+
        "   3) The zero level set of a function f(x, y).\n\n"+

        "For example,\n"+
        "   exp(-x^2/2)/sqrt(2*pi)\n"+
        "   ln(cos(.6535*x))\n"+
        "   sum(1/n^x,n,1,20)\n"+
        "   x^2 - y^2 - 1 (interpreted as x^2 - y^2 - 1 = 0).\n\n"+

        "Syntax for sum and product expressions:\n"+
        "   sum(expr, index variable, start index, limit index )\n"+
        "   prod(expr, index variable, start index, limit index )\n\n"+

        "We also have one special form which is a compiled representation\n"+
        "of the Riemann explicit formula for the func concentrated on prime powers:\n"+
        "   1 - 2*sum(cos(ln(x)*zeta_zero(n)), n, 1, 200) - 1/x(x^2-1)\n\n"+
        
        "The following constant expressions are recognized:\n"+
        "   e, pi, g, G, c\n\n" + 

        "The following built-in functions are recognized:\n"+
        "   sin, cos, exp, tan, sqrt, log, ln, abs, factorial, arcsin, arccos, arctan\n\n";

    ArrayList<Color> colorprogression = new ArrayList<Color>();

    int r_curr = 200;
    int g_curr = 100;
    int b_curr = 50;

    int r_i = 10;
    int g_i = -10;
    int b_i = 10;
        
    public GraphCanvas() {

        super();

        assembleLayout();

        WIDTH = this.getWidth();
        HEIGHT = this.getHeight();
        RWIDTH = WIDTH - 2 * HORIZONTAL_BORDER_OFFSET;
        RHEIGHT = HEIGHT - 2 * VERTICAL_BORDER_OFFSET;

        this.setIgnoreRepaint(true);
        this.createBufferStrategy(2);
        strategy = this.getBufferStrategy();

        G = new RenderController(this, frame, inputbar, 
                                 xminbar, xmaxbar, 
                                 yminbar, ymaxbar, 
                                 xincrementbar, yincrementbar, 
                                 helpbutton, helpdialog,    
                                 ticklabelscheckbox, compilecheckbox,
                                 derivcheckbox, nextmultiplescheckbox);

        inputbar.addKeyListener(G);
        xminbar.addKeyListener(G);
        xmaxbar.addKeyListener(G);
        yminbar.addKeyListener(G);
        ymaxbar.addKeyListener(G);
        xincrementbar.addKeyListener(G);
        yincrementbar.addKeyListener(G);
        ticklabelscheckbox.addItemListener(G);
        compilecheckbox.addItemListener(G);
        derivcheckbox.addItemListener(G);
        nextmultiplescheckbox.addItemListener(G);
        frame.addComponentListener(G);
        this.addMouseMotionListener(G);
        this.addMouseListener(G);
        helpbutton.addActionListener(G);
        helpdialog.addWindowListener(G);
        frame.addWindowListener(G);
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
        bottom.setPreferredSize(new Dimension(1500,80));
        frame.add(bottom, oc);

        //bottom layout; 
        GridBagConstraints bc = new GridBagConstraints();
        bottom.setLayout(new GridBagLayout());
        bc.ipadx = 10;
        bc.ipady = 0;

        inputbarlabel.setText("expr=");
        bc.gridx = 0;
        bc.gridy = 0;
        bc.fill = GridBagConstraints.NONE;
        bc.weightx = 0.0; 
        bc.insets = new Insets(0, 20, 0, 0);
        bottom.add(inputbarlabel, bc);

        //inputbar
        bc.gridx = 1;
        bc.gridy = 0;
        bc.fill = GridBagConstraints.HORIZONTAL;
        bc.weightx = 1.0; //request any extra horiz space
        bc.insets = new Insets(0, 0, 0, 0);
        bottom.add(inputbar, bc);

        Container tablecontainer = new Container();
        bc.gridx = 0;
        bc.gridy = 1;
        bc.gridwidth = 8;
        bc.gridheight = 2;
        bc.weightx = 0.0;
        bc.fill = GridBagConstraints.NONE; 
        bc.anchor = GridBagConstraints.WEST;
        bottom.add(tablecontainer, bc);
        tablecontainer.setPreferredSize(new Dimension(1000, 60));

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

        ticklabelscheckboxlabel.setText("ticklabels:");
        ic.gridx = 6;
        ic.gridy = 0;
        tablecontainer.add(ticklabelscheckboxlabel, ic);
        
        ic.gridx = 7;
        ic.gridy = 0;
        tablecontainer.add(ticklabelscheckbox, ic);

        compilecheckboxlabel.setText("compile:");
        ic.gridx = 8;
        ic.gridy = 0;
        tablecontainer.add(compilecheckboxlabel, ic);
        
        ic.gridx = 9;
        ic.gridy = 0;
        tablecontainer.add(compilecheckbox, ic);

        nextmultiplescheckboxlabel.setText("nextMultiples:");
        ic.gridx = 10;
        ic.gridy = 0;
        tablecontainer.add(nextmultiplescheckboxlabel, ic);
        
        ic.gridx = 11;
        ic.gridy = 0;
        tablecontainer.add(nextmultiplescheckbox, ic);
/*
        derivcheckboxlabel.setText("deriv:");
        ic.gridx = 10;
        ic.gridy = 0;
        tablecontainer.add(derivcheckboxlabel, ic);
        
        ic.gridx = 11;
        ic.gridy = 0;
        tablecontainer.add(derivcheckbox, ic);

*/
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
        ic.gridx = 6;
        ic.gridy = 1;
        ic.gridwidth = 3;
        tablecontainer.add(helpbutton, ic);
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
    public void setCompile(boolean compile) { this.compile = compile; }
    public String getInputString() { return inputString; }
    public void setInputString(String inputString) { this.inputString = inputString; }

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

    public void updateRenderObject(String str) {
        inputString = str;
System.out.println("inputString = " + inputString);
System.out.println("compile = " + compile);
        func = null;
        zls = null;
        ec = null;
        ecpoints.clear();
        if (!inputString.equals("")) {
            Parser P = new Parser(inputString);
            if (Node.isEC(P.root)) { 
                long[] ecparams = Node.getECparams(P.root);
                long A = ecparams[0];
                long B = ecparams[1];
                ec = new EllipticCurve(A, B);
            }
            else if (compile) {
                Compiler.compileFunction(P); 
                compiledfunctionnumber++;
                try {

                    Class c = Class.forName(Compiler.getClassName(P));
                    Function func = (Function) c.newInstance(); 
                    if (func.isBivariate()) zls = new ZeroLevelSet(func);
                    else this.func = func;
                    System.out.println("updated func");
                    
                } catch (ClassNotFoundException e2) {
                    e2.printStackTrace();
					System.out.println(e2.getMessage());
                } catch (IllegalAccessException e3) {
                    e3.printStackTrace();
                } catch (InstantiationException e4) {
                    e4.printStackTrace();
                } catch (SecurityException e5) {
                    e5.printStackTrace();
                } catch (Exception e6) {
                    e6.printStackTrace();
                } 
            }
            else {
                NonCompiledFunction func = new NonCompiledFunction(P.root, P.argList); 
                if (func.isBivariate()) zls = new ZeroLevelSet(func);
                else this.func = func;
            }
        }
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
        if (func != null) paintUnivariateFunc(func, g);
        if (zls != null) paintZeroLevelSet(zls, g); 
        if (ec != null) 
            paintZeroLevelSet(ec, g);
            paintECpoints(g);
        if (hovering) paintHoverDot(g);
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
    
    public void paintUnivariateFunc(Function func, Graphics g) {

            g.setColor(Color.BLACK);
            ArrayList<Integer> currxpoints = new ArrayList<Integer>();
            ArrayList<Integer> currypoints = new ArrayList<Integer>();

            try {
                for (int i = 0; i <= NUM_POINTS; i++) {

                    double x = xmin + i * xrange / NUM_POINTS;

                    if (   !func.isContinuous(x)
                         || mathematicalYToRenderY(func.value(x)) <= VERTICAL_BORDER_OFFSET
                         || mathematicalYToRenderY(func.value(x)) >= HEIGHT - VERTICAL_BORDER_OFFSET ) {
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

    public void paintHoverDot(Graphics g) {

        g.setColor(labeledpointcolor);

        g.fillOval( mathematicalXToRenderX(lx) - targetRadius,
                    mathematicalYToRenderY(ly) - targetRadius,
                    2*targetRadius, 2*targetRadius);

        String targetstring = "("+nf.format(lx)+", "+nf.format(ly)+")";
        
        g.drawString(targetstring,
                     mathematicalXToRenderX(lx) + TARGET_STRING_HORIZONTAL_OFFSET, 
                     mathematicalYToRenderY(ly) + TARGET_STRING_VERTICAL_OFFSET);
    }

    public void paintECpoints(Graphics g) {
        for (int i = 0; i < ecpoints.size(); i++) {
            Point p = ecpoints.get(i); 
            g.setColor(colorprogression.get(i));
            g.fillOval(mathematicalXToRenderX(p.x) - targetRadius, 
                       mathematicalYToRenderY(p.y) - targetRadius, 
                       2*targetRadius, 2*targetRadius);
        }
    }

    public void paintZeroLevelSet(ZeroLevelSet zls, Graphics g) {
        g.setColor(Color.BLACK);
        double tmp = 0;
        double xincr = 10*xrange/NUM_POINTS;
        double yincr = 10*yrange/NUM_POINTS;
        try {
            for (double x = xmin; x <= xmax; x += xincr) {
                for (double y = ymin; y <= ymax; y += yincr) {
                    double val = zls.lhsvalue(x, y);
                    if (val * tmp < 0 || val == 0) {
                        g.fillRect(mathematicalXToRenderX(x), 
                                   mathematicalYToRenderY(y), 1, 1);
                    }
                    tmp = val;
                }
                tmp = 0;
            }    
            for (double y = ymin; y <= ymax; y += yincr) {
                for (double x = xmin; x <= xmax; x += xincr) {
                    double val = zls.lhsvalue(x, y);
                    if (val * tmp < 0 || val == 0)  {
                        g.fillRect(mathematicalXToRenderX(x), 
                                   mathematicalYToRenderY(y), 1, 1);
                    }
                    tmp = val;
                }
                tmp = 0;
            }    
        }
        catch (Exception e) { e.printStackTrace(); }
    }

    public Color nextColor() {
        r_curr += r_i; 
        g_curr += g_i;
        b_curr += b_i;
        if (r_curr < 0 || r_curr > 255) {
            r_i *= -1;
            r_curr += r_i;
        }
        if (g_curr < 0 || g_curr > 255) {
            g_i *= -1;
            g_curr += g_i;
        }
        if (b_curr < 0 || b_curr > 255) {
            b_i *= -1;
            b_curr += b_i;
        }
        return new Color(r_curr, g_curr, b_curr);
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
        GraphCanvas g = new GraphCanvas();
     }

}
