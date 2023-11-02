package types.collections.number;
import types.collections.base.Base;
import types.collections.base.Span;
import functions.functions;
import jnr.ffi.Pointer;

/**
 * Class for representing sets of contiguous integer values between a lower and
 *     an upper bound. The bounds may be inclusive or not.
 * <p>
 *     ``IntSpan`` objects can be created with a single argument of type string
 *     as in MobilityDB.
 * <p>
 *         >>> IntSpan('(2, 5]')
 * <p>
 *     Another possibility is to provide the ``lower`` and ``upper`` named parameters (of type str or int), and
 *     optionally indicate whether the bounds are inclusive or exclusive (by default, the lower bound is inclusive and the
 *     upper is exclusive):
 * <p>
 *         >>> IntSpan(lower=2, upper=5)
 *         >>> IntSpan(lower=2, upper=5, lower_inc=False, upper_inc=True)
 *         >>> IntSpan(lower='2', upper='5', upper_inc=True)
 */
public class IntSpan extends Span<Integer> implements Number{
    private Pointer _inner;

    public IntSpan(Pointer inner){
        this._inner = inner;
    }

    public IntSpan(String str){
        this._inner = functions.intspan_in(str);
    }

    /** ------------------------- Output ---------------------------------------- */

    /**
     * Return the string representation of the content of "this".
     *
     *  <p>
     *         MEOS Functions:
     *             <li>intspan_out</li>
     *
     *
     * @return A new {@link String} instance
     */
    public String toString(){
        return functions.intspan_out(this._inner);
    }



    /** ------------------------- Conversions ----------------------------------- */


    /**
     * Returns a SpanSet that contains a Span for each element in "this".
     *
     *  <p>
     *
     *         MEOS Functions:
     *             <li>span_to_spanset</li>
     *
     * @return A new {@link IntSpanSet} instance
     */
    public IntSpanSet to_spanset(){
        return new IntSpanSet(super.to_spanset().get_inner());
    }


    /**
     * Converts "this" to a {@link FloatSpan} instance.
     *
     * <p>
     *
     *         MEOS Functions:
     *             <li>intspan_floatspan</li>
     *
     * @return A new :class:`FloatSpan` instance
     */
    /*
    public FloatSpan tofloatspan(){
        return new FloatSpan(functions.intspan_floatspan(this._inner));
    }
    */



    /** ------------------------- Accessors ------------------------------------- */


    /**
     * Returns the lower bound of "this".
     *
     *  <p>
     *
     *         MEOS Functions:
     *             <li>period_lower</li>
     *
     * @return The lower bound of the span as a {@link Integer}
     */
    public Integer lower(){
        return functions.intspan_lower(this._inner);
    }



    /**
     * Returns the upper bound of "this".
     *
     *  <p>
     *
     *         MEOS Functions:
     *             <li>period_upper</li>
     *
     * @return The lower bound of the span as a {@link Integer}
     */
    public Integer upper(){
        return functions.intspan_upper(this._inner);
    }


    /**
     * Returns the width of "this".
     *
     *  <p>
     *
     *         MEOS Functions:
     *             <li>span_width</li>
     *
     * @return Returns a "float" representing the width of the span
     */
    public float width(){
        return (float) functions.span_width(this._inner);
    }



    /** ------------------------- Topological Operations -------------------------------- */

    /**
     * Returns whether "this" is adjacent to "other". That is, they share
     *         a bound but only one of them contains it.
     *
     *  <p>
     *
     *         MEOS Functions:
     *             <li>adjacent_span_span</li>
     *             <li>adjacent_span_spanset</li>
     *             <li>adjacent_intspan_int</li>
     *
     * @param other object to compare with
     * @return True if adjacent, False otherwise
     * @throws Exception
     */
    public boolean is_adjacent(Object other) throws Exception {
        if (other instanceof Integer){
            return functions.adjacent_intspan_int(this._inner, (int) other);
        }
        else {
            return super.is_adjacent((Base) other);
        }
    }


    /**
     * Returns whether "this" contains "content".
     *
     * <p>
     *
     *         MEOS Functions:
     *             <li>contains_set_set</li>
     *             <li>contains_intspan_int</li>
     *
     * @param other object to compare with
     * @return True if contains, False otherwise
     * @throws Exception
     */
    public boolean contains(Object other) throws Exception {
        if (other instanceof Integer){
            return functions.contains_intspan_int(this._inner, (int) other);
        }
        else {
            return super.contains((Base) other);
        }
    }


    /**
     * Returns whether "this" and the bounding period of "other is the
     *         same.
     *
     *  <p>
     *
     *         MEOS Functions:
     *             <li>same_period_temporal</li>
     *
     * @param other object to compare with
     * @return True if equal, False otherwise
     * @throws Exception
     */
    public boolean is_same(Object other) throws Exception {
        if (other instanceof Integer){
            return functions.span_eq(this._inner, functions.int_to_intspan((int)other));
        }
        else {
            return super.is_same((Base) other);
        }
    }



