A minimalist CLI application which copies some of the contents of one folder to another, while taking advantage of multiple threads. The user inputs the desired file extension and a pattern, and all files whose name contain the pattern substring, and the same extension, will be copied over.

To run, start by running ``javac DiskSearcher.java`` from your terminal to compile the java files, and then enter the following:

``java DiskSearcher <filename-pattern> <file-extension> <root directory> <destination directory> <# of search threads> <# of copy threads>``

