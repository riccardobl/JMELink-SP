package wf.frk.jmesplink;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import wf.frk.jmesplink.link.SubstanceLink;
import wf.frk.jmesplink.substances.Exporter;
import wf.frk.jmesplink.substances.Substance;
import wf.frk.jmesplink.substances.SubstanceDef;

public class SubstanceProject{
	private final static Logger LOGGER=Logger.getLogger(SubstancesList.class.getName());

	private final String _PATH;
	private final SubstancesList _SLIST;
	private final SubstanceLinkAppState _LINK;
	private final String _NAME;
	public SubstanceProject(SubstanceLinkAppState link,String name,String path,SubstancesList slist){
		_PATH=path;
		_NAME=name;
		_LINK=link;
		_SLIST=slist;
	
	}
	
	public String getName(){
		return _NAME;
	}

	public void open() throws IOException {
		_LINK.getLink().openProject(_PATH);
	}

	public void close() throws IOException {
		_LINK.getLink().closeProject();
	}

	public void save() throws IOException {
		_LINK.getLink().saveProject(_PATH);
	}

	public void saveAndClose() throws IOException {
		_LINK.getLink().saveProjectAndClose();
	}
	
	public boolean needReload() throws IOException {
		return _LINK.getLink().texturesChanged();

	}

	public Collection<Substance> exportSubstances() throws IOException {
		Map<Object,Object> shader_settings=_LINK.getLink().getShaderSettings();

		Map<Object,Object> texture_sets=(Map<Object,Object>)shader_settings.get("texturesets");
		if(texture_sets!=null){
			Map<String,Map> substances=new HashMap<String,Map>();

			for(java.util.Map.Entry<Object,Object> texture_set:texture_sets.entrySet()){
				String tx_set_name=(String)texture_set.getKey();
				Map<Object,Object> shader_settingx=(Map)((Map)shader_settings.get("shaders")).get((String)((Map)texture_set.getValue()).get("shader"));
				HashMap shader_setting=new HashMap();
				for(java.util.Map.Entry<Object,Object> xe:shader_settingx.entrySet()){
					shader_setting.put(xe.getKey(),xe.getValue());
				}
				substances.put(tx_set_name,(Map)(shader_setting));

			}

			Collection<Substance> out=new ArrayList<Substance>();
			for(java.util.Map.Entry<String,Map> substance:substances.entrySet()){

				Substance sx=new Substance();
				sx.putAll(substance.getValue());
				sx.put("name",substance.getKey());
				sx.setSubstancesList(_SLIST);
				SubstanceDef sdef=_SLIST.getSubstanceDef(sx);

				if(sdef!=null){
					Exporter ex=sdef.getExporter(sx,_LINK);
					byte preset[]=ex.getPreset();
					File temppreset=File.createTempFile("substancejmelink",".spexp");
					Files.write(temppreset.toPath(),preset);

					Map<Object,Object> map=_LINK.getLink().exportTextures(new String[]{substance.getKey()},
							temppreset.getAbsolutePath(),
							
						_SLIST.getFileSystemPath(),null);

					Map<Object,Object> txs=(Map<Object,Object>)map.get(substance.getKey());

					for(Entry<Object,Object> tx:txs.entrySet()){
						tx.setValue(ex.transformTexture(tx.getKey().toString(),tx.getValue().toString()));
					}
					
					sx.put("textures",txs);

					out.add(sx);
				}else{
					LOGGER.log(Level.FINE,"No substance can map "+sx.get("name"));
				}
			}
			return out;
		}
		return null;

	}
}
