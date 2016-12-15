import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.atomic.AtomicStampedReference;


public class KArySearchTree {
	public KAryInternalNode root;
	private int K;
	private int consoleType;
	private static final AtomicReferenceFieldUpdater<KAryInternalNode, Info> infoUpdater =
	        AtomicReferenceFieldUpdater.newUpdater(KAryInternalNode.class, Info.class, "info");
	
	ThreadLocal<KAryInternalNode> freeListInternal = new ThreadLocal<KAryInternalNode>(){
		protected KAryInternalNode initialValue(){
			KAryInternalNode headNode = new KAryInternalNode(K,false);
			KAryInternalNode temp = new KAryInternalNode(K,false);
			headNode.next = temp;
			for(int i = 0 ; i < 100; ++i ){
				KAryInternalNode newNode = new KAryInternalNode(K, false);
				temp.next = newNode;
				temp = newNode;
			}
			return headNode;
		}
	};
	
	ThreadLocal <KAryBaseNode> freeListLeaf = new ThreadLocal<KAryBaseNode>(){
		protected KAryBaseNode initialValue(){
			KAryLeafNode headNode = new KAryLeafNode(K);
			KAryLeafNode temp = new KAryLeafNode(K);
			headNode.next = temp;
			for(int i = 0 ; i < 100; ++i ){
				KAryLeafNode newNode = new KAryLeafNode(K);
				temp.next = newNode;
				temp = newNode;
			}
			return headNode;
		}
	};
	
	private KAryInternalNode allocateInternalNode(){
		KAryInternalNode newNode = freeListInternal.get();
		if(newNode == null){
			newNode = new KAryInternalNode(K,false);
		}else{
			freeListInternal.set((KAryInternalNode)newNode.next);
			if(consoleType == 1)
				System.out.println("Memory allocated from free Internal pool"+newNode);
		}
		return newNode;
	}
	
	private KAryBaseNode allocateLeafNode(){
		KAryBaseNode newNode = freeListLeaf.get();
		if(newNode == null){
			newNode = new KAryLeafNode(K);
		}
		else{
			freeListLeaf.set(newNode.next);
			if(consoleType == 1)
				System.out.println("Memory allocated from free Leaf pool"+newNode);
		}
		return newNode;
	}
	
	private void moveToInternalFree(KAryInternalNode node){
		KAryInternalNode head = freeListInternal.get();
		node.next = head;
		if(consoleType == 1)
			System.out.println("Internal Node moved to free pool"+node);
		freeListInternal.set(node);
	}
	
	private void moveToLeafFree(KAryBaseNode node){
		KAryBaseNode head = freeListLeaf.get();
		node.next = head;
		if(consoleType == 1)
			System.out.println("Leaf Node moved to free pool"+node);
		freeListLeaf.set(node);
	}
	
	public KArySearchTree(int K,int consoleType){
		this(K, new KAryInternalNode(K, true));
		this.consoleType = consoleType;
	}
	
	public KArySearchTree(int K, KAryInternalNode root){
		this.root = root;
		this.K = K;
	}
	
	private AtomicStampedReference<KAryBaseNode> searchKey(int key, KAryInternalNode l){
		if(l.keys[0] == Integer.MAX_VALUE)
			return l.children.get(0);
		int left = 0;
		int right = l.keyCount-1;
		while(right > left){
			int mid = (left+right)/2;
			if( key < l.keys[mid]){
				right = mid;
			}
			else{
				left = mid+1;
			}
		}
		if(left == l.keyCount-1 && (key >= l.keys[left]) )
			return l.children.get(l.keyCount);
		return l.children.get(left);
	}
	
	public boolean containsKey(int key){
		if(key == -1)
			return false;
		int[] stamp = new int[1];
		KAryBaseNode l = root.children.get(0).get(stamp);
		while( l!= null && !l.isLeaf()){
			AtomicStampedReference<KAryBaseNode> baseNode = searchKey(key, (KAryInternalNode)l);
			l = baseNode.get(stamp);
		}
		if(l == null){
			return false;
		}
		return ((KAryLeafNode)l).hasKey(key);
	}
	
