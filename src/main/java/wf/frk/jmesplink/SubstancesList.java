package wf.frk.jmesplink;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;

import wf.frk.jmesplink.link.SubstanceLink;
import wf.frk.jmesplink.substances.Substance;
import wf.frk.jmesplink.substances.SubstanceDef;

public class SubstancesList  extends HashMap<String,Substance>{
	private final static Logger LOGGER=Logger.getLogger(SubstancesList.class.getName());

	private transient final String _AM_PATH;
	private transient  String FS_PATH;
	private transient final Json _JSON;
	private transient final AssetManager _AM;
	private transient final Collection<SubstanceDef> _REGISTERED_SUBSTANCES_DEF=new ArrayList<SubstanceDef>();
	

	public String getAssetPath(){
		return _AM_PATH;
	}
	
	public String getFileSystemPath(){
		return FS_PATH;
	}
	
	@Deprecated
	public SubstancesList() throws IOException{
		this(null,null,null);
	}
	
	public SubstancesList(AssetManager am,String asset_path,Json json) throws IOException{
		_AM=am;
		_AM_PATH=asset_path;
		_JSON=json;
		reloadList();
	}
	
	public void setFsPath(String p){
		FS_PATH=p;
	}
	
	
	public void saveList() throws IOException{
		File output=new File((FS_PATH+"/substances.json").replace("/",File.separator));
		Files.write(output.toPath(),_JSON.stringify((Map)this).getBytes(Charset.forName("UTF-8")));		
	}
	
	public void reloadList() throws IOException{
		clear();
		AssetInfo info=_AM.locateAsset(new AssetKey(_AM_PATH+"/substances.json"));
		if(info!=null){
			InputStream is=info.openStream();
			ByteArrayOutputStream bos=new ByteArrayOutputStream();
			byte chunk[]=new byte[1024*1024];
			int readed;
			while((readed=is.read(chunk))!=-1)bos.write(chunk,0,readed);
			String json=bos.toString("UTF-8");
			Map<Object,Object> map=(Map)_JSON.parse(json);
			for(java.util.Map.Entry<Object,Object> e:map.entrySet()){
				Substance s=new Substance();
				s.setSubstancesList(this);
				s.putAll((Map)e.getValue());
				put((String)e.getKey(),s);	
			}
		}else{
			LOGGER.log(Level.FINE,"substances.json not found");
		}
	}
	
	public Collection<SubstanceDef> registeredSubstancesDef(){
		return _REGISTERED_SUBSTANCES_DEF;
	}

	public Substance getSubstance(String matname) {
		return get(matname);
	}
	
	public void addSubstances(Collection<Substance> ss) throws IOException{
		for(Substance s:ss)put((String)s.get("name"),s);
		saveList();

	}

	public SubstanceDef getSubstanceDef(Substance sx) {
		for(SubstanceDef d:this._REGISTERED_SUBSTANCES_DEF){
			if(d.canMap(sx)){
				return d;
			}
		}
		return null;
	}
	

	
	
}
