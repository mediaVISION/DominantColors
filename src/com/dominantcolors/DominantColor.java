package com.dominantcolors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class DominantColor {

	public static final int DEFAULT_NUM_COLORS = 3;
	public static final int DEFAULT_MIN_DIFF = 5;
	public static final int SIDE_SIZE = 200;

	public static int[] getDominantColors(Bitmap bitmap) {
		return getDominantColors(bitmap, DEFAULT_NUM_COLORS);
	}

	public static int[] getDominantColors(Bitmap bitmap, int numColors) {
		return getDominantColors(bitmap, numColors, DEFAULT_MIN_DIFF);
	}

	public static int[] getDominantColors(Bitmap bitmap, int numColors, int minDiff) {
		// scale down while maintaining aspect ratio
		if (bitmap.getWidth() > SIDE_SIZE || bitmap.getHeight() > SIDE_SIZE) {
			if (bitmap.getWidth() > bitmap.getHeight()) {
				bitmap = Bitmap.createScaledBitmap(bitmap, SIDE_SIZE, (int) (SIDE_SIZE*((float)bitmap.getHeight()/bitmap.getWidth())), false);
			} else {
				bitmap = Bitmap.createScaledBitmap(bitmap, (int) (SIDE_SIZE*((float)bitmap.getWidth()/bitmap.getHeight())), SIDE_SIZE, false);	
			}
		}
		return kMeans(getPoints(bitmap), numColors, minDiff);
	}

	private static int[] getPoints(Bitmap bitmap) {
		int[] points = new int[bitmap.getWidth() * bitmap.getHeight()];
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		// create the points in row-major order
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				points[j+i*width] = bitmap.getPixel(j, i);
		return points;
	}

	private static int[] kMeans(int[] points, int numColors, int minDiff) {
		// create the clusters
		int[] middles = getRandomMiddles(points, numColors);

		while (true) {
			// resample and resort the points
			ArrayList<Integer>[] newClusters = new ArrayList[numColors];
			for (int i = 0; i < numColors; i++)
				newClusters[i] = new ArrayList<Integer>();

			for (int point : points) {
				double minDist = Double.MAX_VALUE;
				int minId = 0;
				for (int i = 0; i < middles.length; i++) {
					double dist = euclideanDistance(point, middles[i]);
					if (dist < minDist) {
						minDist = dist;
						minId = i;
					}
				}
				newClusters[minId].add(point);
			}
			// copy the new cluster data into the old clusters
			double diff = 0;
			for (int i = 0; i < middles.length; i++) {
				int newCenter = calculateCenter(newClusters[i]);
				diff = Math.max(diff, euclideanDistance(newCenter, middles[i]));
				middles[i] = newCenter;
			}
			if (diff < minDiff)
				break;
		}

		// copy over and return the middles
		int[] colors = new int[numColors];
		for (int i = 0; i < middles.length; i++)
			colors[i] = middles[i];
		return colors;
	}

	private static int[] getRandomMiddles(int[] points, int numColors) {
		int[] middles = new int[numColors];
		ArrayList<Integer> indices = new ArrayList<Integer>();
		for (int i = 0; i < points.length; i++)
			indices.add(i);

		Collections.shuffle(indices);
		for (int i = 0; i < numColors; i++) {
			middles[i] = points[indices.get(i)];
		}
		return middles;
	}

	private static int calculateCenter(List<Integer> points) {
		int rSum, gSum, bSum;
		rSum = gSum = bSum = 0;
		for (int i : points) {
			rSum += Color.red(i);
			gSum += Color.green(i);
			bSum += Color.blue(i);
		}
		if (points.size() == 0) {
			return 0;
		} else {
			return Color.rgb(rSum/points.size(), 
					gSum/points.size(), 
					bSum/points.size());
		}
	}

	private static double euclideanDistance(int c1, int c2) {
		return Math.sqrt(
				Math.pow(Color.red(c1) - Color.red(c2), 2) + 
				Math.pow(Color.blue(c1) - Color.blue(c2), 2) + 
				Math.pow(Color.green(c1) - Color.green(c2), 2));
	}

}
