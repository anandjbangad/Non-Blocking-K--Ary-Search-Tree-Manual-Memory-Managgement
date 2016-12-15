import java.util.concurrent.atomic.AtomicStampedReference;


public class InsertInfo implements Info{
	KAryBaseNode p;
	KAryBaseNode  newChild;
	KAryBaseNode oldChild;
	int pindex;
	int oldStamp;
	int newStamp;
	InsertInfo( KAryBaseNode oldChild, KAryBaseNode p, KAryBaseNode newChild,
			int pindex,int oldStamp){
		this.p = p;
		this.oldChild = oldChild;
		this.newChild = newChild;
		this.pindex = pindex;
		this.oldStamp = oldStamp;
	}
	
	//@Override
	public boolean equals(Object o){
		InsertInfo x = (InsertInfo)o;
		if(x.p != p
			|| x.oldChild != oldChild
			|| x.newChild != newChild
			|| x.pindex != pindex
			|| x.oldStamp!= oldStamp)
			return false;
		return true;
	}
}
