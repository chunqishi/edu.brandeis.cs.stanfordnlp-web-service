package edu.brandeis.lapps.stanford.corenlp;

import edu.brandeis.lapps.TestBrandeisService;
import org.junit.Test;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.lif.Container;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.lappsgrid.discriminator.Discriminators.Uri;

public class TestCoreference extends TestBrandeisService {


    public TestCoreference() {
        service = new Coreference();
        testText = "Smith is a good person and he is from Boston. John and Mary went to the store. They bought some milk.";
    }


    @Test
    public void testMetadata(){
        ServiceMetadata metadata = super.testCommonMetadata();
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

    }
}
