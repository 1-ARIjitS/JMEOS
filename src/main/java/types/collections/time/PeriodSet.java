package types.collections.time;

import functions.functions;
import jnr.ffi.Pointer;
import types.TemporalObject;
import types.collections.base.Span;
import types.collections.base.SpanSet;
import types.core.TypeName;

import types.boxes.*;

import javax.naming.OperationNotSupportedException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;




/**
 * Class for representing lists of disjoint periods.
 *<p>
 *     ``PeriodSet`` objects can be created with a single argument of type string
 *     as in MobilityDB.
 *<p>
 *         >>> PeriodSet(string='{[2019-09-08 00:00:00+01, 2019-09-10 00:00:00+01], [2019-09-11 00:00:00+01, 2019-09-12 00:00:00+01]}')
 *<p>
 *     Another possibility is to give a list specifying the composing
 *     periods, which can be instances  of ``str`` or ``Period``. The composing
 *     periods must be given in increasing order.
 *<p>
 *         >>> PeriodSet(period_list=['[2019-09-08 00:00:00+01, 2019-09-10 00:00:00+01]', '[2019-09-11 00:00:00+01, 2019-09-12 00:00:00+01]'])
 *         >>> PeriodSet(period_list=[Period('[2019-09-08 00:00:00+01, 2019-09-10 00:00:00+01]'), Period('[2019-09-11 00:00:00+01, 2019-09-12 00:00:00+01]')])
 *
 * @author Nidhal Mareghni
 * @since 10/09/2023
 */
@TypeName(name = "periodset")
public class PeriodSet extends SpanSet<LocalDateTime> implements Time, TimeCollection {
	private List<Period> periodList = null;
	private Pointer _inner;

	/* ------------------------- Constructors ---------------------------------- */
	/**
	 * The default constructor
	 */
	public PeriodSet() {
	}
	
	public PeriodSet(Pointer _inner) {
		super(_inner);
		this._inner = _inner;
		String str = functions.periodset_out(this._inner);
	}
	
	/**
	 * The string constructor
	 *
	 * @param value - a string with a PeriodSet value
	 */
	public PeriodSet(String value) {
		super(value);
		this._inner = functions.periodset_in(value);
	}
	
	/**
	 * The array of Periods constructor
	 *
	 * @param periods - an array of Periods separated by a comma
	 */
	public PeriodSet(Period... periods)  {
		super(periods);
		periodList = new ArrayList<Period>();
	}

	@Override
	public Pointer createStringInner(String str){
		return functions.periodset_in(str);
	}

	@Override
	public Pointer createInner(Pointer inner){
		return _inner;
	}

	@Override
	public Pointer createListInner(Period... periods){
		return null;
	}

	/**
	 * Return a copy of "this".
	 * <p>
	 * Meos Functions:
	 *
	 *     <li>spanset_copy
	 *
	 * @return a new PeriodSet instance
	 */

	public PeriodSet copy()  {
		return new PeriodSet(functions.spanset_copy(this._inner));
	}


	/**
	 * Returns a "PeriodSet" from its WKB representation in hex-encoded ASCII.
	 * <p>
	 * MEOS Functions:
	 *             <li>spanset_from_hexwkb
	 * @param str WKB representation in hex-encoded ASCII
	 * @return a new PeriodSet instance
	 */
	public static PeriodSet from_hexwkb(String str)  {
		Pointer result = functions.spanset_from_hexwkb(str);
		return new PeriodSet(result);
	}


	/** ------------------------- Output ---------------------------------------- */


	/**
	 * Return the string representation of the content of "this".
	 * <p>
	 *         MEOS Functions:
	 *             <li>periodset_out
	 * @return a new String instance
	 */
	public String toString(){
		return functions.periodset_out(this._inner);
	}
	/** ------------------------- Conversions ----------------------------------- */

	/**
	 * Returns a period that encompasses "this".
	 * <p>
	 *         MEOS Functions:
	 *             <li>spanset_span
	 * @return a new Period instance
	 */
	public Period to_period() {
		return new Period(functions.spanset_span(this._inner));
	}

