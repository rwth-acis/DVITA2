package i5.dvita.webapplication.client;

import i5.dvita.commons.DTSExecutionChain;
import i5.dvita.commons.DTSExecutionChain.WorkbenchElementData;
import i5.dvita.commons.DTSExecutionChainConnection;
import i5.dvita.commons.DTSExecutionChainInvoke;
import i5.dvita.commons.DTSExecutionChainRunningInformation;
import i5.dvita.commons.DTSExecutionChainRunningInformation.DTSToolRunningInformation;
import i5.dvita.commons.DatabaseConfigurationAnalysis.Granularity;
import i5.dvita.commons.DatabaseRepresentationData.DatabaseConfigurationAnalysisRepresentation;
import i5.dvita.commons.ACL;
import i5.dvita.commons.DTSExecutionChainUniqueId;
import i5.dvita.commons.DTSGetRunningToolOutput;
import i5.dvita.commons.DTSReturn;
import i5.dvita.commons.DTSServerInformation;
import i5.dvita.commons.DTSTool;
import i5.dvita.commons.DTSToolsIOSQL;
import i5.dvita.commons.DTSToolsIOString;
import i5.dvita.commons.DatabaseConfigurationAnalysis;
import i5.dvita.commons.DatabaseConfigurationRawdata;
import i5.dvita.commons.DatabaseConnection;
import i5.dvita.commons.DatabaseRepresentationData;
import i5.dvita.commons.IDTSToolsIO;
import i5.dvita.commons.RequestAnalysis;
import i5.dvita.webapplication.shared.UserShared;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DefaultDateTimeFormatInfo;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.docs.Offline;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ContentsType;
import com.smartgwt.client.types.DragAppearance;
import com.smartgwt.client.types.ExpansionMode;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.EventHandler;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.HTMLPane;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.drawing.DrawLinePath;
import com.smartgwt.client.widgets.drawing.DrawPane;
import com.smartgwt.client.widgets.drawing.Point;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.DragRepositionStopEvent;
import com.smartgwt.client.widgets.events.DragRepositionStopHandler;
import com.smartgwt.client.widgets.events.DropEvent;
import com.smartgwt.client.widgets.events.DropHandler;
import com.smartgwt.client.widgets.events.DropOutEvent;
import com.smartgwt.client.widgets.events.DropOutHandler;
import com.smartgwt.client.widgets.events.DropOverEvent;
import com.smartgwt.client.widgets.events.DropOverHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.RecordDoubleClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordDoubleClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.tab.events.TabSelectedEvent;
import com.smartgwt.client.widgets.tab.events.TabSelectedHandler;

public class ControlPanelWindow extends Window
{
	private VLayout _mainLayout;
	private TabSet _tabBar;
	
	private WorkbenchArea _workbenchArea;
	private HashMap<String, DrawLinePath> _connections = new HashMap<String, DrawLinePath>();
	
	private ControlPanelServiceAsync controlPanelService = GWT.create(ControlPanelService.class);
	private SetupServiceAsync setupService = GWT.create(SetupService.class);
	
	private HashMap<String,DTSServerInformation> _dtsData = new HashMap<String,DTSServerInformation>();
	private DatabaseRepresentationData _databaseRepresentationData = null;
	private HashMap<String,DatabaseConfigurationAnalysis> _analysisConfigList = null;
	private DVITA_WebApplication _mainWindow = null;
	
	private Tab _tabGeneral = null;
	private List<String> _allGuiIds = new ArrayList<String>();
	
