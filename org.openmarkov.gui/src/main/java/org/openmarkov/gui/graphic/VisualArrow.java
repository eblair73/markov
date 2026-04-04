/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.graphic;

import org.openmarkov.core.exception.UnreachableException;

import java.awt.*;
import java.awt.geom.*;

import org.openmarkov.core.model.network.Point2D;

/**
 * This class is the visual representation of a link.
 *
 * @author jmendoza
 * @version 1.0
 */
public class VisualArrow extends VisualElement {

	/**
	 * Width of the top of arrow.
	 */
	private static final double WIDTH_TOP_ARROW = 8;

	/**
	 * Height of the top of arrow.
	 */
	private static final double HEIGHT_TOP_ARROW = 12;

	/**
	 * This constant contains the value of the units that the line must
	 * increment its width qto be selected with the mouse.
	 */
	private static final double WIDTH_LINE_TO_SELECT = 2;

	/***
	 * Heigth of the stripe
	 */
	private static final double HEIGTH_STRIPE = 6;

	/***
	 * Distance between stripes
	 */
	private static final double STRIPE_DISTANCE = 3;

	/**
	 * Color of lines.
	 */
	private static final Color FOREGROUND_COLOR = Color.DARK_GRAY;

	/**
	 * Start point.
	 */
    private Point2D.Double startPoint;

	/**
	 * End point.
	 */
    private Point2D.Double endPoint;

	/**
	 * Is the link directed
	 */
	private boolean isDirected;

	/**
	 * Is the link double striped
	 */
	private boolean isDoubleStriped;

	/**
	 * Is the link single striped
	 */
	private boolean isSingleStriped;

	private Color linkColor = FOREGROUND_COLOR;

	/**
	 * Creates a new visual link from the two points that define the start and
	 * the end of the arrow.
	 *
	 * @param newStartPoint the starting point of the arrow.
	 * @param newEndPoint   the ending point of the arrow.
	 */
	public VisualArrow(Point2D.Double newStartPoint, Point2D.Double newEndPoint, boolean isDirected) {
		this.startPoint = newStartPoint;
		this.endPoint = newEndPoint;
		this.isDirected = isDirected;
	}

	public VisualArrow(Point2D.Double newStartPoint, Point2D.Double newEndPoint) {
		this(newStartPoint, newEndPoint, true);
	}

