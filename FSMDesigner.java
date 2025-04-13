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
    private Map<String, Map<String, String>> transitions = new LinkedHashMap<>();

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
    private void handleTransition(String[] parts) {
        if (parts.length != 3) {
            System.out.println("Warning: TRANSITION requires 3 arguments (fromState symbol toState)");
            return;
        }

        String from = parts[0].toUpperCase();
        String symbol = parts[1].toUpperCase();
        String to = parts[2].toUpperCase();

        if (!states.contains(from)) {
            System.out.println("Warning: source state '" + from + "' not found, added automatically");
            states.add(from);
        }

        if (!states.contains(to)) {
            System.out.println("Warning: destination state '" + to + "' not found, added automatically");
            states.add(to);
        }

        if (!symbols.contains(symbol)) {
            System.out.println("Warning: symbol '" + symbol + "' not found, added automatically");
            symbols.add(symbol);
        }

        transitions.putIfAbsent(from, new LinkedHashMap<>());

        if (transitions.get(from).containsKey(symbol)) {
            System.out.println("Warning: transition from '" + from + "' with symbol '" + symbol + "' already exists, overwritten");
        }

        transitions.get(from).put(symbol, to);
    }

    private void handleDelete(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Warning: DELETE command requires type and name");
            return;
        }

        String type = parts[0].toUpperCase();
        String name = parts[1].toUpperCase();

        switch (type) {
            case "STATE":
                if (states.remove(name)) {
                    if (name.equals(initialState)) initialState = null;
                    finalStates.remove(name);
                    transitions.remove(name);
                    for (Map<String, String> map : transitions.values()) {
                        map.values().removeIf(val -> val.equals(name));
                    }
                    System.out.println("State '" + name + "' deleted.");
                } else {
                    System.out.println("Warning: state '" + name + "' not found.");
                }
                break;

            case "SYMBOL":
                if (symbols.remove(name)) {
                    for (Map<String, String> map : transitions.values()) {
                        map.remove(name);
                    }
                    System.out.println("Symbol '" + name + "' deleted.");
                } else {
                    System.out.println("Warning: symbol '" + name + "' not found.");
                }
                break;

            case "TRANSITIONS":
                String joined = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
                String[] transitionsArray = joined.split(",");
                for (String t : transitionsArray) {
                    String[] oneTransitionParts = t.trim().split("\\s+");
                    handleTransition(oneTransitionParts);
                }
                break;
            case "DELETE":
                handleDelete(Arrays.copyOfRange(parts, 1, parts.length));
                break;

            case "PRINT":
                handlePrint();
                break;

            case "SIMULATE":
                handleSimulate(Arrays.copyOfRange(parts, 1, parts.length));
                break;

            default:
                System.out.println("Warning: unknown DELETE type '" + type + "'");
        }
    }
    private void handlePrint() {
        System.out.println("States:");
        for (String s : states) {
            String info = s;
            if (s.equals(initialState)) info += " (initial)";
            if (finalStates.contains(s)) info += " (final)";
            System.out.println(" - " + info);
        }

        System.out.println("Symbols:");
        System.out.println(" - " + String.join(", ", symbols));

        System.out.println("Transitions:");
        for (String from : transitions.keySet()) {
            for (String sym : transitions.get(from).keySet()) {
                String to = transitions.get(from).get(sym);
                System.out.println(" - " + from + " -" + sym + "-> " + to);
            }
        }
    }

    private void handleSimulate(String[] parts) {
        if (parts.length != 1) {
            System.out.println("Warning: SIMULATE requires a single input string");
            return;
        }

        String input = parts[0].toUpperCase();
        if (initialState == null) {
            System.out.println("Simulation failed: No initial state defined.");
            return;
        }

        String current = initialState;
        System.out.println("Simulation path:");

        for (char c : input.toCharArray()) {
            String symbol = String.valueOf(c);
            System.out.print(current + " --" + symbol + "--> ");
            if (transitions.containsKey(current) && transitions.get(current).containsKey(symbol)) {
                current = transitions.get(current).get(symbol);
            } else {
                System.out.println("ERROR");
                System.out.println("Simulation failed: No transition for symbol '" + symbol + "'");
                return;
            }
        }
        System.out.println(current);
        if (finalStates.contains(current)) {
            System.out.println("Input accepted. Final state reached.");
        } else {
            System.out.println("Input rejected. Final state not reached.");
        }
    }

}