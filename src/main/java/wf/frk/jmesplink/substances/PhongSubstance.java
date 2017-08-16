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
import com.jme3.renderer.queue.RenderQueue.Bucket;

import wf.frk.jmesplink.PathUtils;
import wf.frk.jmesplink.resources.Resources;

public class PhongSubstance extends DDSSubstanceDef{
	private final static Logger LOGGER=Logger.getLogger(PhongSubstance.class.getName());

	@Override
	public boolean canMap(Substance s) {
		String shader=(String)s.get("shader");
		return shader.equals("non-pbr-spec-gloss");
	}

	public PhongSubstance(boolean use_dds){
		super(use_dds);
	}




	@Override
	public String getOutputFormat(String tx_name,String tx_path,String substance_fs_path) {
		tx_name=tx_name.substring(tx_name.lastIndexOf("_")+1);
		boolean has_alpha=false;
		try{
			BufferedImage bimg=ImageIO.read(new File(PathUtils.toNative(substance_fs_path+tx_path)));
			has_alpha=bimg.getTransparency()==BufferedImage.TRANSLUCENT;
		}catch(Exception e){
			LOGGER.log(Level.WARNING,"Can't read image",e);
		}
		if(has_alpha)LOGGER.log(Level.FINE,tx_name+" has alpha channel.");
		switch(tx_name){
			case "DiffuseMap":
				return has_alpha?"S3TC_DXT5":"S3TC_DXT1";
			case "SpecularMap":
				return "S3TC_DXT1";
			case "GlowMap":
				return "S3TC_DXT1";
		
			case "NormalMap":
				return "rgb8";
			case "ParallaxMap":
				return "rgb8";
		}
		return null;
	}

	

	@Override
	public MaterialMap toMaterial(AssetManager am, Substance substance, String substance_assets_path) {
		Material mat=new Material(am,"Common/MatDefs/Light/Lighting.j3md");
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
		
		Map parameters=(Map)substance.get("parameters");
		if(parameters!=null){
			Number fresnel=(Number)parameters.get("fresnel_str");
			if(fresnel!=null) mat.setFloat("Shininess",fresnel.floatValue());
		}
		
		MaterialMap map=new MaterialMap();
		map.material=mat;
		map.render_bucket=Bucket.Opaque;
		return map;
		
	}

	@Override
	public byte[] getPreset(Substance s) {
		try{
			return Resources.getAsBytes("presets/JME_Lighting.spexp");
		}catch(Exception e){
			e.printStackTrace();
		}
		return new byte[0];
	}
}