	/** ------------------------- Accessors ------------------------------------- */


	public Pointer get_inner(){
		return this._inner;
	}

	/**
	 * Gets the interval on which the temporal value is defined
	 *
	 * @return a duration
	 */
	public Duration duration() {
		Duration d = Duration.ZERO;

		for (Period p : periodList) {
			d = d.plus(p.duration());
		}

		return d;
	}

	/**
	 * Returns the number of timestamps in "this".
	 * <p>
	 *         MEOS Functions:
	 *             <li>periodset_num_timestamps
	 * @return an Integer instance
	 */
	public int num_timestamps(){
		return functions.periodset_num_timestamps(this._inner);
	}

	/**
	 * Returns the number of periods in "this".
	 * <p>
	 *         MEOS Functions:
	 *             <li>spanset_num_spans
	 * @return an Integer instance
	 */
	public int num_periods(){
		return functions.spanset_num_spans(this._inner);
	}


	/**
	 * Returns the first period in "this".
	 * <p>
	 *         MEOS Functions:
	 *             <li>periodset_lower
	 * @return a new Period instance
	 */
	public Period start_period()  {
		return new Period(functions.spanset_start_span(this._inner));
	}

	/**
	 * Returns the last period in "this".
	 * <p>
	 *         MEOS Functions:
	 *             <li>periodset_upper
	 * @return a new Period instance
	 */
	public Period end_period() {
		return new Period(functions.spanset_end_span(this._inner));
	}

	/**
	 * Return the hash representation of "this".
	 * <p>
	 * MEOS Functions:
	 *             <li>spanset_hash
	 * @return a new Integer instance
	 */
	public long hash(){
		return functions.spanset_hash(this._inner);
	}


	/** ------------------------- Topological Operations ------------------------ */

	/**
	 * Returns whether "this" is temporally adjacent to "other".
	 * That is, they share a bound but only one of them contains it.
	 * <pre>
	 * Examples:
	 * >>> PeriodSet('{[2012-01-01, 2012-01-02)}').is_adjacent(PeriodSet('{[2012-01-02, 2012-01-03]}'))
	 * >>> True
	 * >>> PeriodSet('{[2012-01-01, 2012-01-02]}').is_adjacent(PeriodSet('{[2012-01-02, 2012-01-03]}'))
	 * >>> False  # Both contain bound
	 * >>> PeriodSet('{[2012-01-01, 2012-01-02)}').is_adjacent(PeriodSet('{[(2012-01-02, 2012-01-03]]}'))
	 * >>> False  # Neither contain bound
	 * </pre>
	 * MEOS Functions:
	 * <ul>
	 *     <li>adjacent_spanset_span</li>
	 *     <li>adjacent_spanset_spanset</li>
	 *     <li>adjacent_periodset_timestamp</li>
	 *     <li>adjacent_periodset_timestampset</li>
	 * </ul>
	 *
	 * @param other temporal object to compare with
	 * @return True if adjacent, False otherwise
	 */
	public boolean is_adjacent(Time other) {
		boolean returnValue;
		switch (other) {
			case Period p -> returnValue = functions.adjacent_spanset_span(this._inner, p.get_inner());
			case PeriodSet ps -> returnValue = functions.adjacent_spanset_spanset(this._inner, ps.get_inner());
			//case DateTime dt -> returnValue = functions.adjacent_periodset_timestamp(this._inner, ConversionUtils.datetimeToTimestampTz(dt.get_inner()));
			case TimestampSet ts -> returnValue = functions.adjacent_spanset_spanset(this._inner, functions.set_to_spanset(ts.get_inner()));
			// case Temporal t -> returnValue = functions.adjacent_spanset_spanset(this._inner, functions.temporal_time(t.period().get_inner()));
			//case Temporal t -> returnValue = functions.adjacent_spanset_span(this._inner, t.period().get_inner());
			case Box b -> returnValue = functions.adjacent_spanset_span(this._inner, b.to_period().get_inner());
			default -> throw new TypeNotPresentException(other.getClass().toString(), new Throwable("Operation not supported with this type"));
		}
		return returnValue;
	}


