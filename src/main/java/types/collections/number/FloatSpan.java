package types.collections.number;
import functions.functions;
import jnr.ffi.Pointer;
import types.collections.base.Base;
import types.collections.base.Span;
import types.collections.base.SpanSet;

/**
 * Class for representing sets of contiguous float values between a lower and
 *     an upper bound. The bounds may be inclusive or not.
 * <p>
 *     ``FloatSpan`` objects can be created with a single argument of type string
 *     as in MobilityDB.
 * <p>
 *         >>> FloatSpan('(2.5, 5.21]')
 * <p>
 *     Another possibility is to provide the ``lower`` and ``upper`` named
 *     parameters (of type str or float), and optionally indicate whether the
 *     bounds are inclusive or exclusive (by default, the lower bound is inclusive
 *     and the upper is exclusive):
 * <p>
 *         >>> FloatSpan(lower=2.0, upper=5.8)
 *         >>> FloatSpan(lower=2.0, upper=5.8, lower_inc=False, upper_inc=True)
 *         >>> FloatSpan(lower='2.0', upper='5.8', upper_inc=True)
 */
public class FloatSpan extends Span<Float> implements Number{
    private Pointer _inner;

    public FloatSpan(Pointer inner){
        this._inner = inner;
    }

    public FloatSpan(String str){
        this._inner = functions.floatspan_in(str);
    }

    /** ------------------------- Constructors ---------------------------------- */


    /** ------------------------- Output ---------------------------------------- */

    /**
     * Return the string representation of the content of "this".
     *
     *  <p>
     *         MEOS Functions:
     *             <li>floatspan_out</li>
     *
     *
     * @param max_decimals number of maximum decimals
     * @return A new {@link String} instance
     */
    public String toString(int max_decimals){
        return functions.floatspan_out(this._inner, max_decimals);
    }



    /** ------------------------- Conversions ----------------------------------- */


    /**
     * Returns a SpanSet that contains a Span for each element in "this".
     *
     *  <p>
     *
     *         MEOS Functions:
     *             <li>set_to_spanset</li>
     *
     * @return A new {@link FloatSpanSet} instance
     */
    public FloatSpanSet to_spanset(){
        return new FloatSpanSet(super.to_spanset().get_inner());
    }


    /** ------------------------- Accessors ------------------------------------- */


    /**
     * Returns the lower bound of "this".
     *
     *  <p>
     *
     *         MEOS Functions:
     *             <li>period_lower</li>
     *
     * @return The lower bound of the span as a {@link float}
     */
    public Float lower(){
        return (float) functions.floatspan_lower(this._inner);
    }



    /**
     * Returns the upper bound of "this".
     *
     *  <p>
     *
     *         MEOS Functions:
     *             <li>period_upper</li>
     *
     * @return The lower bound of the span as a {@link float}
     */
    public Float upper(){
        return (float) functions.floatspan_upper(this._inner);
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



    /** ------------------------- Transformations ------------------------------- */

    //TODO: make the shift tscale functions


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
     *             <li>adjacent_floatspan_float</li>
     *
     * @param other object to compare with
     * @return True if adjacent, False otherwise
     * @throws Exception
     */
    public boolean is_adjacent(Object other) throws Exception {
        if ((other instanceof Integer) || (other instanceof Float)){
            return functions.adjacent_floatspan_float(this._inner, (float) other);
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
     *             <li>contains_floatspan_float</li>
     *
     * @param other object to compare with
     * @return True if contains, False otherwise
     * @throws Exception
     */
    public boolean contains(Object other) throws Exception {
        if ((other instanceof Integer) || (other instanceof Float)){
            return functions.contains_floatspan_float(this._inner, (float) other);
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
        if ((other instanceof Integer) || (other instanceof Float)){
            return functions.span_eq(this._inner, functions.float_to_floatspan((float)other));
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
     *             left_floatspan_float
     *
     * @param other object to compare with
     * @return True if before, False otherwise
     * @throws Exception
     */
    public boolean is_left(Object other) throws Exception {
        if ((other instanceof Integer) || (other instanceof Float)){
            return functions.left_floatspan_float(this._inner, (float) other);
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
     *             <li>overleft_floatspan_float</li>
     *
     * @param other object to compare with
     * @return True if before, False otherwise
     * @throws Exception
     */
    public boolean is_over_or_left(Object other) throws Exception {
        if ((other instanceof Integer) || (other instanceof Float)){
            return functions.overleft_floatspan_float(this._inner, (float) other);
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
     *             <li>right_floatspan_float</li>
     *
     * @param other object to compare with
     * @return True if after, False otherwise
     * @throws Exception
     */
    public boolean is_right(Object other) throws Exception {
        if ((other instanceof Integer) || (other instanceof Float)){
            return functions.right_floatspan_float(this._inner, (float) other);
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
     *             <li>overright_floatspan_float</li>
     *
     * @param other object to compare with
     * @return True if overlapping or after, False otherwise
     * @throws Exception
     */
    public boolean is_over_or_right(Object other) throws Exception {
        if ((other instanceof Integer) || (other instanceof Float)){
            return functions.overright_floatspan_float(this._inner, (float) other);
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
     *             <li>distance_floatspan_float</li>
     *
     * @param other object to compare with
     * @return A float value
     * @throws Exception
     */
    public Float distance(Object other) throws Exception {
        if ((other instanceof Integer) || (other instanceof Float)){
            return (float) functions.distance_floatspan_float(this._inner, (float) other);
        }
        else {
            return super.distance((Base) other);
        }
    }



    /** ------------------------- Set Operations -------------------------------- */
    //TODO: intersection

    public Float intersection(Object other) throws Exception {
        if ((other instanceof Integer) || (other instanceof Float)){
            return (float) functions.distance_floatspan_float(this._inner, (float) other);
        }
        else {
            return super.distance((Base) other);
        }
    }




    /**
     * Returns the difference of "this" and "other".
     *
     *  <p>
     *         MEOS Functions:
     *             <li>minus_span_span</li>
     *             <li>minus_spanset_span</li>
     *             <li>minus_floatspan_float</li>
     *
     * @param other object to diff with
     * @return A {@link FloatSpanSet} instance.
     */
    public FloatSpanSet minus(Object other){
        Pointer result = null;
        if ((other instanceof Integer) || (other instanceof Float)){
            //result = functions.minus_floatspan_float(this._inner,other);
        }
        else if (other instanceof FloatSpan) {
            result = functions.minus_span_span(this._inner,((FloatSpan) other).get_inner());
        }
        else if (other instanceof FloatSpanSet) {
            result = functions.minus_spanset_span(((FloatSpanSet) other).get_inner(), this._inner);
        }
        else {
            //result = super.minus(other);
        }
        return new FloatSpanSet(result);
    }


    /**
     * Returns the union of "this" and "other".
     *
     *  <p>
     *
     *         MEOS Functions:
     *             <li>union_spanset_span</li>
     *             <li>union_span_span</li>
     *             <li>union_floatspan_float</li>
     *
     * @param other object to merge with
     * @return A {@link FloatSpanSet} instance.
     */
    public FloatSpanSet union(Object other){
        Pointer result = null;
        if (other instanceof Integer){
            //result = functions.union_floatspan_float(this._inner,other);
        }
        else if (other instanceof FloatSpan) {
            result = functions.union_span_span(this._inner,((FloatSpan) other).get_inner());
        }
        else if (other instanceof FloatSpanSet) {
            result = functions.union_spanset_span(((FloatSpanSet) other).get_inner(), this._inner);
        }
        else {
            //result = super.union(other);
        }
        return new FloatSpanSet(result);
    }





}
