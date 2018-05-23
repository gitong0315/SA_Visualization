/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sa_visualization;

import java.util.Random;

/**
 *
 * @author putu
 */
public class Node {
    private int x,y;
    private char label;
    private int index;

    public Node(char label, int width, int height, Random random) {
        this.label = label;
        this.index = label;
        this.index -=65;
        //randomly generate x and y
        int maxwidth = width - 30;
        int maxheight = height - 30;
        int min=30;
        this.x = random.nextInt(maxwidth + 1 - min) + min;
        this.y = random.nextInt(maxheight + 1 - min) + min;
        
        System.out.println("x: "+x+" y: "+y +" char:"+label+" idx: "+index);
    }
    
    
    
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public char getLabel() {
        return label;
    }

    public void setLabel(char label) {
        this.label = label;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
    
    
}
