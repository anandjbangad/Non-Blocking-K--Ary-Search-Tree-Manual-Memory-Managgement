import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicStampedReference;


public class KAryBaseNode {
	
	public volatile int keyCount;
	public int[] keys;
	public ArrayList<AtomicStampedReference<KAryBaseNode>> children;
	public KAryBaseNode next;
	public KAryBaseNode(){
		this.keys = null;
		this.keyCount = 0;
		this.next = null;
	}
	
	protected boolean isLeaf(){
		return false;
	}
}
