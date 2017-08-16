package wf.frk.jmesplink.substances;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;

import wf.frk.jmesplink.SubstancesList;

public  class Substance extends HashMap {	
//	public abstract byte[] getPreset();
//	public abstract Material toMaterial(AssetManager am);
	private final static Logger LOGGER=Logger.getLogger(Substance.class.getName());
	private transient SubstancesList SLIST;
	
	
	public void setSubstancesList(SubstancesList x){
		SLIST=x;
	}

	public MaterialMap toMaterial(AssetManager am, Geometry geo) {
		SubstanceDef sdef=SLIST.getSubstanceDef(this);
		if(sdef!=null){

			return sdef.toMaterial(am,this,SLIST.getAssetPath());

		}else{
			LOGGER.log(Level.FINE,"No material can be map to "+get("name"));
		}
		return null;
	}

}