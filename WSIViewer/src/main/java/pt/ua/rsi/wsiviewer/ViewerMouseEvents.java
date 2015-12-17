package pt.ua.rsi.wsiviewer;

import pt.ua.rsi.wsi.Wsi;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 *
 * @author Rui Lebre (<a href="mailto:ruilebre@ua.pt">ruilebre@ua.pt</a>)
 */
public class ViewerMouseEvents implements MouseWheelListener, MouseListener {

    private Wsi wsi;
    
    public ViewerMouseEvents(Wsi wsi) {
        this.wsi = wsi;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent me) {
        int x = me.getX() - 3;
        int y = me.getY() - 26;
        int frame = getFrameNumber(x, y);
        if (me.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            if (me.getWheelRotation() < 0) {
                System.out.println("Zoom In (" + x + ',' + y + ')' + " Frame: " + frame);
            } else {
                System.out.println("Zoom Out (" + x + ',' + y + ')' + " Frame: " + frame);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent me) {
        int x = me.getX() - 3;
        int y = me.getY() - 26;
        int frame = getFrameNumber(x, y);
        if (me.getButton() == MouseEvent.BUTTON1) {
            System.out.println("Zoom In (" + x + ',' + y + ')' + " Frame: " + frame);
        } else if (me.getButton() == MouseEvent.BUTTON2) {
            System.out.println("Show thumbnail");
        } else if (me.getButton() == MouseEvent.BUTTON3) {
            System.out.println("Zoom Out (" + x + ',' + y + ')' + " Frame: " + frame);
        }
    }

    public int getFrameNumber(int col, int row) {//136x133
        int whichRow = 0;
        for (int i = 0; i < 1064; i += 8) {
            if (row <= i) {
                break;
            }
            whichRow++;
        }

        int whichCol = 0;
        for (int i = 0; i < 1088; i += 8) {
            if (col <= i) {
                break;
            }
            whichCol++;
        }
        System.out.println("Row: " + whichRow + " Col:" + whichCol);
        return whichRow * 136 + whichCol;
    }

    /**
     * @deprecated @param me
     */
    @Override
    public void mouseReleased(MouseEvent me) {
    }

    /**
     * @deprecated @param me
     */
    @Override
    public void mouseEntered(MouseEvent me) {
    }

    /**
     * @deprecated @param me
     */
    @Override
    public void mouseExited(MouseEvent me) {
    }

    /**
     * @deprecated @param me
     */
    @Override
    public void mouseClicked(MouseEvent me) {
    }
}
