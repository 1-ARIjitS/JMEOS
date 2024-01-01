package types.temporal;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import jnr.ffi.Pointer;

/**
 * Base class for temporal instant
 *
 * @param <V>
 */
public abstract class TInstant<V extends Serializable> extends Temporal<V> {
	private TemporalValue<V> temporalValue = null;
	private Pointer _inner = null;


	public TInstant(){
		super();
	}


	public TInstant(Pointer inner){
		super(inner);
		this._inner = createInner(inner);
	}

	public TInstant(String str){
		super(str);
		this._inner = createStringInner(str);
	}

	public abstract Pointer createStringInner(String str);
	public abstract Pointer createInner(Pointer inner);
	
}
