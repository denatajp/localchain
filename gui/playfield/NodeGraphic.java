/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package gui.playfield;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

import core.Connection;
import core.Coord;
import core.DTNHost;
import core.NetworkInterface;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Visualization of a DTN Node
 *
 */
public class NodeGraphic extends PlayFieldGraphic {
	private static boolean drawCoverage = true;
	private static boolean drawNodeName = true;
	private static boolean drawConnections = true;

	private static Color rangeColor = Color.GREEN;
	private static Color conColor = Color.BLACK;
	private static Color hostColor = Color.BLUE;
	private static Color hostNameColor = Color.BLUE;
	private static Color msgColor1 = Color.BLUE;
	private static Color msgColor2 = Color.GREEN;
	private static Color msgColor3 = Color.RED;

	private DTNHost node;

        private Image nodeImage;
        
	public NodeGraphic(DTNHost node) {	
		this.node = node;
                try {
                    if (isMiner(node)) {
                        nodeImage = ImageIO.read(new File("data/Node/miner.png"));
                        hostNameColor = Color.BLUE;
                    } else if (isOperatorProxy(node)) {
                        nodeImage = ImageIO.read(new File("data/Node/opeproxy.png"));
                        hostNameColor = Color.RED;
                    } else if (isCollector(node)) {
                         nodeImage = ImageIO.read(new File("data/Node/collector.png"));
                         hostNameColor = Color.MAGENTA;
                    } else if (isInternet(node)) {
                         nodeImage = ImageIO.read(new File("data/Node/internet.png"));
                         hostNameColor = Color.BLUE;
                    }
                } catch (IOException e) {
                    System.err.println("Gambar node tidak ditemukan!");
                    nodeImage = null; // Fallback ke drawRect
                }
	}

	@Override
	public void draw(Graphics2D g2) {
		drawHost(g2);
		drawMessages(g2);
	}

	/**
	 * Visualize node's location, radio ranges and connections
	 * @param g2 The graphic context to draw to
	 */
	private void drawHost(Graphics2D g2) {
		Coord loc = node.getLocation();

		if (drawCoverage && node.isActive()) {
			ArrayList<NetworkInterface> interfaces = new ArrayList<NetworkInterface>();
			interfaces.addAll(node.getInterfaces());
			for (NetworkInterface ni : interfaces) {
				double range = ni.getTransmitRange();
				Ellipse2D.Double coverage;

				coverage = new Ellipse2D.Double(scale(loc.getX()-range),
						scale(loc.getY()-range), scale(range * 2), scale(range * 2)); 

				// draw the "range" circle
				g2.setColor(rangeColor);
				g2.draw(coverage);
			}
		}

		if (drawConnections) {
			g2.setColor(conColor);
			Coord c1 = node.getLocation();
			ArrayList<Connection> conList = new ArrayList<Connection>();
			// create a copy to prevent concurrent modification exceptions
			conList.addAll(node.getConnections());
			for (Connection c : conList) {
				Coord c2 = c.getOtherNode(node).getLocation();

				g2.drawLine(scale(c1.getX()), scale(c1.getY()),
						scale(c2.getX()), scale(c2.getY()));
			}
		}

		g2.setColor(hostColor);	// draw rectangle to host's location
                
                // Ganti bagian drawRect dengan:
                if (nodeImage != null) {
                    int imgWidth = nodeImage.getWidth(null);
                    int imgHeight = nodeImage.getHeight(null);
                    
                    // Hitung posisi tengah
                    int x = scale(loc.getX()) - imgWidth/2;
                    int y = scale(loc.getY()) - imgHeight/2;
                    
                    g2.drawImage(nodeImage, x, y, null);
                } else {
                    // fallback ke drawRect jika gambar gagal load
                    g2.drawRect(scale(loc.getX()-1), scale(loc.getY()-1), scale(2), scale(2));
                }
		// g2.drawRect(scale(loc.getX()-1),scale(loc.getY()-1),scale(2),scale(2));

		if (drawNodeName) {
			g2.setColor(hostNameColor);
			// Draw node's address next to it
			g2.drawString(node.toString(), scale(loc.getX()),
					scale(loc.getY()));
		}
	}

	/**
	 * Sets whether radio coverage of nodes should be drawn
	 * @param draw If true, radio coverage is drawn
	 */
	public static void setDrawCoverage(boolean draw) {
		drawCoverage = draw;
	}

	/**
	 * Sets whether node's name should be displayed
	 * @param draw If true, node's name is displayed  
	 */
	public static void setDrawNodeName(boolean draw) {
		drawNodeName = draw;
	}

	/**
	 * Sets whether node's connections to other nodes should be drawn
	 * @param draw If true, node's connections to other nodes is drawn  
	 */
	public static void setDrawConnections(boolean draw) {
		drawConnections = draw;
	}


	/**
	 * Visualize the messages this node is carrying
	 * @param g2 The graphic context to draw to
	 */
	private void drawMessages(Graphics2D g2) {
		int nrofMessages = node.getNrofMessages();
		Coord loc = node.getLocation();

		drawBar(g2,loc, nrofMessages % 10, 1);
		drawBar(g2,loc, nrofMessages / 10, 2);
	}

	/**
	 * Draws a bar (stack of squares) next to a location
	 * @param g2 The graphic context to draw to
	 * @param loc The location where to draw
	 * @param nrof How many squares in the stack
	 * @param col Which column
	 */
	private void drawBar(Graphics2D g2, Coord loc, int nrof, int col) {
		final int BAR_HEIGHT = 5;
		final int BAR_WIDTH = 5;
		final int BAR_DISPLACEMENT = 2;

		// draws a stack of squares next loc
		for (int i=1; i <= nrof; i++) {
			if (i%2 == 0) { // use different color for every other msg
				g2.setColor(msgColor1);
			}
			else {
				if (col > 1) {
					g2.setColor(msgColor3);
				}
				else {
					g2.setColor(msgColor2);
				}
			}

			g2.fillRect(scale(loc.getX()-BAR_DISPLACEMENT-(BAR_WIDTH*col)),
					scale(loc.getY()- BAR_DISPLACEMENT- i* BAR_HEIGHT),
					scale(BAR_WIDTH), scale(BAR_HEIGHT));
		}

	}
 
    private boolean isOperatorProxy(DTNHost host) {
        return host.toString().startsWith("ope");
    }

    private boolean isMiner(DTNHost host) {
        return host.toString().startsWith("min");
    }

    private boolean isHome(DTNHost host) {
        return host.toString().startsWith("home");
    }

    private boolean isInternet(DTNHost host) {
        return host.toString().startsWith("inter");
    }

    private boolean isCollector(DTNHost host) {
        return host.toString().startsWith("col");
    }
}