	/**
	 * Calculates the nine points of the line with top of arrow. /** Calculates
	 * the nine points of the line with top of arrow.
	 * <p>
	 * <pre>
	 * 0
	 * </pre>
	 * <p>
	 * <pre>
	 *         * *
	 * </pre>
	 * <p>
	 * <pre>
	 *        *   *
	 * </pre>
	 * <p>
	 * <pre>
	 *       *     *
	 * </pre>
	 * <p>
	 * <pre>
	 *      *       *
	 * </pre>
	 * <p>
	 * <pre>
	 *     *         *
	 * </pre>
	 * <p>
	 * <pre>
	 *    *           *
	 * </pre>
	 * <p>
	 * <pre>
	 *   *             *
	 * </pre>
	 * <p>
	 * <pre>
	 *  1****2**3**4****5
	 * </pre>
	 * <p>
	 * <pre>
	 *       *     *
	 * </pre>
	 * <p>
	 * <pre>
	 *       *     *
	 * </pre>
	 * <p>
	 * <pre>
	 *       *     *
	 * </pre>
	 * <p>
	 * <pre>
	 *       *     *
	 * </pre>
	 * <p>
	 * <pre>
	 *       *     *
	 * </pre>
	 * <p>
	 * <pre>
	 *       *     *
	 * </pre>
	 * <p>
	 * <pre>
	 *       *     *
	 * </pre>
	 * <p>
	 * <pre>
	 *       *     *
	 * </pre>
	 * <p>
	 * <pre>
	 *       6**7**8
	 * </pre>
	 *
	 * @param start start of the line.
	 * @param end   end of the line.
	 * @return return an array that contains the coordinates of the nine points.
	 */
	private static Point2D.Double[] calculatePointsOfArrow(Point2D.Double start, Point2D.Double end) {
        
        
        double tx = start.getX();
		double ty = start.getY();
		double angle = Math.atan((end.getY() - ty) / (end.getX() - tx));
        java.awt.geom.Point2D.Double[] points = new java.awt.geom.Point2D.Double[9];
        double halfWidth = WIDTH_TOP_ARROW / 2;
        int index;
        AffineTransform transformation2D = new AffineTransform();

		transformation2D.rotate(-angle);
		transformation2D.translate(-tx, -ty);
        points[0] = new java.awt.geom.Point2D.Double();
        
        java.awt.geom.Point2D.Double swingEnd = new java.awt.geom.Point2D.Double(end.x, end.y);
        transformation2D.transform(swingEnd, points[0]);
        end.setLocation(swingEnd.getX(), swingEnd.getY());
        
        double incrHeight = (points[0].getX() >= 0) ? HEIGHT_TOP_ARROW : -HEIGHT_TOP_ARROW;
        points[1] = new java.awt.geom.Point2D.Double(points[0].getX() - incrHeight, points[0].getY() - halfWidth);
        points[3] = new java.awt.geom.Point2D.Double(points[1].getX(), points[0].getY());
        points[2] = new java.awt.geom.Point2D.Double(points[3].getX(), points[3].getY() - WIDTH_LINE_TO_SELECT);
        points[4] = new java.awt.geom.Point2D.Double(points[3].getX(), points[3].getY() + WIDTH_LINE_TO_SELECT);
        points[5] = new java.awt.geom.Point2D.Double(points[1].getX(), points[0].getY() + halfWidth);
        points[7] = new java.awt.geom.Point2D.Double(0, 0);
        points[6] = new java.awt.geom.Point2D.Double(points[7].getX(), points[7].getY() - WIDTH_LINE_TO_SELECT);
        points[8] = new java.awt.geom.Point2D.Double(points[7].getX(), points[7].getY() + WIDTH_LINE_TO_SELECT);
		try {
			transformation2D = transformation2D.createInverse();
		} catch (NoninvertibleTransformException e) {
            throw new UnreachableException(e);
		}
        int length = points.length;
		for (index = 0; index < length; index++) {
			transformation2D.transform(points[index], points[index]);
		}
        
        
        Point2D.Double[] corePoints = new Point2D.Double[9];
        for (index = 0; index < corePoints.length; index++) {
            corePoints[index] = new Point2D.Double(points[index].x, points[index].y);
        }
        return corePoints;

	}

	/**
	 * Sets the starting point of the arrow.
	 *
	 * @param point new starting point.
	 */
	public void setStartPoint(Point2D.Double point) {
		startPoint = point;
	}

	/**
	 * Sets the ending point of the arrow.
	 *
	 * @param point new ending point.
	 */
	public void setEndPoint(Point2D.Double point) {
		endPoint = point;
	}

	/**
	 * Returns the shape of the arrow so that it can be selected with the mouse.
	 *
	 * @return shape of the arrow.
	 */
	@Override public Shape getShape(Graphics2D g) {
        
        GeneralPath polygon;
        int index;
        int length;
        Point2D.Double[] allPoints;
        Point2D.Double[] points;
        
        Point2D.Double pStart = startPoint;
        Point2D.Double pEnd = endPoint;

		if ((pStart != null) && (pEnd != null)) {
			allPoints = calculatePointsOfArrow(pStart, pEnd);
			points = new Point2D.Double[8];
			points[0] = allPoints[0];
			points[1] = allPoints[1];
			points[2] = allPoints[2];
			points[3] = allPoints[6];
			points[4] = allPoints[8];
			points[5] = allPoints[4];
			points[6] = allPoints[5];
			points[7] = allPoints[0];
            polygon = new GeneralPath(Path2D.WIND_EVEN_ODD, points.length);
			polygon.moveTo((float) points[0].getX(), (float) points[0].getY());
			length = points.length;
			for (index = 1; index < length; index++) {
				polygon.lineTo((float) points[index].getX(), (float) points[index].getY());
			}
			polygon.closePath();
		} else {
            polygon = new GeneralPath(Path2D.WIND_EVEN_ODD, 0);
		}

		return polygon;

	}

