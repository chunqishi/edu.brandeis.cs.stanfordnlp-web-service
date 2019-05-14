package edu.brandeis.lapps.stanford;

import edu.brandeis.lapps.stanford.corenlp.*;
import edu.brandeis.lapps.stanford.corenlp.Parser;

import java.io.*;
import java.lang.reflect.InvocationTargetException;

/**
 * A Class to provide command line interface to run a specific tool in this
 * LIF-backed coreNLP suite. User can pick the tool to run by a full java class
 * name of the tool or by a three-letter alias. As all tools in the suite are
 * standalone annotators (that is, don't need existing annotations / views),
 * the input always be a LIF with primary text at minimum passed as a file or
 * via STDIN, and output will be a LIF with an additional view into STDOUT.
 */
public class Cli {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Class<? extends AbstractStanfordCoreNLPWebService> toolClass;
        InputStream is;
        if (args.length < 1) {
            help();
        } else {
            toolClass = pickToolClass(args[0]);
            if (args.length == 1 || args[1].equals("-")) {
                is = System.in;
            } else {
                is = new FileInputStream(args[1]);
            }
            try {
                AbstractStanfordCoreNLPWebService tool = toolClass.getConstructor().newInstance();
                ByteArrayOutputStream toString = new ByteArrayOutputStream();
                byte[] buffer = new byte[10240];
                int length;
                while ((length = is.read(buffer)) != -1) {
                    toString.write(buffer, 0, length);
                }
                String inString =  toString.toString("UTF-8");
                System.out.println(tool.execute(inString));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException ignored) {
                // will not happen
            }
        }
    }

    private static Class<? extends AbstractStanfordCoreNLPWebService> pickToolClass(String arg) throws ClassNotFoundException {
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
                    arg = packageName + ".corenlp." + arg;
                }
                return (Class<? extends AbstractStanfordCoreNLPWebService>) Class.forName(arg);
        }
    }

    private static void help() {
        System.out.println("Usage: One required argument to specify NLP annotator," +
                "\n       and another optional argument to specify input LIF file are passable." +
                "\n       If an input file is not given, it will read from STDIN. " +
                "\n       Output will be written to STDOUT so can be piped to any desired place. "
        );
    }

}
