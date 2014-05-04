package net.jackyliao123.webserver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;

import sun.awt.shell.ShellFolder;

public class PageFile extends Webpage{
	public static File root = new File("files");
	public PageFile(){
		if(!root.exists()) {
			root = new File("./files");
			root.mkdir();
		}
	}
	public String getPageName(){
		return "/file";
	}
	public ResponseParams getHeader(String req, String[] reqParams, String method, String postData, InetAddress address){
		File f = new File(root, req);
		if(req.startsWith("/download")){
			f = new File(root, req.replaceFirst("/download", ""));
		}
		if(!f.exists()){
			return new ResponseParams(getPageHeader("404 Not Found", "text/html", -1), "The specified file was not found");
		}
		if(!f.getAbsolutePath().contains(root.getAbsolutePath())){
			return new ResponseParams(getPageHeader("403 Forbidden", "text/html", -1), "You do not have permission to access this file");
		}
		if(f.isDirectory()){
			try{
				File[] files = f.listFiles();
				String body = "";
				body += "<div id=\"indent\">";
				int dirs = 0;
				int fils = 0;
				for(int i = 0; i < files.length; i ++){
					if(files[i].isDirectory()){
						String relativePath = files[i].getAbsolutePath().replace(root.getAbsolutePath(), "").replace("\\", "/");
						String actualPath = (files[i].getName().toLowerCase().endsWith(".lnk") ? ShellFolder.getShellFolder(files[i]).getLinkLocation().getAbsolutePath().replace(root.getAbsolutePath(), "").replace("\\", "/") : relativePath);
						body += "<img src=\"/icon" + relativePath + "\" width=\"16\" height=\"16\" alt=\"<folder>\"><a href=\"/file" + actualPath + "\">" + files[i].getName() + "</a><br>";
						dirs ++;
					}
				}
				for(int i = 0; i < files.length; i ++){
					if(!files[i].isDirectory()){
						String relativePath = files[i].getAbsolutePath().replace(root.getAbsolutePath(), "").replace("\\", "/");
						String actualPath = (files[i].getName().toLowerCase().endsWith(".lnk") ? ShellFolder.getShellFolder(files[i]).getLinkLocation().getAbsolutePath().replace(root.getAbsolutePath(), "").replace("\\", "/") : relativePath);
						body += "&lt;<a href=\"/file/download" + actualPath + "\">Download</a>&gt; <img src=\"/icon" + relativePath + "\" width=\"16\" height=\"16\" alt=\"\"><a href=\"/file" + actualPath + "\">" + files[i].getName() + "</a><br>";
						fils ++;
					}
				}
				body += "</div><div id=\"footer\"><div id=\"spacing\">" + dirs + " folders, " + fils + " files</div></div>";
				return new ResponseParams(getPageHeader("200 OK", "text/html", -1), new Object[]{getPageCode(req.equals("/") ? "File Server" : req.substring(1), null, "", body, address)});
			}
			catch(NullPointerException e){
				return new ResponseParams(getPageHeader("403 Forbidden", "text/html", -1), "You do not have permission to access this file");
			}
			catch (FileNotFoundException e){
				return new ResponseParams(getPageHeader("404 Not Found", "text/html", -1), "The specified file was not found");
			}
		}
		else{
			long startByte = 0;
			long endByte = f.length() - 1;
			boolean partialContent = false;
			for(int i = 0; i < reqParams.length; i ++){
				if(reqParams[i].startsWith("Range: bytes=")){
					startByte = Long.parseLong(reqParams[i].split("=")[1].split("-")[0]);
					if(reqParams[i].split("=")[1].split("-").length > 1){
						endByte = Long.parseLong(reqParams[i].split("=")[1].split("-")[1]);
					}
					partialContent = true;
				}
			}
			if(!req.startsWith("/download")){
				ArrayList<String> params = getPageHeader(partialContent ? "206 Partial Content" : "200 OK", MimeType.get(f.getName()), endByte - startByte + 1);
				if(partialContent)
					params.add("Content-Range: bytes " + startByte + "-" + endByte + "/" + f.length());
				return new ResponseParams(params, new Object[]{f, startByte, endByte});
			}
			else{
				ArrayList<String> params = getPageHeader(partialContent ? "206 Partial Content" : "200 OK", "application/octet-stream", endByte - startByte + 1);
				if(partialContent)
					params.add("Content-Range: bytes " + startByte + "-" + endByte + "/" + f.length());
				return new ResponseParams(params, new Object[]{f, startByte, endByte});
			}
		}
	}
	public void writePageContent(ResponseParams param, String request, OutputStream o, InetAddress address){
		try {
			Object data = param.getParam()[0];
			if(data instanceof String){
				o.write(((String)data).getBytes());
				o.close();
			}
			else if(data instanceof File){
				FileInputStream finput = new FileInputStream((File)data);
				long startByte = (Long)param.getParam()[1];
				long endByte = (Long)param.getParam()[2];
				finput.skip(startByte);
				long bytesRead = 0;
				int arraySize = (int)Math.min(2147483645L, endByte - startByte + 1);
				byte[] b = new byte[arraySize];
				int writeLength;
				while(bytesRead < endByte - startByte + 1){
					writeLength = (int)(Math.min(arraySize, endByte - startByte + 1 - bytesRead));
					bytesRead += finput.read(b, 0, writeLength);
					o.write(b, 0, writeLength);
				}
				finput.close();
				o.close();
			}
		}
		catch (IOException e) {
		}
	}
}