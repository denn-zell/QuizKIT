package edu.kit.aifb.proksy.quiz.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Represents the main user interface of the QuizKIT application.
 * Built using Java Swing with a CardLayout to navigate between the main menu, 
 * the active game screen, and the history leaderboard.
 *
 * @author denn-zell
 * @version 1.4
 */
public class QuizView extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    // Layout manager for switching screens
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);

    // Styling constants for a modern look
    private static final Color COLOR_BACKGROUND = new Color(43, 43, 43);
    private static final Color COLOR_PANEL_BG = new Color(50, 50, 50);
    private static final Color COLOR_TEXT = new Color(240, 240, 240);
    private static final Color COLOR_BUTTON_BG = new Color(70, 73, 75);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);

    // Main Menu Components
    private final JPanel menuPanel = new JPanel();
    private JButton startButton;
    private JButton historyButton;
    private JLabel statsLabel;
    private JComboBox<Integer> countCombo;
    private JComboBox<String> difficultyCombo;

    // Game Screen Components
    private final JPanel gamePanel = new JPanel();
    private JLabel questionLabel;
    private JLabel scoreLabel;
    private JButton[] answerButtons;
    private JButton nextQuestionButton;

    // History Screen Components
    private final JPanel historyPanel = new JPanel();
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JButton backToMenuButton;

    /**
     * Constructs the main application frame, applies structural padding, 
     * initializes all sub-panels, and positions the window centered on screen.
     */
    public QuizView() {
        setTitle("QuizKIT - The Ultimate Trivia Game");
        setSize(650, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        mainPanel.setBackground(COLOR_BACKGROUND);
        
        createMenuPanel();
        createGamePanel();
        createHistoryPanel();
        
        add(mainPanel);
        showMenu();
    }

    /**
     * Builds the main menu panel layout, featuring game setup dropdowns and overall statistics.
     */
    private void createMenuPanel() {
        menuPanel.setLayout(new GridBagLayout());
        menuPanel.setBackground(COLOR_BACKGROUND);
        menuPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Statistics Header
        statsLabel = new JLabel("Highscore: 0 | Total Games: 0", SwingConstants.CENTER);
        statsLabel.setFont(FONT_TITLE);
        statsLabel.setForeground(COLOR_TEXT);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        menuPanel.add(statsLabel, gbc);

        // Question Count Selection
        JLabel countLabel = new JLabel("Number of Questions:");
        countLabel.setFont(FONT_BODY);
        countLabel.setForeground(COLOR_TEXT);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        menuPanel.add(countLabel, gbc);

        List<Integer> countsList = new ArrayList<>();
        for (int i = 5; i <= 50; i += 5) {
            countsList.add(i);
        }
        countCombo = new JComboBox<>(countsList.toArray(new Integer[0]));
        countCombo.setFont(FONT_BODY);
        gbc.gridx = 1; gbc.gridy = 1;
        menuPanel.add(countCombo, gbc);

        // Difficulty Selection
        JLabel difficultyLabel = new JLabel("Difficulty Level:");
        difficultyLabel.setFont(FONT_BODY);
        difficultyLabel.setForeground(COLOR_TEXT);
        gbc.gridx = 0; gbc.gridy = 2;
        menuPanel.add(difficultyLabel, gbc);

        String[] difficulties = {"Easy", "Medium", "Hard"};
        difficultyCombo = new JComboBox<>(difficulties);
        difficultyCombo.setFont(FONT_BODY);
        difficultyCombo.setSelectedItem("Medium");
        gbc.gridx = 1; gbc.gridy = 2;
        menuPanel.add(difficultyCombo, gbc);

        // Action Buttons
        startButton = styleButton(new JButton("Start Quiz"));
        startButton.setFont(FONT_TITLE);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        menuPanel.add(startButton, gbc);

        historyButton = styleButton(new JButton("View Game History"));
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        menuPanel.add(historyButton, gbc);

        mainPanel.add(menuPanel, "MENU");
    }

    /**
     * Builds the interactive gameplay arena grid with dynamic question labels and answer grids.
     */
    private void createGamePanel() {
        gamePanel.setLayout(new BorderLayout(15, 15));
        gamePanel.setBackground(COLOR_BACKGROUND);
        gamePanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        scoreLabel = new JLabel("Question: 0/0  |  Points: 0  |  Highscore: 0", SwingConstants.CENTER);
        scoreLabel.setFont(FONT_TITLE);
        scoreLabel.setForeground(COLOR_TEXT);
        gamePanel.add(scoreLabel, BorderLayout.NORTH);
        
        questionLabel = new JLabel("Loading trivia text...", SwingConstants.CENTER);
        questionLabel.setFont(FONT_BODY);
        questionLabel.setForeground(COLOR_TEXT);
        gamePanel.add(questionLabel, BorderLayout.CENTER);
        
        JPanel southPanel = new JPanel(new BorderLayout(15, 15));
        southPanel.setBackground(COLOR_BACKGROUND);
        
        JPanel buttonGrid = new JPanel(new GridLayout(2, 2, 12, 12));
        buttonGrid.setBackground(COLOR_BACKGROUND);
        answerButtons = new JButton[4];
        for (int i = 0; i < 4; i++) {
            answerButtons[i] = styleButton(new JButton("Answer Option " + (i + 1)));
            buttonGrid.add(answerButtons[i]);
        }
        southPanel.add(buttonGrid, BorderLayout.CENTER);
        
        nextQuestionButton = styleButton(new JButton("Next Question"));
        nextQuestionButton.setFont(FONT_TITLE);
        nextQuestionButton.setEnabled(false);
        southPanel.add(nextQuestionButton, BorderLayout.SOUTH);
        
        gamePanel.add(southPanel, BorderLayout.SOUTH);
        mainPanel.add(gamePanel, "GAME");
    }
    
    /**
     * Builds the history logging window featuring a stylized data overview table.
     */
    private void createHistoryPanel() {
        historyPanel.setLayout(new BorderLayout(15, 15));
        historyPanel.setBackground(COLOR_BACKGROUND);
        historyPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        String[] columns = {"Game ID", "Question", "Difficulty", "Correct Answer", "Your Answer", "Score"};
        tableModel = new DefaultTableModel(columns, 0);
        historyTable = new JTable(tableModel);
        historyTable.getTableHeader().setFont(FONT_BODY);
        historyTable.setFont(FONT_BODY);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.getViewport().setBackground(COLOR_PANEL_BG);
        historyPanel.add(scrollPane, BorderLayout.CENTER);

        backToMenuButton = styleButton(new JButton("Back to Main Menu"));
        historyPanel.add(backToMenuButton, BorderLayout.SOUTH);

        mainPanel.add(historyPanel, "HISTORY");
    }

    /**
     * Helper method to inject consistent flat styling parameters into standard Swing buttons.
     * Bypasses OS native painting overrides to guarantee flat dark themes.
     */
    private JButton styleButton(JButton button) {
        button.setFont(FONT_BODY);
        button.setBackground(COLOR_BUTTON_BG);
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BUTTON_BG.brighter(), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        return button;
    }

    // Navigation triggers
    public void showMenu() { cardLayout.show(mainPanel, "MENU"); }
    public void showGame() { cardLayout.show(mainPanel, "GAME"); }
    public void showHistory() { cardLayout.show(mainPanel, "HISTORY"); }

    // Controller entry encapsulation getters
    public JButton getStartButton() { return startButton; }
    public JButton getHistoryButton() { return historyButton; }
    public JButton getBackToMenuButton() { return backToMenuButton; }
    public JButton[] getAnswerButtons() { return answerButtons; }
    public int getSelectedQuestionCount() { return (int) countCombo.getSelectedItem(); }
    public DefaultTableModel getTableModel() { return tableModel; }
    public String getSelectedDifficulty() { return (String) difficultyCombo.getSelectedItem(); }
    public JButton getNextQuestionButton() { return nextQuestionButton; }

    // Dynamic text refresh binding utilities
    public void updateQuestion(String text) { questionLabel.setText(text); }
    
    public void updateGameHeader(int currentQuestion, int totalQuestions, int points, int highscore) {
        scoreLabel.setText("Question: " + currentQuestion + "/" + totalQuestions + "  |  Points: " + points + "  |  Highscore: " + highscore);
    }
    
    public void updateStats(int highscore, int totalGames) {
        statsLabel.setText("Highscore: " + highscore + " | Total Games Played: " + totalGames);
    }

    /**
     * Pops up a graphical error handling message window.
     *
     * @param message the description text explaining the encountered runtime anomaly
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Runtime Error", JOptionPane.ERROR_MESSAGE);
    }
}