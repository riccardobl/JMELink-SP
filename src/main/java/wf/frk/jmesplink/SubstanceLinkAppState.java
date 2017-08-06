package wf.frk.jmesplink;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.omg.Messaging.SyncScopeHelper;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Mesh.Mode;

import wf.frk.jmesplink.link.SubstanceLink;
import wf.frk.jmesplink.obj.OBJMesh;
import wf.frk.jmesplink.obj.SimpleObjWriter;
import wf.frk.jmesplink.substances.PBRSubstance;
import wf.frk.jmesplink.substances.PhongSubstance;
import wf.frk.jmesplink.substances.Substance;
import wf.frk.jmesplink.substances.SubstanceDef;

import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;

public class SubstanceLinkAppState extends BaseAppState{
	private final static Logger LOGGER=Logger.getLogger(SubstanceLinkAppState.class.getName());

	private static class Entry{
		Geometry geo;
		boolean selected;
		private OBJMesh obj;
		public OBJMesh obj(){
			if(obj==null){
				obj=new OBJMesh(geo);
			}
			return obj;
		}
		
	}
	private SubstanceProject CURRENT_PROJECT;

	private Collection<Entry> ENTRIES=new ConcurrentLinkedQueue<Entry>();
	private SubstanceProjectList PROJECTS;
	private SubstancesList SUBSTANCES;
	
	private SubstanceLink LINK;
	private Json JSON;
	private String PROJECTS_FS_PATH,SUBSTANCES_ASSETS_PATH,SUBSTANCES_FS_PATH;
	Thread UPDATE_LOOP;
	private boolean NEED_SUBSTANCES_UPDATE;
	private final boolean _USE_DDS;

	public SubstanceLinkAppState(String substances_assets_path,Json json,boolean use_dds){
		JSON=json;
		SUBSTANCES_ASSETS_PATH=substances_assets_path;
		_USE_DDS=use_dds;
	}
	
	public void connect(String ip, int port, String substances_fs_path, String projects_fs_path) throws UnknownHostException, IOException {
		PROJECTS_FS_PATH=projects_fs_path.replace(File.separator,"/");
		SUBSTANCES_FS_PATH=substances_fs_path.replace(File.separator,"/");
		LINK=new SubstanceLink(ip,port,JSON);
	}
	
	public SubstanceProjectList getProjects(){
		return PROJECTS;
	}
	
	public SubstancesList getSubstances(){
		return SUBSTANCES;
	}
	
	public SubstanceLink getLink(){
		return LINK;
	}
	

	public void removeSpatial(Spatial s) {
		s.depthFirstTraversal(new SceneGraphVisitor(){
			@Override
			public void visit(Spatial sx) {
				if(sx instanceof Geometry){
					Geometry geo=(Geometry)sx;
					Material mat=geo.getMaterial();
					String mat_name=mat==null?null:mat.getName();
					if(mat_name==null){
						System.err.println("Invalid material or missing name");
					}else{
						Iterator<Entry> e_i=ENTRIES.iterator();
						while(e_i.hasNext()){
							if(e_i.next().geo==geo){
								e_i.remove();
								break;
							}
						}
					}
				}
			}
		});
	}

	public void addSpatial(Spatial s) {
		NEED_SUBSTANCES_UPDATE=true;
		s.depthFirstTraversal(new SceneGraphVisitor(){
			@Override
			public void visit(Spatial sx) {
				if(sx instanceof Geometry){
					Geometry geo=(Geometry)sx;
					Material mat=geo.getMaterial();
					String mat_name=mat==null?null:mat.getName();
					if(mat_name==null){
						System.err.println("Invalid material or missing name");
					}else{
						boolean exists=false;
						Iterator<Entry> e_i=ENTRIES.iterator();
						while(e_i.hasNext()){
							if(e_i.next().geo==geo){
								exists=true;
								break;
							}
						}
						if(!exists){
							Entry e=new Entry();
							e.geo=geo;
							ENTRIES.add(e);
						}
						// Apply substance
					}
				}
			}
		});
	}


	public SelectionResults deselectSpatial(Spatial s) {
		final SelectionResults out=new SelectionResults();

		if(LINK==null){
			System.err.println("You can't deselect without a link");
			return out;
		}
		s.depthFirstTraversal(new SceneGraphVisitor(){
			@Override
			public void visit(Spatial sx) {
				if(sx instanceof Geometry){
					Geometry geo=(Geometry)sx;
					out.geometries.add(geo);

					Material mat=geo.getMaterial();
					String mat_name=mat==null?null:mat.getName();
					if(mat_name==null&&geo.getMesh().getMode()==Mode.Triangles){
						System.err.println("Invalid material or missing name");
					}else{
						Iterator<Entry> e_i=ENTRIES.iterator();
						while(e_i.hasNext()){
							Entry e=e_i.next();
							if(e.geo==geo){
								e.selected=false;
								try{
									SubstanceProject pj=PROJECTS.getProject(Arrays.asList(e.obj()));
									if(pj!=null){
										out.has_project=true;
										Collection<String> s=PROJECTS.getMeshHashesForProject(pj);
										for(Entry e1:ENTRIES){
											if(e1.geo==geo)continue;
											String hash=""+e1.obj().hashCode();
											if(s.contains(hash)){
												e1.selected=false;
												out.geometries.add(e1.geo);
											}
										}
									}
								}catch(IOException e2){
									// TODO Auto-generated catch block
									e2.printStackTrace();
								}
								break;
							}
						}
						

					}
				}
			}
		});
		return out;
	}
	
