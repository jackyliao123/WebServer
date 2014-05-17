import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.codehaus.janino.ClassBodyEvaluator;
import org.codehaus.janino.SimpleCompiler;

public abstract class Webpage{
	public static File file;
	public static int fileBufferSize;
	public static void writePageContent(RequestThread thread, String requestMethod, HashMap<String, String> requestHeader, HashMap<String, String> postData, String request, OutputStream o, InetAddress address){
		try {
			File f = new File(file, request);
			
			if(!f.exists()){
				String message = "File not found";
				ArrayList<String> headers = Webpage.getPageHeader("404 Not Found", "text/html", message.length());
				for(String header : headers){
					o.write((header + "\r\n").getBytes());
				}
				o.write("\r\n".getBytes());
				o.write(message.getBytes());
				return;
			}
			
			if(f.getName().endsWith(".jht")){
				StringBuilder builder = new StringBuilder(Utils.loadFile(f));
				String headers = "";
				ClassBodyEvaluator e = new ClassBodyEvaluator();
				
				String htmlCode = "200 OK";
				String contentType = "text/html";
				
				String classBody = "";
				int methodNo = 0;
				
				while(builder.toString().contains("//")){
					int begin = builder.indexOf("//");
					int end = builder.indexOf("\n", begin);
					builder.delete(begin, end + 1);
				}
				while(builder.toString().contains("/*")){
					int begin = builder.indexOf("/*");
					int end = builder.indexOf("*/", begin);
					builder.delete(begin, end + 2);
				}
				while(builder.toString().contains("?#load ")){
					int begin = builder.indexOf("?#load ");
					int end = builder.indexOf("#?", begin);
					builder.replace(begin, end + 2, Utils.loadFile(new File(file, builder.substring(begin + 7, end))));
				}
				while(builder.toString().contains("?#loadjava ")){
					int begin = builder.indexOf("?#loadjava ");
					int end = builder.indexOf("#?", begin);
					SimpleCompiler compiler = new SimpleCompiler();
					compiler.cook(Utils.loadFile(new File(file, builder.substring(begin + 11, end))));
					builder.delete(begin, end + 2);
				}
				while(builder.toString().contains("//")){
					int begin = builder.indexOf("//");
					int end = builder.indexOf("\n", begin);
					builder.delete(begin, end + 1);
				}
				while(builder.toString().contains("/*")){
					int begin = builder.indexOf("/*");
					int end = builder.indexOf("*/", begin);
					builder.delete(begin, end + 2);
				}
				if(builder.toString().contains("?class{")){
					int begin = builder.indexOf("?class{");
					int end = builder.indexOf("}c?", begin);
					StringBuilder s = new StringBuilder(builder.substring(begin + 7, end).trim());
					int sBegin = s.indexOf("#name ");
					int sEnd = s.indexOf("\n", sBegin);
					if(sBegin != -1){
						e.setClassName(s.substring(sBegin + 7, sEnd).trim());
						s.delete(sBegin, sEnd + 1);
					}
					e.setDefaultImports((s.toString() + "\njava.util.*\njava.io.*\njava.net.*").trim().split("\n"));
					builder.delete(begin, end + 3);
				}
				else{
					e.setDefaultImports(new String[]{"java.util.*", "java.io.*", "java.net.*"});
				}
				classBody += 
						"static String requestMethod;" +
						"static String requestPath;" +
						"static HashMap<String, String> requestData;" +
						"static HashMap<String, String> requestHeaders;" +
						"static OutputStream serverOutput;" +
						"static InetAddress ipAddress;";
				if(builder.toString().contains("?java{")){
					int begin = builder.indexOf("?java{");
					int end = builder.indexOf("}j?", begin);
					classBody += builder.substring(begin + 6, end).trim();
					builder.delete(begin, end + 3);
				}
				while(builder.toString().contains("?{")){
					int begin = builder.indexOf("?{");
					int end = builder.indexOf("}?", begin);
					int nBegin = builder.indexOf("?#currentMethod?");
					while(nBegin > 0 && nBegin < end){
						builder.replace(nBegin, nBegin + 16, "javaMethod" + methodNo);
						nBegin = builder.indexOf("?#currentMethod?");
					}
					classBody += "public static Object javaMethod" + methodNo + "(){" + builder.substring(begin + 2, end) + "}";
					builder.replace(begin, end + 2, "$#javaMethod" + methodNo + "#$");
					++ methodNo;
				}
				e.cook(classBody);
				Class<?> clazz = e.getClazz();
				
				Field modifierField = Field.class.getDeclaredField("modifiers");
				modifierField.setAccessible(true);
				int modifier = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
				
				Field field = clazz.getDeclaredField("requestMethod");
				field.setAccessible(true);
				field.set(null, requestMethod);
				modifierField.setInt(field, modifier);
				
				field = clazz.getDeclaredField("requestPath");
				field.setAccessible(true);
				field.set(null, request);
				modifierField.setInt(field, modifier);
				
				field = clazz.getDeclaredField("requestData");
				field.setAccessible(true);
				field.set(null, postData);
				modifierField.setInt(field, modifier);
				
				field = clazz.getDeclaredField("requestHeaders");
				field.setAccessible(true);
				field.set(null, requestHeader);
				modifierField.setInt(field, modifier);
				
				field = clazz.getDeclaredField("serverOutput");
				field.setAccessible(true);
				field.set(null, o);
				modifierField.setInt(field, modifier);
				
				field = clazz.getDeclaredField("ipAddress");
				field.setAccessible(true);
				field.set(null, address);
				modifierField.setInt(field, modifier);
				
				for(int i = 0; i < methodNo; ++i){
					String methodName = "javaMethod" + i;
					int index = builder.indexOf("$#" + methodName + "#$");
					builder.replace(index, index + methodName.length() + 4, clazz.getMethod(methodName).invoke(null).toString());
				}
				if(builder.toString().contains("?response{")){
					int begin = builder.indexOf("?response{");
					int end = builder.indexOf("}r?", begin);
					String[] header = builder.substring(begin + 10, end).trim().split("\n");
					builder.delete(begin, end + 3);
					for(String h : header){
						h = h.trim();
						if(h.startsWith("#code "))
							htmlCode = h.substring(6);
						else if(h.startsWith("#type "))
							contentType = h.substring(6);
						else if(h.startsWith("#connection ")){
							String connectionType = h.substring(12);
							if(connectionType.equalsIgnoreCase("keep-alive")){
								headers += "Connection: Keep-Alive\r\n";
								thread.keepAliveConnection = true;
							}
							else if(connectionType.equalsIgnoreCase("close")){
								headers += "Connection: Close\r\n";
								thread.keepAliveConnection = false;
							}
							else if(connectionType.equalsIgnoreCase("default")){
								String value = requestHeader.get("Connection");
								if(value != null){
									if(value.equalsIgnoreCase("Keep-Alive"))
										thread.keepAliveConnection = true;
									else
										thread.keepAliveConnection = false;
								}
							}
						}
						else if(h.startsWith("#notext"))
							builder.delete(0, builder.length());
						else
							headers += h + "\r\n";
					}
				}
				
				String document = builder.toString().trim();
				
				ArrayList<String> header = getPageHeader(htmlCode, contentType, document.length());
				for(String s : header){
					o.write((s + "\r\n").getBytes());
				}
				if(headers.length() > 0)
					o.write(headers.getBytes());
				o.write("\r\n".getBytes());
				o.write(document.getBytes());
				return;
			}
			
			long startByte = 0;
			long endByte = f.length() - 1;
			boolean partialContent = false;
			boolean updateContent = true;
			
			if(requestHeader.containsKey("Range")){
				String[] range = requestHeader.get("Range").split("=")[1].split("-");
				startByte = Long.parseLong(range[0]);
				if(range.length == 2){
					endByte = Long.parseLong(range[1]);
				}
				partialContent = true;
			}
			
			if(requestHeader.containsKey("If-Modified-Since")){
				if(Utils.parseDate(requestHeader.get("If-Modified-Since")) > f.lastModified()){
					requestMethod = "HEAD";
					updateContent = false;
				}
			}
			
			ArrayList<String> header = Webpage.getPageHeader(updateContent ? (partialContent ? "206 Partial Content" : "200 OK") : "304 Not Modified", MimeType.get(f.getName()), updateContent ? endByte - startByte + 1 : 0);
			if(partialContent){
				header.add("Content-Range: bytes " + startByte + "-" + endByte + "/" + f.length());
			}
			for(String s : header){
				o.write((s + "\r\n").getBytes());
			}
			o.write("\r\n".getBytes());
			if(requestMethod.equals("HEAD")){
				return;
			}
			FileInputStream finput = new FileInputStream(f);
			finput.skip(startByte);
			long bytesRead = 0;
			int arraySize = (int)Math.min(4096, endByte - startByte + 1);
			byte[] b = new byte[arraySize];
			int writeLength;
			while(bytesRead < endByte - startByte + 1){
				writeLength = (int)(Math.min(arraySize, endByte - startByte + 1 - bytesRead));
				bytesRead += finput.read(b, 0, writeLength);
				o.write(b, 0, writeLength);
			}
			finput.close();
		}
		catch(IOException e){
		}
		catch (Exception e) {
			e.printStackTrace(new PrintStream(o));
			e.printStackTrace();
		}
	}
	public static ArrayList<String> getPageHeader(String code, String contentType, long contentLength){
		ArrayList<String> list = new ArrayList<String>();
		list.add("HTTP/1.1 " + code);
		list.add("Date: " + new Date().toString());
		list.add("Content-Type: " + contentType);
		list.add("Content-Length: " + contentLength);
		list.add("Last-Modified: " + new Date());
		return list;
	}
}