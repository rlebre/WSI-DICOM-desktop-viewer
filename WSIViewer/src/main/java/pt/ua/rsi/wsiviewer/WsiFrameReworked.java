/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.rsi.wsiviewer;

import java.awt.BorderLayout;
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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import pt.ua.rsi.wsi.Wsi;
import pt.ua.rsi.wsi.WsiFramesPositions;

/**
 *
 * @author ruile
 */
public class WsiFrameReworked extends javax.swing.JFrame implements MouseWheelListener, MouseListener {

    private static final long serialVersionUID = 1L;

    private final Wsi wsi;
    private final JPanel mainPanel;
    private final JLabel label;
    private ImageIcon icon;
    private int zoomLevel;
    private final Stack<BufferedImage> stack;

    /**
     * Creates new form WsiFrameReworked
     *
     * @param wsi
     */
    public WsiFrameReworked(Wsi wsi) {
        initComponents();

        this.wsi = wsi;
        mainPanel = new JPanel(new BorderLayout());
        icon = new ImageIcon(wsi.getThumbnail());
        label = new JLabel(icon);
        mainPanel.add(label);

        addMouseListener(this);
        addMouseWheelListener(this);
        setResizable(false);
        pack();

        zoomLevel = 0;
        stack = new Stack<BufferedImage>();
        stack.push(wsi.getThumbnail());
        this.setVisible(true);
        mainPanel.setVisible(true);
        label.setVisible(true);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent me) {
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
    public void mousePressed(MouseEvent me) {
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
        zoomLevel++;
        if (zoomLevel >= wsi.getMaxZoom()) {
            zoomLevel = wsi.getMaxZoom() - 1;
            JOptionPane.showMessageDialog(this, "Maximum zoom reached.", "Information", JOptionPane.INFORMATION_MESSAGE);
        } else {
            WsiFramesPositions pos = wsi.zoomIn(quadrante, zoomLevel);
            try {
                BufferedImage image = wsi.getFrames(pos, zoomLevel);
                if (zoomLevel + 1 < wsi.getMaxZoom()) {
                    stack.push(image);
                }
                setCurrentImage(image);
            } catch (IOException ex) {
                Logger.getLogger(WsiFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void zoomOut() {
        if (stack.size() > 0) {
            zoomLevel--;

            if (stack.size() == 1) {
                setCurrentImage(stack.peek());
                zoomLevel = 0;
                JOptionPane.showMessageDialog(this, "No more zoom out. Already showing thumbnail.", "Information", JOptionPane.INFORMATION_MESSAGE);

            } else {
                setCurrentImage(stack.pop());
            }
        }
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 694, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 520, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    /* public static void main(String args[]) {
     //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
     try {
     for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
     if ("Nimbus".equals(info.getName())) {
     javax.swing.UIManager.setLookAndFeel(info.getClassName());
     break;
     }
     }
     } catch (ClassNotFoundException ex) {
     java.util.logging.Logger.getLogger(WsiFrameReworked.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
     } catch (InstantiationException ex) {
     java.util.logging.Logger.getLogger(WsiFrameReworked.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
     } catch (IllegalAccessException ex) {
     java.util.logging.Logger.getLogger(WsiFrameReworked.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
     } catch (javax.swing.UnsupportedLookAndFeelException ex) {
     java.util.logging.Logger.getLogger(WsiFrameReworked.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
     }
     //</editor-fold>

     java.awt.EventQueue.invokeLater(new Runnable() {
     public void run() {
     new WsiFrameReworked().setVisible(true);
     }
     });
     }*/

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    // End of variables declaration//GEN-END:variables
}
