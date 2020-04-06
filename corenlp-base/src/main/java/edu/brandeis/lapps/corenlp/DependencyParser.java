package edu.brandeis.lapps.corenlp;

import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;
import org.lappsgrid.vocabulary.Features;

import java.util.ArrayList;
import java.util.List;

import static org.lappsgrid.discriminator.Discriminators.Uri;

public class DependencyParser extends AbstractCorenlpWrapper {

    private static String rootLabel = "ROOT";
    private String TOOL_DESCRIPTION = "This service is a wrapper around Stanford CoreNLP " + getWrappeeVersion() + " providing a dependency parser service"
            + "\nInternally it uses CoreNLP default \"tokenize\", \"ssplit\", \"parse\" annotators as one pipeline.";

    public DependencyParser() {
        this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT, PROP_PARSE);
    }

    @Override
    protected String processPayload(Container container) {

        String text = container.getText();
        View view = container.newView();
        setUpContainsMetadata(view, "stanford");

        edu.stanford.nlp.pipeline.Annotation doc
                = new edu.stanford.nlp.pipeline.Annotation(text);
        snlp.annotate(doc);
        List<CoreMap> list = doc.get(SentencesAnnotation.class);
        int cntSent = 0;
        for (CoreMap sent : list) {
            int start = sent.get(CharacterOffsetBeginAnnotation.class);
            int end = sent.get(CharacterOffsetEndAnnotation.class);
            Annotation ann = view.newAnnotation(DS_ID + cntSent,
                    Uri.DEPENDENCY_STRUCTURE, start, end);

            ann.addFeature("sentence", sent.toString());
            SemanticGraph graph = sent.get(BasicDependenciesAnnotation.class);
            int cntEdge = 0;
            List<String> dependencies = new ArrayList<>();
            for (IndexedWord root : graph.getRoots()) {
                String id = String.format("%s%d_%d", DEPENDENCY_ID, cntSent, cntEdge++);
                dependencies.add(id);
                Annotation dependency = view.newAnnotation(id, Uri.DEPENDENCY);
                // TODO: 2/22/2018 top-level "label" will go away after LIF JSON scheme 1.1.0
                dependency.setLabel(rootLabel);
                dependency.addFeature(Features.Dependency.GOVERNOR,
                        "null");
                dependency.addFeature(Features.Dependency.DEPENDENT,
                        makeTokenId(cntSent, root.index() - 1));
                dependency.addFeature("dependent_word", root.word());
                // as of LIF JSON scheme 1.1.0, all the "label"-ish go into the features map
                dependency.addFeature(Features.Dependency.LABEL, rootLabel);
            }
            for(SemanticGraphEdge edge:graph.getEdgeSet()) {
                String id = String.format("%s%d_%d",
                        DEPENDENCY_ID, cntSent, cntEdge++);
                dependencies.add(id);

                Annotation dependency = view.newAnnotation(id, Uri.DEPENDENCY);
                String depLabel = edge.getRelation().toString();
                // TODO: 2/22/2018 top-level "label" will go away after LIF JSON scheme 1.1.0
                dependency.setLabel(depLabel);
                // stanford indexing starts from 1, for consistency, we start from 0
                dependency.addFeature(Features.Dependency.GOVERNOR,
                        makeTokenId(cntSent, edge.getGovernor().index() - 1));
                dependency.addFeature("governor_word", edge.getGovernor().word());
                dependency.addFeature(Features.Dependency.DEPENDENT,
                        makeTokenId(cntSent, edge.getDependent().index() - 1));
                dependency.addFeature("dependent_word", edge.getDependent().word());
                // as of LIF JSON scheme 1.1.0, all the "label"-ish go into the features map
                dependency.addFeature(Features.Dependency.LABEL, depLabel);

            }
            ann.addFeature("dependencies", dependencies);


            int cntToken = 0;
            for (CoreLabel token : sent.get(TokensAnnotation.class)) {
                ann = view.newAnnotation(makeTokenId(cntSent, cntToken++),
                        Uri.TOKEN, token.beginPosition(), token.endPosition());
                ann.addFeature("pos", token.tag());
                ann.addFeature("word", token.value());
            }
            cntSent ++;
        }

        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }

    private String makeTokenId(int sid, int tid) {
        return String.format("%s%d_%d", TOKEN_ID, sid, tid);
    }

    @Override
    protected ServiceMetadata loadMetadata() {
        ServiceMetadata metadata = setDefaultMetadata();

        metadata.setDescription(TOOL_DESCRIPTION);
        metadata.getProduces().addAnnotations(Uri.DEPENDENCY, Uri.DEPENDENCY_STRUCTURE, Uri.POS);
        metadata.getProduces().addTagSet(Uri.POS, Uri.TAGS_POS_PENNTB);
        metadata.getProduces().addTagSet(Uri.DEPENDENCY_STRUCTURE, Uri.TAGS_DEP_STANFORD);

        return metadata;

    }
}
