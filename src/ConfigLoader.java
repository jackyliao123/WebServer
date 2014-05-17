import java.io.File;

public class ConfigLoader{
	String[] lines;
	public ConfigLoader(File f){
		try{
			if(!f.exists()){
				f.createNewFile();
				Utils.saveToFile(f, 
						"server-directory=website\r\n" +
						"server-log=connections.log\r\n\r\n" +
						
						"file-buffer-size=4096\r\n" +
						"default-connection=keep-alive\r\n\r\n" +
						
						"http=true\r\n" +
						"http-port=80\r\n" +
						"http-max-thread=-1\r\n\r\n" +
						
						"https=false\r\n" +
						"https-port=443\r\n" +
						"https-max-thread=-1\r\n" +
						"https-keystore=\r\n" +
						"https-keystore-pass=");
			}
			lines = Utils.loadFile(f).split("\n");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	public boolean loadBoolean(String name, boolean def){
		for(String s : lines){
			if(s.split("=")[0].trim().equals(name)){
				return Boolean.parseBoolean(s.split("=")[1].trim());
			}
		}
		System.err.println("Property " + name + " not found");
		return def;
	}
	public int loadInt(String name, int def){
		for(String s : lines){
			if(s.split("=")[0].trim().equals(name)){
				return Integer.parseInt(s.split("=")[1].trim());
			}
		}
		System.err.println("Property " + name + " not found");
		return def;
	}
	public String loadString(String name, String def){
		for(String s : lines){
			if(s.split("=")[0].trim().equals(name)){
				return s.split("=")[1].trim();
			}
		}
		System.err.println("Property " + name + " not found");
		return def;
	}
}