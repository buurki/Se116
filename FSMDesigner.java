import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.*;
import java.io.*; // Added for file handling

public class FSMDesigner {
    public static void main(String[] args) {
        // Check for command line arguments
        if (args.length > 0) {
            System.out.println("This program does not accept command line arguments.");
            return;
        }

        // Initialize program with version and timestamp
        String versionNo = "v1.0";
        System.out.println("FSM DESIGNER " + versionNo + " " + LocalDateTime.now());

        // Set up input scanner and command processor
        Scanner sc = new Scanner(System.in);
        StringBuilder commandBuilder = new StringBuilder();
        CommandProcessor processor = new CommandProcessor();

        // Main command processing loop
        while (true) {
            System.out.print("? ");
            String line = sc.nextLine().trim();

            // Skip comments and empty lines
            if (line.startsWith(";") || line.isEmpty()) {
                continue;
            }
            // Check if this line ends a command
            boolean hasSemicolon = line.contains(";");
            boolean isTransitionsStart = commandBuilder.length() == 0
                    && line.toUpperCase().startsWith("TRANSITIONS");

            // Handle lines without a semicolon
            if (!hasSemicolon) {
                if (commandBuilder.length() > 0 || isTransitionsStart) {
                    // Continue building a multi-line TRANSITIONS block
                    commandBuilder.append(line).append(" ");
                } else {
                    // Single-line commands must end with ';'
                    System.out.println("Error: Semicolon expected");
                }
                continue;
            }

            // Extract command up to semicolon
            int index = line.indexOf(';');
            String part = line.substring(0, index).trim();
            commandBuilder.append(part).append(" ");
            String fullCommand = commandBuilder.toString().trim();
            commandBuilder.setLength(0);  // Reset builder for next command

            // Exit condition
            if (fullCommand.equalsIgnoreCase("EXIT")) {
                System.out.println("TERMINATED BY USER");
                break;
            }
            processor.process(fullCommand);
        }

        sc.close();
    }
}

class CommandProcessor implements Serializable {
    // FSM components
    private Set<String> symbols;
    private Set<String> states = new LinkedHashSet<>();
    private String initialState = null;
    private Set<String> finalStates = new LinkedHashSet<>();
    private Map<String, Map<String, String>> transitions = new LinkedHashMap<>();
    private transient PrintWriter logWriter=null; // Added for logging

    public CommandProcessor() { // constructor
        this.symbols = new LinkedHashSet<>(); // to store in a sorted way
    }

    /*
     Processes the given command line input
     The full command string to process*/

