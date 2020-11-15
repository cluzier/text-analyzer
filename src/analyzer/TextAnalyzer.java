package analyzer;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.StringTokenizer;

public class TextAnalyzer extends Application {

    /**
     * @param PrimaryStage GUI for the project, setting up grid pane, title url field and max occurrences field
     */
    public void start(Stage PrimaryStage) {
        PrimaryStage.setTitle("Text Analyzer");
        PrimaryStage.setResizable(false);

        //Creating a GridPane container
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20, 0, 20, 20));
        grid.setVgap(5);

        // title
        Text title = new Text("Text Analyzer");
        title.setStyle("-fx-font: 24 arial;");

        // url field
        Text urlLabel = new Text("url: ");
        TextField urlField = new TextField(targetUrl);
        urlField.setEditable(false);
        urlField.setDisable(true);
        urlField.setMaxWidth(520);
        // the line below is helpful for debugging and working with gridPane layout type
        //grid.setGridLinesVisible(true);

        // max occurrences field
        Text occurranceLabel = new Text("max: ");
        TextField occurranceField = new TextField();
        occurranceField.setMaxWidth(50);

        occurranceField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    occurranceField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });

        // results text area
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setMaxHeight(200);
        area.setMaxWidth(520);

        // sets the binding for the textArea
        StringProperty result = new SimpleStringProperty("");
        IntegerProperty occurrences = new SimpleIntegerProperty(20);
        area.textProperty().bind(Bindings.createStringBinding(() -> (Analyze(occurrences.intValue())), result));

        occurranceField.textProperty().addListener((observable, oldValue, newValue) -> {
            String value = newValue.isEmpty() ? "0" : newValue;
            occurrences.set(Integer.parseInt(value));
            result.set(result.toString());
        });

        // run button
        Button runButton = new Button("Run Analyzer");
        runButton.setMaxWidth(520);
        runButton.setOnAction(e -> result.set(Analyze(occurrences.intValue())));

        // add all components to grid
        grid.add(occurranceLabel, 1, 2);
        grid.add(occurranceField, 2, 2);
        grid.add(title, 2, 0);
        grid.add(area, 2, 3);
        grid.add(runButton, 2, 4);
        grid.add(urlLabel, 1, 1);
        grid.add(urlField, 2, 1);

        Scene scene = new Scene(grid, 600, 300);
        PrimaryStage.setScene(scene);
        PrimaryStage.show();
    }

    // url to parse
    static String targetUrl = "https://www.gutenberg.org/files/1065/1065-h/1065-h.htm";

    // output format
    static String outputFormat = "%-7s %-20s %-8s %1s";

    /**
     * Main method
     */
    public static void main(String[] args) {
        launch(args);
        Analyze(20);
    }

    protected static String Analyze(int occurrences) {
        String output = "";
        // Fetch the URL content
        try {
            BufferedReader urlContent = fetchUrlContent(targetUrl);

            // Remove <pre> tag
            StringBuilder fullDoc = new StringBuilder();
            String line;

            // first convert from bufferedReader to a string via stringBuilder and while loop
            while ((line = urlContent.readLine()) != null) {
                fullDoc.append(line);
            }

            String removedPre = TagRemover.TagRemover(fullDoc.toString(), "pre");

            Reader r = new StringReader(removedPre);
            urlContent = new BufferedReader(r);

            // count word frequencies
            HashMap<String, Integer> wordFrequencies = countWordFrequencies(urlContent);

            // sort the word frequencies
            ArrayList<HashMap.Entry<String, Integer>> sortedWordList = sortWordsByFrequency(wordFrequencies);

            // print the top 20 word frequencies
            //displayWordRankings(sortedWordList, occurrences);
            output = wordRankings(sortedWordList, occurrences);
        } catch (IOException e) {
            System.out.println("An error occurred. Unable to analyze content from URL: " + targetUrl);
        }
        return output;
    }

    /**
     * Fetch the URL to parse
     */
    protected static BufferedReader fetchUrlContent(String url) throws IOException {
        return new BufferedReader(new InputStreamReader(new URL(url).openStream()));
    }

    /**
     * Create a hash map to store the words extracted from the URL and their frequency
     */
    protected static HashMap<String, Integer> countWordFrequencies(BufferedReader urlContent) throws IOException {
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
    protected static String htmlToText(String inputLine) {
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
    protected static ArrayList<HashMap.Entry<String, Integer>> sortWordsByFrequency(HashMap<String, Integer> wordCount) {
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
     * Returns the word frequencies table as a string
     */
    protected static String wordRankings(ArrayList<HashMap.Entry<String, Integer>> sortedWordList, int limit) {
        int rank = 0;

        StringBuilder sb = new StringBuilder(stringHeaders());
        for (HashMap.Entry<String, Integer> temp : sortedWordList) {
            rank++;
            if (rank <= limit) {
                sb.append(String.format(outputFormat, rank, temp.getKey(), temp.getValue(), "\n"));
            } else {
                break;
            }
        }
        return sb.toString();
    }

    /**
     * table headers returned as string
     */
    protected static String stringHeaders() {
        return String.format(outputFormat, "Rank", "Word", "Frequency", "\n");
    }


}