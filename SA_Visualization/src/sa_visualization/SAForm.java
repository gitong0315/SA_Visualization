/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sa_visualization;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author putu
 */
public class SAForm extends javax.swing.JFrame {
    private int framecount = 0;
    private Thread simThread = null;
    boolean isRunning = true; 
    boolean isPause = false; 
    private int framesSkipped;
    private final static int MAX_FPS = 32; //desired fps
    private final static int FRAME_PERIOD = 1000 / MAX_FPS; // the frame period
    private final static int MAX_FRAME_SKIPS = 5;
    MapPanel panel;
    private int speed;
    private ArrayList<Node> nodeList;
    private ArrayList<String> permutationList;
    private int movement = -1;
    private boolean ready = false;//indicate we can start our animation (all initial calculation is done)
    private Icon playIcon;
    private Icon pauseIcon;
    private Icon nextIcon;        
    private JButton buttonStart;
    private Random rand;
    
    //sa parameter
    private float t;
    private float a = 0.9f;
    private float tMin = 1.0f;
    private float t0 = 500.0f;
    private int iteration = 0;
    private int maxIteration = 50;
    private float fBest;
    private float objX;
    private float objY;
    private int[] xBest;
    private int[] x; // current solution representation;
    private int[] y; // new solution representation;
    private float[][] distanceMatrix;
    JLabel current;
    JLabel prev;
    JLabel move;
            
