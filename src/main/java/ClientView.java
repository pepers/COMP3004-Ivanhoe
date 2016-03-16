package main.java;


import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import main.java.ActionCard;
import main.java.Card;
import main.java.Client;
import main.java.Deck;
import main.java.Display;
import main.java.DisplayCard;
import main.java.Player;
import main.java.Tournament;
import main.resources.Trace;
import net.miginfocom.swing.MigLayout;

public class ClientView extends JFrame {
	private static final long serialVersionUID = 1L;
	
	// TODO remove main
	public static void main(String args[]) throws InterruptedException {
	
		ClientView c = new ClientView(null);
	
		Deck d = new Deck();
		d.initialize();
		ArrayList<Card> cards = new ArrayList<Card>();
		ArrayList<Player> players = new ArrayList<Player>();
		Player khalil = new Player("khalil");
		khalil.addToDisplay(d.draw());
		players.add(new Player("khalil"));
		
		c.arena.update(players);
		while(cards.size()<7){
			cards.add(d.draw());
			c.hand.update(cards);
		}
		
		
	}

	//Private members
	private static Image cardback;										//The image of the cardback
	private JPanel parent, header, title, controls, console;			//Purely visual JPanel members
	private Client client;												//Reference to the parent client
	private HashMap<Card, BufferedImage> images;						//Map to hold card images
	
	//Colors
	private static final Color SAND = new Color(235, 210, 165);
	private static final Color DARK_SAND = new Color(133, 113, 72);		
	
	
	private static final Color IVAN_RED = new Color(194, 73, 49);
	private static final Color IVAN_BLUE =  new Color(79, 131, 176);
	private static final Color IVAN_YELLOW = new Color(230, 197, 67);
	private static final Color IVAN_GREEN = new Color(94, 171, 90);
	private static final Color IVAN_PURPLE = new Color(153, 99, 156);
	
	//Public members
	public CardPanel hand;												//Hand of cards for the user
	public DisplayPanel arena;											//Main area where displays are shown
	public JButton endTurn;
	
