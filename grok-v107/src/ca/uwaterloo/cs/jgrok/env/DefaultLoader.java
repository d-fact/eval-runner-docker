package ca.uwaterloo.cs.jgrok.env;

import java.io.FileReader;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class DefaultLoader extends DefaultHandler implements Loader  {
    protected Env env;
    protected String fileName;
    
    public void load(Env env, String fileName) throws LoadingException {
        if (env == null) {
            throw new NullPointerException("env");
        }
        if (fileName == null) {
            throw new NullPointerException("fileName");
        }
        
        this.env = env;
        this.fileName = fileName;
        
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(this);
            xr.setErrorHandler(this);
            
            FileReader r = new FileReader(this.fileName);
            xr.parse(new InputSource(r));
        } catch(Exception e) {
            e.printStackTrace(System.err);
            throw new LoadingException(e.getMessage());
        }
    }
}
