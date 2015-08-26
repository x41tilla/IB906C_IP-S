package whiteboard.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JPanel;

/**
 * 
 * TODO
 *
 * @author
 *
 */
class Paper extends JPanel {
    /**
     * TODO
     */
    private static final long serialVersionUID = -7520132893053334404L;
    private final HashSet<Point> points;

    Paper() {
	this.points = new HashSet<>();

	setOpaque(true);
	setBackground(Color.WHITE);
	setPreferredSize(new Dimension(800, 600));
	setMinimumSize(getPreferredSize());
	setMaximumSize(getPreferredSize());
    }

    @Override
    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	g.setColor(Color.black);
	Iterator<Point> i = this.points.iterator();
	while (i.hasNext()) {
	    Point p = i.next();
	    g.fillOval(p.x, p.y, 2, 2);
	}
    }

    public void addPoint(int x, int y) {
	this.points.add(new Point(x, y));
	repaint();
    }
}