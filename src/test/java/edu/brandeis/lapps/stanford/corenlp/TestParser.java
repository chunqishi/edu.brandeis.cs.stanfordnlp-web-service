package edu.brandeis.lapps.stanford.corenlp;

import edu.brandeis.lapps.TestBrandeisService;
import org.junit.Test;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.lif.Container;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.lappsgrid.discriminator.Discriminators.Uri;

public class TestParser extends TestBrandeisService {


    public TestParser() {
        service = new Parser();
        testText = "If possible, we would appreciate comments no later than 3:00 PM EST on Sunday, August 26.  Comments can be faxed to my attention at 202/338-2416 or emailed to cfr@vnf.com or gdb@vnf.com (Gary GaryBachman).\\n\\nThank you.";
    }

    @Test
    public void testMetadata(){
        ServiceMetadata metadata = super.testCommonMetadata();
        IOSpecification requires = metadata.getRequires();
        IOSpecification produces = metadata.getProduces();
        assertEquals("Expected 3 annotations, found: " + produces.getAnnotations().size(),
                3, produces.getAnnotations().size());
        assertTrue("POS not produced",
                produces.getAnnotations().contains(Uri.POS));
        assertTrue("Constituents not produced",
                produces.getAnnotations().contains(Uri.CONSTITUENT));
        assertTrue("Phrase structures not produced",
                produces.getAnnotations().contains(Uri.PHRASE_STRUCTURE));

    }

    @Test
    public void testExecute(){
        Container executionResult = super.testExecuteFromPlainAndLIFWrapped();
        executionResult.getView(0).getAnnotations();


    }
}
