import java.util.concurrent.atomic.AtomicStampedReference;


public class DeleteInfo implements Info {
	KAryBaseNode p,gp;
	KAryBaseNode oldChild;
	Info pinfo;
	int gpindex;
	int pindex;
	int parentStamp;
	DeleteInfo(KAryBaseNode l,KAryBaseNode p, KAryBaseNode gp,
			Info pinfo, int gpindex,int pindex, int stamp){
		this.p = p;
		this.oldChild = l;
		this.gp = gp;
		this.pinfo = pinfo;
		this.gpindex = gpindex;
		this.pindex = pindex;
		this.parentStamp = stamp;
	}
	
	@Override
	public boolean equals(Object o){
		DeleteInfo x = (DeleteInfo)o;
		if(x.p != p
			|| x.gp != gp
			|| x.oldChild != oldChild
			|| x.pinfo != pinfo
			|| x.gpindex != gpindex
			|| x.parentStamp != parentStamp)
			return false;
		return true;
	}
}