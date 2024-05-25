import java.io.File;

public class Scouter implements Runnable {
    // responsible for listing all directories that exist under the given root
    // directory. It enqueues all directories into the directory queue.
    // There is always only one scouter
    private SynchronizedQueue<File> directoryQueue;
    private File root;
    private static boolean hasntDeclared = true;
    private final Object lock = new Object();

    /**
     * Constructor. Initializes the scouter with a queue for the directories to be
     * searched and a root directory to start from. This class can only be
     * constructed once.
     * 
     * @param directoryQueue A queue for directories to be searched.
     * @param root           Root directory to start from.
     */
    public Scouter(SynchronizedQueue<File> directoryQueue, File root) {
        synchronized (lock) {
            if (hasntDeclared) {
                this.directoryQueue = directoryQueue;
                this.root = root;
                hasntDeclared = false;
            }
        }
    }

    /**
     * Starts the scouter thread. Lists directories under the root directory
     * and adds them to the queue, then lists directories in the next level
     * and enqueues them, and so on.
     */
    @Override
    public void run() {
        try {
            directoryQueue.registerProducer();
            enqueueDirectories(root);
        } finally {
            directoryQueue.unregisterProducer();
        }
    }

    /**
     * Recursively enqueues directories starting from the given directory.
     * 
     * @param dir The directory to start listing from.
     */
    private void enqueueDirectories(File dir) {
        if (dir.isDirectory()) {
            directoryQueue.enqueue(dir);
            File[] subDirs = dir.listFiles(File::isDirectory);
            if (subDirs != null) {
                for (File subDir : subDirs) {
                    enqueueDirectories(subDir);
                }
            }
        }
    }
}
