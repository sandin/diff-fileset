package sandin.tk.ftpsync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;


public class FtpSync {
	private static final String TAG = "FtpSync";
	private static Logger logger = Logger.getLogger(FtpSync.class);
	
	private static File file = null;
	
	public static void main(String[] args) {
		
	    Scanner in = new Scanner(System.in);
	    
	    System.out.println("Put your diff file:");
	    String filename = in.nextLine();
	    
	    System.out.println("Put FTP server URL:");
        String ftpUrl = in.nextLine();
	    
	    System.out.println("Put your local dir path:");
        String localDir = in.nextLine();
        
        System.out.println("Put the server dir path:");
        String serverDir = in.nextLine();
        
		
		file = new File(filename);
		if (! file.exists()) {
			System.out.println(filename + " is not exists.");
			System.exit(1);
		}
		
		try {
			ArrayList<Entity> data = parseFile(file);
			FileWriter fw = new FileWriter("./filelist.txt");
			
			String now = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
			fw.write("# Create on " + now + " from " + file.getName() + "\n\n" );
			fw.write("debug -o lftp-log-" + now +" 10"  + "\n");
			fw.write("connect " + ftpUrl + "\n\n");
			fw.write("cd " + serverDir + "\n");
			fw.write("lcd " + localDir + "\n\n");
			
			for ( Entity e : data) {
			    logger.debug("Write a line: " + e.getFilename());
				fw.write("mput -d " + e.getFilename() + "\n");
			}
			fw.close();
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
	}

	
	public static ArrayList<Entity> parseFile(File diffFile) throws IOException {
		ArrayList<Entity> entities = new ArrayList<Entity>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		while ( br.ready() ) {
			String line = br.readLine();
			if ( !line.startsWith("diff --git") ) {
				continue; // skip
			}
			
			String line_mode = br.readLine(); // diff --gif
			
			Pattern pattern = Pattern.compile("diff --git ./([^ ]*)");
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				String filename = matcher.group(1);
				
				Entity entity = new Entity(filename);
				entity.setChangedType(Entity.MODIFY);
				entities.add(entity);
			}
		}
		
		return entities;
	}
}

class Entity {
	private static Logger logger = Logger.getLogger(FtpSync.class);
	
	public static final int NOCHANGE = 0;
	public static final int CREATE = 1;
	public static final int DELETE = 2;
	public static final int MODIFY = 3;
	public static final int MOVE = 4;
	
	private File file = null;
	private String filename = "";
	private int changedType = NOCHANGE;
	
	public Entity(String filename) {
		this.filename = filename.replaceAll("\\\\", "/"); // replace "\" to "/" 
		this.file = new File(filename);
		
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

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	
	
}
