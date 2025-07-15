package org.bndtools.mcp.tools.workspacetemplate;

import org.eclipse.ecf.ai.mcp.tools.annotation.Tool;
import org.eclipse.ecf.ai.mcp.tools.annotation.ToolParam;

public interface WorkspaceTemplateURLAddingTools {

	@Tool(description = "add a github repo url that represents bndtools workspace template to the existing Eclipse bndtools configuration.  The bndtools user interface for performing the function by this tool in Eclipse is Window->Preferences->Bndtools->Workspace Template")
	boolean addWorkspaceTemplateURL(
		@ToolParam(name="workspaceTemplateURL", description = "The bndtools workspace template tool to add.  Must not be empty or null, and should be a github URL")
		String workspaceTemplateURL,
		@ToolParam(name="name", description = "A name for the workspace template URL. Maybe null or an empty string")
		String name,
		@ToolParam(name ="branch", description = "The github repo branch to use.  If null or empty is provided, the default branch will be selected")
		String branch);

	@Tool(description = "return a string array of the existing workspace template urls in the current bndtools/Eclipse configuration")
	String[] getExistingWorkspaceTemplateURLs();

}
