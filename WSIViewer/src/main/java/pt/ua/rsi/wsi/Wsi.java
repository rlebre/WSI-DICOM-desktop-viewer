package pt.ua.rsi.wsi;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che2.io.DicomInputStream;

/**
 * Class for a Whole-Slide Image
 *
 * @version 0.1
 * @author Rui Lebre (<a href="mailto:ruilebre@ua.pt">ruilebre@ua.pt</a>)
 * @see org.dcm4che2.data.DicomObject
 * @see org.dcm4che2.data.Tag
 * @see org.dcm4che2.imageio.plugins.dcm.DicomImageReadParam
 * @see org.dcm4che2.io.DicomInputStream
 * @see javax.imageio.ImageIO
 * @see javax.imageio.ImageReader
 */
public class Wsi {

    private DicomObject dcmObj = null;
    private DicomInputStream din = null;
    private File file = null;
    private final int rowsTotal;
    private final int columnsTotal;
    private String dcmName = null;
    private File repDirectory = null;
    private final int pad;
    private ArrayList<Integer> thumbLevels;
    private BufferedImage thumbnail;
    private int maxFactor;
    private final int maxZoomLevel;
    private WsiFramesPositions actualFrames;

    /**
     * Instantiate a Whole-Slide Image based on file of the given path
     *
     * @param path Path to file on disk
     * @param repDirectory Directory for which WSI images should be stored
     */
    public Wsi(String path, String repDirectory) {
        this.file = new File(path);
        this.dcmName = file.getName();
        this.dcmName = this.dcmName.substring(0, dcmName.lastIndexOf('.'));
        this.repDirectory = new File(repDirectory);
        if (!this.repDirectory.exists() || !this.repDirectory.isDirectory()) {
            this.repDirectory.mkdir();
        }

        try {
            din = new DicomInputStream(file);
            dcmObj = din.readDicomObject();
        } catch (IOException e) {
            e.printStackTrace();
        }

        rowsTotal = (int) Math.round((double) dcmObj.getInt(Tag.TotalPixelMatrixRows) / dcmObj.getInt(Tag.Rows));
        columnsTotal = (int) Math.round((double) dcmObj.getInt(Tag.TotalPixelMatrixColumns) / dcmObj.getInt(Tag.Columns));
        pad = String.valueOf(rowsTotal * columnsTotal).length();
        thumbLevels = new ArrayList<Integer>();
        thumbnail = null;
        maxFactor = Integer.MAX_VALUE;
        File[] dir = new File(this.repDirectory.getAbsolutePath() + "\\img_" + dcmName).listFiles();
        boolean hasOriginals = false;
        for (File dir1 : dir) {
            if (dir1.isDirectory()) {
                if (dir1.getName().equals("originals")) {
                    hasOriginals = true;
                } else if (dir1.getName().startsWith("thumb")) {
                    thumbLevels.add(Integer.parseInt(dir1.getName().split("_")[1]));
                    if (maxFactor > Integer.parseInt(dir1.getName().split("_")[1])) {
                        maxFactor = Integer.parseInt(dir1.getName().split("_")[1]);
                    }
                }
            } else {
                if (dir1.getName().equals("thumbnail.bmp")) {
                    try {
                        thumbnail = ImageIO.read(dir1);
                    } catch (IOException ex) {
                        Logger.getLogger(Wsi.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        Collections.sort(thumbLevels, Collections.reverseOrder());
        initActualFrames();
        maxZoomLevel = thumbLevels.size();
    }

    public void initActualFrames() {
        actualFrames = new WsiFramesPositions(new int[rowsTotal * columnsTotal], columnsTotal, rowsTotal);
        for (int i = 0; i < actualFrames.getPositions().length; i++) {
            actualFrames.setPositions(i, i + 1);
        }
    }

    public WsiFramesPositions zoomIn(int quadrante, int nivel) {
        if (nivel >= getMaxZoom()) {
            return actualFrames;
        }

        if (nivel == 1) {
            initActualFrames();
        }

        int[] actual = actualFrames.getPositions();
        int[] thisIsIt = new int[actualFrames.getSize() / 4];

        int sao2damanha = actual[0];
        switch (quadrante) {
            case 1: {
                break;
            }
            case 2: {
                sao2damanha += actualFrames.getWidth() / 2 - 1;
                break;
            }
            case 3: {
                sao2damanha += (int) ((double) actualFrames.getWidth() * ((double) actualFrames.getHeight() / 2)) - 1;
                break;
            }
            case 4: {
                sao2damanha += (int) ((double) actualFrames.getWidth() * ((double) actualFrames.getHeight() / 2) - ((double) actualFrames.getWidth() / 2)) - 1;
                break;
            }
            default: {
                return null;
            }
        }

        int initial = sao2damanha;
        //actualFrames.getWidth() / 2
        int j = 0;
        for (int i = 0; i < thisIsIt.length; i++) {
            thisIsIt[i] = sao2damanha + j;
            if (sao2damanha + j == sao2damanha + actualFrames.getWidth() / 2 - 1) {
                sao2damanha += this.getColumnsTotal();// actualFrames.getWidth();
                j = -1;
            }
            j++;
        }

        actualFrames = new WsiFramesPositions(thisIsIt, actualFrames.getWidth() / 2, actualFrames.getHeight() / 2);

        return actualFrames;
    }

    /**
     * Instantiate a Whole-Slide Image based on file of the given path
     *
     * @param file File to perform reading
     * @param repDirectory Directory for which WSI images should be stored
     */
    public Wsi(File file, File repDirectory) {
        this.file = file;
        this.dcmName = file.getName();
        this.dcmName = this.dcmName.substring(0, dcmName.lastIndexOf('.'));
        this.repDirectory = repDirectory;
        if (!this.repDirectory.exists() || !this.repDirectory.isDirectory()) {
            this.repDirectory.mkdir();
        }

        try {
            din = new DicomInputStream(file);
            dcmObj = din.readDicomObject();
        } catch (IOException e) {
            e.printStackTrace();
        }

        rowsTotal = (int) Math.round((double) dcmObj.getInt(Tag.TotalPixelMatrixRows) / dcmObj.getInt(Tag.Rows));
        columnsTotal = (int) Math.round((double) dcmObj.getInt(Tag.TotalPixelMatrixColumns) / dcmObj.getInt(Tag.Columns));
        pad = String.valueOf(rowsTotal * columnsTotal).length();
        thumbLevels = new ArrayList<Integer>();
        thumbnail = null;
        maxFactor = Integer.MAX_VALUE;

        File imagesFolder = new File(this.repDirectory.getAbsolutePath() + "\\img_" + dcmName);
        if (!imagesFolder.exists() || !imagesFolder.isDirectory()) {
            imagesFolder.mkdir();
        }
        File[] files = imagesFolder.listFiles();
        boolean hasOriginals = false;
        for (File dir : files) {
            if (dir.isDirectory()) {
                if (dir.getName().equals("originals")) {
                    hasOriginals = true;
                } else if (dir.getName().startsWith("thumb")) {
                    thumbLevels.add(Integer.parseInt(dir.getName().split("_")[1]));
                    if (maxFactor > Integer.parseInt(dir.getName().split("_")[1])) {
                        maxFactor = Integer.parseInt(dir.getName().split("_")[1]);
                    }
                }
            } else {
                if (dir.getName().equals("thumbnail.bmp")) {
                    try {
                        thumbnail = ImageIO.read(dir);
                    } catch (IOException ex) {
                        Logger.getLogger(Wsi.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        Collections.sort(thumbLevels);

        actualFrames = new WsiFramesPositions(new int[rowsTotal * columnsTotal], columnsTotal, rowsTotal);
        for (int i = 0; i < actualFrames.getPositions().length; i++) {
            actualFrames.setPositions(i, i + 1);
        }

        maxZoomLevel = thumbLevels.size();
    }

    /**
     * Writes all frames of Whole-Slide Image on disk
     *
     * @return True if all images were correctly written, false otherwise
     */
    public boolean getImages() {
        int numberOfFrames = dcmObj.getInt(Tag.NumberOfFrames);
        int numberOfDigits = String.valueOf(numberOfFrames).length();

        BufferedImage dcmImage = null;
        Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName("DICOM");
        ImageReader reader = (ImageReader) iter.next();
        DicomImageReadParam param = (DicomImageReadParam) reader.getDefaultReadParam();
        ImageInputStream iis = null;
        try {
            iis = ImageIO.createImageInputStream(file);
            reader.setInput(iis, false);
            File dir = new File(repDirectory.getAbsolutePath() + "\\img_" + dcmName + "\\originals");
            if (!dir.exists() || !dir.isDirectory()) {
                dir.mkdir();

                for (int i = 0; i < numberOfFrames; i++) {
                    dcmImage = reader.read(i, param);

                    if (dcmImage == null) {
                        return false;
                    }
                    File outImage = null;

                    outImage = new File(dir.getAbsolutePath() + "\\img" + String.format("%0" + numberOfDigits + "d", (i + 1)) + ".bmp");

                    ImageIO.write(dcmImage, "BMP", outImage);
                    if (i % 50 == 0) {
                        System.out.printf("Getting images...: %.2f%%\n", (double) (i + 1) / (double) numberOfFrames * 100);
                    }
                }
            } else {
                return false;
            }
        } catch (IOException ex) {
            return false;
        }

        return true;
    }

    /**
     * Returns a formatted string of this object
     *
     * @return A formatted string of this object
     */
    @Override
    public String toString() {
        return this.getInfo();
    }

    /**
     * Returns info in the form of a String about DCM WSI object
     *
     * @return String containing information about DICOM object created
     */
    public String getInfo() {
        return dcmObj.toString();
    }

    public BufferedImage getFrames(WsiFramesPositions frames, int zoomLevel) throws IOException {
        // 0 1 2
        // 3 4 5
        // 6 7 8
        // Central frame: 4

        int[] positionsToGet = frames.getPositions();
        int zoomCols = frames.getWidth();
        int zoomRows = frames.getHeight();

        BufferedImage[] toReturn = new BufferedImage[positionsToGet.length];

        Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName("DICOM");
        ImageReader reader = (ImageReader) iter.next();
        DicomImageReadParam param = (DicomImageReadParam) reader.getDefaultReadParam();
        ImageInputStream iis = null;

        iis = ImageIO.createImageInputStream(file);
        reader.setInput(iis, false);

        String path = "";
        if (zoomLevel >= maxZoomLevel) {
            path = "C:\\RSI_WSI\\img_Human_15x15_20x\\originals\\img";
        } else {
            path = "C:\\RSI_WSI\\img_Human_15x15_20x\\thumb_" + thumbLevels.get(zoomLevel) + "\\resized";
        }

        for (int i = 0; i < toReturn.length; i++) {
            String aux = path + String.format("%0" + pad + "d", positionsToGet[i]) + ".bmp";
            toReturn[i] = ImageIO.read(new File(aux));
        }

        return concatImages(toReturn, zoomCols, zoomRows);
    }

    /**
     * Build an image based on image array and returns it
     *
     * @param buffImages Image array containing all images to be built as one
     * simple
     * @param cols Number of image columns
     * @param rows Number of image rows
     * @return Built image reference
     */
    public BufferedImage concatImages(BufferedImage[] buffImages, int cols, int rows) {
        int type;
        type = buffImages[0].getType();
        int chunkWidth, chunkHeight;
        chunkWidth = buffImages[0].getWidth();
        chunkHeight = buffImages[0].getHeight();

        //Initializing the final image
        BufferedImage finalImg = new BufferedImage(chunkWidth * cols, chunkHeight * rows, type);

        int num = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                finalImg.createGraphics().drawImage(buffImages[num], chunkWidth * j, chunkHeight * i, null);
                num++;
            }
        }

        return finalImg;
    }

    /**
     * Build an image based on image array and write it to disk on the given
     * path
     *
     * @param buffImages Image array containing all images to be built as one
     * simple
     * @param cols Number of image columns
     * @param rows Number of image rows
     * @param path Path to be written
     * @return True if successfully written, false otherwise
     */
    public boolean concatImages(BufferedImage[] buffImages, int cols, int rows, String path) {
        BufferedImage finalImg = concatImages(buffImages, cols, rows);

        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdir();
        }

        try {
            ImageIO.write(finalImg, "bmp", new File(folder.getAbsolutePath() + "\\concatenated.bmp"));
        } catch (IOException ex) {
            return false;
        }

        return true;
    }

    public void createThumbLevels(int desiredWidth) {
        File folder = new File(repDirectory.getAbsolutePath() + "\\img_" + dcmName + "\\originals");
        //create new folder to save resized images
        File thumbOrigin = null;
        thumbLevels = new ArrayList<Integer>();
        if (!folder.exists() || !folder.isDirectory()) {
            if (getImages()) {
                thumbOrigin = createThumbLevelsAux(folder, desiredWidth);
            }
        } else {
            thumbOrigin = createThumbLevelsAux(folder, desiredWidth);
        }
        createThumbnail(thumbOrigin);
        Collections.sort(thumbLevels);
    }

    public void createThumbLevels(int desiredWidth, int desiredHeight) {
        File folder = new File(repDirectory.getAbsolutePath() + "\\img_" + dcmName + "\\originals");
        //create new folder to save resized images
        File thumbOrigin = null;
        thumbLevels = new ArrayList<Integer>();
        if (!folder.exists() || !folder.isDirectory()) {
            if (getImages()) {
                thumbOrigin = createThumbLevelsAux(folder, desiredWidth, desiredHeight);
            }
        } else {
            thumbOrigin = createThumbLevelsAux(folder, desiredWidth, desiredHeight);
        }
        createThumbnail(thumbOrigin);
        Collections.sort(thumbLevels);
    }

    private File createThumbLevelsAux(File originFolder, int desiredWidth) {
        File destFolder = originFolder.getParentFile().getAbsoluteFile();

        // cria pasta para novas imagens com base na largura da primeira imagem que encontrar
        for (File f : originFolder.listFiles()) {
            // in case of enfing with bmp, we have an image
            if (f.getName().endsWith(".bmp")) {
                try {
                    //reads image
                    BufferedImage bimg = ImageIO.read(f);
                    //reads width of the image and creates the new folder
                    destFolder = new File(destFolder.getAbsolutePath() + "\\thumb_" + bimg.getWidth() / 2);
                    thumbLevels.add(bimg.getWidth() / 2);
                    destFolder.mkdir();
                    maxFactor = bimg.getWidth();
                    break;
                } catch (IOException ex) {
                    Logger.getLogger(Wsi.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            }
        }
        int imgNumber = 0;
        File[] list = originFolder.listFiles();
        int numberOfDigits = String.valueOf(list.length).length();
        for (File f : list) {
            if (!f.getName().endsWith(".bmp")) {
                continue;
            }

            ImagePlus imp = IJ.openImage(f.getAbsolutePath());
            ImageProcessor ip = imp.getProcessor();
            ip = ip.resize(ip.getWidth() / 2);

            BufferedImage resizedImg = ip.getBufferedImage();
            try {
                ImageIO.write(resizedImg, "bmp", new File(destFolder.getAbsolutePath() + "\\resized" + String.format("%0" + numberOfDigits + "d", (imgNumber + 1)) + ".bmp"));

            } catch (IOException ex) {
                Logger.getLogger(Wsi.class.getName()).log(Level.SEVERE, null, ex);
            }

            imgNumber++;
            if (imgNumber % 50 == 0) {
                System.out.printf("Creating thumb lvl %d...\nWrite status: %.2f%%\n", resizedImg.getWidth(), (double) (imgNumber + 1) / (double) list.length * 100);
            }
        }

        for (File f : destFolder.listFiles()) {
            if (f.getName().endsWith(".bmp")) {
                try {
                    BufferedImage bimg = ImageIO.read(f);
                    if (bimg.getWidth() > desiredWidth / columnsTotal) {
                        return createThumbLevelsAux(destFolder, desiredWidth);
                    } else {
                        break;
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Wsi.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return destFolder;
    }

    private File createThumbLevelsAux(File originFolder, int desiredWidth, int desiredHeight) {
        File destFolder = originFolder.getParentFile().getAbsoluteFile();

        // cria pasta para novas imagens com base na largura da primeira imagem que encontrar
        for (File f : originFolder.listFiles()) {
            // in case of enfing with bmp, we have an image
            if (f.getName().endsWith(".bmp")) {
                try {
                    //reads image
                    BufferedImage bimg = ImageIO.read(f);
                    //reads width of the image and creates the new folder
                    destFolder = new File(destFolder.getAbsolutePath() + "\\thumb_" + bimg.getWidth() / 2);
                    if (destFolder.exists()) {
                        if (bimg.getWidth() > desiredWidth / columnsTotal || bimg.getHeight() > desiredHeight / rowsTotal) {
                            return createThumbLevelsAux(destFolder, desiredWidth, desiredHeight);
                        }
                    }
                    thumbLevels.add(bimg.getWidth() / 2);
                    destFolder.mkdir();
                    maxFactor = bimg.getWidth();
                    break;
                } catch (IOException ex) {
                    Logger.getLogger(Wsi.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            }
        }
        int imgNumber = 0;
        File[] list = originFolder.listFiles();
        int numberOfDigits = String.valueOf(list.length).length();
        for (File f : list) {
            if (!f.getName().endsWith(".bmp")) {
                continue;
            }

            ImagePlus imp = IJ.openImage(f.getAbsolutePath());
            ImageProcessor ip = imp.getProcessor();
            ip = ip.resize(ip.getWidth() / 2);

            BufferedImage resizedImg = ip.getBufferedImage();
            try {
                ImageIO.write(resizedImg, "bmp", new File(destFolder.getAbsolutePath() + "\\resized" + String.format("%0" + numberOfDigits + "d", (imgNumber + 1)) + ".bmp"));

            } catch (IOException ex) {
                Logger.getLogger(Wsi.class.getName()).log(Level.SEVERE, null, ex);
            }

            imgNumber++;
            if (imgNumber % 50 == 0) {
                System.out.printf("Creating thumb lvl %d...\nWrite status: %.2f%%\n", resizedImg.getWidth(), (double) (imgNumber + 1) / (double) list.length * 100);
            }
        }

        for (File f : destFolder.listFiles()) {
            if (f.getName().endsWith(".bmp")) {
                try {
                    BufferedImage bimg = ImageIO.read(f);
                    if (bimg.getWidth() > desiredWidth / columnsTotal || bimg.getHeight() > desiredHeight / rowsTotal) {
                        return createThumbLevelsAux(destFolder, desiredWidth, desiredHeight);
                    } else {
                        break;
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Wsi.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return destFolder;
    }

    public void createThumbnail(File folder) {
        int rows = rowsTotal;   //we assume the no. of rows and cols are known and each chunk has equal width and height
        int cols = columnsTotal;
        int chunks = rows * cols;

        int chunkWidth, chunkHeight;
        int type;
        //fetching image files
        File[] imgFiles = new File[chunks];
        for (int i = 0; i < chunks; i++) {
            String s = folder.getAbsolutePath() + "\\resized" + String.format("%0" + pad + "d", (i + 1)) + ".bmp";
            imgFiles[i] = new File(s);
        }

        //creating a bufferd image array from image files
        BufferedImage[] buffImages = new BufferedImage[chunks];
        for (int i = 0; i < chunks; i++) {
            try {
                buffImages[i] = ImageIO.read(imgFiles[i]);

            } catch (IOException ex) {
                System.err.println("Imagem: " + i);
                Logger.getLogger(Wsi.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        type = buffImages[0].getType();
        chunkWidth = buffImages[0].getWidth();
        chunkHeight = buffImages[0].getHeight();

        //Initializing the final image
        thumbnail = new BufferedImage(chunkWidth * cols, chunkHeight * rows, type);

        int num = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                thumbnail.createGraphics().drawImage(buffImages[num], chunkWidth * j, chunkHeight * i, null);
                num++;
            }
        }
        System.out.println("Image concatenated.....");
        try {
            ImageIO.write(thumbnail, "bmp", new File(repDirectory.getAbsolutePath() + "\\img_" + dcmName + "\\thumbnail.bmp"));

        } catch (IOException ex) {
            Logger.getLogger(Wsi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns the number of total rows of WSI
     *
     * @return Number of total rows of WSI
     */
    public int getRowsTotal() {
        return rowsTotal;
    }

    /**
     * Returns the number of total columns of WSI
     *
     * @return Number of total columns of WSI
     */
    public int getColumnsTotal() {
        return columnsTotal;
    }

    public BufferedImage getThumbnail() {
        if (thumbnail == null) {
            createThumbLevels(1600);
        }
        return thumbnail;
    }

    public BufferedImage getThumbnail(int width, int height) {
        if (thumbnail == null) {
            createThumbLevels(width, height);
        } else {
            if (thumbnail.getWidth() > width) {
                createThumbLevels(width, height);
            }
        }
        return thumbnail;
    }

    public int getMaxFactor() {
        return maxFactor;
    }

    public int getMaxZoom() {
        return thumbLevels.size() + 1;
    }

    public WsiFramesPositions getFramesPositions(int row, int col) throws IOException {
        // 0 1 2
        // 3 4 5
        // 6 7 8
        // Central frame: 4
        int[] positionsToGet = null;
        int zoomRows = 0;
        int zoomCols = 0;

        int lastRow1stItem = (rowsTotal - 1) * columnsTotal + 1;
        int lastColumn1stItem = columnsTotal - 1;
        int lastItem = columnsTotal * rowsTotal - 1;
        int centralFramePos = row * (getThumbnail().getWidth() / getMaxFactor() + 1) + col;//= row * this.columnsTotal + col;

        if (centralFramePos >= 0 && centralFramePos <= lastColumn1stItem) {
            // estÃ¡ na primeira linha
            if (centralFramePos == 0) {     // primeira posicao
                // 4 5
                // 6 7
                positionsToGet = new int[4];
                positionsToGet[0] = centralFramePos;
                positionsToGet[1] = centralFramePos + 1;
                positionsToGet[2] = columnsTotal;
                positionsToGet[3] = columnsTotal + 1;
                zoomRows = 2;
                zoomCols = 2;
            } else if (centralFramePos == columnsTotal - 1) { //ultima posicao da linha
                // 3 4
                // 5 6
                positionsToGet = new int[4];
                positionsToGet[0] = columnsTotal - 2;
                positionsToGet[1] = columnsTotal - 1;
                positionsToGet[2] = columnsTotal * 2 - 2;
                positionsToGet[3] = columnsTotal * 2 - 1;
                zoomRows = 2;
                zoomCols = 2;
            } else {    // nem ultima nem primeira posicao da linha
                // 3 4 5
                // 6 7 8
                positionsToGet = new int[6];
                positionsToGet[0] = centralFramePos - 1;
                positionsToGet[1] = centralFramePos;
                positionsToGet[2] = centralFramePos + 1;
                positionsToGet[3] = columnsTotal + centralFramePos - 1;
                positionsToGet[4] = columnsTotal + centralFramePos;
                positionsToGet[5] = columnsTotal + centralFramePos + 1;
                zoomRows = 2;
                zoomCols = 3;
            }
        } else if (centralFramePos >= lastRow1stItem && centralFramePos <= lastItem) {
            // estÃ¡ na ultima linha
            if (centralFramePos == lastColumn1stItem) {     // primeira posicao
                // 1 2
                // 4 5
                positionsToGet = new int[4];
                positionsToGet[0] = lastColumn1stItem - columnsTotal;
                positionsToGet[1] = lastColumn1stItem + 1 - columnsTotal;
                positionsToGet[2] = lastColumn1stItem;
                positionsToGet[3] = lastColumn1stItem + 1;
                zoomRows = 2;
                zoomCols = 2;
            } else if (centralFramePos == lastItem) { //ultima posicao da linha
                // 0 1
                // 3 4
                positionsToGet = new int[4];
                positionsToGet[0] = lastItem - columnsTotal - 2;
                positionsToGet[1] = lastItem - columnsTotal - 1;
                positionsToGet[2] = lastItem - 2;
                positionsToGet[3] = lastItem - 1;
                zoomRows = 2;
                zoomCols = 2;
            } else {    // nem ultima nem primeira posicao da linha
                // 0 1 2
                // 3 4 5
                positionsToGet = new int[6];
                positionsToGet[0] = centralFramePos - columnsTotal - 1;
                positionsToGet[1] = centralFramePos - columnsTotal;
                positionsToGet[2] = centralFramePos - columnsTotal + 1;
                positionsToGet[3] = centralFramePos - 1;
                positionsToGet[4] = centralFramePos;
                positionsToGet[5] = centralFramePos + 1;
                zoomRows = 2;
                zoomCols = 3;
            }
        } else if (centralFramePos % columnsTotal == 0) {        // estÃ¡ na primeira coluna
            // 1 2
            // 4 5
            // 7 8
            positionsToGet = new int[6];
            positionsToGet[0] = centralFramePos - columnsTotal;
            positionsToGet[1] = centralFramePos - columnsTotal + 1;
            positionsToGet[2] = centralFramePos;
            positionsToGet[3] = centralFramePos + 1;
            positionsToGet[4] = centralFramePos + columnsTotal;
            positionsToGet[5] = centralFramePos + columnsTotal + 1;
            zoomRows = 3;
            zoomCols = 2;
        } else if ((centralFramePos + 1) % columnsTotal == 0) {       // estÃ¡ na ultima coluna
            // 0 1
            // 3 4
            // 6 7
            positionsToGet = new int[6];
            positionsToGet[0] = centralFramePos - columnsTotal - 1;
            positionsToGet[1] = centralFramePos - columnsTotal;
            positionsToGet[2] = centralFramePos - 1;
            positionsToGet[3] = centralFramePos;
            positionsToGet[4] = centralFramePos + columnsTotal - 1;
            positionsToGet[5] = centralFramePos + columnsTotal;
            zoomRows = 3;
            zoomCols = 2;
        } else {
            // 0 1 2
            // 3 4 5
            // 6 7 8
            positionsToGet = new int[9];
            positionsToGet[0] = (row - 1) * this.columnsTotal + col - 1;
            positionsToGet[1] = (row - 1) * this.columnsTotal + col;
            positionsToGet[2] = (row - 1) * this.columnsTotal + col + 1;
            positionsToGet[3] = row * this.columnsTotal + col - 1;
            positionsToGet[4] = row * this.columnsTotal + col;
            positionsToGet[5] = row * this.columnsTotal + col + 1;
            positionsToGet[6] = (row + 1) * this.columnsTotal + col - 1;
            positionsToGet[7] = (row + 1) * this.columnsTotal + col;
            positionsToGet[8] = (row + 1) * this.columnsTotal + col + 1;
            zoomRows = 3;
            zoomCols = 3;
        }

        return new WsiFramesPositions(positionsToGet, zoomRows, zoomCols);
    }
}
