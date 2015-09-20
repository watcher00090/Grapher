import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.text.NumberFormat;
import java.awt.*;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowListener;
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

class GraphController implements WindowListener, KeyListener, MouseMotionListener {
    
    OldGraph graph;
    TextField inputbar; 
    TextField xminbar; 
    TextField xmaxbar; 
    TextField yminbar; 
    TextField ymaxbar; 
    TextField xincrementbar;
    TextField yincrementbar;

    String funcString;
    Node func;   

    Point target = new Point();
 
    //initialize
    GraphController(OldGraph graph, TextField inputbar,TextField xminbar,
                     TextField xmaxbar, TextField yminbar, TextField ymaxbar,
                     TextField xincrementbar, TextField yincrementbar) { 
        this.graph = graph;
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
    
    public void keyPressed(KeyEvent e) { 
 
        Object s = e.getSource();
        char c = e.getKeyChar();
 
        if (c == KeyEvent.VK_ENTER) {
             
            if ( !(inputbar.getText() == ""||
                xminbar.getText() == "" ||
                xmaxbar.getText() == "" ||
                yminbar.getText() == "" ||
                ymaxbar.getText() == "" ||
                xincrementbar.getText() == "" ||
                yincrementbar.getText() == "") ) { 

                if ( !(funcString.equals(inputbar.getText())) ){ 
                    Tokenizer T = new Tokenizer(inputbar.getText());
                    Parser P = new Parser(T);
                    func = P.root;    
                    funcString = inputbar.getText();
                }
                
                double xmin = Double.parseDouble(xminbar.getText());
                double xmax = Double.parseDouble(xmaxbar.getText());
                double ymin=  Double.parseDouble(yminbar.getText());
                double ymax = Double.parseDouble(ymaxbar.getText());
                double xincrement = Double.parseDouble(xincrementbar.getText());
                double yincrement = Double.parseDouble(yincrementbar.getText());
                
                graph.update(func, xmin, xmax, ymin, ymax, xincrement, yincrement); 
            } 
        } 
 
    }

        public void mouseMoved(MouseEvent e) { 
            
            int mx = e.getX();
            int my = e.getY();
 
            if (graph.func != null) { 
             
                double px = graph.toPx(mx);
                double py = Double.MAX_VALUE;            

                try { 
                    graph.argList.replace("x", px);
                    py = graph.func.eval( graph.argList );
                }       
                catch(Exception ex) { 
                    ex. printStackTrace();
                }
           
//System.out.println("mx="+mx+" my="+my);
//System.out.println("px="+px+" py="+py+"\n");

System.out.println( graph.toGx(graph.toPx(mx)) + "\n" );
System.out.println( graph.toGy(graph.toPy(my)) );

                double diff =  Math.abs( graph.toGy(py) - my );  

System.out.println("diff="+diff+"\n");

                if ( diff < 15 ) {  

System.out.println("score!");

                    graph.pointhighlighted = true; 
                    graph.updateTarget( graph.toGx(px), graph.toGy(py) );
                }
    
                else { 
                    graph.pointhighlighted = false;
                }
                
                graph.repaint();
                
            }
    
    }

    public void windowClosing(WindowEvent e) {
        System.exit(0);
    }

    //unimplemented methods
    public void mouseDragged(MouseEvent e) {}
    
    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}
    
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

public class OldGraph extends Canvas {
    
    Frame frame; 
    Container container;
    Node func;
    HashMap<String, Double> argList;
   
    int WIDTH;
    int HEIGHT;
 
    boolean pointhighlighted = false;

    double xmax;
    double xmin;
    double ymax;
    double ymin;

    int targetRadius = 5;
    int tx;
    int ty;
 
    double[] xpoints;
    double[] ypoints;
    double xrange;
    double yrange;
    double xscale;
    double yscale;
    double cx; 
    double cy;
    double xincrement;
    double yincrement;
   
    static int n=1000;  

    ArrayList<Integer> asymptotexpoint1 = new ArrayList<Integer>();
    ArrayList<Integer> asymptotexpoint2 = new ArrayList<Integer>();
    ArrayList<Integer> asymptoteypoint1 = new ArrayList<Integer>();
    ArrayList<Integer> asymptoteypoint2 = new ArrayList<Integer>();    

