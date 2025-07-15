package org.bndtools.templating.jgit.tools;

import java.util.stream.Collectors;

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
		Attrs attrs = new Attrs();
		if (name != null && !name.isEmpty())
			attrs.put("name", name);
		if (branch != null && !branch.isEmpty())
			attrs.put("branch", branch);
		Pair<String, Attrs> newEntry = workspaceTemplateURL != null
			? new Pair<String, Attrs>(workspaceTemplateURL, attrs)
			: null;
		if (newEntry != null) {
			params.add(newEntry.getFirst(), newEntry.getSecond());
			prefs.setGitRepos(params);
			return prefs.save();
		}
		return false;
	}

	@Override
	public String[] getExistingWorkspaceTemplateURLs() {
		return this.params.keySet()
			.stream()
			.map(k -> k)
			.collect(Collectors.toList())
			.toArray(new String[] {});
	}

}
