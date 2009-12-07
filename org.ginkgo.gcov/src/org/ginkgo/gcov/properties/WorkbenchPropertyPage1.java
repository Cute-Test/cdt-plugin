package org.ginkgo.gcov.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.ginkgo.gcov.builder.SampleNature;

public class WorkbenchPropertyPage1 extends PropertyPage implements
		IWorkbenchPropertyPage {


	private Button check;

	public WorkbenchPropertyPage1() {
		super();
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);
		
		check = new Button(composite,SWT.CHECK);
		check.setText("Activate nature");
		IAdaptable element = getElement();		
		IProject project = (IProject)element.getAdapter(IProject.class); 
		try {
			check.setSelection(project.hasNature(SampleNature.NATURE_ID));
		} catch (CoreException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return composite;
	}
	@Override
	public boolean performOk() {
		try {
			if(check.getSelection()){
				addNature();
			}else{
				removeNature();
			}
			return true;
		} catch (CoreException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			return false;
		}
		
	}
	private void addNature() throws CoreException {
		IAdaptable element = getElement();		
		IProject project = (IProject)element.getAdapter(IProject.class); 
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();
//		has nature?
		for (int i = 0; i < natures.length; i++) {
			if(SampleNature.NATURE_ID.equals(natures[i])){
				return;
			}
		}
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = SampleNature.NATURE_ID;
		description.setNatureIds(newNatures);
		project.setDescription(description, null);
	}
	private void removeNature() throws CoreException {
		IAdaptable element = getElement();		
		IProject project = (IProject)element.getAdapter(IProject.class); 
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();
		for (int i = 0; i < natures.length; i++) {
			if(SampleNature.NATURE_ID.equals(natures[i])){
				String[] newNatures = new String[natures.length - 1];
				System.arraycopy(natures, 0, newNatures, 0, i);
				System.arraycopy(natures, i + 1, newNatures, i,
						natures.length - i - 1);
				description.setNatureIds(newNatures);
				project.setDescription(description, null);
				return;
			}
		}
	}


	
}