	/**
	 * Returns whether "this" is temporally contained in "other".
	 * <pre>
	 *     Examples:
	 *     >>> PeriodSet('{[2012-01-02, 2012-01-03]}').is_contained_in(Period('{[2012-01-01, 2012-01-04]}'))
	 *     >>> True
	 *     >>> PeriodSet('{(2012-01-01, 2012-01-02)}').is_contained_in(Period('{[2012-01-01, 2012-01-02]}'))
	 *     >>> True
	 *     >>> PeriodSet('{[2012-01-01, 2012-01-02]}').is_contained_in(Period('{(2012-01-01, 2012-01-02)}'))
	 *     >>> False
	 * </pre>
	 *         MEOS Functions:
	 *         <ul>
	 *             <li>contained_spanset_span </li>
	 *             <li>contained_spanset_spanset </li>
	 *             <li>contained_periodset_temporal</li>
	 *         </ul>
	 * @param other temporal object to compare with
	 * @return True if contained, False otherwise
	 */
	public boolean is_contained_in(Time other) {
		boolean returnValue;
		switch (other) {
			case Period p -> returnValue = functions.contained_spanset_span(this._inner, p.get_inner());
			case PeriodSet ps -> returnValue = functions.contained_spanset_spanset(this._inner, ps.get_inner());
			//case Temporal t -> returnValue = functions.contained_spanset_span(this._inner, t.period().get_inner());
			case Box b -> returnValue = functions.contained_spanset_span(this._inner, b.to_period().get_inner());
			default -> throw new TypeNotPresentException(other.getClass().toString(), new Throwable("Operation not supported with this type"));
		}
		return returnValue;
	}


	/**
	 * Returns whether ``self`` temporally contains ``content``.
	 * <pre>
	 *     Examples:
	 *     >>> PeriodSet('{[2012-01-01, 2012-01-04]}').contains(PeriodSet('{[2012-01-02, 2012-01-03]}'))
	 *     >>> True
	 *     >>> PeriodSet('{[2012-01-01, 2012-01-02]}').contains(PeriodSet('{(2012-01-01, 2012-01-02)}'))
	 *     >>> True
	 *     >>> PeriodSet('{(2012-01-01, 2012-01-02)}').contains(PeriodSet('{[2012-01-01, 2012-01-02]}'))
	 *     >>> False
	 * </pre>
	 *
	 *         MEOS Functions:
	 *         <ul>
	 *         		<li>contains_spanset_span </li>
	 *         		<li>contains_spanset_spanset </li>
	 *         		<li>contains_periodset_timestamp </li>
	 *         </ul>
	 * @param other temporal object to compare with
	 * @return True if contains, False otherwise
	 */
	public boolean contains(Time other) {
		boolean returnValue;
		switch (other) {
			case Period p -> returnValue = functions.contains_spanset_span(this._inner, p.get_inner());
			case PeriodSet ps -> returnValue = functions.contains_spanset_spanset(this._inner, ps.get_inner());
			case TimestampSet ts -> returnValue = functions.contains_spanset_spanset(this._inner, functions.set_to_spanset(ts.get_inner()));
			//case DateTime dt -> returnValue = functions.contains_periodset_timestamp(this._inner, ConversionUtils.datetimeToTimestampTz(dt.get_inner()));
			//case Temporal t -> returnValue = functions.contains_spanset_spanset(this._inner, t.period().get_inner());
			case Box b -> returnValue = functions.contains_spanset_span(this._inner, b.to_period().get_inner());
			default -> throw new TypeNotPresentException(other.getClass().toString(), new Throwable("Operation not supported with this type"));
		}
		return returnValue;
	}


