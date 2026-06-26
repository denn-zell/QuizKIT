package edu.kit.aifb.proksy.quiz.controller;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import edu.kit.aifb.atks.opentdb4j.Question;
import edu.kit.aifb.proksy.quiz.model.QuizEntry;
import edu.kit.aifb.proksy.quiz.model.QuizModel;
import edu.kit.aifb.proksy.quiz.view.QuizView;

/**
 * The central controller of the QuizKIT application.
 * Manages the interactions between the {@link QuizModel} and {@link QuizView},
 * binds UI event listeners, and coordinates game states.
 *
 * @author denn-zell
 * @version 1.2
 */
public class QuizController {

    private final QuizModel model;
    private final QuizView view;
    private String currentlySelectedAnswer = null;

    /**
     * Constructs a new QuizController, loads the local game history cache,
     * initializes action listeners, and synchronizes the main menu statistics display.
     *
     * @param model the data and logic container
     * @param view  the graphical user interface frame
     */
    public QuizController(QuizModel model, QuizView view) {
        this.model = model;
        this.view = view;

        // Load the persistent CSV history on startup
        this.model.loadHistory();

        // Initialize UI components and hook up listeners
        initListeners();
        updateMenuStats();
    }

    /**
     * Registers all reactive button listeners across the various application view contexts.
     */
    private void initListeners() {
        // Main Menu Button bindings
        view.getStartButton().addActionListener(e -> handleStartGame());
        view.getHistoryButton().addActionListener(e -> handleShowHistory());

        // History View navigation
        view.getBackToMenuButton().addActionListener(e -> view.showMenu());

        // Multi-choice quiz grid selection bindings
        JButton[] answerButtons = view.getAnswerButtons();
        for (JButton button : answerButtons) {
            button.addActionListener(e -> {
                currentlySelectedAnswer = button.getText();
                
                // Clear selection states across grid and highlight the chosen variant
                for (JButton b : answerButtons) {
                    b.setBackground(null);
                }
                button.setBackground(Color.GRAY);
                
                // Enable next question navigation step
                view.getNextQuestionButton().setEnabled(true);
            });
        }

        view.getNextQuestionButton().addActionListener(e -> handleNextQuestionClick());
    }

    /**
     * Evaluates visual user filters and launches a new quiz game session.
     * Wraps internal network calls safely to guard against API delivery failure states.
     */
    private void handleStartGame() {
        int count = view.getSelectedQuestionCount();
        String difficulty = view.getSelectedDifficulty(); 
        
        // Temporarily clear the progress board for game entry
        view.updateGameHeader(1, count, 0, model.getHighscore());
        
        try {
            model.startNewGame(count, difficulty);
        } catch (Exception e) {
            view.showError("Failed to fetch trivia questions. Please check your internet connection or try a different difficulty setting!");
            view.showMenu();
            return;
        }

        if (model.getCurrentQuestion() == null || model.getTotalQuestionsCount() == 0) {
            view.showError("No data available for the selected criteria. Please modify game configuration rules.");
            view.showMenu();
            return;
        }

        displayCurrentQuestion();
        view.showGame();
    }

    /**
     * Extracts active data nodes from the model layer and renders them within the game grid frame.
     */
    private void displayCurrentQuestion() {
        Question question = model.getCurrentQuestion();
        if (question == null) return;

        int currentNum = model.getCurrentQuestionIndex() + 1;
        int totalNum = model.getTotalQuestionsCount();
        int points = model.getCurrentPoints();
        int highscore = model.getHighscore();
        view.updateGameHeader(currentNum, totalNum, points, highscore);
        
        currentlySelectedAnswer = null;
        view.getNextQuestionButton().setEnabled(false);
        view.updateQuestion(question.question());

        // Flatten question answers dataset and randomize alignment indices
        List<String> answers = new ArrayList<>(question.incorrectAnswers());
        answers.add(question.correctAnswer());
        Collections.shuffle(answers);

        JButton[] buttons = view.getAnswerButtons();
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setBackground(null);
            if (i < answers.size()) {
                buttons[i].setText(answers.get(i));
                buttons[i].setVisible(true);
            } else {
                buttons[i].setVisible(false);
            }
        }

