import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicStampedReference;


public class KAryInternalNode extends KAryBaseNode {
	
	//public AtomicReferenceArray<AtomicStampedReference<KAryInternalNode>> children;
	
	public volatile Info info = null;
	
	public KAryInternalNode(int K, boolean root){
		this.keys = new int[K-1];
		this.keyCount = K-1;
		for(int i = 0; i < keyCount; ++i){
			this.keys[i] = Integer.MAX_VALUE;
		}
		if(root){
			this.children = new ArrayList<AtomicStampedReference<KAryBaseNode>>(K);
			this.children.add(new AtomicStampedReference<KAryBaseNode>(new KAryInternalNode(K,false),0 ) );
			for(int i = 1; i < K; ++i ){
				this.children.add(new AtomicStampedReference<KAryBaseNode>(new KAryLeafNode(K),0) );
			}
		}
		else{
			this.children = new ArrayList<AtomicStampedReference<KAryBaseNode>>(K);
			for(int i = 0; i < K; ++i ){
				this.children.add(new AtomicStampedReference<KAryBaseNode>(new KAryLeafNode(K),0));
			}
		}
	}
	
	
	public KAryInternalNode(int key, KAryBaseNode leaf){
		this.keyCount = leaf.keyCount;
		int i;
		for(i = 0; i < keyCount; ++i){
			if( key < leaf.keys[i])
				break;
		}
		this.children = new ArrayList<AtomicStampedReference<KAryBaseNode>>(keyCount+1);
		for(int j = 0; j < i; ++j){
			//this.children.set(j, new KAryLeafNode(leaf.keys[j],true));
			this.children.set(j, new AtomicStampedReference<KAryBaseNode>(new KAryLeafNode(leaf.keys[j],true),0));
		}
		//this.children.set(i, new KAryLeafNode(key,true));
		this.children.set(i, new AtomicStampedReference<KAryBaseNode>(new KAryLeafNode(key,true),0));
		for(int j = i ; j < keyCount; ++j){
			//this.children.set(j+1, new KAryLeafNode(leaf.keys[j],true));
			this.children.set(j+1, new AtomicStampedReference<KAryBaseNode>(new KAryLeafNode(leaf.keys[j],true),0));
		}
		this.keys = new int[keyCount];
		int []stamp = new int[1];
		for(int j = 0; j < keyCount; ++j){
			//this.keys[j] = this.children.get(j+1).keys[0];
			this.keys[j] = this.children.get(j+1).get(stamp).keys[0];
		}
	}
	
	protected boolean isLeaf(){
		return false;
	}
	
}