    /**
     * Creates new form SAForm
     */
    public SAForm(ArrayList<Node> nodeList, float[][] distanceMatrix, ArrayList<String> permutationList) {
        //initComponents();
        rand = new Random();
        
        this.distanceMatrix = distanceMatrix;
        this.nodeList = nodeList;
        this.permutationList = permutationList;
        
        // create a basic JFrame
        setDefaultLookAndFeelDecorated(true);
        setSize(600,730);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        panel = new MapPanel(nodeList);
        JPanel panelTop = new JPanel();
        JPanel panelBottom = new JPanel();
        JPanel panelMedia = new JPanel();
        
        BoxLayout boxlayout = new BoxLayout(panelTop, BoxLayout.Y_AXIS);
        panelTop.setLayout(boxlayout);
        
        BoxLayout boxlayout2 = new BoxLayout(panelBottom, BoxLayout.Y_AXIS);
        panelBottom.setLayout(boxlayout2);
        
        BoxLayout boxlayoutMedia = new BoxLayout(panelMedia, BoxLayout.X_AXIS);
        panelMedia.setLayout(boxlayoutMedia);
        
        current = new JLabel("<best solution>");
        prev = new JLabel("<current solution>");
        move = new JLabel("<f(x)>");
        
        current.setVisible(false);
        
        JSlider slider = new JSlider(1, 8);
        slider.setValue(1);
        speed=1;
        slider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    speed = ((JSlider)e.getSource()).getValue();
                    System.out.println("speed:"+speed);
                }
        });       
        
        playIcon = new ImageIcon("image/Play16.gif");
        nextIcon = new ImageIcon("image/StepForward16.gif");
        pauseIcon = new ImageIcon("image/Pause16.gif");
        
        
        buttonStart = new JButton(playIcon);
        JButton buttonNext = new JButton(nextIcon);
        
        buttonNext.setVisible(false);
        buttonStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!ready){
                    JOptionPane.showMessageDialog(SAForm.this,"Calculating, please wait.");
                    return;
                }
                //calculate initial solution on button click
                
                if(simThread==null){
                    createRandomInitialSolution();    
                    simStart();
                    System.out.println("start action");
                    buttonStart.setIcon(pauseIcon);
                }
                else{
                    //pause
                    isPause = !isPause;
                    if(isPause){
                        buttonStart.setIcon(playIcon);
                    }
                    else{
                        buttonStart.setIcon(pauseIcon);
                    }
                }
            }
        });
        
        current.setAlignmentX(Component.CENTER_ALIGNMENT);
        prev.setAlignmentX(Component.CENTER_ALIGNMENT);
        move.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        slider.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panelTop.add(current);
        panelTop.add(prev);
        panelTop.add(move);
        
        panelBottom.add(slider);
        panelBottom.add(panelMedia);
        
        panelMedia.add(buttonStart);
        panelMedia.add(buttonNext);
        
        // add panel to main frame
        add(panelTop, BorderLayout.PAGE_START);
        add(panel, BorderLayout.CENTER);
        add(panelBottom, BorderLayout.PAGE_END);
        
        setLocationRelativeTo(null);
        
        new Thread(){
            public void run() {
                DataForm dataForm = new DataForm(permutationList, distanceMatrix, SAForm.this);
                dataForm.setDefaultLookAndFeelDecorated(true);
                dataForm.setVisible(true);
            }
        }.start();
                
    }
    
    public void setReady(){
        ready = true;
    }
    
    public void simStart() {
      // Run the game logic in its own thread.
        simThread = new Thread() {
            public void run() {
            
                while(isRunning){
                    if(!isPause){
                        //System.out.println(".run()");
                        long started = System.currentTimeMillis();
                        framesSkipped = 0;

                        update();
                        panel.repaint();

                        float deltaTime = (System.currentTimeMillis() - started);

                        int sleepTime = (int) (FRAME_PERIOD - deltaTime);

                        if (sleepTime > 0) {
                            try {
                              Thread.sleep(sleepTime);
                            }
                            catch (InterruptedException e) {
                            }
                        }

                        while (sleepTime < 0 && framesSkipped < MAX_FRAME_SKIPS) {
                            // we need to catch up
                            // update without rendering
                            update();

                            // add frame period to check if in next frame
                            sleepTime += FRAME_PERIOD;
                            framesSkipped++;
                            System.out.println("catch up skipped frame");
                        }

                        framecount++;
                        
                        /*
                        if(framecount > (runTime * MAX_FPS)){//(Constant.RUN_TIME_15MINUTE_IN_SECOND * MAX_FPS)){
                            simStop(true, false);
                            //System.out.println("15 minute has past");
                        }
                        */
                    }
                    System.out.print("");
                    
                    
                }
                
                //show best 
                panel.setCurrent(xBest);
                panel.repaint();
                String currentSol = "";
                for (int i = 0; i < nodeList.size(); i++) {
                    currentSol+= (char)(65+xBest[i])+" - ";
                }
                currentSol += (char)(65+xBest[0]);

                prev.setText(currentSol);
                
                 //System.out.println(".run()  BYE");
                String Sol = "";
                for (int i = 0; i < nodeList.size(); i++) {
                    Sol+= (char)(65+xBest[i])+" - ";
                }
                Sol+= (char)(65+xBest[0]);
                JOptionPane.showMessageDialog(SAForm.this,"We finished. BEST ROUTE = "+Sol+" with f(x) = "+fBest);
                buttonStart.setEnabled(false);
            }
        };
      
      simThread.start();  // Invoke run()
    }
    
    private void updateNewSolution(){
        
        String currentSol = "";
        String workingSol = "";
        for (int i = 0; i < nodeList.size(); i++) {
            currentSol+= (char)(65+xBest[i])+" - ";
            workingSol+= (char)(65+x[i])+" - ";
        }
        currentSol += (char)(65+xBest[0]);
        workingSol += (char)(65+x[0]);
        
        current.setText(currentSol);
        prev.setText(workingSol);
        String movestr="";
        switch(movement){
            case 0:
                movestr = "SWAP";
                break;
            case 1:
                movestr = "INSERT";
                break;
            case 2:
                movestr = "REVERSE";
                break;
        }
                
        move.setText(String.valueOf("f(x): "+fBest));
        invalidate();
        
        panel.setCurrent(x);
    }
    
    private void createRandomInitialSolution(){
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < nodeList.size(); i++) {
            list.add(i);
        }
        java.util.Collections.shuffle(list);
        
        x = new int[nodeList.size()];
        y = new int[nodeList.size()];
        xBest = new int[nodeList.size()];
        for (int i = 0; i < nodeList.size(); i++) {
            x[i]=list.get(i);
        }
        
        String currentSol = "";
        for (int i = 0; i < nodeList.size(); i++) {
            currentSol+= (char)(65+x[i])+" - ";
        }
        currentSol = currentSol.substring(0, currentSol.length()-2);
        current.setText(currentSol);
            
        movement++;
        if(movement>2)movement=0;
        updateNewSolution();
        
        //calculate initial obj value
        objX = calculateObjectiveFunction(x);
	fBest = objX;
        System.arraycopy(x, 0, xBest, 0, x.length);
        t = t0;
    }
    
    private void update()
    {
        
        if(framecount%(1*speed*speed)==0){ //for delay (speed control)
            //===== SA
            
            //Because we cant have nesting loop inside our animation loop,
            //we use logical loop for inner loop by using if to wait until inner loop 
            //itterate and update the temperature
                        
            float p = rand.nextFloat();
            if (p < 0.33) {
                    //swap 0-0,33
                    swap();
                    movement=0;
            }
            else if (p > 0.66) {
                    //reversion 0.66-1
                    reversion();
                    movement=2;
            }
            else {
                    //insertion 0.33-0.66
                    insertion();
                    movement=1;
            }
                        
            iteration += 1;
            
            objY = calculateObjectiveFunction(y);
            
            float delta = objY - objX ;

            if ( delta <= 0 ) {
                System.arraycopy( y, 0, x, 0, y.length);
                objX = objY;
                //System.out.println("better");
            }
            else {
                //System.out.println("worse");
                float r = rand.nextFloat();
                if (r < Math.exp(-delta / t)) {
                    System.arraycopy(y, 0, x, 0, y.length); 
                    objX = objY;
                    //System.out.println("worse accepted");
                }
            }

            if (objX <= fBest) {
                System.arraycopy(x, 0, xBest, 0, x.length); 
                fBest = objX;
            }

            if (iteration == maxIteration) {
                t *= a;
                iteration = 0;
                System.out.println("t:"+t+" Tmin:"+tMin);
            }

            
                
            //check termination
            //because we use animation while loop, we terminate using a flag
            if (t<tMin) {
                isRunning = false;
            }
            //createRandomInitialSolution();

            //update map panel
            //panel.update();
            updateNewSolution();
        }
        /*
        board.update();
        for (int i = 0; i < inputCount; i++) {
            agv[i].update();
        }
        */
        //System.out.println("update called");
    }
    
    private void swap(){
        //make y by appling swap in x
        System.arraycopy(x, 0, y, 0, x.length);
        
        int r1 = 0 ;
	int r2 = 0; 
        int max = nodeList.size()-1;
        int min = 0;
        //make sure we didnt get same index
	while (r1 == r2) {
            r1 = rand.nextInt((max - min) + 1) + min;
            r2 = rand.nextInt((max - min) + 1) + min;
	}
        
        int temp = y[r1];
	y[r1] = y[r2];
	y[r2] = temp;
    }
    
    private void insertion(){
        //make y by appling insertion in x
        System.arraycopy(x, 0, y, 0, x.length);
        
	int r1 = 0;
	int r2 = 0;

	int max = nodeList.size()-1;
        int min = 0;
        //make sure we didnt get same index
	while (r1 == r2) {
            r1 = rand.nextInt((max - min) + 1) + min;
            r2 = rand.nextInt((max - min) + 1) + min;
	}
	
	if (r1 > r2) {
            int temp = y[r1];
            for (int i = r1; i > r2; i--)
            {	
                 y[i] = y[i - 1];			
            }
            y[r2] = temp;
	}
	else {
            int temp = y[r1];
            for (int i = r1; i < r2; i++)
            {
                 y[i] = y[i + 1];
            }
            y[r2] = temp;
        }	
    }
    
    private void reversion(){
        //make y by appling reversion in x
        System.arraycopy(x, 0, y, 0, x.length);
        
	int r1 = 0;
	int r2 = 0;

	int max = nodeList.size()-1;
        int min = 0;
        //make sure we didnt get same index
	while (r1 == r2) {
            r1 = rand.nextInt((max - min) + 1) + min;
            r2 = rand.nextInt((max - min) + 1) + min;
	}
	
	if (r2 < r1) {
            int temp = r1;
            r1 = r2;
            r2 = temp;
	}
        
	int[] temp= new int[nodeList.size()];
	for (int i = r1; i <= r2; i++)
	{
            temp[i] = y[i];
	}

	int j = r1;
	for (int i = r2; i >= r1; i--)
	{
            y[j] = temp[i];
            j++;
	}
    }
    
    private float calculateObjectiveFunction(int[]solution){
        float sol=0;
        for (int i = 0; i < solution.length; i++) {
            int cur = solution[i];
            
            if(i<solution.length-1) {                
                int next = solution[i+1];
                sol += distanceMatrix[cur][next];
            }
            else {
                int first = solution[0];
                sol += distanceMatrix[cur][first];
            }
        }
        
        return sol;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
