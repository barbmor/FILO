/**
 * 
 */
package pl.opole.uni.cs.unifDL.Filo.renderer;

import java.util.HashMap;
import java.util.Map;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;

/**
 * This class is modified from UEL class ShortFormProvider
 * 
 * @author Michał Henne
 * @author Sławomir Kost
 * @author Dariusz Marzec
 * @author Barbara Morawska
 *
 */
public class ShortFormProvider extends SimpleIRIShortFormProvider {

	private static final long serialVersionUID = 1L;

	private OWLOntologyManager manager;

	public ShortFormProvider(OWLOntologyManager manager) {
		this.manager = manager;
	}

	/**
	 * Removes single or double quotes around the given string.
	 * 
	 * @param str the input string, possibly with quotes
	 * @return the input string, with quotes removed
	 */
	private static String removeQuotes(String str) {
		String ret = str;
		if ((str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'"))) {
			ret = str.substring(1, str.length() - 1);
		}
		return ret;
	}

	private final Map<String, String> cache = new HashMap<>();

	private String extractShortForm(String id) {
		IRI iri = IRI.create(id);
		String shortForm = super.getShortForm(iri);
		if (shortForm == null) {
			shortForm = super.getShortForm(iri);
		}
		if (shortForm == null) {
			return iri.getShortForm();
		} else {
			return removeQuotes(shortForm);
		}
	}

	public String getShortForm(String id) {
		String shortForm = cache.get(id);
		if (shortForm == null) {
			shortForm = extractShortForm(id);
			cache.put(id, shortForm);
		} else {
		}
		return shortForm;
	}

	public void resetCache() {
		cache.clear();
	}

}
