import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;

public class UserManager {
	private final File file;
	public UserManager(File file){
		this.file = file;
	}
	public synchronized boolean checkUserPassword(String username, String password){
		try{
			DataInputStream input = new DataInputStream(new FileInputStream(file));
			boolean verified = false;
			int count = input.readInt();
			for(int i = 0; i < count; i ++){
				MessageDigest digest = MessageDigest.getInstance("MD5");
				String verUser = input.readUTF();
				input.readUTF();
				byte[] b = digest.digest(password.getBytes());
				int passCount = input.readInt();
				byte[] storedByte = new byte[passCount];
				input.readFully(storedByte);
				if(!verUser.equalsIgnoreCase(username))
					continue;
				if(b.length != storedByte.length)
					continue;
				boolean congruent = true;
				for(int j = 0; j < passCount; j ++){
					if(b[j] != storedByte[j]){
						congruent = false;
						break;
					}
				}
				if(congruent){
					verified = true;
					username = verUser;
				}
			}
			input.close();
			return verified;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	public synchronized String getUsernameFromEmail(String email){
		try{
			DataInputStream input = new DataInputStream(new FileInputStream(file));
			int count = input.readInt();
			for(int i = 0; i < count; i ++){
				String user = input.readUTF();
				String emRead = input.readUTF();
				if(emRead.equalsIgnoreCase(email)){
					return user;
				}
				
				byte[] b = new byte[input.readInt()];
				input.readFully(b);
			}
			input.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	public synchronized String getUsernameFromName(String username){
		try{
			DataInputStream input = new DataInputStream(new FileInputStream(file));
			int count = input.readInt();
			for(int i = 0; i < count; i ++){
				String user = input.readUTF();
				if(user.equalsIgnoreCase(username)){
					return user;
				}
				
				input.readUTF();
				byte[] b = new byte[input.readInt()];
				input.readFully(b);
			}
			input.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	public synchronized void userChangePassword(String username, String newPassword){
		try{
			DataInputStream input = new DataInputStream(new FileInputStream(file));
			int userCount = input.readInt();
			ArrayList<String> us = new ArrayList<String>();
			ArrayList<String> em = new ArrayList<String>();
			ArrayList<byte[]> pa = new ArrayList<byte[]>();
			for(int i = 0; i < userCount; i ++){
				us.add(input.readUTF());
				em.add(input.readUTF());
				byte[] b = new byte[input.readInt()];
				input.readFully(b);
				pa.add(b);
			}
			input.close();
			DataOutputStream output = new DataOutputStream(new FileOutputStream(file));
			output.writeInt(userCount);
			for(int i = 0; i < userCount; i ++){
				output.writeUTF(us.get(i));
				output.writeUTF(em.get(i));
				if(us.get(i).equalsIgnoreCase(username)){
					MessageDigest digest = MessageDigest.getInstance("MD5");
					byte[] b = digest.digest(newPassword.getBytes());
					output.writeInt(b.length);
					output.write(b);
				}
				else{
					output.writeInt(pa.get(i).length);
					output.write(pa.get(i));
				}
			}
			output.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	public synchronized void userChangeEmail(String username, String newEmail){
		try{
			DataInputStream input = new DataInputStream(new FileInputStream(file));
			int userCount = input.readInt();
			ArrayList<String> us = new ArrayList<String>();
			ArrayList<String> em = new ArrayList<String>();
			ArrayList<byte[]> pa = new ArrayList<byte[]>();
			for(int i = 0; i < userCount; i ++){
				us.add(input.readUTF());
				em.add(input.readUTF());
				byte[] b = new byte[input.readInt()];
				input.readFully(b);
				pa.add(b);
			}
			input.close();
			DataOutputStream output = new DataOutputStream(new FileOutputStream(file));
			output.writeInt(userCount);
			for(int i = 0; i < userCount; i ++){
				output.writeUTF(us.get(i));
				if(us.get(i).equalsIgnoreCase(username)){
					output.writeUTF(newEmail);
				}
				else{
					output.writeUTF(em.get(i));
				}
				output.writeInt(pa.get(i).length);
				output.write(pa.get(i));
			}
			output.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	public synchronized void deleteUser(String username){
		try{
			DataInputStream input = new DataInputStream(new FileInputStream(file));
			int userCount = input.readInt();
			ArrayList<String> us = new ArrayList<String>();
			ArrayList<String> em = new ArrayList<String>();
			ArrayList<byte[]> pa = new ArrayList<byte[]>();
			boolean contains = false;
			for(int i = 0; i < userCount; i ++){
				us.add(input.readUTF());
				if(us.get(i).equalsIgnoreCase(username))
					contains = true;
				em.add(input.readUTF());
				byte[] b = new byte[input.readInt()];
				input.readFully(b);
				pa.add(b);
			}
			input.close();
			DataOutputStream output = new DataOutputStream(new FileOutputStream(file));
			output.writeInt(userCount - (contains ? 1 : 0));
			for(int i = 0; i < userCount; i ++){
				if(us.get(i).equalsIgnoreCase(username))
					continue;
				output.writeUTF(us.get(i));
				output.writeUTF(em.get(i));
				output.writeInt(pa.get(i).length);
				output.write(pa.get(i));
			}
			output.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	public synchronized void registerUser(String username, String email, String password){
		try{
			DataInputStream input = new DataInputStream(new FileInputStream(file));
			int userCount = input.readInt();
			ArrayList<String> us = new ArrayList<String>();
			ArrayList<String> em = new ArrayList<String>();
			ArrayList<byte[]> pa = new ArrayList<byte[]>();
			for(int i = 0; i < userCount; i ++){
				us.add(input.readUTF());
				em.add(input.readUTF());
				byte[] b = new byte[input.readInt()];
				input.readFully(b);
				pa.add(b);
			}
			input.close();
			DataOutputStream output = new DataOutputStream(new FileOutputStream(file));
			output.writeInt(userCount + 1);
			for(int i = 0; i < userCount; i ++){
				output.writeUTF(us.get(i));
				output.writeUTF(em.get(i));
				output.writeInt(pa.get(i).length);
				output.write(pa.get(i));
			}
			output.writeUTF(username);
			output.writeUTF(email);
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] b = digest.digest(password.getBytes());
			output.writeInt(b.length);
			output.write(b);
			output.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
