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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

class GraphController implements ComponentListener, WindowListener, KeyListener, 
                                ItemListener, MouseListener, MouseMotionListener {

    Graph graph;
    TextField inputbar;
    TextField xminbar;
    TextField xmaxbar;
    TextField yminbar;
    TextField ymaxbar;
    TextField xincrementbar;
    TextField yincrementbar;

    String funcString = "";
    Function func;
    Frame frame;

    static double MOUSE_CURVE_PROXIMITY_FACTOR = 5; //pixels on screen 
    static double DRAG_SENSITIVITY = 1;

    Point target = new Point();

    int mx1;
    int my1;

    GraphController(Graph graph, Frame frame, TextField inputbar,TextField xminbar,
                     TextField xmaxbar, TextField yminbar, TextField ymaxbar,
                     TextField xincrementbar, TextField yincrementbar) {
        this.graph = graph;
        this.frame = frame;
        this.inputbar = inputbar;
        this.xminbar = xminbar;
        this.xmaxbar = xmaxbar;
        this.yminbar = yminbar;
        this.ymaxbar = ymaxbar;
        this.xincrementbar = xincrementbar;
        this.yincrementbar = yincrementbar;
        funcString = "";

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

            if ( !(xminbar.getText().equals("") ||
                xmaxbar.getText().equals("") ||
                yminbar.getText().equals("") ||
                ymaxbar.getText().equals("") || 
                xincrementbar.getText().equals("") ||
                yincrementbar.getText().equals("") ) ) { 

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
            
            if ( inputbar.getText().equals("") ) graph.setFunction(null);
            else { 
                if ( !(funcString.equals(inputbar.getText())) ){
                    graph.setFunction(new Function(inputbar.getText()));
                    funcString = inputbar.getText();
                }
                graph.computePoints();
            }

            graph.render();
            
        }
    }
   
    public void mouseMoved(MouseEvent e) {
         
        int mx = e.getX();
        int my = e.getY();

System.out.println("mx="+mx+", my="+my);

        if (graph.func != null) {
            double px = graph.renderXToMathematicalX(mx);
            double py = Double.MAX_VALUE;
            try {
                py = graph.func.value(px);
            }
            catch(Exception ex) { ex. printStackTrace(); }

            double diff = Math.abs( graph.mathematicalYToRenderY(py) - my );

            if ( diff < MOUSE_CURVE_PROXIMITY_FACTOR ) {
                graph.hoveringovercurve = true;
                graph.setLabeledPointCoordinates( px, py );
            }
            else { graph.hoveringovercurve = false; }
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
        double xshift = DRAG_SENSITIVITY * (graph.renderXToMathematicalX(mx1) - graph.renderXToMathematicalX(mx2));
        double yshift = DRAG_SENSITIVITY * (graph.renderYToMathematicalY(my1) - graph.renderYToMathematicalY(my2));
        mx1 = mx2;
        my1 = my2;
        graph.updateViewingWindow(xshift, yshift);
        graph.setLabeledPointCoordinates( graph.mathematicalXToRenderX(mx2), graph.mathematicalYToRenderY(my2) );
        if (graph.func != null) graph.computePoints();
        graph.render();
    }

    public void itemStateChanged(ItemEvent e) {
        int state = e.getStateChange();
        if (state == ItemEvent.SELECTED) {
        System.out.println("checkbox checked!");
            graph.setTickLabelsShowing(true); 
        }
        else {
            graph.setTickLabelsShowing(false); 
            System.out.println("checkbox unchecked");
        }
        graph.render();
    }

    public void windowClosing(WindowEvent e) {
        frame.dispose();
        System.exit(0);
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
    Label inputbarlabel = new Label();
    Label xminbarlabel = new Label();
    Label xmaxbarlabel = new Label();
    Label yminbarlabel = new Label();
    Label ymaxbarlabel = new Label();
    Label xincrementbarlabel = new Label();
    Label yincrementbarlabel = new Label();
    Label tickmarklabelscheckboxlabel = new Label();
    Checkbox tickmarklabelscheckbox = new Checkbox();

    Function func;
    HashMap<String, Double> argList;
    BufferStrategy strategy;

    int WIDTH; //canvas width
    int HEIGHT; //canvas height
    int RWIDTH; //width of render area
    int RHEIGHT; //height of render area

    boolean hoveringovercurve = false;
    boolean ticklabelsenabled = false;
    int targetRadius = 5;
    double lx;
    double ly;

    //initialized prior to the input of a function so that the axes can be drawn
    double xmin = -5; 
    double xmax = 5;
    double ymin = -5;
    double ymax = 5;
    double[] xpoints;
    double[] ypoints;
    double xrange = Math.abs(xmax - xmin);
    double yrange = Math.abs(ymax - ymin);
    double cx = 0;
    double cy = 0;
    double xincrement = 1;
    double yincrement = 1;

    static int n=10000;

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
    static final Color labeledpointcolor = Color.BLUE;

    NumberFormat nf = NumberFormat.getInstance();
    GraphController G;

    public Graph() {

        super();

        assembleFrame();

        WIDTH = this.getWidth();
        HEIGHT = this.getHeight();
        RWIDTH = WIDTH - 2 * HORIZONTAL_BORDER_OFFSET;
        RHEIGHT = HEIGHT - 2 * VERTICAL_BORDER_OFFSET;

        this.setIgnoreRepaint(true);
        this.createBufferStrategy(2);
        strategy = this.getBufferStrategy();

        G = new GraphController(this, frame,
                inputbar, xminbar, xmaxbar, yminbar, ymaxbar, xincrementbar, yincrementbar);

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
        frame.addWindowListener(G);

        render();
   }

   private void assembleFrame() {

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

        inputbarlabel.setText("  y=");
        bc.gridx = 0;
        bc.gridy = 0;
        bc.weightx = 0.0;
        bc.fill = GridBagConstraints.NONE; 
        //bc.anchor = GridBagConstraints.WEST;
        bottom.add(inputbarlabel, bc);
        inputbarlabel.setPreferredSize(new Dimension(20,20));

        bc.gridx = 1;
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
        //tickmarklabelscheckboxlabel.setPreferredSize(new Dimension(200,20));
        //tickmarklabelscheckboxlabel.setMinimumSize(new Dimension(50,20));
        
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
        ic.gridx = 3;
        ic.gridy = 1;
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

        frame.setSize(600,600);
        frame.setVisible(true);

   }

   public void setTickLabelsShowing(boolean ticklabelsenabled) { this.ticklabelsenabled = ticklabelsenabled; }

   public void setLabeledPointCoordinates(double lx, double ly) {
        this.lx = lx;
        this.ly = ly;
   }

   public void setXIncrement(double xincrement) { this.xincrement = xincrement; }
   public void setYIncrement(double yincrement) { this.yincrement = yincrement; }

   public void setFunction(Function func) {
       this.func = func;
       if (func != null) func.print();
   }

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

   public void computePoints() {
      xpoints = new double[n+1];
      ypoints = new double[n+1];
      for (int i = 0; i <= n; i++) {

            xpoints[i] = xmin + i * xrange / n;

            try {
                ypoints[i] = func.value(xpoints[i]);
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }

      }
      //System.out.println("xmin="+xmin+"\nxmax="+xmax+"\nymin="+ymin+"\nymax="+ymax);
      // for (int i=0; i<=n; i++) { System.out.println("xpoints[i]="+xpoints[i]+", ypoints[i]="+ypoints[i]); }
      //System.out.println(" | | | | | | | | |");
      /*for (int i=0; i<=n; i++) { System.out.println("xpoints[i]="+
                              renderXToMathematicalX( mathematicalXToRenderX(xpoints[i]) )+", ypoints[i]="+
                             renderYToMathematicalY( mathematicalYToRenderY(ypoints[i]) ) );  }  */
   }
   
   public void render() {
        do { 
            do {
                Graphics g = strategy.getDrawGraphics();
                paint(g);
                g.dispose();
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
      if (func != null) paintCurve(g);
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
                g.drawString(tickmarklabel, mathematicalXToRenderX(d) + HORIZONTAL_AXIS_LABELS_HORIZONTAL_OFFSET,
                             horizticky + HORIZONTAL_AXIS_LABELS_VERTICAL_OFFSET);
            }
        }

        for (double d = xincrement; d <= xmax ; d += xincrement   ) {
            g.drawLine(mathematicalXToRenderX(d), horizticky - TICKMARK_SIZE/2,
                       mathematicalXToRenderX(d), horizticky + TICKMARK_SIZE/2 );

            if (ticklabelsenabled) {
                String tickmarklabel = nf.format(d);
                g.drawString(tickmarklabel, mathematicalXToRenderX(d), horizticky + HORIZONTAL_AXIS_LABELS_VERTICAL_OFFSET);
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
            //System.out.println("yminstring.length()="+nf.format(ymin).length() );
            VERTICAL_AXIS_LABELS_HORIZONTAL_OFFSET = -15;
            againstfarleft = true; 
        }

        for (double d = -yincrement; d >= ymin ; d -= yincrement  ) {
            g.drawLine( verttickx - TICKMARK_SIZE/2, mathematicalYToRenderY(d),
                   verttickx + TICKMARK_SIZE/2, mathematicalYToRenderY(d) );
            
            if (ticklabelsenabled) {
                String tickmarklabel = nf.format(d);
                //System.out.println(tickmarklabel);
                int FAR_RIGHT_HORIZONTAL_OFFSET = againstfarleft ? -CHARACTER_WIDTH*tickmarklabel.length() : 0; 
                g.drawString(tickmarklabel, verttickx + VERTICAL_AXIS_LABELS_HORIZONTAL_OFFSET +
                            FAR_RIGHT_HORIZONTAL_OFFSET, mathematicalYToRenderY(d) + VERTICAL_AXIS_LABELS_VERTICAL_OFFSET);
            }
        }

        for (double d = yincrement; d <= ymax ; d += yincrement   ) {
            g.drawLine(verttickx - TICKMARK_SIZE/2, mathematicalYToRenderY(d),
                   verttickx + TICKMARK_SIZE/2, mathematicalYToRenderY(d) );
            
            if (ticklabelsenabled) {
                String tickmarklabel = nf.format(d);
                int FAR_RIGHT_HORIZONTAL_OFFSET = againstfarleft ? -CHARACTER_WIDTH*tickmarklabel.length() : 0; 
                g.drawString(tickmarklabel, verttickx + VERTICAL_AXIS_LABELS_HORIZONTAL_OFFSET +  
                            FAR_RIGHT_HORIZONTAL_OFFSET, mathematicalYToRenderY(d) + VERTICAL_AXIS_LABELS_VERTICAL_OFFSET);
            }
        }

    }

    public void paintCurve(Graphics g) {

            ArrayList<Integer> xcurrentinterval = new ArrayList<Integer>();
            ArrayList<Integer> ycurrentinterval = new ArrayList<Integer>();

            for (int i=0; i<=n; i++) {
                int rx = mathematicalXToRenderX( xpoints[i] );    
                int ry = mathematicalYToRenderY( ypoints[i] );    
                if ( !(func.isContinuous( xpoints[i] )) ) {
            //        System.out.println("discontinuily at x="+xpoints[i]);
                    g.drawPolyline(toIntArray(xcurrentinterval), toIntArray(ycurrentinterval), xcurrentinterval.size() );
                    xcurrentinterval.clear();
                    ycurrentinterval.clear();
                }
             //   else { System.out.println("function is continuous at x="+xpoints[i]); }
                xcurrentinterval.add(rx);
                ycurrentinterval.add(ry);
            }
            g.drawPolyline(toIntArray(xcurrentinterval), toIntArray(ycurrentinterval), xcurrentinterval.size() );

            if (hoveringovercurve) {
            
                g.setColor(labeledpointcolor);

                g.fillOval( mathematicalXToRenderX(lx) - targetRadius, mathematicalYToRenderY(ly) - targetRadius ,
                    2*targetRadius, 2*targetRadius);

                String targetstring = "("+nf.format(lx)+", "+nf.format(ly)+")";
                
                g.drawString(targetstring, mathematicalXToRenderX(lx) + TARGET_STRING_HORIZONTAL_OFFSET, 
                              mathematicalYToRenderY(ly) + TARGET_STRING_VERTICAL_OFFSET);
            }
    }
    

    public int mathematicalXToRenderX(double px) {
       	int rx = (int) (HORIZONTAL_BORDER_OFFSET + RWIDTH / 2.0 + (px - cx) * (RWIDTH / xrange) );
//System.out.println(gx);
        return rx;
    }

    public int mathematicalYToRenderY(double py) {
// System.out.println("HEIGHT="+HEIGHT+"\ncy="+cy+"\npy="+py+"\nyscale="+yscale+"\n");
        int ry = (int) (VERTICAL_BORDER_OFFSET + RHEIGHT / 2.0 + (cy - py) * (RHEIGHT / yrange) );
//System.out.println(gy);
        return ry;
    }

    public double renderYToMathematicalY(int ry) {
        double py = cy - (yrange / RHEIGHT) * (ry - VERTICAL_BORDER_OFFSET - RHEIGHT / 2.0 ); 
        return py;
    }

    public double renderXToMathematicalX(int rx) {
        double px = (xrange / RWIDTH) * (rx - HORIZONTAL_BORDER_OFFSET - RWIDTH / 2.0 ) + cx;
        return px;
    }

    public double canvasXToMathematicalX(int sx) {
        double px = (xrange / WIDTH) * (sx - WIDTH / 2.0 ) + cx;
        return px;
    }

    public double canvasYToMathematicalY(int sy) { 
        double py = cy - (yrange / HEIGHT) * (sy - HEIGHT / 2.0 ); 
        return py;
    }

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
                Thread.sleep(1);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
         }
     }

}
