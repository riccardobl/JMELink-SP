import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import wf.frk.jmesplink.Json;
import wf.frk.jmesplink.link.SubstanceLink;

public class Test{
	public static void main(String[] args) throws UnknownHostException, IOException {
		SubstanceLink link=new SubstanceLink(6403,new Json(){
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
			System.out.println(link.cmd("alg.shaders.shaderInstancesToObject()"));

	}
}
