package pt.ua.rsi.wsi;

/**
 *
 * @author Rui Lebre (<a href="mailto:ruilebre@ua.pt">ruilebre@ua.pt</a>)
 */
public class WsiFramesPositions {

    private int[] positions;
    private int width;
    private int height;

    public WsiFramesPositions(int[] positions, int width, int height) {
        this.positions = positions;
        this.width = width;
        this.height = height;
    }

    public int[] getPositions() {
        return positions;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setPositions(int[] positions) {
        this.positions = positions;
    }

    public void setPositions(int index, int position) {
        this.positions[index] = position;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getSize() {
        return positions.length;
    }
}