    /** ------------------------- Position Operations --------------------------- */


    /**
     * Returns whether "this" is strictly before "other". That is,
     *         "this" ends before "other" starts.
     *
     *  <p>
     *
     *         MEOS Functions:
     *             left_span_span
     *             left_span_spanset
     *             left_intspan_int
     *
     * @param other object to compare with
     * @return True if before, False otherwise
     * @throws Exception
     */
    public boolean is_left(Object other) throws Exception {
        if (other instanceof Integer){
            return functions.left_intspan_int(this._inner, (int) other);
        }
        else {
            return super.is_left((Base) other);
        }
    }


    /**
     * Returns whether "this" is before "other" allowing overlap. That is,
     *         "this ends before "other" ends (or at the same value).
     *
     *  <p>
     *
     *         MEOS Functions:
     *             <li>overleft_span_span</li>
     *             <li>overleft_span_spanset</li>
     *             <li>overleft_intspan_int</li>
     *
     * @param other object to compare with
     * @return True if before, False otherwise
     * @throws Exception
     */
    public boolean is_over_or_left(Object other) throws Exception {
        if (other instanceof Integer){
            return functions.overleft_intspan_int(this._inner, (int) other);
        }
        else {
            return super.is_over_or_left((Base) other);
        }
    }

    /**
     * Returns whether "this" is strictly after "other". That is, "this"
     *         starts after "other" ends.
     *
     *   <p>
     *
     *         MEOS Functions:
     *             <li>right_span_span</li>
     *             <li>right_span_spanset</li>
     *             <li>right_intspan_int</li>
     *
     * @param other object to compare with
     * @return True if after, False otherwise
     * @throws Exception
     */
    public boolean is_right(Object other) throws Exception {
        if (other instanceof Integer){
            return functions.right_intspan_int(this._inner, (int) other);
        }
        else {
            return super.is_right((Base) other);
        }
    }


    /**
     * Returns whether "this" is after "other" allowing overlap. That is,
     *         "this" starts after "other" starts (or at the same value).
     *
     *  <p>
     *
     *         MEOS Functions:
     *             <li>overright_span_span</li>
     *             <li>overright_span_spanset</li>
     *             <li>overright_intspan_int</li>
     *
     * @param other object to compare with
     * @return True if overlapping or after, False otherwise
     * @throws Exception
     */
    public boolean is_over_or_right(Object other) throws Exception {
        if (other instanceof Integer){
            return functions.overright_intspan_int(this._inner, (int) other);
        }
        else {
            return super.is_over_or_right((Base) other);
        }
    }



    /** ------------------------- Distance Operations --------------------------- */


    /**
     * Returns the distance between "this" and "other".
     *
     *  <p>
     *
     *         MEOS Functions:
     *             <li>distance_span_span</li>
     *             <li>distance_span_spanset</li>
     *             <li>distance_intspan_int</li>
     *
     * @param other object to compare with
     * @return A float value
     * @throws Exception
     */
    public Float distance(Object other) throws Exception {
        if (other instanceof Integer){
            return (float) functions.distance_intspan_int(this._inner, (int) other);
        }
        else {
            return super.distance((Base) other);
        }
    }


    /** ------------------------- Set Operations -------------------------------- */


    /**
     * Returns the difference of "this" and "other".
     *
     *  <p>
     *         MEOS Functions:
     *             <li>minus_span_span</li>
     *             <li>minus_spanset_span</li>
     *             <li>minus_intspan_int</li>
     *
     * @param other object to diff with
     * @return A {@link IntSpanSet} instance.
     */
    public IntSpanSet minus(Object other){
        Pointer result = null;
        if (other instanceof Integer){
            //result = functions.minus_intspan_int(this._inner,other);
        }
        else if (other instanceof IntSpan) {
            result = functions.minus_span_span(this._inner,((IntSpan) other).get_inner());
        }
        else if (other instanceof IntSpanSet) {
            result = functions.minus_spanset_span(((IntSpanSet) other).get_inner(), this._inner);
        }
        else {
            //result = super.minus(other);
        }
        return new IntSpanSet(result);
    }


    /**
     * Returns the union of "this" and "other".
     *
     *  <p>
     *
     *         MEOS Functions:
     *             <li>union_spanset_span</li>
     *             <li>union_span_span</li>
     *             <li>union_intspan_int</li>
     *
     * @param other object to merge with
     * @return A {@link IntSpanSet} instance.
     */
    public IntSpanSet union(Object other){
        Pointer result = null;
        if (other instanceof Integer){
            //result = functions.union_intspan_int(this._inner,other);
        }
        else if (other instanceof IntSpan) {
            result = functions.union_span_span(this._inner,((IntSpan) other).get_inner());
        }
        else if (other instanceof IntSpanSet) {
            result = functions.union_spanset_span(((IntSpanSet) other).get_inner(), this._inner);
        }
        else {
            //result = super.union(other);
        }
        return new IntSpanSet(result);
    }









}