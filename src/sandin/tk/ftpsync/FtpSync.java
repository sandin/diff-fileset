package sandin.tk.ftpsync;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * @author lds
 */
public class FtpSync {
    static String usage = "java -jar git-diff.jar [Options]... File \n"
            + "File created by : git diff master^  > change.diff";

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("dir", true, "git repository root dir path");
        options.addOption("output", true, "output dir path");
        options.addOption("help", false, "get help");
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLineParser parser = new PosixParser();
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("help")) {
                formatter.printHelp(usage, options);
            }

            if (line.hasOption("dir")) {
                String filename = args[args.length - 1];
                String root = line.getOptionValue("dir");
                String output = line.getOptionValue("output", "copy");

                File file = new File(filename);
                if (!file.exists()) {
                    System.out.println(filename + " is not exists.");
                    System.exit(1);
                }
                File dir = new File(root);
                if (!dir.exists() || !dir.isDirectory()) {
                    System.out.println(dir
                            + " is not exists or not a directory.");
                    System.exit(1);
                }

                GitDiff gitDiff = new GitDiff(file, dir, output);
                boolean result = gitDiff.start();
                if (result) {
                    System.out.println("Success.");
                } else {
                    System.out.println("Fail.");
                }
            } else {
                formatter.printHelp(usage, options);
            }
        } catch (ParseException exp) {
            System.out.println(exp.getMessage());
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }
}