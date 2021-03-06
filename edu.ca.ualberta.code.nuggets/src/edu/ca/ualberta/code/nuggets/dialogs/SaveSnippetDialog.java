package edu.ca.ualberta.code.nuggets.dialogs;
import java.awt.Color;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Save snippet dialog, currently deprecated.
 */
public class SaveSnippetDialog extends TitleAreaDialog {

	  private Text txtCode;
	  private Text txtTags;

	  private String code;
	  private String tags;

	  public SaveSnippetDialog(Shell parentShell, String snippet) {
	    super(parentShell);
	    code = snippet;
	  }

	  @Override
	  public void create() {
	    super.create();
	    setTitle("Save Snippet");
	    setMessage("Please tag your snippet with keywords separated by commas.", IMessageProvider.INFORMATION);
	  }

	  @Override
	  protected Control createDialogArea(Composite parent) {
	    Composite area = (Composite) super.createDialogArea(parent);
	    Composite container = new Composite(area, SWT.NONE);
	    container.setLayoutData(new GridData(GridData.FILL_BOTH));
	    GridLayout layout = new GridLayout(1, false);
	    container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    container.setLayout(layout);

	    createSnippet(container);
	    createLastName(container);

	    return area;
	  }

	  private void createSnippet(Composite container) {
	    Label lbInfo = new Label(container, SWT.NONE);
	    lbInfo.setText("Code Snippet:");

	    GridData dataSnippet = new GridData();
	    dataSnippet.grabExcessHorizontalSpace = true;
	    dataSnippet.horizontalAlignment = GridData.FILL;
	    final int heightHint = 150;
	    dataSnippet.heightHint = heightHint;

	    txtCode = new Text(container, SWT.MULTI | SWT.V_SCROLL);
	    txtCode.setLayoutData(dataSnippet);
	    txtCode.setText(code);
	  }

	  private void createLastName(Composite container) {
	    Label lbtTags = new Label(container, SWT.NONE);
	    lbtTags.setText("Tags:");

	    GridData dataTags = new GridData();
	    dataTags.grabExcessHorizontalSpace = true;
	    dataTags.horizontalAlignment = GridData.FILL;
	    txtTags = new Text(container, SWT.BORDER);
	    txtTags.setLayoutData(dataTags);

	  }



	  @Override
	  protected boolean isResizable() {
	    return true;
	  }

	  // save content of the Text fields because they get disposed
	  // as soon as the Dialog closes
	  private void saveInput() {
	    code = txtCode.getText();
	    tags = txtTags.getText();

	  }

	  @Override
	  protected void okPressed() {
	    saveInput();
	    super.okPressed();
	  }

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

}