	/**
	 * Returns whether "this" temporally overlaps "other". That is, both share at least an instant
	 *<pre>
	 *    Examples:
	 *    >>> PeriodSet('{[2012-01-01, 2012-01-02]}').overlaps(PeriodSet('{[2012-01-02, 2012-01-03]}'))
	 *    >>> True
	 *    >>> PeriodSet('{[2012-01-01, 2012-01-02)}').overlaps(PeriodSet('{[2012-01-02, 2012-01-03]}'))
	 *    >>> False
	 *    >>> PeriodSet('{[2012-01-01, 2012-01-02)}').overlaps(PeriodSet('{(2012-01-02, 2012-01-03]}'))
	 *    >>> False
	 *</pre>
	 *         MEOS Functions:
	 *         <ul>
	 *             <li>overlaps_spanset_span</li>
	 *             <li>overlaps_spanset_spanset</li>
	 *         </ul>
	 * @param other temporal object to compare with
	 * @return True if overlaps, False otherwise
	 */
	public boolean overlaps(Time other) {
		boolean returnValue;
		switch (other) {
			case Period p -> returnValue = functions.overlaps_spanset_span(this._inner, p.get_inner());
			case PeriodSet ps -> returnValue = functions.overlaps_spanset_spanset(this._inner, ps.get_inner());
			case TimestampSet ts -> returnValue = functions.overlaps_spanset_spanset(this._inner, functions.set_span(ts.get_inner()));
			//case DateTime dt -> returnValue = functions.overlaps_periodset_timestamp(this._inner, ConversionUtils.datetimeToTimestampTz(dt.get_inner()));
			//case Temporal t -> returnValue = functions.overlaps_spanset_spanset(this._inner, t.period().get_inner());
			case Box b -> returnValue = functions.overlaps_spanset_span(this._inner, b.to_period().get_inner());
			default -> throw new TypeNotPresentException(other.getClass().toString(), new Throwable("Operation not supported with this type"));
		}
		return returnValue;
	}


	/**
	 * Returns whether the bounding period of "this" is the same as the bounding period of "other".
	 *<p>
	 *
	 *         See Also:
	 * 				{@link Period#is_same(Time)}
	 * @param other A time or temporal object to compare to "this".
	 * @return True if same, False otherwise.
	 */
	public boolean is_same(Time other) {
		return this.to_period().is_same(other);
	}


	/** ------------------------- Position Operations --------------------------- */


	/**
	 * Returns whether "this" is strictly before "other". That is, "this" ends before "other" starts.
	 * <pre>
	 *    Examples:
	 *    >>> PeriodSet('{[2012-01-01, 2012-01-02)}').is_before(PeriodSet('{[2012-01-02, 2012-01-03]}'))
	 *    >>> True
	 *    >>> PeriodSet('{[2012-01-01, 2012-01-02)}').is_before(PeriodSet('{(2012-01-02, 2012-01-03]}'))
	 *    >>> True
	 *    >>> PeriodSet('{[2012-01-01, 2012-01-02]}').is_before(PeriodSet('{[2012-01-02, 2012-01-03]}'))
	 *    >>> False
	 *</pre>
	 *
	 *         MEOS Functions:
	 *         <ul>
	 *         <li>before_periodset_timestamp</li>
	 *         <li>left_spanset_span</li>
	 *         <li>left_spanset_spanset</li>
	 *         </ul>
	 * @param other temporal object to compare with
	 * @return True if before, False otherwise
	 */
	public boolean is_before(Time other) {
		boolean returnValue;
		switch (other) {
			case Period p -> returnValue = functions.left_spanset_span(this._inner, p.get_inner());
			case PeriodSet ps -> returnValue = functions.left_spanset_spanset(this._inner, ps.get_inner());
			case TimestampSet ts -> returnValue = functions.left_spanset_spanset(this._inner, functions.set_to_spanset(ts.get_inner()));
			//case DateTime dt -> returnValue = functions.left_periodset_timestamp(this._inner, ConversionUtils.datetimeToTimestampTz(dt.get_inner()));
			//case Temporal t -> returnValue = functions.left_spanset_spanset(this._inner, t.period().get_inner());
			case Box b -> returnValue = functions.left_spanset_span(this._inner, b.to_period().get_inner());
			default -> throw new TypeNotPresentException(other.getClass().toString(), new Throwable("Operation not supported with this type"));
		}
		return returnValue;
	}


