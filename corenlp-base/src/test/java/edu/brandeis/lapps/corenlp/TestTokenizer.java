package edu.brandeis.lapps.corenlp;

import org.junit.Test;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.lappsgrid.discriminator.Discriminators.Uri;

public class TestTokenizer extends TestCorenlpService {

    public TestTokenizer() {
        service = new Tokenizer();
        testText = "Hello World.";
    }

    @Test
    public void testMetadata(){
        ServiceMetadata metadata = super.testDefaultMetadata();
        IOSpecification requires = metadata.getRequires();
        IOSpecification produces = metadata.getProduces();
        assertEquals("Expected 1 annotation, found: " + produces.getAnnotations().size(),
                1, produces.getAnnotations().size());
        assertTrue("Tokens not produced",
                produces.getAnnotations().contains(Uri.TOKEN));
    }

    @Test
    public void testExecute(){
        Container executionResult = super.testExecuteFromPlainAndLIFWrapped();

        List<Annotation> annotations = executionResult.getView(0).getAnnotations();
        assertEquals("Tokens", 3, annotations.size());
    }
}