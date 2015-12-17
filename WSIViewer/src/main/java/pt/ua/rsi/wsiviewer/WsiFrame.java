package pt.ua.rsi.wsiviewer;

import pt.ua.rsi.wsi.Wsi;
import pt.ua.rsi.wsi.WsiFramesPositions;
import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Visual interface for WSI Viewer
 *
 * @author Rui Lebre (<a href="mailto:ruilebre@ua.pt">ruilebre@ua.pt</a>)
 * @see javax.swing.JFrame
 * @see MouseWheelListener
 * @see MouseListener
 *
 * @version 0.1
 */
public class WsiFrame extends javax.swing.JFrame implements MouseWheelListener, MouseListener {

    private static final long serialVersionUID = 1L;

    private final Wsi wsi;
    private final JPanel mainPanel;
    private final JLabel label;
    private ImageIcon icon;
    private int zoomLevel;
    private final Stack<BufferedImage> stack;
    private final JFrame main;

    /**
     * Creates new form WsiFrame
     *
     * @param wsi WSI object
     * @param adjust
     */
    public WsiFrame(Wsi wsi, boolean adjust) {
        this.setTitle("WSI Viewer");
        this.wsi = wsi;
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        BufferedImage img = wsi.getThumbnail();

        if (adjust) {
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            int width = gd.getDisplayMode().getWidth();
            int height = gd.getDisplayMode().getHeight();

            if (img.getWidth() > width || img.getHeight() > height) {
                img = wsi.getThumbnail(width, height);
            }
        }

        mainPanel = new JPanel(new BorderLayout());
        icon = new ImageIcon(img);
        label = new JLabel(icon);
        mainPanel.add(label);

        add(mainPanel);
        addMouseListener(this);
        addMouseWheelListener(this);
        setResizable(false);
        pack();
        zoomLevel = 0;
        stack = new Stack<BufferedImage>();
        stack.push(img);
        main = null;
    }

    /**
     * Creates new form WsiFrame
     *
     * @param wsi WSI object
     * @param adjust
     * @param main
     */
    public WsiFrame(Wsi wsi, boolean adjust, final JFrame main) {
        this.setTitle("WSI Viewer");
        this.wsi = wsi;
        //setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        BufferedImage img = wsi.getThumbnail();

        if (adjust) {
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            int width = gd.getDisplayMode().getWidth();
            int height = gd.getDisplayMode().getHeight();

            if (img.getWidth() > width || img.getHeight() > height) {
                img = wsi.getThumbnail(width, height);
            }
        }

        mainPanel = new JPanel(new BorderLayout());
        icon = new ImageIcon(img);
        label = new JLabel(icon);
        mainPanel.add(label);

        add(mainPanel);
        addMouseListener(this);
        addMouseWheelListener(this);
        setResizable(false);
        pack();
        zoomLevel = 0;
        stack = new Stack<BufferedImage>();
        stack.push(img);
        this.main = main;

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (main != null) {
                    main.setVisible(true);
                    setVisible(false);
                    dispose();
                }
            }
        });
    }

    /**
     * Implemented method to handle mouse wheel movement event
     *
     * @param me Mouse movement event parameter
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent me
    ) {
        int quadrante = getQuadrant(me.getX(), me.getY());
        if (me.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            if (me.getWheelRotation() < 0) {
                System.out.println("Zoom In. Quadrante: " + quadrante);
                zoomIn(quadrante);
            } else {
                System.out.println("Zoom Out. Quadrante: " + quadrante);
                zoomOut();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent me
    ) {
        int quadrante = getQuadrant(me.getX(), me.getY());
        if (me.getButton() == MouseEvent.BUTTON1) {
            System.out.println("Zoom In. Quadrante: " + quadrante);
            zoomIn(quadrante);
        } else if (me.getButton() == MouseEvent.BUTTON2) {
            System.out.println("Show thumbnail");
            zoomLevel = 0;
            BufferedImage toShow = null;
            while (!stack.isEmpty()) {
                toShow = stack.pop();
            }
            setCurrentImage(toShow);
        } else if (me.getButton() == MouseEvent.BUTTON3) {
            System.out.println("Zoom Out. Quadrante: " + quadrante);
            zoomOut();
        }
    }

    private void zoomIn(int quadrante) {
        showMessage = false;
        zoomLevel++;
        if (zoomLevel >= wsi.getMaxZoom()) {
            zoomLevel = wsi.getMaxZoom() - 1;
            JOptionPane.showMessageDialog(this, "Maximum zoom reached.", "Information", JOptionPane.INFORMATION_MESSAGE);
        } else {
            WsiFramesPositions pos = wsi.zoomIn(quadrante, zoomLevel);
            try {
                BufferedImage image = wsi.getFrames(pos, zoomLevel);
                if (zoomLevel < wsi.getMaxZoom()) {
                    stack.push(image);
                    prevWasZoomIn = true;
                }
                setCurrentImage(image);
            } catch (IOException ex) {
                Logger.getLogger(WsiFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.gc();
    }
    private boolean prevWasZoomIn = false;
    private boolean showMessage = false;

    private void zoomOut() {
        if (stack.size() > 0) {
            zoomLevel--;

            if (stack.size() == 1) {
                setCurrentImage(stack.peek());
                zoomLevel = 0;
                if (showMessage) {
                    JOptionPane.showMessageDialog(this, "No more zoom out. Already showing thumbnail.", "Information", JOptionPane.INFORMATION_MESSAGE);
                }
                showMessage = true;
            } else {
                BufferedImage aux = stack.pop();
                if (prevWasZoomIn && stack.size() > 2) {
                    setCurrentImage(stack.pop());
                } else {
                    setCurrentImage(stack.peek());
                }
            }
        }
        prevWasZoomIn = false;
        System.gc();
    }

    private int getQuadrant(int x, int y) {
        int width = icon.getImage().getWidth(null);
        int height = icon.getImage().getHeight(null);
        int quadrante = 0;
        if (x < width / 2) {
            quadrante = 1;
        } else {
            quadrante = 2;
        }

        if (y > height / 2) {
            quadrante += 2;
        }

        return quadrante;
    }

    public int getFrameNumber(int col, int row) {//136x133
        int whichRow = 0;
        int whichCol = 0;

        for (int i = 0; i < wsi.getThumbnail().getHeight(); i += 16) {
            if (row <= i) {
                break;
            }
            whichRow++;
        }

        for (int i = 0; i < wsi.getThumbnail().getWidth(); i += 16) {
            if (col <= i) {
                break;
            }
            whichCol++;
        }
        System.out.println("Row: " + whichRow + " Col:" + whichCol);

        return whichRow * (wsi.getThumbnail().getWidth() / wsi.getMaxFactor() + 1) + whichCol;
    }

    private void setCurrentImage(BufferedImage img) {
        icon = new ImageIcon(img);
        label.setIcon(icon);
        pack();
    }

    /**
     * @deprecated No longer used either implemented
     * @param me Mouse event information
     */
    @Override
    public void mouseReleased(MouseEvent me) {
    }

    /**
     * @deprecated No longer used either implemented
     * @param me Mouse event information
     */
    @Override
    public void mouseEntered(MouseEvent me) {
    }

    /**
     * @deprecated No longer used either implemented
     * @param me Mouse event information
     */
    @Override
    public void mouseExited(MouseEvent me) {
    }

    /**
     * @deprecated No longer used either implemented
     * @param me Mouse event information
     */
    @Override
    public void mouseClicked(MouseEvent me) {
    }
}
