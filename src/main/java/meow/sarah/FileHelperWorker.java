package meow.sarah;

/**
 * @author https://github.com/PrincessAkira (Sarah)
 * Today is the 11/6/2023 @3:46 PM
 * This project is named LeagueBackseat
 * @description Another day of Insanity
 */
public class FileHelperWorker implements Runnable {

    public FileHelperWorker(FileHelper fileHelper) {
    }

    @Override
    public void run() {
        while (true) {
            // Perform your work here (e.g., file processing)

            // Sleep for a specific interval before the next execution
            try {
                Thread.sleep(5000); // Sleep for 5 seconds (adjust as needed)
            } catch (InterruptedException e) {
                // Handle InterruptedException if necessary
            }
        }
    }
}
