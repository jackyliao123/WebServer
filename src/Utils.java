import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Utils {
	public static String loadFile(File f) throws IOException{
		InputStream input = new FileInputStream(f);
		int length = (int)f.length();
		byte[] b = new byte[length];
		int bytesRead = 0;
		while(bytesRead < length){
			bytesRead += input.read(b, bytesRead, length - bytesRead);
		}
		input.close();
		return new String(b);
	}
	public static void saveToFile(File f, String s) throws IOException{
		OutputStream output = new FileOutputStream(f);
		output.write(s.getBytes());
		output.close();
	}
	public static long parseDate(String date) throws ParseException{
		try {
			SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
			return format.parse(date).getTime();
		}
		catch (ParseException e) {
			try{
				SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
				return format.parse(date).getTime();
			}
			catch(ParseException ex){
				SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
				return format.parse(date).getTime();
			}
		}
	}
}