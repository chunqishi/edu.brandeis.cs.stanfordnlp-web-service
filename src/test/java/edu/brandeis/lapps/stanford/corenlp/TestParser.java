package edu.brandeis.lapps.stanford.corenlp;

import junit.framework.Assert;
import org.junit.Test;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;

import java.util.List;

import static org.junit.Assert.*;
import static org.lappsgrid.discriminator.Discriminators.Uri;

/**
 * <i>TestParser.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p>
 * <p> Test cases are from
 * <a href="http://www.programcreek.com/2012/05/opennlp-tutorial/">
 *     OpenNLP Tutorial</a>
 * <p>
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 *
 */
public class TestParser extends TestService {


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
