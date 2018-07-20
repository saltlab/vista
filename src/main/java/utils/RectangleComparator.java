package utils;

import java.awt.geom.Rectangle2D;
import java.util.Comparator;
import java.util.List;

public class RectangleComparator implements Comparator<Rectangle2D> {

	/* upper-left corner rect1. */
	double x1_1;
	double y1_1;

	/* upper-left corner rect2. */
	double x1_2;
	double y1_2;

	/* width-height rect1. */
	double w1_1;
	double h1_1;

	/* width-height rect2. */
	double w2_2;
	double h2_2;

	/* bottom-right corner rect1. */
	double x2_1;
	double y2_1;

	/* bottom-right corner rect2. */
	double x2_2;
	double y2_2;

	@Override
	public int compare(Rectangle2D r1, Rectangle2D r2) {

		/* bottom-right corner rect1. */
		double y2_1 = (r1.getY() + r1.getHeight());

		/* bottom-right corner rect2. */
		double y2_2 = (r2.getY() + r2.getHeight());

		// order based on bottom-right corner, ascending
		if (y2_1 > y2_2) {
			return 1;
		} else if (y2_1 == y2_2) {
			return 0;
		} else
			return -1;

	}

	public static void print(List<Rectangle2D> boxes) {
		for (int i = 0; i < boxes.size(); i++) {
			System.out.println(" [" + i + "] y=" + boxes.get(i).getY() + "\th=" + boxes.get(i).getHeight() + "\ty2="
					+ (boxes.get(i).getY() + boxes.get(i).getHeight()));
		}
	}

	public static void print(Rectangle2D boxes) {

		System.out.println(
				"y=" + boxes.getY() + "\th=" + boxes.getHeight() + "\ty2=" + (boxes.getY() + boxes.getHeight()));

	}

}
