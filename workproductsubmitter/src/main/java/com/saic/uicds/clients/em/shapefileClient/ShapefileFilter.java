package com.saic.uicds.clients.em.shapefileClient;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Hashtable;

/*
 * this class extends the file filter class for ShapeFileSubmitter gui
 * it only accepts file extensions .shz ,doc ,docx ,pdf
 * it is used by javax.swing.JFileChooser
 */
class ShapefileFilter extends FileFilter
{
	/* added xml extension for map layer*/
    private static String description = ".shz .doc .docx .pdf .xml";
    private Hashtable filters = null; 

    public ShapefileFilter(boolean shzOnly)
    {
        this.filters = new Hashtable();

        // if the shapefileonly flag is set, the accept only .shz file
        // and change filter description

        addExtension("shz");

        if (shzOnly == true) {
            description = ".shz";
        }
        else
        {
            // the shapefileonly flag is false, then add the other file type
            addExtension("doc");
            addExtension("docx");
            addExtension("pdf");
            addExtension("xml");
        }
    }

    /*
     * accepts all directories
     * accepts file extensions set in filters
     */
    public boolean accept(File f)
    {
        if(f != null) {         
            if(f.isDirectory()) {        
                return true;         
            }
        }
        String extension = getExtension(f);
        if(extension != null && filters.get(getExtension(f)) != null) {
            return true;         
        }     
        return false;                       
     }

    /*
     * return the description of the file extensions
     */
     public String getDescription()
     {
         return description;
     }

     /*
      * add the extensions to the filters table
      */
     public void addExtension(String extension) {     
         if(filters == null) {         
             filters = new Hashtable(5);     
          }     
         filters.put(extension.toLowerCase(), this);     
     } 

     /*
      * return the extension for the file
      */
     public String getExtension(File f) {     
         if(f != null) {         
             String filename = f.getName();         
             int i = filename.lastIndexOf('.');         
             if(i>0 && i<filename.length()-1) {
                 return filename.substring(i+1).toLowerCase();         
             }     
         }     
         return null;      
     }  
}
