import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;

@SuppressWarnings("serial")
public class Backtracker extends JFrame implements ActionListener
{
	private int mazesize; 						// Size! Maze is a square
	private int mazescale = 0; 					// Scale for drawing maze based on size
	private int[][] vwall = new int[102][102]; 	// Walls!
	private int[][] hwall = new int[102][102];
	private int currentx; 						// Record current position
	private int currenty;
	private Vector<String> directions = new Vector<String>();	// Used to backtrack
	private int[][] boxstatus = new int[102][102];				// 4 is solution path, 3 is ignore, 2 is used, 1 is in maze, 0 is not in maze yet.
	private int counter = 1; 									// Used to determine when we should stop creating this maze
	private String phase = "beginning"; 						// Records different phases throughout the program
	
	private Timer creator = new Timer(); 						// Timer to add a cell or backtrack
	
	// Drawing components and a couple variables
	private JButton goButton = new JButton("Go!");  			
	private JButton instanceButton = new JButton("Timer");
	private boolean instanceState = false;
	private JButton solveButton = new JButton("Solve");
	private boolean solved = false;
	private JTextField size_textfield = new JTextField(5);
	private JTextField speed_textfield = new JTextField(5);
	private JLabel size_label = new JLabel("Size:", JLabel.CENTER);
	private JLabel speed_label = new JLabel("Speed:", JLabel.CENTER);
	private jPanel2 forpicture = new jPanel2(); 					// The maze is drawn to this panel
	private JPanel inputs = new JPanel(new FlowLayout()); 			// Controls are added to this panel
	Container c = getContentPane(); 								// Window
	
	// Variables for double buffering, removes flicker
	Dimension offDimension; 
	Image offImage;
	Graphics Offgraphics;
	
	KeyListener keys = new KeyAdapter()
	{
		public void keyPressed(KeyEvent e)
		{
			// During "play" phase, allow the arrow keys to be used to move through the maze
			if (phase == "play")
			{
				if (e.getKeyCode() == KeyEvent.VK_UP)
				{
					// If there is no wall in the way, adjust current position and redraw frame
					if (hwall[currentx][currenty-1] == 0)
					{
						currenty -= 1;
						forpicture.repaint();
					}
				}
				else if (e.getKeyCode() == KeyEvent.VK_DOWN)
				{
					if (hwall[currentx][currenty] == 0)
					{
						currenty += 1;
						forpicture.repaint();
						
						// Check if user has made it out of the maze, in which case stop
						if (currentx == mazesize && currenty == mazesize + 1)
						{
							JOptionPane.showMessageDialog(c, "Complete!");
							phase = "complete";
						}
					}
				}
				else if (e.getKeyCode() == KeyEvent.VK_LEFT)
				{
					if ((currentx == 0 && currenty == 1) == false)
					{
						if (vwall[currentx-1][currenty] == 0)
						{
							currentx -= 1;
							forpicture.repaint();
						}
					}
				}
				else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
				{
					if (vwall[currentx][currenty] == 0)
					{
						currentx += 1;
						forpicture.repaint();
					}
				}
			}
			
			// Same as pressing the "Go" button again
			if (e.getKeyCode() == KeyEvent.VK_ENTER)
			{
				starting();
			}
		}
	};

	MouseListener mousey = new MouseAdapter()
	{
		public void mouseClicked(MouseEvent e)
		{
			// Clicking on the maze will readjust focus so that key presses will be read 
			if (SwingUtilities.isLeftMouseButton(e))
			{
				c.requestFocusInWindow();
			}
		}
	};
	
