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

public class TestNamedEntityRecognizer extends TestCorenlpService {

    public TestNamedEntityRecognizer() {
        service = new NamedEntityRecognizer();
        testText = "Mike is a person. His wife is also a person.";
    }

    @Test
    public void testMetadata(){
        ServiceMetadata metadata = super.testDefaultMetadata();
        IOSpecification requires = metadata.getRequires();
        IOSpecification produces = metadata.getProduces();
        assertEquals("Expected 1 annotations, found: " + produces.getAnnotations().size(),
                1, produces.getAnnotations().size());
        assertTrue("Instead of NE, found : " + produces.getAnnotations().get(0),
                produces.getAnnotations().contains(Uri.NE));
    }

    @Test
    public void testExecute() {
        Container executionResult = super.testExecuteFromPlainAndLIFWrapped();
        List<Annotation> annotations = executionResult.getView(0).getAnnotations();

        assertEquals("Entities", 1, annotations.size());
        Annotation mike = annotations.get(0);
        assertEquals("Label is not correct.", "person", mike.getLabel());
        assertEquals("Category is not correct.", "person", mike.getFeature("category"));

    }
}


