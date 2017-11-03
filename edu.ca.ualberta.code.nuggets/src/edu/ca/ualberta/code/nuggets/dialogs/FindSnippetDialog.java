package edu.ca.ualberta.code.nuggets.dialogs;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import edu.ca.ualberta.code.nuggets.MainController;
import edu.ca.ualberta.code.nuggets.analysis.ASTAnalyzer;
import edu.ca.ualberta.code.nuggets.backend.BackendConnector;
import edu.ca.ualberta.code.nuggets.crawl.GistParser;
import edu.ca.ualberta.code.nuggets.data.GistResultSet;
import edu.ca.ualberta.code.nuggets.data.Snippet;
import edu.ca.ualberta.code.nuggets.data.User;
import edu.ca.ualberta.code.nuggets.utils.ImageTransformations;
import edu.ca.ualberta.code.nuggets.utils.StringUtils;

/**
 * Main search dialog of the plugin
 */
public class FindSnippetDialog extends TitleAreaDialog {

	/**
	 * Container of the list of results
	 */
	private Composite containerResults;

	/**
	 * Container of the search bar
	 */
	private Composite containerSearch;

	/**
	 * Container of the list of results to make it scrollable
	 */
	private ScrolledComposite scroll;

	/**
	 * Main Search Box
	 */
	private Text txtSearch;

	/**
	 * Language Search
	 */
	private Text txtLanguage;

	private Shell mainShell;

	private BackendConnector backend;

	/**
	 * Table of the current search results for insertion purposes.
	 */
	Hashtable<Integer, Snippet> currentResults;

	/**
	 * Previous button for pagination commands
	 */
	Button previous;

	/**
	 * Next button for pagination commands
	 */
	Button next;

	private MainController controller;

	private ASTAnalyzer analyzer;

	/**
	 * Current page (pagination)
	 */
	private int curPage = 1;

	/**
	 * Current search text
	 */
	private String curSearchText;

	/**
	 * Current search language
	 */
	private String curSearchLanguage;

	/**
	 * Number of results of search
	 */
	private int numResults;

	private Composite containerPageControls;

	/**
	 * Flag indicating if the current search is based on ranking
	 */
	private boolean isRankedSearch = false;

	/**
	 * Stores the current set of results.  For ranked search, it
	 * stores PAGES_PER_RANKED_SEARCH * SNIPPETS_PER_PAGE, so
	 * when there is need to go to the next page, the results are
	 * already stored in this variable
	 */
	private List<Snippet> cachedResults = new ArrayList<Snippet>();

	/**
	 * Constant to set the number of pages to fetch when
	 * the user submits a ranked search
	 */
	private static final int PAGES_PER_RANKED_SEARCH = 5;

	/**
	 * Number of snippets returned per page
	 */
	private static final int SNIPPETS_PER_PAGE = 10;

	private static final String PREVIOUS_BUTTON_TEXT = "<< Previous";
	private static final String NEXT_BUTTON_TEXT = "Next >>";
	private static final String INSERT_BUTTON_TEXT= "Insert";
	private static final String INSERT_SELECTION_BUTTON_TEXT= "Insert Selection";

	public FindSnippetDialog(Shell parentShell, MainController controllerP) {
		super(parentShell);
		controller = controllerP;
		currentResults = new Hashtable<>();
		analyzer = new ASTAnalyzer();
		mainShell = parentShell;
		backend = new BackendConnector();
	}