	/**
	 * Returns the shape of the arrow as it must be painted.
	 *
	 * @return shape of the arrow.
	 */
    private static Shape getShapeToPaint(Point2D.Double start, Point2D.Double end) {
        
        int index;
        Point2D.Double[] allPoints = calculatePointsOfArrow(start, end);
		Point2D.Double[] points = new Point2D.Double[7];

		points[0] = allPoints[0];
		points[1] = allPoints[1];
		points[2] = allPoints[3];
		points[3] = allPoints[7];
		points[4] = allPoints[3];
		points[5] = allPoints[5];
		points[6] = allPoints[0];
        GeneralPath polygon = new GeneralPath(Path2D.WIND_EVEN_ODD, points.length);
		polygon.moveTo((float) points[0].getX(), (float) points[0].getY());
        int length = points.length;
		for (index = 1; index < length; index++) {
			polygon.lineTo((float) points[index].getX(), (float) points[index].getY());
		}
		polygon.closePath();

		return polygon;

	}

	/**
	 * Returns the line to be painted for undirected links.
	 *
	 * @return shape of the line.
	 */
    private static Shape getLineToPaint(Point2D.Double start, Point2D.Double end) {
        
        int index;
        
        Point2D.Double[] points = new Point2D.Double[2];

		points[0] = start;
		points[1] = end;
        
        GeneralPath polygon = new GeneralPath(Path2D.WIND_EVEN_ODD, points.length);
		polygon.moveTo((float) points[0].getX(), (float) points[0].getY());
        int length = points.length;
		for (index = 1; index < length; index++) {
			polygon.lineTo((float) points[index].getX(), (float) points[index].getY());
		}
		polygon.closePath();

		return polygon;
	}

	/**
	 * Paints the arrow into the graphics object.
	 *
	 * @param g graphics object where paint the link.
	 */
    
    public static void paintArrow(Graphics2D g, Point2D.Double start, Point2D.Double end, Stroke stroke) {
        Shape shape;
		if ((start != null) && (end != null)) {
			if ((Math.abs(start.getX() - end.getX()) > 0.01) || (Math.abs(start.getY() - end.getY()) > 0.01)) {
				g.setStroke(stroke);
				shape = getShapeToPaint(start, end);
				g.fill(shape);
				g.draw(shape);
			}
		}
	}

	/**
	 * Paints a double stripe (two perpendicular marks) at the midpoint of the link,
	 * indicating a total link restriction.
	 *
	 * @param g      graphics object where to paint the link
	 * @param start  starting point of the link
	 * @param end    ending point of the link
	 * @param stroke the stroke to use for painting
	 */
    public static void paintDoubleStripe(Graphics2D g, Point2D.Double start, Point2D.Double end, Stroke stroke) {
        
        Shape shape;
		if ((start != null) && (end != null)) {
			if ((Math.abs(start.getX() - end.getX()) > 0.01) || (Math.abs(start.getY() - end.getY()) > 0.01)) {
				g.setStroke(stroke);
				shape = getStripeShape(start, end, STRIPE_DISTANCE);
				g.fill(shape);
				g.draw(shape);
				shape = getStripeShape(start, end, -STRIPE_DISTANCE);
				g.fill(shape);
				g.draw(shape);
			}
		}

	}

	/**
	 * Paints a single stripe (one perpendicular mark) at the midpoint of the link,
	 * indicating a partial link restriction.
	 *
	 * @param g      graphics object where to paint the link
	 * @param start  starting point of the link
	 * @param end    ending point of the link
	 * @param stroke the stroke to use for painting
	 */
    public static void paintSingleStripe(Graphics2D g, Point2D.Double start, Point2D.Double end, Stroke stroke) {
        
        Shape shape;
		if ((start != null) && (end != null)) {
			if ((Math.abs(start.getX() - end.getX()) > 0.01) || (Math.abs(start.getY() - end.getY()) > 0.01)) {
				g.setStroke(stroke);
				shape = getStripeShape(start, end, 0);
				g.fill(shape);
				g.draw(shape);
			}
		}

	}