	public Backtracker()
	{
		// Lots of preferences
		// Main container
		c.setBackground(Color.white);
		c.setForeground(Color.white);
		c.setLayout(new FlowLayout());
		// Labels
		size_label.setForeground(Color.black);
		speed_label.setForeground(Color.black);
		size_textfield.setPreferredSize(new Dimension(66, 20));
		speed_textfield.setPreferredSize(new Dimension(66, 20));
		size_label.setPreferredSize(new Dimension(41, 20));
		speed_label.setPreferredSize(new Dimension(41, 20));
		// Buttons
		goButton.setPreferredSize(new Dimension(80, 20));
		instanceButton.setPreferredSize(new Dimension(80, 20));
		solveButton.setPreferredSize(new Dimension(80, 20));
		goButton.addActionListener(this); 		//adding listener for button presses
		instanceButton.addActionListener(this);
		solveButton.addActionListener(this);
		solveButton.setVisible(false);			// Solve button is invisible until maze is generated
		instanceButton.setToolTipText("Label showing is the currently active state.");
		solveButton.setToolTipText("Shows the solution path.");
		// Textfields
		size_textfield.setToolTipText("Size can be from 2-100.");
		speed_textfield.setToolTipText("Fastest is 1. But 300 nicely shows what's happening.");
		// Panels
		forpicture.setBackground(Color.white);
		forpicture.setPreferredSize(new Dimension(900, 900));
		inputs.setBackground(Color.white);
		
		// Add all the controls to the input panel
		inputs.add(size_label);
		inputs.add(size_textfield);
		inputs.add(speed_label);
		inputs.add(speed_textfield);
		inputs.add(goButton);
		inputs.add(instanceButton);
		inputs.add(solveButton);
		
		// Put it all in the window
		c.add(inputs);
		c.addKeyListener(keys);
		c.addMouseListener(mousey);
		c.add(forpicture);
		setSize(900, 730); 	// window size
		setVisible(true); 	// show window please
		setDefaultCloseOperation(EXIT_ON_CLOSE); // apparently important
	}
	
	class task extends TimerTask
	{
		public void run()
		{
			if (phase == "draw")
			{
				if (instanceState)
				{
					// instant variant just for kicks
					while (counter < mazesize*mazesize*2)
					{
						LinkedList<String> openMoves = generateMoves();
						if (openMoves.size() == 0)
							 backtrack(); 			// can't move anywhere, so backtrack
						else
							 addcell(openMoves);	// can move somewhere, so addcell
					}
					
					endingCleanUp();
				}
				else
				{
					// user can observe drawing process
					if (counter < mazesize*mazesize*2)  // if we haven't touched every square twice yet...
					{
						LinkedList<String> openMoves = generateMoves();
						if (openMoves.size() == 0)
							 backtrack(); 			// can't move anywhere, so backtrack
						else
							 addcell(openMoves);	// can move somewhere, so addcell
					}
					else							// maze should be completed, enter play phase.
					{ 
						endingCleanUp();
					}
					
					forpicture.repaint();				// redraw
				}
			}
			
			
			else if (phase == "solve")
			{
				if (instanceState)
				{
					// instant variant just for kicks
					while (currentx != mazesize || currenty != mazesize)
					{
						LinkedList<String> openMoves = generateMoves();
						if (openMoves.size() == 0)
							 backtrack(); 			// can't move anywhere, so backtrack
						else
							 addcell(openMoves);	// can move somewhere, so addcell
					}
					
					endingCleanUp();
				}
				else
				{
					// user can observe drawing process
					if (currentx != mazesize || currenty != mazesize)  // if we're not at the exit square yet...
					{
						LinkedList<String> openMoves = generateMoves();
						if (openMoves.size() == 0)
							 backtrack(); 			// can't move anywhere, so backtrack
						else
							 addcell(openMoves);	// can move somewhere, so addcell
					}
					else							// maze should be solved, enter play phase.
					{ 
						endingCleanUp();
					}
					
					forpicture.repaint();				// redraw
				}				
			}
		}
	}
	
	public void endingCleanUp()
	{
		// Stupid things that need to be done whenever a phase finishes
		vwall[0][1] = 0; 
		hwall[mazesize][mazesize] = 0;
		if (phase == "solve")
				solved = true;
		phase = "play";
		solveButton.setVisible(true);	// give user an option to solve the maze
		currentx = 0;
		currenty = 1;
		hwall[0][0] = 1;
		hwall[0][1] = 1;
		directions.clear();
		creator.cancel();
		c.requestFocusInWindow();
		
		// Reset the status of all boxes unless they're solution
		for (int i = 1; i <= mazesize; i++)
		{
			for (int j = 1; j <= mazesize; j++)
			{
				if (boxstatus[i][j] != 4)
					boxstatus[i][j] = 0;
			}
		}
		
		forpicture.repaint();					
	}
	
