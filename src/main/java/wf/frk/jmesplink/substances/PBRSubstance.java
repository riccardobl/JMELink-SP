package wf.frk.jmesplink.substances;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;

import wf.frk.jmesplink.PathUtils;
import wf.frk.jmesplink.SubstanceLinkAppState;
import wf.frk.jmesplink.resources.Resources;

public class PBRSubstance extends DDSSubstanceDef{
	private final static Logger LOGGER=Logger.getLogger(PBRSubstance.class.getName());

	public PBRSubstance(boolean use_dds){
		super(use_dds);
	}




	@Override
	public String getOutputFormat(String tx_name,String tx_path,String substance_fs_path) {
		if(tx_path.isEmpty())return tx_path;
		tx_name=tx_name.substring(tx_name.lastIndexOf("_")+1);
		boolean has_alpha=false;
		try{
			File f=new File(PathUtils.toNative(substance_fs_path+"/"+tx_path));
			System.out.println(f);
			BufferedImage bimg=ImageIO.read(f);
			has_alpha=bimg.getTransparency()==BufferedImage.TRANSLUCENT;
		}catch(Exception e){
			LOGGER.log(Level.WARNING,"Can't read image",e);
		}
		if(has_alpha)LOGGER.log(Level.FINE,tx_name+" has alpha channel.");
		switch(tx_name){
			case "BaseColorMap":
				return has_alpha?"S3TC_DXT5":"S3TC_DXT1";
			case "MetallicMap":
				return "S3TC_DXT1";
			case "RoughnessMap":
				return "S3TC_DXT1";
			case "EmissiveMap":
				return "S3TC_DXT1";
			case "NormalMap":
				return "rgb8";
			case "ParallaxMap":
				return "rgb8";
		}
		return null;
	}

	
	@Override
	public boolean canMap(Substance s) {
		String shader=(String)s.get("shader");
		return shader.equals("pbr-metal-rough");
	}

	@Override
	public byte[] getPreset(Substance s) {
		try{
			return Resources.getAsBytes("presets/JME_PBRLighting_METALROUGH.spexp");
		}catch(Exception e){
			e.printStackTrace();
		}
		return new byte[0];
	}

	@Override
	public Material substanceToMaterial(AssetManager am,Substance substance, String substance_assets_path) {
		Material mat=new Material(am,"Common/MatDefs/Light/PBRLighting.j3md");
		mat.setName(substance.get("name").toString());
		Map<Object,Object> textures=(Map<Object,Object>)substance.get("textures");
		if(textures!=null){
		for(Entry e:textures.entrySet()){
			String tx=e.getKey().toString();
			if(!e.getValue().toString().isEmpty()){
				tx=tx.substring(tx.lastIndexOf("_")+1);
				String p=substance_assets_path+"/"+e.getValue();
				LOGGER.log(Level.FINE,"Set "+tx+"="+p);
				mat.setTexture(tx,am.loadTexture(p));
			}
		}
		}
		return mat;
	}

}
