package edu.brandeis.lapps.stanford.corenlp;

import junit.framework.Assert;
import org.junit.Test;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;

import java.util.List;

import static org.junit.Assert.*;
import static org.lappsgrid.discriminator.Discriminators.Uri;

/**
 * <i>TestTokenizer.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p>
 * <p>
 * Test cases are from <a
 * Tutorial</a>
 * href="http://www.programcreek.com/2012/05/opennlp-tutorial/">OpenNLP
 * <p>
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>
 *         Nov 20, 2013<br>
 *
 */
public class TestNamedEntityRecognizer extends TestService {

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


