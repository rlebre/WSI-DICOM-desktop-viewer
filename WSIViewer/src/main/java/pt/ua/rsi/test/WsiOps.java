package pt.ua.rsi.test;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import pt.ua.rsi.wsi.Wsi;

/**
 *
 * @author Rui Lebre (<a href="mailto:ruilebre@ua.pt">ruilebre@ua.pt</a>)
 */
public class WsiOps {

    public static void main(String[] args) {
        String dicomFilesPath = "C:\\Users\\ruile\\OneDrive\\Universidade\\Ano5\\RSI\\Projeto\\imagens\\Human_15x15_20x.dcm";
        String repDirectory = "C:\\RSI_WSI";
        Wsi wsi = new Wsi(dicomFilesPath, repDirectory);
        System.out.println(wsi.toString());
        System.out.println("Total columns: " + wsi.getColumnsTotal());
        System.out.println("Total rows: " + wsi.getRowsTotal());
        Scanner in = new Scanner(System.in);
        int c = 0, r = 0;
        //wsi.getImages();
        wsi.createThumbLevels(1600);
        //wsi.createThumbnail(new File("C:\\RSI_WSI\\img_Human_15x15_20x\\thumb_16"));
        try {
            do {
                System.out.println("Coordinates of central frame");
                System.out.print("Column: ");
                c = in.nextInt();
                System.out.print("Row: ");
                r = in.nextInt();

                if (c > wsi.getColumnsTotal() || r > wsi.getRowsTotal()) {
                    System.out.println("Invalid value. Please try again.");
                } else if (c >= 0 && r >= 0) {// if (c != 0 && r != 0) {
                    File folder = new File("processed");
                    if (!folder.exists()) {
                        folder.mkdir();
                    }

                    ImageIO.write(wsi.getFrames(wsi.getFramesPositions(r, c), 0), "bmp", new File(folder.getAbsolutePath() + "\\concatenated.bmp"));
                    //wsi.concatImages(wsi.getFrames(r, c), 3, 2, "processed");
                    System.out.print("Show? (y/n) ");
                    in.nextLine();
                    String choice = in.nextLine();
                    if (choice.length() == 0 || choice.equalsIgnoreCase("y")) {
                        Desktop desktop = Desktop.getDesktop();
                        File directory = new File("processed");
                        if (!directory.exists() || !directory.isDirectory()) {
                            directory.mkdir();
                        }
                        desktop.open(new File("processed\\concatenated.bmp"));
                    }
                }

                //System.out.println(fileInfo(dicomFilesPath));
                //fileInfo(dicomFilesPath);
                //concatImages(dicomFilesPath);
            } while (!(c < 0 && r < 0));
        } catch (IOException ex) {
            Logger.getLogger(WsiOps.class.getName()).log(Level.SEVERE, null, ex);
        }//*/
    }

    public static void keepIt() throws IOException {
        int rows = 2;   //we assume the no. of rows and cols are known and each chunk has equal width and height
        int cols = 2;
        int chunks = rows * cols;

        int chunkWidth, chunkHeight;
        int type;
        //fetching image files
        File[] imgFiles = new File[chunks];
        for (int i = 0; i < chunks; i++) {
            imgFiles[i] = new File("archi" + i + ".jpg");
        }

        //creating a bufferd image array from image files
        BufferedImage[] buffImages = new BufferedImage[chunks];
        for (int i = 0; i < chunks; i++) {
            buffImages[i] = ImageIO.read(imgFiles[i]);
        }
        type = buffImages[0].getType();
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
        System.out.println("Image concatenated.....");
        ImageIO.write(finalImg, "jpeg", new File("finalImg.jpg"));
    }
}
