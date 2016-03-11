package main.java;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
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

import main.resources.Trace;
import net.miginfocom.swing.MigLayout;

public class ClientView extends JFrame {

	//TODO remove main
	
	public static void main(String args[]) throws InterruptedException{
		new ClientView(null);
		
		Deck d = new Deck();
		d.initialize();
		
		while(true){
			Thread.sleep(1000);
			ArrayList<Card> a = new ArrayList<Card>();
			a.add(d.draw());
			hand.update(a);
		}
	}
	
	private static final long serialVersionUID = 1L;
	private JPanel parent, header, title, arena, context, console;
	static CardPanel hand;
	private Client client;
	private Color sand = new Color(235, 210, 165);
	private Color dark_sand = new Color(133, 113, 72);

	public ClientView(Client c) {
		client = c;
		this.setSize(1240, 655);
		this.setResizable(false);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		parent = new JPanel(new MigLayout(
				"", 
				"5[200!]5[200!]5[200!]5[200!]5[200!]5[200!]5",
				"5[120!]5[120!]5[120!]5[120!]5[120!]5"));
		parent.setBackground(dark_sand);

		header = new ImagePanel("./res/cobblestone.png", ImagePanel.FILL);
		header.setToolTipText("header");
		parent.add(header, "cell 0 0 4 1, grow");

		title = new ImagePanel("./res/title.png");
		title.setBackground(sand);
		title.setToolTipText("title");
		parent.add(title, "cell 4 0 2 1, grow");

		arena = new ImagePanel("./res/sand.png", ImagePanel.FILL);
		arena.setToolTipText("arena");
		parent.add(arena, "cell 0 1 4 3, grow");

		context = new ImagePanel("./res/wood1.png");
		context.setToolTipText("context");
		parent.add(context, "cell 4 1 2 2, grow");

		hand = new CardPanel("./res/wood2.png");
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
				if(client != null)client.processInput(input.getText());
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

	// Hand visual structure
	class CardPanel extends ImagePanel {
		private static final long serialVersionUID = 1L;
		private HashMap<Card, BufferedImage> images;
		private ArrayList<Card> cards;

		public CardPanel(String s) {
			super(s);
			cards = new ArrayList<Card>();
			images = new HashMap<Card, BufferedImage>();
		}

		public void update(ArrayList<Card> newCards) {
			cards.addAll(newCards);
			try {
				for (Card c : cards) {
					if (images.containsKey(c)) {
						continue;
					}
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
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			this.repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			for (int i = 0; i < cards.size(); i++) {
				BufferedImage image = images.get(cards.get(i));
				if (image != null) {
					Trace.getInstance().write(this, "Drawing " + cards.get(i).toString());
					g.drawImage(changeSize(image, 100, 142), (i%7 * 50) + 5, 5 + (142 * (i/7)), null);
				}
			}
		}

		public BufferedImage changeSize(BufferedImage img, int newW, int newH) {
			Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
			BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = dimg.createGraphics();
			g2d.drawImage(tmp, 0, 0, null);
			g2d.dispose();
			return dimg;
		}

		public BufferedImage scale(BufferedImage img, double percent) {
			int w = img.getWidth();
			int h = img.getHeight();
			BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			AffineTransform at = new AffineTransform();
			at.scale(percent, percent);
			AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
			after = scaleOp.filter(img, after);
			return after;
		}

	}

	class ImagePanel extends JPanel {
		private static final long serialVersionUID = 1L;
		public static final int TILE = 2;
		public static final int STRETCH = 1;
		public static final int FILL = 3;

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
				g.drawImage(img, 0, 0, this.getWidth(), img.getHeight(null) * (this.getWidth() / img.getWidth(null)),
						null);
				break;
			default:
				int x = (this.getWidth() - img.getWidth(null)) / 2;
				int y = (this.getHeight() - img.getHeight(null)) / 2;
				g.drawImage(img, x, y, null);
				break;
			}
		}
	}
}
