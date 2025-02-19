package pl.opole.uni.cs.unifDL.Filo.renderer;

/**
 * This interface contains the keywords for rendering DL objects (value restrictions, existential
 * restrictions, conjunctions, ...) in various string representations.
 * 
 * Modified from uel RendererKeywords
 * 
 * @author Barbara Morawska
 */
public interface RendererKeywords {

	/**
	 * the string 'and'
	 */
	String and = "and";

	/**
	 * the string ')'
	 */
	String close = ")";

	/**
	 * a string representing a line break
	 */
	String newLine = System.getProperty("line.separator");

	/**
	 * the string '('
	 */
	String open = "(";

	/**
	 * the string 'some'
	 */
	String some = "some";

	/**
	 * the string 'some'
	 */
	String only = "only";
	
	/**
	 * the string ' '
	 */
	String space = " ";

	/**
	 * the string 'top'
	 */
	String krssTop = "top";

	/**
	 * the string 'owl:Thing'
	 */
	String owlThing = "owl:Thing";

	/**
	 * the string 'ObjectSomeValuesFrom'
	 */
	String objectSomeValuesFrom = "ObjectSomeValuesFrom";

	/**
	 * the string 'ObjectAllValuesFrom'
	 */
	String objectAllValuesFrom = "ObjectAllValuesFrom";
	
	/**
	 * the string 'ObjectIntersectionOf'
	 */
	String objectIntersectionOf = "ObjectIntersectionOf";
}
