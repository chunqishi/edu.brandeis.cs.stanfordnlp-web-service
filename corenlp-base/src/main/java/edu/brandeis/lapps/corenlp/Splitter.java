package edu.brandeis.lapps.corenlp;

import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.util.CoreMap;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;

import java.util.List;

import static org.lappsgrid.discriminator.Discriminators.Uri;

public class Splitter extends AbstractStanfordCoreNLPWebService {

    private String TOOL_DESCRIPTION = "This service is a wrapper around Stanford CoreNLP " + getWrappeeVersion() + " providing a sentence splitter service" +
            "\nInternally it uses CoreNLP default \"tokenize\", \"ssplit\" annotators as one pipeline.";

    public Splitter() {
        this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT);
    }

    @Override
    protected String processPayload(Container container) {

        String text = container.getText();
        View view = container.newView();
        setUpContainsMetadata(view, "stanford");

        edu.stanford.nlp.pipeline.Annotation annotation
                = new edu.stanford.nlp.pipeline.Annotation(text);
        snlp.annotate(annotation);
        int id = -1;
        List<CoreMap> sents = annotation.get(SentencesAnnotation.class);
        for (CoreMap sent : sents) {
            int start = sent.get(CharacterOffsetBeginAnnotation.class);
            int end = sent.get(CharacterOffsetEndAnnotation.class);
            Annotation ann = view.newAnnotation(SENT_ID + (++id), Uri.SENTENCE, start, end);
            // TODO: 3/1/2018 this should go away when we complete ditch the "top-level" label field in LIF scheme
            ann.setLabel("sentence");
            ann.addFeature("sentence", sent.toString());
        }
        // set discriminator to LIF
        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }

    @Override
    protected ServiceMetadata loadMetadata() {
        ServiceMetadata metadata = setDefaultMetadata();
        metadata.setDescription(TOOL_DESCRIPTION);
        metadata.getProduces().addAnnotations(Uri.SENTENCE);

        return metadata;
    }
}
