package com.googlecode.goclipse.debug.launch.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.googlecode.goclipse.Environment;
import com.googlecode.goclipse.builder.GoConstants;
import com.googlecode.goclipse.builder.GoNature;
import com.googlecode.goclipse.debug.GoDebugPlugin;
import com.googlecode.goclipse.debug.ui.BuildConfiguration;

/**
 * 
 * @author steel
 */
public class GoLaunchConfigurationTab extends AbstractLaunchConfigurationTab {
	private GoLaunchConfigurationTabComposite composite;
	private String errorMessage = null;

	public GoLaunchConfigurationTab() {

	}

	@Override
	public boolean canSave() {
		return validate();
	}

	/**
	 * @return
	 */
	protected boolean validate() {
		// run the gaunlet
		try {
			if (composite == null) {
				return false;
			}

			String project = composite.getProject();
			if (project == null) {
				errorMessage = "The user must set a valid Go project.";
				composite.setMainConfigEnabled(false);
				return false;
			}

			IProject iProject = null;

			try {
				// TODO: we should remove references to env get project
				iProject = Environment.INSTANCE.getCurrentProject().getWorkspace().getRoot()
						.getProject(project);
			} catch (Exception e) {
			}

			if (iProject == null) {
				errorMessage = "The project folder given could not be found.";
				composite.setMainConfigEnabled(false);
				return false;
			}

			if (iProject.getNature(GoNature.NATURE_ID) == null) {
				errorMessage = "The project does not have a Go Nature assigned to it.  "
						+ "Please reference valid Go project.";
				composite.setMainConfigEnabled(false);
				return false;
			}

			composite.setMainConfigEnabled(true);
			IResource mainfile = null;

			try {
				mainfile = iProject.findMember(composite.getMainFile());
			} catch (Exception e) {
			}

			if (mainfile == null) {
				errorMessage = "The main file could not be found.  "
						+ "Please make sure the given path is correct and is relative to the project.";
				return false;
			}

		} catch (CoreException e) {
			errorMessage = "A problem has occurred while attempting to validate the fields in this configuration.";
		}
		errorMessage = null;
		return true;
	}

	@Override
	public void createControl(Composite parent) {
		composite = new GoLaunchConfigurationTabComposite(parent, this, SWT.NULL);
	}

	@Override
	public void dispose() {
		if (composite != null)
			composite.dispose();
	}

	@Override
	public Control getControl() {
		return composite;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public Image getImage() {
		return GoDebugPlugin.getImage("icons/go-icon16.png");
	}

	@Override
	public String getMessage() {
		return "Configure a Go application to run.";
	}

	@Override
	public String getName() {
		return "Main";
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {

		try {
			String projectname = configuration.getAttribute(GoConstants.GO_CONF_ATTRIBUTE_PROJECT,
					"");
			String mainfile = configuration.getAttribute(GoConstants.GO_CONF_ATTRIBUTE_MAIN, "");
			String buildconfig = configuration.getAttribute(
					GoConstants.GO_CONF_ATTRIBUTE_BUILD_CONFIG, "");
			String programargs = configuration.getAttribute(GoConstants.GO_CONF_ATTRIBUTE_ARGS, "");

			composite.setProject(projectname);
			composite.setMainFile(mainfile);
			composite
					.setBuildConfig(buildconfig != null && !buildconfig.equals("") ? BuildConfiguration
							.get(buildconfig) : BuildConfiguration.DEBUG);
			composite.setProgramArgs(programargs);

		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		return validate();
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(GoConstants.GO_CONF_ATTRIBUTE_PROJECT, composite.getProject());
		configuration.setAttribute(GoConstants.GO_CONF_ATTRIBUTE_MAIN, composite.getMainFile());
		configuration.setAttribute(GoConstants.GO_CONF_ATTRIBUTE_BUILD_CONFIG, composite
				.getBuildConfiguration().toString());
		configuration.setAttribute(GoConstants.GO_CONF_ATTRIBUTE_ARGS, composite.getProgramArgs());
		configuration.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, true);
		configuration.setAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, "UTF-8");
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(GoConstants.GO_CONF_ATTRIBUTE_PROJECT, "enter a project here...");

		configuration.setAttribute(GoConstants.GO_CONF_ATTRIBUTE_MAIN, "");
		configuration.setAttribute(GoConstants.GO_CONF_ATTRIBUTE_BUILD_CONFIG,
				BuildConfiguration.RELEASE.toString());
		configuration.setAttribute(GoConstants.GO_CONF_ATTRIBUTE_ARGS, "");
	}

	public ILaunchConfigurationDialog getLaunchConfigurationDialog() {
		return super.getLaunchConfigurationDialog();
	}

}
