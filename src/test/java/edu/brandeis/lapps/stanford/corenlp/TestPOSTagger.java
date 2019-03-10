package edu.brandeis.lapps.stanford.corenlp;

import org.junit.Test;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.lappsgrid.discriminator.Discriminators.Uri;

public class TestPOSTagger extends TestCorenlpService {


    public TestPOSTagger() {
        service = new POSTagger();
        testText = "Good morning.";
    }

    @Test
    public void testMetadata(){
        ServiceMetadata metadata = super.testDefaultMetadata();
        IOSpecification produces = metadata.getProduces();
        assertEquals("Expected 1 annotation, found: " + produces.getAnnotations().size(),
                1, produces.getAnnotations().size());
        assertTrue("POS tags not produced", produces.getAnnotations().contains(Uri.POS));
    }

    @Test
    public void testExecute(){
        Container executionResult = super.testExecuteFromPlainAndLIFWrapped();
        List<Annotation> annotations = executionResult.getView(0).getAnnotations();

        assertEquals("Tokens", 3, annotations.size());
        Annotation annotation = annotations.get(0);
        assertEquals("@type is not correct: " + annotation.getAtType(), annotation.getAtType(), Uri.TOKEN);
        String goodPos = annotation.getFeature("pos");
        assertEquals("Correct tag for 'Good' is JJ. Found: " + goodPos, goodPos, "JJ");

    }
}