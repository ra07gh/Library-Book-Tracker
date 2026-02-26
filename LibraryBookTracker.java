import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class LibraryBookTracker {

    /**
     *
     * @param args Command-line arguments:
     *             args[0] catalog file name (must end with .txt)
     *             args[1] operation (keyword, ISBN, or add format)
     *
     * @throws InsufficientArgumentsException if fewer than two arguments are provided
     * @throws InvalidFileNameException if the file does not end with .txt
     */
    public static void main(String[] args) {

        File file = null;

    try {

        if (args.length < 2)
            throw new InsufficientArgumentsException("Not enough arguments.");

        if (!args[0].endsWith(".txt"))
            throw new InvalidFileNameException("File must end with .txt");

        file = new File(args[0]);

        if (file.getParentFile() != null)
            file.getParentFile().mkdirs();

        if (!file.exists())
            file.createNewFile();

        List<Book> books = new ArrayList<>();
        int[] errorCount = {0};

        Thread fileThread = new Thread(
                new FileReaderTask(file, books, errorCount));

        Thread operationThread = new Thread(
                new OperationTask(books, args[1], file, errorCount));

        fileThread.start();
        fileThread.join();   

        operationThread.start();
        operationThread.join();  

        } catch (Exception e) {

            if (file != null)
                logError(file, args.length > 1 ? args[1] : "N/A", e);

            System.out.println("Error: " + e.getMessage());

        } finally {
            System.out.println("Thank you for using the Library Book Tracker.");
        }
    }

    

    /**
     * Logs an invalid record or operation into errors.log.
     *
     * @param file The catalog file (used to locate log directory)
     * @param text The invalid input line or operation
     * @param The exception thrown during processing
     */
   public static void logError(File file, String text, Exception e) {
    try {

        File logFile = new File(file.getParent(), "errors.log");

        BufferedWriter writer =
                new BufferedWriter(new FileWriter(logFile, true));

        writer.write("[" + LocalDateTime.now() + "] INVALID: \""
                + text + "\" - "
                + e.getClass().getSimpleName()
                + ": " + e.getMessage());

        writer.newLine();
        writer.close();

    } catch (IOException ignored) {}
}


    
}