        // Alter action trigger naming dynamically on terminal index discovery
        if (model.getCurrentQuestionIndex() == model.getTotalQuestionsCount() - 1) {
            view.getNextQuestionButton().setText("End Quiz");
        } else {
            view.getNextQuestionButton().setText("Next Question");
        }
    }

    /**
     * Processes input validation scores, commits entries to disk, 
     * and evaluates highscore progression limits.
     */
    private void handleNextQuestionClick() {
        Question question = model.getCurrentQuestion();
        if (question == null || currentlySelectedAnswer == null) return;

        boolean isCorrect = currentlySelectedAnswer.equals(question.correctAnswer());
        int pointsForThisQuestion = isCorrect ? 100 : 0;

        if (isCorrect) {
            model.addPoints(pointsForThisQuestion);
        }

        // Standardize entity schema entry format mapping rules
        QuizEntry entry = new QuizEntry(
            model.getCurrentGameId(),
            question.question(),
            question.difficulty().name(),
            question.correctAnswer(),
            currentlySelectedAnswer,
            pointsForThisQuestion
        );
        model.saveEntry(entry);

        // Evaluate terminal execution boundary states
        if (model.getCurrentQuestionIndex() == model.getTotalQuestionsCount() - 1) {
            int finalScore = model.getCurrentPoints();
            int previousHighscore = 0;
            
            Map<String, Integer> gameScores = new HashMap<>();
            for (QuizEntry e : model.getHistory()) {
                if (!e.gameId().equals(model.getCurrentGameId())) {
                    gameScores.put(e.gameId(), gameScores.getOrDefault(e.gameId(), 0) + e.points());
                }
            }
            if (!gameScores.isEmpty()) {
                previousHighscore = Collections.max(gameScores.values());
            }

            // Highscore progression announcement dialogs
            if (finalScore > previousHighscore) {
                JOptionPane.showMessageDialog(view, 
                    "Congratulations! You broke the highscore!\n" +
                    "New Record: " + finalScore + " points! (Previous: " + previousHighscore + ")", 
                    "New Highscore!", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(view, 
                    "Game Over! Your total score: " + finalScore + " points.\n" +
                    "The highscore remains at " + previousHighscore + " points.", 
                    "Game Over", JOptionPane.INFORMATION_MESSAGE);
            }
            
            updateMenuStats();
            view.showMenu();
        } else {
            model.nextQuestion();
            if (model.hasNextQuestion()) {
                displayCurrentQuestion();
            }
        }
    }

    /**
     * Flushes table view content arrays and loops memory storage entries into active data visuals.
     */
    private void handleShowHistory() {
        DefaultTableModel tableModel = view.getTableModel();
        tableModel.setRowCount(0);

        for (QuizEntry entry : model.getHistory()) {
            // Formats pure uppercase values (e.g. "MEDIUM") to Title Case ("Medium")
            String rawDifficulty = entry.difficulty();
            String formattedDifficulty = rawDifficulty;
            if (rawDifficulty != null && rawDifficulty.length() > 0) {
                formattedDifficulty = rawDifficulty.substring(0, 1).toUpperCase() 
                                    + rawDifficulty.substring(1).toLowerCase();
            }

            tableModel.addRow(new Object[]{
                entry.gameId(),
                entry.question(),
                formattedDifficulty,
                entry.correctAnswer(),
                entry.chosenAnswer(),
                entry.points()
            });
        }

        view.showHistory();
    }

    /**
     * Refreshes overall system metric stats from data layer maps.
     */
    private void updateMenuStats() {
        view.updateStats(model.getHighscore(), model.getPlayedGamesCount());
    }
}