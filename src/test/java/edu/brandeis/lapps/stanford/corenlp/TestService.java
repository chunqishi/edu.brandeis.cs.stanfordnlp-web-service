package edu.brandeis.lapps.stanford.corenlp;

import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static edu.brandeis.lapps.stanford.corenlp.AbstractStanfordCoreNLPWebService.containerScheme;
import static org.junit.Assert.*;
import static org.lappsgrid.discriminator.Discriminators.Uri;

/**
 * Created by Chunqi SHI (shicq@cs.brandeis.edu) on 3/5/14.
 */
public class TestService {

    AbstractStanfordCoreNLPWebService service;
    String testText;
    int expectedViewNum;
    List<String> expectedATypes;

    protected Container wrapContainer(String plainText) {
        Container container = new Container();
        container.setSchema(containerScheme);
        container.setText(plainText);
        container.setLanguage("en");
        return container;
    }

    protected Container reconstructPayload(String json) {
        return new Container(
                (Map) Serializer.parse(json, Data.class).getPayload());
    }

    public void purgeTimestamps(Container...containers) {
        Arrays.stream(containers).forEach(
                cont -> cont.getViews().forEach(
                        view -> view.setTimestamp("")));
    }

    public Container canProcessLIFWrappedPlainText() {
        String input = new Data<>(Uri.LIF, wrapContainer(testText)).asJson();
        Container result = reconstructPayload(service.execute(input));
        return result;

    }

    public Container canProcessPlainText() {
        String result = service.execute(testText);
        return reconstructPayload(result);

    }

    public Container testExecuteFromPlainAndLIFWrapped() {
        Container fromPlain = canProcessPlainText();
        Container fromWrapped = canProcessLIFWrappedPlainText();
        purgeTimestamps(fromPlain, fromWrapped);
        testExecuteResult(fromPlain, true);

        return fromPlain;
    }

    public void testExecuteResult(Container result, boolean wantEyeball) {
        assertNotNull(result);
        assertEquals("Text is corrupted.", result.getText(), testText);
        assertEquals("A service should generate 1 view.", 1, result.getViews().size());
        for (String expectedAType : service.metadata.getProduces().getAnnotations()) {
            String shortenedAType = expectedAType.substring(expectedAType.lastIndexOf('/') + 1);
            assertTrue("Not containing " + shortenedAType, result.getView(0).contains(expectedAType));
        }

        if (wantEyeball) {
            System.out.println("<------------------------------------------------------------------------------");
            System.out.println(String.format("      %s         ", this.getClass().getName()));
            System.out.println("-------------------------------------------------------------------------------");
            System.out.println(Serializer.toPrettyJson(result));
            System.out.println("------------------------------------------------------------------------------>");
        }

    }

    public ServiceMetadata testCommonMetadata() {
        String json = service.getMetadata();
        assertNotNull(service.getClass().getName() + ".getMetadata() returned null", json);

        Data data = Serializer.parse(json, Data.class);
        assertNotNull("Unable to parse metadata json.", data);
        assertNotSame(data.getPayload().toString(), Uri.ERROR, data.getDiscriminator());

        ServiceMetadata metadata = new ServiceMetadata((Map) data.getPayload());

        assertEquals("Vendor is not correct", "http://www.cs.brandeis.edu/", metadata.getVendor());
        assertEquals("Name is not correct", service.getClass().getName(), metadata.getName());
        assertEquals("Version is not correct.", service.getVersion(), metadata.getVersion());
        assertEquals("License is not correct", Uri.APACHE2, metadata.getLicense());

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

        // for human eyeballing
        System.out.println("<------------------------------------------------------------------------------");
        System.out.println(String.format("      %s         ", this.getClass().getName()));
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println(Serializer.toPrettyJson(metadata));
        System.out.println("------------------------------------------------------------------------------>");

        // return json for additional tests
        return metadata;
    }
}
