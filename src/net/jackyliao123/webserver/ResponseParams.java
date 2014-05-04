package net.jackyliao123.webserver;
import java.util.ArrayList;

public class ResponseParams{
	private Object[] param = null;
	private ArrayList<String> response;
	private String error;
	public ResponseParams(ArrayList<String> res, Object[] param){
		response = res;
		this.param = param;
	}
	public ResponseParams(ArrayList<String> res, String error){
		response = res;
		this.error = error;
	}
	public boolean noContent(){
		return param == null;
	}
	public String getError(){
		return error;
	}
	public Object[] getParam(){
		return param;
	}
	public ArrayList<String> getResponse(){
		return response;
	}
}