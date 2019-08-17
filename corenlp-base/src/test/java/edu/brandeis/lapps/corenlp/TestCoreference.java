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

public class TestCoreference extends TestCorenlpService {


    public TestCoreference() {
        service = new Coreference();
        testText = "Smith is a good person and he is from Boston. John and Mary went to the store. They bought some milk.";
    }


    @Test
    public void testMetadata(){
        ServiceMetadata metadata = super.testDefaultMetadata();
        IOSpecification requires = metadata.getRequires();
        IOSpecification produces = metadata.getProduces();
        assertEquals(
                "Expected 3 annotations, found: " + produces.getAnnotations().size(),
                3, produces.getAnnotations().size());
        assertTrue("POS not produced",
                produces.getAnnotations().contains(Uri.POS));
        assertTrue("Markabels not produced",
                produces.getAnnotations().contains(Uri.MARKABLE));
        assertTrue("Coreference chains not produced",
                produces.getAnnotations().contains(Uri.COREF));
    }

    @Test
    public void testExecute(){
        Container executionResult = super.testExecuteFromPlainAndLIFWrapped();
        List<Annotation> annotations = executionResult.getView(0).getAnnotations();

        assertEquals("Chains (\"Smith\" and \"J&M\")", 2,
                annotations.stream().filter(ann -> ann.getAtType().equals(Uri.COREF)).count());

    }
}
