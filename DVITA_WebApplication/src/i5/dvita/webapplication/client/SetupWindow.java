package i5.dvita.webapplication.client;

import i5.dvita.commons.ACL;
import i5.dvita.commons.DTSExecutionChain;
import i5.dvita.commons.DTSServerInformation;
import i5.dvita.commons.DatabaseCollectionSetup;
import i5.dvita.commons.DatabaseConfigurationAnalysis;
import i5.dvita.commons.DatabaseConfigurationRawdata;
import i5.dvita.commons.RequestAnalysis;
import i5.dvita.webapplication.shared.SetupServiceAuthResult;
import i5.dvita.webapplication.shared.UserShared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.TreeModelType;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickEvent;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeNode;

public class SetupWindow extends Window
{
	SetupWindow THIS = this;

	String buttonText = "Load Selected Topic Model";

	DatabaseConfigurationRawdata configRawdata = new DatabaseConfigurationRawdata();

	private final SetupServiceAsync setupService = GWT.create(SetupService.class);

	private DVITA_WebApplication otherWindow;
	private IButton loadButton;
	private TreeGrid treeGrid;
	
	private UserShared _user = null;
	private VLayout _layoutUserArea = new VLayout();

	private ControlPanelServiceAsync controlPanelService = GWT.create(ControlPanelService.class);
	
	private HashMap<String,DTSServerInformation> _dtsData = null;
	
	public void updateTree(ArrayList<DatabaseCollectionSetup> result)
	{
		Tree theTree = new Tree();  
		theTree.setModelType(TreeModelType.PARENT);  
		theTree.setRootValue(1);  
		theTree.setOpenProperty("isOpen"); 

		TreeNode[] data = new TreeNode[result.size()];
		int databaseNr = 0;
		
		for (DatabaseCollectionSetup setupDataCollection : result) 
		{
			data[databaseNr] = new TreeNode("Database: " + setupDataCollection.databaseConfigurationRawdata.metaTitle);
			data[databaseNr].setAttribute("name","Database: " + setupDataCollection.databaseConfigurationRawdata.metaTitle);
			data[databaseNr].setIcon("database.png");
			data[databaseNr].setAttribute("isOpen",true);
			data[databaseNr].setAttribute("info",setupDataCollection.databaseConfigurationRawdata);
			data[databaseNr].setAttribute("descr",setupDataCollection.databaseConfigurationRawdata.metaDescription);
			data[databaseNr].setAttribute("topics",/*"--"*/ "");		
			data[databaseNr].setCustomStyle(data[databaseNr].getCustomStyle() + ";font-weight:bold");
			
			TreeNode[] childs = new TreeNode[setupDataCollection.databaseConfigurationAnalyses.size()];
			int pos = 0;
			
			for(DatabaseConfigurationAnalysis analysisDatabase : setupDataCollection.databaseConfigurationAnalyses)
			{
				childs[pos] = new TreeNode(/*"Analysis: " + */analysisDatabase.metaTitle);
				childs[pos].setAttribute("name",/*"Analysis: " + */analysisDatabase.metaTitle);
				childs[pos].setIcon("fileopen-icon.gif");
				childs[pos].setAttribute("AnalyzeID",analysisDatabase.id);
				childs[pos].setAttribute("info",analysisDatabase);
				childs[pos].setAttribute("descr",analysisDatabase.metaDescription);
				childs[pos].setAttribute("topics",analysisDatabase.NumberTopics);
				pos++;
			}
			
			data[databaseNr].setChildren(childs);
			databaseNr++;
		}

		theTree.setData(data);  
		treeGrid.setData(theTree);  
		
		SelectionChangedHandler handler1 = new SelectionChangedHandler()
		{
			@Override
			public void onSelectionChanged(SelectionEvent event)
			{
				if(event.getSelectedRecord()== null)
				{
					return;
				}

				if(null != event.getSelectedRecord().getAttributeAsInt("AnalyzeID"))
				{
					loadButton.enable();
					loadButton.setTitle("<b><font color=\"#0000FF\">"+buttonText+"</font></b>");
					
				}
				else
				{
					loadButton.disable();
					loadButton.setTitle(buttonText);
				}
			}
		};
			
		treeGrid.addSelectionChangedHandler(handler1);
	}
	
