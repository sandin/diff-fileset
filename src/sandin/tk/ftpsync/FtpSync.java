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


/**
 * 根据git-diff生成patch文件, 新建一个lftp的脚本用来差异上传本地修改集到服务器
 * 
 * 生成git-diff文件:
 * git diff master^  > change.diff
 * 更多方法参考 git help diff
 * 
 * 运行该脚本:
 * java -jar ftpsync.jar
 * 
 * 根据提示依次输入:
 *  - diff文件路径
 *  - FTP服务器地址(需制定帐号密码, 如: ftp://username:password@127.0.0.1)
 *  - 需要上传到服务器的本地文件夹根目录(绝对路径)
 *  - 需要与本地同步的远程服务器绝对路径
 * 
 * 将会生成的脚本格式:
 * ---------------------------------------------------
 * # Create on 2011-03-14_14-28-59 from change1.diff
 * 
 * debug -o lftp-log-2011-03-14_14-28-59 10
 * connect ftp://ftp-user-lds:123@127.0.0.1
 *
 * cd /wwwroot
 * lcd /home/lds/workspace/java/ftpsync/
 *
 * mput -d .change.diff.swp
 * mput -d bin/sandin/tk/ftpsync/FtpSync.class
 * mput -d change.diff
 * mput -d filelist.txt
 * mput -d lftp-log-2011-03-14_14:16:40
 * mput -d lftp.log
 * mput -d lftp.script
 * mput -d src/sandin/tk/ftpsync/FtpSync.java
 * 
 * TODO: src文件夹在diff文件夹的路径要剔除出来
 * ---------------------------------------------------
 *
 * @author lds
 *
 */
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
		    // Parse git-diff file
			ArrayList<Entity> data = parseFile(file);
			
			// Create lftp script file
			FileWriter fw = new FileWriter("./lftp.script");
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

	
	/**
	 * Parse git-diff file 
	 * 
	 * @param diffFile git-diff file
	 * @return ArrayList<Entity> changed files list
	 * @throws IOException when can read the input file
	 */
	public static ArrayList<Entity> parseFile(File diffFile) throws IOException {
		ArrayList<Entity> entities = new ArrayList<Entity>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		while ( br.ready() ) {
			String line = br.readLine();
			if ( !line.startsWith("diff --git") ) {
				continue; // skip
			}
			
			String line_mode = br.readLine(); // the line under the 'diff --gif' 
			
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

/**
 * File entity
 * 
 * @author lds
 */
class Entity { private static Logger logger = Logger.getLogger(FtpSync.class);
	
	public static final int NOCHANGE = 0;
	public static final int CREATE = 1;
	public static final int DELETE = 2;
	public static final int MODIFY = 3;
	public static final int MOVE = 4;
	
	private File file = null;
	private String filename = "";
	private int changedType = NOCHANGE;
	
	public Entity(String filename) {
		this.filename = filename.replaceAll("\\\\", "/"); // replace all "\" to "/" 
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
