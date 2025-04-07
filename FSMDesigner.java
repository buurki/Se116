import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.*;


public class FSMDesigner {


    public static void main(String[] args) {
        if (args.length > 0) {
            System.out.println("This program does not accept command line arguments.");
            return;
        }
        String versionNo = "v1.0"; // I didn't understand how to combine version numbers from github. It will change.
        System.out.println("FSM DESIGNER " + versionNo + " " + LocalDateTime.now());
        Scanner sc = new Scanner(System.in);
        StringBuilder commandBuilder = new StringBuilder();
        CommandProcessor processor = new CommandProcessor();
        String line;

        while (true) {
            System.out.print("? ");
            line = sc.nextLine().trim();
            if (line.startsWith(";") || line.isEmpty()) { //if line starts with ; or is empty, then it skips
                continue;
            }
            if (line.contains(";")) {
                int semicolonIndex = line.indexOf(';'); //take the line until the ";"
                String commandPart = line.substring(0, semicolonIndex).trim(); //take the line until the ";"
                commandBuilder.append(commandPart).append(" "); //we add it to the commandBuilder
                String fullCommand = commandBuilder.toString().trim();

             if (!fullCommand.isEmpty()) {
                    if (fullCommand.equalsIgnoreCase("EXIT")) {
                        System.out.println("TERMINATED BY USER");
                        break;
                    } else {
                        processor.process(fullCommand); // ← if the line is not EXIT, we send the command to the processor
                    }
                }
              commandBuilder.setLength(0); //reset
            } else {
                commandBuilder.append(line).append(" ");
            }
        }
        sc.close();
    }
}

class CommandProcessor {
    private Set<String> symbols;
    private Set<String> states = new LinkedHashSet<>();
    private String initialState = null;
    private Set<String> finalStates = new LinkedHashSet<>();

    public CommandProcessor() { // constructor
        this.symbols = new LinkedHashSet<>(); // to store in a sorted way
    }

    public void process(String commandLine) {
        String[] parts = commandLine.trim().split("\\s+"); // to split the line in pieces
        if (parts.length == 0)
            return;
        String command = parts[0].toUpperCase(); // command is the first word in the line
        switch (command) {
            case "SYMBOLS":
                handleSymbols(Arrays.copyOfRange(parts, 1, parts.length));
                break;
            case "STATES":
                handleStates(Arrays.copyOfRange(parts, 1, parts.length));
                break;
            case "INITIAL-STATE":
                handleInitialState(Arrays.copyOfRange(parts, 1, parts.length));
                break;
            case "FINAL-STATES":
                handleFinalStates(Arrays.copyOfRange(parts, 1, parts.length));
                break;
            default:
                System.out.println("Warning: unknown command '" + command + "'");
        }
    }

    private void handleSymbols(String[] signs) {
        if (signs.length == 0) {
            if (symbols.isEmpty()) {
                System.out.println("No symbols defined yet.");
            } else {
                System.out.println(String.join(", ", symbols));
            }
            return;
        }
        for (String sign : signs) {
            String symbol = sign.toUpperCase();
            if (!symbol.matches("[A-Z0-9]")) { // only alphanumeric characters, (A-Z,0-9)
                System.out.println("Warning: invalid symbol '" + sign + "' (must be alphanumeric single character)");
            } else if (symbols.contains(symbol)) {
                System.out.println("Warning: symbol '" + sign + "' was already declared");
            } else {
                symbols.add(symbol);
            }
        }
    }

    private void handleStates(String[] stateNames) {
        if (stateNames.length == 0) {
            if (states.isEmpty()) {
                System.out.println("No states defined yet.");
            } else {
                states.forEach(s -> {
                    String stateInfo = s;
                    if (s.equals(initialState)) stateInfo += " (initial)";
                    if (finalStates.contains(s)) stateInfo += " (final)";
                    System.out.println(stateInfo);
                });
            }
            return;
        }
        for (String state : stateNames) {
            String upperState = state.toUpperCase();
            if (!upperState.matches("[A-Z0-9]+")) {
                System.out.println("Warning: invalid state '" + state + "' (must be alphanumeric)");
            } else if (states.contains(upperState)) {
                System.out.println("Warning: state '" + state + "' was already declared");
            } else {
                states.add(upperState);
                if (initialState == null) {
                    initialState = upperState;
                    System.out.println("Initial state automatically set to '" + upperState + "'");
                }
            }
        }
    }

    private void handleInitialState(String[] stateNames) {
        if (stateNames.length != 1) {
            System.out.println("Warning: INITIAL-STATE must be followed by exactly one state name");
            return;
        }
        String state = stateNames[0].toUpperCase();
        if (!state.matches("[A-Z0-9]+")) {
            System.out.println("Warning: invalid initial state '" + state + "' (must be alphanumeric)");
            return;
        }
        if (!states.contains(state)) {
            states.add(state);
            System.out.println("Warning: initial state '" + state + "' not found added automatically");
        }
        initialState = state;
    }

    private void handleFinalStates(String[] stateNames) {
        if (stateNames.length == 0) {
            System.out.println("Warning: FINAL-STATES command requires at least one state name");
            return;
        }
        for (String state : stateNames) {
            String upperState = state.toUpperCase();
            if (!upperState.matches("[A-Z0-9]+")) {
                System.out.println("Warning: invalid final state '" + state + "' (must be alphanumeric)");
                continue;
            }
            if (!states.contains(upperState)) {
                states.add(upperState);
                System.out.println("Warning: final state '" + state + "' not found added automatically");
            }
            if (!finalStates.add(upperState)) {
                System.out.println("Warning: state '" + state + "' is already final state");
            }
        }
    }
}