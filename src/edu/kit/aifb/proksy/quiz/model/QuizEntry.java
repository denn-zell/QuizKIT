package edu.kit.aifb.proksy.quiz.model;

/**
 * Represents a single answered question entry within a game session.
 * Immutable data object mapped directly to a single line inside the history CSV file.
 *
 * @param gameId        the unique identifier of the game session (based on timestamp)
 * @param question      the text of the asked question
 * @param difficulty    the difficulty level of the question (EASY, MEDIUM, HARD)
 * @param correctAnswer the correct answer provided by the API
 * @param chosenAnswer  the answer selected by the player
 * @param points        the points awarded for this question (100 for correct, 0 for incorrect)
 *
 * @author denn-zell
 * @version 1.1
 */
public record QuizEntry(
    String gameId,
    String question,
    String difficulty,
    String correctAnswer,
    String chosenAnswer,
    int points
) {
}