	public LinkedList<String> generateMoves()
	{
		// Generate a list of directions that are open depending on the phase
		LinkedList<String> moves = new LinkedList<String>();
		if (phase == "draw")
		{
			if (boxstatus[currentx-1][currenty] == 0)
				moves.add("left");
			if (boxstatus[currentx+1][currenty] == 0)
				moves.add("right");
			if (boxstatus[currentx][currenty-1] == 0)
				moves.add("up");
			if (boxstatus[currentx][currenty+1] == 0)
				moves.add("down");
			
			return moves;
		}
		else if (phase == "solve")
		{
			if (vwall[currentx-1][currenty] == 0 && boxstatus[currentx-1][currenty] == 0)
				moves.add("left");
			if (vwall[currentx][currenty] == 0 && boxstatus[currentx+1][currenty] == 0) 
				moves.add("right");
			if (hwall[currentx][currenty-1] == 0 && boxstatus[currentx][currenty-1] == 0) 
				moves.add("up");
			if (hwall[currentx][currenty] == 0 && boxstatus[currentx][currenty+1] == 0)
				moves.add("down");
			
			return moves;
		}
		else 
			return null;
	}
	
	public void addcell(LinkedList<String> openMoves)
	{
		Random rn = new Random();
		int t = 0;
		counter += 1;
		t = rn.nextInt(openMoves.size()) + 0; 	// Pick a move from the list randomly
		
		if (openMoves.get(t) == "up")	 		// up. checks if above box is open
		{
			directions.add(0, "up"); 			// add direction travelled to stack
			currenty--; 						// adjust current position
			hwall[currentx][currenty] = 0; 		// remove appropriate wall
			
			// update the cell's status
			if (phase == "draw")
				boxstatus[currentx][currenty] = 1; 	
			else if (phase == "solve")
				boxstatus[currentx][currenty] = 4;
		}
		else if (openMoves.get(t) == "down")	// down
		{
			directions.add(0, "down");
			currenty++;
			hwall[currentx][currenty - 1] = 0;

			if (phase == "draw")
				boxstatus[currentx][currenty] = 1; 	
			else if (phase == "solve")
				boxstatus[currentx][currenty] = 4;		
		}
		else if (openMoves.get(t) == "right")	// right
		{
			directions.add(0, "right");
			currentx++;
			vwall[currentx - 1][currenty] = 0;

			if (phase == "draw")
				boxstatus[currentx][currenty] = 1; 	
			else if (phase == "solve")
				boxstatus[currentx][currenty] = 4;
		}
		else if (openMoves.get(t) == "left")	// left
		{
			directions.add(0, "left");
			currentx--;
			vwall[currentx][currenty] = 0;

			if (phase == "draw")
				boxstatus[currentx][currenty] = 1; 	
			else if (phase == "solve")
				boxstatus[currentx][currenty] = 4;
		}
	}
	
	public void backtrack()
	{
		counter += 1;
		if (directions.firstElement() == "up")	// looks at the last direction it travelled and goes the opposite way 
		{
			boxstatus[currentx][currenty] = 2; 	// sets current box to "visited" status
			currenty++; 						// move
			directions.remove(0);				// remove last direction travelled from stack
		} 
		else if (directions.firstElement() == "down")
		{
			boxstatus[currentx][currenty] = 2;
			currenty --;
			directions.remove(0);
		}
		else if (directions.firstElement() == "right")
		{
			boxstatus[currentx][currenty] = 2;
			currentx--;
			directions.remove(0);
		}
		else if (directions.firstElement() == "left")
		{
			boxstatus[currentx][currenty] = 2;
			currentx++;
			directions.remove(0);
		}
	}
	
