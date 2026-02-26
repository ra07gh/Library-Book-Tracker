import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OperationTask implements Runnable {

    private List<Book> books;
    private String operation;
    private File file;
    private int[] errorCount;

    public OperationTask(List<Book> books,
                         String operation,
                         File file,
                         int[] errorCount) {

        this.books = books;
        this.operation = operation;
        this.file = file;
        this.errorCount = errorCount;
    }

    public void run() {

        int searchResults = 0;
        int booksAdded = 0;

        try {

            if (operation.matches("\\d{13}")) {

                for (Book b : books) {
                    if (b.getIsbn().equals(operation)) {
                        printHeader();
                        printBook(b);
                        searchResults = 1;
                    }
                }

            } else if (operation.contains(":")) {

                String[] parts = operation.split(":");

                if (parts.length != 4)
                    throw new MalformedBookEntryException("Invalid add format.");

                String title = parts[0];
                String author = parts[1];
                String isbn = parts[2];
                int copies = Integer.parseInt(parts[3]);

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
            System.out.println("Number of errors encountered: " + errorCount[0]);

        } catch (Exception e) {

            LibraryBookTracker.logError(file, operation, e);
            errorCount[0]++;
        }
    }

    private void printHeader() {
        System.out.printf("%-30s %-20s %-15s %5s%n",
                "Title", "Author", "ISBN", "Copies");
        System.out.println("-------------------------------------------------------------");
    }

    private void printBook(Book b) {
        System.out.printf("%-30s %-20s %-15s %5d%n",
                b.getTitle(),
                b.getAuthor(),
                b.getIsbn(),
                b.getCopies());
    }
}