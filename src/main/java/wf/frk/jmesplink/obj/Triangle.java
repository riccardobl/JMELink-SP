/*
 * $Id$
 *
 * Copyright (c) 2014, Simsilica, LLC
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the 
 *    distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package wf.frk.jmesplink.obj;

import com.jme3.math.Vector3f;

//import com.jme3.math.Vector3f;


/**
 *
 *  @author    Paul Speed
 */
public class Triangle {
    public final Vertex v1;
    public final Vertex v2;
    public final Vertex v3;
    
    public Triangle clone(){
    	Triangle out=new Triangle(v1.clone(),v2.clone(),v3.clone());
    	return out;
    }
    
  
    
    public Triangle( Vertex v1, Vertex v2, Vertex v3 ) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        v1.usageCount++;
        v2.usageCount++;
        v3.usageCount++;
    }
 
    public Vertex[] vertexes() {
        return new Vertex[] { v1, v2, v3 };
    }
    
    public Vector3f calculateNormal() {        
        Vector3f edge1 = v2.pos.subtract(v1.pos);
        Vector3f edge2 = v3.pos.subtract(v1.pos);
        return edge1.cross(edge2).normalizeLocal();       
    }
    
    /**
     *  Returns the angle in radians at this triangles specified 
     *  corner.
     */        
    public float angle( Vertex corner ) {
    
        Vertex a;
        Vertex b;
        if( v1 == corner ) {
            a = v2;
            b = v3;
        } else if( v2 == corner ) {
            a = v3;
            b = v1;
        } else if( v3 == corner ) {
            a = v1; 
            b = v2;
        } else {
            throw new IllegalArgumentException("Corner is not part of this triangle");
        }       

        Vector3f edge1 = a.pos.subtract(corner.pos).normalizeLocal();
        Vector3f edge2 = b.pos.subtract(corner.pos).normalizeLocal();
        float result = edge1.angleBetween(edge2);
        return result; 
    }    
    

    protected Vector3f barycentricCoords(Vector3f pA, Vector3f pB, Vector3f pC, Vector3f point) {
		Vector3f v0=pB.subtract(pA);
		Vector3f v1=pC.subtract(pA);
		Vector3f v2=point.subtract(pA);
		float d00=v0.dot(v0);
		float d01=v0.dot(v1);
		float d11=v1.dot(v1);
		float d20=v2.dot(v0);
		float d21=v2.dot(v1);
		float invDenom=1.0f/(d00*d11-d01*d01);
		float v=(d11*d20-d01*d21)*invDenom;
		float w=(d00*d21-d01*d20)*invDenom;
		float u=1.0f-v-w;
		if(u<0) return new Vector3f(-1.0f,-1.0f,-1.0f);
		return new Vector3f(u,v,w);
	}

    
	protected Vector3f interpolateWithBarycentricCoords(Vector3f point, Vector3f interpolation_extremes[], Vector3f interpolation_obj[]) {
		Vector3f pA=(interpolation_extremes)[0].clone();
		Vector3f pB=(interpolation_extremes)[1].clone();
		Vector3f pC=(interpolation_extremes)[2].clone();
		Vector3f bc=barycentricCoords(pA,pB,pC,point);
		return (interpolation_obj)[0].mult(bc.x).addLocal((interpolation_obj)[1].mult(bc.y)).addLocal((interpolation_obj)[2].mult(bc.z));
	}
    
    public Vector3f getNormalAt(Vector3f point){
		return interpolateWithBarycentricCoords(point,new Vector3f[]{v1.pos,v2.pos,v3.pos},
				new Vector3f[]{v1.normal,v2.normal,v3.normal});
	}
}
