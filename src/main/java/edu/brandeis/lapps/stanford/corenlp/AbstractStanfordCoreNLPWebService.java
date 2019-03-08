package edu.brandeis.lapps.stanford.corenlp;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.lappsgrid.api.WebService;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.Contains;
import org.lappsgrid.serialization.lif.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static org.lappsgrid.discriminator.Discriminators.Uri;


/**
 * <a href="http://nlp.stanford.edu/software/corenlp.shtml" target="_blank">
 * Stanford Core NLP </a> provides a collection of available NLP tools,
 * including "tokenize, ssplit, pos, lemma, ner, parse, dcoref".
 *
 * <p>
 *
 * They are available through unique interface called annotation.
 *
 * @author shicq@cs.brandeis.edu
 *
 */

public abstract class AbstractStanfordCoreNLPWebService implements WebService {

    private static final Logger log = LoggerFactory
            .getLogger(AbstractStanfordCoreNLPWebService.class);

    private static ConcurrentHashMap<String, StanfordCoreNLP> cache =
            new ConcurrentHashMap<>();

    static final String containerScheme = "http://vocab.lappsgrid.org/schema/container-schema-1.1.0.json";
    static final String metadataScheme = "http://vocab.lappsgrid.org/schema/metadata-schema-1.1.0.json";

    static final String PROP_TOKENIZE = "tokenize";
    static final String PROP_SENTENCE_SPLIT = "ssplit";
    static final String PROP_POS_TAG = "pos";
    static final String PROP_LEMMA = "lemma";
    static final String PROP_NER = "ner";
    static final String PROP_PARSE = "parse";
    static final String PROP_CORERENCE = "dcoref";
    static final String PROP_KEY = "annotators";
    static final String TOKEN_ID = "tk_";
    static final String SENT_ID = "s_";
    static final String CONSTITUENT_ID = "c_";
    static final String PS_ID = "ps_";
    static final String DEPENDENCY_ID = "dep_";
    static final String DS_ID = "ds_";
    static final String MENTION_ID = "m_";
    static final String COREF_ID = "coref_";
    static final String NE_ID = "ne_";

    static final Map<String, String> tagsetMap;
    static {
        Map<String, String> aMap = new HashMap<>();
        aMap.put(Uri.POS, "posTagSet");
        aMap.put(Uri.NE, "namedEntityCategorySet");
        aMap.put(Uri.PHRASE_STRUCTURE, "categorySet");
        aMap.put(Uri.DEPENDENCY_STRUCTURE, "dependencySet");
        tagsetMap = Collections.unmodifiableMap(aMap);
    }

    static final Map<String, String> containsTypesMap;
    static {
        Map<String, String> aMap = new HashMap<>();
        aMap.put(Uri.TOKEN, "tokenizer:stanford:brandeis");
        aMap.put(Uri.SENTENCE, "splitter:stanford:brandeis");
        aMap.put(Uri.POS, "postagger:stanford:brandeis");
        aMap.put(Uri.NE, "ner:stanford:brandeis");
        aMap.put(Uri.COREF, "coreference:stanford:brandeis");
        aMap.put(Uri.MARKABLE, "markable:stanford:brandeis");
        aMap.put(Uri.CONSTITUENT, "syntacticparser:stanford:brandeis");
        aMap.put(Uri.PHRASE_STRUCTURE, "syntacticparser:stanford:brandeis");
        aMap.put(Uri.DEPENDENCY, "dependency-parser:stanford:brandeis");
        aMap.put(Uri.DEPENDENCY_STRUCTURE, "dependency-parser:stanford:brandeis");
        containsTypesMap = Collections.unmodifiableMap(aMap);
    }

    private Properties props = new Properties();
    StanfordCoreNLP snlp = null;

    ServiceMetadata metadata;

    /**
     * Default constructor only tries to load metadata.
     * Doing this will also set up metadata and keep it in memory
     */
    AbstractStanfordCoreNLPWebService() {
        try {
            this.metadata = loadMetadata();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get version from pom metadata
     */
    String getVersion() {
        String path = "/version.properties";
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            log.error("version.properties file not found, version is UNKNOWN.");
            return "UNKNOWN";
        }
        Properties properties = new Properties();
        try {
            properties.load(stream);
            stream.close();
            return (String) properties.get("version");
        } catch (IOException e) {
            log.error("error loading version.properties, version is UNKNOWN.");
            return "UNKNOWN";
        }
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

    /**
     * This is default execute: takes a json, wrap it as a LIF, run modules
     */
    @Override
    public String execute(String input) {
        if (input == null)
            return null;
        // in case of Json
        Data data;

        try {
            data = Serializer.parse(input, Data.class);
            // Serializer#parse throws JsonParseException if input is not well-formed
        } catch (Exception e) {
            data = new Data();
            data.setDiscriminator(Uri.TEXT);
            data.setPayload(input);
        }

        final String discriminator = data.getDiscriminator();
        Container cont;

        switch (discriminator) {
            case Uri.ERROR:
                return input;
            case Uri.JSON_LD:
            case Uri.LIF:
                cont = new Container((Map) data.getPayload());
                // TODO: 5/9/18 what if the existing payload has different schema version?
                break;
            case Uri.TEXT:
                cont = new Container();
                // TODO: 5/9/18  fix url when it settles in
                cont.setSchema(containerScheme);
                cont.setText((String) data.getPayload());
                cont.setLanguage("en");
                break;
            default:
                String message = String.format
                        ("Unsupported discriminator type: %s", discriminator);
                return new Data<>(Uri.ERROR, message).asJson();
        }

        try {
            return execute(cont);
        } catch (Throwable th) {
            th.printStackTrace();
            String message =
                    String.format("Error processing input: %s", th.toString());
            return new Data<>(Uri.ERROR, message).asJson();
        }
    }

    /**
     * This will be overridden for each module
     */
    public abstract String execute(Container json);

    abstract ServiceMetadata loadMetadata();

    @Override
    public String getMetadata() {
        return new Data<>(Uri.META, this.metadata).asPrettyJson();
    }

    void setUpContainsMetadata(View view) {
        for (String atype : this.metadata.getProduces().getAnnotations()) {
            Contains newContains = view.addContains(atype,
                    String.format("%s:%s", this.getClass().getName(), getVersion()),
                    containsTypesMap.get(atype));
            if (this.metadata.getProduces().getTagSets().containsKey(atype)) {
                newContains.put(tagsetMap.get(atype),
                        this.metadata.getProduces().getTagSets().get(atype));
            }
        }
    }

    ServiceMetadata setCommonMetadata() {
        ServiceMetadata commonMetadata = new ServiceMetadata();
        // TODO: 4/22/18 fix url when it settles in
        commonMetadata.setSchema(metadataScheme);
        commonMetadata.setVendor("http://www.cs.brandeis.edu/");
        commonMetadata.setLicense(Uri.APACHE2);
        commonMetadata.setVersion(this.getVersion());
        commonMetadata.setName(this.getClass().getName());

        IOSpecification required = new IOSpecification();
        required.addLanguage("en");
        required.setEncoding("UTF-8");
        required.addFormat(Uri.TEXT);
        required.addFormat(Uri.LIF);
        commonMetadata.setRequires(required);

        IOSpecification produces = new IOSpecification();
        produces.addLanguage("en");
        produces.setEncoding("UTF-8");
        produces.addFormat(Uri.LIF);
        commonMetadata.setProduces(produces);

        return commonMetadata;
    }
}

