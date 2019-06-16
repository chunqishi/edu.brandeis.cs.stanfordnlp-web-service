package edu.brandeis.lapps.corenlp;

import edu.brandeis.lapps.BrandeisService;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractCoreNLPWebService extends BrandeisService {

    private static final Logger log = LoggerFactory
            .getLogger(AbstractCoreNLPWebService.class);

    private static ConcurrentHashMap<String, StanfordCoreNLP> cache =
            new ConcurrentHashMap<>();

    static final String PROP_TOKENIZE = "tokenize";
    static final String PROP_SENTENCE_SPLIT = "ssplit";
    static final String PROP_POS_TAG = "pos";
    static final String PROP_LEMMA = "lemma";
    static final String PROP_NER = "ner";
    static final String PROP_PARSE = "parse";
    static final String PROP_CORERENCE = "dcoref";
    static final String PROP_KEY = "annotators";

    private Properties props = new Properties();
    StanfordCoreNLP snlp = null;

    AbstractCoreNLPWebService() {
        super();
    }

    /**
     * Initiate stanford NLP
     */
    void init(String... tools) {
        props.clear();
        String toolList;
        if (tools.length > 1) {
            StringBuilder sb = new StringBuilder();
            for(String tool: tools) {
                sb.append(tool).append(" ");
            }
            toolList = sb.toString();
        } else {
            toolList = tools[0];
        }
        props.put(PROP_KEY, toolList);
        snlp = getProcessor(props);
    }

    private StanfordCoreNLP getProcessor(Properties props) {
        String key = props.getProperty(PROP_KEY);
        log.info(String.format("Retrieving from cache: %s", key));
        StanfordCoreNLP val = cache.get(key);
        if (val == null) {
            val = new StanfordCoreNLP(props);
            cache.put(key, val);
            log.info(String.format("No cached found, newly cached: %s", key));
        }
        return val;
    }

    protected ServiceMetadata setDefaultMetadata() {
        ServiceMetadata metadata = super.setDefaultMetadata();
        metadata.setLicense(Discriminators.Uri.GPL3);
        metadata.setLicenseDesc("This service provides an interface to a Stanford CoreNLP tool, which is originally licensed under GPLv3. For more information, please visit `the official CoreNLP website <https://stanfordnlp.github.io/CoreNLP/#license>`_. ");
        IOSpecification required = new IOSpecification();
        required.addLanguage("en");
        required.setEncoding("UTF-8");
        required.addFormat(Discriminators.Uri.TEXT);
        required.addFormat(Discriminators.Uri.LIF);
        metadata.setRequires(required);

        IOSpecification produces = new IOSpecification();
        produces.addLanguage("en");
        produces.setEncoding("UTF-8");
        produces.addFormat(Discriminators.Uri.LIF);
        metadata.setProduces(produces);

        return metadata;

    }
}

