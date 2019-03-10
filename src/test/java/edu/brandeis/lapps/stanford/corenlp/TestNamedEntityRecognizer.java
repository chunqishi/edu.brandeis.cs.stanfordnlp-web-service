package edu.brandeis.lapps.stanford.corenlp;

import edu.brandeis.lapps.TestBrandeisService;
import org.junit.Test;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.lappsgrid.discriminator.Discriminators.Uri;

public class TestNamedEntityRecognizer extends TestBrandeisService {

    public TestNamedEntityRecognizer() {
        service = new NamedEntityRecognizer();
        testText = "Mike is a person. His wife is also a person.";
    }

    @Test
    public void testMetadata(){
        ServiceMetadata metadata = super.testCommonMetadata();
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

        View view = executionResult.getView(0);
        List<Annotation> annotations = view.getAnnotations();
        assertEquals("Entities", 1, annotations.size());
        Annotation mike = annotations.get(0);
        assertEquals("Label is not correct.", "person", mike.getLabel());
        assertEquals("Category is not correct.", "person", mike.getFeature("category"));

    }
}


