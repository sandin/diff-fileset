package sandin.tk.ftpsync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;


public class FtpSync {
	private static final String TAG = "FtpSync";
	private static Logger logger = Logger.getLogger(FtpSync.class);
	
	private static File file = null;
	
	public static void main(String[] args) {
		
		if (args.length < 2) {
			System.out.println("filename/rootPath is null");
			System.exit(1);
		}
			
		String filename = args[0];
		String rootPath = args[1];
		
		// 系统路径分隔符
		Properties properties = System.getProperties();
		final String DS = properties.getProperty("file.separator");
		
		file = new File(filename);
		if (! file.exists()) {
			System.out.println(filename + " is not exists.");
			System.exit(1);
		}
		
		try {
			ArrayList<Entity> data = parseFile(file, rootPath);
			
	
			FileWriter fw = new FileWriter("./filelist.txt");
			
			for ( Entity e : data) {
				fw.write(e.getFilename() + "\n");
			}
			fw.close();
			

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
	}
	
	public static ArrayList<Entity> parseFile(File diffFile, String rootPath) throws IOException {
		ArrayList<Entity> entities = new ArrayList<Entity>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		while ( br.ready() ) {
			String line = br.readLine();
			if ( !line.startsWith("diff --git") ) {
				continue; // skip
			}
			
			String line_mode = br.readLine(); // diff --gif 下面总至少还有一行
			
			Pattern pattern = Pattern.compile("diff --git ./([^ ]*)");
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				String filename = matcher.group(1);
				
				Entity entity = new Entity(rootPath + "/" + filename);
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