	public boolean insert(int key){
		//System.out.println("Insert Method Called");
		if(key == -1)
			return false;
		try{int[] stamp = new int[1];
		int[] oldLeafStamp = new int[1];
		int[] newLeafStamp = new int[1];
		KAryInternalNode p;
		KAryBaseNode l,newChild;
		AtomicStampedReference<KAryBaseNode> oldStampedChild;
		AtomicStampedReference<KAryBaseNode> newStampedChild;
		Info pinfo;
		int pindex;
		long startTime = System.currentTimeMillis();
		long elapsedTime = System.currentTimeMillis();
		while(true ){
			if( (elapsedTime - startTime) > 100 ) 
				return false;
			p = root;
			pinfo = p.info;
			oldStampedChild = p.children.get(0);
			l = p.children.get(0).get(oldLeafStamp);
					
			while(l!= null && !l.isLeaf()){
				p = (KAryInternalNode)l;
				oldStampedChild = searchKey(key,p);
				l = oldStampedChild.get(oldLeafStamp);
			}
			
			pinfo = p.info;
			KAryBaseNode currentL = (KAryBaseNode)p.children.get(p.keyCount).getReference();
			pindex = p.keyCount;
			for(int i = 0; i < p.keyCount; ++i){
				if( key < p.keys[i] ){
					currentL = (KAryBaseNode)p.children.get(i).get(stamp);
					pindex = i;
					break;
				}
			}
			if(l!= currentL)
				continue;
			
			if(((KAryLeafNode)l).hasKey(key))
				return false;
			else if(pinfo != null && pinfo.getClass() != Clean.class){
				help(pinfo);
			}
			else{
				if(l.keyCount == K-1){
					newChild = allocateInternalNode();
					newChild.keyCount = l.keyCount;
					
					int idx;
					for(idx = 0; idx < newChild.keyCount; ++idx){
						if( key < l.keys[idx])
							break;
					}
					KAryBaseNode node;
					AtomicStampedReference<KAryBaseNode> stampedNode;
					for(int j = 0; j < idx; ++j){
						node = allocateLeafNode();
						node.keyCount = 1;
						node.keys[0] = l.keys[j];
						stampedNode = newChild.children.get(j);
						stampedNode.set(node, stampedNode.getStamp()+1);
						newChild.children.set(j,stampedNode);
					}
					node = allocateLeafNode();
					node.keyCount = 1;
					node.keys[0] = key;
					stampedNode = newChild.children.get(idx);
					stampedNode.set(node, stampedNode.getStamp()+1);
					newChild.children.set(idx, stampedNode);
					for(int j = idx ; j < l.keyCount; ++j){
						node = allocateLeafNode();
						node.keyCount = 1;
						node.keys[0] = l.keys[j];
						stampedNode = newChild.children.get(j);
						stampedNode.set(node, stampedNode.getStamp()+1);
						newChild.children.set(j+1, stampedNode);
					}
					for(int j = 0; j < newChild.keyCount; ++j){
						newChild.keys[j] = newChild.children.get(j+1).getReference().keys[0];
					}
				}
				else{
					newChild = allocateLeafNode();
					newChild.keyCount = l.keyCount + 1;
					try{
					int k;
					for(k = 0; k < l.keyCount; ++k){
						if( key < l.keys[k] )
							break;
					}
					
					for(int j = 0; j < k; ++j){
						newChild.keys[j] = l.keys[j];
					}
					newChild.keys[k] = key;
					for(int j = k; j < l.keyCount;++j){
						newChild.keys[j+1] = l.keys[j];	
					}}																																							catch(Exception e){}
				}
				InsertInfo newPInfo = new InsertInfo(l,p,newChild,pindex,oldLeafStamp[0]);
				if( infoUpdater.compareAndSet(p, pinfo, newPInfo)){
					helpInsert(newPInfo);
					return true;
				}
				else{
					help(p.info);
				}
			}	
			elapsedTime = System.currentTimeMillis();
		}}																																																														catch(Exception e){return false;}
	}
	
	public boolean remove(int key){
		//System.out.println("Remove Method Called");
		if(key == -1)
			return false;
		
		try{int[] stamp = new int[1];
		int[] oldLeafStamp = new int[1];
		int[] oldParentStamp = new int[1];
		int[] newLeafStamp = new int[1];
		
		KAryInternalNode gp,p;
		KAryBaseNode l,newChild;
		
		AtomicStampedReference<KAryBaseNode> oldStampedChild;
		AtomicStampedReference<KAryBaseNode> newStampedChild;
		
		Info gpinfo,pinfo;
		int pindex;
		int gpindex;
		long startTime = System.currentTimeMillis();
		long elapsedTime = System.currentTimeMillis();
		while(true){
			if( (elapsedTime - startTime) > 100 ) 
				return false;
			gp = null;
			gpinfo = null;
			p = root;
			pinfo = p.info;
			l = root.children.get(0).get(stamp);
			oldStampedChild = root.children.get(0);
			
			while(l!= null && !l.isLeaf()){
				gp = p;
				p = (KAryInternalNode)l;
				oldStampedChild = searchKey(key, p);
				l = oldStampedChild.get(oldLeafStamp);
			}
			gpinfo = gp.info;
			
			KAryBaseNode currentP = gp.children.get(gp.keyCount).getReference(); 
			gpindex = gp.keyCount;
			for(int i = 0; i < gp.keyCount; ++i){
				if( key < gp.keys[i] ){
					try{currentP = (KAryInternalNode)gp.children.get(i).get(oldParentStamp);}																													catch(Exception e){}
					gpindex = i;
					break;
				}
			}
			if(p != (KAryInternalNode)currentP)
				continue;
			
			pinfo = p.info;
			KAryBaseNode currentL = p.children.get(p.keyCount).getReference();
			pindex = p.keyCount;
			for(int i = 0; i < p.keyCount; ++i){
				if( key < p.keys[i] ){
					currentL = p.children.get(i).get(oldLeafStamp);
					pindex = i;
					break;
				}
			}
			
			if(l != currentL)
				continue;
			
			if(!(((KAryLeafNode)l).hasKey(key)) )
				return false;
			else if(gpinfo!= null && gpinfo.getClass() != Clean.class)
				help(gpinfo);
			else if(pinfo!= null && pinfo.getClass() != Clean.class)
				help(pinfo);
			else{
				int ccount = 0;
				if(l.keyCount == 1){
					for(int i = 0; i <= p.keyCount; ++i){
						if(p.children.get(i).getReference().keyCount > 0){
							ccount++;
						}
						if(ccount > 2)
							break;
					}
				}
				
				if(l.keyCount == 1 && ccount == 2){
					//System.out.println("Sprouting Deletion Case");
					DeleteInfo newGPInfo = new DeleteInfo(l, p, gp, pinfo, gpindex,pindex, oldParentStamp[0]);
					if(infoUpdater.compareAndSet(gp, gpinfo, newGPInfo)){
						if(helpDelete(newGPInfo))
							return true;
						else
							help(gp.info);
					}
				}
				//Simple Deletion
				else{
					newChild = allocateLeafNode();
					newChild.keyCount = l.keyCount - 1;
					try{int i,j;
					for( i = 0; i < l.keyCount; ++i){
						if( key == l.keys[i]){
							for( j = 0;j < i; ++j){
								newChild.keys[j] = l.keys[j];
							}
							for( j = i+1; j < l.keyCount; ++j){
								newChild.keys[j-1] = l.keys[j];
							}
						}
					}}																																							catch(Exception e){}
					InsertInfo newPInfo = new InsertInfo(l,p,newChild,pindex,oldLeafStamp[0]);
					if(infoUpdater.compareAndSet(p, pinfo, newPInfo)){
						helpInsert(newPInfo);
						return true;
					}
					else{
						help(p.info);
					}
				}
			}
			elapsedTime = System.currentTimeMillis();
		}}																																										catch(Exception e){return false;}
	}
	
