package wf.frk.jmesplink.substances;

import java.util.Map;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;

import wf.frk.jmesplink.SubstanceLinkAppState;

public interface SubstanceDef{
	public boolean canMap(Substance s);
	public Exporter getExporter(Substance s,SubstanceLinkAppState slink);
	public Material substanceToMaterial(AssetManager am,Substance s,String substance_assets_path);
}
