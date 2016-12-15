import java.util.concurrent.atomic.AtomicStampedReference;


public class UpdateInfo {
	AtomicStampedReference<Info> infoRef;
	UpdateInfo(Info info, int state){
		infoRef = new AtomicStampedReference<Info>(info, state);
	}
	
	int getState(){
		return infoRef.getStamp();
	}
	
	Info getInfo(){
		return infoRef.getReference();
	}
	
	Info getStateHolder(int[] state){
		return infoRef.get(state);
	}
	
}
