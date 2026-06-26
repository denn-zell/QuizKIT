package edu.kit.aifb.proksy.quiz.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.kit.aifb.atks.opentdb4j.OpenTDB;
import edu.kit.aifb.atks.opentdb4j.Question;
import edu.kit.aifb.atks.opentdb4j.QuestionDifficulty;
import edu.kit.aifb.atks.opentdb4j.QuestionType;

/**
 * Manages the core logic, state, and data persistence of the QuizKIT application.
 * Handles fetching questions via the OpenTDB API and saving/loading the game history to a CSV file.
 *
 * @author denn-zell
 * @version 1.2
 */
public class QuizModel {
    private static final String CSV_FILE_PATH = "history.csv";
    private static final String CSV_SEPARATOR = ";";
    
    private final List<QuizEntry> history = new ArrayList<>();
    
    private List<Question> currentQuestions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int currentPoints = 0;
    private String currentGameId;

    /**
     * Starts a new game session, generates a unique game ID based on the current timestamp,
     * and fetches matching questions from the OpenTDB API.
     *
     * @param amount        the number of questions to fetch
     * @param difficultyStr the difficulty level as a String ("Easy", "Medium", "Hard")
     * @throws Exception if fetching questions fails due to network issues or invalid API responses
     */
    public void startNewGame(int amount, String difficultyStr) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        this.currentGameId = "GAME_" + now.format(formatter);
        
        this.currentQuestionIndex = 0;
        this.currentPoints = 0;
        
        QuestionDifficulty difficultyEnum = null;
        if (difficultyStr.equalsIgnoreCase("Easy")) {
            difficultyEnum = QuestionDifficulty.EASY;
        } else if (difficultyStr.equalsIgnoreCase("Medium")) {
            difficultyEnum = QuestionDifficulty.MEDIUM;
        } else if (difficultyStr.equalsIgnoreCase("Hard")) {
            difficultyEnum = QuestionDifficulty.HARD;
        }

        QuestionType typeEnum = QuestionType.MULTIPLE_CHOICE;
       
        try {
            this.currentQuestions = OpenTDB.fetchQuestions(amount, typeEnum, difficultyEnum, null);
            if (this.currentQuestions == null || this.currentQuestions.isEmpty()) {
                throw new IOException("No questions returned from the API.");
            }
        } catch (Exception e) {
            this.currentQuestions = new ArrayList<>();
            throw new Exception("Failed to load questions from API: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the current question to be displayed.
     *
     * @return the current {@link Question}, or {@code null} if no active session or game is over
     */
    public Question getCurrentQuestion() {
        if (currentQuestionIndex >= 0 && currentQuestionIndex < currentQuestions.size()) {
            return currentQuestions.get(currentQuestionIndex);
        }
        return null;
    }

    /**
     * Advances the internal index to the next question.
     */
    public void nextQuestion() {
        currentQuestionIndex++;
    }

    /**
     * Checks if there are more questions available in the current game session.
     *
     * @return {@code true} if there is a next question, {@code false} otherwise
     */
    public boolean hasNextQuestion() {
        return currentQuestionIndex < currentQuestions.size();
    }

    public String getCurrentGameId() { return currentGameId; }
    public int getCurrentPoints() { return currentPoints; }
    public void addPoints(int points) { this.currentPoints += points; }
    public int getCurrentQuestionIndex() { return currentQuestionIndex; }
    public int getTotalQuestionsCount() { return currentQuestions.size(); }
    public List<QuizEntry> getHistory() { return Collections.unmodifiableList(history); }

    /**
     * Loads the entire quiz history from the local CSV storage file.
     * Skips empty or corrupted lines to ensure data stability.
     */
    public void loadHistory() {
        File file = new File(CSV_FILE_PATH);
        if (!file.exists()) {
            return;
        }

        history.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                String[] parts = line.split(CSV_SEPARATOR);
                if (parts.length == 6) {
                    try {
                        QuizEntry entry = new QuizEntry(
                            parts[0], 
                            parts[1], 
                            parts[2], 
                            parts[3], 
                            parts[4], 
                            Integer.parseInt(parts[5])
                        );
                        history.add(entry);
                    } catch (NumberFormatException nfe) {
                        System.err.println("Skipping corrupted history line (invalid points format): " + line);
                    }
                } else {
                    System.err.println("Skipping corrupted history line (invalid column count): " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the history CSV file: " + e.getMessage());
        }
    }

    /**
     * Appends a new quiz answer entry to the local CSV history file and updates the in-memory cache.
     *
     * @param entry the {@link QuizEntry} containing details of the answered question
     */
    public void saveEntry(QuizEntry entry) {
        history.add(entry);
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CSV_FILE_PATH, true))) {
            String line = String.join(CSV_SEPARATOR,
                entry.gameId(),
                entry.question(),
                entry.difficulty(),
                entry.correctAnswer(),
                entry.chosenAnswer(),
                String.valueOf(entry.points())
            );
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error writing entry to history CSV file: " + e.getMessage());
        }
    }

    /**
     * Computes the highest total score achieved within a single completed game session.
     * Excludes the currently active live game session from the calculation.
     *
     * @return the highscore value, or {@code 0} if no completed history exists
     */
    public int getHighscore() {
        Map<String, Integer> gameScores = new HashMap<>();
        for (QuizEntry entry : history) {
            if (currentGameId != null && entry.gameId().equals(currentGameId)) {
                continue;
            }
            gameScores.put(entry.gameId(), gameScores.getOrDefault(entry.gameId(), 0) + entry.points());
        }
        return gameScores.values().stream().max(Integer::compare).orElse(0);
    }

    /**
     * Calculates the total number of unique game sessions played.
     *
     * @return the number of unique games
     */
    public int getPlayedGamesCount() {
        Set<String> uniqueGames = new HashSet<>();
        for (QuizEntry entry : history) {
            uniqueGames.add(entry.gameId());
        }
        return uniqueGames.size();
    }
}