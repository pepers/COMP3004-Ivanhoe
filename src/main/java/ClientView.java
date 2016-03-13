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

import main.resources.Trace;
import net.miginfocom.swing.MigLayout;

public class ClientView extends JFrame {

	// TODO remove main
	public static void main(String args[]) throws InterruptedException {
		new ClientView(null);
	
		/*
		Deck d = new Deck();
		d.initialize();
		ArrayList<Card> cards = new ArrayList<Card>();
		ArrayList<Player> players = new ArrayList<Player>();
		players.add(new Player("khalil"));
		
		
		arena.update(players);
		while(cards.size()<7){
			cards.add(d.draw());
			hand.update(cards);
		}
		while(players.get(0).getDisplay().size() < 6){
			Thread.sleep(1000);
			players.get(0).addToDisplay(d.draw());
			players.add(new Player("p#"));
			arena.update(players);
		}
		*/
		
	}

	private static Image cardback;
	private static final long serialVersionUID = 1L;
	private JPanel parent, header, title, controls, console;
	public CardPanel hand;
	public DisplayPanel arena;
	private Client client;
	private Color sand = new Color(235, 210, 165);
	private Color dark_sand = new Color(133, 113, 72);

	public ClientView(Client c) {
		
		try {
			cardback = ImageIO.read(new File("./res/displaycards/cardback.png"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		client = c;
		this.setResizable(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize(1255, 685);
		parent = new JPanel(new MigLayout("fill", 
				"5[200::]5[200::]5[200::]5[200::]5[200::]5[200::]5",
				"5[120::]5[120::]5[120::]5[120::]5[120::]20"));
		parent.setBackground(dark_sand);

		header = new ImagePanel("./res/cobblestone.png", ImagePanel.TILE);
		header.setToolTipText("header");
		parent.add(header, "cell 0 0 4 1, grow");

		title = new ImagePanel("./res/title.png", ImagePanel.CENTER_SCALE);
		title.setBackground(sand);
		title.setToolTipText("title");
		parent.add(title, "cell 4 0 2 1, grow");

		arena = new DisplayPanel("./res/sand.png", ImagePanel.TILE);
		arena.setToolTipText("arena");
		arena.setLayout(new GridLayout(1,0));
		parent.add(arena, "cell 0 1 4 3, grow");

		controls = new ImagePanel("./res/wood1.png", ImagePanel.TILE);
		controls.setToolTipText("context");
		controls.setLayout(new MigLayout("fill", "5[200::]5[200::]5", "5[]5"));
		
		ImagePanel cardContext = new ImagePanel("./res/displaycards/cardback.png", ImagePanel.CENTER_SCALE);
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
		
		JButton endTurn = new JButton();
		endTurn.setText("End Turn");
		endTurn.setAlignmentX(CENTER_ALIGNMENT);
		endTurn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                if(client != null)client.cmdEnd();
            }
        });      
		
		buttons.add(Box.createRigidArea(new Dimension(0, 40)));
		buttons.add(endTurn);
		
		controls.add(buttons, "cell 0 0, grow");
		controls.add(cardContext, "cell 1 0, grow");
		parent.add(controls, "cell 4 1 2 2, grow");

		hand = new CardPanel("./res/wood2.png", ImagePanel.TILE);
		hand.setToolTipText("hand");
		parent.add(hand, "cell 4 3 2 2, grow");

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

		this.getContentPane().add(parent);
		this.setVisible(true);
	}

	public void writeConsole(String s, int type) {
		try {
			JTextPane pane = ((JTextPane) ((JScrollPane) console.getComponent(0)).getViewport().getView());
			StyledDocument doc = pane.getStyledDocument();
			Style style = pane.addStyle("", null);

			if (type == 1) {
				StyleConstants.setForeground(style, Color.yellow);
			} else {
				style = null;
			}

			doc.insertString(doc.getLength(), ("\n " + s), style);
			pane.selectAll();
		} catch (BadLocationException exc) {
			exc.printStackTrace();
		}
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
				int i = 0;
				while (i < this.getWidth()) {
					g.drawImage(img, i, 0, null);
					i += img.getWidth(null);
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
		private HashMap<Card, BufferedImage> images;
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
				}
				CardView view = new CardView(c, images.get(c));
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
		
		public CardView(Card c, BufferedImage i) {
			card = c;
			img = i;
			
			this.setSize(new Dimension(100, 142));
			this.setOpaque(true);
			this.setVisible(true);
			
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					//System.out.println(card.toString() + " pressed");
				}
				@Override
				public void mouseReleased(MouseEvent e) {
					if (client != null) {
						if (card instanceof DisplayCard) {
							DisplayCard selected =(DisplayCard)card;
							if (client.getGameState().getTournament() == null) {
								client.cmdTournament(new String[]{selected.getColour().toString(), selected.toString()});
							} else {
								client.cmdPlay(selected.toString());
							}
						}else{
							ActionCard selected =(ActionCard)card;
							
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
			g2.drawImage(img,0,0, 100, 142, null);
			if(mouseOver){
				g2.setColor(new Color(0, 0, 0, 70));
				g2.setStroke(new BasicStroke(40));
				g2.drawRect(0, 0, 100, 142);
			}
		}

		@Override
		public Dimension getPreferredSize(){
		    return new Dimension(100, 142);
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
		
		Player player;
		Display display;
		JTextArea text;
		
		public DisplayView(Player p){
			Random r = new Random();
			setBackground(new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)));
			text = new JTextArea();
			text.setOpaque(false);
			text.setForeground(Color.white);
			this.add(text);
			update(p);
		}
		
		public void update(Player p){
			player = p;
			display = p.getDisplay();
			setSize(100, (display.size() * 30) + 100);
			text.append(p.getName() + "\n");
			for (Card c : display.elements()){
				text.append(c.toString() + "\n");
			}
		}
		
		@Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
        }
	}
}