	private void setLayoutUserArea()
	{
		_layoutUserArea.setMembers();
				
		if (_user.getOperations().contains(ACL.OPERATION_LOGIN))
		{
			setLayoutLogin();
		}
		
		if (_user.getOperations().contains(ACL.OPERATION_REQUEST_ANALYSIS))
		{
			setLayoutRequest();
		}
		
		if (_user.getOperations().contains(ACL.OPERATION_INVOKE_ANALYSIS))
		{
			setLayoutRequestManager();
		}
	}
	
	public SetupWindow(DVITA_WebApplication theotherWindow, boolean initialWindow)
	{
		// get user
		setupService.getUser(new AsyncCallback<UserShared>()
		{
			@Override
			public void onFailure(Throwable caught)
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(UserShared result)
			{
				_user = result;
				setLayoutUserArea();
			}
		});
		
		otherWindow = theotherWindow; // wenn das fenster geschlossen wird, dann soll die topic liste erscheinen
		// daher brauchen wir hier die variable
		
		VLayout layoutMain = new VLayout();
		VLayout layoutAnalysisSelection = new VLayout();
		
		this.setWidth("90%");
		this.setHeight("90%");
		this.setTitle("Select Data Set");
		this.setShowMinimizeButton(false);
		
		if(initialWindow)
		{
			this.setShowCloseButton(false);
		}
		else
		{
			this.setShowCloseButton(true);	
		}
		
		this.setIsModal(true);
		this.setShowModalMask(true);
		this.centerInPage();
		this.addCloseClickHandler(new CloseClickHandler()
		{
			@Override
			public void onCloseClick(CloseClickEvent event)
			{
				// nur wenn es nicht das initiale window ist erlauben wir ein schließen
				THIS.destroy();
				// so müsste man einfach zum alten fenster zurückkehren
			}
		});

		// USER AREA PART
		
		// GUEST (login)
		
		
		
//		IButton buttonGuest = new IButton("Use D-VITA as a guest");  
//		buttonGuest.setWidth(150);
//		buttonGuest.addClickHandler(new ClickHandler() 
//		{
//			public void onClick(ClickEvent event)
//			{
//				SC.say(textFieldOpenId.getValueAsString());
//          }
//		}); 
		
//		layoutMain.addMember(form);
//		layoutMain.addMember(buttonOpenId);
//		layoutMain.addMember(buttonGuest);
		
//		this.addItem(layoutMain);
				
		// ANALYSIS PART
		treeGrid = new TreeGrid();  
		treeGrid.setWidth100();  
		treeGrid.setHeight(THIS.getViewportHeight()-50);  

		treeGrid.setShowOpenIcons(false);  
		treeGrid.setShowDropIcons(false);  
		treeGrid.setClosedIconSuffix("");  
		
		treeGrid.setSelectionAppearance(SelectionAppearance.ROW_STYLE);  
		treeGrid.setSelectionType(SelectionStyle.SINGLE);
		treeGrid.setShowSelectedStyle(true);

		treeGrid.setShowPartialSelection(false);  
		treeGrid.setCascadeSelection(false); 

		ListGridField nameField = new ListGridField("name", "Topic Model");
		ListGridField infoField = new ListGridField("descr","Description");
		ListGridField topicField = new ListGridField("topics","# Topics");
		topicField.setWidth(70);

		treeGrid.setFields(nameField,topicField,infoField);
		setupService.getSetupInformation(new AsyncCallback<ArrayList<DatabaseCollectionSetup>>()
		{
			@Override
			public void onFailure(Throwable caught) 
			{
			}

			@Override
			public void onSuccess(ArrayList<DatabaseCollectionSetup> result)
			{
				updateTree(result);
			}
		});


		loadButton = new IButton(buttonText);
		loadButton.disable();
		loadButton.setAutoFit(true);

		loadButton.addClickHandler(new ClickHandler()
		{  
			public void onClick(ClickEvent event)
			{
				if(0 == treeGrid.getSelectedRecords().length)
				{
					com.google.gwt.user.client.Window.alert("Please select an analysis!");
					return;
				}

				Integer selected = treeGrid.getSelectedRecords()[0].getAttributeAsInt("AnalyzeID");
				
				if(selected == null)
				{
					com.google.gwt.user.client.Window.alert("Please select an analysis (not a database)!");
					return;
				}
				
				THIS.disable();
				/////////////////////////////////////////////////////////////////////////////////
				///////////////////////////////////////////////////////////////////

				setUpSession(selected);
			}

		});
		
		layoutAnalysisSelection.addMember(loadButton);
		layoutAnalysisSelection.addMember(treeGrid);
		
		layoutMain.addMember(_layoutUserArea);
		layoutMain.addMember(layoutAnalysisSelection);
		
		this.addItem(layoutMain);
	}

