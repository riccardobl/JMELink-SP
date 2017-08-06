package wf.frk.jmesplink.obj;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Type;



/** 
*
*  @author    Riccardo Balbo
*/
public class OBJMesh implements Cloneable{
	private final static Logger LOGGER=Logger.getLogger(OBJMesh.class.getName());

	public String name="";
	public Vector3f position=new Vector3f(0,0,0);
	public Vector3f scale=new Vector3f(1,1,1);
	public Quaternion rotation=new Quaternion();
	public String material;
	public List<Triangle> triangles=new ArrayList<Triangle>();
	public static Vector3f getVector3f(VertexBuffer v,int id){
		if(v==null||v.getNumElements()<=id){
			return new Vector3f();
		}
		
		return new Vector3f((float)v.getElementComponent(id, 0),
		(float)v.getElementComponent(id, 1),
		(float)v.getElementComponent(id, 2));
		
	}
	
	public static Vector2f getVector2f(VertexBuffer v,int id){
		return new Vector2f((float)v.getElementComponent(id, 0),
		(float)v.getElementComponent(id, 1));
		
	}

	
	public int hashCode(){
		try{
			ByteArrayOutputStream hashbb=new ByteArrayOutputStream();
			DataOutputStream hashb=new DataOutputStream(hashbb);
			Collection<Triangle> tris=triangles;
			for(Triangle tri:tris){
				Vertex vertices[]=tri.vertexes();
				for(Vertex v:vertices){
					hashb.writeFloat(v.pos.x);
					hashb.writeFloat(v.pos.y);
					hashb.writeFloat(v.pos.z);
					hashb.writeFloat(v.uv.x);
					hashb.writeFloat(v.uv.y);
					hashb.writeFloat(v.normal.x);
					hashb.writeFloat(v.normal.y);
					hashb.writeFloat(v.normal.z);
				}			
			}
			hashb.writeUTF(material);
			hashb.flush();
			byte arr[]=hashbb.toByteArray();
			hashb.close();
			return Arrays.hashCode(arr);	
		}catch(Exception e){
			e.printStackTrace();
			return -1;
		}
	}
	
	
	public OBJMesh(){
	}
	
	public OBJMesh(Geometry geo){
		material=geo.getMaterial().getName();
		position=geo.getWorldTranslation();
		rotation=geo.getWorldRotation();
		scale=geo.getWorldScale();
		name=geo.getName();
		
		LOGGER.log(Level.FINE,"Create OBJ mesh with name "+name+" material "+material);

		
		com.jme3.scene.Mesh m=geo.getMesh();
		
		VertexBuffer i_b = m.getBuffer(Type.Index);
		ArrayList<Number> i_a=new ArrayList<Number>();
		for(int i=0;i<i_b.getNumElements();i++){
			for(int j=0;j<i_b.getNumComponents();j++){
				i_a.add((Number)i_b.getElementComponent(i, j));
			}
		}

		VertexBuffer p_b = m.getBuffer(Type.Position);
		VertexBuffer n_b = m.getBuffer(Type.Normal);
		VertexBuffer t_b =  m.getBuffer(Type.TexCoord);

		
		for (int i = 0; i+3<=i_a.size(); i += 3) {
			int vertex_a_index = ((Number) i_a.get(i)).intValue();
			int vertex_b_index = ((Number) i_a.get(i +1)).intValue();
			int vertex_c_index = ((Number) i_a.get(i +2)).intValue();

			Vector3f vertex_a_position = getVector3f(p_b,vertex_a_index);
			Vector3f vertex_b_position = getVector3f(p_b,vertex_b_index);
			Vector3f vertex_c_position = getVector3f(p_b,vertex_c_index);

			Vector3f vertex_a_normal = getVector3f(n_b,vertex_a_index);
			assert vertex_a_normal.length()!=0 : "Normal of length 0?? for triangle id "+i+"vertex id "+vertex_a_index;			
			Vector3f vertex_b_normal = getVector3f(n_b,vertex_b_index);
			assert vertex_b_normal.length()!=0 : "Normal of length 0?? for triangle id "+i+"vertex id "+vertex_b_index;			
			Vector3f vertex_c_normal = getVector3f(n_b,vertex_c_index);
			assert vertex_c_normal.length()!=0 : "Normal of length 0?? for triangle id "+i+"vertex id "+vertex_c_index;			


			Vector2f vertex_a_uv =getVector2f( t_b,vertex_a_index);
			Vector2f vertex_b_uv =getVector2f( t_b,vertex_b_index);
			Vector2f vertex_c_uv =getVector2f( t_b,vertex_c_index);

			
			Vertex v1=new Vertex();
			v1.pos=vertex_a_position;
			v1.normal=vertex_a_normal;
			v1.uv=vertex_a_uv;
			
			Vertex v2=new Vertex();
			v2.pos=vertex_b_position;
			v2.normal=vertex_b_normal;
			v2.uv=vertex_b_uv;
			
			Vertex v3=new Vertex();
			v3.pos=vertex_c_position;
			v3.normal=vertex_c_normal;
			v3.uv=vertex_c_uv;
			
			Triangle tri=new Triangle(
					v1,v2,v3
			);
			
			triangles.add(tri);
		}
		
	}

	@Override
	public OBJMesh clone() {
		OBJMesh out=new OBJMesh();
		out.name=name;
		out.material=material;
		out.position=position.clone();
		out.scale=scale.clone();
		out.rotation=rotation.clone();
		LOGGER.log(Level.FINE,"Clone OBJ mesh with name "+out.name+" material "+material);

		for(Triangle t:triangles){
			out.triangles.add(t.clone());
		}
		return out;
	}

	@Override
	public String toString() {
		return name;
	}

	public OBJMesh toWorldSpace() {
		OBJMesh out=clone();
		ArrayList<Triangle> wtriangles=new ArrayList<Triangle>();
		for(Triangle xt:triangles){
			Triangle t=xt.clone();
			
			t.v1.pos.multLocal(scale);
			rotation.multLocal(t.v1.pos);
			t.v1.pos.addLocal(position);

			t.v2.pos.multLocal(scale);
			rotation.multLocal(t.v2.pos);
			t.v2.pos.addLocal(position);

			t.v3.pos.multLocal(scale);
			rotation.multLocal(t.v3.pos);
			t.v3.pos.addLocal(position);
			
			rotation.multLocal(t.v1.normal);
			rotation.multLocal(t.v2.normal);
			rotation.multLocal(t.v3.normal);

			wtriangles.add(t);
		}

		out.triangles=wtriangles;
		return out;
	}
}
