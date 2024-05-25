import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Copier implements Runnable {

    public static final int COPY_BUFFER_SIZE = 4096; // Size of buffer used for a single file copy process
    File destination; // the destination directory to copy to
    SynchronizedQueue<File> resultsQueue; // the list of files to copy

    public Copier(File destination, SynchronizedQueue<File> resultsQueue) {
        this.destination = destination;
        this.resultsQueue = resultsQueue;
    }

    public void run() {
        if (!destination.exists() && !destination.mkdir()) {
            return;
        }
        File file;
        while ((file = resultsQueue.dequeue()) != null) {
            try {
                File destFile = getFileDestination(file);
                copyFile(file, destFile);
            } catch (Exception e) {
                continue;
            }
        }
    }

    private void copyFile(File sourceFile, File destinationFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(sourceFile);
                FileOutputStream fos = new FileOutputStream(destinationFile)) {

            byte[] buffer = new byte[COPY_BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * Generates a new empty filename to which this will be copied to. If a file
     * with this name already exists in the destination directory, an integer will
     * be appended to the base name.
     * 
     * @param file The file you wish to copy from the source directory
     * @return A File object pointing to a newly-created empty file in the
     *         destination directory. Returns null if there are too many duplicate
     *         files.
     * 
     * @throws IOException - If an I/O error occured
     */
    private File getFileDestination(File file) throws IOException {
        String currFileName = file.getName();
        int dotIndex = currFileName.lastIndexOf('.');
        String baseName = currFileName;
        String extension = "";

        if (dotIndex > 0 && dotIndex < currFileName.length() - 1) { // Ensure dot exists and is not the first/last
                                                                    // character
            baseName = currFileName.substring(0, dotIndex);
            extension = currFileName.substring(dotIndex);
        }

        File destFile;
        int i = 2;
        while (!(destFile = new File(destination, currFileName)).createNewFile()) { // we note createNewFile is atomic
            currFileName = baseName + " (" + i + ")" + extension;

            if (++i > 1000) {
                throw new IOException("unable to create new file after 1000+ attempts");
            }
        }
        return destFile;
    }
}
