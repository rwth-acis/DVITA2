package i5.dvita.webapplication.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Color;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.ToolTipData;
import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
import org.moxieapps.gwt.highcharts.client.events.PointClickEvent;
import org.moxieapps.gwt.highcharts.client.events.PointClickEventHandler;
import org.moxieapps.gwt.highcharts.client.events.SeriesClickEvent;
import org.moxieapps.gwt.highcharts.client.events.SeriesClickEventHandler;
import org.moxieapps.gwt.highcharts.client.labels.DataLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.DataLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.LegendLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.LegendLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.PieDataLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.AreaPlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.AreaSplinePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker.Symbol;
import org.moxieapps.gwt.highcharts.client.plotOptions.PiePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.PlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.SeriesPlotOptions;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;

import i5.dvita.commons.ACL;
import i5.dvita.webapplication.shared.DVitaParameters;
import i5.dvita.webapplication.shared.DocumentData;
import i5.dvita.webapplication.shared.DocumentInfo;
import i5.dvita.webapplication.shared.ThemeRiverData;
import i5.dvita.webapplication.shared.TopicLabels;
import i5.dvita.webapplication.shared.UserShared;
import i5.dvita.webapplication.shared.WordData;
import i5.dvita.webapplication.shared.WordEvolutionData;

import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.DragAppearance;
import com.smartgwt.client.types.EmbeddedPosition;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.SelectionType;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.Page;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.HTMLFlow;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.TransferImgButton;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClickEvent;
import com.smartgwt.client.widgets.form.ColorPicker;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.events.ColorSelectedEvent;
import com.smartgwt.client.widgets.form.events.ColorSelectedHandler;
import com.smartgwt.client.widgets.form.fields.PickerIcon;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangeEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangeHandler;
import com.smartgwt.client.widgets.form.fields.events.FormItemClickHandler;
import com.smartgwt.client.widgets.form.fields.events.FormItemIconClickEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyPressEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyPressHandler;
import com.smartgwt.client.widgets.grid.HoverCustomizer;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.Layout;
import com.smartgwt.client.widgets.layout.LayoutSpacer;
import com.smartgwt.client.widgets.layout.SectionStackSection;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.tab.events.TabSelectedEvent;
import com.smartgwt.client.widgets.tab.events.TabSelectedHandler;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */

public class DVITA_WebApplication implements EntryPoint
{
	public LoadingWindow loadingWindow = new LoadingWindow();

	final double OVERLAP_OPACITY = 0.25;
	final double STACKED_OPACITY = 0.75;
	final static String WORDSEPARATOR = " - ";
	
	private static final String SERVER_ERROR = "An error occurred while connection and try again.";
	
	private final DocumentServiceAsync theDocumentService = GWT.create(DocumentService.class);
	private final TopicServiceAsync topicService = GWT.create(TopicService.class);
	private final WordServiceAsync wordService = GWT.create(WordService.class);
	private final SetupServiceAsync setupService = GWT.create(SetupService.class);

	ListGrid  documentGrid  = null;
	HLayout documentBrowserCanvas = new HLayout();
	final VStack TopicOverviewPanel = new VStack();
	final DynamicForm topicSearchForm = new DynamicForm();

	// speichert zu der TopicID die 3 wichtigsten Wörter
	// dies kann nachher für die Legenden etc genutzt werden
	HashMap<Integer,String> topicIDtoName = new  HashMap<Integer,String>();

	// speichert zur WordID das eigentliche Wort
	HashMap<Integer,String> wordIDtoName = new  HashMap<Integer,String>();

	HTMLFlow contentOfDocument = new HTMLFlow();  

	//Aufruf von ThemeRiver:
	final ClickHandlerTopicCheckbox handlerTopicCheckbox= new ClickHandlerTopicCheckbox();

	//Aufruf von ThemeRiver der Wortevolution:
	final ClickHandlerWortCheckbox handlerWortCheckbox= new ClickHandlerWortCheckbox();

	final HTML serverResponseLabel = new HTML();
	final DialogBox dialogBox = new DialogBox();
	final Button closeButton = new Button("Close");

	boolean drawDifferenceInTopicThemeRiver = false;
	boolean drawDifferenceInWordThemeRiver = true;

	HashMap<Long,Integer> xCoordChartToIntervalID = new HashMap<Long,Integer>();

	final  IButton varianceButton= new  IButton();
	final  IButton sinkingButton= new  IButton();
	final  IButton risingButton= new  IButton();
	final  IButton risingDecayButton= new  IButton();
	final  IButton meanButton= new  IButton();

	HLayout splitLayout = new HLayout();
	VStack leftStack = new VStack();
	HLayout hlayout;

	Chart relatedDocCharts = null;

	/**
	 * der ThemeRiver Chart
	 */
	
	Chart themeRiverChart= new Chart();

	private Canvas mainCanvas;
	private Canvas leftpartCanvas;
	private Canvas rightpartCanvas;
	VStack modifyStack = new VStack(3);  

	private HLayout searchAreaLayout;

	private Canvas upperPartCanvas;
	private Canvas leftPartCanvas;
	private Canvas documentsOfTopicCanvas;

	ThemeRiverData[] themeRiverDataForTopic;

	public int lastClickedTimePoint;

	private DVITA_WebApplication thisWindow = this;

	public int lastClickedTopic;

	private ClickHandlerTopicFromThemeRiver handlerTopicFromThemeRiver;
	public int currentNumberDocs;

	private Label currentTopicLabel;

	private Chart wordEvolutionChart = new Chart();

	TextItem textitem = new TextItem();

	private Window topicExplorerWindow;

	private DocumentExplorerWindow documentExplorerWindow;

	private Label currentTopicLabel2;

	int numberTopics = 5;

	public String[][] topicIntervals;

	private Label[] colorLabels;
	Color[] topicColors;
	String[] topicColorsString;

	private Canvas pieChartCanvas;

	private RadioGroupItem radioGroupItemThemeRiver;

	String round(double number, int pos) 
	{
		double factor = Math.pow(10,pos);
		return ""+((int)(number*factor))/factor;
	}
	
	private UserShared _user = null;
	private ToolStripButton _controlPanelButton = null;
	private ToolStripButton _logOutButton = null;
	
	/**
	 * This is the entry point method.
	 */
	
	public void onModuleLoad()
	{
		String discoString = Cookies.getCookie("disco");
		
		if (null != discoString)
		{
			// open id response
			String responseURL = com.google.gwt.user.client.Window.Location.getHref();
			loadingWindow.start("Verify login<br />", 1);
			setupService.openIdVerify(responseURL, discoString, new AsyncCallback<String>()
			{
				@Override
				public void onFailure(Throwable caught)
				{
					loadingWindow.stop();
					SC.say(caught.getMessage());
				}

				@Override
				public void onSuccess(String result)
				{
					loadingWindow.stop();

					if (null != result)
					{
						SC.say(result);
					}

					startDVITA();
				}
			});
			
			Cookies.removeCookie("disco");
		}
		else
		{
			startDVITA();
//			setupService.openIdVerify("bla", "bla", new AsyncCallback<String>()
//					{
//						@Override
//						public void onFailure(Throwable caught)
//						{
//							loadingWindow.stop();
//							SC.say(caught.getMessage());
//						}
//
//						@Override
//						public void onSuccess(String result)
//						{
//							loadingWindow.stop();
//
//							if (null != result)
//							{
//								SC.say(result);
//							}
//						}
//					});
		}
	}

	private void startDVITA()
	{
		// get user
		setupService.getUser(new AsyncCallback<UserShared>()
		{
			@Override
			public void onFailure(Throwable caught)
			{
				SC.say(caught.getMessage());
			}

			@Override
			public void onSuccess(UserShared result)
			{
				_user = result;
				
				if (!_user.getOperations().contains(ACL.OPERATION_LOGIN))
				{
					if (null != _logOutButton)
					{
						_logOutButton.show();
					}
				}
				
				if (_user.getOperations().contains(ACL.OPERATION_CONTROL_PANEL))
				{
					if (null != _controlPanelButton)
					{
						_controlPanelButton.show();
					}
				}
			}
		});
		
		Page.setAppImgDir("[APP]/images/");

		// setup overall layout / viewport
		VLayout main = new VLayout();

		ToolStrip topBar = new ToolStrip();
		topBar.setHeight(33);
		topBar.setWidth100();
		topBar.addSpacer(6);
		Label title = new Label("D-VITA");
		title.setStyleName("sgwtTitle");
		topBar.addMember(title);
		topBar.addSeparator();
		analysisName = new Label("");
		analysisName.setWidth(200);
		topBar.addMember(analysisName);
		
		_controlPanelButton = new ToolStripButton();
		_controlPanelButton.hide();
		_controlPanelButton.setTitle("Control Panel");
		_controlPanelButton.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() 
		{
			@Override
			public void onClick(com.smartgwt.client.widgets.events.ClickEvent event)
			{
				final ControlPanelWindow controlPanelWindow = new ControlPanelWindow(thisWindow);
				controlPanelWindow.show();				
			}
		});
		