	/**
	 *  Returns whether "this" is before "other" allowing overlap. That is, "this" ends before "other" ends (or
	 *         at the same time).
	 * <pre>
	 *    Examples:
	 *    >>> PeriodSet('{[2012-01-01, 2012-01-02)}').is_over_or_before(PeriodSet('{[2012-01-02, 2012-01-03]}'))
	 *    >>> True
	 *    >>> PeriodSet('{[2012-01-01, 2012-01-02]}').is_over_or_before(PeriodSet('{[2012-01-02, 2012-01-03]}'))
	 *    >>> True
	 *    >>> PeriodSet('{[2012-01-03, 2012-01-05]}').is_over_or_before(PeriodSet('{[2012-01-01, 2012-01-04]}'))
	 *    >>> False
	 *</pre>
	 *
	 *         MEOS Functions:
	 *         <ul>
	 *             <li>overleft_spanset_span</li>
	 *             <li>overleft_spanset_spanset</li>
	 *             <li>overbefore_periodset_timestamp</li>
	 *             <li>overbefore_periodset_timestampset</li>
	 *             <li>overbefore_periodset_temporal</li>
	 *         </ul>
	 * @param other temporal object to compare with
	 * @return True if before, False otherwise
	 */
	public boolean is_over_or_before(Time other) {
		boolean returnValue;
		switch (other) {
			case Period p -> returnValue = functions.overleft_spanset_span(this._inner, p.get_inner());
			case PeriodSet ps -> returnValue = functions.overleft_spanset_spanset(this._inner, ps.get_inner());
			case TimestampSet ts -> returnValue = functions.overleft_spanset_span(this._inner, functions.set_span(ts.get_inner()));
			//case DateTime dt -> returnValue = functions.overleft_periodset_timestamp(this._inner, ConversionUtils.datetimeToTimestampTz(dt.get_inner()));
			//case Temporal t -> returnValue = functions.overleft_spanset_spanset(this._inner, t.period().get_inner());
			case Box b -> returnValue = functions.overleft_spanset_span(this._inner, b.to_period().get_inner());
			default -> throw new TypeNotPresentException(other.getClass().toString(), new Throwable("Operation not supported with this type"));
		}
		return returnValue;
	}


	/**
	 * Returns whether "this" is strictly after "other".That is, "this" starts after "other" ends.
	 *<pre>
	 *    Examples:
	 *    >>> PeriodSet('{[2012-01-02, 2012-01-03]}').is_after(PeriodSet('{[2012-01-01, 2012-01-02)}'))
	 *    >>> True
	 *    >>> PeriodSet('{(2012-01-02, 2012-01-03]}').is_after(PeriodSet('{[2012-01-01, 2012-01-02)}'))
	 *    >>> True
	 *    >>> PeriodSet('{[2012-01-02, 2012-01-03]}').is_after(PeriodSet('{[2012-01-01, 2012-01-02]}'))
	 *    >>> False
	 *</pre>
	 *         MEOS Functions:
	 *         <ul>
	 *             <li>right_spanset_span </li>
	 *             <li>right_spanset_spanset </li>
	 *             <li>overbefore_timestamp_periodset </li>
	 *          </ul>
	 * @param other temporal object to compare with
	 * @return True if after, False otherwise
	 */
	public boolean is_after(Time other) {
		boolean returnValue;
		switch (other) {
			case Period p -> returnValue = functions.right_spanset_span(this._inner, p.get_inner());
			case PeriodSet ps -> returnValue = functions.right_spanset_spanset(this._inner, ps.get_inner());
			case TimestampSet ts -> returnValue = functions.right_spanset_span(this._inner, functions.set_span(ts.get_inner()));
			//case DateTime dt -> returnValue = functions.right_periodset_timestamp(this._inner, ConversionUtils.datetimeToTimestampTz(dt.get_inner()));
			//case Temporal t -> returnValue = functions.right_spanset_spanset(this._inner, t.period().get_inner());
			case Box b -> returnValue = functions.right_spanset_span(this._inner, b.to_period().get_inner());
			default -> throw new TypeNotPresentException(other.getClass().toString(), new Throwable("Operation not supported with this type"));
		}
		return returnValue;
	}


