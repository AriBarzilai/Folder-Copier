import java.io.File;
import java.io.FilenameFilter;

public class Searcher extends java.lang.Object implements Runnable {
    private String pattern;
    private String extension;
    private SynchronizedQueue<File> directoryQueue;
    private SynchronizedQueue<File> resultsQueue;

    /**
     * Constructor. Initializes the searcher thread with a pattern, an extension,
     * a queue of directories to search, and a results queue to store matching
     * files.
     *
     * @param pattern        Pattern to look for in the names of files.
     * @param extension      Wanted file extension.
     * @param directoryQueue A queue with directories to search in.
     * @param resultsQueue   A queue for files found.
     */
    public Searcher(String pattern, String extension, SynchronizedQueue<File> directoryQueue,
            SynchronizedQueue<File> resultsQueue) {
        this.pattern = pattern;
        this.extension = extension;
        this.directoryQueue = directoryQueue;
        this.resultsQueue = resultsQueue;
    }

    /**
     * Runs the searcher thread. This method fetches a directory from the directory
     * queue, searches all files within it for files that contain the specified
     * pattern and end with the specified extension, and enqueues the matching files
     * to the results queue.
     */
    @Override
    public void run() {
        try {
            resultsQueue.registerProducer();
            File directory;
            while ((directory = directoryQueue.dequeue()) != null) {
                searchFiles(directory);
            }
        } finally {
            resultsQueue.unregisterProducer();
        }
    }

    /**
     * Searches for files within the specified directory that contain the pattern
     * and extension.
     * Matches are enqueued in the results queue.
     *
     * @param directory The directory to search.
     */
    private void searchFiles(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    String baseName = name.substring(0, name.length() - extension.length());
                    return name.endsWith(extension) && baseName.contains(pattern);
                }
            });

            if (files != null) {
                for (File file : files) {
                    resultsQueue.enqueue(file);
                }
            }
        }
    }
}
