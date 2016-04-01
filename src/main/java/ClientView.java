package main.java;


import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
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
import java.util.Random;
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
import main.java.Display;
import main.java.DisplayCard;
import main.java.Player;
import main.java.Tournament;
import main.resources.Language;
import main.resources.Trace;
import net.miginfocom.swing.MigLayout;

public class ClientView extends JFrame {
	
	private static final long serialVersionUID = 1L;

	//Private members
	private static Image cardback;										//The image of the cardback
	private JPanel gameView, header, title, controls;
	private ConsoleView console;	
	private Client client;												//Reference to the parent client
	private HashMap<Card, BufferedImage> images;						//Map to hold card images
	private LobbyView lobbyView;
	boolean inGame = false;
	private boolean connected = false; // connected to server
	
	//Colors
	public static final Color SAND = new Color(235, 210, 165);
	public static final Color DARK_SAND = new Color(133, 113, 72);		
	public static final Color IVAN_RED = new Color(194, 73, 49);
	public static final Color IVAN_BLUE =  new Color(79, 131, 176);
	public static final Color IVAN_YELLOW = new Color(230, 197, 67);
	public static final Color IVAN_GREEN = new Color(94, 171, 90);
	public static final Color IVAN_PURPLE = new Color(153, 99, 156);
	
	public static final int INFO = 0;
	public static final int CHAT = 1;
	public static final int ERROR = 2;
	
	//Public members
	public CardPanel hand;												//Hand of cards for the user
	public DisplayPanel arena;											//Main area where displays are shown
	public JButton endTurn, translate, shutdown;
	JToggleButton censor;
	public ImagePanel banner;
	public ImagePanel weaponIcon;
	
	//UI Images
	private Image greyBanner, blueBanner, redBanner, greenBanner, yellowBanner, purpleBanner, 
	stun, shield, redToken, blueToken, greenToken, yellowToken, purpleToken;
	
			
	public ClientView(Client c) {
		//load some images
		try {
			greyBanner = ImageIO.read(new File("./res/banner_default.png"));
			redBanner = ImageIO.read(new File("./res/banner_red2.png"));
			blueBanner = ImageIO.read(new File("./res/banner_blue2.png"));
			yellowBanner = ImageIO.read(new File("./res/banner_yellow2.png"));
			greenBanner = ImageIO.read(new File("./res/banner_green2.png"));
			purpleBanner = ImageIO.read(new File("./res/banner_purple2.png"));
			
			redToken = ImageIO.read(new File("./res/token_red.png"));
			greenToken = ImageIO.read(new File("./res/token_green.png"));
			yellowToken = ImageIO.read(new File("./res/token_yellow.png"));
			purpleToken = ImageIO.read(new File("./res/token_purple.png"));
			blueToken = ImageIO.read(new File("./res/token_blue.png"));
			
			stun = ImageIO.read(new File("./res/stunned.png"));
			shield = ImageIO.read(new File("./res/shield.png"));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		client = c;
		this.setResizable(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(true);
		
		enterLobby();
		
		// hacky way to wait until connected to server
		while (!connected) {
			System.out.println("");
		}
		
		enterLobby();

	}
	
	/*
	 * go to the lobby
	 */
	public boolean enterLobby() {
		this.lobbyView = new LobbyView();
		this.setSize(900, 680);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		this.setContentPane(this.lobbyView);
		this.revalidate();
		return true;
	}
	
	public void updateComponents(GameState gameState, Player player){
		if (gameState != null) {
			endTurn.setForeground(player.isTurn()? Color.black : Color.lightGray);
			endTurn.setText(gameState.hasHighScore(player) ? "End Turn" : "Withdraw");
			hand.update(gameState.getPlayer(player).getHand());
			arena.update(gameState.getPlayers());
			setBannerType( (gameState.getTournament() == null) ? new Colour("NONE") : gameState.getTournament().getColour());
		} else { // game has ended
			player.reset(); // go from ready to waiting
		}
	}
	
	public void setupGameView(){
		this.setTitle(client.getPlayer().getName() + " - Ivanhoe");
		lobbyView = null;
		inGame = true;
		//Setup the parent JPanel and general layout of the view
		this.setSize(1255, 685);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		gameView = new JPanel(new MigLayout("fill", 
				"5[200::]5[200::]5[200::]5[200::]5[200::]5[200::]5",
				"5[120::]5[120::]5[120::]5[120::]5[120::]20"));
		gameView.setBackground(DARK_SAND);
		//end parent setup
		
		//Setup header (stone wall that shows the tournament type
		header = new ImagePanel("./res/cobblestone.png", ImagePanel.TILE);
		header.setLayout(new MigLayout("fill", "", ""));
		banner = new ImagePanel(greyBanner, ImagePanel.CENTER);
		banner.setLayout(new BorderLayout());
		header.add(banner, "grow");
		gameView.add(header, "cell 0 0 4 1, grow");
		//end header setup
		
		//Setup title card "Ivanhoe", top right corner
		title = new ImagePanel("./res/title.png", ImagePanel.CENTER_SCALE);
		title.setBackground(SAND);
		gameView.add(title, "cell 4 0 2 1, grow");
		//end title setup
		
		//Setup arena section that shows displays
		arena = new DisplayPanel("./res/sand.png", ImagePanel.TILE);
		arena.setToolTipText("arena");
		arena.setLayout(new GridLayout(1,0));
		gameView.add(arena, "cell 0 1 4 3, grow");
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
		endTurn.setForeground(Color.black);
		endTurn.setFont(new Font("Book Antiqua", Font.BOLD, 20));
		endTurn.setAlignmentX(CENTER_ALIGNMENT);
		endTurn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                if(client != null)client.cmdEnd();
            }
        });      
		buttons.add(Box.createRigidArea(new Dimension(0, this.getHeight()/20)));
		buttons.add(endTurn);
		
