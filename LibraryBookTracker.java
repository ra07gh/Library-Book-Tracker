import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        int errorCount = 0;
        int searchResults = 0;
        int booksAdded = 0;

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

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;

            while ((line = reader.readLine()) != null) {

                try {

                    String[] parts = line.split(":");

                    if (parts.length != 4)
                        throw new MalformedBookEntryException("Invalid format.");

                    String title = parts[0];
                    String author = parts[1];
                    String isbn = parts[2];
                    int copies = Integer.parseInt(parts[3]);

                    if (title.isEmpty() || author.isEmpty())
                        throw new MalformedBookEntryException("Title or Author empty.");

                    if (!isbn.matches("\\d{13}"))
                        throw new InvalidISBNException("ISBN must be 13 digits.");

                    if (copies <= 0)
                        throw new MalformedBookEntryException("Copies must be positive.");

                    books.add(new Book(title, author, isbn, copies));

                } catch (BookCatalogException | NumberFormatException e) {
                    logError(file, line, e);
                    errorCount++;
                }
            }

            reader.close();

            String operation = args[1];

            if (operation.matches("\\d{13}")) {

                Book found = null;

                for (Book b : books) {
                    if (b.getIsbn().equals(operation)) {
                        if (found != null)
                            throw new DuplicateISBNException("Duplicate ISBN found.");
                        found = b;
                    }
                }

                if (found != null) {
                    printHeader();
                    printBook(found);
                    searchResults = 1;
                }

            } else if (operation.contains(":")) {

                try {

                    String[] parts = operation.split(":");

                    if (parts.length != 4)
                        throw new MalformedBookEntryException("Invalid add format.");

                    String title = parts[0];
                    String author = parts[1];
                    String isbn = parts[2];
                    int copies = Integer.parseInt(parts[3]);

                    if (title.isEmpty() || author.isEmpty())
                        throw new MalformedBookEntryException("Title or Author empty.");

                    if (!isbn.matches("\\d{13}"))
                        throw new InvalidISBNException("ISBN must be 13 digits.");

                    if (copies <= 0)
                        throw new MalformedBookEntryException("Copies must be positive.");

                    Book newBook = new Book(title, author, isbn, copies);
                    books.add(newBook);
                    booksAdded = 1;

                    Collections.sort(books,
                            Comparator.comparing(Book::getTitle));

                    BufferedWriter writer =
                            new BufferedWriter(new FileWriter(file));

                    for (Book b : books) {
                        writer.write(b.getTitle() + ":" +
                                b.getAuthor() + ":" +
                                b.getIsbn() + ":" +
                                b.getCopies());
                        writer.newLine();
                    }

                    writer.close();

                    printHeader();
                    printBook(newBook);

                } catch (BookCatalogException | NumberFormatException e) {
                    logError(file, operation, e);
                    errorCount++;
                }

            } else {

                printHeader();

                for (Book b : books) {
                    if (b.getTitle().toLowerCase()
                            .contains(operation.toLowerCase())) {

                        printBook(b);
                        searchResults++;
                    }
                }
            }

            System.out.println();
            System.out.println("Number of valid records processed: " + books.size());
            System.out.println("Number of search results: " + searchResults);
            System.out.println("Number of books added: " + booksAdded);
            System.out.println("Number of errors encountered: " + errorCount);

        } catch (Exception e) {

            if (file != null)
                logError(file, args.length > 1 ? args[1] : "N/A", e);

            System.out.println("Error: " + e.getMessage());

        } finally {
            System.out.println("Thank you for using the Library Book Tracker.");
        }
    }

    /**
     * Prints the formatted header for book display.
     */
    private static void printHeader() {
        System.out.printf("%-30s %-20s %-15s %5s%n",
                "Title", "Author", "ISBN", "Copies");
        System.out.println("---------------------------------------------------------------------");
    }

    /**
     * Prints a single book in formatted table style.
     *
     * @param b The book to display.
     */
    private static void printBook(Book b) {
        System.out.printf("%-30s %-20s %-15s %5d%n",
                b.getTitle(),
                b.getAuthor(),
                b.getIsbn(),
                b.getCopies());
    }

    /**
     * Logs an invalid record or operation into errors.log.
     *
     * @param file The catalog file (used to locate log directory)
     * @param text The invalid input line or operation
     * @param e    The exception thrown during processing
     */
    private static void logError(File file, String text, Exception e) {
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