	/**
	 * Returns whether "this" is after "other" allowing overlap. That is, "this" starts after "other" starts
	 *         (or at the same time).
	 *<pre>
	 *    Examples:
	 *    >>> PeriodSet('{[2012-01-02, 2012-01-03]}').is_over_or_after(PeriodSet('{[2012-01-01, 2012-01-02)}'))
	 *    >>> True
	 *    >>> PeriodSet('{[2012-01-02, 2012-01-03]}').is_over_or_after(PeriodSet('{[2012-01-01, 2012-01-02]}'))
	 *    >>> True
	 *    >>> PeriodSet('{[2012-01-02, 2012-01-03]}').is_over_or_after(PeriodSet('{[2012-01-01, 2012-01-03]}'))
	 *    >>> False
	 *</pre>
	 *         MEOS Functions:
	 *         <ul>
	 *             <li>overright_spanset_span</li>
	 *             <li>overright_spanset_spanset</li>
	 *             <li>overafter_periodset_timestamp</li>
	 *             <li>overafter_periodset_timestampset</li>
	 *         </ul>
	 * @param other temporal object to compare with
	 * @return True if overlapping or after, False otherwise
	 */
	public boolean is_over_or_after(Time other) {
		boolean returnValue;
		switch (other) {
			case Period p -> returnValue = functions.overright_spanset_span(this._inner, p.get_inner());
			case PeriodSet ps -> returnValue = functions.overright_spanset_spanset(this._inner, ps.get_inner());
			case TimestampSet ts -> returnValue = functions.overright_spanset_span(this._inner, functions.set_span(ts.get_inner()));
			//case DateTime dt -> returnValue = functions.overleft_periodset_timestamp(this._inner, ConversionUtils.datetimeToTimestampTz(dt.get_inner()));
			//case Temporal t -> returnValue = functions.overleft_spanset_spanset(this._inner, t.period().get_inner());
			case Box b -> returnValue = functions.overright_spanset_span(this._inner, b.to_period().get_inner());
			default -> throw new TypeNotPresentException(other.getClass().toString(), new Throwable("Operation not supported with this type"));
		}
		return returnValue;
	}

	/** ------------------------- Distance Operations --------------------------- */
	//TODO: Distance operator


	/** ------------------------- Set Operations -------------------------------- */


	/**
	 * Returns the temporal intersection of "this" and "other".
	 *<p>
	 *         MEOS Functions:
	 *         <ul>
	 *         		<li>intersection_periodset_timestamp</li>
	 *         		<li>intersection_spanset_spanset</li>
	 *         		<li>intersection_spanset_span</li>
	 *         </ul>
	 * @param other temporal object to intersect with
	 * @return a Time instance. The actual class depends on "other"
	 */
	public Time intersection(Time other) {
		Time returnValue;
		switch (other) {
			case Period p -> returnValue = new Period(functions.intersection_spanset_span(this._inner,p.get_inner()));
			case PeriodSet ps -> returnValue = new Period(functions.intersection_spanset_spanset(this._inner,ps.get_inner()));
			case TimestampSet ts -> returnValue = new Period(functions.intersection_spanset_spanset(this._inner,functions.set_to_spanset(ts.get_inner())));
			//case DateTime dt -> returnValue = functions.overleft_periodset_timestamp(this._inner, ConversionUtils.datetimeToTimestampTz(dt.get_inner()));
			default -> throw new TypeNotPresentException(other.getClass().toString(), new Throwable("Operation not supported with this type"));
		}
		return returnValue;
	}

