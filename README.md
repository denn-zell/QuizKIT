# QuizKIT

An interactive, MVC-structured desktop trivia quiz application built with Java Swing and integrated with the OpenTDB (Open Trivia Database) API.

## Features
* **Dynamic Questions:** Fetches trivia dynamically from the OpenTDB API.
* **Custom Game Modes:** Select your preferred difficulty and number of questions before starting.
* **MVC Architecture:** Clean separation of concerns between Model, View, and Controller.
* **Persistent History:** Saves game records and tracks your all-time highscore locally via CSV.
* **Javadoc Documentation:** Fully documented codebase for easy maintenance.

## Requirements
* Java Development Kit (JDK) 17 or higher
* External Library: `opentdb4j.jar` (included in the `lib` folder)

## How to Run
1. Clone this repository or download the source code.
2. Ensure the `opentdb4j.jar` inside the `lib` folder is added to your project's build path in your IDE.
3. Run the `QuizKitMain.java` class to start the application.
