package com.ifedorenko.m2e.mavendev.launch.ui.internal.views;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import com.ifedorenko.m2e.mavendev.launch.ui.internal.BuildProgressActivator;
import com.ifedorenko.m2e.mavendev.launch.ui.internal.BuildProgressImages;
import com.ifedorenko.m2e.mavendev.launch.ui.internal.model.BuildStatus;
import com.ifedorenko.m2e.mavendev.launch.ui.internal.model.IBuildProgressListener;
import com.ifedorenko.m2e.mavendev.launch.ui.internal.model.Launch;
import com.ifedorenko.m2e.mavendev.launch.ui.internal.model.MojoExecution;
import com.ifedorenko.m2e.mavendev.launch.ui.internal.model.Project;
import com.ifedorenko.m2e.mavendev.launch.ui.internal.model.Status;

public class BuildProgressView extends ViewPart {

  public static final String ID = "com.ifedorenko.m2e.mavendev.launch.ui.views.SampleView";

  private static final String TAG_FAILURES_ONLY = "failuresOnly";

  private static final BuildProgressActivator CORE = BuildProgressActivator.getInstance();

  private TreeViewer viewer;

  private final List<Object> refreshQueue = new ArrayList<>();

  private UIJob refreshJob;

  private GreenRedProgressBar progressBar;

  private final IBuildProgressListener buildListener = new IBuildProgressListener() {
    @Override
    public void onUpdate(Object source) {
      synchronized (refreshQueue) {
        refreshQueue.add(source);
      }
      refreshJob.schedule(300L);
    }
  };

