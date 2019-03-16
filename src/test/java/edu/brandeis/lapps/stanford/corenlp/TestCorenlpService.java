package edu.brandeis.lapps.stanford.corenlp;

import edu.brandeis.lapps.TestBrandeisService;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.lif.Container;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.lappsgrid.discriminator.Discriminators.Uri;

public class TestCorenlpService extends TestBrandeisService {

    protected String testText;

    public ServiceMetadata testDefaultMetadata() {
        ServiceMetadata metadata = super.testDefaultMetadata();
        assertEquals("License is not correct", Uri.GPL3, metadata.getLicense());

        IOSpecification produces = metadata.getProduces();
        assertEquals("Produces encoding is not correct", "UTF-8", produces.getEncoding());
        assertEquals("Too many output formats", 1, produces.getFormat().size());
        assertEquals("LIF not produces", Uri.LIF, produces.getFormat().get(0));

        IOSpecification requires = metadata.getRequires();
        assertEquals("Requires encoding is not correct", "UTF-8", requires.getEncoding());
        List<String> list = requires.getFormat();
        assertTrue("LIF format not accepted.", list.contains(Uri.LIF));
        assertTrue("Text not accepted", list.contains(Uri.TEXT));
        list = requires.getAnnotations();
        assertEquals("Required annotations should be empty", 0, list.size());

        return metadata;
    }

    public Container testExecuteFromPlainAndLIFWrapped() {
        Container fromAsIs = reconstructPayload(service.execute(testText));
        Container fromWrapped = reconstructPayload(service.execute(new Data<>(Uri.LIF, wrapContainer(testText)).asJson()));
        purgeTimestamps(fromAsIs, fromWrapped);
        testExecuteResult(fromAsIs, true);
        testExecuteResult(fromWrapped, false);
        assertEquals("Text is corrupted.", testText, fromAsIs.getText());
        assertEquals("Text different when wrapped in a LIF.", fromAsIs.getText(), fromWrapped.getText());
        assertEquals("A service should generate 1 view.", 1, fromAsIs.getViews().size());
        assertEquals("#Views different when wrapped in a LIF.", fromAsIs.getViews().size(), fromWrapped.getViews().size());
        assertEquals("#Annotations different when wrapped in a LIF.", fromAsIs.getView(0).getAnnotations().size(), fromWrapped.getView(0).getAnnotations().size());

        return fromAsIs;
    }

}