	/**
	 * Returns the temporal intersection of "this" and "other".
	 *<p>
	 *         MEOS Functions:
	 *         <ul>
	 *         		<li>intersection_periodset_timestamp</li>
	 *         		<li>intersection_spanset_spanset</li>
	 *         		<li>intersection_spanset_span</li>
	 *         </ul>
	 * @param other temporal object to intersect with
	 * @return a Time instance. The actual class depends on "other"
	 */
	public Time mul(Time other) {
		return this.intersection(other);
	}


	/**
	 * Returns the temporal difference of "this" and "other".
	 *<p>
	 *         MEOS Functions:
	 *         <ul>
	 *         		<li>minus_spanset_span</li>
	 *         		<li>minus_spanset_spanset</li>
	 *         		<li>minus_periodset_timestamp</li>
	 *         </ul>
	 *
	 * @param other temporal object to diff with
	 * @return a PeriodSet instance
	 */
	public PeriodSet minus(Time other) {
		PeriodSet returnValue;
		switch (other) {
			case Period p -> returnValue = new PeriodSet(functions.minus_spanset_span(this._inner,p.get_inner()));
			case PeriodSet ps -> returnValue = new PeriodSet(functions.minus_spanset_spanset(this._inner,ps.get_inner()));
			case TimestampSet ts -> returnValue = new PeriodSet(functions.minus_spanset_spanset(this._inner,functions.set_to_spanset(ts.get_inner())));
			//case DateTime dt -> returnValue = functions.overleft_periodset_timestamp(this._inner, ConversionUtils.datetimeToTimestampTz(dt.get_inner()));
			default -> throw new TypeNotPresentException(other.getClass().toString(), new Throwable("Operation not supported with this type"));
		}
		return returnValue;
	}


	/**
	 * Returns the temporal difference of "this" and "other".
	 *<p>
	 *         MEOS Functions:
	 *         <ul>
	 *         		<li>minus_spanset_span</li>
	 *         		<li>minus_spanset_spanset</li>
	 *         		<li>minus_periodset_timestamp</li>
	 *         </ul>
	 *
	 * @param other temporal object to diff with
	 * @return a PeriodSet instance
	 */
	public PeriodSet sub(Time other) {
		return this.minus(other);
	}


	/**
	 * Returns the temporal union of "this" and "other".
	 * <p>
	 *         MEOS Functions:
	 *         <ul>
	 *         		<li>union_periodset_timestamp</li>
	 *         		<li>union_spanset_spanset</li>
	 *         		<li>union_spanset_span</li>
	 *         </ul>
	 *
	 * @param other temporal object to merge with
	 * @return a PeriodSet instance
	 */
	public PeriodSet union(Time other) {
		PeriodSet returnValue;
		switch (other) {
			case Period p -> returnValue = new PeriodSet(functions.union_spanset_span(this._inner,p.get_inner()));
			case PeriodSet ps -> returnValue = new PeriodSet(functions.union_spanset_spanset(this._inner,ps.get_inner()));
			case TimestampSet ts -> returnValue = new PeriodSet(functions.union_spanset_spanset(this._inner,functions.set_to_spanset(ts.get_inner())));
			//case DateTime dt -> returnValue = functions.overleft_periodset_timestamp(this._inner, ConversionUtils.datetimeToTimestampTz(dt.get_inner()));
			default -> throw new TypeNotPresentException(other.getClass().toString(), new Throwable("Operation not supported with this type"));
		}
		return returnValue;
	}



	/**
	 * Returns the temporal union of "this" and "other".
	 * <p>
	 *         MEOS Functions:
	 *         <ul>
	 *         		<li>union_periodset_timestamp</li>
	 *         		<li>union_spanset_spanset</li>
	 *         		<li>union_spanset_span</li>
	 *         </ul>
	 *
	 * @param other temporal object to merge with
	 * @return a PeriodSet instance
	 */
	public PeriodSet add(Time other) {
		return this.union(other);
	}




