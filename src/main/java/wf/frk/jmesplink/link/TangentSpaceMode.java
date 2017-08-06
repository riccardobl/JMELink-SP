package wf.frk.jmesplink.link;
public enum TangentSpaceMode{
		PER_VERTEX("perVertex"),
		PER_FRAGMENT("perFragment");
		public String mode;
		private TangentSpaceMode(String mode){
			this.mode=mode;
		}
		
	}