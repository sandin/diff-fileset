package sandin.tk.ftpsync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

class GitDiff {
    
    private Logger logger = Logger.getLogger(GitDiff.class);
    
    private File mFile;
    private File mBaseDir;
    private File mOutput;

    /**
     * @param file patch file Generate by GIF
     * @param baseDir GIT repository root directory
     * @param output Directory of Output files 
     */
    public GitDiff(File file, File baseDir, String output) {
        mFile = file;
        mBaseDir = baseDir;
        mOutput = new File(output);
        if (! mOutput.exists()) {
            mOutput.mkdirs();
        }
    }

    public boolean start() throws IOException {
        return startCopyFiles();
    }
    
    private boolean startCopyFiles() throws IOException {
        ArrayList<Entity> data = parseFile(mFile);

        FileWriter fw = new FileWriter("./diff_fileset.txt");

        for (Entity e : data) {
            File srcFile = e.getFile();
            File destFile = new File(mOutput.getAbsolutePath() + "/" 
                                     + e.getFilename());
            FileUtils.copyFile(srcFile, destFile);
            
            fw.write(e.getFilename() + "\n");
            logger.debug("Copy: " + srcFile.getAbsoluteFile() + " -> " 
                       + destFile.getAbsoluteFile());
        }
        fw.close();
        
        Collection<File> files =  FileUtils.listFiles(mOutput, null, true);
        if (files.size() != data.size()) {
            logger.error("Missing copy some files.");
        }
        logger.debug("Find files :" + data.size() + " , Copy files : " + files.size());
        
        return true;
    }

    
    /**
     * Parse GIT DIFF file 
     * 
     * @param diffFile GIT DIFF file
     * @return ArrayList<Entity> changed files list
     * @throws IOException When cann't read the input file
     */
    public ArrayList<Entity> parseFile(File diffFile) throws IOException {
        ArrayList<Entity> entities = new ArrayList<Entity>();
        BufferedReader br = new BufferedReader(new FileReader(mFile));
        
        while ( br.ready() ) {
            String line = br.readLine();
            if ( !line.startsWith("diff --git") ) {
                continue; // skip
            }
            String changeMode = br.readLine(); // M , D , A
            
            Pattern pattern = Pattern.compile("diff --git ./([^ ]*)");
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String filename = matcher.group(1); 
                Entity entity = new Entity(mBaseDir.getAbsolutePath(), filename);
                entity.setChangedType(Entity.MODIFY);
                entities.add(entity);
            }
        }
        
        return entities;
    }
}
