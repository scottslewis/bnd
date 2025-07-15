package org.bndtools.templating.jgit.tools;

import java.util.Iterator;

import org.bndtools.mcp.tools.workspacetemplate.WorkspaceTemplateURLAddingTools;
import org.bndtools.templating.jgit.GitRepoPreferences;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import aQute.bnd.header.Attrs;
import aQute.bnd.header.Parameters;
import aQute.libg.tuple.Pair;

@Component(immediate = true, property = {
	"service.exported.interfaces=*"
})
public class WorkspaceTemplateURLAddingToolsImpl implements WorkspaceTemplateURLAddingTools {

	private GitRepoPreferences	prefs;
	private Parameters			params;

	@Activate
	void activate() {
		this.prefs = new GitRepoPreferences();
		this.params = this.prefs.getGitRepos();
	}

	@Override
	public boolean addWorkspaceTemplateURL(String workspaceTemplateURL, String name, String branch) {
		// Check for null
		if (workspaceTemplateURL == null || workspaceTemplateURL.isBlank()) {
			return false;
		}
		// Check if it's already there (case insensitive_
		for (String k : this.params.keySet()) {
			if (k.equalsIgnoreCase(workspaceTemplateURL)) {
				return false;
			}
		}
		Attrs attrs = new Attrs();
		if (name != null && !name.isEmpty())
			attrs.put("name", name);
		if (branch != null && !branch.isEmpty())
			attrs.put("branch", branch);

		Pair<String, Attrs> newEntry = new Pair<String, Attrs>(workspaceTemplateURL, attrs);
		params.add(newEntry.getFirst(), newEntry.getSecond());
		prefs.setGitRepos(params);
		return prefs.save();
	}

	@Override
	public String getExistingWorkspaceTemplateURLs() {
		// Generating json with jackson would be much better
		StringBuffer json = new StringBuffer("{ \"uris\": [");
		for (Iterator<String> i = this.params.keySet()
			.iterator(); i.hasNext();) {
			String item = i.next();
			json.append("\"" + item + "\"");
			if (i.hasNext()) {
				json.append(",");
			}
		}
		json.append("] }");
		return json.toString();
	}

}
