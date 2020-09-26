import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MineSweeper extends JPanel implements ActionListener,MouseListener
{
	JFrame frame;
	JMenuBar menuBar;
	JMenu gameMenu,controlsMenu;
	JRadioButtonMenuItem beginner,intermediate,expert;
	JPanel topPanel,buttonPanel;
	JToggleButton reset;
	JToggleButton[][] togglers;
	int dimR=9, dimC=9, mines=10;
	JLabel flagsLeft,time,directions;
	Font mineFont;
	Image mine,flag,flaggedMine,bomb;
	boolean gameOver = false;
	boolean firstClick = true;
	int flagged = 0;

	public MineSweeper()
	{
		frame = new JFrame("Minesweeper");

		gameMenu = new JMenu("game");
		beginner = new JRadioButtonMenuItem("beginner");
		beginner.addActionListener(this);
		beginner.setSelected(true);
		gameMenu.add(beginner);
		intermediate = new JRadioButtonMenuItem("intermediate");
		intermediate.addActionListener(this);
		intermediate.setSelected(false);
		gameMenu.add(intermediate);
		expert = new JRadioButtonMenuItem("expert");
		expert.addActionListener(this);
		expert.setSelected(false);
		gameMenu.add(expert);
		menuBar = new JMenuBar();
		menuBar.add(gameMenu);
		controlsMenu = new JMenu("controls");
		directions = new JLabel("<html><ul>" + "<li style='padding: 0px 10px 0px 10px;' >Left-click an empty square to reveal it.</li>" + "<li style='padding: 0px 10px 10px 10px;' >Right-click an empty square to flag it.</li>" + "</ul><html>",SwingConstants.CENTER);
		controlsMenu.add(directions);
		menuBar.add(controlsMenu);
		frame.setJMenuBar(menuBar);

		topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(1,3));
		flagsLeft = new JLabel("" + mines,SwingConstants.CENTER);
		topPanel.add(flagsLeft);
		reset = new JToggleButton("reset");
		reset.addMouseListener(this);
		topPanel.add(reset);
		time = new JLabel("0",SwingConstants.CENTER);
		topPanel.add(time);
		frame.add(BorderLayout.NORTH,topPanel);

		try {
		    mineFont = Font.createFont(Font.TRUETYPE_FONT, new File("font.ttf"));
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(mineFont);

			mine = ImageIO.read(new File("mine.png")).getScaledInstance(50,50,Image.SCALE_DEFAULT);
			flag = ImageIO.read(new File("flag.png")).getScaledInstance(50,50,Image.SCALE_DEFAULT);
			flaggedMine = ImageIO.read(new File("flaggedMine.png")).getScaledInstance(50,50,Image.SCALE_DEFAULT);
			bomb = ImageIO.read(new File("bomb.png")).getScaledInstance(50,50,Image.SCALE_DEFAULT);
		}catch (IOException|FontFormatException e) {
			System.out.println("pic");
		}


		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBoard();
	}

	public void setBoard()
	{
		if (buttonPanel != null)
			frame.remove(buttonPanel);
		togglers = new JToggleButton[dimR][dimC];
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(dimR,dimC));
		for (int r=0; r<dimR; r++)
		{
			for (int c=0; c<dimC; c++)
			{
				togglers[r][c] = new JToggleButton();
				togglers[r][c].putClientProperty("column",c);
				togglers[r][c].putClientProperty("row",r);
				togglers[r][c].putClientProperty("mines",0);
				togglers[r][c].putClientProperty("state",0);	//0-9
				togglers[r][c].setFocusPainted(false);
				togglers[r][c].addMouseListener(this);
				togglers[r][c].setFont(mineFont.deriveFont(12f));
				togglers[r][c].setBackground(Color.LIGHT_GRAY);
				togglers[r][c].setBorder(BorderFactory.createBevelBorder(1));
				buttonPanel.add(togglers[r][c]);

			}
		}

		frame.add(buttonPanel,BorderLayout.CENTER);
		frame.setSize(50*dimC,50*dimR);
		frame.revalidate();
	}

	public void dropMines(int currRow,int currCol)
	{
		int count = mines;
		while (count>0)
		{
			int row = (int)(Math.random()*dimR);
			int col = (int)(Math.random()*dimC);
			int state = Integer.parseInt("" + togglers[row][col].getClientProperty("state"));
			if (state==0 && (Math.abs(row-currRow)>1 || Math.abs(col-currCol)>1))
			{
				togglers[row][col].putClientProperty("state",9);
				count--;
			}
		}

		for (int r=0; r<dimR; r++)
		{
			for (int c=0; c<dimC; c++)
			{
				count = 0;
				int currToggle = Integer.parseInt("" + togglers[r][c].getClientProperty("state"));
				if (currToggle != 9)
				{
					for (int x=r-1; x<=r+1; x++)
					{
						for (int y=c-1; y<=c+1; y++)
						{
							try
							{
								int toggleState = Integer.parseInt("" + togglers[x][y].getClientProperty("state"));
								if (toggleState==9 && !(x==r && y==x))
									count++;
							}catch (ArrayIndexOutOfBoundsException e)
							{

							}
						}
					}
					togglers[r][c].putClientProperty("state",count);
				}
			}

/*to check mines*/
/*			for (int x=0; x<dimR; x++)
			{
				for (int y=0; y<dimC; y++)
				{
					int toggleState = Integer.parseInt("" + togglers[x][y].getClientProperty("state"));
					togglers[x][y].setText(""+toggleState); //0-9
				}
			}
/*delete later*/
		}
	}

	public void expand(int row,int col)
	{
		if (!togglers[row][col].isSelected())
			togglers[row][col].setSelected(true);

		int state = Integer.parseInt("" + togglers[row][col].getClientProperty("state"));
		if (state>0 && state!=9)
			writeText(row,col,state);
		else
		{
			for (int r=row-1; r<row+1; r++)
			{
				for (int c=col-1; c<col+1; c++)
				{
					if (!(r==row && c==col))
					{
						try
						{
							if (!togglers[r][c].isSelected())
								expand(r,c);
						}catch (ArrayIndexOutOfBoundsException e)
						{

						}
					}
				}
			}
		}
	}

	public void writeText(int r,int c,int state)
	{
		switch(state)
		{
			case 1: togglers[r][c].setForeground(Color.BLUE);
				break;
			case 2: togglers[r][c].setForeground(Color.GREEN);
				break;
			case 3: togglers[r][c].setForeground(Color.RED);
				break;
			case 4: togglers[r][c].setForeground(new Color(128,0,128));
				break;
			case 5: togglers[r][c].setForeground(new Color(128,0,0));
				break;
			case 6: togglers[r][c].setForeground(Color.CYAN);
				break;
			case 7: togglers[r][c].setForeground(Color.BLACK);
				break;
			case 8: togglers[r][c].setForeground(Color.GRAY);
				break;
			case 9: togglers[r][c].setIcon(new ImageIcon(mine));
					togglers[r][c].setText("");
				break;
		}
		if (state != 9)
			togglers[r][c].setText("" + state);
	}

	public void checkWin()
	{
		int totalSpaces = dimR*dimC;
		int count = 0;
		for (int r=0; r<dimR; r++)
		{
			for (int c=0; c<dimC; c++)
			{
				int state = Integer.parseInt("" + togglers[r][c].getClientProperty("state"));
				if (togglers[r][c].isSelected() && state!=9)
					count++;
			}
		}

		if (mines == totalSpaces-count)
		{
			checkMines();
			JOptionPane.showMessageDialog(null,"you win");
			reset();
		}
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == beginner){
			beginner.setSelected(true);
			intermediate.setSelected(false);
			expert.setSelected(false);
			dimR = 9;
			dimC = 9;
			mines = 10;
			setBoard();
		}

		if (e.getSource() == intermediate){
			beginner.setSelected(false);
			intermediate.setSelected(true);
			expert.setSelected(false);
			dimR = 16;
			dimC = 16;
			mines = 40;
			setBoard();
		}

		if (e.getSource() == expert){
			beginner.setSelected(false);
			intermediate.setSelected(false);
			expert.setSelected(true);
			dimR = 16;
			dimC = 30;
			mines = 99;
			setBoard();
		}
	}

	public void mousePressed(MouseEvent e)
	{


	}

	public void mouseReleased(MouseEvent e)
	{
		int row = 0;
		int col = 0;
		try
		{
			row = Integer.parseInt("" + ((JToggleButton)e.getComponent()).getClientProperty("row"));
			col = Integer.parseInt("" + ((JToggleButton)e.getComponent()).getClientProperty("column"));
		}catch (NumberFormatException ex)
		{
		}


		if (e.getButton() == MouseEvent.BUTTON1)	//left click
		{
			if (firstClick)
			{
				dropMines(row,col);
				firstClick = false;
				Timer t = new Timer();
				t.schedule(new TimerTask() {
					@Override
					public void run() {
						if (!gameOver && !firstClick)
					   		time.setText("" + (Integer.parseInt(time.getText())+1));
					   	else
					 	{
							t.cancel();
							t.purge();
						}
					}
				}, 0, 1000);

			}
			int state = 0;
			try
			{
				state = Integer.parseInt("" + ((JToggleButton)e.getComponent()).getClientProperty("state"));

			}catch (NumberFormatException ex)
			{
			}

			if (state == 9)
			{
				togglers[row][col].setSelected(true);
				System.out.println("bomb");
				togglers[row][col].setIcon(new ImageIcon(bomb));
				checkMines();
				checkBombs();
				gameOver = true;
				JOptionPane.showMessageDialog(null,"you lose");
				reset();
			}
			else
			{
				expand(row,col);
				checkWin();
			}
		}

		if (e.getButton() == MouseEvent.BUTTON3)	//right click
		{
			if (togglers[row][col].getIcon() == null)
			{
				togglers[row][col].setIcon(new ImageIcon(flag));
				flagsLeft.setText("" + (Integer.parseInt(flagsLeft.getText())-1));
				togglers[row][col].putClientProperty("mines",1);
			}
			else
			{
				togglers[row][col].setIcon(null);
				flagsLeft.setText("" + (Integer.parseInt(flagsLeft.getText())+1));
				togglers[row][col].putClientProperty("mines",0);
			}
		}

		if (e.getSource() == reset)
		{
			reset();
		}

	}

	public void reset()
	{
		setBoard();
		firstClick = true;
		gameOver = false;
		time.setText("0");
		flagsLeft.setText("" + mines);
	}

	public void checkMines()
	{
		for (int r=0; r<dimR; r++)
		{
			for (int c=0; c<dimC; c++)
			{
				int state = Integer.parseInt("" + togglers[r][c].getClientProperty("state"));
				int mines = Integer.parseInt("" + togglers[r][c].getClientProperty("mines"));
				if (mines==1 && state==9)
					togglers[r][c].setIcon(new ImageIcon(flaggedMine));
			}
		}
	}

	public void checkBombs()
	{
		for (int r=0; r<dimR; r++)
		{
			for (int c=0; c<dimC; c++)
			{
				int state = Integer.parseInt("" + togglers[r][c].getClientProperty("state"));
				int mines = Integer.parseInt("" + togglers[r][c].getClientProperty("mines"));
				if (state==9 && togglers[r][c].isSelected()==false && mines!=1)
					togglers[r][c].setIcon(new ImageIcon(mine));
			}
		}
	}

	public void mouseEntered(MouseEvent e)
	{

	}

	public void mouseExited(MouseEvent e)
	{

	}

	public void mouseClicked(MouseEvent e)
	{

	}

	public static void main (String[]args)
	{
		MineSweeper app = new MineSweeper();
	}
}