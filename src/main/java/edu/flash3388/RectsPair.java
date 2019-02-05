package edu.flash3388;

import org.opencv.core.RotatedRect;

public class RectsPair implements Comparable<RectsPair> {
	public RotatedRect rect1;
	public RotatedRect rect2;
	public double score;

	public RectsPair(RotatedRect rect1, RotatedRect rect2, double score) {
		this.rect1 = rect1;
		this.rect2 = rect2;
		this.score = score;
	}
	public RectsPair(RotatedRect rect1, RotatedRect rect2) {
		this.rect1 = rect1;
		this.rect2 = rect2;
		this.score = this.calcScore();
	}
	
	public double calcScore() {
		double angleScore = ((rect1.angle + rect2.angle) %360) / 180.0; //angle sum should be 180
			
		double yPosScore = calcYPosScore(rect1, rect2);
		
		//double distScore = (rect1.boundingRect().height) // might be error
		return (this.fixScore(angleScore) + yPosScore)/2;
	}
	private double calcYPosScore(RotatedRect rect1 ,RotatedRect rect2)
	{
		if(rect1.center.y >= rect2.center.y)
			return rect2.center.y / rect1.center.y;
		else
			return rect1.center.y / rect2.center.y;
	}
	public double fixScore(double score) {
		if(score > 1.0)
			score = 1.0 - (score - 1.0);
		return score;
	}
	@Override
	public int compareTo(RectsPair o) { // decreasing order
		if(score > o.score)
			return -1;
		else if(score < o.score)
			return 1;
		else
			return 0;
	}
}