		_logOutButton = new ToolStripButton();
		_logOutButton.hide();
		_logOutButton.setTitle("Logout");
		_logOutButton.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() 
		{
			@Override
			public void onClick(com.smartgwt.client.widgets.events.ClickEvent event)
			{
				loadingWindow.start("Logging out<br />", 1);
				setupService.logout(new AsyncCallback<Void>()
				{
					@Override
					public void onFailure(Throwable caught)
					{
						loadingWindow.stop();
					}

					@Override
					public void onSuccess(Void result)
					{
						_logOutButton.hide();
						loadingWindow.stop();
						Location.reload();
					}
				});			
			}
		});
		
		
		
		ToolStripButton devConsoleButton = new ToolStripButton();
		devConsoleButton.setTitle("Select Data Set");
		// devConsoleButton.setIcon("silk/bug.png");
		devConsoleButton.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
			@Override
			public void onClick(
					com.smartgwt.client.widgets.events.ClickEvent event) {

				final SetupWindow winModal = new SetupWindow(thisWindow,false);
				winModal.show();

			}
		});
		
		final ToolStripButton documentExplorerButton = new ToolStripButton();
		documentExplorerButton.setTitle("Document Explorer");
		documentExplorerButton.setActionType(SelectionType.CHECKBOX);
		documentExplorerButton.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
			@Override
			public void onClick(
					com.smartgwt.client.widgets.events.ClickEvent event) {

				if(!((ToolStripButton)event.getSource()).getSelected()) {
					documentExplorerWindow.hide();
					if(documentExplorerWindow.lastPie!=null) {
						documentExplorerWindow.lastPie.setVisible(false);
					}
				} else {
					documentExplorerWindow.show();
					if(documentExplorerWindow.lastPie!=null) {
						documentExplorerWindow.lastPie.setVisible(true);
					}

				}
			}
		});
		
		final ToolStripButton topicExplorerButton = new ToolStripButton();
		topicExplorerButton.setTitle("Topic Explorer");
		topicExplorerButton.setActionType(SelectionType.CHECKBOX);
		topicExplorerButton.setSelected(true);
		// devConsoleButton.setIcon("silk/bug.png");
		topicExplorerButton.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
			@Override
			public void onClick(
					com.smartgwt.client.widgets.events.ClickEvent event) {

				if(!((ToolStripButton)event.getSource()).isSelected()) {
					topicExplorerWindow.hide();
					themeRiverChart.setVisible(false);
					wordEvolutionChart.setVisible(false);
					if(lastPie!=null) {
						lastPie.setVisible(false);
					}
				} else {
					topicExplorerWindow.show();
					themeRiverChart.setVisible(true);
					wordEvolutionChart.setVisible(true);
					if(lastPie!=null) {
						lastPie.setVisible(true);
					}
				}

			}
		});

		topBar.addFill();
		topBar.addButton(_controlPanelButton);
		topBar.addSeparator();
		topBar.addButton(topicExplorerButton);
		topBar.addSpacer(1);
		topBar.addButton(documentExplorerButton);
		topBar.addSeparator();
		topBar.addButton(devConsoleButton);
		topBar.addSpacer(6);
		topBar.addSeparator();
		topBar.addButton(_logOutButton);

		main.addMember(topBar);

		main.setWidth100();
		main.setHeight100();
		main.setStyleName("tabSetContainer");
		
		FormItemClickHandler sorting = new ClickHandlerRanking();
		PickerIcon clearPicker = new PickerIcon(PickerIcon.CLEAR, sorting);
		ClickHandlerTopicSearch Topicsearch = new ClickHandlerTopicSearch();
		PickerIcon searchPicker = new PickerIcon(PickerIcon.SEARCH, Topicsearch);
		textitem.setIcons(clearPicker, searchPicker); 

		textitem.addKeyPressHandler(Topicsearch);

		topicSearchForm.setFields(textitem);
		textitem.setWidth(120);

		HLayout hLayout = new HLayout();
		hLayout.setLayoutMargin(0);
		hLayout.setWidth100();
		hLayout.setHeight100();

		leftPartCanvas = new Canvas();
		leftPartCanvas.setWidth(220);
		leftPartCanvas.setHeight100();
		leftPartCanvas.setShowResizeBar(true);

		hLayout.addMember(leftPartCanvas);

		TabSet lowerTabPanel = new TabSet();

		Layout paneContainerProperties = new Layout();
		paneContainerProperties.setLayoutMargin(0);
		paneContainerProperties.setLayoutTopMargin(1);
		lowerTabPanel.setPaneContainerProperties(paneContainerProperties);

		lowerTabPanel.setWidth100();
		lowerTabPanel.setHeight100();
		TabSelectedHandler handler = new TabSelectedHandler() {
			public void onTabSelected(TabSelectedEvent event) {
				//aktueller Tab
				Tab selectedTab = event.getTab();
				if (selectedTab.getTitle().startsWith("Word"))
				{
					wordEvolutionChart.setVisible(true);
				} else {
					wordEvolutionChart.setVisible(false);

				}
				String historyToken = selectedTab.getAttribute("historyToken");
				if (historyToken != null) {
					History.newItem(historyToken, false);
				} else {
					History.newItem("main", false);
				}
				if(selectedTab.getTitle().startsWith("W")) {
					wordEvolutionChart.setVisible(true);
				} else {
					wordEvolutionChart.setVisible(false);

				}
			}};
			lowerTabPanel.addTabSelectedHandler(handler);

			LayoutSpacer layoutSpacer = new LayoutSpacer();
			layoutSpacer.setWidth(5);


			Tab documentBrowserTab = new Tab();
			documentBrowserTab.setTitle("Related Documents");
			Tab wordEvolutionTab = new Tab();
			wordEvolutionTab.setTitle("Word Evolution");
			//tab.setWidth(80);

			initializeWordEvolutionPanel(wordEvolutionTab);
			initializeDocumentBrowserPanel(documentBrowserTab);

			lowerTabPanel.addTab(documentBrowserTab);
			lowerTabPanel.addTab(wordEvolutionTab);


			upperPartCanvas = new Canvas();
			upperPartCanvas.setWidth100();
			upperPartCanvas.setHeight100();
			upperPartCanvas.addChild(new Label());
			upperPartCanvas.setShowResizeBar(true);




			// mainTabSet.setShowResizeBar(true);

			VLayout vLayout2 = new VLayout();
			//vLayout2.setShowResizeBar(true);
			vLayout2.setLayoutMargin(0);
			vLayout2.setWidth100();
			vLayout2.setHeight100();
			vLayout2.addMember(upperPartCanvas);
			vLayout2.addMember(lowerTabPanel);


			hLayout.addMember(vLayout2);



			Canvas canvasMain = new Canvas();  
			main.addMember(canvasMain);			
			
			topicExplorerWindow = new Window();  
			topicExplorerWindow.addItem(hLayout);
			topicExplorerWindow.setWidth("100%");  
			topicExplorerWindow.setHeight("100%"); 
			topicExplorerWindow.setCanDragReposition(true);  
			topicExplorerWindow.setTitle("Topic Explorer");
			topicExplorerWindow.setCanDragResize(true);
			topicExplorerWindow.addCloseClickHandler(new CloseClickHandler(){

				@Override
				public void onCloseClick(CloseClickEvent event) {
					topicExplorerWindow.hide();
					themeRiverChart.setVisible(false);
					wordEvolutionChart.setVisible(false);
					if(lastPie!=null) {
						lastPie.setVisible(false);
					}
					topicExplorerButton.setSelected(false);
				}});

			canvasMain.addChild(topicExplorerWindow);

			documentExplorerWindow = new DocumentExplorerWindow();  
			documentExplorerWindow.setService(theDocumentService,this);
			documentExplorerWindow.addCloseClickHandler(new CloseClickHandler(){

				@Override
				public void onCloseClick(CloseClickEvent event) {
					documentExplorerWindow.hide();
					if(documentExplorerWindow.lastPie!=null) {
						documentExplorerWindow.lastPie.setVisible(false);
					}

					documentExplorerButton.setSelected(false);
				}});

			canvasMain.addChild(documentExplorerWindow);

			main.draw();
			
			final SetupWindow setupWindow = new SetupWindow(thisWindow, false);
			setupWindow.show();
	}
	
	
	private  void initializeThemeRiver(){

		themeRiverChart.removeAllSeries(true);

		themeRiverChart
			.setType(Series.Type.AREA_SPLINE)
			.setChartTitleText("Please select topics to display from the left-hand pane")		
			.setMarginRight(10)			
			.setOption("plotOptions/areaspline/stacking", drawDifferenceInTopicThemeRiver? null : "normal")
			.setOption("plotOptions/areaspline/fillOpacity", drawDifferenceInTopicThemeRiver? OVERLAP_OPACITY : STACKED_OPACITY);

		themeRiverChart.setToolTip(new ToolTip()  
		.setFormatter(new ToolTipFormatter() {  
			public String format(ToolTipData toolTipData) {  

				return toolTipData.getPointName()+": "+round(toolTipData.getYAsDouble(),2);
			}  
		}));

		//chart.setHeight("20em");
		//chart.setWidth("60em");
		themeRiverChart.setWidth100();
		themeRiverChart.setHeight100();
		themeRiverChart.getYAxis().setAxisTitleText("Relevance");
		//chart.setWidth("50em");
		themeRiverChart.setCredits( new Credits().setText(""));
		themeRiverChart.setOption("plotOptions/areaspline/trackByArea", true); 


		handlerTopicFromThemeRiver= new ClickHandlerTopicFromThemeRiver();
		themeRiverChart.setSeriesPlotOptions(new SeriesPlotOptions().setSeriesClickEventHandler(handlerTopicFromThemeRiver).setPointClickEventHandler(handlerTopicFromThemeRiver));
		
		
		//themeRiverChart.setOption("rangeSelector/buttons", "[{type: 'month',count: 1,text: '1m'}]");

		//themeRiverChart.setOption("rangeSelector/enabled", true);
		themeRiverChart.setOption("scrollbar/enabled", true);

		themeRiverChart.getXAxis().setType(org.moxieapps.gwt.highcharts.client.Axis.Type.DATE_TIME);
		//themeRiverChart.getNavigator().getXAxis().setType(org.moxieapps.gwt.highcharts.client.Axis.Type.DATE_TIME);



		// DUMMY, ansonsten wird aber die achse nicht korrekt gezeigt
		int number = topicIntervals.length;

		xCoordChartToIntervalID.clear();
		Point[] points = new Point[number];
		for(int i=0; i<number; i++) {
			points[i] = new Point((getTime(topicIntervals[i][0])+getTime(topicIntervals[i][1]))/2,0);
			points[i].setName("");
		}



		Series theSeries = themeRiverChart.createSeries();
		theSeries.setPoints(points);
		theSeries.setName(" ");



		theSeries.setPlotOptions(new AreaPlotOptions().setShowInLegend(false));
		themeRiverChart.addSeries(theSeries);
		//themeRiverChart.getNavigator().setBaseSeries(0);

		themeRiverChart.setOption("navigator/enabled", true);
		themeRiverChart.setOption("navigator/baseSeries", 0);

		upperPartCanvas.addChild(themeRiverChart);



		final DynamicForm form = new DynamicForm();  
		//form.setWidth100();

		radioGroupItemThemeRiver = new RadioGroupItem();
		radioGroupItemThemeRiver.setShowTitle(false);
		radioGroupItemThemeRiver.setVertical(false);
		radioGroupItemThemeRiver.setValueMap("absolute", "relative&nbsp;topic&nbsp;relevance");
		//radioGroupItemThemeRiver.setShowTitle(false);  

		radioGroupItemThemeRiver.setValue("absolute");
		//radioGroupItemThemeRiver.setWidth(50);
		ChangeHandler handler = new ChangeHandlerThemeRiver();

		radioGroupItemThemeRiver.addChangeHandler(handler);
		form.setFields(radioGroupItemThemeRiver);

		form.setHeight("30px");
		HLayout hl = new HLayout();
		hl.setPadding(2);
		Label l1 = new Label("Display");
		l1.setHeight("29px");
		l1.setAutoWidth();
		hl.addMember(l1);
		hl.addMember(form);
		hl.setAutoWidth();

		upperPartCanvas.addChild(hl);




	}

	class ChangeHandlerThemeRiver implements ChangeHandler{

		@Override
		public void onChange(ChangeEvent event) {			
			if(((String)event.getValue()).equals("absolute")) {
				drawDifferenceInTopicThemeRiver = false;
				themeRiverChart.getYAxis().setAxisTitleText("Relevance");				
			} else {
				drawDifferenceInTopicThemeRiver = true;
				themeRiverChart.getYAxis().setAxisTitleText("Change of Relevance");
			}
			themeRiverChart.setOption("plotOptions/areaspline/stacking", drawDifferenceInTopicThemeRiver? null : "normal");			
			handlerTopicCheckbox.onClick(null);
		}};



		private  void initializeThemeRiverForWords(){

			wordEvolutionChart.removeAllSeries(true);

			wordEvolutionChart
				.setType(Series.Type.AREA_SPLINE)
				.setChartTitleText("Word Evolution")
				.setMarginRight(10)				
				.setOption("plotOptions/areaspline/stacking", drawDifferenceInWordThemeRiver? null : "normal")
				.setOption("plotOptions/areaspline/fillOpacity", drawDifferenceInWordThemeRiver? OVERLAP_OPACITY : STACKED_OPACITY);


			wordEvolutionChart.setToolTip(new ToolTip()  
			.setFormatter(new ToolTipFormatter() {  
				public String format(ToolTipData toolTipData) {  
					return toolTipData.getSeriesName()+": "+round(toolTipData.getYAsDouble(),4);  
				}  
			}));

			//chart.setHeight("20em");
			//chart.setWidth("60em");
			wordEvolutionChart.setWidth100();
			wordEvolutionChart.setHeight100();
			wordEvolutionChart.getYAxis().setAxisTitleText("Relevance");
			//chart.setWidth("50em");
			wordEvolutionChart.setCredits( new Credits().setText(""));
			wordEvolutionChart.setOption("plotOptions/areaspline/trackByArea", true); // ACHTUNG: benötigt neue Version von Highcharts

			LegendLabelsFormatter legendLabelsFormatter = new LegendLabelsFormatter() {
				public String format(LegendLabelsData legendLabelsData) {
					return legendLabelsData.getSeriesName();
				}
			};
			Legend legend = new Legend();
			legend.setLabelsFormatter(legendLabelsFormatter);

			wordEvolutionChart.setLegend(legend);
			// ACHTUNG: x-axe wurde in anderer funktion gesetzt
			//handlerWortFromThemeRiver= new ClickHandlerWortCheckbox();
			wordEvolutionChart.getXAxis().setType(org.moxieapps.gwt.highcharts.client.Axis.Type.DATE_TIME);
		}

		private void initializeDocumentBrowserPanel(Tab documentBrowserTab) {


			VLayout lowerPart = new VLayout();
			lowerPart.setWidth100();
			lowerPart.setHeight100();
			currentTopicLabel = new Label("<b>Select a topic in the Topic Evolution above</b>");
			currentTopicLabel.setHeight(15);
			lowerPart.addMember(currentTopicLabel);

			documentBrowserCanvas.setOverflow(Overflow.VISIBLE);



			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			documentBrowserCanvas.setShowEdges(false);
			documentBrowserCanvas.setDragAppearance(DragAppearance.TARGET);  
			documentBrowserCanvas.setOverflow(Overflow.HIDDEN);  
			documentBrowserCanvas.setCanDragResize(true);  
			documentBrowserCanvas.setResizeFrom("L", "R");  
			documentBrowserCanvas.setLayoutMargin(10);  
			documentBrowserCanvas.setMembersMargin(10);  


			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			documentBrowserCanvas.setHeight100();
			documentBrowserCanvas.setWidth100();
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			lowerPart.addMember(documentBrowserCanvas);


			documentsOfTopicCanvas = new Canvas();
			documentsOfTopicCanvas.setWidth100();
			documentsOfTopicCanvas.setHeight100();



			SectionStackSection section2 = new SectionStackSection("");   
			section2.setExpanded(true);
			mainCanvas = new Canvas();
			mainCanvas.setWidth100();

			/**
			 * for similar Documents to selected Document x that are older than x  
			 */
			leftpartCanvas = new Canvas();
			leftpartCanvas.setWidth100();

			/**
			 * for similar Documents to selected Document x that are newer than selected x
			 */
			rightpartCanvas= new Canvas();
			rightpartCanvas.setWidth100();

			Label l2 = new Label("hier das Fenster wo ausgewähltes Dokument die die ähnlichen zu den verschiednen Zeiten unten");
			l2.setWidth100();
			l2.setHeight100();


			documentBrowserCanvas.addMember(documentsOfTopicCanvas);


			pieChartCanvas = new Canvas();
			pieChartCanvas.setOverflow(Overflow.VISIBLE);
			documentBrowserCanvas.addMember(pieChartCanvas);
			pieChartCanvas.setHeight100();
			pieChartCanvas.setWidth(200);


			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			documentBrowserTab.setPane(lowerPart);


			/////
			documentGrid = new ListGrid() {  
				/*public DataSource getRelatedDataSource(ListGridRecord record) {  
                return ItemSupplyXmlDS.getInstance();  
            } */ 

				@Override  
				protected Canvas createRecordComponent(final ListGridRecord record, Integer colNum) {  

					String fieldName = this.getFieldName(colNum);  

					//          com.google.gwt.user.client.Window.alert(colNum+"");


					if (fieldName.equals("buttonField")) {  
						IButton DocumentsText = new IButton( );
						//record.getAttribute("docID")
						DocumentsText.setIcon("icon_documents.gif");
						DocumentsText.setTooltip("Click to see the content of this document");
						//DocumentsText.setAutoFit(true);  
						DocumentsText.setWidth(30);              

						ClickHandlerShowDocument doctext=new ClickHandlerShowDocument();
						doctext.setData(Integer.parseInt(record.getAttribute("docID")),theDocumentService);
						DocumentsText.addClickHandler(doctext);

						return DocumentsText;  
					} 
					/////////////////////////////////////////NEWS///////////////////////////////////////////
					else if (fieldName.equals("buttonField2")) {  
						TransferImgButton NewDocs = new TransferImgButton(TransferImgButton.RIGHT_ALL);
						NewDocs.setTooltip("Click to find similar documents to the selected one (will open the Document Explorer window)");
						ClickHandlerOpenDocumentExplorer handlerSimilarDocs=new ClickHandlerOpenDocumentExplorer();
						//handlerSimilarDocs.setID(Integer.parseInt(record.getAttribute("docID")), DocTopicTime,record.getAttribute("titel"));
						handlerSimilarDocs.setInfo((DocumentInfo) record.getAttributeAsObject("DOCINFOS"));
						NewDocs.addClickHandler(handlerSimilarDocs);

						return NewDocs;  
					} else if (fieldName.equals("pie")) {  

						IButton pies = new IButton( );
						//record.getAttribute("docID")
						pies.setIcon("chart-pie.png");
						pies.setTooltip("Click to see the distribution of topics in this document");
						//DocumentsText.setAutoFit(true);  
						pies.setWidth(30);              


						ClickHandlerShowPieChart pieChart=new ClickHandlerShowPieChart();
						pieChart.setInfo((DocumentInfo)(record.getAttributeAsObject("DOCINFOS")));
						pies.addClickHandler(pieChart);

						return pies;
						//////////////////////////////////////////////////////////////////////////////////////
					} 


					else {  
						return null;  
					}  

				}  
			};  

			documentGrid.setWidth100();  
			documentGrid.setHeight100();  
			documentGrid.setShowAllRecords(true);
			documentGrid.setDrawAheadRatio(6);  
			documentGrid.setSelectionType(SelectionStyle.SINGLE);
			documentGrid.setCanExpandRecords(false); 
			documentGrid.setShowRecordComponents(true);
			documentGrid.setShowRecordComponentsByCell(true);
			documentGrid.setRecordComponentPosition(EmbeddedPosition.WITHIN);
			documentGrid.setWrapCells(true);
			documentGrid.setFixedRecordHeights(false); 


			ListGridField capitalField = new ListGridField("title", "Title");
			//capitalField.setAutoFitWidthApproach(AutoFitWidthApproach.BOTH);
			ListGridField dateField = new ListGridField("date", "Date",65);
			//dateField.setAutoFitWidth(true);
			ListGridField positionField = new ListGridField("position", "#.",20);
			
			ListGridField weightField = new ListGridField("weight", "Weight",50);
			weightField.setAlign(Alignment.CENTER);
			weightField.setShowHover(true);			
			weightField.setHoverCustomizer(new HoverCustomizer() {
		        public String hoverHTML(Object value, ListGridRecord record, int rowNum, int colNum) {		            
		            return "The selected topic accounts for " + record.getAttribute("weight") 
		              + " of the topic distribution of this document at the selected point in time";
		        }
		    });
			
			//positionField.setAutoFitWidth(true);
			ListGridField buttonField = new ListGridField("buttonField", "Content",50);
			ListGridField buttonField2= new ListGridField("buttonField2","Similar Docs",70);
			ListGridField pie= new ListGridField("pie","Topic Pie",55);
			pie.setAlign(Alignment.CENTER);
			buttonField.setAlign(Alignment.CENTER);
			buttonField2.setAlign(Alignment.CENTER);
			
			documentGrid.setFields(positionField,weightField,capitalField,dateField,buttonField,pie,buttonField2);

			this.documentsOfTopicCanvas.addChild(documentGrid);


			contentOfDocument.setOverflow(Overflow.AUTO);  
			contentOfDocument.setPadding(10);  
			contentOfDocument.setWidth100();
			contentOfDocument.setHeight100();


			contentOfDocument.setTitle("Content of selected document");
			contentOfDocument.setContents("no document selected");


			leftPartCanvas.setHeight100();
			TopicOverviewPanel.setOverflow(Overflow.AUTO);
			TopicOverviewPanel.setAlign(Alignment.LEFT);
			TopicOverviewPanel.setLayoutAlign(Alignment.LEFT);
			TopicOverviewPanel.setMembersMargin(0);
			TopicOverviewPanel.setPadding(4);

			TopicOverviewPanel.setWidth100();
			TopicOverviewPanel.setHeight100();
			TopicOverviewPanel.reflow();

			leftPartCanvas.addChild(TopicOverviewPanel);

			initializeTopicSearch();
			initializeRankingButtons();


			VLayout line = new VLayout();
			line.setWidth100();
			//line.setHeight("1px");
			line.setMargin(0);
			line.setBorder("1px dotted gray");
			Label label = new Label("Sort topics based on...");
			label.setHeight("15px");
			line.addMember(label);
			line.addMember(hlayout);
			line.addMember(searchAreaLayout);
			TopicOverviewPanel.addMember(line);


			HTML h = new HTML("<b>Topics in this data set:</b>");
			h.setHeight("18px");
			TopicOverviewPanel.addMember(h);


			HLayout hl1 = new HLayout();
			hl1.setHeight("25px");
			hl1.setWidth100();
			selectAllCheckbox= new CheckBox(); 
			selectAllCheckbox.setHeight("20px");
			selectAllCheckbox.setWidth("20px");
			LayoutSpacer space = new LayoutSpacer();
			space.setWidth("15px");
			hl1.addMember(space);
			hl1.addMember(selectAllCheckbox);
			Label text1 = new Label("Select all");

			text1.setWrap(true);
			text1.setWidth100();
			hl1.addMember(text1);

			stack = new VStack();
			TopicOverviewPanel.addMember(stack);
			TopicOverviewPanel.addMember(hl1);





		}

		public static String formatDateTime(String input) {

			// so werden jahre und monate ausgegeben
			// besser wäre hier eine allgemeinere methode
			if (input == null) return "1970";
			String[] parts = input.split(" ")[0].split("-");
			if(parts.length==1) return parts[0];
			if(parts.length==2) return parts[1]+"."+parts[0];
			return parts[2]+"."+parts[1]+"."+parts[0];
		}


		private void initializeWordEvolutionPanel(Tab tab2) {

			VLayout lowerPart = new VLayout();
			lowerPart.setWidth100();
			lowerPart.setHeight100();
			currentTopicLabel2 = new Label("<b>Select a topic in the Topic Evolution above</b>");
			currentTopicLabel2.setHeight(15);
			currentTopicLabel2.setContents("");
			lowerPart.addMember(currentTopicLabel2);

			lowerPart.addMember(splitLayout);

			leftStack.setShowResizeBar(true);
			leftStack.setWidth("20%");

			Canvas right = new Canvas();
			splitLayout.addMember(leftStack);
			splitLayout.addMember(right);
			tab2.setPane(lowerPart);

			// nun left mache
			leftStack.setMembersMargin(0);
			HTML h = new HTML("<b>Select words to display:</b>");
			h.setHeight("20px");
			leftStack.addMember(h);

			// und right
			initializeThemeRiverForWords();
			right.addChild(wordEvolutionChart);


			final DynamicForm form = new DynamicForm();  
			//form.setWidth100();
			RadioGroupItem radioGroupItem = new RadioGroupItem();  
			radioGroupItem.setShowTitle(false);  
			radioGroupItem.setValueMap("absolute", "relative&nbsp;word&nbsp;relevance"); 
			radioGroupItem.setValue("relative&nbsp;word&nbsp;relevance");
			radioGroupItem.setVertical(false);
			ChangeHandler handler = new ChangeHandlerWordEvolution();

			radioGroupItem.addChangeHandler(handler);
			form.setFields(radioGroupItem);  


			form.setHeight("30px");
			HLayout hl = new HLayout();
			hl.setPadding(2);
			Label l1 = new Label("Display");
			l1.setHeight("29px");
			l1.setAutoWidth();
			hl.addMember(l1);
			hl.addMember(form);
			hl.setAutoWidth();

			right.addChild(hl);





		}

		class ChangeHandlerWordEvolution implements ChangeHandler {

			@Override
			public void onChange(ChangeEvent event) {
				if(((String)event.getValue()).equals("absolute")) {
					drawDifferenceInWordThemeRiver = false;
					wordEvolutionChart.getYAxis().setAxisTitleText("Relevance");										
				} else {
					drawDifferenceInWordThemeRiver = true;
					wordEvolutionChart.getYAxis().setAxisTitleText("Change of Relevance");
					
				}				
				wordEvolutionChart.setOption("plotOptions/areaspline/stacking", drawDifferenceInWordThemeRiver? null : "normal");				
				handlerWortCheckbox.onClick(null);
			}};

			private void initializeTopicSearch(){


				textitem.setShowTitle(false);
				textitem.setTooltip("Filter and sort the topic list using the entered keyword");
				//textitem.
				//form.setFields(textitem);



				searchAreaLayout= new HLayout();

				searchAreaLayout.setAlign(Alignment.LEFT);
				searchAreaLayout.setLayoutAlign(Alignment.LEFT);
				searchAreaLayout.setLayoutAlign(VerticalAlignment.TOP);
				searchAreaLayout.setAlign(VerticalAlignment.CENTER);

				searchAreaLayout.setAutoHeight();
				searchAreaLayout.setAutoWidth();
				//searchArea.setEdgeBackgroundColor("#E1E1E1");
				topicSearchForm.clearValues();

				HTML h = new HTML("Keywords:");
				h.setWidth("52px");
				h.setHeight("10px");


				searchAreaLayout.addMember(h);
				searchAreaLayout.addMember(topicSearchForm);
				//searchArea.addMember(findButton);
				searchAreaLayout.setMargin(10);

				//TopicOverviewPanel.addMember(searchArea);
				topicSearchForm.clearValues();
				//TopicOverviewPanel.addMember(form);
				//com.smartgwt.client.widgets.events.ClickHandler Topicsearch = new ClickHandlerTopicSearch();
				//findButton.addClickHandler(Topicsearch);

			}


			private void initializeRankingButtons(){


				HTML h = new HTML("Relevance:");
				h.setWidth("52px");
				h.setHeight("10px");
				hlayout = new HLayout();
				hlayout.setHeight(23);
				hlayout.setAutoWidth();
				//textfield.setBorder("2px solid blue");
				hlayout.setAlign(Alignment.LEFT);
				hlayout.setLayoutAlign(Alignment.LEFT);
				hlayout.setLayoutAlign(VerticalAlignment.TOP);
				hlayout.setAlign(VerticalAlignment.CENTER);


				//box.addMember(textfield);
				hlayout.addMember(h);
				hlayout.setMargin(10);
				hlayout.setMembersMargin(5);
				//   box.setBorder( "2px solid blue");

				varianceButton.setHeight(23);
				varianceButton.setWidth(23);
				varianceButton.setShowRollOver(false);  
				varianceButton.setIcon("iconSigma.png");
				varianceButton.setTooltip("Click to sort by variance in topic relevance");
				varianceButton.setActionType(SelectionType.RADIO);

				//toolStrip.addMember(varianz);

				sinkingButton.setHeight(23);
				sinkingButton.setWidth(23);  
				sinkingButton.setShowRollOver(false);  
				sinkingButton.setIcon("ic_chart_down.gif");  
				sinkingButton.setTooltip("Click to sort by falling topic relevance ");
				sinkingButton.setActionType(SelectionType.RADIO);
				hlayout.addMember(sinkingButton);

				risingButton.setHeight(23);
				risingButton.setWidth(23);  
				risingButton.setShowRollOver(false);  
				risingButton.setIcon("ic_chart_up.gif");  
				risingButton.setActionType(SelectionType.RADIO);
				risingButton.setTooltip("Click to sort by rising topic relevance ");
				hlayout.addMember(risingButton);

				risingDecayButton.setHeight(23);
				risingDecayButton.setWidth(23);  
				risingDecayButton.setShowRollOver(false);  
				risingDecayButton.setIcon("ic_chartW.gif");  
				risingDecayButton.setActionType(SelectionType.RADIO);
				risingDecayButton.setTooltip("Click to sort by rising topic relevance with decay");
				hlayout.addMember(risingDecayButton);


				//toolStrip.addMember(rising);

				meanButton.setHeight(23);
				meanButton.setWidth(23);  
				meanButton.setShowRollOver(false);  
				meanButton.setIcon("mue-symbol.gif");  
				meanButton.setActionType(SelectionType.RADIO);
				meanButton.setTooltip("Click to sort by average topic relevance ");
				hlayout.addMember(meanButton);
				hlayout.addMember(varianceButton);


				//searchArea.addMember(back);
				// TopicOverviewPanel.addMember(box);

				com.smartgwt.client.widgets.events.ClickHandler sorting = new ClickHandlerRanking();
				meanButton.addClickHandler(sorting);
				varianceButton.addClickHandler(sorting);
				sinkingButton.addClickHandler(sorting);
				risingButton.addClickHandler(sorting);
				risingDecayButton.addClickHandler(sorting);

			}
			//////////////////////////// erster sendNameToServer um topicsliste zu holen!!
			public void retrieveAllTopicsFromServer() {


				/*
				 * get Topic-TimeIntervals by sql query on server side and save the result in the golabel 
				 * defined array , topicInterval. Timevalues in the array will be used by arrowsbuttons 
				 * for the ListGrid.
				 */

				loadingWindow.start("Initialize Topic Explorer<br>",2);

				for(Canvas child : pieChartCanvas.getChildren()) {
					pieChartCanvas.removeChild(child);
				}
				this.handlerTopicCheckbox.selectedTopics.clear();



				topicService. getTimeintervals(
						new AsyncCallback<String[][]>() {
							public void onFailure(Throwable caught) {
								// Show the RPC error message to the user
								dialogBox
								.setText("Remote Procedure Call - Failure");
								serverResponseLabel
								.addStyleName("serverResponseLabelError");
								serverResponseLabel.setHTML(SERVER_ERROR);
								dialogBox.center();
								closeButton.setFocus(true);
							}

							@Override
							public void onSuccess(String [][] topictime) {
								topicIntervals=topictime;

								documentExplorerWindow.setIntervals(topicIntervals);

								// chart bzgl. dem ThemeRiver-Evolution 
								initializeThemeRiver();
								leftStack.removeMembers(leftStack.getMembers());
								currentTopicLabel2.setContents("");
								currentTopicLabel.setContents("");
								
								for(int i = documentGrid.getTotalRows() - 1; i >= 0; i--)
									documentGrid.removeData(documentGrid.getRecord(i));
								
								initializeThemeRiverForWords();

								//a.addNorth(chart, 300);
								loadingWindow.stop();



							}


						});



				// First, we validate the input.	
				//errorLabel.setText("");
				// Then, we send the input to the server.
				//	textToServerLabel.setText(textToServerTopicID);
				serverResponseLabel.setText("");
				topicService.getTopicList(
						new AsyncCallback<TopicLabels>() {
							public void onFailure(Throwable caught) {
								// Show the RPC error message to the user
								dialogBox
								.setText("Remote Procedure Call - Failure");
								serverResponseLabel
								.addStyleName("serverResponseLabelError");
								serverResponseLabel.setHTML(SERVER_ERROR);
								dialogBox.center();
								closeButton.setFocus(true);
							}

							@Override
							public void onSuccess(TopicLabels result) {

								stack.removeMembers(stack.getMembers());
								stack.setMembersMargin(0);
								generateTopicCheckboxes(result);
								loadingWindow.stop();
							}


						});

			}

			private long getTime(String date) {  
				return dateTimeFormat.parse(date.split(" ")[0]).getTime();  
			}  
			final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("yyyy-MM-dd");

			private VStack stack = new VStack();

			private CheckBox selectAllCheckbox; 

			public static String formatTopicLabel(String[] words) {
				String s = "";
				for (int j=0; j<words.length; j++){
					if(j > 0) s += WORDSEPARATOR;										
					s += Character.toUpperCase(words[j].charAt(0)) + words[j].substring(1);
				}
				return s;
			}

			public CheckBox[] generateTopicCheckboxes(TopicLabels result){
				CheckBox [] Words = new CheckBox [result.topicIDs.length];
				Integer [] TopicIds = new Integer[result.topicIDs.length];
				colorLabels = new Label[result.topicIDs.length];
				topicColors = new Color[result.topicIDs.length];
				topicColorsString = new String[result.topicIDs.length];


				numberTopics = TopicIds.length;

				themeRiverDataForTopic = new ThemeRiverData[numberTopics];

				//schreibe den Wert von Wordid und Topic der gewählten Wörrter in den Memebervariabe aus themerive 
				handlerTopicCheckbox.topicCheckboxes=Words;
				handlerTopicCheckbox.topicIDsCheckboxes=TopicIds;


				for(int i=0;i<result.topicIDs.length; i++){
					Words[i]=new CheckBox();

					Words[i].setTitle("Check the topic to analyse the topic evolution");
					String s = formatTopicLabel(result.words[i]);
					
					TopicIds[i]= result.topicIDs[i];	
					Words[i].setText("");
					Words[i].setWidth("20px");


					colorLabels[result.topicIDs[i]] = new Label("&nbsp;");
					colorLabels[result.topicIDs[i]].setTooltip("Click to change color");
					colorLabels[result.topicIDs[i]].setBackgroundColor(getColorString(result.topicIDs[i]));
					topicColors[result.topicIDs[i]] = getColor(result.topicIDs[i]);
					topicColorsString[result.topicIDs[i]] = getColorString(result.topicIDs[i]);
					colorLabels[result.topicIDs[i]].setHeight("15px");
					colorLabels[result.topicIDs[i]].setWidth("15px");

					HLayout pad = new HLayout();
					pad.setHeight("4px");
					stack.addMember(pad);
					
					HLayout hl = new HLayout();
					hl.setHeight("25px");
					hl.setWidth100();
					hl.setAlign(VerticalAlignment.CENTER);
					/*forSelecting.setHeight("20px");
			forSelecting.setWidth("20px");
			TopicOverviewPanel.addMember(forSelecting);*/
					stack.addMember(hl);

					hl.addMember(colorLabels[result.topicIDs[i]]);

					topicIDtoName.put(result.topicIDs[i],s); // jeder topciID werden ihre 3 wichtigstren öwrter zugeirdnet
					Words[i].setHeight("20px");

					hl.addMember(Words[i]);

					Label text = new Label(s);
					text.setWrap(true);
					text.setWidth100();
					hl.addMember(text);


					Words[i].addClickHandler(handlerTopicCheckbox);
					ClickHandlerSelectAll handler1 = new ClickHandlerSelectAll();
					selectAllCheckbox.addClickHandler(handler1);
					ClickHandlerColorPick handler = new ClickHandlerColorPick();
					handler.topicID = result.topicIDs[i];
					colorLabels[result.topicIDs[i]].addClickHandler(handler);



				}




				return Words;

			}


			class ClickHandlerRanking implements com.smartgwt.client.widgets.events.ClickHandler, FormItemClickHandler{

				@Override
				public void onClick(com.smartgwt.client.widgets.events.ClickEvent event) {

					if(event.getSource()==varianceButton){
						sendButtontypToServer(1);
					}
					else if (event.getSource()==sinkingButton){
						sendButtontypToServer(2);
					}
					else if (event.getSource()==risingButton){
						sendButtontypToServer(3);
					}
					else if (event.getSource()==risingDecayButton){
						sendButtontypToServer(5);
					}
					else if (event.getSource()==meanButton){
						sendButtontypToServer(4);
					}
				}	

				private void sendButtontypToServer(int buttonTyp){

					topicService.topicRanking(buttonTyp,
							new AsyncCallback< Integer []>() {
								public void onFailure(Throwable caught) {
									// Show the RPC error message to the user
									dialogBox
									.setText("Remote Procedure Call - Failure");
									serverResponseLabel
									.addStyleName("serverResponseLabelError");
									serverResponseLabel.setHTML(SERVER_ERROR);
									dialogBox.center();
									closeButton.setFocus(true);
								}
								@Override
								public void onSuccess(Integer [] TopicIds) {
									generateNewTopicCheckboxes(TopicIds);


								}

							});

				}

				@Override
				public void onFormItemClick(FormItemIconClickEvent event) {
					textitem.clearValue();	
					Integer [] Topics= new Integer[numberTopics];
					for (int i=0;i<numberTopics; i++){
						Topics[i]=i;
					}
					generateNewTopicCheckboxes(Topics);
					//PROBLEM: Es geht in die new liste wo backbutton eingefuegt wurde. in der urspr. liste sollte aber kein back liste vorhanden sein.
				}



			}
			class ClickHandlerSelectAll implements ClickHandler{
				public void onClick(ClickEvent event) {

					// das war hier nötig, da das Event immer mehrfach ausgelöst hat!!
					if(!selectAllCheckbox.isEnabled()) return;
					selectAllCheckbox.setEnabled(false);

					if (selectAllCheckbox.getValue()){
						for(CheckBox checkB : handlerTopicCheckbox.topicCheckboxes){
							checkB.setValue(true);
						}

					}
					else {
						for(CheckBox checkB : handlerTopicCheckbox.topicCheckboxes){
							checkB.setValue(false);
						}
					}
					handlerTopicCheckbox.onClick(new ClickEvent(){});	
				}
			}

			class ClickHandlerColorPick implements com.smartgwt.client.widgets.events.ClickHandler{

				public int topicID;

				@Override
				public void onClick(
						com.smartgwt.client.widgets.events.ClickEvent event) {



					ColorPicker pick = new ColorPicker();
					ColorSelectedHandler handler2 = new ColorSelectedHandler(){

						@Override
						public void onColorSelected(ColorSelectedEvent event) {
							colorLabels[topicID].setBackgroundColor(event.getColor());
							topicColors[topicID] = new Color(event.getColor());
							topicColorsString[topicID] = event.getColor();

							// geht auch noch besser
							handlerTopicCheckbox.onClick(null);
							redrawPieChart();
							documentExplorerWindow.redrawPie();

						}};
						pick.addColorSelectedHandler(handler2);
						pick.show();

				}

			}

			//Erzeugen der erneuten Tpoicliste bei der suche nach einem Wort 

			public CheckBox[] generateNewTopicCheckboxes(Integer []TopicIds){

				//GWT.log("Lenght of Topics:"+TopicIds.length);
				CheckBox [] Words = new CheckBox [TopicIds.length];
				Integer [] TopicIDs = new Integer[TopicIds.length];
				//schreibe den Wert von Wordid und Topic der gewählten Wörrter in den Memebervariabe aus themerive

				handlerTopicCheckbox.topicCheckboxes=Words;
				handlerTopicCheckbox.topicIDsCheckboxes=TopicIDs;
				stack.removeMembers(stack.getMembers());

				for(int i=0;i<TopicIds.length; i++){
					HLayout pad = new HLayout();
					pad.setHeight("4px");
					stack.addMember(pad);
					
					HLayout horizont= new HLayout();
					horizont.setHeight("25px");
					horizont.setWidth100();
					stack.addMember(horizont);
					Words[i]=new CheckBox();
					Words[i].setWidth("20px");
					String s=topicIDtoName.get(TopicIds[i]);// jeder topciID bekommt ihre 3 wichtigstren wörter
					TopicIDs[i]= TopicIds[i];	
					horizont.addMember(colorLabels[TopicIDs[i]]);
					Words[i].setHeight("20px");
					if(handlerTopicCheckbox.selectedTopics.contains(TopicIds[i])) {
						Words[i].setValue(true);
					}

					//Words[i].setWidth(width)
					horizont.addMember(Words[i]);

					Label text = new Label(s);
					text.setWrap(true);
					text.setWidth100();
					horizont.addMember(text);

					Words[i].addClickHandler(handlerTopicCheckbox);

					//TopicOverviewPanel.addMember(horizont);

				}


				return Words;

			}


			public Color getColor(int topicid) {
				Integer[] rgb = hsvToRgb((topicid+1.0)/(numberTopics+0.1),0.9,0.95);
				return new Color(rgb[0],rgb[1],rgb[2],1);
			}

			public String getColorString(int topicid) {
				Integer[] rgb = hsvToRgb((topicid+1.0)/(numberTopics+0.1),0.9,0.95);

				String out = Integer.toHexString(rgb[2]);
				if(out.length()<2) out="0"+out;
				out = Integer.toHexString(rgb[1])+out;
				if(out.length()<4) out="0"+out;
				out = Integer.toHexString(rgb[0])+out;
				if(out.length()<6) out="0"+out;

				return "#"+out;
			}

			public Integer[] hsvToRgb(double hue, double saturation, double value) {

				double r=0, g=0, b=0;

				int h = (int)(hue * 6);
				double f = hue * 6 - h;
				double p = value * (1 - saturation);
				double q = value * (1 - f * saturation);
				double t = value * (1 - (1 - f) * saturation);

				if (h == 0) {
					r = value;
					g = t;
					b = p;
				} else if (h == 1) {
					r = q;
					g = value;
					b = p;
				} else if (h == 2) {
					r = p;
					g = value;
					b = t;
				} else if (h == 3) {
					r = p;
					g = q;
					b = value;
				} else if (h == 4) {
					r = t;
					g = p;
					b = value;
				} else if (h == 5){
					r = value;
					g = p;
					b = q;
				}

				Integer[] res = new Integer[3];
				res[0]= (int)(256*r);
				res[1]= (int)(256*g);
				res[2]= (int)(256*b);

				return res;

			}


			private CheckBox[] wordButtonsForTopic(WordData WordsInputs) {

				String  [] Words=new String [WordsInputs.words.length];
				Integer [] WordsIDs = new Integer[WordsInputs.wordIds.length];

				CheckBox[] results = new CheckBox[Words.length];
				handlerWortCheckbox.wordsCheckboxes=results;
				handlerWortCheckbox.wordIdsCheckboxes=WordsIDs;


				leftStack.removeMembers(leftStack.getMembers());
				HTML h = new HTML("<b>Select words to display:</b>");
				h.setHeight("20px");
				leftStack.addMember(h);

				for(int i=0;i<results.length; i++){
					results[i]=new CheckBox();
					results[i].setText(WordsInputs.words[i]);
					results[i].setHeight("20px");

					if(i<3) {
						results[i].setValue(true);
					}

					wordIDtoName.put(WordsInputs.wordIds[i],WordsInputs.words[i]);

					leftStack.addMember(results[i]);
					WordsIDs[i]=WordsInputs.wordIds[i];
					results[i].addClickHandler(handlerWortCheckbox);

				}
				return results;


			}

			class ClickHandlerWortCheckbox implements ClickHandler{
				ArrayList<Integer> selectedWords= new ArrayList<Integer>();
				CheckBox[]wordsCheckboxes;
				Integer[] wordIdsCheckboxes;



				//Funtion für Y-Axis
				private void createYaxis(Double [] res,String s){



					Point[] points = new Point[res.length];

					double minVal = Double.MAX_VALUE;
					int minIndex = -1;
					double maxVal = -Double.MAX_VALUE;
					int maxIndex = -1;

					for(int i=0; i<res.length; i++) {
					if (null != res[i]) 
					{
						if(res[i].isNaN()) {
							points[i]= new Point((getTime(topicIntervals[i][0])+getTime(topicIntervals[i][1]))/2,0);
						} else {

							if(!drawDifferenceInWordThemeRiver) {
								points[i]= new Point((getTime(topicIntervals[i][0])+getTime(topicIntervals[i][1]))/2,res[i]);

								if(i>0) {
									if(res[i]>res[i-1]) {
										points[i].setMarker(new Marker().setSymbol(Symbol.TRIANGLE));
									} else if(res[i]<res[i-1]) {
										points[i].setMarker(new Marker().setSymbol(Symbol.TRIANGLE_DOWN));
									}
								}


							} else {
								if(i==0) {
									// MD: check this calcs "absolute change". might want to show "relative change",
									// e.g. res[i] = 0.12, res[i-1] = 0.08, then here diff = +0.04, relative would be +50%
									points[i]= new Point((getTime(topicIntervals[i][0])+getTime(topicIntervals[i][1]))/2,0);		
								} else {
									points[i]= new Point((getTime(topicIntervals[i][0])+getTime(topicIntervals[i][1]))/2,res[i]-res[i-1]);

									if(res[i]>res[i-1]) {
										points[i].setMarker(new Marker().setSymbol(Symbol.TRIANGLE));
									} else if(res[i]<res[i-1]) {
										points[i].setMarker(new Marker().setSymbol(Symbol.TRIANGLE_DOWN));
									}
								}
							}
						}
						points[i].setName(formatDateTime(topicIntervals[i][0])+" - "+formatDateTime(topicIntervals[i][1])+"<br>"+s);


						if(points[i].getY().doubleValue()>maxVal) {
							maxVal = points[i].getY().doubleValue();
							maxIndex = i;
						}
						if(points[i].getY().doubleValue()<minVal) {
							minVal = points[i].getY().doubleValue();
							minIndex = i;
						}
					}}

					Marker marker = new Marker();
					//marker.setOption("symbol", "url(./images/magnifier.png)");
					//marker.setFillColor("black");
					marker.setSymbol(Symbol.TRIANGLE_DOWN);
					marker.setLineWidth(2);
					marker.setLineColor("black");
					points[minIndex].setMarker(marker);

					Marker marker2 = new Marker();
					//marker2.setOption("symbol", "url(./images/database.png)");
					//marker2.setFillColor("");
					marker2.setSymbol(Symbol.TRIANGLE);
					marker2.setLineWidth(2);
					marker2.setLineColor("black");
					points[maxIndex].setMarker(marker2);


					Series theSeries = wordEvolutionChart.createSeries();
					theSeries.setPoints(points);
					theSeries.setName(s);

					theSeries.setPlotOptions(new AreaPlotOptions()
						.setMarker(new Marker().setSymbol(Symbol.CIRCLE))
						.setStacking(drawDifferenceInWordThemeRiver? null : PlotOptions.Stacking.NORMAL));


					wordEvolutionChart.addSeries(theSeries, true, false);	


				}
				//	public ForThemeRiver forthemeriver;
				// sending the checkboxes checked value to the server
				private void sendSelectedWordsToServer(Integer [] WordIdsCheck) {

					//boolean x= true;
					// First, we validate the input.
					//errorLabel.setText("");

					if(WordIdsCheck.length>0) {
						//loadingWindow.start("Loading Words from server<br>",1);
						//loadingWindow.stop();
						
					}
					
					// Then, we send the input to the server.
		 			//	textToServerLabel.setText(textToServerTopicID);
					serverResponseLabel.setText("");
					
					wordService.getWordEvolution(WordIdsCheck,lastClickedTopic,
							new AsyncCallback<WordEvolutionData>() {
						public void onFailure(Throwable caught) {
							// Show the RPC error message to the user
							dialogBox
							.setText("Remote Procedure Call - Failure");
							serverResponseLabel
							.addStyleName("serverResponseLabelError");
							serverResponseLabel.setHTML(SERVER_ERROR);
							dialogBox.center();
							closeButton.setFocus(true);
						}
						@Override
						public void onSuccess(WordEvolutionData result) {
							int i=treatResult(result);
							System.out.println(result.wordIDs.length+""+i);
							//com.google.gwt.user.client.Window.alert("success");
							//loadingWindow.stop();
//							if(result.wordIDs.length>0) {
//								// an alert box is popped up when there is no word returned
//								System.out.println("inside close loading");
//								//loadingWindow.stop();
//							}
						}

					});
				}



				public int treatResult(WordEvolutionData result){
					//GWT.log("Its test:"+result.TopicIDs.length);
					//		int rows = result.TopicIDs.length;
					//		int columns = result.TimeIDs.length;

					/*String[] categories = new String[result.intervalStartDate.length];
					for (int i=0; i<result.intervalStartDate.length;i++) 
						//categories[i] = "Time"+result.TimeIDs[i]+ " is "+result.TopicInterval[i];
						categories[i] = formatDateTime(result.intervalStartDate[i]);*/
					//themeRiverChart.getXAxis().setCategories(categories);

					//wordEvolutionChart.getXAxis().setCategories(categories); // x-axe beim wordchart ist gleich!
				
					int nrselectedWords = result.wordIDs.length;
					if(nrselectedWords == 0)
					{
						wordEvolutionChart.removeAllSeries(true);
						return 0;
					}
					
					// alten plot löschen					
					wordEvolutionChart.removeAllSeries();

					for(int i=0;i <nrselectedWords;i++){//beim Wort 0
						Double [] WordsValue = new Double[result.intervalStartDate.length];
						for (int j=0;j<result.intervalStartDate.length;j++){//zur zeit To...Tx
							WordsValue[j]=result.relevanceAtTime[i][j];
						}
						String wordName = wordIDtoName.get(result.wordIDs[i]);
						createYaxis(WordsValue,wordName);
					}
					return 1;
				}

// handles the checkboxes not the word point click
				public void onClick(ClickEvent event) {

					// Werte von Words[i] werden von TopicsCheck[i](Membervaribel, oben definiert) ausgegeben!
					selectedWords.clear();
					for(int i=0;i<wordsCheckboxes.length; i++ ){
						wordsCheckboxes[i].getValue();
						//GWT.log("WordsCheck[i]:"+WordsCheck[i].getValue());

						if (wordsCheckboxes[i].getValue()) { 	// checkbox i ist ausgewählt
							selectedWords.add( wordIdsCheckboxes[i]);		// füge die entsprechende topic der liste hinzu
						}


					}
					Integer [] words=new Integer[selectedWords.size()];	
					words = selectedWords.toArray(words);
					/*for(int i=0;i<words.length;i++){
				GWT.log("words:"+words);
			}*/
					sendSelectedWordsToServer(words);

				}

			}


			class ClickHandlerTopicSearch implements FormItemClickHandler, KeyPressHandler{

				private void sendTopicToServer(String string){
					serverResponseLabel.setText("");
					loadingWindow.start("Sort topics based on keywords<br>", 1);
					topicService.topicSearch(string,
							new AsyncCallback< Integer []>() {
								public void onFailure(Throwable caught) {
									// Show the RPC error message to the user
									dialogBox
									.setText("Remote Procedure Call - Failure");
									serverResponseLabel
									.addStyleName("serverResponseLabelError");
									serverResponseLabel.setHTML(SERVER_ERROR);
									dialogBox.center();
									closeButton.setFocus(true);
								}
								@Override
								public void onSuccess(Integer [] TopicIds) {
									generateNewTopicCheckboxes(TopicIds);
									loadingWindow.stop();

								}

							});
				} 


				@Override
				public void onFormItemClick(FormItemIconClickEvent event) {

					if(textitem.getEnteredValue().length()<1) {
						// gebe einfach alle topics aus
						Integer [] Topics= new Integer[numberTopics];
						for (int i=0;i<numberTopics; i++){
							Topics[i]=i;
						}
						generateNewTopicCheckboxes(Topics);
					} else {
						sendTopicToServer(textitem.getEnteredValue());
					}
				}


				@Override
				public void onKeyPress(KeyPressEvent event) {
					if(event.getKeyName().equals("Enter")) {
						onFormItemClick(null);
					}
				}



			}




			class ClickHandlerTopicCheckbox implements ClickHandler {
				//MemeberVaribale von gecheckten Topics aus WholeWord
				ArrayList<Integer> selectedTopics= new ArrayList<Integer>();
				CheckBox[]topicCheckboxes;
				Integer[] topicIDsCheckboxes;



				//Funtion für Y-Axis
				private void createYaxis(Double[]res,String s, String[] wordsAtTimeK){


					double minVal = Double.MAX_VALUE;
					int minIndex = -1;
					double maxVal = -Double.MAX_VALUE;
					int maxIndex = -1;

					xCoordChartToIntervalID.clear();
					Point[] points = new Point[res.length];
					for(int i=0; i<res.length; i++) {

						// wenn ein Wert nicht definiert ist, gebe bislang einfach dann 0 aus
						if(res[i].isNaN()) {
							points[i] = new Point((getTime(topicIntervals[i][0])+getTime(topicIntervals[i][1]))/2,0);
						} else {

							if(!drawDifferenceInTopicThemeRiver) {
								points[i] = new Point((getTime(topicIntervals[i][0])+getTime(topicIntervals[i][1]))/2,res[i]);

								if(i>0) {
									if(res[i]>res[i-1]) {
										points[i].setMarker(new Marker().setSymbol(Symbol.TRIANGLE));
									} else if(res[i]<res[i-1]) {
										points[i].setMarker(new Marker().setSymbol(Symbol.TRIANGLE_DOWN));
									}
								}

							} else {
								if(i==0) {
									points[i] = new Point((getTime(topicIntervals[i][0])+getTime(topicIntervals[i][1]))/2,0);
								} else {
									points[i] = new Point((getTime(topicIntervals[i][0])+getTime(topicIntervals[i][1]))/2,res[i]-res[i-1]);

									if(res[i]>res[i-1]) {
										points[i].setMarker(new Marker().setSymbol(Symbol.TRIANGLE));
									} else if(res[i]<res[i-1]) {
										points[i].setMarker(new Marker().setSymbol(Symbol.TRIANGLE_DOWN));
									}

								}
							}
						}

						if(points[i].getY().doubleValue()>maxVal) {
							maxVal = points[i].getY().doubleValue();
							maxIndex = i;
						}
						if(points[i].getY().doubleValue()<minVal) {
							minVal = points[i].getY().doubleValue();
							minIndex = i;
						}


						points[i].setName(formatDateTime(topicIntervals[i][0])+" - "+formatDateTime(topicIntervals[i][1])+"<br>"+wordsAtTimeK[i]);


						xCoordChartToIntervalID.put((getTime(topicIntervals[i][0])+getTime(topicIntervals[i][1]))/2, i);
					}

					Marker marker = new Marker();
					//marker.setOption("symbol", "url(./images/magnifier.png)");
					//marker.setFillColor("black");
					marker.setSymbol(Symbol.TRIANGLE_DOWN);
					marker.setLineWidth(2);
					marker.setLineColor("black");
					points[minIndex].setMarker(marker);

					Marker marker2 = new Marker();
					//marker2.setOption("symbol", "url(./images/database.png)");
					//marker2.setFillColor("");
					marker2.setSymbol(Symbol.TRIANGLE);
					marker2.setLineWidth(2);
					marker2.setLineColor("black");
					points[maxIndex].setMarker(marker2);



					//themeRiverChart.getXAxis().setExtremes(getTime("2010-08-20"), getTime("2010-11-1"));


					Series theSeries = themeRiverChart.createSeries();
					theSeries.setPoints(points);
					theSeries.setName(s);

					Integer topicid = Integer.parseInt(s);
					Color c = topicColors[topicid];
					String cs = topicColorsString[topicid];

					theSeries.setPlotOptions(new AreaPlotOptions()
						//.setFillColor(cs)
						.setZIndex(1)
						.setMarker(new Marker().setSymbol(Symbol.CIRCLE))
						.setColor(cs)
						.setShowInLegend(false)
						.setStacking(drawDifferenceInTopicThemeRiver? null : PlotOptions.Stacking.NORMAL)
						.setFillOpacity(drawDifferenceInTopicThemeRiver? OVERLAP_OPACITY : STACKED_OPACITY));
					
					themeRiverChart.addSeries(theSeries);
					
					themeRiverChart.setOption("navigator/enabled", true);
					themeRiverChart.setOption("navigator/baseSeries", 0);


				}



				private void sendSelectedTopicsToServer(Integer [] Topics) {

					serverResponseLabel.setText("");
					topicService.getTopicCurrent(Topics,
							new AsyncCallback<ThemeRiverData[]>() {
								public void onFailure(Throwable caught) {
									// Show the RPC error message to the user
									dialogBox
									.setText("Remote Procedure Call - Failure");
									serverResponseLabel
									.addStyleName("serverResponseLabelError");
									serverResponseLabel.setHTML(SERVER_ERROR);
									dialogBox.center();
									closeButton.setFocus(true);
								}
								@Override
								public void onSuccess(ThemeRiverData [] result) {
									treatResult(result);
									for(int i=0;i<topicCheckboxes.length; i++ ){
										topicCheckboxes[i].setEnabled(true);
									}
									radioGroupItemThemeRiver.enable();
									selectAllCheckbox.setEnabled(true);
									if(result.length>0) {
										loadingWindow.stop();
									}


								}

							});
				}	



				public void treatResult(ThemeRiverData[]result){

					for(int k=0;k<result.length;k++) {
						themeRiverDataForTopic[result[k].topicID] = result[k];
					}


					// alten plot löschen
					themeRiverChart.removeAllSeries();

					int rows = selectedTopics.size();

					ChartTitle title = new ChartTitle();
					if(rows==0) {

						title.setText("Please select topics to display from the left-hand pane");
					} else {
						title.setText("Topic Evolution");
					}
					themeRiverChart.setTitle(title,null);




					for(int topic : selectedTopics){
						//GWT.log("Its test:"+result.length);
						int columns = themeRiverDataForTopic[topic].relevanceAtTime.length;

						Double[]Y =new Double[columns];
						//String [] axis= new String[rows];

						for(int j=0;j< columns;j++){
							Y[j]=themeRiverDataForTopic[topic].relevanceAtTime[j];
							//axis[i]="Topic"+i;erh 

						}

						String nameOfSeries=String.valueOf(themeRiverDataForTopic[topic].topicID);
						String[] wordsAtTimeK = new String[themeRiverDataForTopic[topic].wordsAtTime.length];
						for (int i=0;i<themeRiverDataForTopic[topic].wordsAtTime.length;i++ ){

							String s = formatTopicLabel(themeRiverDataForTopic[topic].wordsAtTime[i]);
							wordsAtTimeK[i] = s;

						}
						createYaxis(Y,nameOfSeries,wordsAtTimeK);

					}


				}

				@Override
				public void onClick(ClickEvent event) {

					//if(event!=null) { GWT.log("Aufruf von "+event.toString()); }
					//else { GWT.log("Aufruf von null"); }

					// Werte von Words[i] werden von TopicsCheck[i](Membervaribel, oben definiert) ausgegeben!
					selectedTopics.clear();
					radioGroupItemThemeRiver.disable();
					selectAllCheckbox.setEnabled(false);
					LinkedList<Integer> topicsToRetrieveFromServer = new LinkedList<Integer>();
					for(int i=0;i<topicCheckboxes.length; i++ ){


						if (topicCheckboxes[i].getValue()) { 	// checkbox i ist ausgewählt
							selectedTopics.add(topicIDsCheckboxes[i]);		// füge die entsprechende topic der liste hinzu

							if(themeRiverDataForTopic[topicIDsCheckboxes[i]]==null) {
								topicsToRetrieveFromServer.add(topicIDsCheckboxes[i]);
							}

						}

						topicCheckboxes[i].setEnabled(false);
					}
					Integer [] Topics=new Integer[topicsToRetrieveFromServer.size()];	
					Topics = topicsToRetrieveFromServer.toArray(Topics);

					if(Topics.length>0) {
						loadingWindow.start("Loading Topic<br>",1);
					}

					sendSelectedTopicsToServer(Topics);

				}


			}



			class ClickHandlerTopicFromThemeRiver implements SeriesClickEventHandler, PointClickEventHandler{

				public void updatePanel(DocumentInfo [] result){ 


					if(currentNumberDocs == 0) {
						documentGrid.addData(new ListGridRecord()); // nötig damit auto-fit funktioniert						
						
						for(int i = documentGrid.getTotalRows() - 1; i >= 0; i--)
							documentGrid.removeData(documentGrid.getRecord(i));
					}


					Record[] meineButton = createButton(result);

					for(Record entry : meineButton){
						documentGrid.addData(entry);
					}

					documentGrid.selectRecord(0);
					currentNumberDocs += result.length;
				}



				//lISTE DER DOKS IN DEM PANEL
				public Record[] createButton(DocumentInfo [] result){
					
					//ClickHandlerRelatedDocumentsAccordeon [] doctit= new  ClickHandlerRelatedDocumentsAccordeon[result.length];	
					Record [] TitelOfDocument = new Record [result.length];
					for(int i = 0; i< result.length; i++){
						HLayout spaceInDocCheckBox = new HLayout();
						//HLayout test= new HLayout();
						//spaceInDocCheckBox.setMembersMargin(10);
						VLayout ButtonInDocCheckBox= new VLayout();
						ButtonInDocCheckBox.setAutoHeight();
						ButtonInDocCheckBox.setMembersMargin(10);


						spaceInDocCheckBox.setHeight(200);
						spaceInDocCheckBox.setWidth100();

						SectionStackSection section1 = new SectionStackSection("<i>" + result[i].docTitle + "</i>");  
						section1.setExpanded(false);

						spaceInDocCheckBox.addMember(ButtonInDocCheckBox);


						TitelOfDocument[i] = new Record();
						TitelOfDocument[i].setAttribute("DOCINFOS",result[i]);
						TitelOfDocument[i].setAttribute("layout",spaceInDocCheckBox);
						TitelOfDocument[i].setAttribute("position",i+1);						
						TitelOfDocument[i].setAttribute("title",result[i].docTitle);
						TitelOfDocument[i].setAttribute("docID",result[i].docID); // damit wir später drauf zugreifen können
						TitelOfDocument[i].setAttribute("buttonField",""); // damit wir später drauf zugreifen können
						TitelOfDocument[i].setAttribute("date",formatDateTime(result[i].docDate));

						int lastClickedTopicIndex = Arrays.asList(result[i].topicIDs).indexOf(lastClickedTopic);
						TitelOfDocument[i].setAttribute("weight", 
							lastClickedTopicIndex >= 0 ? Math.round(result[i].topicProportions[lastClickedTopicIndex] * 100.) + "%" : "?");
						
						//TitelOfDocument[i].setHeight("1em");
						//TitelOfDocument[i] = section1;

					}


					return TitelOfDocument;
				} 


				long intervalID;


				private void sendClickedTopicToServer(long topictime,int topic, int startBestDocsAtPosition) {
					intervalID=topictime;
					serverResponseLabel.setText("");
					loadingWindow.stop();
					theDocumentService.relatedDocuments(topic,
							DVitaParameters.MaxSimilarDocsDisplay,(int)topictime, new AsyncCallback<DocumentInfo[]>() {
								public void onFailure(Throwable caught) {
									// Show the RPC error message to the user
									dialogBox
									.setText("Remote Procedure Call - Failure");
									serverResponseLabel
									.addStyleName("serverResponseLabelError");
									serverResponseLabel.setHTML(SERVER_ERROR);
									dialogBox.center();
									closeButton.setFocus(true);
								}

								public void onSuccess(DocumentInfo[]result) {

									updatePanel(result);
									//loadingWindow.stop();
								}

							});



					wordService.bestWords(topic,
							10,topictime, new AsyncCallback<WordData>() {
						public void onFailure(Throwable caught) {
							// Show the RPC error message to the user
							dialogBox
							.setText("Remote Procedure Call - Failure");
							serverResponseLabel
							.addStyleName("serverResponseLabelError");
							serverResponseLabel.setHTML(SERVER_ERROR);
							dialogBox.center();
							closeButton.setFocus(true);
						}

						public void onSuccess(WordData Words) {
							wordButtonsForTopic(Words);
							handlerWortCheckbox.onClick(null);
							loadingWindow.stop();
						}
					});

				}
				
				@Override
				public boolean onClick(PointClickEvent pointClickEvent) {
					int newTime = xCoordChartToIntervalID.get(pointClickEvent.getXAsLong());

					int newTopic =Integer.parseInt(pointClickEvent.getSeriesName());
					
					return onClick(newTime,newTopic);
				}

				@Override
				public boolean onClick(SeriesClickEvent seriesClickEvent) {


					int newTime = xCoordChartToIntervalID.get(seriesClickEvent.getNearestXAsLong());

					int newTopic =Integer.parseInt(seriesClickEvent.getSeriesName());
					
					return onClick(newTime,newTopic);
				}
					
				public boolean onClick(int newTime, int newTopic) {

					if(lastClickedTimePoint==newTime && lastClickedTopic==newTopic) {
						// mache ncihts, das gleich wie vorher
						return true;
					}

					loadingWindow.start("Loading Related Documents and Words<br>",2);

					lastClickedTimePoint=newTime;
					lastClickedTopic=newTopic;




					currentTopicLabel.setContents("<b>Top documents related to the topic \""+topicIDtoName.get(lastClickedTopic)+ "\" between " +formatDateTime(topicIntervals[lastClickedTimePoint][0])+" and "+formatDateTime(topicIntervals[lastClickedTimePoint][1]) + "</b> (max. " + DVitaParameters.MaxSimilarDocsDisplay + " documents displayed)");
					currentTopicLabel2.setContents("<b>Evolution of word relevance for topic \""+topicIDtoName.get(lastClickedTopic)+ "\"</b>");

					//chart.createSeries();
					//Window.alert("You choosed the Topic:"+Topic+ "in Time: " +XValue);
					currentNumberDocs = 0;
					sendClickedTopicToServer(lastClickedTimePoint,lastClickedTopic,currentNumberDocs);

					return true;
				}



			
			}


			Double[] lastTopicProportionsPieChart;
			Integer[] lastTopicIDsPieChart;

			public Label analysisName;

			private Chart lastPie;



			void redrawPieChart() {

				if(lastTopicProportionsPieChart==null) return;

				Chart pie = createPieChart(this.lastTopicProportionsPieChart,this.lastTopicIDsPieChart);

				lastPie = pie;

				pie.setWidth("200px");
				pie.setHeight("200px");
				
				//pie.setHeight100();
				for(Canvas c : pieChartCanvas.getChildren()){
					pieChartCanvas.removeChild(c);
				}
				pieChartCanvas.addChild(pie);

				pie.getElement().getStyle().setOverflow(com.google.gwt.dom.client.Style.Overflow.VISIBLE);
				com.google.gwt.dom.client.Element e = pie.getElement();
				for(int i=1; i<=5; i++) {
					e = e.getParentElement();
					e.getStyle().setOverflow(com.google.gwt.dom.client.Style.Overflow.VISIBLE);
				}				
				((com.google.gwt.dom.client.Element) pie.getElement().getChild(0)).getStyle().setOverflow(com.google.gwt.dom.client.Style.Overflow.VISIBLE);
			}



			class ClickHandlerShowPieChart implements com.smartgwt.client.widgets.events.ClickHandler {

				DocumentInfo docInfo;


				@Override
				public void onClick(com.smartgwt.client.widgets.events.ClickEvent event) {

					lastTopicProportionsPieChart = docInfo.topicProportions;
					lastTopicIDsPieChart = docInfo.topicIDs;
					redrawPieChart();
				}

				public void setInfo(DocumentInfo documentInfo) {
					docInfo = documentInfo;
				}

			}

			public Chart createPieChart(Double[] topicProportions, Integer[] topicID) {  

				Point[] points = new Point[topicID.length];
				for(int i=0; i<points.length; i++) {
					points[i] = new Point(topicIDtoName.get(topicID[i]),topicProportions[i]);
					points[i].setColor(topicColorsString[topicID[i]]);
					//GWT.log("test: "+ topicColorsString[0]);
				}


				Chart chart = new Chart()  
					.setType(Series.Type.PIE)  
					.setChartTitleText("Topic Proportions")  
					.setPlotBackgroundColor((String) null)  
					.setPlotBorderWidth(0)  
					.setPlotShadow(false)  
					.setPiePlotOptions(new PiePlotOptions()  
						.setAllowPointSelect(false)
						.setSize(0.9)
						.setCursor(PlotOptions.Cursor.POINTER)  
						.setPieDataLabels(new PieDataLabels()  
							.setConnectorColor("#000000")  
							.setEnabled(true)
							.setDistance(-40)
							.setColor("#000000") 
							.setFormatter(new DataLabelsFormatter() {  
								public String format(DataLabelsData dataLabelsData) { 
									if( dataLabelsData.getYAsDouble()>0.3)
										return dataLabelsData.getPointName().split(WORDSEPARATOR)[0] + " ...";
									else 
										return "";
								}  
							})
						)  
					)  
					.setLegend(new Legend()  
						.setLayout(Legend.Layout.VERTICAL)  
						.setAlign(Legend.Align.RIGHT)  
						.setVerticalAlign(Legend.VerticalAlign.TOP)  
						.setX(-100)  
						.setY(100)  
						.setFloating(true)
						.setBorderWidth(1)  
						.setBackgroundColor("#FFFFFF")  
						.setShadow(true))  
					.setToolTip(new ToolTip()
						.setUseHTML(true)
						.setFormatter(new ToolTipFormatter() {
							public String format(ToolTipData toolTipData) {  
								//return "<b>" + toolTipData.getPointName().split(",")[0]+","+toolTipData.getPointName().split(",")[1]+", ..." + "</b>: " + ((int)(1000*toolTipData.getYAsDouble()))/10.0 + " %";
								return toolTipData.getPointName().replace(WORDSEPARATOR, WORDSEPARATOR + "<br />") + "<br />(" + ((int)(1000*toolTipData.getYAsDouble()))/10.0 + " %)";
							}  
						})
					);  

				chart.addSeries(chart.createSeries()  
					.setName("Topic proportions")  
					.setPoints(points));  

				chart.setCredits( new Credits().setText(""));
				return chart;  
			}  


			static class ClickHandlerShowDocument implements com.smartgwt.client.widgets.events.ClickHandler {
				int DocId;
				DocumentServiceAsync documentService;
				public void setData(int documentId, DocumentServiceAsync docService) {
					DocId= documentId; 
					this.documentService=docService;
				}

				private native void createWindowForDocs(String DocumentContain) /*-{
				var w = $wnd.open('','_blank','width=400,height=400,resizable,scrollbars');
				w.document.write(DocumentContain);
				w.document.close();

			}-*/;


				@Override
				public void onClick(com.smartgwt.client.widgets.events.ClickEvent event) {

					documentService.getDocumentData(DocId,
							new AsyncCallback<DocumentData>() {
						public void onFailure(Throwable caught) {

						}

						public void onSuccess(DocumentData pair) {


							String DocumentContain = pair.content;
							String title = pair.title;
							String header = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"><html><head><title>"+title+"</title></head>";
							String text = "<body style=\"background-color:#EEEEEE\"><span style=\"font-size:120%;color:black;font-family:sans-serif\">Title: "+ title+"<br/>Date: "+pair.date;
							if(pair.url != null) {
								text += "<br/><a href=\""+pair.url+"\" target=\"_blank\">Visit original document</a>";
							}
							text +=	"</span></br><p style=\"margin:30px;padding:10px;border-width:1px;border-style:solid;color:#222222;font-family:sans-serif;background-color:#FFFFFF\">";


							String footer = "</p></body></html>";

							createWindowForDocs(header+text+DocumentContain+footer);
							//////////////////////////////////////////////////////////////////////////////////////////////////////
							//contentOfDocument.setContents(DocumentContain);
						}




					});
				}




				//createWindowForDocs(Docheader+Doctext+DocContain+Docfooter);

			}


			class ClickHandlerOpenDocumentExplorer implements com.smartgwt.client.widgets.events.ClickHandler {
				DocumentInfo  docInfo;
				public void setInfo(DocumentInfo infos) {
					docInfo=infos;
				}

				@Override
				public void onClick(com.smartgwt.client.widgets.events.ClickEvent event) {
					documentExplorerWindow.UpdateWindowByDocID(docInfo,true,null);
					documentExplorerWindow.show();
					//	createWindowForDocs(Docheader+Doctext+DocContain+Docfooter);
				}

			}


}
