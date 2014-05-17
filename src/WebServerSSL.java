import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class WebServerSSL extends WebServer{
	public WebServerSSL(int port, File keystore, String password){
		try{
			KeyStore ks = KeyStore.getInstance("JKS");
			
			ks.load(new FileInputStream(keystore), password.toCharArray());
	
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, password.toCharArray());
	
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ks);
	
			SSLContext sc = SSLContext.getInstance("TLS"); 
			TrustManager[] trustManagers = tmf.getTrustManagers(); 
			sc.init(kmf.getKeyManagers(), trustManagers, null);
			
			SSLServerSocketFactory sslserversocketfactory = sc.getServerSocketFactory();
			serverSocket = sslserversocketfactory.createServerSocket(port);
			running = true;
			System.out.println("HTTPS server started on port " + port);
		}
		catch(Exception e){
			System.err.println("HTTPS server failed to start");
			e.printStackTrace();
		}
	}
}