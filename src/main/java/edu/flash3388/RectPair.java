package edu.flash3388;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;

public class RectPair implements Comparable<RectPair> {
	public RotatedRect rect1;
	public RotatedRect rect2;
	public MatOfPoint c1;
	public MatOfPoint c2;
	public double score;

	public RectPair(RotatedRect rect1, RotatedRect rect2) {
		if(rect1.center.x < rect2.center.x){
			this.rect1 = rect1;
			this.rect2 = rect2;
		}
		else {
			this.rect2 = rect1;
			this.rect1 = rect2;	
		}
		this.score = this.calcScore();
	}

	public double calcScore() {
		double angleScore = calcAngleScore(); // angle sum should be 180
		double yPosScore = calcYPosScore();
		double centerHeightScore = 1;//calcCenterHeightScore();
		double dimentionsScore = calcDimensionsScore();
		double heightScore = calcHeightScore();
		double widthScore = calcWidthScore();
		return (angleScore + yPosScore + centerHeightScore + dimentionsScore + widthScore + heightScore) / 6;
	}

	public double calcAngleScore() {
		return fixScore(((rect1.angle + rect2.angle) % 360) / 180.0);
	}
	
	public double calcCenterHeightScore() {
		return fixScore((rect1.boundingRect().height /centerDistance())/ (14.0/30.0));
		
	}
	public double calcDimensionsScore() {
		return 1.0;
	}
	public double calcHeightScore() { // for the missunderstand: opnecv catches the right rect's height as width
		return fixScore((double)rect1.boundingRect().height / rect2.boundingRect().width);
	}
	public double calcWidthScore() {
		return fixScore((double)rect1.boundingRect().width / rect2.boundingRect().height);
	}
	
	
	public double calcYPosScore() {
		double div;
		if (rect1.center.y >= rect2.center.y)
			div = rect2.center.y / rect1.center.y;
		else
			div = rect1.center.y / rect2.center.y;
		return div;
	}

	public double centerDistance() {
		double xdiff = Math.abs(rect1.center.x - rect2.center.x);
		return xdiff;
	}

	public double fixScore(double score) {
		if(score > 2)
			return 0.0;
		if (score > 1.0)
			score = 1.0 - (score - 1.0);
		return score;
	}
	
	public Point getCenter() {
		return new Point((rect2.center.x + rect1.center.x) / 2.0,
				(rect2.center.y + rect1.center.y) / 2.0);
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
