import java.awt.GridBagLayout;
import java.awt.*;
import java.awt.Frame;
import java.awt.Button;
import java.awt.Canvas;

public class LayoutTest extends Button { 

    Frame frame = new Frame();
    Button inputbarlabel = new Button();
    Button inputbar = new Button();
    Button xminbar = new Button(); 
    Button xmaxbar = new Button();
    Button yminbar = new Button();
    Button ymaxbar = new Button();
    Button xincrementbar = new Button();
    Button yincrementbar = new Button();
    Button xminbarlabel = new Button();
    Button xmaxbarlabel = new Button();
    Button yminbarlabel = new Button();
    Button ymaxbarlabel = new Button();
    Button xincrementbarlabel = new Button();
    Button yincrementbarlabel = new Button();
    Button tickmarklabelscheckboxlabel = new Button();
    Button tickmarklabelscheckbox = new Button();
    Container tablecontainer = new Container();

    public LayoutTest() { 
    
        super();

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
        bottom.setPreferredSize(new Dimension(600,80));
        frame.add(bottom, oc);

       //bottom layout; 
        GridBagConstraints bc = new GridBagConstraints();
        bottom.setLayout(new GridBagLayout());

        inputbarlabel.setLabel("y=");
        bc.gridx = 0;
        bc.gridy = 0;
        bc.fill = GridBagConstraints.NONE; 
        bottom.add(inputbarlabel, bc);
        inputbarlabel.setPreferredSize(new Dimension(20,20));

        bc.fill = GridBagConstraints.HORIZONTAL;
        bc.gridx = 1;
        bc.gridy = 0;
        bc.weightx = 1.0; //request any extra horiz space
        bottom.add(inputbar, bc);

        Container tablecontainer = new Container();
        bc.gridx = 0;
        bc.gridy = 1;
        bc.gridwidth = 2;
        bc.weightx = 1.0;
        bc.fill = GridBagConstraints.BOTH; 
        bottom.add(tablecontainer, bc);
        tablecontainer.setPreferredSize(new Dimension(375, 50));

        //bottom table layout
        GridBagConstraints ic = new GridBagConstraints();
        tablecontainer.setLayout(new GridBagLayout());

        xminbarlabel.setLabel("xmin=");
        ic.gridx = 0;
        ic.gridy = 0;
        tablecontainer.add(xminbarlabel, ic);
        ic.gridx = 1;
        ic.gridy = 0;
        tablecontainer.add(xminbar, ic);
        xminbarlabel.setPreferredSize(new Dimension(50,20));
        xminbar.setPreferredSize(new Dimension(75,20));

        xmaxbarlabel.setLabel("xmax=");
        ic.gridx = 2;
        ic.gridy = 0;
        tablecontainer.add(xmaxbarlabel, ic);
        ic.gridx = 3;
        ic.gridy = 0;
        tablecontainer.add(xmaxbar, ic);
        xmaxbarlabel.setPreferredSize(new Dimension(50,20));
        xmaxbar.setPreferredSize(new Dimension(75,20));

        xincrementbarlabel.setLabel("xincr=");
        ic.gridx = 4;
        ic.gridy = 0;
        tablecontainer.add(xincrementbarlabel, ic);
        ic.gridx = 5;
        ic.gridy = 0;
        tablecontainer.add(xincrementbar, ic);
        xincrementbarlabel.setPreferredSize(new Dimension(50,20));
        //xincrementbar.setPreferredSize(new Dimension(75,20));

        yminbarlabel.setLabel("ymin=");
        ic.gridx = 0;
        ic.gridy = 1;
        tablecontainer.add(yminbarlabel, ic);
        ic.gridx = 1;
        ic.gridy = 1;
        tablecontainer.add(yminbar, ic);
        yminbarlabel.setPreferredSize(new Dimension(50,20));
        yminbar.setPreferredSize(new Dimension(75,20));

        ymaxbarlabel.setLabel("ymax=");
        ic.gridx = 2;
        ic.gridy = 1;
        tablecontainer.add(ymaxbarlabel, ic);
        ic.gridx = 3;
        ic.gridy = 1;
        tablecontainer.add(ymaxbar, ic);
        ymaxbarlabel.setPreferredSize(new Dimension(50,20));
        ymaxbar.setPreferredSize(new Dimension(75,20));

        yincrementbarlabel.setLabel("yincr=");
        ic.gridx = 4;
        ic.gridy = 1;
        tablecontainer.add(yincrementbarlabel, ic);
        ic.gridx = 5;
        ic.gridy = 1;
        tablecontainer.add(yincrementbar, ic);
        yincrementbarlabel.setPreferredSize(new Dimension(50,20));
        yincrementbar.setPreferredSize(new Dimension(75,20));

        frame.setSize(600,600);
        frame.setVisible(true);

//extra code

/*
        yminbarlabel.setLabel("ymin =");
        tablecontainer.add(yminbarlabel);
        tablecontainer.add(yminbar);
        yminbarlabel.setPreferredSize(new Dimension(50,20));
        yminbar.setPreferredSize(new Dimension(50,20));

        ymaxbarlabel.setLabel("ymax =");
        tablecontainer.add(ymaxbarlabel);
        tablecontainer.add(ymaxbar);
        ymaxbarlabel.setPreferredSize(new Dimension(50,20));
        ymaxbar.setPreferredSize(new Dimension(50,20));

        yincrementbarlabel.setLabel("yincrement =");
        tablecontainer.add(yincrementbarlabel);
        tablecontainer.add(yincrementbar);
        yincrementbarlabel.setPreferredSize(new Dimension(85,50));
        yincrementbar.setPreferredSize(new Dimension(50,20));

        tickmarklabelscheckboxlabel.setLabel("tickmarklabels:");
        tablecontainer.add(tickmarklabelscheckboxlabel);
        tablecontainer.add(tickmarklabelscheckbox);
        tickmarklabelscheckboxlabel.setPreferredSize(new Dimension(100,50));
        tickmarklabelscheckbox.setPreferredSize(new Dimension(25,30));
*/
    }

    public static void main(String[] args) {
        LayoutTest g = new LayoutTest();
        System.out.println("got here!");
    }

}