    static int HORIZONTAL_BORDER_OFFSET = 15;
    static int VERTICAL_BORDER_OFFSET = 15;
    
    static int HORIZONTAL_AXIS_LABELS_VERTICAL_OFFSET = 25;
    static int HORIZONTAL_AXIS_LABELS_HORIZONTAL_OFFSET = 15;
    
    static int VERTICAL_AXIS_LABELS_VERTICAL_OFFSET = 0;
    static int VERTICAL_AXIS_LABELS_HORIZONTAL_OFFSET = 10;

    static int TICKMARK_SIZE = 10;

    TextField inputbar;   
    TextField xminbar;
    TextField xmaxbar;
    TextField yminbar;
    TextField ymaxbar;
    TextField xincrementbar;
    TextField yincrementbar;
    
    Label inputbarlabel;
    Label xminbarlabel;
    Label xmaxbarlabel;
    Label yminbarlabel;
    Label ymaxbarlabel;
    Label xincrementbarlabel;
    Label yincrementbarlabel;

    public OldGraph(HashMap<String, Double> argList) { 
        
        super();
        this.argList = argList; 

        frame = new Frame();
        container = new Container();
        inputbar = new TextField();
        xminbar = new TextField();
        xmaxbar = new TextField();
        yminbar = new TextField();
        ymaxbar = new TextField();
        xincrementbar = new TextField();
        yincrementbar = new TextField();       
 
        inputbarlabel = new Label();
        xminbarlabel = new Label();
        xmaxbarlabel = new Label();
        yminbarlabel = new Label();
        ymaxbarlabel = new Label();
        xincrementbarlabel = new Label();
        yincrementbarlabel = new Label();

        GraphController G = new GraphController(this, 
                inputbar, xminbar, xmaxbar, yminbar, ymaxbar, xincrementbar, yincrementbar);
        inputbar.addKeyListener(G);
        xminbar.addKeyListener(G);
        xmaxbar.addKeyListener(G);
        yminbar.addKeyListener(G);
        ymaxbar.addKeyListener(G);
        xincrementbar.addKeyListener(G);
        yincrementbar.addKeyListener(G);
    
        this.addMouseMotionListener(G);        

        frame.addWindowListener(G);
        
        assemble();

   }

   private void assemble() { 

        container.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
     
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0; 
        container.add(this, c);

        Panel bottom = new Panel();
        bottom.setLayout(new FlowLayout());       
 
        inputbarlabel.setText("y =");
        inputbarlabel.setPreferredSize(new Dimension(25,20));
        inputbar.setPreferredSize(new Dimension(100,20));   
        bottom.add(inputbarlabel);
        bottom.add(inputbar);
   
        xminbarlabel.setText("xmin =");
        bottom.add(xminbarlabel);
        bottom.add(xminbar);
        xminbarlabel.setPreferredSize(new Dimension(50,20));
        xminbar.setPreferredSize(new Dimension(50,20));

        xmaxbarlabel.setText("xmax =");
        bottom.add(xmaxbarlabel);
        bottom.add(xmaxbar);
        xmaxbarlabel.setPreferredSize(new Dimension(50,20));
        xmaxbar.setPreferredSize(new Dimension(50,20));
        
        yminbarlabel.setText("ymin =");
        bottom.add(yminbarlabel);
        bottom.add(yminbar);
        yminbarlabel.setPreferredSize(new Dimension(50,20));
        yminbar.setPreferredSize(new Dimension(50,20));
 
        ymaxbarlabel.setText("ymax =");
        bottom.add(ymaxbarlabel);
        bottom.add(ymaxbar);
        ymaxbarlabel.setPreferredSize(new Dimension(50,20));
        ymaxbar.setPreferredSize(new Dimension(50,20));
    
        xincrementbarlabel.setText("xincrement =");
        bottom.add(xincrementbarlabel);
        bottom.add(xincrementbar);
        xincrementbarlabel.setPreferredSize(new Dimension(70,50));
        xincrementbar.setPreferredSize(new Dimension(50,20));

        yincrementbarlabel.setText("yincrement =");
        bottom.add(yincrementbarlabel);
        bottom.add(yincrementbar);
        yincrementbarlabel.setPreferredSize(new Dimension(70,50));
        yincrementbar.setPreferredSize(new Dimension(50,20));

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;   //request any extra vertical space
        c.weighty = 0;
        c.gridx = 0;  
        c.gridy = 1;
        c.ipady = 30;
        //c.anchor = GridBagConstraints.PAGE_END; //bottom of space
        container.add(bottom, c);

        frame.add(container);
        frame.setSize(600,600);
        frame.setVisible(true);
        
    }
   
