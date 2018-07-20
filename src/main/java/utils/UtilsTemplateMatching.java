package utils;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import datatype.Statement;

public class UtilsTemplateMatching {

	/* OpenCV bindings. */
	static {
		nu.pattern.OpenCV.loadShared();
	}

	/* The threshold ratio used for the distance. */
	static float nndrRatio = 1.0f;

	/* Ad-hoc visual locator detector feature. */
	public static Point featureDetectorAndTemplateMatching(String imageFile, String templateFile, Statement statement) {

		Point result = null;

		Set<Point> allMatches = new HashSet<Point>();

		/* run SIFT and FAST to check for the presence/absence of the template image. */
		boolean isPresent = runFeatureDetection(templateFile, imageFile, allMatches);

		if (isPresent) {
			result = templateMatchingBestResult(templateFile, imageFile, statement);
		}

		return result;

	}

	/*
	 * Get the best point out of all possible points by using dom information
	 */
	public static Set<Point> featureDetectorAndTemplateMatching_dom(String imageFile, String templateFile, Statement statement) {

		Set<Point> allMatches = new HashSet<Point>();

		/* run SIFT and FAST to check for the presence/absence of the template image. */
		boolean isPresent = runFeatureDetection(templateFile, imageFile, allMatches);

		if (isPresent) {
			Set<Point> templateMatches = new HashSet<Point>();
			templateMatches.add(templateMatchingBestResult(templateFile, imageFile, null));
			allMatches.addAll(templateMatches);
			return allMatches;
		}

		return null;

	}

	/*
	 * Run the FAST and SIFT feature detector algorithms on the two input images and
	 * try to match the features found in @object image into the @scene image
	 * 
	 */
	public static boolean runFeatureDetection(String templ, String img, Set<Point> allMatches) {
		boolean sift = siftDetector(templ, img, allMatches);
		boolean fast = fastDetector(templ, img, allMatches);

		boolean res = sift || fast;

		if (res) {
			System.out.println("[LOG]\tTemplate Present");
		}
		return res;
	}