	@Override
	public void create() {
		super.create();
		setTitle("Find Snippet");
		setMessage("Introduce keywords and click search!",
				IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		// Area is the main component
		Composite area = (Composite) super.createDialogArea(parent);

		// Initializing the search bar component (just the skeleton)
		GridLayout layout = new GridLayout(6, false);
		containerSearch = new Composite(area, SWT.NONE);
		containerSearch.setLayoutData(new GridData(GridData.FILL_BOTH));
		containerSearch.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		containerSearch.setLayout(layout);

		// Initializing the scroll component for the result component
		scroll = new ScrolledComposite(area, SWT.BORDER | SWT.V_SCROLL);

		// Initializing the component where the results are inserted (empty by default)
		GridLayout layoutResults = new GridLayout(1, false);
		containerResults = new Composite(scroll, SWT.NONE);
		containerResults.setSize(600, 400);
		containerResults.setLayoutData(new GridData(GridData.FILL_BOTH));
		containerResults.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		containerResults.setLayout(layoutResults);

		// Adding the results component to the scroll
		scroll.setContent(containerResults);

		// Initialize the buttons and text fields of the search bar.
		createSearchBar(containerSearch);

		// Initializing the component where the pagination controls are inserted
		RowLayout layoutPagination = new RowLayout();
		layoutPagination.fill = true;
		layoutPagination.pack = false;
		containerPageControls = new Composite(area, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 35;
		containerPageControls.setLayoutData(gd);
		containerPageControls.setLayout(layoutPagination);

		return area;
	}

	/**
	 * Initializes the elements inside the search bar component
	 *
	 * @param container
	 * @param results
	 */
	private void createSearchBar(Composite container) {
		Label lbSearch = new Label(container, SWT.NONE);
		lbSearch.setText("Tags:");

		GridData dataTags = new GridData();
		dataTags.grabExcessHorizontalSpace = true;
		dataTags.horizontalAlignment = GridData.FILL;
		txtSearch = new Text(container, SWT.BORDER);
		txtSearch.setLayoutData(dataTags);

		Label lbLan = new Label(container, SWT.NONE);
		lbLan.setText("Language:");

		txtLanguage = new Text(container, SWT.BORDER);
		txtSearch.setLayoutData(dataTags);

		Button search = new Button(container, SWT.PUSH);
		search.setText("Search");
		search.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				cleanResultsPanelsBeforeSearch();
				cleanPaginationPanel();

  				curSearchText = txtSearch.getText();
  				curSearchLanguage = txtLanguage.getText();
				curPage = 1;
				isRankedSearch = false;
				cachedResults = new ArrayList<Snippet>();
				//sendFeedback(curSearchText.split(" "));
				Job job = new SearchJob(curSearchText, curSearchLanguage, curPage);
				job.setUser(true);
		  		job.schedule();

			}
		});

		Button rankedSearch = new Button(container, SWT.PUSH);
		rankedSearch.setText("Ranked Search");
		rankedSearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				cleanResultsPanelsBeforeSearch();
				cleanPaginationPanel();

  				curSearchText = txtSearch.getText();
  				curSearchLanguage = txtLanguage.getText();
				curPage = 1;
				isRankedSearch = true;
				cachedResults = new ArrayList<Snippet>();

