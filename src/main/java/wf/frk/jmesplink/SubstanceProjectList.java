package wf.frk.jmesplink;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import wf.frk.jmesplink.link.SubstanceLink;
import wf.frk.jmesplink.obj.OBJMesh;
import wf.frk.jmesplink.obj.SimpleObjWriter;
import wf.frk.jmesplink.obj.Triangle;
import wf.frk.jmesplink.obj.Vertex;

public class SubstanceProjectList extends HashMap<Object,Object>{
	private transient final String _PROJECTS_FS_PATH;//,_SUBSTANCES_ASSET_PATH,_SUBSTANCES_FS_PATH;
	private transient final SubstanceLinkAppState _LINK;
	private transient final SubstancesList _SLIST;
	private transient final Json _JSON;
	
	@Deprecated
	public SubstanceProjectList() throws IOException{
		this(null,null,null,null);
	}
	
	public SubstanceProjectList(SubstanceLinkAppState link,String projects_fs_path,SubstancesList slist,Json json) throws IOException{
		_LINK=link;
		_PROJECTS_FS_PATH=projects_fs_path;
//		_SUBSTANCES_ASSET_PATH=substances_asset_path;
//		_SUBSTANCES_FS_PATH=substances_fs_path;
		_SLIST=slist;
		_JSON=json;
		reloadList();
	}
	
	public void saveList() throws IOException{
		File output=new File(PathUtils.toNative((_PROJECTS_FS_PATH+"/projects.json")));
		Files.write(output.toPath(),_JSON.stringify(this).getBytes(Charset.forName("UTF-8")));
	}
	
	public void reloadList() throws IOException{
		clear();
		File input=new File(PathUtils.toNative(_PROJECTS_FS_PATH+"/projects.json"));
		if(input.exists()){
			String json=new String(Files.readAllBytes(input.toPath()),Charset.forName("UTF-8"));
			putAll((Map<Object,Object>)_JSON.parse(json));
		}
	}
	

	
	public SubstanceProject createProject(Collection<OBJMesh> meshes) throws IOException{
		String project_name=UUID.randomUUID().toString()+".spp";		
		Collection<String> project_hashes=new ArrayList<String>(meshes.size());
		for(OBJMesh m:meshes){
			project_hashes.add(""+m.hashCode());
		}
		put(project_name,project_hashes);
		saveList();
		

		File tempobj=File.createTempFile("substancejmelink",".obj");
		OutputStream os=new BufferedOutputStream(new FileOutputStream(tempobj));
		String objpath=tempobj.getAbsolutePath();
		File tempmtl=new File(objpath.substring(0,objpath.length()-3)+".mtl");
		OutputStream osmtl=new BufferedOutputStream(new FileOutputStream(tempmtl));
		SimpleObjWriter.write((meshes),os,osmtl);
		os.close();
		osmtl.close();
		String obj_path=tempobj.getAbsolutePath().replace(File.separator,"/");
		
		_LINK.getLink().createProject(obj_path);
		
		SubstanceProject pj= new SubstanceProject(_LINK,project_name,_PROJECTS_FS_PATH+"/"+project_name,_SLIST);		
		pj.save();
		return pj;
	}
	
	public Collection<String> getMeshHashesForProject(SubstanceProject pj){
		String n=pj.getName();
		return (Collection<String>)get(n);
	}
	
	public SubstanceProject getProject(Collection<OBJMesh> meshes) throws IOException{
		String hashes[]=new String[meshes.size()];
		int i=0;
		for(OBJMesh m:meshes)hashes[i++]=""+m.hashCode();
		
		String project_name=null;
		for(java.util.Map.Entry<Object,Object> e:entrySet()){
			project_name=(String)e.getKey();
			Collection<String> project_hashes=(Collection<String>)e.getValue();
			for(String hash:hashes){
				boolean match=false;
				for(String hash2:project_hashes){
					if(hash2.equals(hash)){
						match=true;
						break;
					}
				}
				if(!match){
					project_name=null;
					break;
				}
			}
			if(project_name!=null)break;
		}
		return project_name==null?null:new SubstanceProject(_LINK,project_name,_PROJECTS_FS_PATH+"/"+project_name,_SLIST);		
		
	}
}
