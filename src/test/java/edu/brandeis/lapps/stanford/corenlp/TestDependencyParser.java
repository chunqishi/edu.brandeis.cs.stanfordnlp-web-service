package edu.brandeis.lapps.stanford.corenlp;

import edu.brandeis.lapps.TestBrandeisService;
import org.junit.Test;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.lif.Container;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.lappsgrid.discriminator.Discriminators.Uri;

public class TestDependencyParser extends TestBrandeisService {


    public TestDependencyParser() {
        service = new DependencyParser();
        testText = "Hi, Programcreek is a very huge and useful website.";
    }


    @Test
    public void testMetadata() {
        ServiceMetadata metadata = super.testCommonMetadata();
        IOSpecification requires = metadata.getRequires();
        IOSpecification produces = metadata.getProduces();
        assertEquals("Expected 3 annotations, found: " + produces.getAnnotations().size(),
                3, produces.getAnnotations().size());
        assertTrue("POS not produced",
                produces.getAnnotations().contains(Uri.POS));
        assertTrue("Dependencies not produced",
                produces.getAnnotations().contains(Uri.DEPENDENCY));
        assertTrue("Dependency Structures not produced",
                produces.getAnnotations().contains(Uri.DEPENDENCY_STRUCTURE));
    }

    @Test
    public void testExecute(){
        Container executionResult = super.testExecuteFromPlainAndLIFWrapped();

    }
}
