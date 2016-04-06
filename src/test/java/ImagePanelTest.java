package test.java;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import abbot.tester.ComponentTester;
import junit.extensions.abbot.ComponentTestFixture;
import junit.extensions.abbot.TestHelper;
import main.java.*;
import main.java.ClientView.ImagePanel;

public class ImagePanelTest extends ComponentTestFixture{
    private ComponentTester tester;
    protected void setUp() {
        tester = ComponentTester.getTester(ImagePanel.class);
    }
    public void testLoadImage() {
        ImagePanel cardback = new ClientView(null).new ImagePanel("./res/displayCards/cardback.png");
        showFrame(cardback);
        tester.assertImage(cardback, new File("./res/displayCards/cardback.png"), true);
     
    }
    
    public void testChangeImage() {
        ImagePanel cardback = new ClientView(null).new ImagePanel("./res/displayCards/cardback.png");
        showFrame(cardback);
        tester.assertImage(cardback, new File("./res/displayCards/cardback.png"), true);     
        try {
			cardback.setImage(ImageIO.read(new File("./res/displayCards/red5.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
        tester.assertImage(cardback, new File("./res/displayCards/red5.png"), true);
    }
    public ImagePanelTest(String name) { super(name); }

    public static void main(String[] args) {
        TestHelper.runTests(args, ImagePanelTest.class);
    }
}
