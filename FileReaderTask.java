import java.io.*;
import java.util.List;

public class FileReaderTask implements Runnable {
    
    private int[] errorCount;
    private File file;
    private List<Book> books;

  public FileReaderTask(File file, List<Book> books, int[] errorCount) {
    this.file = file;
    this.books = books;
    this.errorCount = errorCount;
}

    public void run() {

        try {

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
                    LibraryBookTracker.logError(file, line, e);
                    errorCount[0]++;              
                  }
            }

            reader.close();
        } catch (Exception e) {
            System.out.println("Error in FileReaderTask");
        }
    }
}



