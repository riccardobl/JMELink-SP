package wf.frk.jmesplink.obj;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;

/** 
*
*  @author    Riccardo Balbo
*  @license
*  
* Copyright (c) 2016, Riccardo Balbo
* All rights reserved.
* 
* Redistribution and use in source and binary forms, with or without modification, 
* are permitted provided that the following conditions are met:
* 
* 1. Redistributions of source code must retain the above copyright notice, this 
* list of conditions and the following disclaimer.
* 
* 2. Redistributions in binary form must reproduce the above copyright notice, 
* this list of conditions and the following disclaimer in the documentation 
* and/or other materials provided with the distribution.
* 
* 3. Neither the name of the copyright holder nor the names of its contributors 
* may be used to endorse or promote products derived from this software 
* without specific prior written permission.
* 
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
* IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
* IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
* THE POSSIBILITY OF SUCH DAMAGE.
*  
*/
public class SimpleObjWriter{
	private final static Logger LOGGER=Logger.getLogger(SimpleObjWriter.class.getName());



	private static int addToB(Object v, List uniqueb) {
		int i=0;
		boolean found=false;
			//			LOGGER.log(Level.FINE,"Remove doubles");

			for(Object vx:uniqueb){
				if(vx.equals(v)){
					found=true;
					break;
				}else i++;
			}
		

		if(!found){
			uniqueb.add(v);
		}
		return i;

	}
	
	// return true if unique
	private static boolean toUniqueVB(Vertex v, List<Vertex> uniquev, List<Integer> indexes,boolean removedoubles) {
		boolean found=false;
		int j=0;
		if(removedoubles){
			for(Vertex vx:uniquev){
				if(vx.pos.equals(v.pos)&&vx.normal.equals(v.normal)&&vx.uv.equals(v.uv)){
					found=true;
					break;				
				}else j++;
			}
			
		}else{
			j=uniquev.size();

		}

		if(!found){
			uniquev.add(v);
		}
		indexes.add(j);
		return !found;
	}

	private static void writeVertex(Vertex vx,OutputStream out) throws IOException{
		StringBuilder sout=new StringBuilder();
		Vector3f pos=vx.pos;
		sout.append("v ").append(pos.x).append(" ").append(pos.y).append(" ").append(pos.z).append(" 1.0\n");
		Vector2f uv=vx.uv;
		sout.append("vt ").append(uv.x).append(" ").append(uv.y).append("\n");
		Vector3f norm=vx.normal;
		sout.append("vn ").append(norm.x).append(" ").append(norm.y).append(" ").append(norm.z).append("\n");
		out.write(sout.toString().getBytes(Charset.forName("UTF-8")));
	}
	
	public static void write(Collection<OBJMesh> meshes,OutputStream out,OutputStream out_mtl) throws IOException {
		write(meshes,true,out,out_mtl);
	}

	public static void write(Collection<OBJMesh> meshes,boolean remove_doubles,OutputStream out,OutputStream out_mtl) throws IOException {
		ArrayList<String> materials=new ArrayList<String>();
		int last_i=0;	
		for(OBJMesh m:meshes){
			if(!materials.contains(m.material))materials.add(m.material);

			out.write(("o "+m.name+"\n").getBytes(Charset.forName("UTF-8")));
			out.write(("usemtl "+m.material+"\n").getBytes(Charset.forName("UTF-8")));
			LOGGER.log(Level.FINE,"Serialize mesh "+m.name+" triangles count: "+m.triangles.size()+" remove doubles: "+remove_doubles+ " material "+m.material);

			List<Integer> indexes=new ArrayList<Integer>();
			List<Vertex> uniquev=new LinkedList<Vertex>();
			
			LOGGER.log(Level.FINE,"Collect vertices");
			for(Triangle t:m.triangles){
				if(toUniqueVB(t.v1,uniquev,indexes,remove_doubles))writeVertex(t.v1,out);
				if(toUniqueVB(t.v2,uniquev,indexes,remove_doubles))writeVertex(t.v2,out);
				if(toUniqueVB(t.v3,uniquev,indexes,remove_doubles))writeVertex(t.v3,out);				
			}

			LOGGER.log(Level.FINE,"Write faces");

			for(int i=0;i<indexes.size();i+=3){
				int iv1=indexes.get(i)+1;
				int iv2=indexes.get(i+1)+1;
				int iv3=indexes.get(i+2)+1;
				iv1+=last_i;
				iv2+=last_i;
				iv3+=last_i;
				StringBuilder outs=new StringBuilder();
				outs.append("f ").append((int)iv1).append("/").append((int)iv1).append("/").append((int)iv1).append(" ");
				outs.append((int)iv2).append("/").append((int)iv2).append("/").append((int)iv2).append(" ");
				outs.append((int)iv3).append("/").append((int)iv3).append("/").append((int)iv3).append("\n");
				out.write(outs.toString().getBytes(Charset.forName("UTF-8")));


			}
			last_i+=uniquev.size();
		}
		if(out_mtl!=null){
			for(String m:materials){
				String mtl=("\nnewmtl "+m+"\nNs 96.078431\nKa 1.000000 1.000000 1.000000\nKd 0.640000 0.640000 0.640000\nKs 0.500000 0.500000 0.500000\nKe 0.000000 0.000000 0.000000\nNi 1.000000\nd 1.000000\nillum 2\n");
				out_mtl.write(mtl.getBytes(Charset.forName("UTF-8")));
			}
		}
		LOGGER.log(Level.FINE,"Done");

	}
}
