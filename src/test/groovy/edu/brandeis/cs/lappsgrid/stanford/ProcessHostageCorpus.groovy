package edu.brandeis.cs.lappsgrid.stanford

import edu.brandeis.cs.lappsgrid.stanford.corenlp.NamedEntityRecognizer
import edu.brandeis.cs.lappsgrid.stanford.corenlp.POSTagger
import edu.brandeis.cs.lappsgrid.stanford.corenlp.Splitter
import edu.brandeis.cs.lappsgrid.stanford.corenlp.Tokenizer
import org.lappsgrid.api.WebService
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.Serializer

import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Created by krim on 8/5/2016.
 */

class ProcessHostageCorpus {
    private static final PATH = "c:/Users/krim/Projects/lappsgrid/!discovery/corpora/kidnap2"
    private final packPath

    public ProcessHostageCorpus() {
        packPath = PATH + '/' + this.getClass().getPackage().getName().split('\\.')[-1]
        println packPath
    }

    public void run() {
        File directory = new File(PATH)
        List<File> list = directory.listFiles(new Filter())
        ConcurrentLinkedQueue<File> files = new ConcurrentLinkedQueue<>(list)
        ConcurrentLinkedQueue<Packet> loaded = new ConcurrentLinkedQueue<>()
        ConcurrentLinkedQueue<Packet> tokenized = new ConcurrentLinkedQueue<>()
        ConcurrentLinkedQueue<Packet> split = new ConcurrentLinkedQueue<>()
        ConcurrentLinkedQueue<Packet> tagged = new ConcurrentLinkedQueue<>()
//        ConcurrentLinkedQueue<Packet> neq = new ConcurrentLinkedQueue<>()
//        ConcurrentLinkedQueue<Packet> converted = new ConcurrentLinkedQueue<>()

        Thread reader = new DataReader(files, loaded)
        Thread tokenizer = new TokenizerWorker(loaded, tokenized)
        Thread splitter = new SplitterWorker(tokenized, split)
        Thread tagger = new TaggerWorker(split, tagged)
//        Thread recognizer = new NameEnitityRecognizerWorker(tagged, neq)
//        Thread converter = new ConverterWorker(tagged, converted)
        Thread writer = new DataWriter(packPath, tagged)

//        List threads = [ reader, tokenizer, splitter, tagger, recognizer, converter, writer ]
//        List threads = [ reader, tokenizer, splitter, tagger, converter, writer]
        List threads = [ reader, tokenizer, splitter, tagger, writer]

        threads*.start()
        threads*.join()
    }

    static void main(String[] args) {
        new ProcessHostageCorpus().run()
    }
}


class DataReader extends Thread {
    ConcurrentLinkedQueue<File> files;
    ConcurrentLinkedQueue<Packet> packets

    public DataReader(ConcurrentLinkedQueue<File> files, ConcurrentLinkedQueue<Packet> packets) {
        this.files = files
        this.packets = packets
    }

    void run() {
        File file = files.poll();
        while (file) {
            println "Reading ${file.path}"
            Packet packet = new Packet()
            packet.filename = file.name
            packet.data = Serializer.parse(file.text, Data)
            packets.add(packet)
            file = files.poll()
        }
        packets.add(Packet.POISON)
        println "Done loading files."
    }
}

class Filter implements FilenameFilter {

    /**
     * Tests if a specified file should be included in a file list.
     *
     * @param dir the directory in which the file was found.
     * @param name the name of the file.
     * @return <code>true</code> if and only if the name should be
     * included in the file list; <code>false</code> otherwise.
     */
    @Override
    boolean accept(File dir, String name) {
        return name.endsWith(".lif")
    }
}

abstract class Worker extends Thread {
    WebService worker
    ConcurrentLinkedQueue<Packet> source
    ConcurrentLinkedQueue<Packet> sink

    public Worker(WebService worker, ConcurrentLinkedQueue<Packet> input, ConcurrentLinkedQueue<Packet> output) {
        this.worker = worker
        source = input
        sink = output
    }

    abstract String name();

    public void run() {
        println "starting ${name()}"
        Packet packet = source.poll();
        while (packet != Packet.POISON) {
            if (packet == null) {
                sleep(1000)
            }
            else {
                println "${name()} : ${packet.filename}"
                String json = worker.execute(packet.data.asJson())
                packet.data = Serializer.parse(json, Data)
                sink.add(packet)
            }
            packet = source.poll()
        }
        sink.add(packet)
        println "Terminating ${name()}"
    }
}

class TokenizerWorker extends Worker {

    public TokenizerWorker(ConcurrentLinkedQueue<Packet> input, ConcurrentLinkedQueue<Packet> output) {
        super(new Tokenizer(), input, output)
    }

    String name() { 'tokenizer' }
}

class SplitterWorker extends Worker {
    SplitterWorker(ConcurrentLinkedQueue<Packet> input, ConcurrentLinkedQueue<Packet> output) {
        super(new Splitter(), input, output)
    }
    String name() { 'splitter' }
}

class TaggerWorker extends Worker {
    public TaggerWorker(ConcurrentLinkedQueue<Packet> input, ConcurrentLinkedQueue<Packet> output) {
        super(new POSTagger(), input, output)
    }

    String name() { 'tagger' }
}

class NameEnitityRecognizerWorker extends Worker {
    NameEnitityRecognizerWorker(ConcurrentLinkedQueue<Packet> input, ConcurrentLinkedQueue<Packet> output) {
        super(new NamedEntityRecognizer(), input, output)
        this.name = "ner"
    }

    String name() { 'recognizer' }
}

/* krim - no need for gate conversion
class ConverterWorker extends Worker {
    ConverterWorker(ConcurrentLinkedQueue<Packet> input, ConcurrentLinkedQueue<Packet> output) {
        super(new GateToJsonConverter(), input, output)
    }

    String name() { 'converter' }
}
*/

class DataWriter extends Thread {
    File destination
    ConcurrentLinkedQueue<Packet> queue

    DataWriter(String path, ConcurrentLinkedQueue<Packet> queue) {
        this(new File(path), queue)
    }
    DataWriter(File destination, ConcurrentLinkedQueue<Packet> queue) {
        this.destination = destination
        this.queue = queue
        if (!destination.exists()) {
            if (!destination.mkdirs()) {
                throw new IOException("Unable to create output directory: ${destination.path}")
            }
        }
    }

    private String name() {
        return 'file-writer'
    }

    public void run() {
        println "starting ${name()}"
        Packet packet = queue.poll()
        while (packet != Packet.POISON) {
            if (!packet) {
                sleep(1000)
            }
            else {
                File file = new File(destination, packet.filename)
                println "Writing ${file.path}"
                file.text = packet.data.asPrettyJson()
            }
            packet = queue.poll()
        }
        println "Finished writing files."
    }
}

class Packet {
    // POISON is the last packet sent though the pipeline.  All threads exit when they
    // see the poison pill.
    static final Packet POISON = new Packet()
    String filename
    Data data

    Packet() { }
    Packet(String name, Data data) {
        this.filename = name
        this.data = data
    }
}
