
public class MarkInfo implements Info {
	public DeleteInfo dinfo;
	MarkInfo(DeleteInfo dinfo){
		this.dinfo = dinfo;
	}
	
	@Override
	public boolean equals(Object o){
		MarkInfo x = (MarkInfo)o;
		if(x.dinfo != dinfo)
			return false;
		return true;
	}
}
