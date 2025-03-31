import java.time.LocalDateTime;
import java.util.Scanner;

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
        String line;

        while (true) {
            System.out.print("? ");
            line = sc.nextLine();
            String trimmedLine = line.trim();

            if (trimmedLine.startsWith(";")) {
                continue;
            }

            if (trimmedLine.contains(";")) {
                int semicolonIndex = trimmedLine.indexOf(";");
                String commandPart = trimmedLine.substring(0, semicolonIndex).trim();
                commandBuilder.append(commandPart).append(" ");

                String command = commandBuilder.toString().trim();
                if (!command.isEmpty()) {
                    System.out.println("Processing command: " + command);

                    if (command.equalsIgnoreCase("EXIT")) {
                        System.out.println("TERMINATED BY USER");
                        break;
                    }
                }
                commandBuilder.setLength(0);
            } else {
                commandBuilder.append(trimmedLine).append(" ");
            }
        }

    }
}