		censor = new JToggleButton();
		censor.setText("Censor");
		censor.setOpaque(false);
		censor.setBackground(DARK_SAND);
		censor.setForeground(Color.black);
		censor.setFont(new Font("Book Antiqua", Font.BOLD, 20));
		censor.setAlignmentX(CENTER_ALIGNMENT);
		censor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                if(client != null)client.cmdCensor();
            }
        });      
		buttons.add(censor);
		
		translate = new JButton();
		translate.setText("Translate");
		translate.setOpaque(false);
		translate.setBackground(DARK_SAND);
		translate.setForeground(Color.black);
		translate.setFont(new Font("Book Antiqua", Font.BOLD, 20));
		translate.setAlignmentX(CENTER_ALIGNMENT);
		translate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
            	
            	Language.Dialect choice = (Language.Dialect) JOptionPane.showInputDialog(null, 
            			"Please choose a language:", 
            			"Translate",
            	        JOptionPane.QUESTION_MESSAGE, 
            	        null,
	            	    Language.Dialect.values(),
            	        "none");
            	System.out.println("Setting language to " + choice.toString());
                if(client != null)client.cmdTranslate(choice.toString());
            }
        });      
		buttons.add(translate);
		
		shutdown = new JButton();
		shutdown.setText("Shutdown");
		shutdown.setOpaque(false);
		shutdown.setBackground(DARK_SAND);
		shutdown.setForeground(Color.black);
		shutdown.setFont(new Font("Book Antiqua", Font.BOLD, 20));
		shutdown.setAlignmentX(CENTER_ALIGNMENT);
		shutdown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
            	if(JOptionPane.showConfirmDialog(null, "Exit to desktop?", "Shutdown", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
            		if(client != null)client.shutdown();
            	}
                
            }
        });      
		buttons.add(shutdown);
		
		controls.add(buttons, "cell 0 0, grow");
		controls.add(cardContext, "cell 1 0, grow");
		gameView.add(controls, "cell 4 1 2 2, grow");
		//end controls setup
		
		//Setup hand panel below controls that shows card images
		hand = new CardPanel("./res/wood2.png", ImagePanel.TILE);
		hand.setToolTipText("hand");
		gameView.add(hand, "cell 4 3 2 2, grow");
		//end hand setup
		
		
		//Setup console for input output below the arena
		console = new ConsoleView("./res/cloth2.png", ImagePanel.TILE, false);
		gameView.add(console, "cell 0 4 4 1, grow");
		//end console setup
		
		//Add the parent to the main view
		this.getContentPane().removeAll();
		this.getContentPane().add(gameView);
		this.revalidate();
	}
	
	public void writeConsole(String message, int type) {
		if(lobbyView != null){
			ConsoleView lobbyConsole = (ConsoleView) ((ImagePanel) lobbyView.getComponent(1)).getComponent(0);
			lobbyConsole.write(message, type);
		}
		if(gameView != null){
			console.write(message, type);
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
	
	/*
	 * Below are the custom swing classes
	 */
	
	
	class ImagePanel extends JPanel {
		private static final long serialVersionUID = 1L;
		public static final int STRETCH = 1;
		public static final int TILE = 2;
		public static final int FILL = 3;
		public static final int CENTER_SCALE = 4;
		public static final int CENTER = 5;
		public static final int FIT = 6;

		Image img;
		int mode = 0;
		int imgWidth = 0;
		int imgHeight = 0;

		public void setImageSize(int width, int height){
			imgWidth = width;
			imgHeight = height;
		}
		
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
			this(i);
			this.mode = mode;
		}
		public ImagePanel(String i, int mode, int w, int h) {
			this(i, mode);
			this.imgWidth = w;
			this.imgHeight = h;
		}
		public ImagePanel() {}
		public void setImage(Image i){
			img = i;
		}
		@Override
		protected void paintComponent(Graphics g) {
			this.setOpaque(false);
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
			case FIT:
				r = (double) this.getWidth() / img.getWidth(null);
				x = (this.getWidth() - img.getWidth(null) * r) / 2;
				g.drawImage(img, (int) x, (int) -((img.getHeight(null) - this.getHeight())/2 * r), (int) (img.getWidth(null) * r), (int) (img.getHeight(null) * r), null);
				break;
			default:
				g.drawImage(img, 0, 0, null);
				break;
			}
		}
	}

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
				public void mousePressed(MouseEvent e) {}	
				//Clicking on a card (to play)
				@Override
				public void mouseReleased(MouseEvent e) {
					if (client.getGameState().canPlay(card, client.getPlayer()) == 0){
						if (card instanceof DisplayCard) {
							DisplayCard selected =(DisplayCard)card;
							if (client.getGameState().getTournament() == null) {
								//Auto start a tournament
								if(selected.getColour().equals("none")){
									SelectionMenu menu = new SelectionMenu(selected);
								    menu.show(e.getComponent(), e.getX(), e.getY());
								}else{
									client.cmdTournament(new String[]{selected.getColour().toString(), selected.toString()});
								}
							
							} else {
								client.cmdPlay(selected.toString());
							}
						}else{
							ActionCard selected =(ActionCard)card;
							
							if(selected.hasTargets()){
								ArrayList<Object> options = client.getGameState().getTargets(selected, client.getPlayer());
								if ((options != null) && (options.size() > 0)) {
									SelectionMenu menu = new SelectionMenu(selected);
								    menu.show(e.getComponent(), e.getX(), e.getY());
								}
							}else{
								client.send(new Play(selected, null, null, null));
							}
							
						}
					}
					updateComponents(client.getGameState(), client.getPlayer());
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
			if(client.getGameState().canPlay(card, client.getPlayer()) == 0){
				if (mouseOver) {
					g2.setColor(new Color(0, 0, 0, 70));
					g2.setStroke(new BasicStroke(w / 2));
					g2.drawRect(0, 0, w, h);
				}
			}else{
				g2.setColor(new Color(0, 0, 0, 100));
				g2.fillRect(0, 0, w, h);
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
		
	    public SelectionMenu(Card card){
	    	
	    	if(card instanceof DisplayCard){
	    		JLabel title = new JLabel("Pick a color");
				add(title);
				ArrayList<Object> possibleColours = new ArrayList<Object>();
				possibleColours.add(new Colour(Colour.c.RED));
				possibleColours.add(new Colour(Colour.c.BLUE));
				possibleColours.add(new Colour(Colour.c.YELLOW));
				possibleColours.add(new Colour(Colour.c.GREEN));
				if(!client.getGameState().getLastColour().equals("purple")){
					possibleColours.add(new Colour(Colour.c.PURPLE));
				}
				for (Object t : possibleColours) {
					Colour colour = (Colour) t;
					JMenuItem item = new JMenuItem(colour.toString());
					item.setBackground(toRGB(colour));
					item.setForeground(Color.white);
					item.addMouseListener(new MouseAdapter(){
						@Override
						public void mouseReleased(MouseEvent e) {
							client.cmdTournament(new String[] { ((JMenuItem) e.getComponent()).getText().toLowerCase(),
									((DisplayCard) card).toString() });
						}	
					});
					add(item);
				}
	    	}else{
	    		ActionCard actionCard = (ActionCard) card;
	    		ArrayList<Object> options = client.getGameState().getTargets(actionCard, client.getPlayer());
	    		
		    	switch(card.toString()){
				case "Break Lance":
					addPlayerOptions(actionCard, options);
					break;
				case "Change Weapon":				
					addColorOptions(actionCard, options);
					break;
				case "Dodge":
					JLabel title = new JLabel("Choose a player");
					add(title);
					for (Object t : options) {
						Player p = (Player) t;
						JMenu submenu = new JMenu(p.getName());
						submenu.setOpaque(true);
						submenu.setBackground(Color.black);
						submenu.setForeground(Color.white);
						title = new JLabel("Their cards");
						submenu.add(title);
						for (Card c : p.getDisplay().elements()){
							JMenuItem item = new JMenuItem(c.toString());
							
							ArrayList<Player> players = new ArrayList<Player>();
							players.add(p);
							ArrayList<Card> cards = new ArrayList<Card>();
							cards.add(c);
							item.addMouseListener(commandGenerator(actionCard, null, players, cards));
							
							submenu.add(item);
						}
						add(submenu);
					}
					break;
				case "Drop Weapon":
					addColorOptions(actionCard, options);
					break;
				case "Outwit":
					title = new JLabel("Choose a player");
					add(title);
					for (Object t : options) {
						Player p = (Player) t;
						JMenu submenu = new JMenu(p.getName());
						submenu.setOpaque(true);
						submenu.setBackground(Color.black);
						submenu.setForeground(Color.white);
						title = new JLabel("Their cards");
						submenu.add(title);
						for (Card theirCard : p.getDisplay().elements()){
							JMenu theirItem = new JMenu(theirCard.toString());
							theirItem.setOpaque(true);
							theirItem.setBackground(toRGB(theirCard.getColour()));
							theirItem.setForeground(Color.white);
							title = new JLabel("Your cards");
							theirItem.add(title);
							for (Card yourCard : client.getPlayer().getDisplay().elements()){
								JMenuItem yourItem = new JMenuItem(yourCard.toString());
								yourItem.setOpaque(true);
								yourItem.setBackground(toRGB(theirCard.getColour()));
								yourItem.setForeground(Color.white);
								ArrayList<Player> players = new ArrayList<Player>();
								players.add(p);
								ArrayList<Card> cards = new ArrayList<Card>();
								cards.add(theirCard);
								cards.add(yourCard);
								yourItem.addMouseListener(commandGenerator(actionCard, null, players, cards));
								theirItem.add(yourItem);
							}
							submenu.add(theirItem);
						}
						if(p.getShielded()){
							JMenuItem item = new JMenuItem("Shield");
							ArrayList<Player> players = new ArrayList<Player>();
							players.add(p);
							ArrayList<Card> cards = new ArrayList<Card>();
							cards.add(new ActionCard("Shield"));
							item.addMouseListener(commandGenerator(actionCard, null, players, cards));
							submenu.add(item);
						}
						if(p.getStunned()){
							JMenuItem item = new JMenuItem("Stunned");
							ArrayList<Player> players = new ArrayList<Player>();
							players.add(p);
							ArrayList<Card> cards = new ArrayList<Card>();
							cards.add(new ActionCard("Stunned"));
							item.addMouseListener(commandGenerator(actionCard, null, players, cards));
							submenu.add(item);
						}
						add(submenu);
					}
					break;
				case "Retreat":
					addCardOptions(actionCard, options);
					break;
				case "Stunned":
					addPlayerOptions(actionCard, options);
					break;
				case "Unhorse":
					addColorOptions(actionCard, options);
					break;
				default:
	
		    	}
	    	}
	    }
	    public MouseListener commandGenerator(Card actionCard, ArrayList<Colour> colours, ArrayList<Player> players, ArrayList<Card> cards){
	    	MouseListener listener = (new MouseAdapter() {
				@Override
				public void mouseReleased(MouseEvent e) {
					client.send(new Play(actionCard, colours, players, cards));
				}
			});
	    	return listener;
	    }
    	
	    public void addColorOptions(ActionCard card, ArrayList<Object> options){
	    	JLabel title = new JLabel("Pick a color");
			add(title);
	    	for (Object t : options) {
				Colour colour = (Colour) t;
				JMenuItem item = new JMenuItem(colour.toString());
				item.setBackground(toRGB(colour));
				item.setForeground(Color.white);
				ArrayList<Colour> selectedColour = new ArrayList<Colour>();
				selectedColour.add(colour);
				item.addMouseListener(commandGenerator(card, selectedColour, null, null));
				add(item);
			}
	    }
	    
	    public void addPlayerOptions(ActionCard card, ArrayList<Object> options){
	    	JLabel title = new JLabel("Choose a player");
			add(title);
	    	for (Object t : options) {
				Player p = (Player) t;
				JMenuItem item = new JMenuItem(p.getName());
				item.setBackground(Color.black);
				item.setForeground(Color.white);
				ArrayList<Player> selectedPlayer = new ArrayList<Player>();
				selectedPlayer.add(p);
				item.addMouseListener(commandGenerator(card, null, selectedPlayer, null));
				add(item);
			}
	    }
	    
	    public void addCardOptions(ActionCard card, ArrayList<Object> options){
	    	JLabel title = new JLabel("Choose a card");
			add(title);
	    	for (Object t : options) {
				Card c = (Card) t;
				JMenuItem item = new JMenuItem(c.toString());
				item.setBackground(Color.black);
				item.setForeground(Color.white);
				ArrayList<Card> selectedCard = new ArrayList<Card>();
				selectedCard.add(c);
				item.addMouseListener(commandGenerator(card, null, null, selectedCard));
				add(item);
			}
	    }
	}

	private Color toRGB(Colour colour) {
		switch (colour.toString().toLowerCase()) {
		case "red":
			return IVAN_RED;
		case "blue":
			return IVAN_BLUE;
		case "yellow":
			return IVAN_YELLOW;
		case "green":
			return IVAN_GREEN;
		case "purple":
			return IVAN_PURPLE;
		default:
			return Color.black;
		}
	}

	class TransparentTextArea extends JTextArea {
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
	
	class DisplayPanel extends ImagePanel{
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
	
	class DisplayView extends JPanel{
		private static final long serialVersionUID = 1L;
		
		//The data members needed to populate the display
		Player player;
		Display display;
		int height;
		int xm;
		int width = 150;
		
		public DisplayView(Player p){		
			display = new Display();
			setOpaque(false);
			addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					ImagePanel panel = ((ImagePanel) ((ImagePanel) controls).getComponent(1));
					if(display.size() > 0 && inGame && e.getX() > xm-37 && e.getX() < xm+37 && e.getY()>70 && e.getY() < 70 + (20 * display.size()-1) + 106){
						int i = (e.getY() - 70) / 20;
						if (i > display.size() - 1)i = display.size() - 1;
						
						panel.setImage(getImage(display.get(i)));
						controls.repaint();
					}else{
						panel.setImage(cardback);
						controls.repaint();
					}
				}
			});
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
			
	            xm = this.getWidth()/2;
	            
	            Tournament tournament = null;
	            if(client != null){
	            	tournament = client.getGameState().getTournament();
	            }
	            if (player == null){
	            	player = new Player("NULL", 0);
	            }
	            
	            //Draw a crude banner
	            height = (tournament != null) ? 200 + 15 * display.size(): 150;
	            
	            if(player.equals(client.getPlayer())){
		            g2.setColor(Color.black);
		            g2.fillRect(xm-15-width/2, 0, width + 30, height + 20); 
		            g2.fillPolygon(new int[]{xm-10-width/2, xm, xm+10+width/2}, new int[]{height + 20, height + 45, height+20}, 3);
	            }
	            if(client != null)g2.setColor(player.getColor());
	            g2.fillRect(xm-10-width/2, 0, width + 20, height + 10); 
	            g2.fillPolygon(new int[]{xm-10-width/2, xm, xm+10+width/2}, new int[]{height+10, height + 35, height+10}, 3);
	            
	            if(client != null)g2.setColor(client.getGameState().hasHighScore(player) ? toRGB(client.getGameState().getTournament().getColour()) : SAND);
	            g2.fillRect(xm-width/2, 0, width, height);
	            g2.fillPolygon(new int[]{xm-width/2, xm, xm + width/2}, new int[]{height, height + 20, height}, 3);
	            
	            g2.setColor(Color.darkGray);
	            g2.fillRect(xm-width/2, 0, width, 55);
	            
	            g2.setColor(Color.white);
	            g2.setFont(new Font("Book Antiqua", Font.BOLD, 20));
	            g2.drawString(player.getName(), xm - player.getName().length()*5, 25);
	            
	            int i = 0;
	            for (Card c : display.elements()){
	            	BufferedImage img = getImage(c);
	            	
	            	g2.drawImage(img, xm-37, 70 + (20 * i), 75, 106, null);
	            	i++;
				}
	            i = 0;
	            for (Token t : player.getTokens()){
	            	i++;
	            	if(t.getColour().equals("red")){
	            		g2.drawImage(redToken, xm-width/2 + (i * width/7), 35, 20, 20, null);
	            	}else if(t.getColour().equals("blue")){
	            		g2.drawImage(blueToken, xm-width/2 + (i * width/7), 35, 20, 20, null);
	            	}else if(t.getColour().equals("yellow")){
	            		g2.drawImage(yellowToken, xm-width/2 + (i * width/7), 35, 20, 20, null);
	            	}else if(t.getColour().equals("green")){
	            		g2.drawImage(greenToken, xm-width/2 + (i * width/7), 35, 20, 20, null);
	            	}else if(t.getColour().equals("purple")){
	            		g2.drawImage(purpleToken, xm-width/2 + (i * width/7), 35, 20, 20, null);
	            	}
				}
	            
	            if(player.getStunned()){
	            	g2.drawImage(stun, xm - 20, height - 27, 40, 40, null);
	            }
	            if(player.getShielded()){
	            	g2.drawImage(shield, xm - 20, height - 27, 40, 40, null);
	            }
        }
	}
	
	class LobbyView extends JPanel{
		private static final long serialVersionUID = 1L;

		private ConsoleView console;
		private LoginView login;
		
		public LobbyView(){
			setLayout(new MigLayout(
					"fill",
					"0[:450:][:450:]0",
					"0[:680:]0"));
			setSize(900, 680);
			setBackground(DARK_SAND);
			
			ImagePanel cover = new ImagePanel("./res/ivanhoe_cover.png", ImagePanel.CENTER);
			this.add(cover, "cell 0 0, grow");
			
			ImagePanel bricks = new ImagePanel("./res/stonebrick2.png", ImagePanel.TILE, 200, 200);
			bricks.setLayout(new MigLayout(
					"fill",
					"20[410!]20",
					"20[600!]20"));
			
			if (!connected) {
				bricks.removeAll();
				login = new LoginView("./res/arena.png");
				bricks.add(login);
			} else {
				bricks.removeAll();
				console = new ConsoleView("./res/arena.png", ImagePanel.TILE, true);
				bricks.add(console, "grow");
			}
			
			this.add(bricks, "cell 1 0, grow");
		}
	}
	
	/*
	 * enter name, server address, server port
	 */
	class LoginView extends ImagePanel {
		private static final long serialVersionUID = 1L;
		private JLabel lname    = new JLabel("Your Name: ");
		private JLabel laddress = new JLabel("Server Address: ");
		private JLabel lport    = new JLabel("Server Port: ");
		private JLabel lcolor    = new JLabel("Color: ");
		private JTextField tname = new JTextField(10);
		private JTextField taddress = new JTextField(10);
		private JTextField tport = new JTextField(10);
		private JButton tcolor = new JButton("Choose");
		private JButton connect = new JButton("Connect");
		private JPanel labels = new JPanel();
		private JPanel textfields = new JPanel();
		
		public LoginView(String path) {
			this(path, ImagePanel.TILE);
		}
		
		public LoginView(String path, int style){
			super(path, style);
			this.setOpaque(false);
			
			this.labels.setLayout(new BoxLayout(this.labels, BoxLayout.Y_AXIS));
			this.labels.setOpaque(false);
			
			this.textfields.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			this.textfields.setOpaque(false);

			// add labels
			this.lname.setFont(new Font("Book Antiqua", Font.BOLD, 20));
			this.lname.setForeground(Color.WHITE);
			this.lname.setAlignmentX(Component.RIGHT_ALIGNMENT);
			this.labels.add(this.lname);
			
			this.laddress.setFont(new Font("Book Antiqua", Font.BOLD, 20));
			this.laddress.setForeground(Color.WHITE);
			this.laddress.setAlignmentX(Component.RIGHT_ALIGNMENT);
			this.labels.add(this.laddress);
			
			this.lport.setFont(new Font("Book Antiqua", Font.BOLD, 20));
			this.lport.setForeground(Color.WHITE);
			this.lport.setAlignmentX(Component.RIGHT_ALIGNMENT);
			this.labels.add(this.lport);
			
			this.lcolor.setFont(new Font("Book Antiqua", Font.BOLD, 20));
			this.lcolor.setForeground(Color.WHITE);
			this.lcolor.setAlignmentX(Component.RIGHT_ALIGNMENT);
			this.labels.add(this.lcolor);
			
			// add textfields
			this.tname.setFont(new Font("Book Antiqua", Font.BOLD, 20));
			this.tname.setText("Player");
			gbc.gridy++;
			this.textfields.add(this.tname, gbc);
			
			this.taddress.setFont(new Font("Book Antiqua", Font.BOLD, 20));
			this.taddress.setText("127.0.0.1");
			gbc.gridy++;
			this.textfields.add(this.taddress, gbc);
			
			this.tport.setFont(new Font("Book Antiqua", Font.BOLD, 20));
			this.tport.setText("5050");
			gbc.gridy++;
			this.textfields.add(this.tport, gbc);
			
			this.tcolor.setFont(new Font("Book Antiqua", Font.BOLD, 20));
			Random r = new Random();
			Color c = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
			this.tcolor.setBackground(c);
			if ((c.getBlue() + c.getRed() + c.getGreen()) / 3 < 125) {
				tcolor.setForeground(Color.white);
			} else {
				tcolor.setForeground(Color.black);
			}
			this.tcolor.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					Color c = JColorChooser.showDialog(null, "Choose a Color", tcolor.getForeground());
				      if (c != null){
				    	  tcolor.setBackground(c);
				    	  if((c.getBlue() + c.getRed() + c.getGreen()) / 3 < 125){
				    		  tcolor.setForeground(Color.white);
				    	  }else{
				    		  tcolor.setForeground(Color.black);
				    	  }
				      }
				}
			});
			gbc.gridy++;
			this.textfields.add(this.tcolor, gbc);
			
			// set properties of Connect button
			this.connect.setFont(new Font("Book Antiqua", Font.BOLD, 20));
			
			// add each line
			this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			this.add(this.labels);
			this.add(this.textfields);
			this.add(this.connect);
			
			// send info when ready button is clicked
			this.connect.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (client != null) {
						client.initialize(getName());
						client.getPlayer().setColor(getColor());
						int port;
						try {
							port = Integer.parseInt(getPort());
						} catch (NumberFormatException nfe) {
							return;
						}
						if (client.guiStartUp(getAddress(), port)) {
							connected = true;
							connect.setEnabled(false);
						}
					}
				}
			});
		}
		
		public String getName() { return this.tname.getText(); }
		public String getAddress() { return this.taddress.getText(); }
		public String getPort() { return this.tport.getText(); }
		public Color getColor() { return this.tcolor.getBackground(); }
	}
	
	class ConsoleView extends ImagePanel{
		private static final long serialVersionUID = 1L;
		public static final int COMMAND = 0;
		public static final int USER_INPUT = 1;
		private String inputText = null;
		int mode = COMMAND;
		
		JScrollPane areaScrollPane;
		JTextPane textArea;
		JTextField input;
		private JButton ready = new JButton("Ready");
		private boolean isReady = false;
		private JPanel bottom = new JPanel();
		
		public ConsoleView(String path){
			this(path, ImagePanel.TILE, false);
		}
		public ConsoleView(String path, int style, boolean showReady){
			super(path, style);
			this.setLayout(new BorderLayout());
			this.bottom.setLayout(new BoxLayout(this.bottom, BoxLayout.X_AXIS));
			
			textArea = new JTextPane();
			textArea.setSize(this.getSize());
			textArea.setOpaque(false);
			textArea.setForeground(Color.white);
			textArea.setFont(new Font("Book Antiqua", Font.BOLD, 20));
			textArea.setEditable(false);
			textArea.setFocusable(false);
			DefaultCaret caret = ((DefaultCaret) textArea.getCaret());
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
			areaScrollPane = new JScrollPane(textArea);
			areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			areaScrollPane.setOpaque(false);
			areaScrollPane.getViewport().setOpaque(false);
			areaScrollPane.setAutoscrolls(true);
			this.add(areaScrollPane, BorderLayout.CENTER);
			
			if(showReady){
				// set the Ready button
				this.ready.setFont(new Font("Book Antiqua", Font.BOLD, 20));
				this.ready.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						client.cmdReady();
						if (isReady) {
							isReady = false;
							ready.setText("Ready");
						} else {
							isReady = true;
							ready.setText("Not Ready");
						}
					}
				});
			}

			input = new JTextField();
			input.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					switch (mode){
						case USER_INPUT:
							inputText = input.getText();
							input.setText("");
							break;
						default:
							if (client != null)client.processInput(input.getText());
							input.setText("");
							break;
					}
				}});
			
			input.setOpaque(false);
			input.setForeground(Color.white);
			input.setFont(new Font("Book Antiqua", Font.BOLD, 20));
			this.bottom.setOpaque(false);
			this.bottom.add(input); // add textfield to bottom
			if(showReady)this.bottom.add(ready); // add Ready button beside textfield
			this.add(bottom, BorderLayout.SOUTH);
		}
	
		public String getText(){return inputText;}
		public void clearText(){inputText = null;}
		public int getMode(){return mode;}
		public void setMode(int mode){
			//write("Now operating in " + mode + " mode.",INFO);
			this.mode = mode;
		}
		public void write(String s, int type) {
			StyledDocument doc = textArea.getStyledDocument();
			Style style = textArea.addStyle("", null);
			try {
				switch(type){
				case INFO:
					StyleConstants.setForeground(style, Color.lightGray);
					break;
				case CHAT:
					StyleConstants.setForeground(style, Color.lightGray);
					break;
				default:
					StyleConstants.setForeground(style, Color.lightGray);
				}
				doc.insertString(doc.getLength(), ("\n " + s), style);
				textArea.setCaretPosition(textArea.getDocument().getLength());
			} catch (BadLocationException exc) {
				exc.printStackTrace();
			}
		}
	}

	public ConsoleView getConsole() {
		if(!inGame){
			return (ConsoleView) ((ImagePanel) lobbyView.getComponent(1)).getComponent(0);
		}else{
			return console;
		}
	}
	
	public void setBannerType(Colour c){
		switch(c.toString()){
		case("None"):
			banner.setImage(greyBanner);
			break;
		case("Red"):
			banner.setImage(redBanner);
			break;
		case("Blue"):
			banner.setImage(blueBanner);
			break;
		case("Yellow"):
			banner.setImage(yellowBanner);
			break;
		case("Green"):
			banner.setImage(greenBanner);
			break;
		case("Purple"):
			banner.setImage(purpleBanner);
			break;
		default:
			System.out.println("ERROR");
			break;
		}
		header.removeAll();
		header.add(banner, "grow");
		header.repaint();
	}

	public String showPromptOptions(ArrayList<String> options, String message, String title) {
		String choice = (String) JOptionPane.showInputDialog(null, message, title,
    	        JOptionPane.QUESTION_MESSAGE, null, options.toArray(new Object[1]), options.get(0));
    	System.out.println("Chosen token: " + choice.toString());
        return choice;
	}
	
	public void endGame(Player winner) {
		inGame = false;
		JFrame victoryFrame = new JFrame("End Game");
		victoryFrame.setSize(600, 800);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		victoryFrame.setLocation(dim.width/2-victoryFrame.getSize().width/2, dim.height/2-victoryFrame.getSize().height/2);
		victoryFrame.setAlwaysOnTop(true);
		victoryFrame.setAutoRequestFocus(true);
		
		JPanel parent = new JPanel(new MigLayout("fill", "[]", "[][][][][]"));
		parent.setBackground(DARK_SAND);
		
		ImagePanel top = new ImagePanel("./res/wood.png", ImagePanel.TILE);
		top.setLayout(new MigLayout("fill"));
		parent.add(top, "grow, cell 0 0 1 2");
		
		ImagePanel scroll = new ImagePanel("./res/scroll2.png", ImagePanel.STRETCH);
		scroll.setLayout(new MigLayout("fill", "80[][][][]80", "20[][][][][]20"));
		top.add(scroll, "grow");
		
		JLabel text = new JLabel(winner.getName() + " has been victorious!", SwingConstants.CENTER);
		text.setFont(new Font("Book Antiqua", Font.BOLD, 24));
		scroll.add(text, "grow, cell 0 0 4 1");
		
		JPanel tokens = new JPanel();
		tokens.setOpaque(false);
		tokens.setLayout(new BoxLayout(tokens, BoxLayout.Y_AXIS));
		for (Token t : winner.getTokens()){
			JPanel panel = new JPanel(new MigLayout("fill"));
			panel.setOpaque(false);
			JLabel tokenLabel = new JLabel(t.toString());
			ImagePanel tokenIcon = null;
			
			
			if(t.getColour().equals("red")){
				tokenIcon = new ImagePanel(redToken, ImagePanel.CENTER_SCALE);
        	}else if(t.getColour().equals("blue")){
        		tokenIcon = new ImagePanel(blueToken, ImagePanel.CENTER_SCALE);
        	}else if(t.getColour().equals("yellow")){
        		tokenIcon = new ImagePanel(yellowToken, ImagePanel.CENTER_SCALE);
        	}else if(t.getColour().equals("green")){
        		tokenIcon = new ImagePanel(greenToken, ImagePanel.CENTER_SCALE);
        	}else if(t.getColour().equals("purple")){
        		tokenIcon = new ImagePanel(purpleToken, ImagePanel.CENTER_SCALE);
        	}
			tokenLabel.setFont(new Font("Book Antiqua", Font.BOLD, 16));
			panel.add(tokenIcon, "grow");
			panel.add(tokenLabel, "grow");
			tokens.add(panel);
		}
		scroll.add(tokens, "grow, cell 0 1 4 3");
		
		JButton back = new JButton("Return To Lobby");
		back.setFont(new Font("Book Antiqua", Font.ITALIC, 20));
		back.setOpaque(false);
		back.setBorderPainted(false);
		back.setBackground(DARK_SAND);
		back.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				enterLobby();
				victoryFrame.dispose();
			}
		});
		scroll.add(back, "grow, cell 0 4 4 1");
		
		ImagePanel img = new ImagePanel("./res/victory.png", ImagePanel.STRETCH);
		parent.add(img, "grow, cell 0 2 1 3");
		
		victoryFrame.add(parent);
		victoryFrame.setVisible(true);
	}
}
