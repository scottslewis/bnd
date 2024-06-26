package bndtools.editor.workspace;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bndtools.core.ui.icons.Icons;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.ide.ResourceUtil;

import aQute.bnd.build.model.BndEditModel;
import aQute.bnd.build.model.BndEditModelHeaderClause;
import aQute.bnd.header.Attrs;
import aQute.bnd.osgi.Constants;
import bndtools.Plugin;

public class PluginsPart extends SectionPart implements PropertyChangeListener {

	private final Image									editImg			= Icons.image("/icons/pencil.png");
	private final Image									refreshImg		= Icons.image("icons/arrow_refresh.png");

	private final Map<String, IConfigurationElement>	configElements	= new HashMap<>();

	private Map<String, List<BndEditModelHeaderClause>>		data;
	private Set<String>									pluginsPropertiesToRemove	= new LinkedHashSet<>();

	private Table										table;
	private TableViewer									viewer;

	private ToolItem									editItemTool;
	private ToolItem									removeItemTool;

	private BndEditModel								model;


	public PluginsPart(Composite parent, FormToolkit toolkit, int style) {
		super(parent, toolkit, style);

		IConfigurationElement[] configElems = Platform.getExtensionRegistry()
			.getConfigurationElementsFor(Plugin.PLUGIN_ID, "bndPlugins");
		for (IConfigurationElement configElem : configElems) {
			String className = configElem.getAttribute("class");
			configElements.put(className, configElem);
		}

		createSection(getSection(), toolkit);
	}

	final void createSection(Section section, FormToolkit toolkit) {
		section.setText("Plugins");
		section.setDescription("Bnd plugins are used to specify repositories and extended behaviours.");

		createToolBar(section);

		Composite composite = toolkit.createComposite(section, SWT.NONE);
		table = toolkit.createTable(composite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER);

		viewer = new TableViewer(table);
		ColumnViewerToolTipSupport.enableFor(viewer);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new PluginClauseLabelProvider(configElements));
		viewer.addDoubleClickListener(e -> {
			doEdit();
		});

		Button btnReload = toolkit.createButton(composite, "Reload", SWT.NONE);
		btnReload.setImage(refreshImg);

