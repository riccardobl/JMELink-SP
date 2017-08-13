package wf.frk.jmesplink;

import java.io.File;

public class PathUtils{
	public static String toNative(String path){
		return path.replace("/",File.separator);
	}
	
	public static String toVirtual(String path){
		return path.replaceAll(File.separator,"/");
	}
	
	public static String toFileUrl(String path){
		path=toVirtual(path);
		if(!path.startsWith("/"))path="/"+path;
		return  "file://"+(path);
	}
	public static String toNativeDir(String path){
		if(!path.endsWith(File.separator))path=path+File.separator;
		return path;
	}
	
	public static String toVirtualDir(String path){
		if(!path.endsWith("/"))path=path+"/";
		return path;
	}
}
