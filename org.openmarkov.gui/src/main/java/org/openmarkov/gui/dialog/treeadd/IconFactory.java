/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.treeadd;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Factory for creating small node-type icons (chance, decision, utility) used
 * in the TreeADD cell renderer to visually distinguish variable types.
 */
abstract public class IconFactory {

	/**
	 * Creates a chance-node icon (rounded rectangle) with the given text.
	 *
	 * @param text the label to render inside the icon
	 * @param f    the font used for the label text
	 * @return the chance icon
	 */
	public static Icon createChanceIcon(String text, Font f) {
		FontRenderContext fr = new FontRenderContext(null, false, false);
		TextLayout t = new TextLayout(text, f, fr);

		int hMargin = 6;
		int vMargin = 6;

		Rectangle2D r = t.getBounds();
		int width = (int) r.getWidth() + 2 * (hMargin + 1);
		int height = (int) r.getHeight() + 2 * vMargin;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

		int ovalWidth = Math.min(height, width);

		g.setColor(new Color(251, 249, 153));
		g.fillArc(0, 0, ovalWidth, height - 1, 90, 180);
		g.fillArc(width - ovalWidth - 1, 0, ovalWidth, height - 1, 270, 180);
		g.fillRect(ovalWidth / 2, 0, width - ovalWidth, height - 1);

		g.setColor(Color.black);
		g.drawArc(0, 0, ovalWidth, height - 1, 90, 180);
		g.drawArc(width - ovalWidth - 1, 0, ovalWidth, height - 1, 270, 180);
		g.drawLine(ovalWidth / 2, 0, width - ovalWidth / 2, 0);
		g.drawLine(ovalWidth / 2, height - 1, width - ovalWidth / 2, height - 1);

		t.draw(g, hMargin, height - vMargin - 1);

		return new ImageIcon(image);
	}

	/**
	 * Creates a decision-node icon (rectangle) with the given text.
	 *
	 * @param text the label to render inside the icon
	 * @param f    the font used for the label text
	 * @return the decision icon
	 */
	public static Icon createDecisionIcon(String text, Font f) {
		FontRenderContext fr = new FontRenderContext(null, false, false);
		TextLayout t = new TextLayout(text, f, fr);

		int hMargin = 6;
		int vMargin = 6;

		Rectangle2D r = t.getBounds();
		int width = (int) r.getWidth() + 2 * hMargin;
		int height = (int) r.getHeight() + 2 * vMargin;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) image.getGraphics();

		g.setColor(new Color(207, 227, 253));
		g.fillRect(0, 1, width - 2, height - 2);
		g.setColor(Color.black);
		g.drawRect(0, 1, width - 2, height - 2);

		t.draw(g, hMargin, height - vMargin);

		return new ImageIcon(image);
	}

	/**
	 * Creates a utility-node icon (hexagon) with the given text.
	 *
	 * @param text the label to render inside the icon
	 * @param f    the font used for the label text
	 * @return the utility icon
	 */
	public static Icon createUtilityIcon(String text, Font f) {
		FontRenderContext fr = new FontRenderContext(null, false, false);
		TextLayout t = new TextLayout(text, f, fr);

		Rectangle2D r = t.getBounds();

		int hMargin = (int) (6 + r.getHeight() / 2);
		int vMargin = 6;

		int width = (int) (r.getWidth() + 2 * hMargin);
		int height = (int) (r.getHeight() + 2 * vMargin);

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) image.getGraphics();
		
		/*
		 g.setColor(new Color(0,255,255));
		 g.fillRect(0,1,width-2,height-2);
		 */
		Polygon polygon = new Polygon();
		polygon.addPoint(1, height / 2);
		polygon.addPoint(height / 2, height - 1);
		polygon.addPoint(width - height / 2, height - 1);
		polygon.addPoint(width - 1, height / 2);
		polygon.addPoint(width - height / 2, 1);
		polygon.addPoint(height / 2, 1);

		g.setColor(new Color(208, 230, 178));
		g.fillPolygon(polygon);
		g.setColor(Color.black);
		g.drawPolygon(polygon);
		t.draw(g, hMargin, height - vMargin);

		return new ImageIcon(image);
	}
}