	/*
	 * Run the FAST feature detector algorithms on the two input images and try to
	 * match the features found in @object image into the @scene image
	 * 
	 */
	private static boolean fastDetector(String object, String scene, Set<Point> allMatches) {

		// System.out.println("FAST Detector");
		Mat objectImage = Highgui.imread(object, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		Mat sceneImage = Highgui.imread(scene, Highgui.CV_LOAD_IMAGE_GRAYSCALE);

		MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint();
		FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.FAST);
		featureDetector.detect(objectImage, objectKeyPoints);
//		System.out.println("[LOG]\tFAST: Detecting key-points in templage image");

		MatOfKeyPoint objectDescriptors = new MatOfKeyPoint();
		DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
		descriptorExtractor.compute(objectImage, objectKeyPoints, objectDescriptors);
//		System.out.println("[LOG]\tFAST: Computing descriptors in templage image");

		/* Create output image. */
		Mat outputImage = new Mat(objectImage.rows(), objectImage.cols(), Highgui.CV_LOAD_IMAGE_COLOR);
		Scalar newKeypointColor = new Scalar(255, 0, 0);

		Features2d.drawKeypoints(objectImage, objectKeyPoints, outputImage, newKeypointColor, 0);

		/* Match object image with the scene image. */
		MatOfKeyPoint sceneKeyPoints = new MatOfKeyPoint();
		MatOfKeyPoint sceneDescriptors = new MatOfKeyPoint();
//		System.out.println("[LOG]\tFAST: Detecting key-points in reference image");
		featureDetector.detect(sceneImage, sceneKeyPoints);
//		System.out.println("[LOG]\tFAST: Computing descriptors in reference image");
		descriptorExtractor.compute(sceneImage, sceneKeyPoints, sceneDescriptors);

		Mat matchoutput = new Mat(sceneImage.rows() * 2, sceneImage.cols() * 2, Highgui.CV_LOAD_IMAGE_COLOR);
		Scalar matchestColor = new Scalar(0, 255, 0);

		List<MatOfDMatch> matches = new LinkedList<MatOfDMatch>();
		DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
//		System.out.println("[LOG]\tFAST: Matching descriptors");
		descriptorMatcher.knnMatch(objectDescriptors, sceneDescriptors, matches, 2);

		// System.out.println("Calculating good match list...");
		LinkedList<DMatch> goodMatchesList = new LinkedList<DMatch>();

		for (int i = 0; i < matches.size(); i++) {
			MatOfDMatch matofDMatch = matches.get(i);
			DMatch[] dmatcharray = matofDMatch.toArray();
			DMatch m1 = dmatcharray[0];
			DMatch m2 = dmatcharray[1];

			if (m1.distance <= m2.distance * nndrRatio) {
				goodMatchesList.addLast(m1);
			}
		}

		if (goodMatchesList.size() == 0) {
			return false;
		}

		// System.out.println("Good matches (FAST): " + goodMatchesList.size());

		int min_accepted_matches = (int) (objectKeyPoints.toList().size() * 0.3);

		// System.out.println("Min matches (FAST): " + min_accepted_matches);

		if (goodMatchesList.size() > min_accepted_matches) {

			// System.out.println("Object Found!");

			List<KeyPoint> objKeypointlist = objectKeyPoints.toList();
			List<KeyPoint> scnKeypointlist = sceneKeyPoints.toList();

			LinkedList<Point> objectPoints = new LinkedList<Point>();
			LinkedList<Point> scenePoints = new LinkedList<Point>();

			for (int i = 0; i < goodMatchesList.size(); i++) {
				objectPoints.addLast(objKeypointlist.get(goodMatchesList.get(i).queryIdx).pt);
				scenePoints.addLast(scnKeypointlist.get(goodMatchesList.get(i).trainIdx).pt);
			}

			// add the scenepoints to list of all matching points
			if (allMatches != null) {
				allMatches.addAll(scenePoints);
				// System.out.println(scenePoints);
			}

			MatOfPoint2f objMatOfPoint2f = new MatOfPoint2f();
			objMatOfPoint2f.fromList(objectPoints);
			MatOfPoint2f scnMatOfPoint2f = new MatOfPoint2f();
			scnMatOfPoint2f.fromList(scenePoints);

			/* output filename. */
			String filename = object.toString();
			int i = filename.lastIndexOf("/");
			filename = filename.substring(i + 1, filename.length());
			filename = filename.replace(".png", "");

			/* visualize detected features. */
			Highgui.imwrite("output/templateMatching/FAST-" + filename + "-outputImage.jpg", outputImage);

			/* Get the rectangle the the potential match is. */
			try {
				Mat homography = Calib3d.findHomography(objMatOfPoint2f, scnMatOfPoint2f, Calib3d.LMEDS, 3);
				Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
				Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

				obj_corners.put(0, 0, new double[] { 0, 0 });
				obj_corners.put(1, 0, new double[] { objectImage.cols(), 0 });
				obj_corners.put(2, 0, new double[] { objectImage.cols(), objectImage.rows() });
				obj_corners.put(3, 0, new double[] { 0, objectImage.rows() });

				// System.out.println("Transforming object corners to scene corners...");
				Core.perspectiveTransform(obj_corners, scene_corners, homography);

				Mat img = Highgui.imread(scene, Highgui.CV_LOAD_IMAGE_COLOR);

				Core.line(img, new Point(scene_corners.get(0, 0)), new Point(scene_corners.get(1, 0)), new Scalar(255, 0, 0), 2);
				Core.line(img, new Point(scene_corners.get(1, 0)), new Point(scene_corners.get(2, 0)), new Scalar(255, 0, 0), 2);
				Core.line(img, new Point(scene_corners.get(2, 0)), new Point(scene_corners.get(3, 0)), new Scalar(255, 0, 0), 2);
				Core.line(img, new Point(scene_corners.get(3, 0)), new Point(scene_corners.get(0, 0)), new Scalar(255, 0, 0), 2);

				// System.out.println("Drawing matches image...");
				MatOfDMatch goodMatches = new MatOfDMatch();
				goodMatches.fromList(goodMatchesList);

				Features2d.drawMatches(objectImage, objectKeyPoints, sceneImage, sceneKeyPoints, goodMatches, matchoutput, matchestColor, newKeypointColor, new MatOfByte(), 2);

				/* visualize feature detection. */
				Highgui.imwrite("output/templateMatching/FAST-" + filename + "-matchoutput.jpg", matchoutput);
				Highgui.imwrite("output/templateMatching/FAST-" + filename + "-img.jpg", img);
			} catch (Exception e) {
				System.out.println("Homography not found");
			}

			return true;

		} else {
			// System.out.println("Object Not Found");
			return false;
		}

	}

