import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.Timer;

@SuppressWarnings("serial")
public class Maze extends JFrame implements ActionListener{
	private int mazesize; //size! maze is a square
	private int mazescale = 0; //scale for drawing maze based on size
	private int[][] vwall = new int[102][102]; //walls!
	private int[][] hwall = new int[102][102];
	private int currentx; //record current position
	private int currenty;
	private Vector<String> directions = new Vector<String>(); //used to backtrack
	private int[][] boxstatus = new int[102][102]; //3 is ignore, 2 is used, 1 is in maze, 0 is not in maze yet.
	private int counter = 1; //used to determine when we should stop creating this maze
	private String phase = "beginning"; //is maze being drawn or person solving?
	
	private Timer creator = new Timer(); //timer to add a cell or backtrack
	
	private JButton gobutton = new JButton("Go!");  //items on the screen
	private JButton instance = new JButton("Timer");
	private boolean instancestate = false;
	private JTextField size_textfield = new JTextField(5);
	private JTextField speed_textfield = new JTextField(5);
	private JLabel size_label = new JLabel("Size:", JLabel.CENTER);
	private JLabel speed_label = new JLabel("Speed:", JLabel.CENTER);
	private jPanel2 forpicture = new jPanel2(); //the maze is drawn to this panel
	private JPanel inputs = new JPanel(new FlowLayout()); //inputs are added to this panel
	private JLayeredPane p1 = new JLayeredPane(); //allows one panel to be on top of the other
	Container c = getContentPane(); //all powerful
	
	Dimension offDimension; //used for double buffering
	Image offImage;
	Graphics Offgraphics;
	
	KeyListener keys = new KeyAdapter(){
		public void keyPressed(KeyEvent e){
			if (phase == "play"){
				if (e.getKeyCode() == KeyEvent.VK_UP){
					if (hwall[currentx][currenty-1] == 0){
						currenty -= 1;
						forpicture.repaint();
					}
				}else if (e.getKeyCode() == KeyEvent.VK_DOWN){
					if (hwall[currentx][currenty] == 0){
						currenty += 1;
						forpicture.repaint();
						if (currentx == mazesize && currenty == mazesize+1){
							JOptionPane.showMessageDialog(c, "Complete!");
							System.exit(0);
						}
					}
				}else if (e.getKeyCode() == KeyEvent.VK_LEFT){
					if ((currentx == 0 && currenty == 1) == false){
						if (vwall[currentx-1][currenty] == 0){
							currentx -= 1;
							forpicture.repaint();
						}
					}
				}else if (e.getKeyCode() == KeyEvent.VK_RIGHT){
					if (vwall[currentx][currenty] == 0){
						currentx += 1;
						forpicture.repaint();
					}
				}
			}
			if (e.getKeyCode() == KeyEvent.VK_ENTER){
				starting();
			}
		}
	};
	