   public void updateTarget(int newtx, int newty) {  
        tx = newtx; 
        ty = newty;  
   } 
  
   public void update(Node func, double xmin, double xmax, double ymin, 
                        double ymax, double xincrement, double yincrement) {  

System.out.println("reached update");

       this.func = func;
       this.xmin = xmin;
       this.xmax = xmax;
       this.ymin = ymin;
       this.ymax = ymax;
       this.xincrement = xincrement;
       this.yincrement = yincrement;
       xrange = Math.abs(xmax - xmin);
       yrange = Math.abs(ymax - ymin);
       cx = (xmin + xmax)/2;
       cy = (ymin + ymax)/2;      
 
       computePoints();
       repaint();           
   }

   private void computePoints() { 
      xpoints = new double[n];
      ypoints = new double[n];
      for (int i=0; i<n; i=i+1) { 
            
            xpoints[i] = xmin + i * xrange / 1000; 
            
            argList.replace( "x", xpoints[i]);      
   
            try { 
                ypoints[i] = func.eval(argList);
            }
            
            catch(Exception ex) { 
                ex.printStackTrace();
            }

      }   
        argList.replace("x", Double.MIN_VALUE); 

     // func.print(0);
      
      //System.out.println("xmin="+xmin+"\nxmax="+xmax+"\nymin="+ymin+"\nymax="+ymax);
      
    //  for (int i=0; i<n; i++) { 
      //      System.out.println("xpoints[i]="+xpoints[i]+", ypoints[i]="+ypoints[i]);
     // }
      
   }
     