	/*
	 * Run the SIFT feature detector algorithms on the two input images and try to
	 * match the features found in @object image into the @scene image
	 * 
	 */
	private static boolean siftDetector(String object, String scene, Set<Point> allMatches) {

		// System.out.println("SIFT Detector");
		Mat objectImage = Highgui.imread(object, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		Mat sceneImage = Highgui.imread(scene, Highgui.CV_LOAD_IMAGE_GRAYSCALE);

		MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint();
		FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.SIFT);
		featureDetector.detect(objectImage, objectKeyPoints);
		System.out.println("[LOG]\tDetecting key-points in templage image");

		MatOfKeyPoint objectDescriptors = new MatOfKeyPoint();
		DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
		descriptorExtractor.compute(objectImage, objectKeyPoints, objectDescriptors);
		System.out.println("[LOG]\tComputing descriptors in template image");

		/* Create output image. */
		Mat outputImage = new Mat(objectImage.rows(), objectImage.cols(), Highgui.CV_LOAD_IMAGE_COLOR);
		Scalar newKeypointColor = new Scalar(255, 0, 0);

		Features2d.drawKeypoints(objectImage, objectKeyPoints, outputImage, newKeypointColor, 0);

		/* Match object image with the scene image. */
		MatOfKeyPoint sceneKeyPoints = new MatOfKeyPoint();
		MatOfKeyPoint sceneDescriptors = new MatOfKeyPoint();
		System.out.println("[LOG]\tDetecting key-points in reference image");
		featureDetector.detect(sceneImage, sceneKeyPoints);
		System.out.println("[LOG]\tComputing descriptors in reference image");
		descriptorExtractor.compute(sceneImage, sceneKeyPoints, sceneDescriptors);

		Mat matchoutput = new Mat(sceneImage.rows() * 2, sceneImage.cols() * 2, Highgui.CV_LOAD_IMAGE_COLOR);
		Scalar matchestColor = new Scalar(0, 255, 0);

		List<MatOfDMatch> matches = new LinkedList<MatOfDMatch>();
		DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
		System.out.println("[LOG]\tMatching descriptors");
		descriptorMatcher.knnMatch(objectDescriptors, sceneDescriptors, matches, 2);

		// System.out.println("Calculating good match list...");
		LinkedList<DMatch> goodMatchesList = new LinkedList<DMatch>();

		for (int i = 0; i < matches.size(); i++) {
			MatOfDMatch matofDMatch = matches.get(i);
			DMatch[] dmatcharray = matofDMatch.toArray();
			DMatch m1 = dmatcharray[0];
			DMatch m2 = dmatcharray[1];

			if (m1.distance <= m2.distance * nndrRatio) {
				goodMatchesList.addLast(m1);
			}
		}

		if (goodMatchesList.size() == 0) {
			return false;
		}

		// System.out.println("Good matches (SIFT): " + goodMatchesList.size());

		int min_accepted_matches = (int) (objectKeyPoints.toList().size() * 0.3);

		// System.out.println("Min matches (SIFT): " + min_accepted_matches);

		if (goodMatchesList.size() > min_accepted_matches) {

			// System.out.println("[LOG]\tTemplate Present!");

			List<KeyPoint> objKeypointlist = objectKeyPoints.toList();
			List<KeyPoint> scnKeypointlist = sceneKeyPoints.toList();

			LinkedList<Point> objectPoints = new LinkedList<Point>();
			LinkedList<Point> scenePoints = new LinkedList<Point>();

			for (int i = 0; i < goodMatchesList.size(); i++) {
				objectPoints.addLast(objKeypointlist.get(goodMatchesList.get(i).queryIdx).pt);
				scenePoints.addLast(scnKeypointlist.get(goodMatchesList.get(i).trainIdx).pt);
			}

			// add the scenepoints to list of all matching points
			if (allMatches != null) {
				allMatches.addAll(scenePoints);
				// System.out.println(scenePoints);
			}

			MatOfPoint2f objMatOfPoint2f = new MatOfPoint2f();
			objMatOfPoint2f.fromList(objectPoints);
			MatOfPoint2f scnMatOfPoint2f = new MatOfPoint2f();
			scnMatOfPoint2f.fromList(scenePoints);

			/* output filename. */
			String filename = object.toString();
			int index = filename.lastIndexOf("/");
			filename = filename.substring(index + 1, filename.length());
			filename = filename.replace(".png", "");

			/* visualize detected features. */
			Highgui.imwrite("output/templateMatching/SIFT-" + filename + "-outputImage.jpg", outputImage);

			try {

				/* Get the rectangle the the potential match is. */
				Mat homography = Calib3d.findHomography(objMatOfPoint2f, scnMatOfPoint2f, Calib3d.RANSAC, 3);
				Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
				Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

				obj_corners.put(0, 0, new double[] { 0, 0 });
				obj_corners.put(1, 0, new double[] { objectImage.cols(), 0 });
				obj_corners.put(2, 0, new double[] { objectImage.cols(), objectImage.rows() });
				obj_corners.put(3, 0, new double[] { 0, objectImage.rows() });

				// System.out.println("Transforming object corners to scene corners...");
				Core.perspectiveTransform(obj_corners, scene_corners, homography);

				Mat img = Highgui.imread(scene, Highgui.CV_LOAD_IMAGE_COLOR);

				Core.line(img, new Point(scene_corners.get(0, 0)), new Point(scene_corners.get(1, 0)), new Scalar(255, 0, 0), 2);
				Core.line(img, new Point(scene_corners.get(1, 0)), new Point(scene_corners.get(2, 0)), new Scalar(255, 0, 0), 2);
				Core.line(img, new Point(scene_corners.get(2, 0)), new Point(scene_corners.get(3, 0)), new Scalar(255, 0, 0), 2);
				Core.line(img, new Point(scene_corners.get(3, 0)), new Point(scene_corners.get(0, 0)), new Scalar(255, 0, 0), 2);

				// System.out.println("Drawing matches image...");
				MatOfDMatch goodMatches = new MatOfDMatch();
				goodMatches.fromList(goodMatchesList);

				Features2d.drawMatches(objectImage, objectKeyPoints, sceneImage, sceneKeyPoints, goodMatches, matchoutput, matchestColor, newKeypointColor, new MatOfByte(), 2);

				filename = object.toString();
				index = filename.lastIndexOf("/");
				filename = filename.substring(index + 1, filename.length());
				filename = filename.replace(".png", "");

				Highgui.imwrite("output/templateMatching/SIFT-" + filename + "-matchoutput.jpg", matchoutput);
				Highgui.imwrite("output/templateMatching/SIFT-" + filename + "-img.jpg", img);

			} catch (Exception e) {
				System.out.println("[LOG]\tHomography not found");
			}

			return true;

		} else {
			// System.out.println("Object Not Found");
			return false;
		}

	}

