package wf.frk.jmesplink.link;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import wf.frk.jmesplink.Json;
import wf.frk.jmesplink.PathUtils;

public class SubstanceLink{

	protected String IP;
	protected int PORT;
	protected int RESOLUTION=128;
	//	protected String TEMPLATE;
	protected Json JSON;
	protected TangentSpaceMode TANGENTSPACE_MODE=TangentSpaceMode.PER_FRAGMENT;

	public SubstanceLink(int port,Json json) throws UnknownHostException,IOException{
		this("127.0.0.1",port,json);
	}

	public SubstanceLink(String ip,int port,Json json) throws UnknownHostException,IOException{
		IP=ip;
		PORT=port;
		JSON=json;
	}

	public void createProject(String mesh) throws IOException {
//		mesh=mesh.replace(File.separator,"/");
		String meshurl=PathUtils.toFileUrl(mesh);
		Map<Object,Object> project_settings=new HashMap<Object,Object>();
		project_settings.put("normalMapFormat","OpenGL");
		project_settings.put("tangentSpaceMode",TANGENTSPACE_MODE.mode);
		project_settings.put("resolution",RESOLUTION);
		project_settings.put("splitMaterialsByUDIM",false);
		//		project_settings.put("exportUrl","");		
		cmd("alg.project.close();");
		cmd("alg.project.create('"+meshurl+"',null,null,"+JSON.stringify(project_settings)+");");
	}

	public boolean texturesChanged() throws IOException {
		return (boolean)cmd("texturesChanged();");

	}

	public Map<Object,Object> getShaderSettings() throws IOException {
		return (Map<Object,Object>)cmd("alg.shaders.shaderInstancesToObject();");
	}
	//	
	//	public void exportTextures(String path) throws IOException{
	//		exportTextures(path,null);
	//	}

	public Map<Object,Object> exportTextures(String texture_set[], String preset_path, String path, Integer resolution) throws IOException {
//		preset_path=preset_path.replace(File.separator,"/");

//		path=path.replace(File.separator,"/");

		String map_infoS="null";

		if(resolution!=null){
			//			Map<Object,Object> map_info=new HashMap<Object,Object>();
			//			map_info.put("resolution",resolution);
			map_infoS="{\"resolution\":["+resolution+","+resolution+"]}";
		}

		String stack="null";
		if(texture_set!=null){
			stack="[";
			boolean f=true;
			for(String tx:texture_set){
				if(f){
					f=false;
				}else stack+=",";
				stack+="'"+tx+"'";
			}
			stack+="]";
		}

		Map<Object,Object> out=(Map<Object,Object>)(cmd("alg.mapexport.exportDocumentMaps('"+preset_path+"','"+path+"','png',"+map_infoS+","+stack+",false);"));

		for(Entry e:out.entrySet()){
			for(Entry e1:((Map<Object,Object>)e.getValue()).entrySet()){
				String s=e1.getValue().toString().trim();
				if(!s.isEmpty()){
					s=s.replace(File.separator,"/"); // TODO: This may be not necessary
					int i=s.lastIndexOf("/");
					if(i!=-1) e1.setValue(s.substring(i+1));
				}
			}
		}
		return out;
	}

	
	public void saveProject(String path) throws IOException {
//		path=path.replace(File.separator,"/");
		String url=PathUtils.toFileUrl(path);
		cmd("alg.project.save('"+url+"');");

	}

	public void saveProjectAndClose() throws IOException {
//		path=path.replace(File.separator,"/");
//		String url="file://"+path;
		cmd("alg.project.saveAndClose();");
		while((boolean)cmd("alg.project.isOpen();")){
			try{
				Thread.sleep(100);
			}catch(InterruptedException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void closeProject() throws IOException {
		cmd("alg.project.close();");
	}

	public boolean openProject(String path) throws IOException {
		cmd("alg.project.close();");

//		path=path.replace(File.separator,"/");
		String url=PathUtils.toFileUrl(path);
		cmd("alg.project.open('"+url+"');");
		return cmd("alg.project.url();").toString().equals(url);
	}

	public void echo(EchoType type, String message) throws IOException {
		message=message.replace("\\","\\\\");
		message=message.replace("'","\\'");
		String cmd="('"+message+"');";
		switch(type){
			case INFO:
				cmd="info"+cmd;
				break;
			case ERROR:
				cmd="error"+cmd;
				break;
			case WARNING:
				cmd="warn"+cmd;
				break;
		}
		cmd="alg.log."+cmd;
		cmd(cmd);
	}

	public synchronized Object cmd(String js) throws IOException {
		Map<Object,Object> packet=new HashMap<Object,Object>();
		System.out.println(">>> "+js);

		packet.put("js",Base64.getEncoder().encodeToString(js.getBytes(Charset.forName("UTF-8"))));
		return sendPacket(packet);
	}

	protected Object sendPacket(Map<Object,Object> packet) throws IOException {
		String json=JSON.stringify(packet);
		URL obj=new URL("http://"+IP+":"+PORT+"/run.json");
		HttpURLConnection conn=(HttpURLConnection)obj.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setRequestProperty("Content-type","text/plain");
		conn.setRequestProperty("Accept","application/json");
		conn.setConnectTimeout(0);
		conn.getOutputStream().write(json.getBytes(Charset.forName("UTF-8")));
		conn.getOutputStream().close();

		ByteArrayOutputStream response=new ByteArrayOutputStream();
		int readed;
		byte chunk[]=new byte[1024*1024];
		while((readed=conn.getInputStream().read(chunk))!=-1){
			response.write(chunk,0,readed);
			System.out.println("Read");
		}
		conn.getInputStream().close();
		String response_s=response.toString("UTF-8");
		System.out.println("<<< "+response_s);
		return JSON.parse(response_s);
	}

	public static void main(String[] args) {}
}
