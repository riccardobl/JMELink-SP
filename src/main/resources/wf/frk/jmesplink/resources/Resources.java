package wf.frk.jmesplink.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Resources{
	public static byte[] getAsBytes(String path) throws IOException{
		InputStream is=Resources.class.getResource(path).openStream();
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		byte chunk[]=new byte[1024*1024];
		int readed;
		while((readed=is.read(chunk))!=-1){
			bos.write(chunk,0,readed);
		}
		is.close();
		byte out[]=bos.toByteArray();
		bos.close();
		return out;
	}
}
