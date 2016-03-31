package main.java;

import java.io.Serializable;

public abstract class Card implements Serializable{ 
	private static final long serialVersionUID = 1L;
	public abstract String toString();
	public abstract String toToolTip();
	public abstract Colour getColour();
}