	public void starting()
	{
		counter = 1;
		if (creator != null)
		{
			creator.cancel(); 		// deletes timer if it exists
		}
		
		mazesize = Integer.parseInt(size_textfield.getText()); 	// get maze size from text box
		if (mazesize > 100)
			mazesize = 100;
		else if (mazesize < 2)
			mazesize = 2;
		
	    // sets a good scale for drawing the maze
		if (mazesize >= 20){ 
	        mazescale = 630/mazesize;
	    }else if (mazesize < 20 && mazesize > 5){
	    	mazescale = 550/mazesize;
	    }else{
	    	mazescale = 400/mazesize;
	    }
		
	    solveButton.setVisible(false);	// hide this option
		solved = false;					// maze is not solved
		phase = "draw";					// time to draw a maze
	    Random rn = new Random();
	    currentx = rn.nextInt(mazesize) + 1; // Pick a random starting point
	    currenty = rn.nextInt(mazesize) + 1;
	    counter += 1;
		
		// set walls inside maze to "up" state
	    for (int i = 1; i <= mazesize; ++i) 
	    {
	        for (int j = 1; j <= mazesize; ++j)
	        {
	            hwall[i][j] = 1;
	            vwall[j][i] = 1;
	        }
	    }
	    
		 // reset all the box states
		for (int i = 0; i < 102; i++)
		{
	    	for (int j = 0; j < 102; j++)
			{
	    		boxstatus[i][j] = 0; // 0 = not in maze
	    	}
	    }
		
	    for (int i = 0; i <= mazesize; ++i) 
	    {
	    	vwall[0][i] = 1; 		// setting the missing border walls to "up" state
	    	hwall[i][0] = 1;
	    	boxstatus[0][i] = 3; 	// setting top row and leftmost column boxes to "ignore" state
	    	boxstatus[i][0] = 3;
	    }
		
	    boxstatus[currentx][currenty] = 1; 			// updating the starting cell's status
	    
		// sets extra boxes to "ignore" state
		for (int i = mazesize + 1; i != 102; ++i) 
		{ 
	        for (int j = 1; j != 102; ++j)
			{
	            boxstatus[i][j] = 3;
	            boxstatus[j][i] = 3;
	        }
	    }
		
		// create a new Timer and start it
	    creator = new Timer(); 
	    creator.scheduleAtFixedRate(new task(), 1, Integer.parseInt(speed_textfield.getText()));
	}
	
	public void solve()
	{
		if (phase == "play" && !solved)
		{
			phase = "solve";
			currentx = 1;
			currenty = 1;
			forpicture.repaint();
			
			// update first box, since it'll always be in the solution
			boxstatus[1][1] = 4;
			// close the entrance and exit during this
			vwall[0][1] = 1; 
			hwall[mazesize][mazesize] = 1; 
			
			// Create a new Timer
			creator = new Timer();
			// start the timer and gets tick speed from text box
			creator.scheduleAtFixedRate(new task(), 1, Integer.parseInt(speed_textfield.getText()));
	    }
	}
	
	public static void main(String[] arg) 
	{
		@SuppressWarnings("unused") // I guess this just gets rid of the warning that Maze isn't used
		Backtracker j = new Backtracker();
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		 // if goButton is clicked, restart
		if (e.getSource() == goButton)
		{
		    starting();
		}
		// If instanceButton is clicked, change instanceState variable
		else if (e.getSource() == instanceButton)
		{
			instanceState = !instanceState;
			if (instanceState)
				instanceButton.setText("Instant");
			else
				instanceButton.setText("Timer");
		}
		// if solveButton is clicked, solve the maze
		else if (e.getSource() == solveButton)
		{
			solve();
		}
	}
	
