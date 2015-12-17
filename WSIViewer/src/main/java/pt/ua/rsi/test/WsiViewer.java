package pt.ua.rsi.test;



/**
 *
 * @author Rui Lebre (<a href="mailto:ruilebre@ua.pt">ruilebre@ua.pt</a>)
 */
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import pt.ua.rsi.wsi.Wsi;
import pt.ua.rsi.wsiviewer.WsiFrame;

public class WsiViewer {

    public static void main(String[] args) {
        System.out.println(File.listRoots()[0].getAbsolutePath());

        JFileChooser fileCh = new JFileChooser(File.listRoots()[0].getAbsolutePath());
        fileCh.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileCh.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".dcm") || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "DICOM Files (.dcm)";
            }
        });

        int choice = fileCh.showOpenDialog(new JFrame("Choose DICOM File"));

        if (choice != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File chosenFile = fileCh.getSelectedFile();

        if (!chosenFile.canRead() || !chosenFile.exists()) {
            JOptionPane.showMessageDialog(new JFrame(), "File couldn't be read!");

        }

        Wsi wsi = new Wsi(chosenFile, new File("C:\\RSI_WSI"));
        int zoomMax = wsi.getMaxZoom();
        System.out.println(zoomMax);

        //System.out.println(wsi.getInfo());
        System.out.println(wsi.getColumnsTotal());
        System.out.println(wsi.getRowsTotal());

        BufferedImage img = wsi.getThumbnail();
        WsiFrame showWindow = new WsiFrame(wsi, false);
        showWindow.setVisible(true);
    }
}