	/**
	 * Returns the shape of a perpendicular stripe at the midpoint of a link segment.
	 *
	 * @param start    starting point of the link
	 * @param end      ending point of the link
	 * @param distance lateral offset from the midpoint (0 for centered, positive/negative for offset)
	 * @return the stripe shape to paint
	 */
    public static Shape getStripeShape(Point2D.Double start, Point2D.Double end, double distance) {
		double mx = (end.getX() - start.getX()) / 2;
		double my = (end.getY() - start.getY()) / 2;
        java.awt.geom.Point2D.Double firstPoint = new java.awt.geom.Point2D.Double(0, -HEIGTH_STRIPE);
        java.awt.geom.Point2D.Double secondPoint = new java.awt.geom.Point2D.Double(0, HEIGTH_STRIPE);
		double tx = start.getX();
		double ty = start.getY();
		double angle = Math.atan((end.getY() - ty) / (end.getX() - tx));
		AffineTransform transformation2D = new AffineTransform();
		transformation2D.translate(-distance, 0);
		transformation2D.rotate(-angle);
		transformation2D.translate(-tx - mx, -ty - my);

		try {
			transformation2D = transformation2D.createInverse();
		} catch (NoninvertibleTransformException e) {
            throw new UnreachableException(e);
		}
        transformation2D.transform(firstPoint, firstPoint);
        transformation2D.transform(secondPoint, secondPoint);
        
        GeneralPath polygon = new GeneralPath(Path2D.WIND_EVEN_ODD, 2);
        
        polygon.moveTo((float) firstPoint.getX(), (float) firstPoint.getY());
        polygon.lineTo((float) secondPoint.getX(), (float) secondPoint.getY());
		polygon.closePath();
		return polygon;

	}

	/**
	 * Paints the line into the graphics object.
	 *
	 * @param g      graphics object where paint the link.
	 * @param stroke the stroke
	 */
    
    public static void paintLine(Graphics2D g, Point2D.Double start, Point2D.Double end, Stroke stroke) {
        Shape shape;
		if ((start != null) && (end != null)) {
			if ((Math.abs(start.getX() - end.getX()) > 0.01) || (Math.abs(start.getY() - end.getY()) > 0.01)) {
				g.setStroke(stroke);
				shape = getLineToPaint(start, end);
				g.draw(shape);
			}
		}
	}

	/**
	 * Paints the visual link into the graphics object.
	 *
	 * @param g graphics object where paint the link.
	 */
	@Override public void paint(Graphics2D g) {

		g.setPaint(linkColor);
		Stroke stroke = getStroke();
		if (isDoubleStriped) {
			paintDoubleStripe(g, startPoint, endPoint, stroke);
		}
		if (isSingleStriped) {
			paintSingleStripe(g, startPoint, endPoint, stroke);
		}
		if (isDirected) {
			// Paint the arrow while the user has not released the button of the
			// mouse
			paintArrow(g, startPoint, endPoint, stroke);
		} else {
			paintLine(g, startPoint, endPoint, stroke);
		}
	}

	protected Stroke getStroke() {
		return (isSelected()) ? WIDE_STROKE : NORMAL_STROKE;
	}

	/**
	 * @return the isDirected
	 */
	public boolean isDirected() {
		return isDirected;
	}

	/**
	 * @param isDirected the isDirected to set
	 */
	public void setDirected(boolean isDirected) {
		this.isDirected = isDirected;
	}

	/**
	 * @return the isDoubleStriped
	 */
	public boolean isDoubleStriped() {
		return isDoubleStriped;
	}

	/**
	 * @param isDoubleStriped the isDoubleStriped to set
	 */
	public void setDoubleStriped(boolean isDoubleStriped) {
		this.isDoubleStriped = isDoubleStriped;
	}

	/**
	 * @param linkColor the linkColor to set
	 */
	public void setLinkColor(Color linkColor) {
		this.linkColor = linkColor;
	}

	/**
	 * @return the isSingleStriped
	 */
	public boolean isSingleStriped() {
		return isSingleStriped;
	}

	/**
	 * @param isSingleStriped the isSingletriped to set
	 */
	public void setSingleStriped(boolean isSingleStriped) {
		this.isSingleStriped = isSingleStriped;
	}

}