    public void process(String commandLine) {
        String[] parts = commandLine.trim().split("\\s+"); // to split the line in pieces
        if (parts.length == 0)
            return;

        // Extract and uppercase the first word as the command
        String command = parts[0].toUpperCase(); // command is the first word in the line

        // Route to appropriate handler
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
            case "TRANSITION":
                handleTransition(Arrays.copyOfRange(parts, 1, parts.length));
                break;
            case "TRANSITIONS":
                handleTransitions(Arrays.copyOfRange(parts, 1, parts.length));
                break;
            case "DELETE":
                handleDelete(Arrays.copyOfRange(parts, 1, parts.length));
                break;
            case "PRINT":
                handlePrint();
                break;
            case "EXECUTE":
                handleExecute(Arrays.copyOfRange(parts, 1, parts.length));
                break;
            case "LOG": // Added for logging functionality
                handleLog(Arrays.copyOfRange(parts, 1, parts.length));
                break;
            case "COMPILE": // Added for saving FSM to a file
                handleCompile(Arrays.copyOfRange(parts, 1, parts.length));
                break;
            case "LOAD": // Added for loading FSM from a file
                handleLoad(Arrays.copyOfRange(parts, 1, parts.length));
                break;
            case "CLEAR":
                handleClear();
                break;
            default:
                System.out.println("Warning: unknown command '" + command + "'");
        }
    }

    /*
     Handles SYMBOLS command - manages FSM alphabet symbols
     Array of symbols to add (empty for listing)*/

    private void handleSymbols(String[] signs) {
        if (signs.length == 0) {
            // List current symbols
            if (symbols.isEmpty()) {
                System.out.println("No symbols defined yet.");
            } else {
                System.out.println(String.join(", ", symbols));
            }
            return;
        }

        // Process each symbol
        for (String sign : signs) {
            String symbol = sign.toUpperCase();
            // Validate symbol format
            if (!symbol.matches("[A-Z0-9]")) { // only alphanumeric characters, (A-Z,0-9)
                System.out.println("Warning: invalid symbol '" + sign + "' (must be alphanumeric single character)");
            } else if (symbols.contains(symbol)) {
                System.out.println("Warning: symbol '" + sign + "' was already declared");
            } else {
                symbols.add(symbol);
            }
        }
    }

    /*
     Handles STATES command - manages FSM states
     Array of state names to add (empty for listing)*/

    private void handleStates(String[] stateNames) {
        if (stateNames.length == 0) {
            // List current states with their properties
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

        // Process each state
        for (String state : stateNames) {
            String upperState = state.toUpperCase();
            // Validate state name
            if (!upperState.matches("[A-Z0-9]+")) {
                System.out.println("Warning: invalid state '" + state + "' (must be alphanumeric)");
            } else if (states.contains(upperState)) {
                System.out.println("Warning: state '" + state + "' was already declared");
            } else {
                states.add(upperState);
                // Set first state as initial if none exists
                if (initialState == null) {
                    initialState = upperState;
                    System.out.println("Initial state automatically set to '" + upperState + "'");
                }
            }
        }
    }

    /*
     Handles INITIAL-STATE command - sets the initial state
     Array containing single state name*/

    private void handleInitialState(String[] stateNames) {
        // Validate input
        if (stateNames.length != 1) {
            System.out.println("Warning: INITIAL-STATE must be followed by exactly one state name");
            return;
        }

        String state = stateNames[0].toUpperCase();
        // Validate state name format
        if (!state.matches("[A-Z0-9]+")) {
            System.out.println("Warning: invalid initial state '" + state + "' (must be alphanumeric)");
            return;
        }

        // Add state if not exists
        if (!states.contains(state)) {
            states.add(state);
            System.out.println("Warning: initial state '" + state + "' not found added automatically");
        }

        // Set initial state
        initialState = state;
    }

    /*
     Handles FINAL-STATES command - manages accepting states
     Array of state names to mark as final*/

    private void handleFinalStates(String[] stateNames) {
        // Validate input
        if (stateNames.length == 0) {
            System.out.println("Warning: FINAL-STATES command requires at least one state name");
            return;
        }

        // Process each state
        for (String state : stateNames) {
            String upperState = state.toUpperCase();
            // Validate state name
            if (!upperState.matches("[A-Z0-9]+")) {
                System.out.println("Warning: invalid final state '" + state + "' (must be alphanumeric)");
                continue;
            }

            // Add state if not exists
            if (!states.contains(upperState)) {
                states.add(upperState);
                System.out.println("Warning: final state '" + state + "' not found added automatically");
            }

            // Add to final states if not already
            if (!finalStates.add(upperState)) {
                System.out.println("Warning: state '" + state + "' is already final state");
            }
        }
    }

    /*
     Handles single TRANSITION command
     Array containing [fromState, symbol, toState]*/

    private void handleTransition(String[] parts) {
        // Validate input
        if (parts.length != 3) {
            System.out.println("Warning: TRANSITION requires 3 arguments (fromState symbol toState)");
            return;
        }

        // Extract and normalize transition components
        String from = parts[0].toUpperCase();
        String symbol = parts[1].toUpperCase();
        String to = parts[2].toUpperCase();

        // Handle missing states/symbols
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

        // Initialize transition map if needed
        transitions.putIfAbsent(from, new LinkedHashMap<>());

        // Handle duplicate transitions
        if (transitions.get(from).containsKey(symbol)) {
            System.out.println("Warning: transition from '" + from + "' with symbol '" + symbol + "' already exists, overwritten");
        }

        // Add the transition
        transitions.get(from).put(symbol, to);
    }

    /*
     Handles multiple TRANSITIONS command
     Array containing comma-separated transitions*/

    private void handleTransitions(String[] parts) {
        // Combine parts and split by commas
        String joined = String.join(" ", parts);
        String[] entries = joined.split(",");

        // Process each transition
        for (String e : entries) {
            String[] p = e.trim().split("\\s+");
            // Validate transition format
            if (p.length != 3) {
                System.out.println("Error: TRANSITIONS entries must be '<symbol> <from> <to>'");
                continue;
            }

            // Extract and normalize components
            String sym = p[0].toUpperCase();
            String from = p[1].toUpperCase();
            String to = p[2].toUpperCase();

            // Validate components
            if (!symbols.contains(sym)) {
                System.out.println("Error: invalid symbol '" + sym + "'");
                continue;
            }
            if (!states.contains(from)) {
                System.out.println("Error: invalid state '" + from + "'");
                continue;
            }
            if (!states.contains(to)) {
                System.out.println("Error: invalid state '" + to + "'");
                continue;
            }

            // Add the transition
            transitions.putIfAbsent(from, new LinkedHashMap<>());
            transitions.get(from).put(sym, to);
        }
    }

    /*
     Clears the entire FSM definition
     */
    public void handleClear() {
        symbols.clear();
        states.clear();
        initialState = null;
        finalStates.clear();
        transitions.clear();
        System.out.println("FSM cleared");
    }

    /*
     Handles DELETE command - removes states or symbols
     Array containing [type, name]*/
    private void handleDelete(String[] parts) {
        // Validate input
        if (parts.length < 2) {
            System.out.println("Warning: DELETE command requires type and name");
            return;
        }

        // Extract components
        String type = parts[0].toUpperCase();
        String name = parts[1].toUpperCase();

        // Process deletion by type
        switch (type) {
            case "STATE":
                if (states.remove(name)) {
                    // Clean up related state references
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
                    // Remove all transitions using this symbol
                    for (Map<String, String> map : transitions.values()) {
                        map.remove(name);
                    }
                    System.out.println("Symbol '" + name + "' deleted.");
                } else {
                    System.out.println("Warning: symbol '" + name + "' not found.");
                }
                break;

            default:
                System.out.println("Warning: unknown DELETE type '" + type + "'");
        }
    }

    /*
     Prints the current FSM definition
     */
    private void handlePrint() {
        // Print states with their properties
        System.out.println("States:");
        for (String s : states) {
            String info = s; // start with the state name
            if (s.equals(initialState)) info += " (initial)"; //mark the inital state
            if (finalStates.contains(s)) info += " (final)"; //mark the final state
            System.out.println(" - " + info);  //print the info
        }

        // Print symbol alphabet
        System.out.println("Symbols:");
        System.out.println(" - " + String.join(", ", symbols));

        // Print all transitions
        System.out.println("Transitions:");
        for (String from : transitions.keySet()) {
            for (String transitionSymbol : transitions.get(from).keySet()) {
                String to = transitions.get(from).get(transitionSymbol); //get the destination state for this transition
                System.out.println(" - " + from + " -" + transitionSymbol + "-> " + to);
            }
        }
    }

    /*
     Executes the FSM on an input string
     Array containing the input string*/

    private void handleExecute(String[] parts) {
        // Validate input
        if (parts.length != 1) {
            System.out.println("Error: EXECUTE requires exactly one input string");
            return;
        }
        String input = parts[0].toUpperCase();

        try {
            // Validate input format
            if (!input.matches("[A-Z0-9]+")) {
                System.out.println("Error: input must be alphanumeric (A–Z, 0–9 only)");
                return;
            }

            // Check initial state exists
            if (initialState == null) {
                System.out.println("Error: no initial state defined");
                return;
            }

            // Validate all symbols in input
            for (char c : input.toCharArray()) {
                String sym = String.valueOf(c);
                if (!symbols.contains(sym)) {
                    System.out.println("Error: symbol '" + sym + "' not recognized");
                    return;
                }
            }

            // Simulate FSM execution
            List<String> path = new ArrayList<>();
            String current = initialState;
            path.add(current);

            // Process each symbol
            for (char c : input.toCharArray()) {
                Map<String, String> map = transitions.get(current);
                // Check for undefined transition
                if (map == null || !map.containsKey(String.valueOf(c))) {
                    System.out.println("NO");
                    return;
                }
                // Follow transition
                current = map.get(String.valueOf(c));
                path.add(current);
            }

            // Output state sequence
            System.out.println(String.join(" ", path));

            // Output acceptance result
            System.out.println(finalStates.contains(current) ? "YES" : "NO");

        } catch (Exception e) {
            System.out.println("Error: unexpected exception during EXECUTE – " + e.getMessage());
        }
    }

    /*
      Handles LOG command - manages logging to file
      Array containing filename (empty to stop logging)*/
    private void handleLog(String[] parts) {
        if (parts.length == 0) {
            // Stop logging if active
            if (logWriter != null) {
                logWriter.close();
                logWriter = null;
                System.out.println("STOPPED LOGGING");
            } else {
                System.out.println("LOGGING was not enabled");
            }
            return;
        }

        // Start new log file
        String filename = parts[0];
        try {
            // Close existing log if open
            if (logWriter != null) {
                logWriter.close();
            }
            // Create new log file
            logWriter = new PrintWriter(new FileWriter(filename, false));
            System.out.println("LOGGING to " + filename);
        } catch (IOException e) {
            System.out.println("Error Unable to create log file " + filename);
        }
    }

    /*
     Handles COMPILE command - saves FSM to binary file
     Array containing filename*/
    private void handleCompile(String[] parts) {
        // Validate input
        if (parts.length != 1) {
            System.out.println("Warning COMPILE requires exactly one filename");
            return;
        }

        // Serialize FSM to file
        String filename = parts[0];
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this);
            System.out.println("FSM saved to file " + filename);
        } catch (IOException e) {
            System.out.println("Error Unable to save FSM to file '" + filename + "'");
        }
    }

    /*
    Handles LOAD command - loads FSM from file
    Array containing filename*/

    void handleLoad(String[] parts) {
        // Validate input
        if (parts.length != 1) {
            System.out.println("Warning LOAD requires exactly one filename");
            return;
        }

        // Deserialize FSM from file
        String filename = parts[0];
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            CommandProcessor loadedFSM = (CommandProcessor) ois.readObject();
            // Copy loaded FSM state
            this.symbols = loadedFSM.symbols;
            this.states = loadedFSM.states;
            this.initialState = loadedFSM.initialState;
            this.finalStates = loadedFSM.finalStates;
            this.transitions = loadedFSM.transitions;
            System.out.println("FSM loaded from file: " + filename);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error: Unable to load FSM from file '" + filename + "': " + e);
        }
    }
}
