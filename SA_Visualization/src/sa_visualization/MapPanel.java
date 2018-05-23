/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sa_visualization;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 *
 * @author putu
 */
public class MapPanel extends JPanel{
    BufferedImage backBuffer; 
    ArrayList<Node> nodeList;
    ArrayList<Node> current;
    ArrayList<Node> prev;
    int nodeSize = 6;
    int halfNodeSize = nodeSize/2;
    int panelSize = 600;
    public MapPanel(ArrayList<Node> nodeList) {
        
        //setPreferredSize(new Dimension(panelSize,panelSize));
        this.nodeList = nodeList;
        this.current = new ArrayList<>(nodeList);
        this.prev = new ArrayList<>(nodeList);
        //to prevent flickers
        backBuffer = new BufferedImage(panelSize, panelSize, BufferedImage.TYPE_INT_RGB); 
    }
    
    public void setCurrent(int[] x){
        prev = new ArrayList<>(current);
        current = new ArrayList<>();
        for (int i = 0; i < x.length; i++) {
            current.add(nodeList.get(x[i]));
        }
        repaint();
    }
    
    /*
    public void update()
    {
        //current.remove(0);
        int i=8;
        System.out.println("update map panel called");
    }
    */
    
    /** Custom rendering codes for drawing the JPanel */
    @Override
     public void paintComponent(Graphics g) {
      super.paintComponent(g);    // Paint background
      
      draw(g);
    }
    
    public void draw(Graphics g) {
        
        Graphics2D g2 = (Graphics2D)backBuffer.getGraphics();
        
        //draw to backbuffer g2
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                          RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(Color.white);
        g2.fillRect(0,0,panelSize,panelSize);
        g2.setColor(Color.red);
        g2.setStroke(new BasicStroke(3));
        int alpha = 255;
        for (int i = 0; i < current.size(); i++) {
            //draw prev solution
            g2.setColor(Color.lightGray);
            
            if(i<current.size()-1) {
                g2.drawLine(prev.get(i).getX(), prev.get(i).getY(), prev.get(i+1).getX(), prev.get(i+1).getY());
            }
            else {
                g2.drawLine(prev.get(i).getX(), prev.get(i).getY(), prev.get(0).getX(), prev.get(0).getY());
            }
        }
        for (int i = 0; i < current.size(); i++) {
            //draw current solution
            int a = alpha-(current.size()-i)*20;
            int b = current.size()-i*50;
            if(a>255)a=255;
            else if(a<0) a=0;
            if(b>255)b=255;
            else if(b<0) b=0;
            
            //System.out.println("a:"+a);
            Color linecolor = new Color(255, b, a);
            g2.setColor(linecolor);
            if(i<current.size()-1) {
                g2.drawLine(current.get(i).getX(), current.get(i).getY(), current.get(i+1).getX(), current.get(i+1).getY());
            }
            else {
                g2.drawLine(current.get(i).getX(), current.get(i).getY(), current.get(0).getX(), current.get(0).getY());
            }
        }
        g2.setColor(Color.black);
        g2.setFont(new Font("TimesRoman", Font.BOLD, 18)); 
        for (int i = 0; i < nodeList.size(); i++) {
            g2.fillRect(nodeList.get(i).getX()-halfNodeSize, nodeList.get(i).getY()-halfNodeSize, nodeSize, nodeSize);
            g2.drawString(String.valueOf(nodeList.get(i).getLabel()), nodeList.get(i).getX()-(nodeSize*4), nodeList.get(i).getY()+nodeSize);
        }
        
        g2.setColor(Color.black);
        g2.fillRect(current.get(0).getX()-nodeSize, current.get(0).getY()-nodeSize, nodeSize*2, nodeSize*2);
        
        //actualy draw to panel g
        g.drawImage(backBuffer, 0, 0, this);
    }
    
    
}