  private final ViewerFilter failureFilter = new ViewerFilter() {
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
      if (!actionFailuresOnly.isChecked()) {
        return true;
      }
      if (element instanceof Project) {
        Status status = ((Project) element).getStatus();
        return status == Status.failed || status == Status.inprogress;
      }
      if (element instanceof MojoExecution) {
        Status status = ((MojoExecution) element).getStatus();
        return status == Status.failed || status == Status.inprogress;
      }
      return true;
    }
  };

  private final Action actionFailuresOnly = new Action("Show failures only", IAction.AS_CHECK_BOX) {
    {
      setImageDescriptor(BuildProgressImages.FAILURE.getDescriptor());
    }

    @Override
    public void run() {
      applyFailuresOnlyFilter();
    }
  };

  public BuildProgressView() {
    CORE.addListener(buildListener);
  }

  public void createPartControl(Composite parent) {
    parent.setLayout(new GridLayout(1, false));

    progressBar = new GreenRedProgressBar(parent);
    progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
    Tree tree = viewer.getTree();
    tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

    final Menu menu = new Menu(tree);
    tree.setMenu(menu);

    final MenuItem mntmOpenBuildLog = new MenuItem(menu, SWT.NONE);
    mntmOpenBuildLog.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        openLogViewer();
      }
    });
    mntmOpenBuildLog.setText("Open build log in editor");

    viewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        mntmOpenBuildLog.setEnabled(getSelectedElement() instanceof Project);
      }
    });

    viewer.setContentProvider(new ITreeContentProvider() {

      @Override
      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

      @Override
      public void dispose() {}

      @Override
      public boolean hasChildren(Object element) {
        return getChildren(element) != null;
      }

      @Override
      public Object getParent(Object element) {
        return null;
      }

      @Override
      public Object[] getElements(Object inputElement) {
        if (inputElement instanceof Launch) {
          return ((Launch) inputElement).getProjects().toArray();
        }
        return null;
      }

      @Override
      public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof Project) {
          return ((Project) parentElement).getExecutions().toArray();
        }
        return null;
      }
    });
    viewer.setLabelProvider(new ILabelProvider() {

      @Override
      public void removeListener(ILabelProviderListener listener) {}

      @Override
      public boolean isLabelProperty(Object element, String property) {
        return false;
      }

      @Override
      public void dispose() {}

      @Override
      public void addListener(ILabelProviderListener listener) {}

      @Override
      public String getText(Object element) {
        if (element instanceof Project) {
          return ((Project) element).getId();
        }
        if (element instanceof MojoExecution) {
          return ((MojoExecution) element).getId();
        }
        return null;
      }

      @Override
      public Image getImage(Object element) {
        if (element instanceof Project) {
          return getStatusImage(((Project) element).getStatus());
        }
        if (element instanceof MojoExecution) {
          return getStatusImage(((MojoExecution) element).getStatus());
        }
        return null;
      }

      protected Image getStatusImage(Status status) {
        switch (status) {
          case inprogress:
            return BuildProgressImages.PROJECT_INPROGRESS.get();
          case succeeded:
            return BuildProgressImages.PROJECT_SUCCESS.get();
          case failed:
            return BuildProgressImages.PROJECT_FAILURE.get();
          case skipped:
            return BuildProgressImages.PROJECT_SKIPPED.get();
          default:
            return BuildProgressImages.PROJECT.get();
        }
      }
    });
    applyFailuresOnlyFilter();

    IActionBars actionBars = getViewSite().getActionBars();
    IToolBarManager toolBar = actionBars.getToolBarManager();
    IMenuManager viewMenu = actionBars.getMenuManager();

    toolBar.add(actionFailuresOnly);

    actionBars.updateActionBars();
  }

  public void setFocus() {
    viewer.getControl().setFocus();
  }

  @Override
  public void dispose() {
    CORE.removeListener(buildListener);
    refreshJob.cancel();

    super.dispose();
  }

  @Override
  protected void setSite(IWorkbenchPartSite site) {
    super.setSite(site);
    refreshJob = new UIJob(site.getShell().getDisplay(), "Maven Build Progress View Refresh Job") {
      @Override
      public IStatus runInUIThread(IProgressMonitor monitor) {
        return BuildProgressView.this.runInUIThread(monitor);
      }
    };
    refreshJob.setUser(false);
  }


  protected IStatus runInUIThread(IProgressMonitor monitor) {
    List<Object> queue;
    synchronized (refreshQueue) {
      queue = new ArrayList<>(refreshQueue);
      refreshQueue.clear();
    }

    viewer.getTree().setRedraw(false);
    try {
      for (Object object : queue) {
        if (object instanceof Launch) {
          viewer.setInput(object);
          BuildStatus status = ((Launch) object).getStatus();
          progressBar.reset(status.hasFailures(), false /* stopped */,
              status.getCompleted() /* tickDone */, status.getTotal() /* maximum */);
        } else if (object instanceof Project) {
          if (isProjectShown(object)) {
            // workaround apparent TreeViewer bug
            // filtered nodes are not revealed when filter state changes
            viewer.refresh();
          } else {
            viewer.refresh(object, true);
          }
          if (actionFailuresOnly.isChecked()) {
            viewer.expandToLevel(object, 2);
          } else {
            viewer.reveal(object);
          }

          Launch launch = (Launch) viewer.getInput();
          BuildStatus status = launch.getStatus();
          progressBar.reset(status.hasFailures(), false /* stopped */,
              status.getCompleted() /* tickDone */, status.getTotal() /* maximum */);
        }
      }
    } finally {
      viewer.getTree().setRedraw(true);
    }
    return org.eclipse.core.runtime.Status.OK_STATUS;
  }

  protected boolean isProjectShown(Object object) {
    return failureFilter.select(null, null, object);
  }

  private Object getSelectedElement() {
    IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
    return selection.getFirstElement();
  }

  private void openLogViewer() {
    Object selection = getSelectedElement();
    if (selection instanceof Project) {
      Launch launch = (Launch) viewer.getInput();
      Project project = (Project) selection;
      File file = CORE.getLogFile(launch.getId(), project.getId());
      IFileStore fileStore = EFS.getLocalFileSystem().getStore(file.toURI());
      try {
        IDE.openEditorOnFileStore(getSite().getPage(), fileStore);
      } catch (PartInitException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    }
  }

  protected void applyFailuresOnlyFilter() {
    if (actionFailuresOnly.isChecked()) {
      viewer.addFilter(failureFilter);
    } else {
      viewer.removeFilter(failureFilter);
    }
  }

  @Override
  public void init(IViewSite site, IMemento memento) throws PartInitException {
    super.init(site);

    if (memento == null) {
      return;
    }

    Boolean failuresOnly = memento.getBoolean(TAG_FAILURES_ONLY);
    if (failuresOnly != null && failuresOnly.booleanValue()) {
      actionFailuresOnly.setChecked(true);
    }
  }

  @Override
  public void saveState(IMemento memento) {
    memento.putBoolean(TAG_FAILURES_ONLY, actionFailuresOnly.isChecked());
  }
}
