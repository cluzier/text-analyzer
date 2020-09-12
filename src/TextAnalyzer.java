import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.StringTokenizer;

public class TextAnalyzer {
    // url to parse
    static String targetUrl = "https://www.gutenberg.org/files/1065/1065-h/1065-h.htm";

    // output format
    static String outputFormat = "%-7s %-20s %-8s %1s";

    /**
     * Main method
     */
    public static void main(String[] args) {
        // Fetch the URL content
        try {
            BufferedReader urlContent = fetchUrlContent();

            // Remove <pre> tag
            StringBuilder fullDoc = new StringBuilder();
            String line;

            // first convert from bufferedReader to a string via stringBuilder and while loop
            while((line = urlContent.readLine()) != null) {
                fullDoc.append(line);
            }

            String removedPre = TagRemover.TagRemover(fullDoc.toString());

            Reader r = new StringReader(removedPre);
            urlContent = new BufferedReader(r);

            // count word frequencies
            HashMap<String, Integer> wordFrequencies = countWordFrequencies(urlContent);

            // sort the word frequencies
            ArrayList<HashMap.Entry<String, Integer>> sortedWordList = sortWordsByFrequency(wordFrequencies);

            // print the top 20 word frequencies
            displayWordRankings(sortedWordList, 20);
        } catch (IOException e) {
            System.out.println("An error occurred. Unable to analyze content from URL: " + targetUrl);
        }
    }

    /**
     * Fetch the URL to parse
     */
    private static BufferedReader fetchUrlContent() throws IOException {
        return new BufferedReader(new InputStreamReader(new URL(targetUrl).openStream()));
    }

    /**
     * Create a hash map to store the words extracted from the URL and their frequency
     */
    private static HashMap<String, Integer> countWordFrequencies(BufferedReader urlContent) throws IOException {
        // temp string stores each line of buffered inputUrl
        String inputLine;
        // temp array stores words from the inputUrl
        String[] words;

        // HashMap stores words as keys and frequency as values
        HashMap<String, Integer> wordCount = new HashMap<String, Integer>();

        // Add words and their frequency to the hash map
        while ((inputLine = urlContent.readLine()) != null) {
            // convert the html formatted line to plain text
            String filteredInputLine = htmlToText(inputLine);

            // extract words from filteredInputLine using StringTokenizer
            StringTokenizer wordsInLine = new StringTokenizer(filteredInputLine);

            // add words and their frequencies to the wordCount HashMap
            while (wordsInLine.hasMoreTokens()) {
                String word = wordsInLine.nextToken();
                Integer currentWordFrequency = wordCount.get(word);
                int newWordFrequency = currentWordFrequency == null ? 1 : currentWordFrequency + 1;
                wordCount.put(word, newWordFrequency);
            }
        }
        urlContent.close();
        return wordCount;
    }

    /**
     * Converts each line of the inputFile from html --> plain text
     */
    private static String htmlToText(String inputLine) {
        return inputLine
                .toLowerCase() // convert to lower case
                .replaceAll("<.*?>", "") // strip html tags
                .replaceAll("<.*", "") // hack: strip unclosed tags
                .replaceAll(".*?>", "") // hack: strip unopened tags
                .replaceAll("[|.?!,;:{}()]", "") // strip punctuation
                .replaceAll("--", " ") // strip multiple double dashes found in the text
                .trim(); // trim remaining whitespace
    }

    /**
     * Method to sort the wordCount HashMap by frequency values
     */
    private static ArrayList<HashMap.Entry<String, Integer>> sortWordsByFrequency(HashMap<String, Integer> wordCount) {
        // create and populate an ArrayList with the words in the wordCount HashMap and their frequencies
        ArrayList<HashMap.Entry<String, Integer>> sortedWordList = new ArrayList<HashMap.Entry<String, Integer>>(wordCount.entrySet());

        // use Comparator to sort the ArrayList
        sortedWordList.sort(new Comparator<HashMap.Entry<String, Integer>>() {
            public int compare(HashMap.Entry<String, Integer> freq1, HashMap.Entry<String, Integer> freq2) {
                return freq2.getValue().compareTo(freq1.getValue());
            }
        });

        return sortedWordList;
    }

    /**
     * Displays the word frequencies table in console
     */
    private static void displayWordRankings(ArrayList<HashMap.Entry<String, Integer>> sortedWordList, int limit) {
        int rank = 0;

        outputHeaders();

        for (HashMap.Entry<String, Integer> temp : sortedWordList) {
            rank++;
            if (rank <= limit) {
                System.out.format(outputFormat, rank + ".", temp.getKey(), temp.getValue(), "\n");
            } else {
                break;
            }
        }
    }

    /**
     * table headers sent to console
     */
    private static void outputHeaders() {
        System.out.printf(outputFormat, "Rank", "Word", "Frequency", "\n");
    }

}