	public static final class SelectionResults{
		public Collection<Geometry> geometries=new ArrayList<Geometry>();
		public boolean has_project=false;
	}

	public SelectionResults selectSpatial(Spatial s) {
		final SelectionResults out=new SelectionResults();

		if(LINK==null){
			System.err.println("You can't select without a link");
			return out;
		}
		
		s.depthFirstTraversal(new SceneGraphVisitor(){
			@Override
			public void visit(Spatial sx) {
				if(sx instanceof Geometry){
					Geometry geo=(Geometry)sx;
					Material mat=geo.getMaterial();
					String mat_name=mat==null?null:mat.getName();
					if(mat_name==null&&geo.getMesh().getMode()==Mode.Triangles){
						System.err.println("Invalid material or missing name");
					}else{
						Iterator<Entry> e_i=ENTRIES.iterator();
						while(e_i.hasNext()){
							Entry e=e_i.next();
							if(e.geo==geo){
								e.selected=true;
								out.geometries.add(geo);
								try{
									SubstanceProject pj=PROJECTS.getProject(Arrays.asList(e.obj()));
									if(pj!=null){
										out.has_project=true;
										Collection<String> s=PROJECTS.getMeshHashesForProject(pj);
										for(Entry e1:ENTRIES){
											if(e1.geo==geo) continue;
											String hash=""+e1.obj().hashCode();
											if(s.contains(hash)){
												e1.selected=true;
												out.geometries.add(e1.geo);
											}
										}
									}
								}catch(IOException e2){
									// TODO Auto-generated catch block
									e2.printStackTrace();
								}
								break;
							}
							
						}
						
		
					}
				}
			}
		});
		return out;
	}


	public void editSelected() throws IOException {
		if(LINK==null){
			System.err.println("You can't edit without a link");
			return;
		}
		
		Collection<OBJMesh> meshes=new ArrayList<OBJMesh>();

		Iterator<Entry> e_i=ENTRIES.iterator();
		while(e_i.hasNext()){
			Entry e=e_i.next();
			if(e.selected){
				meshes.add(e.obj().toWorldSpace());
			}
		}
		

		if(CURRENT_PROJECT!=null){
			CURRENT_PROJECT.saveAndClose();
		}
		CURRENT_PROJECT=PROJECTS.getProject(meshes);
		if(CURRENT_PROJECT==null){
			CURRENT_PROJECT=PROJECTS.createProject(meshes);
		}else{
			CURRENT_PROJECT.open();
		}
	}

	private void applySubstances(final boolean clear_cache) throws IOException {

		if(LINK!=null&&clear_cache){
			if(CURRENT_PROJECT==null||!CURRENT_PROJECT.needReload()) return;
			Collection<Substance> substances=CURRENT_PROJECT.exportSubstances();
			SUBSTANCES.addSubstances(substances);
		}
		
		
		LOGGER.log(Level.FINE,"applySubstances");


		for(final Entry e:ENTRIES){
			final Geometry g=e.geo;
			if(g.getMaterial()!=null){
				String mat_name=g.getMaterial().getName();
	
				final Substance s=SUBSTANCES.get(mat_name);
				if(s!=null){
					LOGGER.log(Level.FINE,"Found substance for "+mat_name);

					getApplication().enqueue(new Runnable(){
						@Override
						public void run() {
							if(clear_cache){
								getApplication().getAssetManager().clearCache();
							}
							Material newmat=s.toMaterial(getApplication().getAssetManager());
							g.setMaterial(newmat);
							LOGGER.log(Level.FINE,"Set substance for "+g.getMaterial().getName());

						}
					});
				}else{
					LOGGER.log(Level.FINE,"No substance for "+mat_name);

				}


			}
		}
	}
	
	@Override
	public void update(float tpf) {
		try{
			
			if(LINK!=null&&PROJECTS==null){
				PROJECTS=new SubstanceProjectList(this,PROJECTS_FS_PATH,SUBSTANCES,JSON);
				SUBSTANCES.setFsPath(SUBSTANCES_FS_PATH);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		if(LINK!=null&&UPDATE_LOOP==null){

			UPDATE_LOOP=new Thread(){
				@Override
				public void run() {
					while(isInitialized()){
						try{
							if(isEnabled()){
								if(LINK!=null){
									applySubstances(true);
								}
							}
							Thread.sleep(500);
						}catch(Exception e){
							e.printStackTrace();

						}
					}
				}
			};
			UPDATE_LOOP.setPriority(Thread.MIN_PRIORITY);
			UPDATE_LOOP.setDaemon(true);
			UPDATE_LOOP.start();
		}
		try{
			if(NEED_SUBSTANCES_UPDATE){
				NEED_SUBSTANCES_UPDATE=false;
				applySubstances(false);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	



	@Override
	protected void initialize(Application app) {
		try{
			SUBSTANCES=new SubstancesList(getApplication().getAssetManager(),SUBSTANCES_ASSETS_PATH,JSON);
			SUBSTANCES.registeredSubstancesDef().add(new PBRSubstance(_USE_DDS));
			SUBSTANCES.registeredSubstancesDef().add(new PhongSubstance(_USE_DDS));

		}catch(IOException e){
			e.printStackTrace();
		}		
		

	}


	@Override
	protected void cleanup(Application app) {

		UPDATE_LOOP=null;
	}

	@Override
	protected void onEnable() {

	}

	@Override
	protected void onDisable() {

	}

}
