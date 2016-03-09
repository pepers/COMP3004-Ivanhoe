package main.resources;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

public class ImageDownloader {
	
	public static void main(String[] args){
		ImageDownloader d = new ImageDownloader();
		d.fetchDisplayCards();
	}
	
	public void fetchCards(String[] names, String path, String folder){
		URL url;
		Image image = null;
		System.out.println("Downloading to ("+ folder +")...");
		int count = 0;
		for (int i = 0; i < names.length; i++){
			image = null;
			try {
				File f = new File("./res/"+folder+"/" + names[i] + ".png");
				if(f.exists()){continue;}
				f.mkdirs();
				url = new URL(path + ((i==0) ? "":i) + ".jpeg");
			    image = ImageIO.read(url.openStream());
			    BufferedImage imageCropped = rotate90(((BufferedImage) image).getSubimage(9, 20, 985, 692));
			    ImageIO.write((RenderedImage) imageCropped, "png", f);
			    System.out.println("  " + names[i] + " - s");
			    count++;
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("  " + names[i] + " - f");
			}
		}
		System.out.println("Done! Fetched (" + count + "/"+names.length+")");
	}
	
	public void fetchDisplayCards(){
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
				"purple7",
				"cardback"
		};
		fetchCards(
				displayNames,
				"http://people.scs.carleton.ca/~jeanpier//304W16/project%20material/simpleCards/simpleCards",
				"displaycards");
	}
	
	public void fetchActionCards(){
		String[] actionNames = {
				"dodge",
				"disgrace",
				"retreat",
				"riposte",
				"outmaneuver",
				"countercharge",
				"charge",
				"breaklance",
				"adapt",
				"dropweapon",
				"changeweapon",
				"unhorse",
				"knockdown",
				"outwit",
				"shield",
				"stunned",
				"ivanhoe"
		};
		fetchCards(
				actionNames,
				"http://people.scs.carleton.ca/~jeanpier//304W16/project%20material/actionCards/actionCards",
				"actioncards");
	}
	
	
	
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