	private void setLayoutRequestManager()
	{
		final VLayout layoutMain = new VLayout();
		final VLayout layoutRequestList = new VLayout();
		
		setupService.getRequestAnalysis(new AsyncCallback<HashMap<Integer,RequestAnalysis>>()
		{
			@Override
			public void onFailure(Throwable caught)
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(HashMap<Integer, RequestAnalysis> result)
			{
				if (0 < result.size())
				{
					Label labelRequestManager = new Label("Requests list");
					labelRequestManager.setStyleName("anotherHeading");
					labelRequestManager.setLeft(20);
					labelRequestManager.setTop(20);
					labelRequestManager.setAutoHeight();
					layoutMain.addMember(labelRequestManager,0);
				}
				
				for (Map.Entry<Integer, RequestAnalysis> entry : result.entrySet())
				{
					HLayout requestRow = new HLayout();
					final RequestAnalysis requestAnalysis = entry.getValue();
					final Label labelId = new Label(entry.getValue().getId().toString());
					labelId.setWidth(20);
					Label labelUser = new Label(entry.getValue().getUser().getName());
					labelUser.setWidth(100);
					Label labelUrl = new Label(entry.getValue().getUrl());
					labelUrl.setWidth(400);
					Label labelStatus = new Label(entry.getValue().getStatus());
					labelStatus.setWidth(50);
					
					IButton buttonRequest = null;
					
					if (requestAnalysis.getStatus().equalsIgnoreCase(RequestAnalysis.STATUS_PENDING))
					{
						buttonRequest = new IButton("Start!");
						buttonRequest.addClickHandler(new ClickHandler()
						{  
							public void onClick(ClickEvent event)
							{
								otherWindow.loadingWindow.start("Receiving Templates<br />", 1);
								
								// get all the necessary data
								controlPanelService.getServerInformation(new AsyncCallback<HashMap<String,DTSServerInformation>>()
								{
									@Override
									public void onFailure(Throwable caught) 
									{
										otherWindow.loadingWindow.stop();
										SC.say("Fail on DTS communication");
									}

									@Override
									public void onSuccess(HashMap<String,DTSServerInformation> result)
									{
										final Window windowChooseTemplate = new Window();
										windowChooseTemplate.setHeight(110);
										windowChooseTemplate.setWidth(300);
										windowChooseTemplate.setTitle("Choose Template");
										windowChooseTemplate.setIsModal(true);
										windowChooseTemplate.centerInPage();
										
										_dtsData = result;
										
										final DynamicForm form = new DynamicForm();
										form.setWidth100();
										final ComboBoxItem comboBoxItem = new ComboBoxItem();
										comboBoxItem.setTitle("choose template:");
										comboBoxItem.setType("comboBox");  
										LinkedHashMap<String, String> templateComboBoxStrings = new LinkedHashMap<String, String>();  
										for (Map.Entry<String, DTSExecutionChain> entry : _dtsData.get("1").getExecutionChains().entrySet())
										{
											templateComboBoxStrings.put(entry.getKey(), entry.getValue().getName());
										}
										 
										comboBoxItem.setValueMap(templateComboBoxStrings);
										form.setFields(comboBoxItem);
										
										IButton buttonNext = new IButton("Next");
										buttonNext.addClickHandler(new ClickHandler()
										{  
											public void onClick(ClickEvent event)
											{
												ControlPanelWindow windowControlPanel = new ControlPanelWindow(otherWindow);
												windowControlPanel.setDTSData(_dtsData);
												Window WindowInvokeExecutionChain = windowControlPanel.tabToolServerWindowInvokeExecutionChain("1", comboBoxItem.getValueAsString(), requestAnalysis);
							                    WindowInvokeExecutionChain.show();
							                    windowChooseTemplate.destroy();
											}
										});

										windowChooseTemplate.addItem(form);
										windowChooseTemplate.addItem(buttonNext);
										otherWindow.loadingWindow.stop();
										windowChooseTemplate.show();
									}
								});
							}
						});
					}
					else if (requestAnalysis.getStatus().equalsIgnoreCase(RequestAnalysis.STATUS_RUNNING))
					{
						buttonRequest = new IButton("Mark request as finished");
						buttonRequest.setWidth(150);
						buttonRequest.addClickHandler(new ClickHandler()
						{  
							public void onClick(ClickEvent event)
							{
								requestAnalysis.setStatus(RequestAnalysis.STATUS_FINISHED);
								setupService.saveRequestAnalysis(requestAnalysis, new AsyncCallback<Void>()
								{
									@Override
									public void onFailure(Throwable caught){caught.printStackTrace();}
									@Override
									public void onSuccess(Void result){setLayoutUserArea();}
								});
							}
						});
					}

					requestRow.addMembers(labelUser, labelUrl, labelStatus);
					
					if (null != buttonRequest)
					{
						requestRow.addMember(buttonRequest);
					}
					
					layoutRequestList.addMember(requestRow);
				}
			}
		});
		
		layoutMain.addMember(layoutRequestList);
		HTML htmlhr = new HTML("<hr>");
		htmlhr.setHeight("10px");
		layoutMain.addMember(htmlhr);
		_layoutUserArea.addMember(layoutMain);
	}
	
