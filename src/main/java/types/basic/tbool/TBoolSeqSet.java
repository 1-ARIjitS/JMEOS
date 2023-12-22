package types.basic.tbool;

import functions.functions;
import jnr.ffi.Pointer;
import types.temporal.TSequenceSet;
import types.temporal.TemporalType;


public class TBoolSeqSet extends TSequenceSet<Boolean> implements TBool{
	private Pointer inner;
	private String customType = "Boolean";
	private TemporalType temporalType = TemporalType.TEMPORAL_SEQUENCE_SET;

	public TBoolSeqSet(Pointer inner){
		super(inner);
		this.inner = inner;
	}

	/**
	 * The string constructor
	 *
	 * @param value - the string with the TBoolInst value
	 */
	public TBoolSeqSet(String value) {
		super(value);
		this.inner = functions.tbool_in(value);
	}



	@Override
	public Pointer createStringInner(String str){
		return functions.tbool_in(str);
	}

	@Override
	public Pointer createInner(Pointer inner){
		return inner;
	}

	@Override
	public String getCustomType(){
		return this.customType;
	}

	@Override
	public TemporalType getTemporalType(){
		return this.temporalType;
	}

	@Override
	public Pointer getBoolInner(){
		return inner;
	}

}