	/**
	 * Run the TM_CCOEFF_NORMED template matching algorithm when normalization has
	 * been applied to the results. Returns the center of the rectangle where the
	 * best match has been found.
	 * 
	 * @param templateFile
	 * @param imageFile
	 * @param statement
	 * @return
	 */
	private static Point templateMatchingBestResult(String templateFile, String imageFile, Statement statement) {

		System.out.println("[LOG]\tSearching the template position in the reference image");

		/* load the images in grayscale. */
		// Mat img = Highgui.imread(imageFile, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		// Mat templ = Highgui.imread(templateFile, Highgui.CV_LOAD_IMAGE_GRAYSCALE);

		/* load the images full color. */
		Mat img = Highgui.imread(imageFile);
		Mat templ = Highgui.imread(templateFile);

		// File t = new File("output/templateMatching/TM-template.png");
		// Highgui.imwrite(t.getPath(), templ);
		//
		// File o = new File("output/templateMatching/TM-imageoriginal.png");
		// Highgui.imwrite(o.getPath(), img);

		/* Create the result matrix. */
		int result_cols = img.cols() - templ.cols() + 1;
		int result_rows = img.rows() - templ.rows() + 1;
		Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

		/* Do the Matching and Normalize. */
		Imgproc.matchTemplate(img, templ, result, Imgproc.TM_CCOEFF_NORMED);
		Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

		// File risultato = new
		// File("output/templateMatching/TM-result-normalized.png");
		// Highgui.imwrite(risultato.getPath(), result);

		List<Point> matches = new LinkedList<Point>();

		for (int i = 0; i < result_rows; i++) {
			for (int j = 0; j < result_cols; j++) {

				if (result.get(i, j)[0] >= 0.99) {
					matches.add(new Point(i, j));
				}
			}
		}

		/* check the results. */
		if (matches.size() == 0) {
			System.err.println("[LOG]\tWARNING: No matches found!");
		} else if (matches.size() > 1) {
			System.err.println("[LOG]\tWARNING: Multiple matches: " + matches.size());
		}

		/* Localizing the best match with minMaxLoc. */
		MinMaxLocResult mmr = Core.minMaxLoc(result);
		Point matchLoc = mmr.maxLoc;

		// System.out.println("Max point found at: " + matchLoc.x + ", " + matchLoc.y);

		/* Draws a rectangle over the detected area. */
		Core.rectangle(img, matchLoc, new Point(matchLoc.x + templ.cols(), matchLoc.y + templ.rows()), new Scalar(0, 255, 0), 2);

		/* Draws a cross mark at the center of the detected area. */
		Core.line(img, new Point(matchLoc.x + templ.cols() / 2, (matchLoc.y + templ.rows() / 2) - 5), new Point(matchLoc.x + templ.cols() / 2, (matchLoc.y + templ.rows() / 2) + 5),
				new Scalar(0, 0, 255), 2);
		Core.line(img, new Point(((matchLoc.x + templ.cols() / 2) - 5), matchLoc.y + templ.rows() / 2), new Point(((matchLoc.x + templ.cols() / 2) + 5), matchLoc.y + templ.rows() / 2),
				new Scalar(0, 0, 255), 2);

		/* Draws the others detected matches. */
		for (Point point : matches) {
			/* Draws a rectangle over the detected area. */
			Core.rectangle(img, point, new Point(point.x + templ.cols(), point.y + templ.rows()), new Scalar(0, 0, 255), 1);
		}

		/* Save the visualized detection. */
		String filename = imageFile.toString();
		int i = filename.lastIndexOf("/");
		filename = filename.substring(i + 1, filename.length());
		filename = filename.replace(".png", "");
		File annotated = new File("output/templateMatching/TM-normalized-" + statement.getLine() + ".png");
		Highgui.imwrite(annotated.getPath(), img);

		/* Return the center. */
		return new Point(matchLoc.x + templ.cols() / 2, matchLoc.y + templ.rows() / 2);
	}