	private void setLayoutRequest()
	{
		VLayout layoutMain = new VLayout();
		HLayout layoutRequest = new HLayout();
		
		final VLayout layoutRequestList = new VLayout();
		
		setupService.getRequestAnalysis(new AsyncCallback<HashMap<Integer,RequestAnalysis>>()
		{
			@Override
			public void onFailure(Throwable caught)
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(HashMap<Integer, RequestAnalysis> result)
			{
				for (Map.Entry<Integer, RequestAnalysis> entry : result.entrySet())
				{
					HLayout requestRow = new HLayout();
					Label labelUrl = new Label(entry.getValue().getUrl());
					labelUrl.setWidth(400);
					Label labelStatus = new Label(entry.getValue().getStatus());
					labelStatus.setWidth(100);
					
					final Integer analysisId = entry.getValue().getAnalysisId();
				
					requestRow.addMembers(labelUrl, labelStatus);
					
					if (entry.getValue().getStatus().equalsIgnoreCase(RequestAnalysis.STATUS_FINISHED))
					{
						IButton buttonShareAnalysis = new IButton("Share!");
						buttonShareAnalysis.addClickHandler(new ClickHandler()
						{  
							public void onClick(ClickEvent event)
							{
								final Window windowShare = new Window();
								windowShare.setHeight(110);
								windowShare.setWidth(300);
								windowShare.setTitle("Share your topic analytics");
								windowShare.setIsModal(true);
								windowShare.centerInPage();
								
								final DynamicForm form = new DynamicForm();
								form.setWidth100();
								
								final TextItem textFieldEmail = new TextItem("email");  
								
								form.setFields(textFieldEmail);
								
								IButton buttonShare = new IButton("share!");
								buttonShare.addClickHandler(new ClickHandler()
								{  
									public void onClick(ClickEvent event)
									{
										setupService.shareRequestedAnalysis(analysisId, textFieldEmail.getValueAsString(), new AsyncCallback<String>()
										{
											@Override
											public void onFailure(Throwable caught)
											{
												 windowShare.destroy();
											}

											@Override
											public void onSuccess(String result)
											{
												if (null == result)
												{
													SC.say("shared!");
												}
												else
												{
													SC.say(result);
												}
												
												windowShare.destroy();
											}
										});
										
										
					                   
									}
								});

								windowShare.addItem(form);
								windowShare.addItem(buttonShare);
								windowShare.show();
							}
						});
						
						requestRow.addMember(buttonShareAnalysis);
					}
					
					
					layoutRequestList.addMember(requestRow);
				}
			}
		});
		
		
		DynamicForm form = new DynamicForm();
		form.setWidth100();
		form.setColWidths("50%", "50%");
		
		final TextItem textFieldRequestAnalysis = new TextItem();
		textFieldRequestAnalysis.setTitle("DBLP Conference URL");
		textFieldRequestAnalysis.setValue("");
		
		form.setFields(textFieldRequestAnalysis);
		
		IButton buttonRequest = new IButton("Request");  
		buttonRequest.addClickHandler(new ClickHandler() 
		{
			public void onClick(ClickEvent event)
			{
				RequestAnalysis requestAnalysis = new RequestAnalysis();
				requestAnalysis.setUrl(textFieldRequestAnalysis.getValueAsString());
				requestAnalysis.setStatus(RequestAnalysis.STATUS_PENDING);
				
				setupService.saveRequestAnalysis(requestAnalysis, new AsyncCallback<Void>()
				{
					@Override
					public void onFailure(Throwable caught)
					{
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onSuccess(Void result)
					{
						// TODO Auto-generated method stub
						SC.say("Request sent!");
						setLayoutUserArea();
					}
				});
			}
		}); 
		
		HTML htmlhr = new HTML("<hr>");
		htmlhr.setHeight("10px");
		
		layoutRequest.setWidth(400);
		layoutRequest.addMember(form);
		layoutRequest.addMember(buttonRequest);
		
		layoutMain.addMember(layoutRequest);
		layoutMain.addMember(layoutRequestList);
		layoutMain.addMember(htmlhr);
		
		_layoutUserArea.addMember(layoutMain);
	}
	
	private void setLayoutLogin()
	{
		VLayout layoutMain = new VLayout();
		HLayout layoutForm = new HLayout();
		
		Label labelLogin = new Label("Login");
		labelLogin.setStyleName("anotherHeading");
		labelLogin.setLeft(20);
		labelLogin.setTop(20);
		labelLogin.setAutoHeight();
		
		DynamicForm form = new DynamicForm();
		form.setWidth100();
		form.setColWidths("50%", "50%");
		
		final TextItem textFieldOpenId = new TextItem();
		textFieldOpenId.setTitle("Your OpenID Identifier");
		textFieldOpenId.setValue("");
		
		form.setFields(textFieldOpenId);
		
		IButton buttonOpenId = new IButton("Login");  
		buttonOpenId.addClickHandler(new ClickHandler() 
		{
			public void onClick(ClickEvent event)
			{
				otherWindow.loadingWindow.start("Contact OpenID provider<br />", 1);
				setupService.openIdAuth(textFieldOpenId.getValueAsString(), new AsyncCallback<SetupServiceAuthResult>()
				{
					@Override
					public void onFailure(Throwable caught)
					{
						otherWindow.loadingWindow.stop();
						// TODO Auto-generated method stub
						SC.say(caught.getMessage());
					}

					@Override
					public void onSuccess(SetupServiceAuthResult result)
					{
						otherWindow.loadingWindow.stop();
//						Cookies.setCookie("disco", result.getDiscovery());
						Cookies.setCookie("disco", result.getAuthUID());
						Location.replace(result.getRedirectURL());
					}
				});
          }
		}); 		
		
		HTML htmlhr = new HTML("<hr>");
		htmlhr.setHeight("10px");

		layoutForm.setWidth(400);
		layoutForm.addMember(form);
		layoutForm.addMember(buttonOpenId);
		
		layoutMain.addMember(labelLogin);
		layoutMain.addMember(layoutForm);
		layoutMain.addMember(htmlhr);
		
		_layoutUserArea.addMember(layoutMain);
	}
	
	public void setUpSession(Integer selected)
	{
		String user = null;
		String pw = null;

		setupService.setUpSession(selected, user, pw, new AsyncCallback<DatabaseConfigurationAnalysis>()
		{
			@Override
			public void onFailure(Throwable caught)
			{
				THIS.enable();
			}

			@Override
			public void onSuccess(DatabaseConfigurationAnalysis result)
			{
				if(result != null)
				{
					otherWindow.analysisName.setContents("Analysis: "+result.metaTitle);
					otherWindow.retrieveAllTopicsFromServer();
					THIS.destroy();
				}
				else
				{
					THIS.enable();
					THIS.show();
				}
			}

		});
	}
}
