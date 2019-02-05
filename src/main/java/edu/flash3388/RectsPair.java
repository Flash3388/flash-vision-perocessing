package edu.flash3388;

import org.opencv.core.RotatedRect;

public class RectsPair {
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
		//double distScore = (rect1.boundingRect().height) // might be error
		return this.fixScore(angleScore);
	}
	public double fixScore(double score) {
		if(score > 1.0)
			score = 1.0 - (score - 1.0);
		return score;
	}
}

