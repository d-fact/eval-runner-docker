package ca.uwaterloo.cs.jgrok.test;

import org.xml.sax.Attributes;

import ca.uwaterloo.cs.jgrok.env.DefaultLoader;

public class RegressionLoader extends DefaultLoader {
    private Regression regress = null;
    private RegressionTest test = null;
    
    public RegressionLoader(Regression regress) {
        this.regress = regress;
    }

    public void startElement(String uri, String name, String qName, Attributes atts) {
        if (name.equalsIgnoreCase("Regression")) {
            for(int i = 0; i < atts.getLength(); i++) {
                String localName = atts.getLocalName(i);
                
                if(localName.equalsIgnoreCase("Name"))
                    regress.setName(atts.getValue(i));
                else if(localName.equalsIgnoreCase("State"))
                    regress.setState(atts.getValue(i));
                else if(localName.equalsIgnoreCase("Input"))
                    regress.setInput(atts.getValue(i));
                else if(localName.equalsIgnoreCase("Output"))
                    regress.setOutput(atts.getValue(i));
            }
        } else if (name.equalsIgnoreCase("Test")) {
            test = new RegressionTest(regress);
            
            for(int i = 0; i < atts.getLength(); i++) {
                String localName = atts.getLocalName(i);
                
                if(localName.equalsIgnoreCase("Name"))
                    test.setName(atts.getValue(i));
                else if(localName.equalsIgnoreCase("State"))
                    test.setState(atts.getValue(i));
            }
        } else if (name.equalsIgnoreCase("Script")) {
            for(int i = 0; i < atts.getLength(); i++) {
                String localName = atts.getLocalName(i);
                
                if(localName.equalsIgnoreCase("File"))
                    test.setScriptFile(atts.getValue(i));
            }
        } else if (name.equalsIgnoreCase("Data")) {
            for(int i = 0; i < atts.getLength(); i++) {
                String localName = atts.getLocalName(i);
                
                if(localName.equalsIgnoreCase("File"))
                    test.setInputFile(atts.getValue(i));
            }
        } else if (name.equalsIgnoreCase("Log")) {
            for(int i = 0; i < atts.getLength(); i++) {
                String localName = atts.getLocalName(i);
                
                if(localName.equalsIgnoreCase("File"))
                    test.setLogFile(atts.getValue(i));
            }
        }
    }
}