		// Listeners
		viewer.addSelectionChangedListener(event -> {
			boolean enable = !viewer.getSelection()
				.isEmpty();
			removeItemTool.setEnabled(enable);
			editItemTool.setEnabled(enable);
		});
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.character == SWT.DEL) {
					doRemove();
				} else if (e.character == '+') {
					doAdd();
				}
			}
		});
		btnReload.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doReload();
			}
		});

		composite.setLayout(new GridLayout(1, false));
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		btnReload.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		section.setClient(composite);

	}

	void doReload() {
		IFormPage page = (IFormPage) getManagedForm().getContainer();
		final IFile file = ResourceUtil.getFile(page.getEditorInput());
		if (file != null && file.exists()) {
			WorkspaceJob job = new WorkspaceJob("Reload Plugins") {
				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
					file.touch(monitor);
					return Status.OK_STATUS;
				}
			};
			job.setUser(true);
			job.schedule();
		}
	}

	void createToolBar(Section section) {
		ToolBar toolbar = new ToolBar(section, SWT.FLAT);
		section.setTextClient(toolbar);

		ToolItem addItem = new ToolItem(toolbar, SWT.PUSH);
		addItem.setImage(PlatformUI.getWorkbench()
			.getSharedImages()
			.getImage(ISharedImages.IMG_OBJ_ADD));
		addItem.setToolTipText("Add Plugin");
		addItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doAdd();
			}
		});

		editItemTool = new ToolItem(toolbar, SWT.PUSH);
		editItemTool.setImage(editImg);
		editItemTool.setToolTipText("Edit");
		editItemTool.setEnabled(false);
		editItemTool.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doEdit();
			}
		});

		removeItemTool = new ToolItem(toolbar, SWT.PUSH);
		removeItemTool.setImage(PlatformUI.getWorkbench()
			.getSharedImages()
			.getImage(ISharedImages.IMG_TOOL_DELETE));
		removeItemTool.setDisabledImage(PlatformUI.getWorkbench()
			.getSharedImages()
			.getImage(ISharedImages.IMG_TOOL_DELETE_DISABLED));
		removeItemTool.setToolTipText("Remove");
		removeItemTool.setEnabled(false);
		removeItemTool.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doRemove();
			}
		});
	}

	@Override
	public void initialize(IManagedForm form) {
		super.initialize(form);

		model = (BndEditModel) form.getInput();
		model.addPropertyChangeListener(Constants.PLUGIN, this);
	}

	@Override
	public void dispose() {
		super.dispose();
		if (model != null)
			model.removePropertyChangeListener(Constants.PLUGIN, this);
	}

	@Override
	public void refresh() {
		Map<String, List<BndEditModelHeaderClause>> modelData = model.getPluginsProperties();
		if (modelData != null)
			this.data = new LinkedHashMap<>(modelData);
		else
			this.data = new LinkedHashMap<>();
		viewer.setInput(this.data.values()
			.stream()
			.flatMap(List::stream)
			.toList());
		super.refresh();
	}

	@Override
	public void commit(boolean onSave) {
		super.commit(onSave);
		model.setPlugins(data, pluginsPropertiesToRemove);
		pluginsPropertiesToRemove.clear();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		IFormPage page = (IFormPage) getManagedForm().getContainer();
		if (page.isActive()) {
			refresh();
		} else {
			markStale();
		}
	}

	void doAdd() {

		String uniqueKey = uniqueKey(Constants.PLUGIN);

		PluginSelectionWizard wizard = new PluginSelectionWizard(() -> uniqueKey);
		WizardDialog dialog = new WizardDialog(getManagedForm().getForm()
			.getShell(), wizard);
		if (dialog.open() == Window.OK) {
			BndEditModelHeaderClause newPlugin = wizard.getHeader();

			data.put(uniqueKey, Collections.singletonList(newPlugin));
			viewer.add(newPlugin);
			markDirty();
		}
	}

	private String uniqueKey(String key) {
		String newKey = key;
		int i = 1;
		while (data.containsKey(newKey)) {
			newKey = key + "." + i;
			i++;
		}
		return newKey;
	}

	void doEdit() {
		BndEditModelHeaderClause header = (BndEditModelHeaderClause) ((IStructuredSelection) viewer.getSelection())
			.getFirstElement();

		if (!header.isLocal()) {
			// only local plugins in this file can be edited
			return;
		}

		if (header != null) {
			Attrs copyOfProperties = new Attrs(header.getAttribs());

			IConfigurationElement configElem = configElements.get(header.getName());
			PluginEditWizard wizard = new PluginEditWizard(configElem, copyOfProperties);
			WizardDialog dialog = new WizardDialog(getManagedForm().getForm()
				.getShell(), wizard);

			if (dialog.open() == Window.OK && wizard.isChanged()) {
				header.getAttribs()
					.clear();
				header.getAttribs()
					.putAll(copyOfProperties);

				viewer.update(new BndEditModelHeaderClause(header.key(), header, header.isLocal()), null);
				markDirty();
			}
		}
	}

	void doRemove() {

		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();

		BndEditModelHeaderClause header = (BndEditModelHeaderClause) sel.getFirstElement();

		if (!header.isLocal()) {
			// only local plugins in this file can be removed
			return;
		}

		viewer.remove(sel.toArray());

		// remove by value
		List<?> list = sel.toList();
		list
			.forEach(selectedPlugin -> {
				Set<Entry<String, List<BndEditModelHeaderClause>>> entrySet = data.entrySet();
				inner: for (Iterator<Entry<String, List<BndEditModelHeaderClause>>> iterator = entrySet.iterator(); iterator
					.hasNext();) {
					Entry<String, List<BndEditModelHeaderClause>> entry = iterator.next();
					String key = entry.getKey();
					List<BndEditModelHeaderClause> headers = entry.getValue();

					boolean removed = headers.removeIf(selectedPlugin::equals);
					if (removed) {

						if (headers.isEmpty()) {
							// remove the map entry too
							iterator.remove();
							this.pluginsPropertiesToRemove.add(key);
						}

						// stop when we have removed the plugin
						break inner;
					}
				}
			});

		if (!sel.isEmpty())
			markDirty();
	}

	public ISelectionProvider getSelectionProvider() {
		return viewer;
	}

}
