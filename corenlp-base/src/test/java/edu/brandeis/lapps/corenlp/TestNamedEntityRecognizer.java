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

public class TestNamedEntityRecognizer extends TestCorenlpService {

    public TestNamedEntityRecognizer() {
        service = new NamedEntityRecognizer();
        testText = "That guy, John Johnson is a sales associate at Sotheby's. He started working on 7/4/2015 and it was 5 years ago.";
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

        assertEquals("Entities", 4, annotations.size());
        Annotation entity = annotations.get(0);
        assertEquals("Label is not correct.", "person", entity.getLabel());
        assertEquals("Word is not correct.", "John Johnson", entity.getFeature("word"));
        assertEquals("Category is not correct.", "person", entity.getFeature("category"));
        entity = annotations.get(1);
        assertEquals("Category is not correct.", "organization", entity.getFeature("category"));
        assertEquals("Word is not correct.", "Sotheby's", entity.getFeature("word"));
        entity = annotations.get(2);
        assertEquals("Category is not correct.", "date", entity.getFeature("category"));
        assertEquals("Word is not correct.", "7/4/2015", entity.getFeature("word"));
        entity = annotations.get(3);
        assertEquals("Category is not correct.", "date", entity.getFeature("category"));
        assertEquals("Word is not correct.", "5 years ago", entity.getFeature("word"));

    }
}