   public void paint(Graphics g) {         
        
      g.clearRect(0, 0, WIDTH, HEIGHT);
      
       if(func !=null) {

            WIDTH = this.getWidth();
            HEIGHT = this.getHeight();
            xscale = (WIDTH - 2 * HORIZONTAL_BORDER_OFFSET) / xrange;    
            yscale = (HEIGHT - 2 * VERTICAL_BORDER_OFFSET) / yrange;

System.out.println("inner repaint is called");
    
            int[] xgraphicspoints = new int[n];  
            int[] ygraphicspoints = new int[n];
           
            for (int i=0; i<n; i++) { 
                xgraphicspoints[i] = toGx(xpoints[i]);
                ygraphicspoints[i] = toGy(ypoints[i]);
            } 
            
            g.setColor(Color.black);
            g.drawPolyline(xgraphicspoints, ygraphicspoints, n); 
            
            if (pointhighlighted) {
                g.fillOval(tx - targetRadius, ty - targetRadius , 
                    2*targetRadius, 2*targetRadius);  
            }
               
            int hx1 = toGx(xmin); 
            int hx2 = toGx(xmax);
            int horizaxisy = HEIGHT - VERTICAL_BORDER_OFFSET;
            int xaxisy = toGy(0);
            
            if (VERTICAL_BORDER_OFFSET <= xaxisy && xaxisy <= horizaxisy) horizaxisy = xaxisy;

            g.drawLine(hx1, horizaxisy, hx2, horizaxisy);
          
            for (double d = 0.0; d >= xmin ; d -= xincrement   ) { 
                    g.drawLine(toGx(d), horizaxisy - TICKMARK_SIZE/2,
                               toGx(d), horizaxisy + TICKMARK_SIZE/2 );   
            }
            
            for (double d = 0.0; d <= xmax ; d += xincrement   ) { 
                    g.drawLine(toGx(d), horizaxisy - TICKMARK_SIZE/2, 
                               toGx(d), horizaxisy + TICKMARK_SIZE/2 );   
            }
            
            g.drawString(
            ( new Double(xmin) ).toString() ,  
            hx1 + HORIZONTAL_AXIS_LABELS_HORIZONTAL_OFFSET, 
            horizaxisy - HORIZONTAL_AXIS_LABELS_VERTICAL_OFFSET );
        
            g.drawString(
            ( new Double(xmax) ).toString() ,  
            hx2 - HORIZONTAL_AXIS_LABELS_HORIZONTAL_OFFSET, 
            horizaxisy - HORIZONTAL_AXIS_LABELS_VERTICAL_OFFSET );
                    
            int vy1 = toGy(ymin); 
            int vy2 = toGy(ymax);
            int vertaxisx = HORIZONTAL_BORDER_OFFSET;
            int yaxisx = toGx(0);
 
            if (vertaxisx < yaxisx && yaxisx <= WIDTH - HORIZONTAL_BORDER_OFFSET) { 
                vertaxisx = yaxisx;
            }

            g.drawLine(vertaxisx, vy1, vertaxisx, vy2);
          
            for (double d = 0.0; d >= ymin ; d -= yincrement   ) { 
                    g.drawLine(vertaxisx - TICKMARK_SIZE/2, toGy(d),
                               vertaxisx + TICKMARK_SIZE/2, toGy(d) ); 
                              
            }
            
            for (double d = 0.0; d <= ymax ; d += yincrement   ) { 
                    g.drawLine(vertaxisx - TICKMARK_SIZE/2, toGy(d),
                               vertaxisx + TICKMARK_SIZE/2, toGy(d) ); 
            }
            
            g.drawString(
            ( new Double(ymin) ).toString() ,  
            vertaxisx + VERTICAL_AXIS_LABELS_HORIZONTAL_OFFSET, 
            vy1 + VERTICAL_AXIS_LABELS_VERTICAL_OFFSET );
        
            g.drawString(
            ( new Double(ymax) ).toString() ,  
            vertaxisx + VERTICAL_AXIS_LABELS_HORIZONTAL_OFFSET, 
            vy2 - VERTICAL_AXIS_LABELS_VERTICAL_OFFSET );
        } 
    
    }
    
    public int toGx(double px) { 
        int gx = (int) (WIDTH/2 + (px - cx) * xscale) ;
        //System.out.println(gx);
        return gx; 
    }

    public double toPx(int gx) { 
        return cx + (gx - WIDTH/2) / xscale;
    }
    
    public int toGy(double py) { 
       // System.out.println("HEIGHT="+HEIGHT+"\ncy="+cy+"\npy="+py+"\nyscale="+yscale+"\n");
        int gy = (int) (HEIGHT/2 + (cy - py) * yscale);
        //System.out.println(gy);                 
        return gy;
    }

    public double toPy(int gy) { 
        return cy - (gy - HEIGHT/2) / yscale;
    }
    
    public static double sign(double d) { 
        return d/Math.abs(d);
    }

    public static double roundDown(double x, int places) {
        double frac = x>0 ? x - Math.floor(x) : -1 * (Math.floor(x) + 1 - x); // floor(-1.75) = -2, we want -1, so we add 1.
        double retval = x>0 ? Math.floor(x) : Math.floor(x) + 1; //same as above
        if (places == 1) frac = Math.floor(frac * 10) / 10; //deals with extra zeros in an example such 1 * .1
        else frac = Math.floor(frac * Math.pow(10, places)) * Math.pow(10, -places); //shift forward, append, shift back.
        return retval + frac; //put the whole and fractional parts together.
    }

    public static void main(String[] args) { 
        HashMap<String, Double> argList = new HashMap<String, Double>();
        
        argList.put("x", new Double(Double.MIN_VALUE)); //need to initialize it to something
        
        /*Parser parser = new Parser(token);
        parser.root.print(0);
        */
        OldGraph G = new OldGraph(argList );
    }
    
}