	/**
	 * Run the TM_CCOEFF_NORMED template matching algorithm when normalization has
	 * been applied to the results. Returns the center of the rectangle where the
	 * best match has been found.
	 * 
	 * @param templateFile
	 * @param imageFile
	 * @return
	 */
	private static Set<Point> templateMatchingAllResults(String templateFile, String imageFile) {

		/* load the images in grayscale. */
		// Mat img = Highgui.imread(imageFile, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		// Mat templ = Highgui.imread(templateFile, Highgui.CV_LOAD_IMAGE_GRAYSCALE);

		/* load the images full color. */
		Mat img = Highgui.imread(imageFile);
		Mat templ = Highgui.imread(templateFile);

		File t = new File("output/templateMatching/TM-template.png");
		Highgui.imwrite(t.getPath(), templ);

		File o = new File("output/templateMatching/TM-imageoriginal.png");
		Highgui.imwrite(o.getPath(), img);

		/* Create the result matrix. */
		int result_cols = img.cols() - templ.cols() + 1;
		int result_rows = img.rows() - templ.rows() + 1;
		Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

		/* Do the Matching and Normalize. */
		Imgproc.matchTemplate(img, templ, result, Imgproc.TM_CCOEFF_NORMED);
		Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

		File risultato = new File("output/templateMatching/TM-result-normalized.png");
		Highgui.imwrite(risultato.getPath(), result);

		List<Point> matches = new LinkedList<Point>();
		for (int i = 0; i < result_rows; i++) {
			for (int j = 0; j < result_cols; j++) {

				if (result.get(i, j)[0] >= 0.99) {
					matches.add(new Point(i, j));
				}
			}
		}

		/* check the results. */
		if (matches.size() == 0) {
			System.err.println("[LOG]\tWARNING: No matches found!");
		} else if (matches.size() > 1) {
			System.err.println("[LOG]\tWARNING: Multiple matches: " + matches.size());
		}

		Set<Point> allMatchesCenter = new HashSet<Point>();

		for (Point point : matches) {

			Point center = new Point(point.x + templ.cols() / 2, point.y + templ.rows() / 2);
			allMatchesCenter.add(center);

			/* Draws a rectangle over the detected area. */
			Core.rectangle(img, point, new Point(point.x + templ.cols(), point.y + templ.rows()), new Scalar(0, 255, 0), 2);

			/* Draws a cross mark at the center of the detected area. */
			Core.line(img, new Point(center.x + templ.cols() / 2, (center.y + templ.rows() / 2) - 5), new Point(center.x + templ.cols() / 2, (center.y + templ.rows() / 2) + 5), new Scalar(0, 0, 255),
					2);
			Core.line(img, new Point(((center.x + templ.cols() / 2) - 5), center.y + templ.rows() / 2), new Point(((center.x + templ.cols() / 2) + 5), center.y + templ.rows() / 2),
					new Scalar(0, 0, 255), 2);
		}

		/* Save the visualized detection. */
		String filename = imageFile.toString();
		int i = filename.lastIndexOf("/");
		filename = filename.substring(i + 1, filename.length());
		filename = filename.replace(".png", "");
		File annotated = new File("output/templateMatching/TM-normalized-" + filename + ".png");
		Highgui.imwrite(annotated.getPath(), img);

		/* Return all centers. */
		return allMatchesCenter;
	}

}
