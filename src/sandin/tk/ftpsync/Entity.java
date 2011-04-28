package sandin.tk.ftpsync;

import java.io.File;

import org.apache.log4j.Logger;

/**
 * File entity
 * 
 * @author lds
 */
class Entity {
    private static Logger logger = Logger.getLogger(Entity.class);
    
    public static final int NOCHANGE = 0;
    public static final int CREATE = 1;
    public static final int DELETE = 2;
    public static final int MODIFY = 3;
    public static final int MOVE = 4;
    
    private File file = null;
    private String filename = ""; // absolute
    private int changedType = NOCHANGE;
    
    public Entity(String dir, String filename) {
        this.filename = filename.replaceAll("\\\\", "/"); // replace all "\" to "/" 
        this.file = new File(dir + "/" + filename);
        
        if (! file.exists()) {
            logger.error( filename + " is not exists");
        }
    }

    public int getChangedType() {
        return changedType;
    }

    public void setChangedType(int changedType) {
        this.changedType = changedType;
    }
    
    public File getFile() {
        return file;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
    
}
