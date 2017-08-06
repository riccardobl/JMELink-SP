package wf.frk.jmesplink.substances;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;

import wf.frk.jmesplink.SubstanceLinkAppState;


public abstract class DDSSubstanceDef implements SubstanceDef{
	protected final boolean _USE_DDS;
	private final static Logger LOGGER=Logger.getLogger(DDSSubstanceDef.class.getName());

	public DDSSubstanceDef(boolean use_dds){
		_USE_DDS=use_dds;
	}

	public abstract byte[] getPreset(Substance s);

	public abstract String getOutputFormat(String tx_name,String tx_path,String substance_fs_path);

	@Override
	public Exporter getExporter(final Substance s, final SubstanceLinkAppState link) {
		final String substance_fs_path=link.getSubstances().getFileSystemPath();
		
		return new Exporter(){

			@Override
			public String transformTexture(final String tx_name,final String path) {
				if(path.isEmpty())return path;
				if(_USE_DDS){
					final boolean wait[]={true};
					final String out_path[]={""};
					link.getApplication().enqueue(new Runnable(){
						public void run() {
							try{
								String format=getOutputFormat(tx_name,path,substance_fs_path);
								if(format==null||format.isEmpty()){
									out_path[0]=path;
									wait[0]=false;
									return;
									
								}

								String fs=substance_fs_path.replace("/",File.separator)+File.separator;
							
								String in=path.replace("/",File.separator);
								out_path[0]=path.substring(0,path.lastIndexOf("."))+".dds";
								String out=out_path[0].replace("/",File.separator);
								
								
								Map<String,String> options=new HashMap<String,String>();
								options.put("gen-mipmaps","true");
//								options.put("debug","true");
								options.put("format",format);
								Collection<Object> delegates=new ArrayList<Object>();
								
								

								try{
									Class c=getClass().getClassLoader().loadClass("ddswriter.delegates.GenericDelegate");
									delegates.add(c.newInstance());
								}catch(Exception e){
									LOGGER.log(Level.WARNING,"Can't load generic delegate",e);
								}
								
								try{
									Class c=getClass().getClassLoader().loadClass("ddswriter.delegates.lwjgl2_s3tc.S3TC_LWJGL2CompressionDelegate");
									delegates.add(c.newInstance());
								}catch(Exception e){
									LOGGER.log(Level.WARNING,"Can't load s3tc delegate",e);
								}
								try{
									
									Class c=getClass().getClassLoader().loadClass("ddswriter.delegates.lwjgl2_rgtc.RGTC_LWJGL2CompressionDelegate");
									delegates.add(c.newInstance());
								}catch(Exception e){
									LOGGER.log(Level.WARNING,"Can't load rgtc delegate",e);
								}
								
								Texture tx=new Texture2D(new AWTLoader().load(ImageIO.read(new File(fs+in)),false));
								FileOutputStream fo=new FileOutputStream(new File(fs+out));
								
								Class c=getClass().getClassLoader().loadClass("ddswriter.DDSWriter");
								c.getMethod("write",Texture.class,Map.class,Collection.class,OutputStream.class).invoke(null,tx,options,delegates,fo);
													
								fo.close();
								
								new File(fs+in).delete();
								tx.getImage().dispose();
								
							}catch(Exception e){
								LOGGER.log(Level.WARNING,"Can't export DDS",e);
							}
							wait[0]=false;
						}
					});
					
					while(wait[0]){
						try{
							Thread.sleep(100);
						}catch(InterruptedException e){
							e.printStackTrace();
						}
					}
					return out_path[0];
				}
				return path;
			}

			@Override
			public byte[] getPreset() {
				return DDSSubstanceDef.this.getPreset(s);
			}

		};
	}

}
