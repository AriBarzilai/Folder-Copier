import java.io.File;

public class DiskSearcher {
    public static final int DIRECTORY_QUEUE_CAPACITY = 50;
    public static final int RESULTS_QUEUE_CAPACITY = 50;

    public static void main(java.lang.String[] args) {
        // <filename-pattern> <file-extension> <root directory> <destination directory>
        // <# of searchers> <# of copiers>
        if (args.length != 6) {
            System.err.println(
                    "Correct usage: java DiskSearcher <filename-pattern> <file-extension> <root directory> <destination directory> <# of searchers> <# of copiers>");
            return;
        }
        String pattern = args[0];
        String extension = args[1];
        File rootDir = new File(args[2]);
        File destDir = new File(args[3]);
        int numSearchers = Integer.parseInt(args[4]);
        int numCopiers = Integer.parseInt(args[5]);

        if (!rootDir.exists() || !rootDir.isDirectory()) {
            System.err.println("Please enter a valid root path!");
            return;
        } else if ((!destDir.exists() && !destDir.mkdir()) || !destDir.isDirectory()) {
            System.err.println("Please enter a valid destination path!");
        }

        SynchronizedQueue<File> directoryQueue = new SynchronizedQueue<File>(DIRECTORY_QUEUE_CAPACITY);
        SynchronizedQueue<File> resultsQueue = new SynchronizedQueue<File>(RESULTS_QUEUE_CAPACITY);

        Thread scouterThread = new Thread(new Scouter(directoryQueue, rootDir));
        scouterThread.start();

        Thread[] searchers = new Thread[numSearchers];
        for (int i = 0; i < numSearchers; i++) {
            searchers[i] = new Thread(new Searcher(pattern, extension, directoryQueue, resultsQueue));
            searchers[i].start();
        }

        Thread[] copiers = new Thread[numCopiers];
        for (int i = 0; i < numCopiers; i++) {
            copiers[i] = new Thread(new Copier(destDir, resultsQueue));
            copiers[i].start();
        }

        try {
            scouterThread.join();
            for (int i = 0; i < numSearchers; i++) {
                searchers[i].join();
            }
            for (int i = 0; i < numCopiers; i++) {
                copiers[i].join();
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
}