	public ClientView(Client c) {
		
		LobbyView lobby = new LobbyView();
		JFrame testFrame = new JFrame();
		testFrame.add(lobby);
		testFrame.setSize(lobby.getWidth(), lobby.getHeight());
		testFrame.setVisible(true);
		testFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		client = c;
		
		//Setup the parent JPanel and general layout of the view
		this.setResizable(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize(1255, 685);
		parent = new JPanel(new MigLayout("fill", 
				"5[200::]5[200::]5[200::]5[200::]5[200::]5[200::]5",
				"5[120::]5[120::]5[120::]5[120::]5[120::]20"));
		parent.setBackground(DARK_SAND);
		//end parent setup
		
		//Setup header (stone wall that shows the tournament type
		header = new ImagePanel("./res/cobblestone.png", ImagePanel.TILE);
		header.setToolTipText("header");
		parent.add(header, "cell 0 0 4 1, grow");
		//end header setup
		
		//Setup title card "Ivanhoe", top right corner
		title = new ImagePanel("./res/title.png", ImagePanel.CENTER_SCALE);
		title.setBackground(SAND);
		title.setToolTipText("title");
		parent.add(title, "cell 4 0 2 1, grow");
		//end title setup
		
		//Setup arena section that shows displays
		arena = new DisplayPanel("./res/sand.png", ImagePanel.TILE);
		arena.setToolTipText("arena");
		arena.setLayout(new GridLayout(1,0));
		parent.add(arena, "cell 0 1 4 3, grow");
		//end arena setup
		
		//Setup controls arena under title that shows context and turn buttons
		controls = new ImagePanel("./res/wood1.png", ImagePanel.TILE);
		controls.setToolTipText("context");
		controls.setLayout(new MigLayout("fill", "5[200::]5[200::]5", "5[]5"));
		
		try {
			cardback = ImageIO.read(new File("./res/displaycards/cardback.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		ImagePanel cardContext = new ImagePanel(cardback, ImagePanel.CENTER_SCALE);
		cardContext.setOpaque(false);
		cardContext.setLayout(new MigLayout("", "5[200::]5", "5[120::][120::]5"));
		TransparentTextArea cardDescription = new TransparentTextArea();
		cardDescription.setEditable(false);
		cardDescription.setFont(new Font("Book Antiqua", Font.BOLD, 14));
		cardDescription.setLineWrap(true);
		cardDescription.setWrapStyleWord(true);
		cardDescription.setVisible(false);
		cardContext.add(cardDescription, "cell 0 1, grow");
		
		ImagePanel buttons = new ImagePanel("./res/scroll.png", ImagePanel.CENTER_SCALE);
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
		buttons.setOpaque(false);
		
		endTurn = new JButton();
		endTurn.setText("End Turn");
		endTurn.setOpaque(false);
		endTurn.setBackground(DARK_SAND);
		endTurn.setFont(new Font("Book Antiqua", Font.BOLD, 20));
		endTurn.setAlignmentX(CENTER_ALIGNMENT);
		endTurn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                if(client != null)client.cmdEnd();
            }
        });      
		
		buttons.add(Box.createRigidArea(new Dimension(0, this.getHeight()/20)));
		buttons.add(endTurn);
		
		controls.add(buttons, "cell 0 0, grow");
		controls.add(cardContext, "cell 1 0, grow");
		parent.add(controls, "cell 4 1 2 2, grow");
		//end controls setup
		
		//Setup hand panel below controls that shows card images
		hand = new CardPanel("./res/wood2.png", ImagePanel.TILE);
		hand.setToolTipText("hand");
		parent.add(hand, "cell 4 3 2 2, grow");
		//end hand setup
		
		
		//Setup console for input output below the arena
		console = new ImagePanel("./res/cloth2.png", ImagePanel.TILE);
		console.setToolTipText("console");
		console.setLayout(new BorderLayout());

		JTextPane textArea = new JTextPane();
		textArea.setSize(console.getSize());
		textArea.setOpaque(false);
		textArea.setForeground(Color.white);
		textArea.setFont(new Font("Book Antiqua", Font.BOLD, 20));
		textArea.setEditable(false);
		textArea.setFocusable(false);
		DefaultCaret caret = ((DefaultCaret) textArea.getCaret());
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane areaScrollPane = new JScrollPane(textArea);
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setOpaque(false);
		areaScrollPane.getViewport().setOpaque(false);
		areaScrollPane.setAutoscrolls(true);
		console.add(areaScrollPane, BorderLayout.CENTER);

		JTextField input = new JTextField();
		input.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (client != null)
					client.processInput(input.getText());
				input.setText("");
			}
		});
		input.setOpaque(false);
		input.setForeground(Color.white);
		input.setFont(new Font("Book Antiqua", Font.BOLD, 20));
		console.add(input, BorderLayout.SOUTH);
		parent.add(console, "cell 0 4 4 1, grow");
		//end console setup
		
		//Add the parent to the main view
		this.getContentPane().add(parent);
		this.setVisible(true);
	}

	public void writeConsole(String s, Color color) {
		try {
			JTextPane pane = ((JTextPane) ((JScrollPane) console.getComponent(0)).getViewport().getView());
			StyledDocument doc = pane.getStyledDocument();
			Style style = pane.addStyle("", null);
			StyleConstants.setForeground(style, color);
			doc.insertString(doc.getLength(), ("\n " + s), style);
			pane.selectAll();
		} catch (BadLocationException exc) {
			exc.printStackTrace();
		}
	}

	public BufferedImage getImage(Card c){
		try{
			if (images.containsKey(c)) {
				Trace.getInstance().write(this, "already have " + c.toString());
			} else {
				if (c instanceof DisplayCard) {
					String[] subs = c.toString().toLowerCase().split(":");
					String name = String.join("", subs);
					Trace.getInstance().write(this, "Loading image for " + c.toString() + " " + name + ".png");
					images.put(c, ImageIO.read(new File("./res/displaycards/" + name + ".png")));
				} else if (c instanceof ActionCard) {
					String[] subs = c.toString().toLowerCase().split(" ");
					String name = String.join("", subs);
					Trace.getInstance().write(this, "Loading image for " + c.toString() + " " + name + ".png");
					images.put(c, ImageIO.read(new File("./res/actioncards/" + name + ".png")));
				}
			}
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}
		return images.get(c);
	}
	
	class ImagePanel extends JPanel {
		private static final long serialVersionUID = 1L;
		public static final int STRETCH = 1;
		public static final int TILE = 2;
		public static final int FILL = 3;
		public static final int CENTER_SCALE = 4;
		public static final int CENTER = 5;

		Image img;
		int mode = 0;
		int imgWidth = 0;
		int imgHeight = 0;

		public ImagePanel(Image i) {img = i;}
		public ImagePanel(Image i, int m) {img = i; mode = m;}
		public ImagePanel(String i) {
			try {
				img = ImageIO.read(new File(i));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public ImagePanel(String i, int mode) {
			try {
				img = ImageIO.read(new File(i));
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.mode = mode;
		}

		public ImagePanel(String i, int mode, int w, int h) {
			try {
				img = ImageIO.read(new File(i));
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.mode = mode;
			this.imgWidth = w;
			this.imgHeight = h;
		}
		public void setImage(Image i){
			img = i;
		}
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			switch (mode) {
			case STRETCH:
				g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), null);
				break;
			case TILE:
				if(imgWidth == 0)imgWidth = img.getWidth(null);
				if(imgHeight == 0)imgHeight = img.getHeight(null);
				
				int j = 0;
				while (j < this.getHeight()) {
					int i = 0;
					while (i < this.getWidth()) {
						g.drawImage(img, i, j, imgWidth, imgHeight, null);
						i += imgWidth;
					}
					j = j + imgHeight;
				}

				break;
			case FILL:
				double d = img.getHeight(null) * ((double) this.getWidth() / img.getWidth(null));
				g.drawImage(img, 0, 0, this.getWidth(), (int) d, null);
				break;
			case CENTER_SCALE:
				double r = (double) this.getHeight() / img.getHeight(null);
				double x = (this.getWidth() - img.getWidth(null) * r) / 2;
				g.drawImage(img, (int) x, 0, (int) (img.getWidth(null) * r), (int) (img.getHeight(null) * r), null);
				break;
			case CENTER:
				g.drawImage(img, (this.getWidth() - img.getWidth(null)) / 2,
						(this.getHeight() - img.getHeight(null)) / 2, null);
				break;
			default:
				g.drawImage(img, 0, 0, null);
				break;
			}
		}
	}

	// Hand visual structure
	class CardPanel extends ImagePanel {
		private static final long serialVersionUID = 1L;
		private ArrayList<Card> hand;
		
		public CardPanel(String s) {
			super(s);
			this.setLayout(new FlowLayout());
			hand = new ArrayList<Card>();
			images = new HashMap<Card, BufferedImage>();	
		}
		public CardPanel(String s, int mode) {
			super(s, mode);
			this.setLayout(new FlowLayout());
			hand = new ArrayList<Card>();
			images = new HashMap<Card, BufferedImage>();	
		}
		
		public void update(ArrayList<Card> newHand) {
			hand = newHand;
			this.removeAll();
			for (Card c : hand){
				BufferedImage img = getImage(c);
				CardView view = new CardView(c, img);
				this.add(view);
			}
			this.validate();
			this.repaint();
		}
	}

	class CardView extends JPanel {
		private static final long serialVersionUID = 1L;
		
		private Card card;
		private BufferedImage img;
		boolean mouseOver = false;
		private int w = 75;
		private int h = 106;
		
		public CardView(Card c, BufferedImage i) {
			card = c;
			img = i;
			this.setSize(w, h);
			this.setOpaque(true);
			this.setVisible(true);
			
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					//System.out.println(card.toString() + " pressed");
				}
				
				//Clicking on a card (to play)
				@Override
				public void mouseReleased(MouseEvent e) {
		
					if (client != null) {
						if (card instanceof DisplayCard) {
							DisplayCard selected =(DisplayCard)card;
							if (client.getGameState().getTournament() == null) {
								//Auto start a tournament
								if(selected.getColour().equals("none")){
									ArrayList<Object> possibleColours = new ArrayList<Object>();
									possibleColours.add(new Colour(Colour.c.RED));
									possibleColours.add(new Colour(Colour.c.BLUE));
									possibleColours.add(new Colour(Colour.c.YELLOW));
									possibleColours.add(new Colour(Colour.c.GREEN));
									if(!client.getGameState().getLastColour().equals("purple")){
										possibleColours.add(new Colour(Colour.c.PURPLE));
									}
									SelectionMenu menu = new SelectionMenu(selected, possibleColours);
								    menu.show(e.getComponent(), e.getX(), e.getY());
								}else{
									client.cmdTournament(new String[]{selected.getColour().toString(), selected.toString()});
								}
							
							} else {
								client.cmdPlay(selected.toString());
							}
						}else{
							ActionCard selected =(ActionCard)card;
							client.cmdPlay(selected.toString());
							
							while(client.lastPrompt == null){
								//wait around
							}
							Prompt prompt = client.lastPrompt;
							client.lastPrompt = null;
							
							SelectionMenu menu = new SelectionMenu(selected, prompt.getOptions());
						    menu.show(e.getComponent(), e.getX(), e.getY());
						}
					}
				}
				@Override
				public void mouseExited(MouseEvent e) {
					ImagePanel i = ((ImagePanel)((ImagePanel)controls).getComponent(1));
					i.setImage(cardback);
					mouseOver = false;
					((JTextArea)i.getComponent(0)).setText("");
					((JTextArea)i.getComponent(0)).setVisible(false);
					controls.repaint();
					repaint();
				}
				@Override
				public void mouseEntered(MouseEvent e) {
					ImagePanel i = ((ImagePanel)((ImagePanel)controls).getComponent(1));
					i.setImage(img);
					mouseOver = true;
					if(c instanceof ActionCard){
						((JTextArea)i.getComponent(0)).setText(((ActionCard)card).getDescription());
						((JTextArea)i.getComponent(0)).setVisible(true);
					}
					controls.repaint();
					repaint();
				}
			});
			addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					
				}
			});
			
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.drawImage(img,0,0, w, h, null);
			if(mouseOver){
				g2.setColor(new Color(0, 0, 0, 70));
				g2.setStroke(new BasicStroke(w/2));
				g2.drawRect(0, 0, w, h);
			}
		}

		@Override
		public Dimension getPreferredSize(){
		    return new Dimension(w, h);
		}
	}
	
	class SelectionMenu extends JPopupMenu {
		private static final long serialVersionUID = 1L;
		static final int COLOURS = 0;
		static final int PLAYERS = 1;
		static final int CARDS = 2;
		
	    public SelectionMenu(Card card, ArrayList<Object> contents){
	    	
	    	setBackground(DARK_SAND);
	    	if (contents.get(0) instanceof Colour){
	    		if(card instanceof DisplayCard){
	    			
	    			//Start a tournament with a supporter
	    			
	    			
		    		MouseListener mouseListener = (new MouseAdapter() {
						@Override
						public void mouseReleased(MouseEvent e) {
							client.cmdTournament(new String[]{((JMenuItem) e.getComponent()).getText().toLowerCase(), ((DisplayCard) card).toString()});
						}
					}); 
		    		
		    		JLabel title = new JLabel("Pick a color");
		    		add(title);
		    		for (Object t : contents){
		    			Colour colour = (Colour) t;
		    			JMenuItem item = new JMenuItem(colour.toString());
		    			item.setBackground(toRGB(colour));
		    			item.setForeground(Color.white);
		    			item.addMouseListener(mouseListener);
			    		add(item);
		    		}
	    		}else{
	    			
	    			//Start a execute the command 
	    			
	    			MouseListener mouseListener = (new MouseAdapter() {
						@Override
						public void mouseReleased(MouseEvent e) {
							//client.send(e.getComponent())
						}
					}); 
		    		
		    		JLabel title = new JLabel("Pick a color");
		    		add(title);
		    		for (Object t : contents){
		    			Colour colour = (Colour) t;
		    			JMenuItem item = new JMenuItem(colour.toString());
		    			item.setBackground(toRGB(colour));
		    			item.setForeground(Color.white);
		    			item.addMouseListener(mouseListener);
			    		add(item);
		    		}
	    		}
	    	}
	    }
	    
	    private Color toRGB(Colour colour){
	    	switch(colour.toString().toLowerCase()){
	    		case "red":
	    			return IVAN_RED;
	    		case "blue":
	    			return IVAN_BLUE;
	    		case "yellow":
	    			return IVAN_YELLOW;
	    		case "green":
	    			return IVAN_GREEN;
	    		case "purple":
	    			return IVAN_RED;
	    		default:
	    			return Color.black;
	    	}
	    }
	}
	
	public class TransparentTextArea extends JTextArea {
		private static final long serialVersionUID = 1L;

		public TransparentTextArea() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(new Color(255, 255, 255, 128));
            Insets insets = getInsets();
            int x = insets.left;
            int y = insets.top;
            int width = getWidth() - (insets.left + insets.right);
            int height = getHeight() - (insets.top + insets.bottom);
            g.fillRect(x, y, width, height);
            super.paintComponent(g);
        }

    }
	
	public class DisplayPanel extends ImagePanel{
		private static final long serialVersionUID = 1L;

		public DisplayPanel(String path, int i){
			super(path, i);
		}
		
		public void update(ArrayList<Player> a){
			this.removeAll();
			for (Player p : a){
				addDisplay(p);
			}
			this.validate();
			this.repaint();
		}
		
		public void addDisplay(Player p){
			add(new DisplayView(p));
		}
	}
	
	public class DisplayView extends JPanel{
		private static final long serialVersionUID = 1L;
		
		//The data members needed to populate the display
		Player player;
		Display display;
		
		public DisplayView(Player p){		
			display = new Display();
			setOpaque(false);
			update(p);
		}
		
		public void update(Player p){
			player = p;
			display = p.getDisplay();
		}
		
		@Override
        protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			
			int width = 150;
            int xm = this.getWidth()/2;
            
            Tournament tournament = null;
            if(client != null){
            	tournament = client.getGameState().getTournament();
            }
            if (player == null){
            	player = new Player("NULL");
            }
            
            //Draw a crude banner
            int height = (tournament != null) ? 150 + 15 * display.score(tournament.getColour()) : 150;
            
            
            if(client != null)g2.setColor((player.equals(client.getPlayer())) ? Color.lightGray : Color.black);
            g2.fillRect(xm-10-width/2, 0, width + 20, height + 10); 
            g2.fillPolygon(new int[]{xm-10-width/2, xm, xm+10+width/2}, new int[]{height+10, height + 35, height+10}, 3);
            
            if(client != null)g2.setColor(client.getGameState().hasHighScore(player) ? IVAN_PURPLE : SAND);
            g2.fillRect(xm-width/2, 0, width, height);
            g2.fillPolygon(new int[]{xm-width/2, xm, xm + width/2}, new int[]{height, height + 20, height}, 3);
            
            g2.setColor(Color.black);
            g2.setFont(new Font("Book Antiqua", Font.BOLD, 20));
            g2.drawString(player.getName(), xm - player.getName().length()*5, 40);
            int i = 1;
            for (Card c : display.elements()){
            	BufferedImage img = getImage(c);
            	i++;
            	g2.drawImage(img, xm-37, 20 * i, 75, 106, null);
			}
        }
	}
	
	class LobbyView extends JPanel{
		private static final long serialVersionUID = 1L;

		public LobbyView(){
			setLayout(new MigLayout(
					"fill",
					"0[:450:][:450:]0",
					"0[:680:]0"));
			setSize(900, 680);
			setBackground(DARK_SAND);
			
			ImagePanel cover = new ImagePanel("./res/ivanhoe_cover.png");
			this.add(cover, "cell 0 0, grow");
			
			ImagePanel bricks = new ImagePanel("./res/stonebrick2.png", ImagePanel.TILE, 200, 200);
			bricks.setLayout(new MigLayout(
					"fill",
					"20[410!]20",
					"20[640!]20"));
			
			JTextArea console = new JTextArea();
			console.setBackground(Color.black);
			console.setForeground(Color.white);
			
			
			bricks.add(console, "cell 0 0, grow");
			
			
			this.add(bricks, "cell 1 0, grow");
		}
	}
}
