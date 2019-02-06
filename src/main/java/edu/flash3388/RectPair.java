package edu.flash3388;

import org.opencv.core.RotatedRect;

public class RectPair implements Comparable<RectPair> {
	public RotatedRect rect1;
	public RotatedRect rect2;
	public double score;

	public RectPair(RotatedRect rect1, RotatedRect rect2, double score) {
		this.rect1 = rect1;
		this.rect2 = rect2;
		this.score = score;
	}

	public RectPair(RotatedRect rect1, RotatedRect rect2) {
		this.rect1 = rect1;
		this.rect2 = rect2;
		this.score = this.calcScore();
	}

	public double calcScore() {
		double angleScore = fixScore(((rect1.angle + rect2.angle) % 360) / 180.0); // angle sum should be 180
		double yPosScore = calcYPosScore();
		double centerHeightScore = fixScore(this.calcCenterHeightScore());
		// double distScore = (rect1.boundingRect().height) // might be error
		return (angleScore + yPosScore + centerHeightScore) / 3;
	}
	
	private double calcCenterHeightScore() {
		return (centerDistance()/ getHeight())/ (30/14.0);
	}
	private double getHeight() { // in case that opencv was wrong, height is bigger
		return Math.max(rect1.boundingRect().height, rect1.boundingRect().width);
	}
	private double calcYPosScore() {
		if (rect1.center.y >= rect2.center.y)
			return rect2.center.y / rect1.center.y;
		else
			return rect1.center.y / rect2.center.y;
	}

	public double centerDistance() {
		double xdiff = Math.abs(rect1.center.x - rect2.center.x);
		return xdiff;
		// double ydiff = rect1.center.y - rect2.center.y;
		// return Math.sqrt(xdiff*xdiff + ydiff*ydiff);
	}

	public double fixScore(double score) {
		if (score > 1.0)
			score = 1.0 - (score - 1.0);
		return score;
	}

	@Override
	public int compareTo(RectPair o) { // decreasing order
		if (score > o.score)
			return -1;
		else if (score < o.score)
			return 1;
		else
			return 0;
	}
}
