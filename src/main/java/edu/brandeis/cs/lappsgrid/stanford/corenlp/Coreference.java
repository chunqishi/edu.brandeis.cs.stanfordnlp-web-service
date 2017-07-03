package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;
import org.lappsgrid.discriminator.Discriminators.Uri;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;
import org.lappsgrid.vocabulary.Features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lappsgrid.vocabulary.Features.Token;

/**
 *
 * @author Chunqi SHI (shicq@cs.brandeis.edu)
 * @author Keigh Rim (krim@brandeis.edu)
 * @since 2014-03-25
 *
 */
@org.lappsgrid.annotations.ServiceMetadata(
        description = "Stanford CoreNLP 3.3.1 Coreference",
        requires_format = { "text", "lif" },
        produces_format = { "lif" },
        produces = { "coref", "token", "markable" }
)
public class Coreference extends AbstractStanfordCoreNLPWebService {

    public Coreference() {
        this.init(PROP_TOKENIZE,PROP_SENTENCE_SPLIT,
                PROP_POS_TAG, PROP_LEMMA, PROP_NER, PROP_PARSE, PROP_CORERENCE);
    }

    @Override
    public String execute(Container container) throws StanfordWebServiceException {

        String text = container.getText();

        // Prepare two containers, one for tokens and one for coreference chain
        View view = container.newView();
        view.addContains(Uri.TOKEN,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "tokenizer:stanford");

        view.addContains(Uri.COREF,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "coreference:stanford");

        view.addContains(Uri.MARKABLE,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "markable:stanford");

        edu.stanford.nlp.pipeline.Annotation annotation
                = new edu.stanford.nlp.pipeline.Annotation(text);
        snlp.annotate(annotation);
        List<CoreMap> sents = annotation.get(SentencesAnnotation.class);

        // first iteration to populate token annotations
        int sid = 0;
        for (CoreMap sent : sents) {

            // first, populate tokenization view
            Map<String, String> tokenIndex = new HashMap<>();
            int tid = 0;
            for (CoreLabel token : sent.get(TokensAnnotation.class)) {
                String tokenId = String.format("%s%d_%d", TOKEN_ID, sid, tid++);
                tokenIndex.put(token.word(), tokenId);
                Annotation ann = view.newAnnotation(tokenId,
                        Uri.TOKEN, token.beginPosition(), token.endPosition());
                ann.addFeature("word", token.value());
                ann.addFeature(Token.POS,  token.tag());
            }
            sid++;
        }
        // next iteration to populate mentions and markables

        Map<Integer, CorefChain> corefs = annotation.get(CorefChainAnnotation.class);
        for(Integer corefId : corefs.keySet()) {
            CorefChain coref =   corefs.get(corefId);
            List<CorefMention> mentions = coref.getMentionsInTextualOrder();
            if(mentions.size() <= 1)
                continue;
            ArrayList<String> mentionIds = new ArrayList<>();
            for (CorefMention mention : mentions) {
                CoreMap sent = sents.get(mention.sentNum - 1);
                List<CoreLabel> tokens = sent.get(TokensAnnotation.class);
                int mBegin = tokens.get(mention.startIndex - 1).beginPosition();
                int mEnd = tokens.get(mention.endIndex - 2).endPosition();
                Annotation mentionAnn = view.newAnnotation(
                        MENTION_ID + mention.mentionID, Uri.MARKABLE, mBegin, mEnd);
                mentionAnn.addFeature("words", text.substring(mBegin, mEnd));
                mentionAnn.addFeature("sentenceIndex", Integer.toString(mention.sentNum - 1));
                ArrayList<String> targets = new ArrayList<>();
                for (int m = mention.startIndex; m < mention.endIndex; m++)
                    // stanford idx starts from 1, need to subtract 1 for each index
                    targets.add("tk_" + (mention.sentNum - 1) + "_" + (m - 1));
                mentionAnn.getFeatures().put(Features.Markable.TARGETS, targets);
                mentionIds.add(MENTION_ID + mention.mentionID);

            }

            // instead of using own ID numbering, we can take the numeric ID from
            // CorefChainAnnotation class, which is always the same as the id of
            // representative mention.
            Annotation chain = view.newAnnotation(COREF_ID + corefId, Uri.COREF);
            chain.addFeature("representative",
                    MENTION_ID + coref.getRepresentativeMention().mentionID);
            chain.getFeatures().put(Features.Coreference.MENTIONS, mentionIds);
        }

        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }
}
