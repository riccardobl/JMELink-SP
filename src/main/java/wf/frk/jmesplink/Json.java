package wf.frk.jmesplink;

import java.util.Map;

public interface Json{
	public Object parse(String json);
	public String stringify(Map<Object,Object> map);
	
}