	private void help(Info info){
		if(info.getClass() == InsertInfo.class){
			helpInsert((InsertInfo)info);
		}
		else if(info.getClass() == DeleteInfo.class){
			helpDelete((DeleteInfo)info);
		}
		else if(info.getClass() == MarkInfo.class){
			helpMarked(((MarkInfo)info).dinfo);
		}
	}
	
	private void helpInsert(InsertInfo info){
		boolean value = false;
		KAryBaseNode leafToBeRecycled = info.oldChild;
		value = info.p.children.get(info.pindex).compareAndSet(info.oldChild, info.newChild, info.oldStamp, info.oldStamp+1);
		if(value == true){
			for(int i = 0; i < K-1;++i){
				leafToBeRecycled.keys[i] = Integer.MAX_VALUE;
			}
			leafToBeRecycled.keyCount = 0;
			moveToLeafFree(leafToBeRecycled);
			infoUpdater.compareAndSet((KAryInternalNode)info.p, info, new Clean());
		}	
	}
	
	private boolean helpDelete(DeleteInfo info){
		boolean markSuccess = infoUpdater.compareAndSet((KAryInternalNode)info.p, info.pinfo, new MarkInfo(info));
		Info currentPInfo = ((KAryInternalNode)info.p).info;
		if(markSuccess || currentPInfo.getClass() == MarkInfo.class 
				&& ((MarkInfo)currentPInfo).dinfo == info ){
			helpMarked(info);
			return true;
		}
		else{
			help(currentPInfo);
			infoUpdater.compareAndSet((KAryInternalNode)info.gp, info, new Clean());
			return false;
		}
	}
	
	private void helpMarked(DeleteInfo info){
		int[]stamp = new int[1];
		KAryBaseNode other = info.p.children.get(info.p.keyCount).get(stamp);
		int index = 0;
		for(int i = 0; i < info.p.keyCount ;++i){
			KAryBaseNode u = info.p.children.get(i).getReference();
			if( u!= null && u.keyCount > 0 && u != info.oldChild ){
				other = u;
				index = i;
				break;
			}
		}
		boolean casValue = false;
		casValue = info.gp.children.get(info.gpindex).compareAndSet(info.p, other, info.parentStamp, info.parentStamp+1);
		
		if(casValue){
			KAryInternalNode parentNode = (KAryInternalNode)info.p;
			for(int i = 0; i < K ; ++i){
				AtomicStampedReference<KAryBaseNode> recycledStamp;
				KAryBaseNode recycledNode;
				
				if(i != info.pindex){
					recycledStamp = parentNode.children.get(i);
					recycledNode = recycledStamp.getReference();
					//recycledStamp.set(null,stamp[0]+1);
					if(recycledNode != null){
						for(int j = 0; j < K-1; ++j ){
							recycledNode.keys[j] = Integer.MAX_VALUE;
						}
						recycledNode.keyCount = 0;
						moveToLeafFree(recycledNode);
					}
				}
			}
			parentNode.info = new Clean();
			parentNode.keyCount = 0;
			moveToInternalFree(parentNode);
			infoUpdater.compareAndSet((KAryInternalNode)info.gp, info, new Clean());
		}
	}
}
