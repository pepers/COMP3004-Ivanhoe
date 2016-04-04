package main.java;

import java.io.Serializable;

public abstract class Card implements Serializable{ 
	private static final long serialVersionUID = 1L;
	public abstract String toString();
	public abstract String toToolTip();
	public abstract Colour getColour();
	public abstract boolean equals(Object other);
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + toString().hashCode();
		return result;	
	}
}