	public ControlPanelWindow(DVITA_WebApplication mainWindow)
	{
		// initialize 
		this._mainLayout = new VLayout();
		this._tabBar = new TabSet();
		this._mainWindow = mainWindow;
		
		// settings for the window itself
		this.setWidth("90%");
		this.setHeight("90%");
		this.centerInPage();
		this.setTitle("Control Panel");
		
		// create tab bar
		_tabGeneral = new Tab("General");
		_tabGeneral.setDisabled(true);
		_tabGeneral.addTabSelectedHandler(new TabSelectedHandler()
		{
			@Override
			public void onTabSelected(TabSelectedEvent event)
			{
				_mainWindow.loadingWindow.start("Access database...<br />", 1);
				
				// get all the necessary data
				controlPanelService.getDatabaseRepresentationData(new AsyncCallback<DatabaseRepresentationData>()
				{
					@Override
					public void onFailure(Throwable caught)
					{
						_mainWindow.loadingWindow.stop();
					}

					@Override
					public void onSuccess(DatabaseRepresentationData result)
					{
						_databaseRepresentationData = result;
						_tabGeneral();
						_mainWindow.loadingWindow.stop();
					}
				}); 
			}
		});
		this._tabBar.addTab(_tabGeneral);
		
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
				if (result.getOperations().contains(ACL.OPERATION_CONFIGURATION))
				{
					if (null != _tabGeneral)
					{
						_tabGeneral.setDisabled(false);
					}
				}
			}
		});
		
		Tab tabToolServer = new Tab("Tool Server");
		tabToolServer.addTabSelectedHandler(new TabSelectedHandler()
		{
			@Override
			public void onTabSelected(TabSelectedEvent event)
			{
				_mainWindow.loadingWindow.start("Receiving Toolserver Data<br />", 1);
				
				// get all the necessary data
				controlPanelService.getServerInformation(new AsyncCallback<HashMap<String,DTSServerInformation>>()
				{
					@Override
					public void onFailure(Throwable caught) 
					{
						_mainWindow.loadingWindow.stop();
						SC.say("Failed to connect to DTS");
					}

					@Override
					public void onSuccess(HashMap<String,DTSServerInformation> result)
					{
						if (0 == result.size())
						{
							SC.say("No toolserver available!");
						}
						else 
						{
							_dtsData = result;
							_tabToolServer();
						}
						
						_mainWindow.loadingWindow.stop();
					}
				});
			}
		});

		this._tabBar.addTab(tabToolServer);
		this._tabBar.selectTab(1);
		// add tab bar to main layout	
		this._mainLayout.addMember(this._tabBar);
		
		// add main layout to window
		this.addItem(this._mainLayout);	
	}
	
	private void _tabGeneral()
	{
		// layouts
		VLayout layoutMain = new VLayout();
		
		HLayout layoutAnalysisRawdata = new HLayout();
		VLayout layoutAnalysis = new VLayout();
		HLayout layoutAnalysisLabelButtons = new HLayout();
		HLayout layoutAnalysisButtons = new HLayout();
		VLayout layoutRawdata = new VLayout();
		HLayout layoutRawdataLabelButtons = new HLayout();
		HLayout layoutRawdataButtons = new HLayout();
		
		VLayout layoutConnections = new VLayout();
		HLayout layoutConnectionsLabelButtons = new HLayout();
		
		VLayout layoutAnalysisRepresentation = new VLayout();
		HLayout layoutAnalysisRepresentationLabelButtons = new HLayout();
		
		// analysis
		Label labelAnalysis = new Label("Analyses configurations");
		labelAnalysis.setStyleName("controlPanelHeaders");
		labelAnalysis.setAutoHeight();
		labelAnalysis.setWidth(180);
		
		final ListGrid gridAnalysis = new ListGrid()
		{  
            @Override  
            protected Canvas getExpansionComponent(final ListGridRecord record)
            {
            	VLayout layoutExpansion = new VLayout();
            	HLayout layoutExpansionButtons = new HLayout();
            	
            	DatabaseConfigurationAnalysis databaseConfigurationAnalysis = 
            			_databaseRepresentationData.getAnalysisRows().get(Integer.parseInt(record.getAttribute("id")));
            	final DynamicForm form = new DynamicForm();
				form.setWidth100();
				form.setColWidths("50%", "50%");
				final TextItem textFieldId = new TextItem();
				textFieldId.setTitle("id");
				textFieldId.setValue(databaseConfigurationAnalysis.id);
				final TextItem textFieldRawdataId = new TextItem();
				textFieldRawdataId.setTitle("Rawdata id");
				textFieldRawdataId.setValue(databaseConfigurationAnalysis.databaseConfigurationRawdata.id);
				final TextItem textFieldConnectionId = new TextItem();
				textFieldConnectionId.setTitle("Connection id");
				textFieldConnectionId.setValue(databaseConfigurationAnalysis.connectionId);
				final TextItem textFieldGranularity = new TextItem();
				textFieldGranularity.setTitle("Granularity");
				switch (databaseConfigurationAnalysis.gran) 
				{
					case YEARLY: textFieldGranularity.setValue(1); break;
					case MONTHLY: textFieldGranularity.setValue(2); break;
					case WEEKLY: textFieldGranularity.setValue(3); break;
					case DAYLY: textFieldGranularity.setValue(4); break;
					default: textFieldGranularity.setValue(1); break;
				} 
				final TextItem textFieldRangeStart = new TextItem();
				textFieldRangeStart.setTitle("Range start");
				DateTimeFormat dtf = new DateTimeFormat("yyyy-MM-dd HH:mm:ss", new DefaultDateTimeFormatInfo()) {};
				String rangeStartString = "";
				if (null != databaseConfigurationAnalysis.rangeStart)
				{
					rangeStartString = dtf.format(databaseConfigurationAnalysis.rangeStart);
				}
				textFieldRangeStart.setValue(rangeStartString);
				final TextItem textFieldRangeEnd = new TextItem();
				textFieldRangeEnd.setTitle("Range end");
				String rangeEndString = "";
				if (null != databaseConfigurationAnalysis.rangeEnd)
				{
					rangeEndString = dtf.format(databaseConfigurationAnalysis.rangeEnd);
				}
				textFieldRangeEnd.setValue(rangeEndString);
				final TextItem textFieldNumberTopics = new TextItem();
				textFieldNumberTopics.setTitle("Number topics");
				textFieldNumberTopics.setValue(databaseConfigurationAnalysis.NumberTopics);
				final TextItem textFieldMetaTitle = new TextItem();
				textFieldMetaTitle.setTitle("Title");
				textFieldMetaTitle.setValue(databaseConfigurationAnalysis.metaTitle);
				final TextItem textFieldMetaDescription = new TextItem();
				textFieldMetaDescription.setTitle("Description");
				textFieldMetaDescription.setValue(databaseConfigurationAnalysis.metaDescription);
				final TextItem textFieldTablePrefix = new TextItem();
				textFieldTablePrefix.setTitle("Table prefix");
				textFieldTablePrefix.setValue(databaseConfigurationAnalysis.tablePrefix);
				final TextItem textFieldTableStatus = new TextItem();
				textFieldTableStatus.setTitle("Status");
				textFieldTableStatus.setValue(databaseConfigurationAnalysis.status);

				form.setFields
				(
						textFieldId, textFieldRawdataId, textFieldConnectionId, 
						textFieldGranularity, textFieldRangeStart, textFieldRangeEnd, 
						textFieldNumberTopics, textFieldMetaTitle, textFieldMetaDescription,
						textFieldTablePrefix, textFieldTableStatus
				);

				IButton buttonSave = new IButton("Save");  
				buttonSave.addClickHandler(new ClickHandler() 
				{
					public void onClick(ClickEvent event)
					{
						DatabaseConfigurationAnalysis databaseConfigurationAnalysis = new DatabaseConfigurationAnalysis();
						databaseConfigurationAnalysis.databaseConfigurationRawdata = new DatabaseConfigurationRawdata();
						databaseConfigurationAnalysis.id = Integer.parseInt(textFieldId.getValueAsString());
						databaseConfigurationAnalysis.databaseConfigurationRawdata.id = Integer.parseInt(textFieldRawdataId.getValueAsString());
						databaseConfigurationAnalysis.connectionId = Integer.parseInt(textFieldConnectionId.getValueAsString());
						switch (Integer.parseInt(textFieldGranularity.getValueAsString())) 
						{
							case 1: databaseConfigurationAnalysis.gran = Granularity.YEARLY; break;
							case 2: databaseConfigurationAnalysis.gran = Granularity.MONTHLY; break;
							case 3: databaseConfigurationAnalysis.gran = Granularity.WEEKLY; break;
							case 4: databaseConfigurationAnalysis.gran = Granularity.DAYLY; break;
							default: databaseConfigurationAnalysis.gran = Granularity.YEARLY; break;
						}
						
						databaseConfigurationAnalysis.rangeStart = Timestamp.valueOf(textFieldRangeStart.getValueAsString());
						databaseConfigurationAnalysis.rangeEnd = Timestamp.valueOf(textFieldRangeEnd.getValueAsString());
						databaseConfigurationAnalysis.NumberTopics = Integer.parseInt(textFieldNumberTopics.getValueAsString());
						databaseConfigurationAnalysis.metaTitle = textFieldMetaTitle.getValueAsString();
						databaseConfigurationAnalysis.metaDescription = textFieldMetaDescription.getValueAsString();
						databaseConfigurationAnalysis.tablePrefix = textFieldTablePrefix.getValueAsString();
						databaseConfigurationAnalysis.status = Integer.parseInt(textFieldTableStatus.getValueAsString());
						
						DatabaseRepresentationData databaseRepresentationData = new DatabaseRepresentationData();
						HashMap<Integer, DatabaseConfigurationAnalysis> databaseRow = new HashMap<Integer, DatabaseConfigurationAnalysis>();
						databaseRow.put(databaseConfigurationAnalysis.id, databaseConfigurationAnalysis);
						databaseRepresentationData.setAnalysisRows(databaseRow);
						controlPanelService.setDatabaseRepresentationData(databaseRepresentationData, new AsyncCallback<Void>()
						{
							@Override
							public void onFailure(Throwable caught)
							{
								SC.say("Failed to save database data");
							}

							@Override
							public void onSuccess(Void result)
							{
								_mainWindow.loadingWindow.start("Access database...<br />", 1);
								
								controlPanelService.getDatabaseRepresentationData(new AsyncCallback<DatabaseRepresentationData>()
								{
									@Override
									public void onFailure(Throwable caught)
									{
										_mainWindow.loadingWindow.stop();
									}

									@Override
									public void onSuccess(DatabaseRepresentationData result)
									{
										_databaseRepresentationData = result;
										_tabGeneral();
										_mainWindow.loadingWindow.stop();
									}
								}); 
							}
						});
                  }
				}); 
				
				layoutExpansionButtons.addMember(buttonSave);
				layoutExpansion.addMember(form);
				layoutExpansion.addMember(layoutExpansionButtons);
				
				return layoutExpansion;
            }  
        };
        
        gridAnalysis.setCanExpandRecords(true);
        gridAnalysis.setAutoFetchData(true); 
        gridAnalysis.setShowRecordComponents(true);
        gridAnalysis.setShowRecordComponentsByCell(true);
        DataSource gridAnalysisdataSource = new DataSource();
        DataSourceTextField gridAnalysisDataSourceFieldId = new DataSourceTextField("id");
        DataSourceTextField gridAnalysisDataSourceFieldTitle = new DataSourceTextField("title", "Title");  
		DataSourceTextField gridAnalysisDataSourceFieldDescription = new DataSourceTextField("description", "Description");
		gridAnalysisdataSource.setFields(gridAnalysisDataSourceFieldId, gridAnalysisDataSourceFieldTitle, gridAnalysisDataSourceFieldDescription);
		List<ListGridRecord> gridAnalysisRecords = new ArrayList<ListGridRecord>();
		for (Map.Entry<Integer, DatabaseConfigurationAnalysis> entry : this._databaseRepresentationData.getAnalysisRows().entrySet())
		{
			ListGridRecord record = new ListGridRecord();
			record.setAttribute("id", entry.getKey());
			record.setAttribute("title", entry.getValue().metaTitle);
			record.setAttribute("description", entry.getValue().metaDescription);
			gridAnalysisRecords.add(record);
		}
		gridAnalysisdataSource.setTestData(gridAnalysisRecords.toArray(new ListGridRecord[gridAnalysisRecords.size()]));
		gridAnalysisdataSource.setClientOnly(true);
		gridAnalysis.setShowAllRecords(true);  
		gridAnalysis.setDataSource(gridAnalysisdataSource);
		
		ImgButton buttonAnalysisAdd = new ImgButton(); 
	    buttonAnalysisAdd.setShowDown(false);  
	    buttonAnalysisAdd.setShowRollOver(false); 
    	buttonAnalysisAdd.setAlign(Alignment.CENTER);  
    	buttonAnalysisAdd.setSrc("controlpanel/1396472257_plus_32.png");  
    	buttonAnalysisAdd.setPrompt("Add analysis configuration row");  
    	buttonAnalysisAdd.setHeight(16);  
    	buttonAnalysisAdd.setWidth(16);  
		buttonAnalysisAdd.addClickHandler(new ClickHandler() 
		{  
            public void onClick(ClickEvent event) 
            {
            	_mainWindow.loadingWindow.start("Create new dataset...<br />", 1);
            	DatabaseRepresentationData databaseRepresentationData = new DatabaseRepresentationData();
            	databaseRepresentationData.setAnalysisRows(new HashMap<Integer, DatabaseConfigurationAnalysis>());
            	controlPanelService.addDatabaseRepresentationData(databaseRepresentationData, new AsyncCallback<Void>()
				{
					@Override
					public void onFailure(Throwable caught) 
					{
						_mainWindow.loadingWindow.stop();
						SC.say("Can't create new dataset");
					}

					@Override
					public void onSuccess(Void result)
					{
						_mainWindow.loadingWindow.stop();
						_mainWindow.loadingWindow.start("Access database...<br />", 1);
						
						controlPanelService.getDatabaseRepresentationData(new AsyncCallback<DatabaseRepresentationData>()
						{
							@Override
							public void onFailure(Throwable caught)
							{
								_mainWindow.loadingWindow.stop();
							}

							@Override
							public void onSuccess(DatabaseRepresentationData result)
							{
								_databaseRepresentationData = result;
								_tabGeneral();
								_mainWindow.loadingWindow.stop();
							}
						}); 
					}
				});
            }
		});
		
		ImgButton buttonAnalysisRemove = new ImgButton(); 
	    buttonAnalysisRemove.setShowDown(false);  
	    buttonAnalysisRemove.setShowRollOver(false); 
    	buttonAnalysisRemove.setAlign(Alignment.CENTER);  
    	buttonAnalysisRemove.setSrc("controlpanel/1396472361_delete_32.png");  
    	buttonAnalysisRemove.setPrompt("Delete selected analysis configuration row");  
    	buttonAnalysisRemove.setHeight(16);  
    	buttonAnalysisRemove.setWidth(16);  
		buttonAnalysisRemove.addClickHandler(new ClickHandler() 
		{  
            public void onClick(ClickEvent event) 
            {
            	SC.ask
            	(
            		"Delete analysis",
            		"Do you really want to delete " + gridAnalysis.getSelectedRecord().getAttribute("title") + "?",
            		new BooleanCallback()
            		{
						@Override
						public void execute(Boolean value)
						{
							if (value)
							{
								_mainWindow.loadingWindow.start("Delete analysis configuration...<br />", 1);
								
								DatabaseRepresentationData databaseRepresentationData = new DatabaseRepresentationData();
				            	DatabaseConfigurationAnalysis databaseConfigurationAnalysis = new DatabaseConfigurationAnalysis();
				            	databaseConfigurationAnalysis.id = Integer.parseInt(gridAnalysis.getSelectedRecord().getAttribute("id"));
				            	HashMap<Integer, DatabaseConfigurationAnalysis> databaseRow = new HashMap<Integer, DatabaseConfigurationAnalysis>();
				            	databaseRow.put(databaseConfigurationAnalysis.id, databaseConfigurationAnalysis);
				            	databaseRepresentationData.setAnalysisRows(databaseRow);
				            	
								controlPanelService.removeDatabaseRepresentationData(databaseRepresentationData, new AsyncCallback<Void>()
								{
									@Override
									public void onFailure(Throwable caught) 
									{
										_mainWindow.loadingWindow.stop();
										SC.say("Deletion failed");
									}

									@Override
									public void onSuccess(Void result)
									{
										_mainWindow.loadingWindow.stop();
										_mainWindow.loadingWindow.start("Access database...<br />", 1);
										controlPanelService.getDatabaseRepresentationData(new AsyncCallback<DatabaseRepresentationData>()
										{
											@Override
											public void onFailure(Throwable caught)
											{
												_mainWindow.loadingWindow.stop();
											}

											@Override
											public void onSuccess(DatabaseRepresentationData result)
											{
												_databaseRepresentationData = result;
												_tabGeneral();
												_mainWindow.loadingWindow.stop();
											}
										}); 
									}
								});
							}
						}
            			
            		}
            	); 
            }
		});
		
    	// rawdata
 		Label labelRawdata = new Label("Rawdata configurations");
 		labelRawdata.setStyleName("controlPanelHeaders");
 		labelRawdata.setAutoHeight();
 		labelRawdata.setWidth(220);
 		
 		final ListGrid gridRawdata = new ListGrid()
 		{  
             @Override  
             protected Canvas getExpansionComponent(final ListGridRecord record)
             {
             	VLayout layoutExpansion = new VLayout();
             	HLayout layoutExpansionButtons = new HLayout();
             	
             	DatabaseConfigurationRawdata databaseConfigurationRawdata = 
             			_databaseRepresentationData.getRawdataRows().get(Integer.parseInt(record.getAttribute("id")));
             	final DynamicForm form = new DynamicForm();
 				form.setWidth100();
 				form.setColWidths("50%", "50%");
 				final TextItem textFieldId = new TextItem();
 				textFieldId.setTitle("id");
 				textFieldId.setValue(databaseConfigurationRawdata.id);
 				final TextItem textFieldConnectionId = new TextItem();
 				textFieldConnectionId.setTitle("Connection id");
 				textFieldConnectionId.setValue(databaseConfigurationRawdata.connectionId);
 				final TextItem textFieldColumnNameID = new TextItem();
 				textFieldColumnNameID.setTitle("Column name of 'id'");
 				textFieldColumnNameID.setValue(databaseConfigurationRawdata.columnNameID);
 				final TextItem textFieldColumnNameDate = new TextItem();
 				textFieldColumnNameDate.setTitle("Column name of 'date'");
 				textFieldColumnNameDate.setValue(databaseConfigurationRawdata.columnNameDate);
 				final TextItem textFieldColumnNameContent = new TextItem();
 				textFieldColumnNameContent.setTitle("Column name of 'content'");
 				textFieldColumnNameContent.setValue(databaseConfigurationRawdata.columnNameContent);
 				final TextItem textFieldColumnNameTitle = new TextItem();
 				textFieldColumnNameTitle.setTitle("Column name of 'title'");
 				textFieldColumnNameTitle.setValue(databaseConfigurationRawdata.columnNameTitle);
 				final TextItem textFieldColumnNameURL = new TextItem();
 				textFieldColumnNameURL.setTitle("Column name of 'URL'");
 				textFieldColumnNameURL.setValue(databaseConfigurationRawdata.columnNameURL);
 				final TextItem textFieldFromClause = new TextItem();
 				textFieldFromClause.setTitle("From clause");
 				textFieldFromClause.setValue(databaseConfigurationRawdata.fromClause);
 				final TextItem textFieldWhereClause = new TextItem();
 				textFieldWhereClause.setTitle("Where clause");
 				textFieldWhereClause.setValue(databaseConfigurationRawdata.whereClause);
 				final TextItem textFieldTitle = new TextItem();
 				textFieldTitle.setTitle("Title");
 				textFieldTitle.setValue(databaseConfigurationRawdata.metaTitle);
 				final TextItem textFieldDescription = new TextItem();
 				textFieldDescription.setTitle("Description");
 				textFieldDescription.setValue(databaseConfigurationRawdata.metaDescription);
 				final TextItem textFieldTablePrefix = new TextItem();
 				textFieldTablePrefix.setTitle("Table prefix");
 				textFieldTablePrefix.setValue(databaseConfigurationRawdata.tablePrefix);
 				form.setFields
 				(
 						textFieldId, textFieldConnectionId, 
 						textFieldColumnNameID, textFieldColumnNameDate, textFieldColumnNameContent, 
 						textFieldColumnNameTitle, textFieldColumnNameURL, textFieldFromClause,
 						textFieldWhereClause, textFieldTitle, textFieldDescription, textFieldTablePrefix
 				);

 				IButton buttonSave = new IButton("Save");  
 				buttonSave.addClickHandler(new ClickHandler() 
 				{
 					public void onClick(ClickEvent event)
 					{
 						DatabaseConfigurationRawdata databaseConfigurationRawdata = new DatabaseConfigurationRawdata();
 						databaseConfigurationRawdata.id = Integer.parseInt(textFieldId.getValueAsString());
 						databaseConfigurationRawdata.connectionId = Integer.parseInt(textFieldConnectionId.getValueAsString());
 						databaseConfigurationRawdata.columnNameID = textFieldColumnNameID.getValueAsString();
 						databaseConfigurationRawdata.columnNameDate = textFieldColumnNameDate.getValueAsString();
 						databaseConfigurationRawdata.columnNameContent = textFieldColumnNameContent.getValueAsString();
 						databaseConfigurationRawdata.columnNameTitle = textFieldColumnNameTitle.getValueAsString();
 						databaseConfigurationRawdata.columnNameURL = textFieldColumnNameURL.getValueAsString();
 						databaseConfigurationRawdata.fromClause = textFieldFromClause.getValueAsString();
 						databaseConfigurationRawdata.whereClause = textFieldWhereClause.getValueAsString();    						
 						databaseConfigurationRawdata.metaTitle = textFieldTitle.getValueAsString();
 						databaseConfigurationRawdata.metaDescription = textFieldDescription.getValueAsString();
 						databaseConfigurationRawdata.tablePrefix = textFieldTablePrefix.getValueAsString();
 						
 						DatabaseRepresentationData databaseRepresentationData = new DatabaseRepresentationData();
 						HashMap<Integer, DatabaseConfigurationRawdata> databaseRow = new HashMap<Integer, DatabaseConfigurationRawdata>();
 						databaseRow.put(databaseConfigurationRawdata.id, databaseConfigurationRawdata);
 						databaseRepresentationData.setRawdataRows(databaseRow);
 						controlPanelService.setDatabaseRepresentationData(databaseRepresentationData, new AsyncCallback<Void>()
 						{
 							@Override
 							public void onFailure(Throwable caught)
 							{
 								SC.say("Failed to save database data");
 							}

 							@Override
 							public void onSuccess(Void result)
 							{
 								SC.say("Data saved!");
 							}
 						});
                   }
 				}); 
 				
 				layoutExpansionButtons.addMember(buttonSave);
 				layoutExpansion.addMember(form);
 				layoutExpansion.addMember(layoutExpansionButtons);
 				
 				return layoutExpansion;
             }  
        };
        
        gridRawdata.setCanExpandRecords(true);
        gridRawdata.setAutoFetchData(true); 
        gridRawdata.setShowRecordComponents(true);
        gridRawdata.setShowRecordComponentsByCell(true);
        DataSource gridRawdatadataSource = new DataSource();
        DataSourceTextField gridRawdataDataSourceFieldId = new DataSourceTextField("id");
        DataSourceTextField gridRawdataDataSourceFieldTitle = new DataSourceTextField("title", "Title");  
		DataSourceTextField gridRawdataDataSourceFieldDescription = new DataSourceTextField("description", "Description");
		gridRawdatadataSource.setFields(gridRawdataDataSourceFieldId, gridRawdataDataSourceFieldTitle, gridRawdataDataSourceFieldDescription);
		List<ListGridRecord> gridRawdataRecords = new ArrayList<ListGridRecord>();
		for (Map.Entry<Integer, DatabaseConfigurationRawdata> entry : this._databaseRepresentationData.getRawdataRows().entrySet())
		{
			ListGridRecord record = new ListGridRecord();
			record.setAttribute("id", entry.getKey());
			record.setAttribute("title", entry.getValue().metaTitle);
			record.setAttribute("description", entry.getValue().metaDescription);
			gridRawdataRecords.add(record);
		}
		gridRawdatadataSource.setTestData(gridRawdataRecords.toArray(new ListGridRecord[gridRawdataRecords.size()]));
		gridRawdatadataSource.setClientOnly(true);
		gridRawdata.setShowAllRecords(true);  
		gridRawdata.setDataSource(gridRawdatadataSource);
		
		ImgButton buttonRawdataAdd = new ImgButton(); 
	    buttonRawdataAdd.setShowDown(false);  
	    buttonRawdataAdd.setShowRollOver(false); 
    	buttonRawdataAdd.setAlign(Alignment.CENTER);  
    	buttonRawdataAdd.setSrc("controlpanel/1396472257_plus_32.png");  
    	buttonRawdataAdd.setPrompt("Add rawdata configuration row");  
    	buttonRawdataAdd.setHeight(16);  
    	buttonRawdataAdd.setWidth(16);  
		buttonRawdataAdd.addClickHandler(new ClickHandler() 
		{  
            public void onClick(ClickEvent event) 
            {
            	_mainWindow.loadingWindow.start("Create new dataset...<br />", 1);
            	DatabaseRepresentationData databaseRepresentationData = new DatabaseRepresentationData();
            	databaseRepresentationData.setRawdataRows(new HashMap<Integer, DatabaseConfigurationRawdata>());
            	controlPanelService.addDatabaseRepresentationData(databaseRepresentationData, new AsyncCallback<Void>()
				{
					@Override
					public void onFailure(Throwable caught) 
					{
						_mainWindow.loadingWindow.stop();
						SC.say("Can't create new dataset");
					}

					@Override
					public void onSuccess(Void result)
					{
						_mainWindow.loadingWindow.stop();
						_mainWindow.loadingWindow.start("Access database...<br />", 1);
						
						controlPanelService.getDatabaseRepresentationData(new AsyncCallback<DatabaseRepresentationData>()
						{
							@Override
							public void onFailure(Throwable caught)
							{
								_mainWindow.loadingWindow.stop();
							}

							@Override
							public void onSuccess(DatabaseRepresentationData result)
							{
								_databaseRepresentationData = result;
								_tabGeneral();
								_mainWindow.loadingWindow.stop();
							}
						}); 
					}
				});
            }
		});
		
		ImgButton buttonRawdataRemove = new ImgButton(); 
	    buttonRawdataRemove.setShowDown(false);  
	    buttonRawdataRemove.setShowRollOver(false); 
    	buttonRawdataRemove.setAlign(Alignment.CENTER);  
    	buttonRawdataRemove.setSrc("controlpanel/1396472361_delete_32.png");  
    	buttonRawdataRemove.setPrompt("Delete selected rawdata configuration row");  
    	buttonRawdataRemove.setHeight(16);  
    	buttonRawdataRemove.setWidth(16);  
		buttonRawdataRemove.addClickHandler(new ClickHandler() 
		{  
            public void onClick(ClickEvent event) 
            {
            	SC.ask
            	(
            		"Delete rawdata",
            		"Do you really want to delete " + gridRawdata.getSelectedRecord().getAttribute("title") + "?",
            		new BooleanCallback()
            		{
						@Override
						public void execute(Boolean value)
						{
							if (value)
							{
								_mainWindow.loadingWindow.start("Delete rawdata configuration...<br />", 1);
								
								DatabaseRepresentationData databaseRepresentationData = new DatabaseRepresentationData();
				            	DatabaseConfigurationRawdata databaseConfigurationRawdata = new DatabaseConfigurationRawdata();
				            	databaseConfigurationRawdata.id = Integer.parseInt(gridRawdata.getSelectedRecord().getAttribute("id"));
				            	HashMap<Integer, DatabaseConfigurationRawdata> databaseRow = new HashMap<Integer, DatabaseConfigurationRawdata>();
				            	databaseRow.put(databaseConfigurationRawdata.id, databaseConfigurationRawdata);
				            	databaseRepresentationData.setRawdataRows(databaseRow);
				            	
								controlPanelService.removeDatabaseRepresentationData(databaseRepresentationData, new AsyncCallback<Void>()
								{
									@Override
									public void onFailure(Throwable caught) 
									{
										_mainWindow.loadingWindow.stop();
										SC.say("Deletion failed");
									}

									@Override
									public void onSuccess(Void result)
									{
										_mainWindow.loadingWindow.stop();
										_mainWindow.loadingWindow.start("Access database...<br />", 1);
										controlPanelService.getDatabaseRepresentationData(new AsyncCallback<DatabaseRepresentationData>()
										{
											@Override
											public void onFailure(Throwable caught)
											{
												_mainWindow.loadingWindow.stop();
											}

											@Override
											public void onSuccess(DatabaseRepresentationData result)
											{
												_databaseRepresentationData = result;
												_tabGeneral();
												_mainWindow.loadingWindow.stop();
											}
										}); 
									}
								});
							}
						}
            			
            		}
            	); 
            }
		});
		
		// connections
 		Label labelConnections = new Label("Databases");
 		labelConnections.setStyleName("controlPanelHeaders");
 		labelConnections.setAutoHeight();
 		labelConnections.setWidth(220);
 		
 		final ListGrid gridConnections = new ListGrid()
 		{  
             @Override  
             protected Canvas getExpansionComponent(final ListGridRecord record)
             {
             	VLayout layoutExpansion = new VLayout();
             	HLayout layoutExpansionButtons = new HLayout();
             	
             	DatabaseConnection databaseConnection = 
             			_databaseRepresentationData.getConnectionsRows().get(Integer.parseInt(record.getAttribute("id")));
             	final DynamicForm form = new DynamicForm();
 				form.setWidth100();
 				form.setColWidths("50%", "50%");
 				final TextItem textFieldId = new TextItem();
 				textFieldId.setTitle("id");
 				textFieldId.setValue(databaseConnection.id);
 				final TextItem textFieldName = new TextItem();
 				textFieldName.setTitle("Name");
 				textFieldName.setValue(databaseConnection.name);
 				final TextItem textFieldDescription = new TextItem();
 				textFieldDescription.setTitle("Description");
 				textFieldDescription.setValue(databaseConnection.description);
 				final TextItem textFieldServer = new TextItem();
 				textFieldServer.setTitle("Server");
 				textFieldServer.setValue(databaseConnection.server);
 				final TextItem textFieldPort = new TextItem();
 				textFieldPort.setTitle("Port");
 				textFieldPort.setValue(databaseConnection.port);
 				final TextItem textFieldDatabasename = new TextItem();
 				textFieldDatabasename.setTitle("Database name");
 				textFieldDatabasename.setValue(databaseConnection.databasename);
 				final TextItem textFieldSchema = new TextItem();
 				textFieldSchema.setTitle("Schema");
 				textFieldSchema.setValue(databaseConnection.schema);
 				final TextItem textFieldType = new TextItem();
 				textFieldType.setTitle("Type");
 				textFieldType.setValue(databaseConnection.type);
 				final TextItem textFieldUser = new TextItem();
 				textFieldUser.setTitle("User");
 				textFieldUser.setValue(databaseConnection.user);
 				final TextItem textFieldPassword = new TextItem();
 				textFieldPassword.setTitle("Password");
 				textFieldPassword.setValue(databaseConnection.passwort);
 				form.setFields
 				(
 						textFieldId, textFieldName, 
 						textFieldDescription, textFieldServer, textFieldPort, 
 						textFieldDatabasename, textFieldSchema, textFieldType,
 						textFieldUser, textFieldPassword
 				);

 				IButton buttonSave = new IButton("Save");  
 				buttonSave.addClickHandler(new ClickHandler() 
 				{
 					public void onClick(ClickEvent event)
 					{
 						DatabaseConnection databaseConnection = new DatabaseConnection();
 						databaseConnection.id = Integer.parseInt(textFieldId.getValueAsString());
 						databaseConnection.name = textFieldName.getValueAsString();
 						databaseConnection.description = textFieldDescription.getValueAsString();
 						databaseConnection.server = textFieldServer.getValueAsString();
 						databaseConnection.port = Integer.parseInt(textFieldPort.getValueAsString());
 						databaseConnection.databasename = textFieldDatabasename.getValueAsString();
 						databaseConnection.schema = textFieldSchema.getValueAsString();
 						databaseConnection.type = Integer.parseInt(textFieldType.getValueAsString());    						
 						databaseConnection.user = textFieldUser.getValueAsString();
 						databaseConnection.passwort = textFieldPassword.getValueAsString();
 						
 						DatabaseRepresentationData databaseRepresentationData = new DatabaseRepresentationData();
 						HashMap<Integer, DatabaseConnection> databaseRow = new HashMap<Integer, DatabaseConnection>();
 						databaseRow.put(databaseConnection.id, databaseConnection);
 						databaseRepresentationData.setConnectionsRows(databaseRow);
 						controlPanelService.setDatabaseRepresentationData(databaseRepresentationData, new AsyncCallback<Void>()
 						{
 							@Override
 							public void onFailure(Throwable caught)
 							{
 								SC.say("Failed to save database data");
 							}

 							@Override
 							public void onSuccess(Void result)
 							{
 								SC.say("Data saved!");
 							}
 						});
                   }
 				}); 
 				
 				layoutExpansionButtons.addMember(buttonSave);
 				layoutExpansion.addMember(form);
 				layoutExpansion.addMember(layoutExpansionButtons);
 				
 				return layoutExpansion;
             }  
        };
        
        gridConnections.setCanExpandRecords(true);
        gridConnections.setAutoFetchData(true); 
        gridConnections.setShowRecordComponents(true);
        gridConnections.setShowRecordComponentsByCell(true);
        DataSource gridConnectionsdataSource = new DataSource();
        DataSourceTextField gridConnectionsDataSourceFieldId = new DataSourceTextField("id");
        DataSourceTextField gridConnectionsDataSourceFieldName = new DataSourceTextField("name", "Name");  
		DataSourceTextField gridConnectionsDataSourceFieldDescription = new DataSourceTextField("description", "Description");
		DataSourceTextField gridConnectionsDataSourceFieldServer = new DataSourceTextField("server", "Server");
		DataSourceTextField gridConnectionsDataSourceFieldDatabasename = new DataSourceTextField("databasename", "Databasename");
		gridConnectionsdataSource.setFields(gridConnectionsDataSourceFieldId, gridConnectionsDataSourceFieldName, gridConnectionsDataSourceFieldDescription, gridConnectionsDataSourceFieldServer, gridConnectionsDataSourceFieldDatabasename);
		List<ListGridRecord> gridConnectionsRecords = new ArrayList<ListGridRecord>();
		for (Map.Entry<Integer, DatabaseConnection> entry : this._databaseRepresentationData.getConnectionsRows().entrySet())
		{
			ListGridRecord record = new ListGridRecord();
			record.setAttribute("id", entry.getKey());
			record.setAttribute("name", entry.getValue().name);
			record.setAttribute("description", entry.getValue().description);
			record.setAttribute("server", entry.getValue().server);
			record.setAttribute("databasename", entry.getValue().databasename);
			gridConnectionsRecords.add(record);
		}
		gridConnectionsdataSource.setTestData(gridConnectionsRecords.toArray(new ListGridRecord[gridConnectionsRecords.size()]));
		gridConnectionsdataSource.setClientOnly(true);
		gridConnections.setShowAllRecords(true);  
		gridConnections.setDataSource(gridConnectionsdataSource);
		
		ImgButton buttonConnectionAdd = new ImgButton(); 
	    buttonConnectionAdd.setShowDown(false);  
	    buttonConnectionAdd.setShowRollOver(false); 
    	buttonConnectionAdd.setAlign(Alignment.CENTER);  
    	buttonConnectionAdd.setSrc("controlpanel/1396472257_plus_32.png");  
    	buttonConnectionAdd.setPrompt("Add server data row");  
    	buttonConnectionAdd.setHeight(16);  
    	buttonConnectionAdd.setWidth(16);  
		buttonConnectionAdd.addClickHandler(new ClickHandler() 
		{  
            public void onClick(ClickEvent event) 
            {
            	_mainWindow.loadingWindow.start("Create new dataset...<br />", 1);
            	DatabaseRepresentationData databaseRepresentationData = new DatabaseRepresentationData();
            	databaseRepresentationData.setConnectionsRows(new HashMap<Integer, DatabaseConnection>());
            	controlPanelService.addDatabaseRepresentationData(databaseRepresentationData, new AsyncCallback<Void>()
				{
					@Override
					public void onFailure(Throwable caught) 
					{
						_mainWindow.loadingWindow.stop();
						SC.say("Can't create new dataset");
					}

					@Override
					public void onSuccess(Void result)
					{
						_mainWindow.loadingWindow.stop();
						_mainWindow.loadingWindow.start("Access database...<br />", 1);
						
						controlPanelService.getDatabaseRepresentationData(new AsyncCallback<DatabaseRepresentationData>()
						{
							@Override
							public void onFailure(Throwable caught)
							{
								_mainWindow.loadingWindow.stop();
							}

							@Override
							public void onSuccess(DatabaseRepresentationData result)
							{
								_databaseRepresentationData = result;
								_tabGeneral();
								_mainWindow.loadingWindow.stop();
							}
						}); 
					}
				});
            }
		});
		
		ImgButton buttonConnectionRemove = new ImgButton(); 
	    buttonConnectionRemove.setShowDown(false);  
	    buttonConnectionRemove.setShowRollOver(false); 
    	buttonConnectionRemove.setAlign(Alignment.CENTER);  
    	buttonConnectionRemove.setSrc("controlpanel/1396472361_delete_32.png");  
    	buttonConnectionRemove.setPrompt("Delete selected server configuration row");  
    	buttonConnectionRemove.setHeight(16);  
    	buttonConnectionRemove.setWidth(16);  
		buttonConnectionRemove.addClickHandler(new ClickHandler() 
		{  
            public void onClick(ClickEvent event) 
            {
            	SC.ask
            	(
            		"Delete server data",
            		"Do you really want to delete " + gridConnections.getSelectedRecord().getAttribute("name") + "?",
            		new BooleanCallback()
            		{
						@Override
						public void execute(Boolean value)
						{
							if (value)
							{
								_mainWindow.loadingWindow.start("Delete server configuration...<br />", 1);
								
								DatabaseRepresentationData databaseRepresentationData = new DatabaseRepresentationData();
				            	DatabaseConnection databaseConnection = new DatabaseConnection();
				            	databaseConnection.id = Integer.parseInt(gridConnections.getSelectedRecord().getAttribute("id"));
				            	HashMap<Integer, DatabaseConnection> databaseRow = new HashMap<Integer, DatabaseConnection>();
				            	databaseRow.put(databaseConnection.id, databaseConnection);
				            	databaseRepresentationData.setConnectionsRows(databaseRow);
				            	
								controlPanelService.removeDatabaseRepresentationData(databaseRepresentationData, new AsyncCallback<Void>()
								{
									@Override
									public void onFailure(Throwable caught) 
									{
										_mainWindow.loadingWindow.stop();
										SC.say("Deletion failed");
									}

									@Override
									public void onSuccess(Void result)
									{
										_mainWindow.loadingWindow.stop();
										_mainWindow.loadingWindow.start("Access database...<br />", 1);
										controlPanelService.getDatabaseRepresentationData(new AsyncCallback<DatabaseRepresentationData>()
										{
											@Override
											public void onFailure(Throwable caught)
											{
												_mainWindow.loadingWindow.stop();
											}

											@Override
											public void onSuccess(DatabaseRepresentationData result)
											{
												_databaseRepresentationData = result;
												_tabGeneral();
												_mainWindow.loadingWindow.stop();
											}
										}); 
									}
								});
							}
						}
            			
            		}
            	); 
            }
		});
		
		// analysis representation
 		Label labelAnalysisRepresentation = new Label("Available analysis for the frontend");
 		labelAnalysisRepresentation.setStyleName("controlPanelHeaders");
 		labelAnalysisRepresentation.setAutoHeight();
 		labelAnalysisRepresentation.setWidth(250);
 		
 		final ListGrid gridAnalysisRepresentation = new ListGrid()
 		{  
             @Override  
             protected Canvas getExpansionComponent(final ListGridRecord record)
             {
             	VLayout layoutExpansion = new VLayout();
             	HLayout layoutExpansionButtons = new HLayout();
             	
             	DatabaseConfigurationAnalysisRepresentation databaseConfigurationAnalysisRepresentation = 
             			_databaseRepresentationData.getAnalysisRepresentationRows().get(Integer.parseInt(record.getAttribute("id")));
             	final DynamicForm form = new DynamicForm();
 				form.setWidth100();
 				form.setColWidths("50%", "50%");
 				final TextItem textFieldId = new TextItem();
 				textFieldId.setTitle("id");
 				textFieldId.setValue(databaseConfigurationAnalysisRepresentation.getId());
 				final TextItem textFieldAnalysisId = new TextItem();
 				textFieldAnalysisId.setTitle("Analysis id");
 				textFieldAnalysisId.setValue(databaseConfigurationAnalysisRepresentation.getAnalysisId());
 				final TextItem textFieldAnalysisTitleOverwrite = new TextItem();
 				textFieldAnalysisTitleOverwrite.setTitle("Analysis title overwrite");
 				textFieldAnalysisTitleOverwrite.setValue(databaseConfigurationAnalysisRepresentation.getTitleAnalysisOverwrite());
 				final TextItem textFieldAnalysisDescriptionOverwrite = new TextItem();
 				textFieldAnalysisDescriptionOverwrite.setTitle("Analysis Description overwrite");
 				textFieldAnalysisDescriptionOverwrite.setValue(databaseConfigurationAnalysisRepresentation.getDescriptionAnalysisOverwrite());
 				final TextItem textFieldRawdataTitleOverwrite = new TextItem();
 				textFieldRawdataTitleOverwrite.setTitle("Rawdata title overwrite");
 				textFieldRawdataTitleOverwrite.setValue(databaseConfigurationAnalysisRepresentation.getTitleRawdataOverwrite());
 				final TextItem textFieldRawdataDescriptionOverwrite = new TextItem();
 				textFieldRawdataDescriptionOverwrite.setTitle("Rawdata description overwrite");
 				textFieldRawdataDescriptionOverwrite.setValue(databaseConfigurationAnalysisRepresentation.getDescriptionRawdataOverwrite());
 				form.setFields
 				(
 						textFieldId, textFieldAnalysisId, textFieldAnalysisTitleOverwrite,
 						textFieldAnalysisDescriptionOverwrite, textFieldRawdataTitleOverwrite,
 						textFieldRawdataDescriptionOverwrite
 				);

 				IButton buttonSave = new IButton("Save");  
 				buttonSave.addClickHandler(new ClickHandler() 
 				{
 					public void onClick(ClickEvent event)
 					{
 						DatabaseConfigurationAnalysisRepresentation databaseConfigurationAnalysisRepresentation = new DatabaseConfigurationAnalysisRepresentation();
 						databaseConfigurationAnalysisRepresentation.setAnalysisId(Integer.parseInt(textFieldAnalysisId.getValueAsString()));
 						databaseConfigurationAnalysisRepresentation.setTitleAnalysisOverwrite(textFieldAnalysisTitleOverwrite.getValueAsString());
 						databaseConfigurationAnalysisRepresentation.setDescriptionAnalysisOverwrite(textFieldAnalysisDescriptionOverwrite.getValueAsString());
 						databaseConfigurationAnalysisRepresentation.setTitleRawdataOverwrite(textFieldRawdataTitleOverwrite.getValueAsString());
 						databaseConfigurationAnalysisRepresentation.setDescriptionRawdataOverwrite(textFieldRawdataDescriptionOverwrite.getValueAsString());
 						databaseConfigurationAnalysisRepresentation.setId(Integer.parseInt(textFieldId.getValueAsString()));
 						DatabaseRepresentationData databaseRepresentationData = new DatabaseRepresentationData();
 						HashMap<Integer, DatabaseConfigurationAnalysisRepresentation> databaseRow = new HashMap<Integer, DatabaseConfigurationAnalysisRepresentation>();
 						databaseRow.put(databaseConfigurationAnalysisRepresentation.getId(), databaseConfigurationAnalysisRepresentation);
 						databaseRepresentationData.setAnalysisRepresentationRows(databaseRow);
 						controlPanelService.setDatabaseRepresentationData(databaseRepresentationData, new AsyncCallback<Void>()
 						{
 							@Override
 							public void onFailure(Throwable caught)
 							{
 								SC.say("Failed to save database data");
 							}

 							@Override
 							public void onSuccess(Void result)
 							{
 								SC.say("Data saved!");
 							}
 						});
                   }
 				}); 
 				
 				layoutExpansionButtons.addMember(buttonSave);
 				layoutExpansion.addMember(form);
 				layoutExpansion.addMember(layoutExpansionButtons);
 				
 				return layoutExpansion;
             }  
        };
        
        gridAnalysisRepresentation.setCanExpandRecords(true);
        gridAnalysisRepresentation.setAutoFetchData(true); 
        gridAnalysisRepresentation.setShowRecordComponents(true);
        gridAnalysisRepresentation.setShowRecordComponentsByCell(true);
        DataSource gridAnalysisRepresentationdataSource = new DataSource();
        DataSourceTextField gridAnalysisRepresentationDataSourceFieldId = new DataSourceTextField("id");
        DataSourceTextField gridAnalysisRepresentationDataSourceFieldAnalysisId = new DataSourceTextField("analysisId", "Analysis id");  
		DataSourceTextField gridAnalysisRepresentationDataSourceFieldAnalysisTitleOverwrite = new DataSourceTextField("analysistitleoverwrite", "Analysis title overwrite");
		DataSourceTextField gridAnalysisRepresentationDataSourceFieldAnalysisDescriptionOverwrite = new DataSourceTextField("analysisdescriptionoverwrite", "Analysis description overwrite");
		DataSourceTextField gridAnalysisRepresentationDataSourceFieldRawdataTitleOverwrite = new DataSourceTextField("rawdatatitleoverwrite", "Rawdata title overwrite");
		DataSourceTextField gridAnalysisRepresentationDataSourceFieldRawdataDescriptionOverwrite = new DataSourceTextField("rawdatadescriptionoverwrite", "Rawdata description overwrite");
		gridAnalysisRepresentationdataSource.setFields
		(
			gridAnalysisRepresentationDataSourceFieldId, gridAnalysisRepresentationDataSourceFieldAnalysisId,
			gridAnalysisRepresentationDataSourceFieldAnalysisTitleOverwrite, gridAnalysisRepresentationDataSourceFieldAnalysisDescriptionOverwrite,
			gridAnalysisRepresentationDataSourceFieldRawdataTitleOverwrite, gridAnalysisRepresentationDataSourceFieldRawdataDescriptionOverwrite
		);
		List<ListGridRecord> gridAnalysisRepresentationRecords = new ArrayList<ListGridRecord>();
		for (Map.Entry<Integer, DatabaseConfigurationAnalysisRepresentation> entry : this._databaseRepresentationData.getAnalysisRepresentationRows().entrySet())
		{
			ListGridRecord record = new ListGridRecord();
			record.setAttribute("id", entry.getKey());
			record.setAttribute("analysisId", entry.getValue().getAnalysisId());
			record.setAttribute("analysistitleoverwrite", entry.getValue().getTitleAnalysisOverwrite());
			record.setAttribute("analysisdescriptionoverwrite", entry.getValue().getDescriptionAnalysisOverwrite());
			record.setAttribute("rawdatatitleoverwrite", entry.getValue().getTitleRawdataOverwrite());
			record.setAttribute("rawdatadescriptionoverwrite", entry.getValue().getDescriptionRawdataOverwrite());
			
			gridAnalysisRepresentationRecords.add(record);
		}
		gridAnalysisRepresentationdataSource.setTestData(gridAnalysisRepresentationRecords.toArray(new ListGridRecord[gridAnalysisRepresentationRecords.size()]));
		gridAnalysisRepresentationdataSource.setClientOnly(true);
		gridAnalysisRepresentation.setShowAllRecords(true);  
		gridAnalysisRepresentation.setDataSource(gridAnalysisRepresentationdataSource);
		
		ImgButton buttonAnalysisRepresentationAdd = new ImgButton(); 
	    buttonAnalysisRepresentationAdd.setShowDown(false);  
	    buttonAnalysisRepresentationAdd.setShowRollOver(false); 
    	buttonAnalysisRepresentationAdd.setAlign(Alignment.CENTER);  
    	buttonAnalysisRepresentationAdd.setSrc("controlpanel/1396472257_plus_32.png");  
    	buttonAnalysisRepresentationAdd.setPrompt("Add analysis for the frontend");  
    	buttonAnalysisRepresentationAdd.setHeight(16);  
    	buttonAnalysisRepresentationAdd.setWidth(16);  
		buttonAnalysisRepresentationAdd.addClickHandler(new ClickHandler() 
		{  
            public void onClick(ClickEvent event) 
            {
            	_mainWindow.loadingWindow.start("Create new dataset...<br />", 1);
            	DatabaseRepresentationData databaseRepresentationData = new DatabaseRepresentationData();
            	databaseRepresentationData.setAnalysisRepresentationRows(new HashMap<Integer, DatabaseConfigurationAnalysisRepresentation>());
            	controlPanelService.addDatabaseRepresentationData(databaseRepresentationData, new AsyncCallback<Void>()
				{
					@Override
					public void onFailure(Throwable caught) 
					{
						_mainWindow.loadingWindow.stop();
						SC.say("Can't create new dataset");
					}

					@Override
					public void onSuccess(Void result)
					{
						_mainWindow.loadingWindow.stop();
						_mainWindow.loadingWindow.start("Access database...<br />", 1);
						
						controlPanelService.getDatabaseRepresentationData(new AsyncCallback<DatabaseRepresentationData>()
						{
							@Override
							public void onFailure(Throwable caught)
							{
								_mainWindow.loadingWindow.stop();
							}

							@Override
							public void onSuccess(DatabaseRepresentationData result)
							{
								_databaseRepresentationData = result;
								_tabGeneral();
								_mainWindow.loadingWindow.stop();
							}
						}); 
					}
				});
            }
		});
		
		ImgButton buttonAnalysisRepresentationRemove = new ImgButton(); 
	    buttonAnalysisRepresentationRemove.setShowDown(false);  
	    buttonAnalysisRepresentationRemove.setShowRollOver(false); 
    	buttonAnalysisRepresentationRemove.setAlign(Alignment.CENTER);  
    	buttonAnalysisRepresentationRemove.setSrc("controlpanel/1396472361_delete_32.png");  
    	buttonAnalysisRepresentationRemove.setPrompt("Delete selected analysis frontend row");  
    	buttonAnalysisRepresentationRemove.setHeight(16);  
    	buttonAnalysisRepresentationRemove.setWidth(16);  
		buttonAnalysisRepresentationRemove.addClickHandler(new ClickHandler() 
		{  
            public void onClick(ClickEvent event) 
            {
            	SC.ask
            	(
            		"Delete analysis frontend",
            		"Do you really want to delete " + gridAnalysisRepresentation.getSelectedRecord().getAttribute("id") + "?",
            		new BooleanCallback()
            		{
						@Override
						public void execute(Boolean value)
						{
							if (value)
							{
								_mainWindow.loadingWindow.start("Delete analysis configuration...<br />", 1);
								
								DatabaseRepresentationData databaseRepresentationData = new DatabaseRepresentationData();
				            	DatabaseConfigurationAnalysisRepresentation databaseConfigurationAnalysisRepresentation = new DatabaseConfigurationAnalysisRepresentation();
				            	databaseConfigurationAnalysisRepresentation.setId(Integer.parseInt(gridAnalysisRepresentation.getSelectedRecord().getAttribute("id")));
				            	HashMap<Integer, DatabaseConfigurationAnalysisRepresentation> databaseRow = new HashMap<Integer, DatabaseConfigurationAnalysisRepresentation>();
				            	databaseRow.put(databaseConfigurationAnalysisRepresentation.getId(), databaseConfigurationAnalysisRepresentation);
				            	databaseRepresentationData.setAnalysisRepresentationRows(databaseRow);
				            	
								controlPanelService.removeDatabaseRepresentationData(databaseRepresentationData, new AsyncCallback<Void>()
								{
									@Override
									public void onFailure(Throwable caught) 
									{
										_mainWindow.loadingWindow.stop();
										SC.say("Deletion failed");
									}

									@Override
									public void onSuccess(Void result)
									{
										_mainWindow.loadingWindow.stop();
										_mainWindow.loadingWindow.start("Access database...<br />", 1);
										controlPanelService.getDatabaseRepresentationData(new AsyncCallback<DatabaseRepresentationData>()
										{
											@Override
											public void onFailure(Throwable caught)
											{
												_mainWindow.loadingWindow.stop();
											}

											@Override
											public void onSuccess(DatabaseRepresentationData result)
											{
												_databaseRepresentationData = result;
												_tabGeneral();
												_mainWindow.loadingWindow.stop();
											}
										}); 
									}
								});
							}
						}
            			
            		}
            	); 
            }
		});
		
		layoutAnalysisLabelButtons.addMember(labelAnalysis);
		layoutAnalysisButtons.addMember(buttonAnalysisAdd);
		layoutAnalysisButtons.addMember(buttonAnalysisRemove);
		layoutAnalysisLabelButtons.addMember(layoutAnalysisButtons);
		layoutAnalysisLabelButtons.setAutoHeight();
		layoutAnalysis.addMember(layoutAnalysisLabelButtons);
		layoutAnalysis.addMember(gridAnalysis);
		
		layoutRawdataLabelButtons.addMember(labelRawdata);
		layoutRawdataButtons.addMember(buttonRawdataAdd);
		layoutRawdataButtons.addMember(buttonRawdataRemove);
		layoutRawdataLabelButtons.addMember(layoutRawdataButtons);
		layoutRawdataLabelButtons.setAutoHeight();
		layoutRawdata.addMember(layoutRawdataLabelButtons);
		layoutRawdata.addMember(gridRawdata);
		
		layoutAnalysisRawdata.addMember(layoutAnalysis);
		layoutAnalysisRawdata.addMember(layoutRawdata);
		
		layoutConnectionsLabelButtons.addMember(labelConnections);
		layoutConnectionsLabelButtons.addMember(buttonConnectionAdd);
		layoutConnectionsLabelButtons.addMember(buttonConnectionRemove);
		layoutConnectionsLabelButtons.setAutoHeight();
		layoutConnections.addMember(layoutConnectionsLabelButtons);
		layoutConnections.addMember(gridConnections);
		
		layoutAnalysisRepresentationLabelButtons.addMember(labelAnalysisRepresentation);
		layoutAnalysisRepresentationLabelButtons.addMember(buttonAnalysisRepresentationAdd);
		layoutAnalysisRepresentationLabelButtons.addMember(buttonAnalysisRepresentationRemove);
		layoutAnalysisRepresentationLabelButtons.setAutoHeight();
		layoutAnalysisRepresentation.addMember(layoutAnalysisRepresentationLabelButtons);
		layoutAnalysisRepresentation.addMember(gridAnalysisRepresentation);
		
		layoutMain.addMember(layoutAnalysisRawdata);
		layoutMain.addMember(layoutConnections);
		layoutMain.addMember(layoutAnalysisRepresentation);
		
		this._tabBar.getTab(0).setPane(layoutMain);
	}
	
	private void _tabToolServer()
	{
		// basic layout container
		VLayout layoutMain = new VLayout();
		
		HLayout layoutOverview = new HLayout();
		HLayout layoutOverviewLabelRefresh = new HLayout();
		VLayout layoutOverviewLabelRefreshList = new VLayout();
		
		VLayout layoutECRunning = new VLayout();
		
		VLayout layoutECTemplates = new VLayout();
		HLayout layoutECTemplatesButtons = new HLayout();
		
		// overview
		Label labelOverview = new Label("DVITA Toolserver Overview");
		labelOverview.setStyleName("controlPanelHeaders");
		labelOverview.setAutoHeight();
		labelOverview.setWidth(220);
		
		ImgButton buttonRefreshTabToolserver = new ImgButton(); 
	    buttonRefreshTabToolserver.setShowDown(false);  
	    buttonRefreshTabToolserver.setShowRollOver(false); 
    	buttonRefreshTabToolserver.setAlign(Alignment.CENTER);  
    	buttonRefreshTabToolserver.setSrc("controlpanel/1396472823_reload.png");  
    	buttonRefreshTabToolserver.setPrompt("Refresh");  
    	buttonRefreshTabToolserver.setHeight(24);  
    	buttonRefreshTabToolserver.setWidth(24);  
		buttonRefreshTabToolserver.addClickHandler(new ClickHandler() 
		{  
            public void onClick(ClickEvent event) 
            {
            	_mainWindow.loadingWindow.start("Receiving Toolserver Data<br />", 1);
				
				// get all the necessary data
				controlPanelService.getServerInformation(new AsyncCallback<HashMap<String,DTSServerInformation>>()
				{
					@Override
					public void onFailure(Throwable caught) 
					{
					}

					@Override
					public void onSuccess(HashMap<String,DTSServerInformation> result)
					{
						if (0 == _dtsData.size())
						{
							SC.say("No toolserver available!");
						}
						else 
						{
							_dtsData = result;
							_tabToolServer();
						}
						
						_mainWindow.loadingWindow.stop();
					}
				});
            }
		});
		
		ListGrid gridOverviewDTS = new ListGrid();
		gridOverviewDTS.setCanExpandRecords(true);
		gridOverviewDTS.setExpansionMode(ExpansionMode.DETAILS);		
		ListGridRecord[] gridOverviewDTSRecords = new ListGridRecord[this._dtsData.size()];
		Integer count = 0;
		for (Map.Entry<String, DTSServerInformation> toolserver : this._dtsData.entrySet())
		{
			ListGridRecord record = new ListGridRecord();
			record.setAttribute("name", toolserver.getValue().getName());
			record.setAttribute("ipport", toolserver.getValue().getIpport());
			record.setAttribute("status", "OKAY");
			record.setAttribute("description", toolserver.getValue().getDescription());
			gridOverviewDTSRecords[count++] = record;
		}

		DataSource gridOverviewDTSdataSource = new DataSource();
		DataSourceTextField gridOverviewDTSDataSourceFieldName = new DataSourceTextField("name", "Name");  
		DataSourceTextField gridOverviewDTSDataSourceFieldIpPort = new DataSourceTextField("ipport", "IP:Port");
		DataSourceTextField gridOverviewDTSDataSourceFieldStatus = new DataSourceTextField("status", "Status");
		DataSourceTextField gridOverviewDTSDataSourceFieldDescription = new DataSourceTextField("description", "Description");
		gridOverviewDTSdataSource.setFields(gridOverviewDTSDataSourceFieldName,gridOverviewDTSDataSourceFieldIpPort,gridOverviewDTSDataSourceFieldStatus,gridOverviewDTSDataSourceFieldDescription);
		gridOverviewDTSdataSource.setTestData(gridOverviewDTSRecords);
		gridOverviewDTSdataSource.setClientOnly(true);
		gridOverviewDTS.setShowAllRecords(true);  
        gridOverviewDTS.setAutoFetchData(true); 
		gridOverviewDTS.setDataSource(gridOverviewDTSdataSource);
		ListGridField gridOverviewDTSFieldName = new ListGridField("name");  
	    ListGridField gridOverviewDTSFieldIpPort = new ListGridField("ipport"); 
	    ListGridField gridOverviewDTSFieldStatus = new ListGridField("status");  
	    gridOverviewDTS.setFields(gridOverviewDTSFieldName,gridOverviewDTSFieldIpPort,gridOverviewDTSFieldStatus);
		
		HTMLPane messageBoxLog = new HTMLPane();  
		messageBoxLog.setShowEdges(true);  
		messageBoxLog.setBackgroundColor("#EFEFFB");
		if (0 < this._dtsData.size())
		{
			messageBoxLog.setContents(this.formatLog(this._dtsData.get("1").getMessageBoxLines()));
		}
		
		messageBoxLog.setContentsType(ContentsType.PAGE);
		
		labelOverview.setLayoutAlign(Alignment.CENTER);
		layoutOverviewLabelRefresh.setHeight("20%");
		layoutOverviewLabelRefresh.addMember(labelOverview);
		layoutOverviewLabelRefresh.addMember(buttonRefreshTabToolserver);
		layoutOverviewLabelRefreshList.addMember(layoutOverviewLabelRefresh);
		layoutOverviewLabelRefreshList.addMember(gridOverviewDTS);
		layoutOverview.addMember(layoutOverviewLabelRefreshList);
		layoutOverview.addMember(messageBoxLog);
		
		// EC Running
		Label labelECRunning = new Label("Running Execution Chains");
		labelECRunning.setStyleName("controlPanelHeaders");
		labelECRunning.setAutoHeight();
		
		ListGrid gridECRunning = _tabToolServerListGridRunningExecutionChains();
		layoutECRunning.addMember(labelECRunning);
		layoutECRunning.addMember(gridECRunning);
		
		// EC Templates
		Label labelECTemplates = new Label("Templates Execution Chains");
		labelECTemplates.setStyleName("controlPanelHeaders");
		labelECTemplates.setAutoHeight();
		
		final ListGrid gridECTemplates = new ListGrid();
		ListGridRecord[] gridECTemplatesRecords = new ListGridRecord[0];
		
		if (0 < this._dtsData.size())
		{
			gridECTemplatesRecords = new ListGridRecord[this._dtsData.get("1").getExecutionChains().size()];
			Integer countExecutionChains = 0;
			for (Map.Entry<String, DTSExecutionChain> entry : this._dtsData.get("1").getExecutionChains().entrySet())
			{
				ListGridRecord record = new ListGridRecord();
				record.setAttribute("name", entry.getValue().getName());
				record.setAttribute("description", entry.getValue().getDescription());
				record.setAttribute("dts", this._dtsData.get("1").getName());
				gridECTemplatesRecords[countExecutionChains++] = record;
			}
		}
		else
		{
			
		}
		
		DataSource gridECTemplatesdataSource = new DataSource();
		DataSourceTextField gridECTemplatesDataSourceFieldName = new DataSourceTextField("name", "Name");  
		DataSourceTextField gridECTemplatesDataSourceFieldDescription = new DataSourceTextField("description", "Description");
		DataSourceTextField gridECTemplatesDataSourceFieldDTS = new DataSourceTextField("dts", "DTS");
		gridECTemplatesdataSource.setFields(gridECTemplatesDataSourceFieldName,gridECTemplatesDataSourceFieldDescription,gridECTemplatesDataSourceFieldDTS);
		gridECTemplatesdataSource.setTestData(gridECTemplatesRecords);
		gridECTemplatesdataSource.setClientOnly(true);
		gridECTemplates.setShowAllRecords(true);  
        gridECTemplates.setAutoFetchData(true); 
		gridECTemplates.setDataSource(gridECTemplatesdataSource);
		ListGridField gridECTemplatesFieldName = new ListGridField("name");  
	    ListGridField gridECTemplatesFieldDescription = new ListGridField("description"); 
	    ListGridField gridECTemplatesFieldDTS = new ListGridField("dts");  
	    gridECTemplates.setFields(gridECTemplatesFieldName,gridECTemplatesFieldDescription,gridECTemplatesFieldDTS);
	    gridECTemplates.addRecordDoubleClickHandler(new RecordDoubleClickHandler()
	    {
			@Override
			public void onRecordDoubleClick(RecordDoubleClickEvent event)
			{
				Window ECTWorkbench = _tabToolServerECTWorkbenchWindow("1", event.getRecord().getAttribute("name"));
                ECTWorkbench.show();
			}
				
		});
		
	    ImgButton buttonECTemplatesNew = new ImgButton(); 
	    buttonECTemplatesNew.setShowDown(false);  
	    buttonECTemplatesNew.setShowRollOver(false); 
    	buttonECTemplatesNew.setAlign(Alignment.CENTER);  
    	buttonECTemplatesNew.setSrc("controlpanel/1396472253_plus_16.png");  
    	buttonECTemplatesNew.setPrompt("New Execution Chain Template");  
    	buttonECTemplatesNew.setHeight(16);  
    	buttonECTemplatesNew.setWidth(16);  
		buttonECTemplatesNew.addClickHandler(new ClickHandler() 
		{  
            public void onClick(ClickEvent event) 
            {
                Window ECTWorkbench = _tabToolServerECTWorkbenchWindow("1", null);
                ECTWorkbench.show();
            }
		});
		
		ImgButton buttonECTemplatesRemove = new ImgButton();
		buttonECTemplatesRemove.setShowDown(false);  
		buttonECTemplatesRemove.setShowRollOver(false); 
    	buttonECTemplatesRemove.setAlign(Alignment.CENTER);  
    	buttonECTemplatesRemove.setSrc("controlpanel/1396472363_delete_16.png");  
    	buttonECTemplatesRemove.setPrompt("Remove Selected Execution Chain");  
    	buttonECTemplatesRemove.setHeight(16);  
    	buttonECTemplatesRemove.setWidth(16);
    	
    	ImgButton buttonECTemplatesInvoke = new ImgButton();
    	buttonECTemplatesInvoke.setShowDown(false);  
    	buttonECTemplatesInvoke.setShowRollOver(false); 
    	buttonECTemplatesInvoke.setAlign(Alignment.CENTER);  
    	buttonECTemplatesInvoke.setSrc("controlpanel/1396535736_paly.png");  
    	buttonECTemplatesInvoke.setPrompt("Invoke Selected Execution Chain");  
    	buttonECTemplatesInvoke.setHeight(16);  
    	buttonECTemplatesInvoke.setWidth(16);
    	
		buttonECTemplatesInvoke.addClickHandler(new ClickHandler() 
		{  
            public void onClick(ClickEvent event) 
            { 
            	if (null == gridECTemplates.getSelectedRecord())
            	{
            		SC.say("no Execution Chain selected!");
            	}
            	else
            	{
            		Window WindowInvokeExecutionChain = tabToolServerWindowInvokeExecutionChain("1", gridECTemplates.getSelectedRecord().getAttribute("name"), null);
                    WindowInvokeExecutionChain.show();
				}
            }
		});
		
		layoutECTemplatesButtons.addMember(buttonECTemplatesNew);
		layoutECTemplatesButtons.addMember(buttonECTemplatesRemove);
		layoutECTemplatesButtons.addMember(buttonECTemplatesInvoke);
		layoutECTemplatesButtons.setAutoHeight();
		
		layoutECTemplates.addMember(labelECTemplates);
		layoutECTemplates.addMember(layoutECTemplatesButtons);
		layoutECTemplates.addMember(gridECTemplates);
		
		// main layout
		layoutMain.addMember(layoutOverview);
		layoutMain.addMember(layoutECRunning);
		layoutMain.addMember(layoutECTemplates);
		
		this._tabBar.getTab(1).setPane(layoutMain);
	}
	
	private ListGrid _tabToolServerListGridRunningExecutionChains()
	{
		ListGrid gridRunningEC = new ListGrid()
		{   
            @Override  
            protected Canvas getExpansionComponent(final ListGridRecord record)
            {  
                VLayout layout = new VLayout();
                
                DTSExecutionChainRunningInformation dtsExecutionChainRunningInformation =  
                		_dtsData.get("1").getExecutionChainsRunningInfo().get(record.getAttribute("id"));

                final ListGrid gridRunningTools = new ListGrid()
                {
                	protected Canvas createRecordComponent(final ListGridRecord record, Integer colNum)
                    {  
                        String fieldName = this.getFieldName(colNum);

                        if (fieldName.equals("viewToolOutput") && !record.getAttribute("status").equalsIgnoreCase("notready")) 
                        {  
                        	ImgButton viewLogImageButton = new ImgButton(); 
                        	viewLogImageButton.setShowDown(false);  
                        	viewLogImageButton.setShowRollOver(false); 
                        	viewLogImageButton.setAlign(Alignment.CENTER);  
                        	viewLogImageButton.setSrc("controlpanel/1396474503_document_pencil.png");  
                        	viewLogImageButton.setPrompt("View Tool Output");  
                        	viewLogImageButton.setHeight(16);  
                        	viewLogImageButton.setWidth(16);  
                        	viewLogImageButton.addClickHandler(new ClickHandler()
                        	{  
                                public void onClick(ClickEvent event)
                                { 
                                	DTSGetRunningToolOutput dtsGetRunningToolOutput = new DTSGetRunningToolOutput();
                                	dtsGetRunningToolOutput.setRunningExecutionChainUID(record.getAttribute("recId"));
                                	dtsGetRunningToolOutput.setRunningToolUID(record.getAttribute("id"));
                                	controlPanelService.getRunningToolOutput
                                		(_dtsData.get("1").getId(), dtsGetRunningToolOutput, new AsyncCallback<DTSReturn<ArrayList<String>>>()
									{
										@Override
										public void onSuccess(DTSReturn<ArrayList<String>> result)
										{
											Window windowShowToolOutput = new Window();
											windowShowToolOutput.setWidth(350);
											windowShowToolOutput.setHeight(500);
											windowShowToolOutput.centerInPage();
											windowShowToolOutput.setTitle("Output");
											HTMLPane htmlPaneShowToolOutput = new HTMLPane(); 
											String toolOutputHtml = "";
											for (String toolOutputLine : result.getReturnValue())
											{
												toolOutputHtml += toolOutputLine + "<br />";
											}
											
											htmlPaneShowToolOutput.setContents(toolOutputHtml);
											htmlPaneShowToolOutput.setContentsType(ContentsType.PAGE);
											windowShowToolOutput.addItem(htmlPaneShowToolOutput);
											windowShowToolOutput.show();
										}
										
										@Override
										public void onFailure(Throwable caught)
										{
											SC.say("Can not receive tool output from server");
										}
									});  
                                }  
                            });
                            return viewLogImageButton;  
                        }
                        else
                        {  
                            return null;  
                        }  
                    }
                };
                gridRunningTools.setAutoFetchData(true); 
                gridRunningTools.setShowRecordComponents(true);
                gridRunningTools.setShowRecordComponentsByCell(true);
                
                DataSource gridRunningToolsdataSource = new DataSource();
                DataSourceTextField gridRunningToolsDataSourceFieldId = new DataSourceTextField("id");
                gridRunningToolsDataSourceFieldId.setHidden(true);
                DataSourceTextField gridRunningToolsDataSourceFieldRECId = new DataSourceTextField("recId");
                gridRunningToolsDataSourceFieldRECId.setHidden(true);
                DataSourceTextField gridRunningToolsDataSourceFieldName = new DataSourceTextField("toolname", "Tool Name");  
        		DataSourceTextField gridRunningToolsDataSourceFieldStatus = new DataSourceTextField("status", "Status");
        		gridRunningToolsdataSource.setFields(gridRunningToolsDataSourceFieldId, gridRunningToolsDataSourceFieldRECId, gridRunningToolsDataSourceFieldName,gridRunningToolsDataSourceFieldStatus);
        		List<ListGridRecord> gridRunningToolsRecords = new ArrayList<ListGridRecord>();
				for (Map.Entry<String, DTSToolRunningInformation> entry : dtsExecutionChainRunningInformation.getRunningTools().entrySet())
				{
					String toolId = DTSExecutionChainUniqueId.fromString(entry.getKey().replace("#", ":")).getId();
					ListGridRecord record2 = new ListGridRecord();
    				record2.setAttribute("id", entry.getKey());
    				record2.setAttribute("recId", record.getAttribute("id"));
    				record2.setAttribute("toolname", _dtsData.get("1").getDTSTools().get(toolId).getToolName());
    				System.out.println(_dtsData.get("1").getDTSTools().get(toolId).getToolName());
    				record2.setAttribute("status", entry.getValue().getStatus());
    				gridRunningToolsRecords.add(record2);
				}
        		gridRunningToolsdataSource.setTestData(gridRunningToolsRecords.toArray(new ListGridRecord[gridRunningToolsRecords.size()]));
        		gridRunningToolsdataSource.setClientOnly(true);
        		gridRunningTools.setShowAllRecords(true);  
        		gridRunningTools.setDataSource(gridRunningToolsdataSource);	
        		ListGridField listGridFieldRunningToolsName = new ListGridField("toolname", "Tool Name");
        		ListGridField listGridFieldRunningToolsStatus = new ListGridField("status", "Status");
        		ListGridField listGridFieldRunningToolsViewLog = new ListGridField("viewToolOutput", "View Tool Output");
        		gridRunningTools.setFields(listGridFieldRunningToolsName, listGridFieldRunningToolsStatus, listGridFieldRunningToolsViewLog);

                layout.addMember(gridRunningTools);

                layout.setHeight((gridRunningToolsRecords.size()*28)+25);
 
                
                return layout;  
            }
            
            protected Canvas createRecordComponent(final ListGridRecord record, Integer colNum)
            {  
                String fieldName = this.getFieldName(colNum);  
  
                if (fieldName.equals("viewLog")) 
                {  
                	ImgButton viewLogImageButton = new ImgButton();
                	viewLogImageButton.setShowDown(false);  
                	viewLogImageButton.setShowRollOver(false); 
                	viewLogImageButton.setShowDown(false);  
                	viewLogImageButton.setShowRollOver(false);  
                	viewLogImageButton.setAlign(Alignment.CENTER);  
                	viewLogImageButton.setSrc("controlpanel/1396472312_clipboard_32.png");  
                	viewLogImageButton.setPrompt("View Log");  
                	viewLogImageButton.setHeight(16);  
                	viewLogImageButton.setWidth(16);  
                	viewLogImageButton.addClickHandler(new ClickHandler()
                	{  
                        public void onClick(ClickEvent event)
                        {  

                        	controlPanelService.getRunningExecutionChainLog
                        		(_dtsData.get("1").getId(), record.getAttribute("id"), new AsyncCallback<DTSReturn<ArrayList<String>>>()
							{
								@Override
								public void onSuccess(DTSReturn<ArrayList<String>> result)
								{
									Window windowShowToolOutput = new Window();
									windowShowToolOutput.setWidth(550);
									windowShowToolOutput.setHeight(500);
									windowShowToolOutput.centerInPage();
									windowShowToolOutput.setTitle("Log");
									HTMLPane htmlPaneShowToolOutput = new HTMLPane(); 
									htmlPaneShowToolOutput.setContents(formatLog(result.getReturnValue()));
									htmlPaneShowToolOutput.setContentsType(ContentsType.PAGE);
									windowShowToolOutput.addItem(htmlPaneShowToolOutput);
									windowShowToolOutput.show();
								}
								
								@Override
								public void onFailure(Throwable caught)
								{
									SC.say("Can not receive tool output from server");
								}
							}); 
                        }  
                    });
                    return viewLogImageButton;  
                }
                else
                {  
                    return null;  
                }  
            }
        };  
        
        gridRunningEC.setCanExpandRecords(true);
        gridRunningEC.setAutoFetchData(true); 
        gridRunningEC.setShowRecordComponents(true);
        gridRunningEC.setShowRecordComponentsByCell(true);
        DataSource gridRunningECdataSource = new DataSource();
        DataSourceTextField gridRunningECDataSourceFieldId = new DataSourceTextField("id");
        gridRunningECDataSourceFieldId.setHidden(true);
        DataSourceTextField gridRunningECDataSourceFieldRECName = new DataSourceTextField("recname", "Name of this run");  
        DataSourceTextField gridRunningECDataSourceFieldECName = new DataSourceTextField("ecname", "Used execution chain");  
		DataSourceTextField gridRunningECDataSourceFieldStatus = new DataSourceTextField("status", "Status");
		DataSourceTextField gridRunningECDataSourceFieldDTS = new DataSourceTextField("dts", "DTS");
		gridRunningECdataSource.setFields(gridRunningECDataSourceFieldId, gridRunningECDataSourceFieldRECName, gridRunningECDataSourceFieldECName, gridRunningECDataSourceFieldStatus, gridRunningECDataSourceFieldDTS);
		List<ListGridRecord> gridRunningECRecords = new ArrayList<ListGridRecord>();
		for (Map.Entry<String, DTSServerInformation> toolserver : this._dtsData.entrySet())
		{
			for (Map.Entry<String, DTSExecutionChainRunningInformation> entry : toolserver.getValue().getExecutionChainsRunningInfo().entrySet())
			{
				ListGridRecord record = new ListGridRecord();
				record.setAttribute("id", entry.getValue().getId());
				record.setAttribute("recname", entry.getValue().getTemplateExecutionChainName());
				record.setAttribute("ecname", entry.getValue().getRunningExecutionChainName());
				record.setAttribute("status", entry.getValue().getStatus());
				record.setAttribute("dts", toolserver.getValue().getName());
				gridRunningECRecords.add(record);
			}
		}
		gridRunningECdataSource.setTestData(gridRunningECRecords.toArray(new ListGridRecord[gridRunningECRecords.size()]));
		gridRunningECdataSource.setClientOnly(true);
		gridRunningEC.setShowAllRecords(true);  
		gridRunningEC.setDataSource(gridRunningECdataSource);
		ListGridField listGridFieldRunningECName = new ListGridField("ecname", "Execution Chain");
		ListGridField listGridFieldRunningECStatus = new ListGridField("status", "Status");
		ListGridField listGridFieldRunningECDTS = new ListGridField("dts", "DTS");
		ListGridField listGridFieldRunningECViewLog = new ListGridField("viewLog", "View Log");
		gridRunningEC.setFields(listGridFieldRunningECName, listGridFieldRunningECStatus, listGridFieldRunningECDTS, listGridFieldRunningECViewLog);
		
		return gridRunningEC;
	}
	
	public Window tabToolServerWindowInvokeExecutionChain(String dtsId, final String dtsExecutionChainName, RequestAnalysis requestAnalysis)
	{
		final Window windowMain = new Window();
		final RequestAnalysis request = requestAnalysis;
		
		controlPanelService.getConfigurationDataAnalysisList(new AsyncCallback<HashMap<String,DatabaseConfigurationAnalysis>>()
		{
			@Override
			public void onFailure(Throwable caught) 
			{
			}
	
			@Override
			public void onSuccess(HashMap<String,DatabaseConfigurationAnalysis> databaseConfigurationAnalysisList)
			{
				_analysisConfigList = databaseConfigurationAnalysisList;

				windowMain.setHeight("30%");
				windowMain.setWidth("40%");
				windowMain.setTitle("Invoke Execution Chain");
				windowMain.setIsModal(true);
				windowMain.centerInPage();
				
				VLayout layoutMain = new VLayout();
				
				Label heading = new Label("Invoke " + dtsExecutionChainName);
				heading.setStyleName("invokeHeading");
				heading.setLeft(20);
				heading.setTop(20);
				heading.setAutoHeight();
				
				final DynamicForm form = new DynamicForm();
				form.setWidth100();
				form.setColWidths("50%", "50%");
				final List<FormItem> invokeInputItems = new ArrayList<FormItem>();
				
				final TextItem textItemName = new TextItem("ecName");
				textItemName.setTitle("Name");
				textItemName.setWidth(300);
				textItemName.setTitleStyle("font-size:11pt");
				invokeInputItems.add(textItemName);
				
				List<DTSExecutionChainConnection> dtsExecutionChainConnections = _dtsData.get("1").getExecutionChains().get(dtsExecutionChainName).getConnections();
				
				for (DTSExecutionChainConnection dtsExecutionChainConnection : dtsExecutionChainConnections)
				{
					if (dtsExecutionChainConnection.getOutputToolUID().getType().equals("customInput"))
					{
						if (dtsExecutionChainConnection.getOutputToolUID().getId().equals("string"))
						{
							final TextItem textField = new TextItem(dtsExecutionChainConnection.getOutputToolUID().toString());
							textField.setWidth(300);
							textField.setTitle
							(
								"String: " 
								+  _dtsData.get("1").getDTSTools().get(dtsExecutionChainConnection.getInputToolUID().getId()).getToolName()
								+ "(" + dtsExecutionChainConnection.getInputDTSIOString() + ")"
							);
							if (null != request)
							{
								if (dtsExecutionChainConnection.getInputDTSIOString().contains("URL"))
								{
									textField.setValue(request.getUrl());
								}
							}
							invokeInputItems.add(textField);
						}
						else if (dtsExecutionChainConnection.getOutputToolUID().getId().equals("sql"))
						{
							final ComboBoxItem comboBoxItem = new ComboBoxItem(dtsExecutionChainConnection.getOutputToolUID().toString());  
							comboBoxItem.setTitle
							(
								"Analysis configuration: " 
								+  _dtsData.get("1").getDTSTools().get(dtsExecutionChainConnection.getInputToolUID().getId()).getToolName()
								+ "(" + dtsExecutionChainConnection.getInputDTSIOString() + ")"
							);
							comboBoxItem.setType("comboBox");  
							LinkedHashMap<String, String> analysisComboBoxStrings = new LinkedHashMap<String, String>();  
							for (Map.Entry<String, DatabaseConfigurationAnalysis> entry : _analysisConfigList.entrySet())
							{
								analysisComboBoxStrings.put(entry.getKey(), entry.getValue().metaTitle);
							}
							 
							comboBoxItem.setValueMap(analysisComboBoxStrings);
							invokeInputItems.add(comboBoxItem);
						}
					}
				}
				
				form.setFields(invokeInputItems.toArray(new TextItem[invokeInputItems.size()]));
				
				Button buttonStart = new Button("Start!");	
				buttonStart.addClickHandler(new ClickHandler() 
				{  
		            public void onClick(ClickEvent event) 
		            {
		            	DTSExecutionChainInvoke dtsExecutionChainInvoke = new DTSExecutionChainInvoke();
		            	dtsExecutionChainInvoke.setTemplateExecutionChainName(dtsExecutionChainName);
		            	
		            	Integer sqlId = null;
		            	
		            	for (FormItem invokeInputItem : invokeInputItems)
		            	{
		            		if (invokeInputItem instanceof ComboBoxItem)
		            		{
		            			IDTSToolsIO dtsToolsIO = new DTSToolsIOSQL();
		    					HashMap<String, String> value = new HashMap<String, String>();
		    					value.put("analysisId", ((ComboBoxItem) invokeInputItem).getValue().toString());
		    					sqlId = Integer.parseInt(value.get("analysisId"));
		    					dtsToolsIO.setData(value);
		    					
		    					dtsExecutionChainInvoke.addCustomInput(((ComboBoxItem) invokeInputItem).getName(), dtsToolsIO);
		            		}
		            		else if (invokeInputItem instanceof TextItem)
		            		{
		            			IDTSToolsIO dtsToolsIO = new DTSToolsIOString();
		    					HashMap<String, String> value = new HashMap<String, String>();
		    					value.put("text", ((TextItem) invokeInputItem).getValueAsString());
		    					dtsToolsIO.setData(value);

			            		dtsExecutionChainInvoke.addCustomInput(((TextItem) invokeInputItem).getName(), dtsToolsIO);
		            		}
		            	}

		            	final Integer analysisId = sqlId;
		            	
		            	dtsExecutionChainInvoke.setRunningExecutionChainName(textItemName.getValueAsString());
		            	
		            	controlPanelService.invokeExecutionChain(_dtsData.get("1").getId(), dtsExecutionChainInvoke, new AsyncCallback<DTSReturn<Void>>()
						{
							@Override
							public void onFailure(Throwable caught) 
							{
								System.out.println(caught);
								_mainWindow.loadingWindow.stop();
								destroy();
							}

							@Override
							public void onSuccess(DTSReturn<Void> result)
							{
								if (null != request)
								{
									request.setStatus(RequestAnalysis.STATUS_RUNNING);
									request.setAnalysisId(analysisId);
									setupService.saveRequestAnalysis(request, new AsyncCallback<Void>()
									{
										@Override
										public void onFailure(Throwable caught){caught.printStackTrace();}
										@Override
										public void onSuccess(Void result){}
									});
								}
								_mainWindow.loadingWindow.stop();
								SC.say(result.getMessage());
								windowMain.destroy();
							}
						});
		            }
				});
				
				layoutMain.addMember(heading);
				layoutMain.addMember(form);
				layoutMain.addMember(buttonStart);
				
				windowMain.addItem(layoutMain);
			}
		});
		
		return windowMain;
	}
	
	private Window _tabToolServerECTWorkbenchWindowSaveExecutionChain()
	{
		final Window windowMain = new Window();
		windowMain.setHeight("15%");
		windowMain.setWidth("20%");
		windowMain.setTitle("Saving Execution Chain Template");
		windowMain.setIsModal(true);
		windowMain.centerInPage();
		
		VLayout layoutMain = new VLayout();
		HLayout layoutButtons = new HLayout();

		final DynamicForm form = new DynamicForm();  
		final TextItem textFieldName = new TextItem("name");  
		textFieldName.setTitle("Name");  
        final TextItem textFieldDesc = new TextItem("description"); 
        textFieldDesc.setTitle("Description");
        form.setFields(textFieldName,textFieldDesc);
        
        Button saveButton = new Button("Save");
        saveButton.addClickHandler(new ClickHandler() 
		{  
			public void onClick(ClickEvent event) 
            {  
				_mainWindow.loadingWindow.start("Saving Execution Chain<br />", 1);

				List<DTSExecutionChainConnection> connections = new ArrayList<DTSExecutionChainConnection>();
				for (String connectionString : _connections.keySet())
				{
					connections.add(DTSExecutionChainConnection.fromString(connectionString));
				}
				
				List<Canvas> workbenchElementsCanvas = Arrays.asList(_workbenchArea.getChildren());
				DTSExecutionChain dtsExecutionChain = new DTSExecutionChain();
				dtsExecutionChain.setName(textFieldName.getValueAsString());
				dtsExecutionChain.setDescription(textFieldDesc.getValueAsString());
				dtsExecutionChain.setConnections(connections);
				
				for (Canvas workbenchElement : workbenchElementsCanvas)
				{
					if (workbenchElement instanceof WorkbenchDNDElement)
					{
						String guiId = ((WorkbenchDNDElement)workbenchElement).getGuiId();
						dtsExecutionChain.addWorkbenchElement(guiId, workbenchElement.getLeft(), workbenchElement.getTop());
					}
				}
				
				controlPanelService.saveExecutionChain(_dtsData.get("1").getId(), dtsExecutionChain, new AsyncCallback<DTSReturn<Void>>()
				{
					@Override
					public void onFailure(Throwable caught) 
					{
						System.out.println(caught);
						_mainWindow.loadingWindow.stop();
						destroy();
					}

					@Override
					public void onSuccess(DTSReturn<Void> result)
					{
						System.out.println(result.getMessage());
						_mainWindow.loadingWindow.stop();
						SC.say(result.getMessage());
						windowMain.destroy();
					}
				});
            }
		});
        layoutButtons.addMember(saveButton);
        layoutMain.addMember(form);
        layoutMain.addMember(layoutButtons);
        windowMain.addItem(layoutMain);
        
        return windowMain;
	}
	
	private Window _tabToolServerECTWorkbenchWindow(String dtsId, String dtsExecutionChainName)
	{
		Window windowMain = new Window();
		windowMain.setHeight("80%");
		windowMain.setWidth("80%");
		windowMain.setTitle("Execution Chain Template Workbench");
		windowMain.setIsModal(true);
		windowMain.centerInPage();
		
		VLayout layoutMain = new VLayout();
		HLayout layoutLists = new HLayout();
		VLayout layoutInputs = new VLayout();
		HLayout layoutInputsButtons = new HLayout();
		VLayout layoutTools = new VLayout();
		HLayout layoutToolsButtons = new HLayout();
		VLayout layoutMainButtons = new VLayout();
		
		// init
		_connections = new HashMap<String, DrawLinePath>();
		_allGuiIds = new ArrayList<String>();
		
		// TODO assume there is just one dts server, and we take him as default
		HashMap<String,DTSTool> dtsTools = this._dtsData.get(dtsId).getDTSTools();
		
		ListGrid gridTools = new ListGrid();
		gridTools.setCanDragRecordsOut(true);
		ListGridField gridToolsFieldName = new ListGridField("name", "Name");
		ListGridField gridToolsFieldId = new ListGridField("id", "");
		gridToolsFieldId.setHidden(true);
		ListGridField gridToolsFieldGridType = new ListGridField("gridType", "");
		gridToolsFieldGridType.setHidden(true);
		gridTools.setFields(gridToolsFieldName,gridToolsFieldId,gridToolsFieldGridType);  
		ListGridRecord[] gridToolsRecords = new ListGridRecord[dtsTools.size()];
		Integer count = 0;

		for (Map.Entry<String,DTSTool> dtsTool : dtsTools.entrySet())
		{
			ListGridRecord gridToolsRecord = new ListGridRecord();
			gridToolsRecord.setAttribute("name", dtsTool.getValue().getToolName());
			gridToolsRecord.setAttribute("id", dtsTool.getKey());
			gridToolsRecord.setAttribute("gridType", "tool");
			gridToolsRecords[count++] = gridToolsRecord;
		}
		
		gridTools.setData(gridToolsRecords);
		
		ListGrid gridInputs = new ListGrid();
		gridInputs.setCanDragRecordsOut(true);
		ListGridField gridInputsFieldType = new ListGridField("type", "Type");  
		ListGridField gridInputsFieldDescription = new ListGridField("description", "Description");  
		gridInputs.setFields(new ListGridField[] {gridInputsFieldType, gridInputsFieldDescription, gridToolsFieldGridType});  
		ListGridRecord[] gridInputsRecords = new ListGridRecord[2];
		ListGridRecord gridInputsRecordString = new ListGridRecord();
		gridInputsRecordString.setAttribute("type", "string");
		gridInputsRecordString.setAttribute("description", "enter some string");
		gridInputsRecordString.setAttribute("gridType", "customInput");
		gridInputsRecords[0] = gridInputsRecordString;
		ListGridRecord gridInputsRecordSQL = new ListGridRecord();
		gridInputsRecordSQL.setAttribute("type", "sql");
		gridInputsRecordSQL.setAttribute("description", "provide database credentials");
		gridInputsRecordSQL.setAttribute("gridType", "customInput");
		gridInputsRecords[1] = gridInputsRecordSQL;
		gridInputs.setData(gridInputsRecords);
		
		Label labelInputs = new Label("Custom Inputs");
		labelInputs.setAutoHeight();
		Button buttonInputAdd = new Button("Add New Input");
		layoutInputsButtons.addMember(buttonInputAdd);
		layoutInputsButtons.setAutoHeight();
		layoutInputs.addMember(labelInputs);
		layoutInputs.addMember(layoutInputsButtons);
		layoutInputs.addMember(gridInputs);
		layoutInputs.setWidth("30%");
		
		Label labelTools = new Label("Available Tools");
		labelTools.setAutoHeight();
		Button buttonToolsDetails = new Button("Show Details");
		layoutToolsButtons.addMember(buttonToolsDetails);
		layoutToolsButtons.setAutoHeight();
		layoutTools.addMember(labelTools);
		layoutTools.addMember(layoutToolsButtons);
		layoutTools.addMember(gridTools);
		
		Button buttonMainSave = new Button("Save Execution Chain Template");
		buttonMainSave.setWidth(200);
		buttonMainSave.addClickHandler(new ClickHandler() 
		{  
			public void onClick(ClickEvent event) 
            {  
				Window saveExecutionChainTemplate = _tabToolServerECTWorkbenchWindowSaveExecutionChain();
				saveExecutionChainTemplate.show();
            }
		});
		layoutMainButtons.addMember(buttonMainSave);

		layoutLists.addMember(layoutInputs);
		layoutLists.addMember(layoutTools);
		layoutLists.addMember(layoutMainButtons);
		layoutLists.setHeight("30%");
		
		Label labelWorkbenchArea = new Label("Workbench");
		labelWorkbenchArea.setAutoHeight();
		
		this._workbenchArea = new WorkbenchArea();
		layoutMain.addMember(labelWorkbenchArea);
		layoutMain.addMember(this._workbenchArea);
		layoutMain.addMember(layoutLists);
		
		windowMain.addItem(layoutMain);
		
		// When a dtsExecutionChain is given, we initialize the ECTWorkbench with it.
		// Otherwise we are done (creating new execution chains)
		if (null != dtsExecutionChainName)
		{
			// TODO IMPORTANT
			// when loaded and displaying a saved workflow, the connections are not displayed
			// the problem are the top values of the inputs and outputs which can not be read here properly
			// in this section i leave some out-commented code to document the approaches to 
			// display the connections
			DTSExecutionChain dtsExecutionChain = this._dtsData.get(dtsId).getExecutionChains().get(dtsExecutionChainName);
			HashMap<String, WorkbenchDNDElement> toolMapping = new HashMap<String, WorkbenchDNDElement>();
			
//			for (String connection : dtsExecutionChain.getConnections())
//			{
//				DrawLinePath drawLinePath = new DrawLinePath();  
//				drawLinePath.setLineWidth(2);
//                drawLinePath.draw();
//                
//		        _workbenchArea.addDrawItem(drawLinePath, true);
//				this._connections.put(connection, drawLinePath);
//			}
			
			for (Map.Entry<String, WorkbenchElementData> workbenchElementData : dtsExecutionChain.getWorkbenchElements().entrySet())
			{
				String guiId = workbenchElementData.getKey();
				String[] guiIdSplit = guiId.split(":");
				String workbenchElementType = guiIdSplit[0];
				String dtsToolId = guiIdSplit[1];
				
				_allGuiIds.add(workbenchElementData.getKey());
				
				if (workbenchElementType.equals("customInput"))
				{
					WorkbenchAreaDNDCustomInput workbenchAreaDNDCustomInput = new WorkbenchAreaDNDCustomInput
							(guiId, dtsToolId, workbenchElementData.getValue().getLeft(), workbenchElementData.getValue().getTop());
					this._workbenchArea.addChild(workbenchAreaDNDCustomInput);
					toolMapping.put(guiId, workbenchAreaDNDCustomInput);
					//workbenchAreaDNDCustomInput.fireEvent(new DragRepositionStopEvent(config));
				}
				else if (workbenchElementType.equals("tool"))
				{
					WorkbenchAreaDNDTool workbenchDNDTool = new WorkbenchAreaDNDTool
							(guiId, dtsToolId, workbenchElementData.getValue().getLeft(), workbenchElementData.getValue().getTop());
					this._workbenchArea.addChild(workbenchDNDTool);
					toolMapping.put(guiId, workbenchDNDTool);
//					workbenchDNDTool.doReposition();
					//workbenchDNDTool.fireEvent(new DragRepositionStopEvent(config));
				}
			}
			DrawLinePath drawLinePath;
            for(Iterator iterator2 = dtsExecutionChain.getConnections().iterator(); iterator2.hasNext(); _workbenchArea.addDrawItem(drawLinePath, true))
            {
                DTSExecutionChainConnection connection = (DTSExecutionChainConnection)iterator2.next();
                String outputComponentGUID = connection.getOutputToolUID().toString();
                String inputComponentGUID = connection.getInputToolUID().toString();
                Logger logger = Logger.getLogger("test");
                double leftOutput = 0.0D;
                double topOutput = 0.0D;
                double leftInput = 0.0D;
                double topInput = 0.0D;
                if(toolMapping.get(outputComponentGUID) instanceof WorkbenchAreaDNDTool)
                {
                    leftOutput = ((WorkbenchAreaDNDTool)toolMapping.get(outputComponentGUID)).getWidth().intValue() + ((WorkbenchAreaDNDTool)toolMapping.get(outputComponentGUID)).getLeft();
                    topOutput = 0.5D * (double)((WorkbenchAreaDNDTool)toolMapping.get(outputComponentGUID)).getHeight().intValue() + (double)((WorkbenchAreaDNDTool)toolMapping.get(outputComponentGUID)).getTop();
                    leftInput = ((WorkbenchAreaDNDTool)toolMapping.get(inputComponentGUID)).getLeft();
                    topInput = 0.5D * (double)((WorkbenchAreaDNDTool)toolMapping.get(inputComponentGUID)).getHeight().intValue() + (double)((WorkbenchAreaDNDTool)toolMapping.get(inputComponentGUID)).getTop();
                } else
                {
                    leftOutput = ((WorkbenchAreaDNDCustomInput)toolMapping.get(outputComponentGUID)).getWidth().intValue() + ((WorkbenchAreaDNDCustomInput)toolMapping.get(outputComponentGUID)).getLeft();
                    topOutput = 0.5D * (double)((WorkbenchAreaDNDCustomInput)toolMapping.get(outputComponentGUID)).getHeight().intValue() + (double)((WorkbenchAreaDNDCustomInput)toolMapping.get(outputComponentGUID)).getTop();
                    leftInput = ((WorkbenchAreaDNDTool)toolMapping.get(inputComponentGUID)).getLeft();
                    topInput = 0.5D * (double)((WorkbenchAreaDNDTool)toolMapping.get(inputComponentGUID)).getHeight().intValue() + (double)((WorkbenchAreaDNDTool)toolMapping.get(inputComponentGUID)).getTop();
                }
                logger.info("getting inside");
                System.out.println((new StringBuilder(String.valueOf(leftOutput))).append(" ").append(topOutput).append(" ").append(leftInput).append(" ").append(topInput).toString());
                drawLinePath = new DrawLinePath();
                drawLinePath.setStartPoint(new Point((int)leftOutput, (int)topOutput));
                drawLinePath.setEndPoint(new Point((int)leftInput, (int)topInput));
                drawLinePath.setLineWidth(2);
                drawLinePath.draw();
            }
			
//			for (DTSExecutionChainConnection connection : dtsExecutionChain.getConnections())
//			{
//				String outputComponentGUID = connection.getOutputToolUID().toString();
//				String inputComponentGUID = connection.getInputToolUID().toString();
//				
//				Integer leftOutput = 0;
//				Integer topOutput = 0;
//				Integer leftInput = 0;
//				Integer topInput = 0;
//				
//				if (toolMapping.get(outputComponentGUID) instanceof WorkbenchAreaDNDTool)
//				{
//					leftOutput = 
//							((WorkbenchAreaDNDTool)toolMapping.get(outputComponentGUID)).getLeft() +
//							((WorkbenchAreaDNDTool)toolMapping.get(outputComponentGUID)).getOffsetWidth();
//					topOutput = ((WorkbenchAreaDNDTool)toolMapping.get(outputComponentGUID)).getTop();
//				}
//				else
//				{
//					leftOutput = 
//							((WorkbenchAreaDNDCustomInput)toolMapping.get(outputComponentGUID)).getLeft() +
//							((WorkbenchAreaDNDCustomInput)toolMapping.get(outputComponentGUID)).getOffsetWidth();
//					topOutput = ((WorkbenchAreaDNDCustomInput)toolMapping.get(outputComponentGUID)).getTop();
//				}
//				
//				
//				WorkbenchAreaDNDInput areaDNDInput = ((WorkbenchAreaDNDTool)toolMapping.get(inputComponentGUID)).getInput(connection.getInputDTSIOString());
//				Double halfTop = 0.5*areaDNDInput.getOffsetHeight();
//				
//				leftInput = ((WorkbenchAreaDNDTool)toolMapping.get(inputComponentGUID)).getLeft();
//				topInput = 
//						((WorkbenchAreaDNDTool)toolMapping.get(inputComponentGUID)).getTop() + 
//						areaDNDInput.getTop() +
//						halfTop.intValue();
//
//				System.out.println("z: "+ ((WorkbenchAreaDNDTool)toolMapping.get(inputComponentGUID)).getTop() + "," + areaDNDInput.getTop() + "," +halfTop.intValue());
//				System.out.println("v: "+ ((WorkbenchAreaDNDTool)toolMapping.get(inputComponentGUID)).getTopIO(connection.getInputDTSIOString()));
//				
//				if (1 == ((WorkbenchAreaDNDTool)toolMapping.get(inputComponentGUID)).getInputs().size())
//				{
//					topInput = ((WorkbenchAreaDNDTool)toolMapping.get(inputComponentGUID)).getTop() + 24;
//				}
//				
//				if (toolMapping.get(outputComponentGUID) instanceof WorkbenchAreaDNDTool)
//				{
//					if (1 == ((WorkbenchAreaDNDTool)toolMapping.get(outputComponentGUID)).getOutputs().size())
//					{
//						topOutput = ((WorkbenchAreaDNDTool)toolMapping.get(outputComponentGUID)).getTop() + 24;
//					}
//				}
//				else
//				{
//					topOutput = ((WorkbenchAreaDNDCustomInput)toolMapping.get(outputComponentGUID)).getTop() + 24;
//				}
////				else if (2 == ((WorkbenchAreaDNDTool)toolMapping.get(inputComponentGUID)).getInputs().size())
////				{
////					if (((WorkbenchAreaDNDTool)toolMapping.get(inputComponentGUID)).getInputs())
////				}
//				
//				DrawLinePath drawLinePath = new DrawLinePath();  
//				drawLinePath.setStartPoint(new Point(leftOutput,topOutput));
//				drawLinePath.setEndPoint(new Point(leftInput,topInput));
//				drawLinePath.setLineWidth(2);
//	            drawLinePath.draw();
//	            
//	            _workbenchArea.addDrawItem(drawLinePath, true);
//		        _connections.put(connection.toString(), drawLinePath);
//			}
				
//			for (String connection : dtsExecutionChain.getConnections())
//			{
//				String[] connectionSplit = connection.split("#");
//				String outputToolGuiId = connectionSplit[0];
//				String outputId = connectionSplit[1];
//				String inputToolGuiId = connectionSplit[2];
//				String inputId = connectionSplit[3];
//				
//				int leftOutput;
//				int topOutput;
//				int leftOutputParent;
//				int topOutputParent;
//				
//				if (toolMapping.get(outputToolGuiId) instanceof WorkbenchAreaDNDTool)
//				{
//					leftOutput = ((WorkbenchAreaDNDTool)toolMapping.get(outputToolGuiId)).getOffsetWidth();
//					leftOutputParent = ((WorkbenchAreaDNDTool) toolMapping.get(outputToolGuiId)).getLeft();
//					topOutputParent = ((WorkbenchAreaDNDTool) toolMapping.get(outputToolGuiId)).getTop();
//				}
//				else 
//				{
//					leftOutput = ((WorkbenchAreaDNDCustomInput)toolMapping.get(outputToolGuiId)).getOffsetWidth();
//					leftOutputParent = ((WorkbenchAreaDNDCustomInput) toolMapping.get(outputToolGuiId)).getLeft();
//					topOutputParent = ((WorkbenchAreaDNDCustomInput) toolMapping.get(outputToolGuiId)).getTop();
//				}
//
//				Double halfTop = 0.5* toolMapping.get(outputToolGuiId).getOutput(outputId).getOffsetHeight();
//				topOutput = toolMapping.get(outputToolGuiId).getOutput(outputId).getTop() + halfTop.intValue();				
//			
//				halfTop = 0.5*toolMapping.get(inputToolGuiId).getInput(inputId).getOffsetHeight();
//				
//				int leftInput = toolMapping.get(inputToolGuiId).getInput(inputId).getLeft();
//				int topInput = toolMapping.get(inputToolGuiId).getInput(inputId).getTop() + halfTop.intValue();
//				int leftInputParent = ((WorkbenchAreaDNDTool) toolMapping.get(inputToolGuiId)).getLeft();
//				int topInputParent = ((WorkbenchAreaDNDTool) toolMapping.get(inputToolGuiId)).getTop();
//
//				System.out.println("k");
//				DrawLinePath drawLinePath = new DrawLinePath();  
//				drawLinePath.setStartPoint(new Point(leftOutput+leftOutputParent,topOutput+topOutputParent));
//				drawLinePath.setEndPoint(new Point(leftInput + leftInputParent,topInput + topInputParent));
//				drawLinePath.setLineWidth(2);
//	            drawLinePath.draw();
//	            
//		        _workbenchArea.addDrawItem(drawLinePath, true);
//		        _connections.put(connection, drawLinePath);
//			}
		}
		
		return windowMain;
	}
	
	private class WorkbenchArea extends DrawPane
	{		
		public WorkbenchArea()
		{
			this.setWidth100();
			this.setHeight("50%");
			this.setShowEdges(true);  
			this.setEdgeSize(6); 
			this.setCanAcceptDrop(true);
			
			this.addDropHandler(new DropHandler()
			{  
                public void onDrop(DropEvent event)
                {
                	int offsetX = getOffsetX() - 15 - getEdgeSize();
                	int offsetY = getOffsetY() - 15 - getEdgeSize();
                	
                	if (EventHandler.getDragTarget() instanceof ListGrid)
                	{
                		ListGrid gridTools = (ListGrid) EventHandler.getDragTarget();
                    	ListGridRecord gridToolsSelection = gridTools.getSelectedRecord();

                    	if (gridToolsSelection.getAttribute("gridType").equals("tool"))
                    	{
                    		String dtsToolId = gridToolsSelection.getAttribute("id");
                        	String guiId = "tool:" + dtsToolId + ":" + Integer.toString(Random.nextInt());
                        	
                        	while (_allGuiIds.contains(guiId))
                        	{
                        		guiId = "tool:" + dtsToolId + ":" + Integer.toString(Random.nextInt());
                        	}
                        	
                        	_allGuiIds.add(guiId);
                        	
                    		WorkbenchAreaDNDTool dndTool = new WorkbenchAreaDNDTool(guiId, dtsToolId, offsetX, offsetY);
                    		addChild(dndTool); 
                    	}
                    	else
                    	{
                    		// custom input id is just it's type
                    		String customInputId = gridToolsSelection.getAttribute("type");
                        	String guiId = "customInput:" + customInputId + ":" + Integer.toString(Random.nextInt());
                        	
                        	while (_allGuiIds.contains(guiId))
                        	{
                        		guiId = "customInput:" + customInputId + ":" + Integer.toString(Random.nextInt());
                        	}
                        	
                        	_allGuiIds.add(guiId);
                        	
                    		WorkbenchAreaDNDCustomInput dndCustomInput = new WorkbenchAreaDNDCustomInput(guiId, customInputId, offsetX, offsetY);
                    		addChild(dndCustomInput);
                    	}
                    	System.out.println("test");
                	}
                }  
            });
		}
	}
	
	private class WorkbenchAreaDNDInput extends Canvas
	{
		private String _id = null;
		
		public WorkbenchAreaDNDInput(String id, String label, int marginLeft, int marginTop)
		{
			this._id = id;
			
			this.setWidth("100px");
			this.setHeight("20px");
			this.setBorder("1px solid red");
			
			Label l = new Label(label);
			l.setAutoHeight();
			this.addChild(l);
			this.setCanAcceptDrop(true);
			this.setCanDragReposition(false);
			this.setCanDrag(false);

			this.addDropOverHandler(new DropOverHandler()
			{  
                public void onDropOver(DropOverEvent event)
                {  
                	setBackgroundColor("#ffff80");  
                }  
            }); 
			
			this.addDropOutHandler(new DropOutHandler()
			{  
                public void onDropOut(DropOutEvent event)
                {  
                	setBackgroundColor("#ffffff");  
                }  
            });  
			
			this.addDropHandler(new DropHandler()
			{
				public void onDrop(DropEvent event)
                {
					int leftOutput;
					int topOutput;
					int leftOutputParent;
					int topOutputParent;
					
					int leftInput;
					int topInput;
					int leftInputParent;
					int topInputParent;
					
					WorkbenchAreaDNDOutput output = (WorkbenchAreaDNDOutput) EventHandler.getDragTarget();
					String guiId = "";
					
					if (output.getParentElement().getParentElement().getParentElement() instanceof WorkbenchAreaDNDCustomInput)
					{
						WorkbenchAreaDNDCustomInput outputParent = (WorkbenchAreaDNDCustomInput) output.getParentElement().getParentElement().getParentElement();
						leftOutput = outputParent.getOffsetWidth();
						leftOutputParent = outputParent.getLeft();
						topOutputParent = outputParent.getTop();
						guiId = outputParent.getGuiId();
					}
					else
					{
						WorkbenchAreaDNDTool outputParent = (WorkbenchAreaDNDTool) output.getParentElement().getParentElement().getParentElement();
						leftOutput = outputParent.getOffsetWidth();
						leftOutputParent = outputParent.getLeft();
						topOutputParent = outputParent.getTop();
						guiId = outputParent.getGuiId();
					}

					WorkbenchAreaDNDTool inputParent = (WorkbenchAreaDNDTool) getParentElement().getParentElement().getParentElement();
					
					Double halfTop = 0.5*output.getOffsetHeight();
					
					topOutput = output.getTop() + halfTop.intValue();
					System.out.println("a: "+getTop());
					halfTop = 0.5*getOffsetHeight();
					leftInput = getLeft();
					topInput = getTop() + halfTop.intValue();;
					leftInputParent = inputParent.getLeft();
					topInputParent = inputParent.getTop();
					
					DrawLinePath drawLinePath = new DrawLinePath();  
					drawLinePath.setStartPoint(new Point(leftOutput+leftOutputParent,topOutput+topOutputParent));
					drawLinePath.setEndPoint(new Point(leftInput + leftInputParent,topInput + topInputParent));
					drawLinePath.setLineWidth(2);
	                drawLinePath.draw();
	                
			        _workbenchArea.addDrawItem(drawLinePath, true);
			        
			        String conn = (guiId + "#" + output.getId() + "#" + inputParent.getGuiId() + "#" + getId());
			        _connections.put(conn, drawLinePath);
                }
			});
		}
		
		public String getId()
		{
			return this._id;
		}
	}
	
	private class WorkbenchAreaDNDOutput extends Canvas
	{
		private String _id = null;
		
		public WorkbenchAreaDNDOutput(String id, String label, int marginLeft, int marginTop)
		{
			this._id = id;
			
			this.setWidth("100px");
			this.setHeight("20px");
			this.setBorder("1px solid green");

			Label l = new Label(label);
			l.setAutoHeight();
			this.addChild(l);
			this.setCanAcceptDrop(false);
			this.setCanDragReposition(false);
			this.setCanDrag(true);
			this.setCanDrop(true);

			this.setDragAppearance(DragAppearance.OUTLINE);
		}
		
		public String getId()
		{
			return this._id;
		}
	}
	
	private class WorkbenchAreaDNDTool extends Canvas implements WorkbenchDNDElement
	{
		private String _guiId = null;
		private String _dtsToolId = null;
		
		private HashMap<String,WorkbenchAreaDNDInput> _inputs = new HashMap<String,WorkbenchAreaDNDInput>();
		private HashMap<String,WorkbenchAreaDNDOutput> _outputs = new HashMap<String,WorkbenchAreaDNDOutput>();
		
		public WorkbenchAreaDNDTool(String guiId, String dtsToolId, int marginLeft, int marginTop)
		{
			this._guiId = guiId;
			this._dtsToolId = dtsToolId;
			
			this.setWidth("310px");
			this.setHeight("70px");
			this.setCanAcceptDrop(false);
			this.setCanDragReposition(false);
			this.setCanDrag(false);
			this.setDragAppearance(DragAppearance.TARGET);
			this.setKeepInParentRect(true);
			this.setLeft(marginLeft);
			this.setTop(marginTop);
			this.setBorder("1px solid yellow");
			
			DTSTool dtsTool = _dtsData.get("1").getDTSTools().get(dtsToolId);
			Integer numInputs = dtsTool.getInputs().size();
			Integer numOutputs = dtsTool.getOutputs().size();
			Integer guiToolHeight = 70;
			
			if (2 < numInputs || 2 < numOutputs)
			{
				guiToolHeight = Math.max(numInputs,numOutputs)*30;
				this.setHeight(guiToolHeight + "px");
			}
			
			Canvas toolMainBox = new Canvas();
			toolMainBox.setWidth("100px");
			toolMainBox.setHeight(guiToolHeight + "px");
			toolMainBox.setBorder("1px solid blue");
			toolMainBox.setCanDragReposition(true);
			toolMainBox.setDragTarget(this);	
			Label toolLabel = new Label(_dtsData.get("1").getDTSTools().get(this._dtsToolId).getToolName());
			toolLabel.setAutoHeight();
			toolMainBox.addChild(toolLabel);

			HLayout toolLayout = new HLayout();
			VLayout toolLayoutInputs = new VLayout();
			VLayout toolLayoutOutputs = new VLayout();
			
			toolLayoutInputs.setBorder("1px solid green");
			toolLayoutInputs.setAlign(Alignment.CENTER);
			toolLayoutInputs.setMembersMargin(5);
			toolLayoutOutputs.setBorder("1px solid green");
			toolLayoutOutputs.setAlign(Alignment.CENTER);
			toolLayoutOutputs.setMembersMargin(5);
			
			toolLayout.setHeight(guiToolHeight + "px");
			
			for(Map.Entry<String, IDTSToolsIO> entry : dtsTool.getInputs().entrySet())
			{
				WorkbenchAreaDNDInput guiInput = new WorkbenchAreaDNDInput(entry.getKey(), entry.getValue().getName(),  0, 0);
				this._inputs.put(entry.getKey(), guiInput);
				toolLayoutInputs.addMember(guiInput);
			}
			
			for(Map.Entry<String, IDTSToolsIO> entry : dtsTool.getOutputs().entrySet())
			{
				WorkbenchAreaDNDOutput guiOutput = new WorkbenchAreaDNDOutput(entry.getKey(), entry.getValue().getName(),  0, 0);
				this._outputs.put(entry.getKey(), guiOutput);
				toolLayoutOutputs.addMember(guiOutput);
			}
			
			toolLayout.addMember(toolLayoutInputs);
			toolLayout.addMember(toolMainBox);
			toolLayout.addMember(toolLayoutOutputs);

			this.addChild(toolLayout);

			this.addDragRepositionStopHandler(new DragRepositionStopHandler()
			{
				public void onDragRepositionStop(DragRepositionStopEvent event)
				{		
					doReposition();
				}
			});
		}
		
		public void doReposition()
		{
			for (String connectionId : _connections.keySet())
			{
				if (!connectionId.contains(getGuiId()))
				{
					continue;
				}
				
				List<String> connectionIds = Arrays.asList(connectionId.split("#"));
				
				int inputParentLeft = getLeft();
				int inputParentTop = getTop();
				if (connectionIds.get(0).equals(getGuiId()))
				{
					Double halfTop = 0.5*_outputs.get(connectionIds.get(1)).getOffsetHeight();
					
					_connections.get(connectionId).setStartPoint(new Point(inputParentLeft + getOffsetWidth(),_outputs.get(connectionIds.get(1)).getTop()+inputParentTop+halfTop.intValue()));
				}
				else
				{
					Double halfTop = 0.5*_inputs.get(connectionIds.get(3)).getOffsetHeight();
					int inputLeft = _inputs.get(connectionIds.get(3)).getLeft();
					int inputTop = _inputs.get(connectionIds.get(3)).getTop();
					System.out.println("w:"+inputTop);
					_connections.get(connectionId).setEndPoint(new Point(inputLeft+inputParentLeft,inputTop+inputParentTop+halfTop.intValue()));
				}
			}
		}
		
		// TODO see line 2108 why this out-commented code is still here 
//		public HashMap<String,WorkbenchAreaDNDInput> getInputs()
//		{
//			return _inputs;
//		}
//		
//		public HashMap<String,WorkbenchAreaDNDOutput> getOutputs()
//		{
//			return _outputs;
//		}
//		
//		public Integer getTopIO(String inputId)
//		{
//			return _inputs.get(inputId).getTop();
//		}
		
		public WorkbenchAreaDNDInput getInput(String inputId)
		{
			return this._inputs.get(inputId);
		}
		
		public WorkbenchAreaDNDOutput getOutput(String outputId)
		{
			return this._outputs.get(outputId);
		}
		
		public String getGuiId()
		{
			return this._guiId;
		}
	}
	
	private class WorkbenchAreaDNDCustomInput extends Canvas implements WorkbenchDNDElement
	{
		private String _guiId = null;
		private String _customInputId = null;
		
		private HashMap<String,WorkbenchAreaDNDOutput> _outputs = new HashMap<String,WorkbenchAreaDNDOutput>();
		
		public WorkbenchAreaDNDCustomInput(String guiId, String customInputId, int marginLeft, int marginTop)
		{
			this._guiId = guiId;
			this._customInputId = customInputId;
			
			this.setWidth("110px");
			this.setHeight("40px");
			this.setCanAcceptDrop(false);
			this.setCanDragReposition(false);
			this.setCanDrag(false);
			this.setDragAppearance(DragAppearance.TARGET);
			this.setKeepInParentRect(true);
			this.setLeft(marginLeft);
			this.setTop(marginTop);
			this.setBorder("1px solid yellow");
			
			Canvas toolMainBox = new Canvas();
			toolMainBox.setWidth("10px");
			toolMainBox.setHeight("40px");
			toolMainBox.setBorder("1px solid blue");
			toolMainBox.setCanDragReposition(true);
			toolMainBox.setDragTarget(this);	

			HLayout toolLayout = new HLayout();
			VLayout toolLayoutOutputs = new VLayout();
			
			toolLayoutOutputs.setBorder("1px solid green");
			toolLayoutOutputs.setAlign(Alignment.CENTER);
			toolLayoutOutputs.setMembersMargin(5);
			
			toolLayout.setHeight("40px");
			
			WorkbenchAreaDNDOutput guiOutput = new WorkbenchAreaDNDOutput(this._customInputId, this._customInputId, 0, 0);
			this._outputs.put(this._customInputId, guiOutput);
			toolLayoutOutputs.addMember(guiOutput);

			toolLayout.addMember(toolMainBox);
			toolLayout.addMember(toolLayoutOutputs);
			
			this.addChild(toolLayout);

			this.addDragRepositionStopHandler(new DragRepositionStopHandler()
			{
				public void onDragRepositionStop(DragRepositionStopEvent event)
				{		
					for (String connectionId : _connections.keySet())
					{
						if (!connectionId.contains(getGuiId()))
						{
							continue;
						}
						
						List<String> connectionIds = Arrays.asList(connectionId.split("#"));
						
						if (connectionIds.get(0).equals(getGuiId()))
						{
							Double halfTop = 0.5*_outputs.get(connectionIds.get(1)).getOffsetHeight();
							_connections.get(connectionId).setStartPoint(new Point(getLeft() + getOffsetWidth(),_outputs.get(connectionIds.get(1)).getTop()+getTop()+halfTop.intValue()));
						}
					}
				}
			});
		}
		
		public String getGuiId()
		{
			return this._guiId;
		}

		@Override
		public WorkbenchAreaDNDInput getInput(String inputId)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public WorkbenchAreaDNDOutput getOutput(String outputId)
		{
			return this._outputs.get(outputId);
		}
	}
	
	private interface WorkbenchDNDElement
	{
		public String getGuiId();
		public WorkbenchAreaDNDInput getInput(String inputId);
		public WorkbenchAreaDNDOutput getOutput(String outputId);
	}
	
	private String formatLog(List<String> log)
	{
		String formattedLog = "";
		
		for (String line : log)
		{
			if (line.contains("[ERROR]"))
			{
				line = "<span style='color:red'>"+line+"</span>";
			}
			if (line.contains("[WARNING]"))
			{
				line = "<span style='color:#868A08'>"+line+"</span>";
			}
			
			
			formattedLog += line + "<br />";
		}
		
		return formattedLog;
	}
	
	public void setDTSData(HashMap<String,DTSServerInformation> dtsData)
	{
		_dtsData = dtsData;
	}
}
