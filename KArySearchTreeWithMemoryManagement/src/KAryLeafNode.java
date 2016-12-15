import java.util.concurrent.atomic.AtomicReferenceArray;


public class KAryLeafNode extends KAryBaseNode {
	//public AtomicReferenceArray<KAryBaseNode> children = null;
	public int maxKeyCount = 0;
	public KAryLeafNode(int K){
		this.keys = new int[K-1];
		for(int i = 0; i < K-1; ++i){
			this.keys[i] = Integer.MAX_VALUE;
		}
		this.keyCount = 0;
		this.children = null;
		this.maxKeyCount = K-1;
	}
	
	public KAryLeafNode(int key, boolean keyValue){
		this.keys = new int[]{key};
		this.children = null;
		this.keyCount = 1;
	}
	// Sprouting Insertion
	public KAryLeafNode(int key, KAryLeafNode leaf){
		this.children = null;
		this.keyCount = leaf.keyCount + 1;
		this.keys = new int[keyCount];
		
		int i;
		for(i = 0; i < leaf.keyCount; ++i){
			if( key < leaf.keys[i] )
				break;
		}
		for(int j = 0; j < i; ++j){
			this.keys[j] = leaf.keys[j];
		}
		this.keys[i] = key;
		for(int j = i; j < leaf.keyCount;++j){
			this.keys[j+1] = leaf.keys[j];
		}
	}
	
	public boolean hasKey(int key) {
        for (int i=0;i<keyCount;i++) {
            if ( i < maxKeyCount && key == this.keys[i])
            	return true;
        }
        return false;
    }
	
	public KAryLeafNode(int key, KAryLeafNode leaf, boolean deleted){
		this.children = null;
		this.keyCount = leaf.keyCount-1;
		this.keys = new int[this.keyCount];
		for(int i = 0; i < leaf.keyCount; ++i){
			if( key == leaf.keys[i]){
				for(int j = 0;j < i; ++j){
					this.keys[j] = leaf.keys[j];
				}
				for(int j = i+1; j < leaf.keyCount; ++j){
					this.keys[j-1] = leaf.keys[j];
				}
			}
		}
	}
	
	protected boolean isLeaf(){
		return true;
	}
}
