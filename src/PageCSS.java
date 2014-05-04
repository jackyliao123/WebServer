import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.InetAddress;

public class PageCSS extends Webpage{
	private File cssRoot = new File("stylesheets");
	public String getPageName() {
		return "/css";
	}
	public String getContentType(){
		return "text/css";
	}
	public long getContentLength(String req){
		if(req.startsWith("/")){
			return new File(cssRoot, req.substring(1)).length();
		}
		return -1;
	}
	public void writePageContent(ResponseParams param, String request, OutputStream o, InetAddress address) {
		try{
			if(request.startsWith("/")){
				File f = new File(cssRoot, request.substring(1));
				FileInputStream finput = new FileInputStream(f);
				long bytesRead = 0;
				int arraySize = (int)Math.min(2147483645L, f.length());
				byte[] b = new byte[arraySize];
				int writeLength;
				while(bytesRead < f.length()){
					writeLength = (int)(Math.min(arraySize, f.length() - bytesRead));
					bytesRead += finput.read(b, 0, writeLength);
					o.write(b, 0, writeLength);
				}
				finput.close();
				o.close();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
