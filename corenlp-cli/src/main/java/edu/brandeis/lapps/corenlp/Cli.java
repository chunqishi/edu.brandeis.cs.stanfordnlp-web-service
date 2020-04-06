package edu.brandeis.lapps.corenlp;

import edu.brandeis.lapps.CliUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A Class to provide command line interface to run a specific tool in this
 * LIF-backed coreNLP suite. User can pick the tool to run by a full java class
 * name of the tool or by a three-letter alias. As all tools in the suite are
 * standalone annotators (that is, don't need existing annotations / views),
 * the input always be a LIF with primary text at minimum passed as a file or
 * via STDIN, and output will be a LIF with an additional view into STDOUT.
 */
public class Cli {

    private static Class<? extends AbstractCorenlpWrapper> toolClass;

    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (args.length < 1) {
            help();
        } else {
            toolClass = pickToolClass(args[0]);
            AbstractCorenlpWrapper tool = toolClass.getConstructor().newInstance();
            if (args.length == 1 || args[1].equals("-")) {
                System.out.println(CliUtil.processInputStream(tool, System.in));
            } else {
                Path inputPath = Paths.get(args[1]);
                if (Files.exists(inputPath) && Files.isDirectory(inputPath)) {
                    CliUtil.processDirectory(tool, inputPath, args[0]);
                } else {
                    System.out.println(CliUtil.processInputStream(tool, new FileInputStream(inputPath.toFile())));
                }
            }
        }
    }

    private static Class<? extends AbstractCorenlpWrapper> pickToolClass(String arg) throws ClassNotFoundException {
        switch (arg) {
            case "tok":
                return Tokenizer.class;
            case "spl":
                return Splitter.class;
            case "pos":
                return POSTagger.class;
            case "ner":
                return NamedEntityRecognizer.class;
            case "cor":
                return Coreference.class;
            case "dep":
                return DependencyParser.class;
            case "par":
                return Parser.class;
            default:
                String fullname = Cli.class.getCanonicalName();
                String packageName = fullname.substring(0, fullname.lastIndexOf("."));
                if (!arg.startsWith(packageName)) {
                    arg = packageName + arg;
                }
                return (Class<? extends AbstractCorenlpWrapper>) Class.forName(arg);
        }
    }

    private static void help() {
        System.out.println("Usage: java -jar jar [tok|spl|pos|ner|cor|dep|par] (input)" +
                "\n" +
                "\n       First argument to specify NLP annotator is required. " +
                "\n       Second and optional argument to specify input." +
                "\n       If the input is a file, annotated output will be written to " +
                "\n         STDOUT and can be piped to any other processing. " +
                "\n       If the input is a directory, a new timestamped-subdirectory will be created " +
                "\n         named after the annotator, and *.lif (not starting with a dot) " +
                "\n         files in the original directory will be annotated and " +
                "\n         the results are written in the subdirectory." +
                "\n       NOTE that when input is a directory, the program does NOT recursively " +
                "\n         search for .lif files. It only looks for the input directory." +
                "\n       And finally, the input is not given, STDIN will be read in. "
        );
    }

}
