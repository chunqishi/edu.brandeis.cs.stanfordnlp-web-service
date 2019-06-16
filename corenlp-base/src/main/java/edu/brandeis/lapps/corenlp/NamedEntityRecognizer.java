package edu.brandeis.lapps.corenlp;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;

import java.util.List;

import static org.lappsgrid.discriminator.Discriminators.Uri;

public class NamedEntityRecognizer extends AbstractStanfordCoreNLPWebService {

    private String TOOL_DESCRIPTION = "This service is a wrapper around Stanford CoreNLP " + getWrappeeVersion() + " providing a named entity recognition service" +
            "\nInternally it uses CoreNLP default \"tokenize\", \"ssplit\", \"pos\", \"lemma\", \"ner\" annotators as one pipeline.";

    public NamedEntityRecognizer() {
        this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT,
                PROP_POS_TAG, PROP_LEMMA, PROP_NER);
    }

    @Override
    protected String processPayload(Container container) {

        String text = container.getText();
        View view = container.newView();
        setUpContainsMetadata(view, "stanford");

        int id = -1;
        edu.stanford.nlp.pipeline.Annotation annotation
                = new edu.stanford.nlp.pipeline.Annotation(text);
        snlp.annotate(annotation);
        List<CoreMap> sents = annotation.get(SentencesAnnotation.class);
        for (CoreMap sent : sents) {
            int begin = -1;
            int end = -1;
            StringBuilder surfForm = new StringBuilder();
            String category = "";
            for (CoreLabel token : sent.get(TokensAnnotation.class)) {
                String label = token.ner().toLowerCase();

                if (!label.equalsIgnoreCase("O")) {

                    // at the first token or a token after "Out" tag
                    if (begin == -1) {
                        begin = token.beginPosition();
                        end = token.endPosition();
                        surfForm = new StringBuilder(token.value());
                        category = label;
                    }

                    // second or later tokens in a multi-token NE (John/PERSON Johnson/PERSON)
                    else if (label.equals(category)) {
                        while (end < token.beginPosition()) {
                            surfForm.append(" ");
                            end++;
                        }
                        surfForm.append(token.value());
                        end = token.endPosition();
                    }
                    // a NE follows right after a previous NE (does this occur in English?)
                    else {
                        // add an annotation for the previous NE of that was kept track
                        String type = Uri.NE;
                        Annotation ann = new Annotation(NE_ID + (++id), type, category, begin, end);
                        ann.addFeature("category", category);
                        ann.addFeature("word", surfForm.toString());
                        view.addAnnotation(ann);

                        // then start to track a new NE
                        begin = token.beginPosition();
                        end = token.endPosition();
                        surfForm = new StringBuilder(token.value());
                        category = label;
                    }

                }
                // meets "Out" tag with a NE being tracked
                else if (begin != -1) {
                    // add an annotation of that NE
                    String type = Uri.NE;
                    Annotation ann = new Annotation(NE_ID + (++id), type, category, begin, end);
                    ann.addFeature("category", category);
                    ann.addFeature("word", surfForm.toString());
                    view.addAnnotation(ann);

                    // then reset tracking
                    begin = -1;
                    end = -1;
                    surfForm = new StringBuilder();
                    category = "";
                }
            }
        }
        // set discriminator to LIF
        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }

    @Override
    protected ServiceMetadata loadMetadata() {
        ServiceMetadata metadata = setDefaultMetadata();
        metadata.setDescription(TOOL_DESCRIPTION);
        metadata.getProduces().addAnnotations(Uri.NE);
        metadata.getProduces().addTagSet(Uri.NE, Uri.TAGS_NER_STANFORD);

        return metadata;
    }
}
