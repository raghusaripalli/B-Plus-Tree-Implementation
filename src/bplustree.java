import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static java.lang.System.exit;

/**
 * bplustree driver class
 * reads input from the file and executes commands on B+ tree
 *
 * @author Raghuveer Sharma Saripalli
 */

public class bplustree {

    final static String NEWLINE = "\n";
    final static String COMMA = ",";

    private static String parseMode(String line) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (Character.isLetter(c))
                sb.append(c);
        }
        return sb.toString();
    }

    private static double[] parseNumbers(String line) {
        double[] res = new double[2];
        res[1] = Double.MIN_VALUE;
        String data = line.substring(line.indexOf('(') + 1, line.lastIndexOf(')'));
        String[] values = data.split(",");
        res[0] = Integer.parseInt(values[0].trim());
        if (values.length == 2)
            res[1] = Double.parseDouble(values[1].trim());
        return res;
    }

    public static void main(String[] args) {

        // Terminate program if input file argument is missing
        if (args.length != 1) {
            System.err.println("Incorrect number of program arguments entered! Required only one. Terminating program.");
            exit(-1);
        }

        // Display filename argument entered
        String filePath = args[0];
        System.out.println("Filename provided: " + args[0]);

        // try with resources block
        // opened file pointers and scanners will be closed automatically(even when there's an exception)
        try (FileInputStream fis = new FileInputStream(filePath);
             FileWriter fw = new FileWriter("output_file.txt", false);
             Scanner scanner = new Scanner(fis, StandardCharsets.UTF_8)) {
            Tree tree = new Tree(1);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String mode = parseMode(line);
                System.out.println(mode);
                double[] values = parseNumbers(line);
                System.out.println(values[0] + ", " + values[1]);
                switch (mode) {
                    case "Initialize":
                        tree = new Tree((int) values[0]);
                        break;
                    case "Insert":
                        tree.insert((int) values[0], values[1]);
                        break;
                    case "Delete":
                        tree.delete((int) values[0]);
                        break;
                    case "Search":
                        if (values[1] == Double.MIN_VALUE) {
                            Double ans = tree.search((int) values[0]);
                            fw.write((ans == null ? "Null" : ans.toString()) + NEWLINE);
                        } else {
                            List<Double> answers = tree.searchRange((int) values[0], (int) values[1]);
                            List<String> res = answers.stream()
                                    .map(ans -> ans == null ? "Null" : ans.toString())
                                    .collect(Collectors.toList());
                            if (res.size()==0)
                                fw.write("Null");
                            else
                                fw.write(String.join(COMMA, res) + NEWLINE);
                        }
                        break;
                    default:
                        System.err.println("Wrong B+ tree operation:\n\t" + line);
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            exit(-1);
        }
        System.out.println("result is written to the file 'output_file.txt'");
    }
}
