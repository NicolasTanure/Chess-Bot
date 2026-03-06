# Chess-Bot

A small Java-based chess bot that uses Selenium to control a browser (Safari) and Stockfish as the engine.
This project is designed to play fast time controls (bullet/1-minute) on chess.com by reading the board,
generating a FEN position, querying Stockfish for a move, and performing the move via Selenium.

## Features
- Reads piece positions from the chess.com board and builds a FEN string
- Uses Stockfish (command-line) to compute the best move
- Controls the browser via Selenium (SafariDriver)
- Handles promotions like `e7e8q` (best-effort selector heuristics)
- Fast polling mode suitable for bullet games (aggressive 100ms poll)

## Prerequisites
- Java 11 or newer
- Maven
- Stockfish installed and available in your `PATH` (e.g. `brew install stockfish` on macOS)
- Safari (macOS): enable `Develop -> Allow Remote Automation` in Safari's menu

Notes:
- The code currently uses `SafariDriver`. If you want to use a different browser, update `Main.java` accordingly.
- Ensure the `stockfish` executable is callable from the terminal where you run Maven.

## Run
1. Open a terminal in the project root.
2. Build and run the `Main` class using Maven:

```bash
mvn clean compile exec:java -Dexec.mainClass="com.bot.Main"
```

When you run the command the program will open Safari and navigate to `https://www.chess.com/play/computer`.
The program includes a prompt that pauses execution so you can set up the game in the browser. Configure the
match (time control, color, etc.) in the Safari window and press ENTER in the terminal to let the bot start.

## Usage Notes
- For 1-minute (bullet) games the bot uses a fast polling frequency and a short engine time (e.g., 500ms).
- Promotions are parsed (e.g. `e7e8q`). The bot attempts to click the promotion UI — if the promotion UI
	isn't recognized on chess.com you may need to inspect the promotion dialog classes and update the selectors
	in `Main.java`.

## Troubleshooting
- `IOException` when starting Stockfish: ensure `stockfish` is installed and in your `PATH`.
- Selenium/Safari issues: enable `Allow Remote Automation` in Safari's `Develop` menu and give Terminal
	Automation permission in System Preferences if prompted.
- If the bot moves at the wrong time, tweak the detection heuristics in `ehMinhaVez` and/or increase the
	polling delay.

## Internals (quick)
- `Main.java` — reads board, builds FEN, polls the page, and issues clicks via Selenium.
- `Engine.java` — starts a `stockfish` process and communicates using UCI commands (`position`, `go movetime`).


