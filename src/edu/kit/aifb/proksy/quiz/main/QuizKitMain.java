package edu.kit.aifb.proksy.quiz.main;

import javax.swing.UIManager;
import edu.kit.aifb.proksy.quiz.controller.QuizController;
import edu.kit.aifb.proksy.quiz.model.QuizModel;
import edu.kit.aifb.proksy.quiz.view.QuizView;

/**
 * The main entry point for the QuizKIT application.
 * Initializes the system look-and-feel framework and boots the core MVC structural layers.
 *
 * @author denn-zell
 * @version 1.1
 */
public class QuizKitMain {

    /**
     * Main method that launches the standalone interactive application instance.
     *
     * @param args runtime execution arguments (not utilized)
     */
    public static void main(String[] args) {
        // Apply the native operating system look-and-feel layout style
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not load system look-and-feel framework: " + e.getMessage());
        }

        // Initialize core application components
        QuizModel model = new QuizModel();
        QuizView view = new QuizView();
        
        // Link components through the controller pipeline
        new QuizController(model, view);

        // Make the styled view frame interactive
        view.setVisible(true);
    }
}