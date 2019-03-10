package edu.brandeis.lapps.stanford.corenlp;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
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
import static org.lappsgrid.vocabulary.Features.Token;

public class POSTagger extends AbstractStanfordCoreNLPWebService {

    private static String TOOL_DESCRIPTION = "This service is a wrapper around Stanford CoreNLP 3.3.1 providing a part-of-speech tagging service" +
                    "\nInternally it uses CoreNLP default \"tokenize\", \"ssplit\", \"pos\" annotators as one pipeline.";

    public POSTagger() {
        this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT, PROP_POS_TAG);
    }

    @Override
    protected String execute(Container container) {

        String text = container.getText();
        View view = container.newView();
        setUpContainsMetadata(view, "stanford");

        edu.stanford.nlp.pipeline.Annotation annotation
                = new edu.stanford.nlp.pipeline.Annotation(text);
        snlp.annotate(annotation);
        int sid = 0;
        List<CoreMap> sents = annotation.get(SentencesAnnotation.class);
        for (CoreMap sent : sents) {
            int tid = 0;
            for (CoreLabel token : sent.get(TokensAnnotation.class)) {
                Annotation a = view.newAnnotation(
                        String.format("%s%d_%d", TOKEN_ID, sid, tid++), Uri.TOKEN,
                        token.beginPosition(), token.endPosition());
                a.setLabel(token.get(PartOfSpeechAnnotation.class));
                a.addFeature(Token.POS, token.get(PartOfSpeechAnnotation.class));
                a.addFeature(Token.WORD, token.value());
            }
            sid++;
        }

        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }

    @Override
    protected ServiceMetadata loadMetadata() {
        ServiceMetadata metadata = setDefaultMetadata();
        metadata.setDescription(TOOL_DESCRIPTION);
        metadata.getProduces().addAnnotations(Uri.POS);
        metadata.getProduces().addTagSet(Uri.POS, Uri.TAGS_POS_PENNTB);

        return metadata;
    }
}
