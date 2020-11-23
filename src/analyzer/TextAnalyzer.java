package analyzer;

import java.sql.Statement;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.UUID;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class TextAnalyzer extends Application {

	static String originalUrl = "https://www.gutenberg.org/files/1065/1065-h/1065-h.htm";
	static UUID uuid = null;
	
	
	protected static void updateWordFrequency(String word, int frequency) {
		Connection conn = null;

		String str = "replace into word_occurences.word (word,frequency) values (?, ?)";
		try {
			conn = JDBCMySQLConnection.getConnection();
			PreparedStatement query = conn.prepareStatement(str);
			query.setString(1, word);
			query.setInt(2, frequency);
			query.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}	
	}
	
	protected static int getCurrentWordFrequency(String word) {
		Connection conn = null;
		Statement statement = null;

		String str = "select frequency from word_occurences.word where word = ?;";
		int freq = 0;
		try {
			
			conn = JDBCMySQLConnection.getConnection();
			PreparedStatement query = conn.prepareStatement(str);
			query.setString(1, word);
			ResultSet rs = query.executeQuery();
			
			while (rs.next()) {
				freq = rs.getInt("frequency");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return freq;
	}

	
	private static void setUUID() {
		uuid = UUID.randomUUID();
	}
	
    public void start(Stage PrimaryStage) {
        PrimaryStage.setTitle("Text Analyzer");
        PrimaryStage.setResizable(false);
        //Creating a GridPane container
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20,0,20,20));
        grid.setVgap(5);
        
        // title
        Text title = new Text("Text Analyzer");
        title.setStyle("-fx-font: 24 arial;");
        
        // url field
        Text urlLabel = new Text("url: ");
        TextField urlField = new TextField(targetUrl);
        // setting this to false again since we do not have a solution to deal 
        // with multiple different sites within the DB
        urlField.setEditable(false);
        urlField.setDisable(false);
        urlField.setMaxWidth(520);
        // the below is helpful for debugging
        // and working with gridPane layout type
        //grid.setGridLinesVisible(true);
        
        
        
        // max occurrences field
        Text occurranceLabel = new Text("max: ");
        TextField occurranceField = new TextField();
        occurranceField.setMaxWidth(50);
        
        // shamelessly stolen from:
        // https://stackoverflow.com/questions/7555564/what-is-the-recommended-way-to-make-a-numeric-textfield-in-javafx
        occurranceField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                String newValue) {
                if (!newValue.matches("\\d*")) {
                	occurranceField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        
        // result text area
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setMaxHeight(200);
        area.setMaxWidth(520);
        
        // sets the binding for the textArea 
        // based on https://stackoverflow.com/questions/34514694/display-variable-value-in-text-javafx
        StringProperty result = new SimpleStringProperty("");
        IntegerProperty occurrences = new SimpleIntegerProperty(20);
        area.textProperty().bind(Bindings.createStringBinding(() -> (runAnalyzer(occurrences.intValue())), result));
        
        occurranceField.textProperty().addListener((observable, oldValue, newValue) -> {
        	String value = newValue.isEmpty() ? "0" : newValue;
    		occurrences.set(Integer.parseInt(value));
    		result.set(result.toString());
        });
        
        // reset button
        Button resetBtn = new Button("Reset URL");
        resetBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override 
            public void handle(ActionEvent e) {
            	urlField.setText(originalUrl);
            }
        });
        
        // run button
        Button runButton = new Button("Run Analyzer");
        runButton.setMaxWidth(520);
        runButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override 
            public void handle(ActionEvent e) {
            	targetUrl = urlField.getText();
        		result.set(result.toString());
            	runAnalyzer(occurrences.intValue());
            }
        });
        // add all components to grid
        grid.add(occurranceLabel, 1, 2);
        grid.add(occurranceField, 2, 2);
        //grid.add(resetBtn, 3, 1);
        grid.add(title, 2, 0);
        grid.add(area, 2, 3);
        grid.add(runButton, 2, 4);
        grid.add(urlLabel, 1, 1);
        grid.add(urlField, 2, 1);
        
        Scene scene = new Scene(grid, 600, 300);
        PrimaryStage.setScene(scene);
        PrimaryStage.show();
        
       
    }
	private String runAnalyzer(int intValue) {
		String ret = "";
    	try {
    		setUUID();
    		ret = Analyze(intValue);
    	} catch (IOException e) {
    		Alert alert = new Alert(AlertType.ERROR);
    		alert.setTitle("URL Error");
    		alert.setHeaderText(null);
    		alert.setContentText("Invalid website! Cannot parse\nPerhaps you forgot to add 'https://'?");
    		alert.showAndWait();
    	}
    	return ret;
	}
	// url to parse
    static String targetUrl = originalUrl;

    // output format
    static String outputFormat = "%-7s %-20s %-8s %1s";

    /**
     * Main method
     */
    public static void main(String[] args) {
    	setUUID();
        launch(args);
    }

    protected static String Analyze(int occurrences) throws IOException {
    	String output = "";
        // Fetch the URL content
        try {
            BufferedReader urlContent = fetchUrlContent(targetUrl);

            // Remove <pre> tag
            StringBuilder fullDoc = new StringBuilder();
            String line;

            // first convert from bufferedReader to a string via stringBuilder and while loop
            while((line = urlContent.readLine()) != null) {
                fullDoc.append(line);
            }

            String removedPre = TagRemover.TagRemover(fullDoc.toString(), "pre");

            Reader r = new StringReader(removedPre);
            urlContent = new BufferedReader(r);

            // count word frequencies
            countWordFrequencies(urlContent);

            // sort the word frequencies
            //ArrayList<HashMap.Entry<String, Integer>> sortedWordList = sortWordsByFrequency(wordFrequencies);

            // print the top 20 word frequencies
            //displayWordRankings(sortedWordList, occurrences);
            //output = wordRankings(sortedWordList, occurrences);
        } catch (IOException e) {
            throw new IOException("An error occurred. Unable to analyze content from URL: " + targetUrl);
        	//System.out.println("An error occurred. Unable to analyze content from URL: " + targetUrl);
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
    protected static void countWordFrequencies(BufferedReader urlContent) throws IOException {
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
                Integer currentWordFrequency = getCurrentWordFrequency(word);
                updateWordFrequency(word, currentWordFrequency + 1);
            }
        }
        urlContent.close();
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