	public class jPanel2 extends JPanel
	{
		public void paint(Graphics g)
		{
			update(g); //method called upon repaint. simply defers to update().
		}
		public void update(Graphics g)
		{
			Dimension d = new Dimension(900, 900);
			if ((Offgraphics == null) || (d.getWidth() != offDimension.width) || (d.getHeight() != offDimension.height)) 
			{ 
				offDimension = d; //stuff for double buffering
				offImage = createImage(d.width, d.height);
				Offgraphics = offImage.getGraphics();
			}
			Offgraphics.setColor(Color.white); //back color of everything
			if (mazesize < 50) //necessary due to how I'm drawing the maze.
				Offgraphics.fillRect(0, -10, d.width, d.height);	//mazescale gets added to y-components, which moves stuff down enough at lower maze sizes
			else //but barely moves them down at all for high maze sizes. 
				Offgraphics.fillRect(0, 0, d.width, d.height); //so this is included to move stuff around to make it look good.
			
			int yoffset = 0;
			int xoffset = 100 + mazesize/2;
			
			// Drawing walls that are up and walls in between colored squares
			for (int i = 0; i <= mazesize; i++)
			{
				for (int j = 1; j <= mazesize; j++)
				{
					if (vwall[i][j] == 0 && boxstatus[i][j] == 1 && boxstatus[i+1][j] == 1)
					{
						Offgraphics.setColor(new Color(12, 255, 9, 100));	// light green
						Offgraphics.drawLine(mazescale + mazescale*i +xoffset, mazescale*j+yoffset, mazescale+mazescale*i+xoffset, mazescale*j+mazescale+yoffset);
					}
					else if (vwall[i][j] == 0 && boxstatus[i][j] == 2 && boxstatus[i+1][j] == 2)
					{
						Offgraphics.setColor(new Color(200, 200, 200));		// light gray
						Offgraphics.drawLine(mazescale + mazescale*i +xoffset, mazescale*j+yoffset, mazescale+mazescale*i+xoffset, mazescale*j+mazescale+yoffset);
					}
					else if (vwall[i][j] == 0 && boxstatus[i][j] == 4 && boxstatus[i+1][j] == 4)
					{
						Offgraphics.setColor(new Color(255, 255, 0, 100));	// light yellow
						Offgraphics.drawLine(mazescale + mazescale*i +xoffset, mazescale*j+yoffset, mazescale+mazescale*i+xoffset, mazescale*j+mazescale+yoffset);
					}
					else if (vwall[i][j] == 1)								
					{
						Offgraphics.setColor(Color.black);
						Offgraphics.drawLine(mazescale + mazescale*i +xoffset, mazescale*j+yoffset, mazescale+mazescale*i+xoffset, mazescale*j+mazescale+yoffset);
					}
				}
		    }
			for (int i = 1; i <= mazesize; i++)
			{
		    	for (int j = 0; j <= mazesize; j++)
				{
		    		if (hwall[i][j] == 0 && boxstatus[i][j] == 1 && boxstatus[i][j+1] == 1)
					{
						Offgraphics.setColor(new Color(12, 255, 9, 100));	// light green
						Offgraphics.drawLine(mazescale*i+xoffset, mazescale+mazescale*j+yoffset, mazescale*i+mazescale+xoffset,mazescale+mazescale*j+yoffset);    
					}
					else if (hwall[i][j] == 0 && boxstatus[i][j] == 2 && boxstatus[i][j+1] == 2)
					{
						Offgraphics.setColor(new Color(200, 200, 200));		// light gray
						Offgraphics.drawLine(mazescale*i+xoffset, mazescale+mazescale*j+yoffset, mazescale*i+mazescale+xoffset,mazescale+mazescale*j+yoffset);    
					}
					else if (hwall[i][j] == 0 && boxstatus[i][j] == 4 && boxstatus[i][j+1] == 4)
					{
						Offgraphics.setColor(new Color(255, 255, 0, 100));	// light yellow
						Offgraphics.drawLine(mazescale*i+xoffset, mazescale+mazescale*j+yoffset, mazescale*i+mazescale+xoffset,mazescale+mazescale*j+yoffset);    
					}
					else if (hwall[i][j] == 1)
					{
						Offgraphics.setColor(Color.black);
						Offgraphics.drawLine(mazescale*i+xoffset, mazescale+mazescale*j+yoffset, mazescale*i+mazescale+xoffset,mazescale+mazescale*j+yoffset);    
					}
		    	}
		    } 
			
			// Coloring boxes based on if they're in maze or used or nothing or solution path
			for (int i = 1; i <= mazesize; i++)
			{
				for (int j = 1; j <= mazesize; j++)
				{
					if (boxstatus[i][j] == 1)	// "in the maze"
					{
						Offgraphics.setColor(new Color(12, 255, 9, 100));	// light green
						Offgraphics.fillRect(i*mazescale+1+xoffset, j*mazescale+1 + yoffset, mazescale-1, mazescale-1);
					}
					else if (boxstatus[i][j] == 2)	// "visited"
					{
						Offgraphics.setColor(new Color(200, 200, 200));		// light gray
						Offgraphics.fillRect(i*mazescale+1+xoffset, j*mazescale+1 + yoffset, mazescale-1, mazescale-1);
					}
					else if (boxstatus[i][j] == 4)	// "solution"
					{
						Offgraphics.setColor(new Color(255, 255, 0, 100));	// light yellow
						Offgraphics.fillRect(i*mazescale+1+xoffset, j*mazescale+1 + yoffset, mazescale-1, mazescale-1);
					}
				}
			}
			
			// draw current square as red (will be on top of anything else as it)
			Offgraphics.setColor(Color.red); 
			Offgraphics.fillRect(currentx*mazescale+1+xoffset, currenty*mazescale+1 + yoffset, mazescale-1, mazescale-1);
			
			// redraw the complete image to the JPanel, thus eliminating flicker
			if (mazesize < 50)	 
			{
				g.drawImage(offImage, 0, -10, forpicture);
			}
			else
			{
				g.drawImage(offImage, 0, 0, forpicture);
			}
		}
	}
}