package myunittest;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.research.xacml.api.*;
import com.att.research.xacml.api.pdp.*;
import com.att.research.xacml.std.json.JSONRequest;
import com.att.research.xacml.util.FactoryException;
import com.att.research.xacml.util.XACMLProperties;

public class MyUnitTest {
    private static final Logger logger  = LoggerFactory.getLogger(MyUnitTest.class);
    
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    public MyUnitTest() {
        
    }
    
    private static final String POLICY =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
            " <!--This file was generated by the ALFA Plugin for Eclipse from Axiomatics AB (http://www.axiomatics.com). \n" + 
            " Any modification to this file will be lost upon recompilation of the source ALFA file-->\n" + 
            "<xacml3:Policy xmlns:xacml3=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\"\n" + 
            "    PolicyId=\"http://axiomatics.com/alfa/identifier/nf.playbackPolicy\"\n" + 
            "    RuleCombiningAlgId=\"urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit\"\n" + 
            "    Version=\"1.0\">\n" + 
            "    <xacml3:Description />\n" + 
            "    <xacml3:PolicyDefaults>\n" + 
            "        <xacml3:XPathVersion>http://www.w3.org/TR/1999/REC-xpath-19991116</xacml3:XPathVersion>\n" + 
            "    </xacml3:PolicyDefaults>\n" + 
            "    <xacml3:Target />\n" + 
            "    <xacml3:Rule \n" + 
            "            Effect=\"Permit\"\n" + 
            "            RuleId=\"http://axiomatics.com/alfa/identifier/nf.playbackPolicy.activeRule\">\n" + 
            "        <xacml3:Description />\n" + 
            "        <xacml3:Target>\n" + 
            "            <xacml3:AnyOf>\n" + 
            "                <xacml3:AllOf>\n" + 
            "                    <xacml3:Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:boolean-equal\">\n" + 
            "                        <xacml3:AttributeValue\n" + 
            "                            DataType=\"http://www.w3.org/2001/XMLSchema#boolean\">true</xacml3:AttributeValue>\n" + 
            "                        <xacml3:AttributeDesignator \n" + 
            "                            AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject:subject-active-state\"\n" + 
            "                            DataType=\"http://www.w3.org/2001/XMLSchema#boolean\"\n" + 
            "                            Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\"\n" + 
            "                            MustBePresent=\"false\"\n" + 
            "                        />\n" + 
            "                    </xacml3:Match>\n" + 
            "                </xacml3:AllOf>\n" + 
            "            </xacml3:AnyOf>\n" + 
            "        </xacml3:Target>\n" + 
            "    </xacml3:Rule>\n" + 
            "</xacml3:Policy>\n";
    
    private Response makeRequest(Request request) throws FactoryException, IOException {
        //
        // Send it to the PDP
        //
        Response response = null;
        //
        // Embedded call to PDP
        //
        long lTimeStart = System.currentTimeMillis();
        try {            
            PDPEngineFactory factory = PDPEngineFactory.newInstance();
            PDPEngine engine = factory.newEngine();
            response = engine.decide(request);
        } catch (PDPException e) {
            logger.error("{}", e);
        }
        long lTimeEnd = System.currentTimeMillis();
        logger.info("Elapsed Time: " + (lTimeEnd - lTimeStart) + "ms");

        return response;
    }
    
    @Test
    public void testPolicy() throws Exception {
        String req = "{\"Request\":{\"ReturnPolicyIdList\":\"false\",\"CombinedDecision\":\"false\",\"Category\":[{\"CategoryId\":\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\",\"Attribute\":[{\"AttributeId\":\"urn:oasis:names:tc:xacml:1.0:subject:subject-id\",\"Value\":\"Julius Hibbert\"},{\"AttributeId\":\"urn:oasis:names:tc:xacml:1.0:subject:subject-active-state\",\"Value\":\"true\",\"DataType\":\"http://www.w3.org/2001/XMLSchema#boolean\"}]},{\"CategoryId\":\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\",\"Attribute\":[{\"AttributeId\":\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\",\"Value\":\"device1\",\"DataType\":\"http://www.w3.org/2001/XMLSchema#string\"}]}]}}";
        
        // Create a temporary file.
        final File tempXacmlPropertiesFile = tempFolder.newFile("tempXacmlPropertiesFile.txt");
        final String TEMP_ROOT_POLICY_NAME = "tempRootPolicy.xml";
        final File tempRootPolicyFile = tempFolder.newFile(TEMP_ROOT_POLICY_NAME);
        
        FileUtils.writeStringToFile(tempXacmlPropertiesFile, XACMLProperties.PROP_ROOTPOLICIES + "=" + TEMP_ROOT_POLICY_NAME + "\n");
        FileUtils.writeStringToFile(tempXacmlPropertiesFile, TEMP_ROOT_POLICY_NAME + ".file =" + tempRootPolicyFile.getAbsolutePath(), true);
        FileUtils.writeStringToFile(tempRootPolicyFile, POLICY);
        System.setProperty(XACMLProperties.XACML_PROPERTIES_NAME, tempXacmlPropertiesFile.getAbsolutePath());
        
        Response resp = makeRequest(JSONRequest.load(req));
        
        assertEquals(Decision.PERMIT, resp.getResults().iterator().next().getDecision());
    }
}