				Job job = new SearchJob(curSearchText, curSearchLanguage, curPage, PAGES_PER_RANKED_SEARCH);
				job.setUser(true);
		  		job.schedule();

			}
		});
	}

	private void cleanResultsPanelsBeforeSearch() {
		// print loading message
		cleanResultsContainer();
		Label loadingLabel = new Label(containerResults, SWT.NONE);
		loadingLabel.setText("Please wait...");
		containerResults.pack();
		containerResults.update();
	}

	private void cleanPaginationPanel() {
		// clean pagination controls
		cleanContainer(containerPageControls);
		containerPageControls.layout();
		containerPageControls.update();
	}

	/**
	 * Gets the tags, cleans them using extractTags(..) and then return the snippets.
	 *
	 * @param rawKeywords
	 * @param page
	 * @param monitor Progress monitor
	 * @return List of snippets satisfying the conjunctive query formed by the tags
	 */
	private List<Snippet> getResults(String rawKeywords, String language, int page, int numPages, IProgressMonitor monitor) {

		System.out.println("SEARCH= " + rawKeywords);
		String[] keywords = extractTags(rawKeywords);
		System.out.println(keywords.length);

		GistResultSet r = controller.getSnippets(language, keywords, page, numPages, monitor);
		List<Snippet> resultList = r.getSnippets();
		numResults = r.getTotalResults();
		System.out.println("Num Results: " + numResults);

		if (isRankedSearch) {
			Collections.sort(resultList, Collections.reverseOrder());
		}

		return resultList;
	}


	/**
	 * Render the snippets in the container for results.
	 *
	 * @param resultList List of snippets to draw
	 * @param rawKeywords Keywords used to highlight portions of the snippet
	 */
	private void drawResults(List<Snippet> resultList, String rawKeywords) {

		String[] keywords = extractTags(rawKeywords);

		cleanResultsContainer();

		if (resultList != null) {
			for (Snippet s : resultList) {
				if (currentResults.get(s.getId()) == null) {
					addSnippetView(s, keywords);
					currentResults.put(s.getId(), s);
				}
			}

			createPaginationControls();
		}

		if (resultList == null || resultList.size() == 0) {
			cleanResultsContainer();

			if (curPage == 1) {
				Label loadingLabel = new Label(containerResults, SWT.NONE);
				loadingLabel.setText("No results were found");
			} else {
				createPaginationControls();
				Label loadingLabel = new Label(containerResults, SWT.NONE);
				loadingLabel.setText("Sorry, no more results");
				next.setEnabled(false);
			}
			containerResults.pack();
			containerResults.update();
		}
	}

	/**
	 * Creates pagination controls (Previous and Next buttons)
	 */
	private void createPaginationControls() {

		cleanContainer(containerPageControls);

		previous = new Button(containerPageControls, SWT.PUSH);

		previous.setText(PREVIOUS_BUTTON_TEXT);
		previous.addSelectionListener(new PageSelection());

		if (curPage == 1) {
			previous.setEnabled(false);
		} else {
			previous.setEnabled(true);
		}

		next = new Button(containerPageControls, SWT.PUSH);
		next.setText(NEXT_BUTTON_TEXT);
		next.addSelectionListener(new PageSelection());

		if (curPage * GistParser.RESULTS_PER_PAGE < numResults) {
			next.setEnabled(true);
		} else {
			next.setEnabled(false);
		}

		containerPageControls.layout();
		containerPageControls.update();
	}

	/**
	 * Generic utility method to display information panes to the user.
	 *
	 * @param title
	 * @param content
	 */
	private void showToast(String title, String content) {
		MessageBox dialog = new MessageBox(mainShell, SWT.ICON_INFORMATION
				| SWT.OK);
		dialog.setText(title);
		dialog.setMessage(content);
		dialog.open();
	}

	/**
	 * Cleans the snippet view results from the gui container
	 */
	private void cleanResultsContainer() {
		currentResults = new Hashtable<>();
		cleanContainer(containerResults);
	}

	/**
	 * Cleans the pagination controls
	 */
	private void cleanContainer(Composite container) {
		for (Control control : container.getChildren()) {
			control.dispose();
		}
	}

	/**
	 * Adds a new snippet view to the results component. 100 by 570 is the
	 * default and enforced size of the views to make the scroll work after
	 * calling pack (so it never packs and resizes less that those values)
	 *
	 * @param s The snippet
	 * @param keywords
	 *            (the list of keywords used to obtain this snippet for
	 *            highlighting purposes)
	 */
	private void addSnippetView(Snippet s, String[] keywords) {

		final Snippet finalSnippet = s;
		GridData dataSnippet = new GridData();

		createSnippetTitleLabel(s);

		dataSnippet.grabExcessHorizontalSpace = true;
		dataSnippet.horizontalAlignment = SWT.FILL;
		final int heightHint = 200;
		final int widthHint = 570;
		dataSnippet.heightHint = heightHint;
		dataSnippet.widthHint = widthHint;

		final StyledText txtCode = new StyledText(containerResults, SWT.MULTI | SWT.V_SCROLL);
		txtCode.setLayoutData(dataSnippet);
		txtCode.setText(s.getSnippet());

		highlightKeywords(keywords, txtCode);

		GridLayout buttonsLayout = new GridLayout(2, false);
		Composite buttonPanel = new Composite(containerResults, SWT.NONE);
		//buttonPanel.setSize(600, 500);
		buttonPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
		buttonPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		buttonPanel.setLayout(buttonsLayout);

		// Insert the entire snippet.
		Button insert = new Button(buttonPanel, SWT.PUSH);
		insert.setText(INSERT_BUTTON_TEXT);
		insert.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				controller.insertSnippet(finalSnippet);
				close();
			}
		});

		//Insert selected sub-snippet
		Button insertPartial = new Button(buttonPanel, SWT.PUSH);
		insertPartial.setText(INSERT_SELECTION_BUTTON_TEXT);
		insertPartial.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//Mutates the snippet so it has only the selection
				finalSnippet.setSnippet(txtCode.getSelectionText());
				controller.insertSnippet(finalSnippet);
				close();
			}
		});


		buttonPanel.pack();
		containerResults.pack();
	}

	/**
	 * Creates the title of the snippet of the form: <i>image</i> username [followers: #, stars: #] - date
	 *
	 * @param s The snippet
	 */
	private CLabel createSnippetTitleLabel(Snippet s) {
		CLabel label = new CLabel(containerResults, SWT.NONE);

		FontDescriptor boldDescriptor = FontDescriptor.createFrom(
				label.getFont()).setStyle(SWT.BOLD);
		Font boldFont = boldDescriptor.createFont(label.getDisplay());
		label.setFont(boldFont);

		try {
			Image avatar = ImageDescriptor.createFromURL(
					new URL(s.getUser().getPicUrl())).createImage();
			if (avatar.getBounds().width > 20) {
				avatar = ImageTransformations.resize(avatar, 20, 20);
			}

			label.setImage(avatar);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		User u = s.getUser();
		label.setText(u.getUsername()
				+ "  [followers: " + u.getFollowers() + ", stars: "+ u.getTotalRepoStars() +"] - "
				+ dateFormat.format(s.getDate()));

		return label;
	}

	/**
	 * Pre-process a StyledText field and highlights the lines of that contain
	 * one or more keywords.
	 *
	 * @param keywords
	 * @param txtCode
	 */
	private void highlightKeywords(String[] keywords, final StyledText txtCode) {

		int numLines = txtCode.getLineCount();
		Color orange = new Color(mainShell.getDisplay(), 198, 250, 155);
		for (int i = 0; i < numLines; i++) {
			for (String word : keywords) {
				if (txtCode.getLine(i).contains(word)) {
					txtCode.setLineBackground(i, 1, orange);
				}
			}
		}
	}

	/**
	 * Cleans a list of tags or keywords separated by commas.
	 *
	 * @param tagsRaw
	 * @return
	 */
	private String[] extractTags(String tagsRaw) {
		String[] tags = tagsRaw.split(",");
		for (int i = 0; i < tags.length; i++) {
			String t = tags[i];
			tags[i] = t.trim();
		}
		return tags;
	}

	@Override
	protected boolean isResizable() {
		return false;
	}

	// *****************************************************************************************
	// Contextual Results Experimental
	// *****************************************************************************************

	/**
	 * Uses extractClassMethodsInformation from the AST analyzer to obtain a
	 * list of keywords (using the names of the methods of the current class on
	 * open on the editor) to query the snippet provider, and then add the
	 * results to the gui .
	 */
	public void addResultsByClassMethodContext() {

		ArrayList<String> methodNames = analyzer.extractClassMethodsInformation();
		System.out.println(methodNames.toString());
		String[] names = methodNames.toArray(new String[methodNames.size()]);

		cleanResultsPanelsBeforeSearch();

		curPage = 1;
		isRankedSearch = false;
		curSearchText = StringUtils.join(names, ",");
		cachedResults = new ArrayList<Snippet>();

		Job job = new SearchJob(curSearchText, curSearchLanguage, curPage);
		job.setUser(true);
  		job.schedule();
	}

	/**
	 * Uses extractMethodLocalTypes from the AST analyzer to obtain a list of
	 * keywords (using the complex types of the variables defined inside the
	 * current method where the cursor stands -- filtered by the stop-word list
	 * the excludes simple types) to query the snippet provider, and then add
	 * the results to the gui .
	 */
	public boolean addResultsByMethodLocalTypesContext() {
		Set<String> methodNames = analyzer.extractMethodLocalTypes();

		sendFeedback(methodNames.toArray(new String[0]));
		if(!methodNames.isEmpty()){
			String[] names = methodNames.toArray(new String[methodNames.size()]);

			cleanResultsPanelsBeforeSearch();

			curPage = 1;
			isRankedSearch = false;
			curSearchText = StringUtils.join(names, ",");
			cachedResults = new ArrayList<Snippet>();

			Job job = new SearchJob(curSearchText, curSearchLanguage, curPage);
			job.setUser(true);
	  		job.schedule();

	  		return true;
		}
		return false; // the list of types is empty

	}


	/**
	 * Sends feedback about the query submitted
	 *
	 * @param keywords
	 */
	private void sendFeedback(String[] keywords) {
		Locale locale = Locale.getDefault();
		String country = locale.getDisplayCountry();
		if (MainController.FEEDBACK_ACTIVE) {
			try {
				backend.registerQuery(Arrays.toString(keywords), ASTAnalyzer.getClassContex(), country);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * Job that is executed as a thread to collect information about a Gist search
	 *
	 * @author Diego Serrano
	 */
	class SearchJob extends Job {

		String searchText;
		String searchLanguage;
		int page = 1;
		int numPages = 1;

		/**
		 * Constructor that initializes a search job with a search text and a page number.
		 * @param searchText
		 * @param page
		 */
		public SearchJob(String searchText, String searchLanguage, int page) {
			super("Search snippets");
			this.searchText = searchText;
			this.searchLanguage = searchLanguage;
			this.page = page;
			System.out.println("Page = " + page);
		}

		public SearchJob(String searchText, String searchLanguage, int page, int numPages) {
			this(searchText, searchLanguage, page);
			this.numPages = numPages;
		}

		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			try {
				final List<Snippet> results;

				if (cachedResults.isEmpty() || !isRankedSearch) {
					int totalProgress = (5 * SNIPPETS_PER_PAGE) 	// 5 per snippet in the results
							+ (10 * SNIPPETS_PER_PAGE)				// 10 per raw gist parsed
							+ 10;									// 10 per finalization steps

					if (isRankedSearch) {
						totalProgress *= PAGES_PER_RANKED_SEARCH;
					}
					monitor.beginTask("Search snippets", totalProgress);

					cachedResults = getResults(searchText, searchLanguage, page, numPages, monitor);
				} else {
					monitor.beginTask("Search Snippets", 10);		// 10 per finalization steps
				}

				if (isRankedSearch) {
					int startIndex = (page-1) * SNIPPETS_PER_PAGE;
					int endIndex = (cachedResults.size() > page * SNIPPETS_PER_PAGE)? page * SNIPPETS_PER_PAGE : cachedResults.size();
					results = cachedResults.subList(startIndex, endIndex);
				} else {
					results = cachedResults;
				}

				// execute in UI thread
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						drawResults(results, searchText);
						monitor.worked(10);
						monitor.done();
					}
				});

				return Status.OK_STATUS;
			} catch (Exception e) {
				e.printStackTrace();
			}

			return Status.CANCEL_STATUS;
		}
	}

	class PageSelection extends SelectionAdapter {

		public PageSelection() {
			super();
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			next.setEnabled(false);
			previous.setEnabled(false);

			// print loading message
			cleanResultsContainer();
			Label loadingLabel = new Label(containerResults, SWT.NONE);
			loadingLabel.setText("Please wait...");
			containerResults.pack();
			containerResults.update();

			// search for gists
			Job job = null;

			Button button = (Button) e.getSource();
			if (button.getText().equals(NEXT_BUTTON_TEXT)) {
				job = new SearchJob(curSearchText, curSearchLanguage, ++curPage);
			} else if (button.getText().equals(PREVIOUS_BUTTON_TEXT)) {
				job = new SearchJob(curSearchText, curSearchLanguage, --curPage);
			}

			if (job != null) {
				job.setUser(true);
		  		job.schedule();
			}
		}
	}
}