	MouseListener mousey = new MouseAdapter(){
		public void mouseClicked(MouseEvent e){
			if (SwingUtilities.isLeftMouseButton(e)){
				c.requestFocusInWindow(); //needed so that you can play after clicking in a text box
			}
		}
	};
	public Maze(){
		c.setBackground(Color.black); //lots of preferences
		c.setForeground(Color.white);
		c.setLayout(new FlowLayout());
		size_label.setForeground(Color.white);
		speed_label.setForeground(Color.white);
		size_textfield.setPreferredSize(new Dimension(66, 20));
		speed_textfield.setPreferredSize(new Dimension(66, 20));
		gobutton.addActionListener(this); //adding listener for button press
		gobutton.setPreferredSize(new Dimension(67, 20));
		instance.setPreferredSize(new Dimension(80, 20));
		instance.addActionListener(this);
		size_label.setPreferredSize(new Dimension(41, 20));
		speed_label.setPreferredSize(new Dimension(41, 20));
		size_textfield.setToolTipText("Size can be from 2-100.");
		speed_textfield.setToolTipText("Fastest is 1. But 300 nicely shows what's happening.");
		instance.setToolTipText("Label showing is the currently active state.");
		forpicture.setBackground(Color.black);
		forpicture.setPreferredSize(new Dimension(900, 900));
		inputs.setBackground(Color.black);
		
		inputs.add(size_label); //creating the panel of inputs
		inputs.add(size_textfield);
		inputs.add(speed_label);
		inputs.add(speed_textfield);
		inputs.add(gobutton);
		inputs.add(instance);
		
		p1.setLayer(inputs, JLayeredPane.PALETTE_LAYER); //on top
		p1.setLayer(forpicture, JLayeredPane.DEFAULT_LAYER); //on bottom
		c.add(inputs);
		c.addKeyListener(keys);
		c.addMouseListener(mousey);
		c.add(forpicture);
		c.add(p1); 
		setSize(900, 730); //window size
		setVisible(true); //show window please
		setDefaultCloseOperation(EXIT_ON_CLOSE); //apparently important
	}
	class task extends TimerTask{
		public void run(){
			if (instancestate){
				//instant variant just for kicks
				while (counter < mazesize*mazesize*2){
					if (boxstatus[currentx-1][currenty] != 0 && boxstatus[currentx+1][currenty] != 0 && boxstatus[currentx][currenty-1] != 0 && boxstatus[currentx][currenty+1] != 0)
				         backtrack(); 
				    else
				         addcell(); 
				}		
				vwall[0][1] = 0; 
			    hwall[mazesize][mazesize] = 0;
			    phase = "play"; 
			    currentx = 0;
			    currenty = 1;
			    hwall[0][0] = 1;
			    hwall[0][1] = 1;
			    directions.clear();
			    creator.cancel();
			    c.requestFocusInWindow();
			    forpicture.repaint();
			 				
			}else{

				//user can observe drawing process
				if (phase == "draw"){ //while we're drawing...
					if (counter < mazesize*mazesize*2){ //if we haven't touched every square twice yet...
						if (boxstatus[currentx-1][currenty] != 0 && boxstatus[currentx+1][currenty] != 0 && boxstatus[currentx][currenty-1] != 0 && boxstatus[currentx][currenty+1] != 0)
					         backtrack(); //called if there are no neighbouring boxes open
					    else
					         addcell(); //called if at least one box is open
					
					}else{ //maze should be completed, enter play phase.
						vwall[0][1] = 0; //creating an entrance 
					    hwall[mazesize][mazesize] = 0; //and exit
					    phase = "play"; 
					    currentx = 0; //puts player at the start
					    currenty = 1;
					    hwall[0][0] = 1; //prevents player from moving up/down at the beginning
					    hwall[0][1] = 1;
					    directions.clear();
					    creator.cancel(); //stop the timer
					    c.requestFocusInWindow(); //lets Key Events in c be picked up by the listener
					}
					
				forpicture.repaint(); //redraw
				}

			}
		}
	}
	public void addcell(){
		Random rn = new Random();
		int t = 0;
		int finished = 1;
		counter += 1;
		while (finished == 1){
			t = rn.nextInt(4) + 1; //trying to pick a random direction to move in. will repeat if chosen direction is blocked
			if (t == 1 && boxstatus[currentx][currenty-1] == 0) //up. checks if above box is open
	          {
	                directions.add(0,"up"); //add direction travelled to stack
	                currenty--; //adjust current position
	                finished = 0; //allows the loop to end
	                hwall[currentx][currenty] = 0; //remove appropriate wall
	                boxstatus[currentx][currenty] = 1; //update the cell's status
	          }else if (t == 2 && boxstatus[currentx][currenty + 1] == 0) //down
	          {
	                directions.add(0, "down");
	                currenty++;
	                finished = 0;
	                hwall[currentx][currenty - 1] = 0;
	                boxstatus[currentx][currenty] = 1;
	          }else if (t == 3 && boxstatus[currentx + 1][currenty] == 0) //right
	          {
	                directions.add(0, "right");
	                currentx++;
	                finished = 0;
	                vwall[currentx - 1][currenty] = 0;
	                boxstatus[currentx][currenty] = 1;
	          }else if (t == 4 && boxstatus[currentx - 1][currenty] == 0) //left
	          {
	                directions.add(0, "left");
	                currentx--;
	                finished = 0;
	                vwall[currentx][currenty] = 0;
	                boxstatus[currentx][currenty] = 1;
	          }
		}
	}
	public void backtrack(){
		counter += 1;
        if (directions.firstElement() == "up"){ //looks at the last direction it travelled and goes the opposite way 
            boxstatus[currentx][currenty] = 2; //sets current box to "visited" status
            currenty++; //move
            directions.remove(0);} //remove last direction travelled from stack
        else if (directions.firstElement() == "down"){
        	boxstatus[currentx][currenty] = 2;
            currenty --;
            directions.remove(0);}
        else if (directions.firstElement() == "right"){
        	boxstatus[currentx][currenty] = 2;
            currentx--;
            directions.remove(0);}
        else if (directions.firstElement() == "left"){
        	boxstatus[currentx][currenty] = 2;
            currentx++;
            directions.remove(0);}
	}	
	public void starting(){
		counter = 1;
		if (creator != null){
			creator.cancel(); //deletes time if it exists
		}
		mazesize = Integer.parseInt(size_textfield.getText()); //get maze size from text box
	    if (mazesize >= 20){ //sets a good scale for drawing the maze
	        mazescale = 630/mazesize;
	    }else if (mazesize < 20 && mazesize > 5){
	    	mazescale = 550/mazesize;
	    }else{
	    	mazescale = 400/mazesize;
	    }
	    phase = "draw";
	    Random rn = new Random();
	    currentx = rn.nextInt(mazesize) + 1; //picking a random starting point
	    currenty = rn.nextInt(mazesize) + 1;
	    counter += 1;
	    for (int i = 1; i <= mazesize; ++i) // setting walls inside maze to "up" state
	    {
	        for (int j = 1; j <= mazesize; ++j)
	        {
	            hwall[i][j] = 1;
	            vwall[j][i] = 1;
	        }
	    }
	    for (int i = 0; i < 102; i++){ //resets all the box states
	    	for (int j = 0; j < 102; j++){
	    		boxstatus[i][j] = 0; //0 = not in maze
	    	}
	    }
	    for (int i = 0; i <= mazesize; ++i) 
	    {
	    	vwall[0][i] = 1; //setting the missing border walls to "up" state
	    	hwall[i][0] = 1;
	    	boxstatus[0][i] = 3; //setting top row and leftmost column boxes to "ignore" state
	    	boxstatus[i][0] = 3;
	    }
	    boxstatus[currentx][currenty] = 1; //updating that 1st cell's status
	    for (int i = mazesize + 1; i != 102; ++i) { //sets extra boxes to "ignore" state
	        for (int j = 1; j != 102; ++j){
	            boxstatus[i][j] = 3;
	            boxstatus[j][i] = 3;
	        }
	    }
	    creator = new Timer(); //create a new Timer
	    creator.scheduleAtFixedRate(new task(), 1, Integer.parseInt(speed_textfield.getText())); //start the timer and gets tick speed from text box
	}
	public static void main(String[] arg) {
		@SuppressWarnings("unused") //i guess this just gets rid of the warning that Maze isn't used
		Maze j = new Maze(); //yup this is all thats needed here
	}
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == gobutton){ //if button is clicked
		    starting();
		}else if (e.getSource() == instance){
			instancestate = !instancestate;
			if (instancestate){
				instance.setText("Instant");
			}else{
				instance.setText("Timer");
			}
		}
	}
	public class jPanel2 extends JPanel{
		public void paint(Graphics g){
			update(g); //method called upon repaint. simply defers to update().
		}
		public void update(Graphics g){
			Dimension d = new Dimension(900, 900);
			if ((Offgraphics == null) || (d.getWidth() != offDimension.width) || (d.getHeight() != offDimension.height)) { 
				offDimension = d; //stuff for double buffering
				offImage = createImage(d.width, d.height);
				Offgraphics = offImage.getGraphics();
			}
			Offgraphics.setColor(Color.black); //back color of everything
			if (mazesize < 50){ //necessary due to how I'm drawing the maze.
				Offgraphics.fillRect(0, -10, d.width, d.height);	//mazescale gets added to y-components, which moves stuff down enough at lower maze sizes
			}else{ //but barely moves them down at all for high maze sizes. 
				Offgraphics.fillRect(0, 0, d.width, d.height); //so this is included to move stuff around to make it look good.
			}
			int yoffset = 0;
			int xoffset = 100 + mazesize/2;
			Offgraphics.setColor(Color.white);
			for (int i = 0; i <= mazesize; i++){ //drawing walls that are still up
				for (int j = 1; j <= mazesize; j++){
					if (vwall[i][j] == 1){
						Offgraphics.drawLine(mazescale + mazescale*i +xoffset, mazescale*j+yoffset, mazescale+mazescale*i+xoffset, mazescale*j+mazescale+yoffset);
					}  
				}
		    }
		    for (int i = 1; i <= mazesize; i++){
		    	for (int j = 0; j <= mazesize; j++){
		    		if (hwall[i][j] == 1){
		    			Offgraphics.drawLine(mazescale*i+xoffset, mazescale+mazescale*j+yoffset, mazescale*i+mazescale+xoffset,mazescale+mazescale*j+yoffset);
		    		}    
		    	}
		    } 
			for (int i = 1; i <= mazesize; i++){ //coloring boxes based on if they're in maze or used or nothing
				for (int j = 1; j <= mazesize; j++){
					if (boxstatus[i][j] == 1 && phase == "draw"){
						Offgraphics.setColor(new Color(12, 140, 9));
						Offgraphics.fillRect(i*mazescale+1+xoffset, j*mazescale+1 + yoffset, mazescale-1, mazescale-1);
					}else if (boxstatus[i][j] == 2 && phase == "draw"){
						Offgraphics.setColor(Color.gray);
						Offgraphics.fillRect(i*mazescale+1+xoffset, j*mazescale+1 + yoffset, mazescale-1, mazescale-1);
					}
				}
			}
			Offgraphics.setColor(Color.red); //draw current square as red
			Offgraphics.fillRect(currentx*mazescale+1+xoffset, currenty*mazescale+1 + yoffset, mazescale-1, mazescale-1);
			if (mazesize < 50){ //redraw the complete image to the JPanel, thus eliminating flicker
				g.drawImage(offImage, 0, -10, forpicture);
			}else{
				g.drawImage(offImage, 0, 0, forpicture);
			}
		}
	}
}