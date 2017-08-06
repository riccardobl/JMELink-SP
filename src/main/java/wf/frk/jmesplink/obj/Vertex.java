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

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

//import com.jme3.math.Vector2f;
//import com.jme3.math.Vector3f;


/**
 *  A position and some associated attributes.
 *
 *  @author    Paul Speed
 */
public class Vertex  {
    public Vector3f pos = new Vector3f();
    public Vector3f normal;
    public Vector3f tangent;
    public Vector2f uv;
    public int index;
    public int group;
    

 
    /**
     *  The number of triangles that share this vertex.
     */   
    public int usageCount;
    
    /**
     *  Used in the smoothing calculations and can be used
     *  to remove a vertex from smoothing.
     */
    public float weight;
    
    public Vertex() {
    }
    
    public Vertex( float x, float y, float z ) {
        this.pos.set(x,y,z); 
    }
    
    public Vertex( Vector3f v ) {
        this.pos.set(v); 
    }
 
    public Vertex clone() {
            Vertex result = new Vertex();
            result.pos = pos.clone();
            if( normal != null ) {
                result.normal = normal.clone();
            }
            if( tangent != null ) {
                result.tangent = tangent.clone();
            }
            if( uv != null ) {
                result.uv = uv.clone();
            }            
            return result;
   
    }
    
    public boolean isSame( Vector3f v, float epsilon ) {
        return isSame(v.x, v.y, v.z, epsilon);
    }
    
    public boolean isSame( float x, float y, float z, float epsilon ) {
        if( Math.abs(x - pos.x) > epsilon ) {
            return false;
        }        
        if( Math.abs(y - pos.y) > epsilon ) {
            return false;
        }        
        if( Math.abs(z - pos.z) > epsilon ) {
            return false;
        }
        return true;        
    }

    public boolean isSame( float x, float y, float z, float u, float v, float epsilon ) {
        if( Math.abs(x - pos.x) > epsilon ) {
            return false;
        }        
        if( Math.abs(y - pos.y) > epsilon ) {
            return false;
        }        
        if( Math.abs(z - pos.z) > epsilon ) {
            return false;
        }
        if( uv != null ) {
            if( Math.abs(u - uv.x) > epsilon ) {
                return false;
            }
            if( Math.abs(v - uv.y) > epsilon ) {
                return false;
            }
        }
        return true;        
    }
    
    @Override
    public String toString() {
        return "Vertex[pos=" + pos + ", normal=" + normal + ", uv=" + uv + ", group=" + group + "]";
    }
}
