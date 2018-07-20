import static org.junit.Assert.assertEquals;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.junit.Before;
import org.junit.Test;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import utils.RectangleComparator;
import utils.UtilsComputerVision;

public class TestOpenCV {

	@Before
	public void setUp() throws Exception {
		nu.pattern.OpenCV.loadShared();
		nu.pattern.OpenCV.loadLocally();
	}

	@Test
	public void testOpenCVWorks() {
		Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
		System.out.println("mat = " + mat.dump());
	}

	@Test
	public void testNonMaxSuppressionUtilityFunctions() {

		Rectangle2D r1 = new Rectangle2D.Double(100, 100, 50, 50);
		Rectangle2D r2 = new Rectangle2D.Double(120, 80, 54, 75);

		ArrayList<Rectangle2D> list = new ArrayList<Rectangle2D>();
		list.add(r1);
		list.add(r2);

		ArrayList<Integer> x1 = UtilsComputerVision.getAllX1(list);
		ArrayList<Integer> y1 = UtilsComputerVision.getAllY1(list);
		ArrayList<Integer> x2 = UtilsComputerVision.getAllX2(list);
		ArrayList<Integer> y2 = UtilsComputerVision.getAllY2(list);
		ArrayList<Double> area = UtilsComputerVision.getAllAreas(list);

		assertEquals("[100, 120]", x1.toString());
		assertEquals("[100, 80]", y1.toString());
		assertEquals("[150, 174]", x2.toString());
		assertEquals("[150, 155]", y2.toString());
		assertEquals("[2500.0, 4050.0]", area.toString());
	}

	@Test
	public void testRectangleComparator() {

		Rectangle2D r1 = new Rectangle2D.Double(100, 100, 50, 50);
		Rectangle2D r2 = new Rectangle2D.Double(120, 80, 54, 75);

		ArrayList<Rectangle2D> list = new ArrayList<Rectangle2D>();
		list.add(r1);
		list.add(r2);

		System.out.println("Original Ordering");
		RectangleComparator.print(list);

		ArrayList<Rectangle2D> idxs = new ArrayList<Rectangle2D>();
		idxs.addAll(list);

		Comparator<Rectangle2D> comp = new RectangleComparator();
		Collections.sort(idxs, comp);

		System.out.println("Ordering by bottom-right y coordinate");
		RectangleComparator.print(idxs);
	}

	@Test
	public void testNonMaxSuppression() {

		Rectangle2D r1 = new Rectangle2D.Double(100, 100, 52, 52);
		Rectangle2D r2 = new Rectangle2D.Double(100, 100, 51, 51);
		Rectangle2D r3 = new Rectangle2D.Double(100, 100, 50, 50);
		Rectangle2D r4 = new Rectangle2D.Double(100, 100, 49, 49);
		Rectangle2D r5 = new Rectangle2D.Double(100, 100, 48, 48);
		Rectangle2D r6 = new Rectangle2D.Double(100, 100, 47, 47);
		Rectangle2D r7 = new Rectangle2D.Double(100, 100, 46, 46);
		Rectangle2D r8 = new Rectangle2D.Double(100, 100, 45, 45);

		ArrayList<Rectangle2D> list = new ArrayList<Rectangle2D>();
		list.add(r1);
		list.add(r2);
		list.add(r3);
		list.add(r4);
		list.add(r5);
		list.add(r6);
		list.add(r7);
		list.add(r8);

		System.out.println("Before NMS");
		RectangleComparator.print(list);

		Rectangle2D result = UtilsComputerVision.nonMaxSuppression(list);

		System.out.println("After NMS");
		RectangleComparator.print(result);
	}

}
