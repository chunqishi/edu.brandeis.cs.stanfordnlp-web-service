package edu.brandeis.lapps.corenlp;

import org.junit.Test;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.lappsgrid.discriminator.Discriminators.Uri;

public class TestSplitter extends TestCorenlpService {


    public TestSplitter() {
        service = new Splitter();
        testText = "If possible, we would appreciate comments no later than 3:00 PM EST on Sunday, August 26.  Comments can be faxed to my attention at 202/338-2416 or emailed to cfr@vnf.com or gdb@vnf.com (Gary GaryBachman).\n\nThank you.";
    }

    @Test
    public void testMetadata() {
        ServiceMetadata metadata = super.testDefaultMetadata();
        IOSpecification requires = metadata.getRequires();
        IOSpecification produces = metadata.getProduces();
        assertEquals("Expected 1 annotation, found: " + produces.getAnnotations().size(),
                1, produces.getAnnotations().size());
        assertEquals("Sentences not produced", Uri.SENTENCE,
                produces.getAnnotations().get(0));
    }

    @Test
    public void testExecute(){
        Container executionResult = super.testExecuteFromPlainAndLIFWrapped();
        List<Annotation> annotations = executionResult.getView(0).getAnnotations();

        assertEquals("Sentences", 3, annotations.size());

    }
}

