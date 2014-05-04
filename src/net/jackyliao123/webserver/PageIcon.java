package net.jackyliao123.webserver;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;
import java.net.InetAddress;

import javax.imageio.ImageIO;

import sun.awt.shell.ShellFolder;

public class PageIcon extends Webpage{
	public String getPageName(){
		return "/icon";
	}
	public String getContentType(String req){
		return "image/png";
	}
	public void writePageContent(ResponseParams param, String request, OutputStream o, InetAddress address){
		try {
			File f = new File(PageFile.root, request);
			ShellFolder shellFolder = ShellFolder.getShellFolder(f);
			Image icon = shellFolder.getIcon(false);
			BufferedImage image = new BufferedImage(icon.getWidth(null), icon.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			image.getGraphics().drawImage(icon, 0, 0, null);
			ImageIO.write(image, "png", o);
			o.close();
		}
		catch (Exception e) {
		}
	}
}