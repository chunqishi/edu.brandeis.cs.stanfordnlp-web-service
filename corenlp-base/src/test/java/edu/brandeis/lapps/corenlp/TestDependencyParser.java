package edu.brandeis.lapps.corenlp;

import org.junit.Test;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.vocabulary.Features;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.lappsgrid.discriminator.Discriminators.Uri;

public class TestDependencyParser extends TestCorenlpService {


    public TestDependencyParser() {
        service = new DependencyParser();
        testText = "Hi, Programcreek is a very huge and useful website.";
    }


    @Test
    public void testMetadata() {
        ServiceMetadata metadata = super.testDefaultMetadata();
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
        List<Annotation> annotations = executionResult.getView(0).getAnnotations();

        assertEquals("Dep-trees", 1,
                annotations.stream().filter(ann -> ann.getAtType().equals(Uri.DEPENDENCY_STRUCTURE)).count());
        List<Annotation> rootNodes = annotations.stream()
                .filter(ann -> ann.getAtType().equals(Uri.DEPENDENCY))
                .filter(depAnn -> depAnn.getFeature(Features.Dependency.LABEL).toLowerCase().equals("root"))
                .collect(Collectors.toList());
        assertEquals("# of root node", 1, rootNodes.size());
        assertEquals("root node word (the subject)", "website",
                rootNodes.get(0).getFeature(Features.Dependency.DEPENDENT + "_word"));
    }
}
