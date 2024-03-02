package org.freenetproject.freemail.ui.web;

import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Function;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.freenetproject.freemail.l10n.FreemailL10n;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pebble extension that adds an {@code l10n} function to pebble templates
 * which can be used to access {@link FreemailL10n Freemail’s translation}.
 * <h2>Usage in Template</h2>
 * <pre>
 *     &lt;div>
 *         This text is translated: {{ l10n('Text.translated') }}
 *     &lt;/div>
 * </pre>
 * <p>
 * This will retrieve the translation from
 * {@link FreemailL10n#getString(String)} using {@code Text.translated}
 * as the key.
 * </p>
 */
public class L10nExtension extends AbstractExtension {

	@Override
	public Map<String, Function> getFunctions() {
		Map<String, Function> functions = new HashMap<>();
		functions.put("l10n", new L10nFunction());
		return functions;
	}

	private static class L10nFunction implements Function {

		@Override
		public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
			Object key = args.get("0");
			if (key == null) {
				return "null";
			}
			return FreemailL10n.getString(key.toString());
		}

		@Override
		public List<String> getArgumentNames() {
			return null;
		}

	}

}
