import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bounding.BoundingSphere;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.environment.EnvironmentCamera;
import com.jme3.environment.LightProbeFactory;
import com.jme3.environment.generation.JobProgressAdapter;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.LightProbe;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FXAAFilter;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.util.SkyFactory;

import wf.frk.f3b.jme3.F3bLoader;
import wf.frk.jmesplink.Json;
import wf.frk.jmesplink.PathUtils;
import wf.frk.jmesplink.SubstanceLinkAppState;
import wf.frk.jmesplink.SubstanceLinkAppState.SelectionResults;
import wf.frk.jmesplink.substances.PBRSubstance;
import wf.frk.jmesplink.substances.PhongSubstance;

public class TestScene extends SimpleApplication implements ActionListener{
	private final static Logger LOGGER=Logger.getLogger(SubstanceLinkAppState.class.getName());
	private final static boolean _RELEASE=false;
	
	public static void main(String[] args) {
		new TestScene().start();
	}

	SubstanceLinkAppState SLINK;
	Node modelNode;

	@Override
	public void simpleInitApp() {
		try{
			setPauseOnLostFocus(false);
			flyCam.setDragToRotate(true);
			flyCam.setMoveSpeed(100f);
			modelNode=new Node("ModelRoot");
			rootNode.attachChild(modelNode);

			FilterPostProcessor fpp=new FilterPostProcessor(assetManager);
			FXAAFilter FXAA=new FXAAFilter();
			FXAA.setSubPixelShift(5.0f);
			FXAA.setReduceMul(5.0f);
			FXAA.setVxOffset(5.0f);
			fpp.addFilter(FXAA);
			viewPort.addProcessor(fpp);

			AmbientLight al=new AmbientLight();
			al.setColor(ColorRGBA.White.mult(1.3f));
			rootNode.addLight(al);

			Spatial sky=SkyFactory.createSky(assetManager,"Textures/Sky/Path.hdr",SkyFactory.EnvMapType.EquirectMap);
			rootNode.attachChild(sky);

			final EnvironmentCamera envCam=new EnvironmentCamera(128,new Vector3f(0,3f,0));
			stateManager.attach(envCam);

			F3bLoader.init(assetManager);
			String tmp=PathUtils.toNativeDir(System.getProperty("java.io.tmpdir"));

			String assets_root=tmp+"sblinktest";
			new File(assets_root).mkdir();
			assetManager.registerLocator(assets_root,FileLocator.class);

			String substances_fs_path=assets_root+File.separator+"substances";
			new File(substances_fs_path).mkdir();

			String substances_assets_path="substances/";

			String projects_fs_path=assets_root+File.separator+"projects";
			new File(projects_fs_path).mkdir();

			SLINK=new SubstanceLinkAppState(assetManager,substances_assets_path,new Json(){

				Gson gson;
				{
					gson=new GsonBuilder().disableHtmlEscaping().create();
				}

				@Override
				public Object parse(String json) {
					return gson.fromJson(json,Object.class);
				}

				@Override
				public String stringify(Map<Object,Object> map) {
					return gson.toJson(map);
				}

			});
			SLINK.getSubstances().registeredSubstancesDef().add(new PBRSubstance(true));
			SLINK.getSubstances().registeredSubstancesDef().add(new PhongSubstance(true));
			if(!_RELEASE)SLINK.connect("127.0.0.1",6403,substances_fs_path,projects_fs_path);
			stateManager.attach(SLINK);

			Spatial scene=assetManager.loadModel("wf/frk/substancejlink/resources/test_scene.f3b");
			modelNode.attachChild(scene);
			SLINK.addSpatial(scene);

			inputManager.addMapping("select",new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
			inputManager.addMapping("edit",new KeyTrigger(KeyInput.KEY_E));

			inputManager.addListener(this,"select","edit");

			modelNode.depthFirstTraversal(new SceneGraphVisitor(){
				@Override
				public void visit(Spatial s) {
					if(s instanceof Geometry){
						Geometry geo=(Geometry)s;
						System.out.println(geo.getName()+" :: "+geo.getMaterial().getName()+" :: "+geo.getMaterial());

					}
				}
			});
		}catch(IOException e){
			e.printStackTrace();
		}

	}
	
	
	void deselect(Geometry g){
		LOGGER.log(Level.INFO,"Deselect "+g);
		SelectionResults sres=SLINK.deselectSpatial(g);
		Collection<Geometry> deselected_geometries=sres.geometries;
		
		for(Geometry gd:deselected_geometries){
			last_selected.remove(gd);
			Geometry selg=gd.getUserData("selected");
			if(selg!=null){
				selg.removeFromParent();
				gd.setUserData("selected",null);
			}
		}
	}

	Collection<Geometry> last_selected=new ArrayList<Geometry>();
	boolean lock_group=false;
	@Override
	public void onAction(String name, boolean isPressed, float tpf) {
		if(!isPressed) return;
		if(name.equals("select")){
			CollisionResults results=new CollisionResults();
			Vector2f click2d=inputManager.getCursorPosition();
			Vector3f click3d=cam.getWorldCoordinates(new Vector2f(click2d.x,click2d.y),0f).clone();
			Vector3f dir=cam.getWorldCoordinates(new Vector2f(click2d.x,click2d.y),1f).subtractLocal(click3d).normalizeLocal();
			Ray ray=new Ray(click3d,dir);
			rootNode.collideWith(ray,results);

			for(CollisionResult r:results){
				boolean sel=false;
				if(r.getGeometry().getUserData("selg")!=null)continue;
				for(Geometry selgg:last_selected){
					if(selgg==r.getGeometry()){
						System.out.println(selgg);
						sel=true;
						break;
					}
				}
				
				if(sel){
					deselect(r.getGeometry());
					break;

				}else{
					LOGGER.log(Level.INFO,"Select "+r.getGeometry());
					if(lock_group){
						Geometry[] ls=last_selected.toArray(new Geometry[0]);
						for(Geometry g:ls){
							deselect(g);
						}
						lock_group=false;
					}
					SelectionResults sres=SLINK.selectSpatial(r.getGeometry());
					Collection<Geometry> selected_geometries=sres.geometries;
					if(sres.has_project){
						lock_group=true;
					}
					if(selected_geometries.size()>0){
						last_selected.addAll(selected_geometries);

						for(Geometry selgg:selected_geometries){

							Geometry selg=selgg.clone(false);
							Material mat=new Material(assetManager,"Common/MatDefs/Misc/Unshaded.j3md");
							mat.setColor("Color",ColorRGBA.Blue);
							selg.setMaterial(mat);
							selg.setUserData("selg",true);
							// Nehon's wireframe https://hub.jmonkeyengine.org/t/outliner-material-toon-outline/37610/5
							selg.getMaterial().getAdditionalRenderState().setWireframe(true); //we want wireframe
							selg.getMaterial().getAdditionalRenderState().setBlendMode(BlendMode.Alpha);//that's just because we add an alpha pulse to the selection later, this is not mandatory
							selg.getMaterial().getAdditionalRenderState().setLineWidth(4); //you can play with this param to increase the line thickness
							selg.getMaterial().getAdditionalRenderState().setPolyOffset(-3f,-3f); //this is trick one, offsetting the polygons
							selg.getMaterial().getAdditionalRenderState().setFaceCullMode(FaceCullMode.Front); // trick 2 we hide the front faces to not see the wireframe on top of the geo
							selgg.getParent().attachChild(selg);
							selgg.setUserData("selected",selg);
						}
						break;
					}
				}
			}

		}else if(name.equals("edit")){
			try{
				lock_group=true;
				SLINK.editSelected();
			}catch(IOException e){
				e.printStackTrace();
			}

		}
	}

	private int frame=0;

	@Override
	public void simpleUpdate(float tpf) {
		frame++;

		if(frame==2){

			modelNode.removeFromParent();
			final LightProbe probe=LightProbeFactory.makeProbe(stateManager.getState(EnvironmentCamera.class),rootNode,new JobProgressAdapter<LightProbe>(){

				@Override
				public void done(LightProbe result) {

				}

			});
			BoundingSphere s=(BoundingSphere)probe.getBounds();
			s.setRadius(100);
			rootNode.addLight(probe);

		}
		if(frame>10&&modelNode.getParent()==null){
			rootNode.attachChild(modelNode);
		}
	}

}