	/** ------------------------- Comparisons ----------------------------------- */


	/**
	 *  Return whether "this" and "other" are equal.
	 *
	 * <p>
	 *         MEOS Functions:
	 *             <li>spanset_eq </li>
	 * @param other temporal object to compare with
	 * @return True if equal, False otherwise
	 */
	public boolean equals(Time other) {
		boolean result;
		result = other instanceof PeriodSet ? functions.spanset_eq(this._inner,((PeriodSet) other).get_inner()) : false;
		return result;
	}

	/**
	 * Return whether "this" and "other" are not equal.
	 *
	 *<p>
	 *         MEOS Functions:
	 *             <li>spanset_ne </li>
	 *
	 * @param other temporal object to compare with
	 * @return True if not equal, False otherwise
	 */
	public boolean notEquals(Time other) {
		boolean result;
		result = other instanceof PeriodSet ? functions.spanset_ne(this._inner,((PeriodSet) other).get_inner()) : true;
		return result;
	}


	/**
	 * Return whether "this" is less than "other".
	 *
	 *<p>
	 *         MEOS Functions:
	 *             <li>spanset_lt</li>
	 *
	 * @param other temporal object to compare with
	 * @return True if less than, False otherwise
	 */
	public boolean lessThan(Time other) throws OperationNotSupportedException {
		if (other instanceof PeriodSet){
			return functions.spanset_lt(this._inner,((PeriodSet) other).get_inner());
		}
		else{
			throw new OperationNotSupportedException("Operation not supported with this type.");
		}
	}


	/**
	 * Return whether "this" is less than or equal to "other.
	 *<p>
	 *         MEOS Functions:
	 *             <li>spanset_le</li>
	 * @param other temporal object to compare with
	 * @return True if less than or equal, False otherwise
	 */
	public boolean lessThanOrEqual(Time other) throws OperationNotSupportedException {
		if (other instanceof PeriodSet){
			return functions.spanset_le(this._inner,((PeriodSet) other).get_inner());
		}
		else{
			throw new OperationNotSupportedException("Operation not supported with this type.");
		}
	}


	/**
	 * Return whether "this" is greater than "other".
	 *<p>
	 *
	 *         MEOS Functions:
	 *             <li>spanset_gt</li>
	 * @param other temporal object to compare with
	 * @return True if greater than, False otherwise
	 */
	public boolean greaterThan(Time other) throws OperationNotSupportedException {
		if (other instanceof PeriodSet){
			return functions.spanset_gt(this._inner,((PeriodSet) other).get_inner());
		}
		else{
			throw new OperationNotSupportedException("Operation not supported with this type.");
		}
	}

	/**
	 * Return whether ``self`` is greater than or equal to ``other``.
	 * <p>
	 *         MEOS Functions:
	 *             <li>spanset_ge</li>
	 *
	 * @param other temporal object to compare with
	 * @return True if greater than or equal, False otherwise
	 */
	public boolean greaterThanOrEqual(Time other) throws OperationNotSupportedException {
		if (other instanceof PeriodSet){
			return functions.spanset_ge(this._inner,((PeriodSet) other).get_inner());
		}
		else{
			throw new OperationNotSupportedException("Operation not supported with this type.");
		}
	}




	
	/**
	 * Gets all the Periods
	 *
	 * @return an array of Periods
	 */
	public Period[] periods() {
		return periodList.toArray(new Period[0]);
	}

	
	/**
	 * Gets the number of periods
	 *
	 * @return a number
	 */
	public int numPeriods() {
		return periodList.size();
	}
	
	
	/**
	 * Gets the Period located at the index position
	 *
	 * @param n - the index
	 * @return a Period
	 */
	public Period periodN(int n) {
		return periodList.get(n);
	}

}
	


