package net.rim.tumbler.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import net.rim.tumbler.session.BBWPProperties;

public class TemplateWrapper {

    public static final String DEVICE_PACKAGE = "blackberry.web.widget";
    
    private BBWPProperties      _bbwpProperties;
    private Hashtable<String, TemplateFile> _templates;
    private File                _root;
    
    public TemplateWrapper(BBWPProperties bbwpProperties) {
        _bbwpProperties = bbwpProperties;
        _templates = new Hashtable<String, TemplateFile>();
        _root = new File(_bbwpProperties.getTemplateDir());
        initTemplates(_root);
    }
        
    public List<String> writeAllTemplates(String toDirectory)
            throws IOException {
        List<String> result = new ArrayList<String>();

        Enumeration<String> e = _templates.keys();
        while (e.hasMoreElements()) {
            // Populate destination path
            TemplateFile df = _templates.get(e.nextElement());
            String strOutputFile = toDirectory
                    + System.getProperty("file.separator") + df.getName();
            result.add(strOutputFile);

            // Create directory
            String strDirectory = strOutputFile.substring(0, strOutputFile
                    .lastIndexOf(System.getProperty("file.separator")));
            File dir = new File(strDirectory);
            dir.mkdirs();

            // Copy file
            OutputStream os = new FileOutputStream(strOutputFile);
            os.write(df.getContents().getBytes());
            os.close();
        }
        return result;
    }
    
    private void initTemplates(File f) {
        if (f.isDirectory()) {
            String[] children = f.list();
            for (int i = 0; i < children.length; i++) {
                initTemplates(new File(f, children[i]));
            }
        } else {
            String relativePath;
            relativePath = f.getAbsolutePath().substring(
                    _root.getAbsolutePath().length() + 1);
            _templates.put(relativePath, new TemplateFile(f
                    .getAbsolutePath(), relativePath));
        }
    }   
}
