package main.resources;

import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

public class ImageDownloader {
	
	static String home = "http://people.scs.carleton.ca/~jeanpier//304W16/project%20material/";
	
	public static void main(String[] args){
		ImageDownloader d = new ImageDownloader();
		d.fetchDisplayCards();
	}
	
	public void fetchDisplayCards(){
		URL url;
		Image image = null;
		
		//get supporters
		for (int i = 0; i < 18; i++){
			image = null;
			try {
				url = new URL(home + "simpleCards/simpleCards" + ((i==0) ? "":i) + ".jpeg");
			    image = ImageIO.read(url.openStream());
			    BufferedImage imageCropped = ((BufferedImage) image).getSubimage(9, 20, 985, 692);

			    imageCropped = rotate90(imageCropped);
			    
			    File f = new File("./res/displaycards/" + displayNames[i] + ".png");
			    System.out.println(f.getAbsolutePath());
			    ImageIO.write((RenderedImage) imageCropped, "png", f);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(image);
		}
	}
	String[] displayNames = {
			"squire2",
			"squire3",
			"maiden6",
			"green1",
			"yellow2",
			"yellow3",
			"yellow4",
			"red3",
			"red4",
			"red5",
			"blue2",
			"blue3",
			"blue4",
			"blue5",
			"purple3",
			"purple4",
			"purple5",
			"purple7"
	};
	public BufferedImage rotate90(BufferedImage bi) {
	    int width = bi.getWidth();
	    int height = bi.getHeight();
	    BufferedImage biFlip = new BufferedImage(height, width, bi.getType());
	    for(int i=0; i<width; i++)
	        for(int j=0; j<height; j++)
	            biFlip.setRGB(j, i, bi.getRGB(i, height-1-j));
	    return biFlip;
	}
}
