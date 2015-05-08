package i5.dvita.webapplication.client;


import java.util.HashMap;

import org.moxieapps.gwt.highcharts.client.Chart;

import com.google.gwt.user.client.rpc.AsyncCallback;
import i5.dvita.webapplication.client.DVITA_WebApplication.ClickHandlerShowDocument;
import i5.dvita.webapplication.shared.DocumentInfo;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.TreeModelType;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.TransferImgButton;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.PickerIcon;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.FormItemClickHandler;
import com.smartgwt.client.widgets.form.fields.events.FormItemIconClickEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyPressEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyPressHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.CellClickEvent;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.LayoutSpacer;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeNode;
//import com.google.gwt.dev.util.collect.HashMap;
//import com.google.gwt.core.shared.GWT;

public class DocumentExplorerWindow extends Window {

	//private SectionStack accordeon = new SectionStack();
	DocumentServiceAsync documentService;

	DVITA_WebApplication mainWindow = null;

	//ThemeRiverServiceAsync themeService ;
	private HashMap<Integer,PartsTreeNode>treeNodes= new HashMap<Integer,PartsTreeNode>();
	private Canvas mainCanvas;
	private Canvas leftpart;
	private Canvas rightpart;
	private VLayout layout;
	private VLayout layout1;
	private HLayout searching;
	private HLayout mainspace;

	Tree grid1Tree = new Tree();
	PartsTreeNode root;
	//final IButton findButton = new IButton();
	TextItem textitem = new TextItem();
	final DynamicForm form = new DynamicForm();
	final VStack docOverviewPanel = new VStack();
	private PartsTreeGrid grid1;
	private String[][] topicInterval;
	private Label label;
	private Canvas pieChartCanvas;

	final IButton clearButton = new IButton("Clear list of documents");

	private Double[] lastProportion;
	private Integer[] lastTopics;
	public Chart lastPie;


	DocumentExplorerWindow() {
		clearButton.setAutoFit(true);
		clearButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				clearWindow();
			}});

		form.setFields(textitem);
		//textitem.setWidth(20);
		textitem.setWidth(110);

		//form.setWidth(10);

		this.setWidth("70%");  
		this.setHeight("70%");
		//this.setWidth100();
		//this.setHeight100();
		this.setCanDragReposition(true);  
		this.setTitle("Document Explorer");
		this.setCanDragResize(true); 
		this.hide();

		layout = new VLayout();
		layout.setHeight100();
		layout.setWidth100();
		/**
		 * for similar Documents to selected Document x that are older than x  
		 */
		layout1= new VLayout();
		layout1.setWidth("50%");
		layout1.setShowResizeBar(true);


		root=new PartsTreeNode("Root",new PartsTreeNode[0]);

		// Tree grid1Tree = new Tree();

		grid1Tree.setModelType(TreeModelType.CHILDREN);  
		grid1Tree.setNameProperty("Name");  
		grid1Tree.setRoot(root);  

		grid1 = new PartsTreeGrid(); 
		grid1.setShowConnectors(true); 
		grid1.setData(grid1Tree);  
		grid1.getData().openAll();  
		CellClickHandlerSimilarDocs handler = new CellClickHandlerSimilarDocs();
		//grid1.addClickHandler(handler);
		grid1.addCellClickHandler(handler);

		grid1.setCanRemoveRecords(true);
		grid1.setShowRecordComponents(true);          
		grid1.setShowRecordComponentsByCell(true);

		mainCanvas = new Canvas();
		mainCanvas.setWidth100();

		this.addItem(mainCanvas);

		leftpart = new Canvas();
		leftpart.setWidth100();

		/**
		 * for similar Documents to selected Document x that are newer than selected x
		 */
		rightpart= new Canvas();
		rightpart.setWidth100();

		mainspace=new HLayout();
		mainspace.setWidth100();
		mainspace.setHeight100();


		mainCanvas.addChild(mainspace);
		mainspace.addMember(layout1);
		mainspace.addMember(layout);


		searchForDocument();
	}


	public void searchForDocument(){


		ClickHandlerDocumentSearch documentsearch = new ClickHandlerDocumentSearch();
		PickerIcon searchPicker = new PickerIcon(PickerIcon.SEARCH, documentsearch);  
		textitem.setIcons(searchPicker); 

		textitem.addKeyPressHandler(documentsearch);

		//findButton.setAutoFit(true);
		//findButton.setIcon("magnifier.png");
		textitem.setTitle("<b>Keywords</b>");
		textitem.setTooltip("Enter keywords you are looking for");
		//textitem.setHeight("10%");
		//	textitem.setWidth("30%");
		searching = new HLayout();
		searching.setAutoWidth();
		searching.setAutoHeight();

		layout1.addMember(searching);

		layout1.setAlign(Alignment.CENTER);
		pieChartCanvas = new Canvas();
		pieChartCanvas.setOverflow(Overflow.VISIBLE);
		layout1.addMember(pieChartCanvas);
		pieChartCanvas.setHeight(200);
		pieChartCanvas.setWidth(200);

		layout1.addMember(clearButton);
		layout1.addMember(grid1);
		searching.addMember(docOverviewPanel);


		form.clearValues();

		docOverviewPanel.addMember(form);
		//form.setLeft(-20);
		docOverviewPanel.setMembersMargin(10);
		//	docOverviewPanel.addMember(findButton);

	}


	public void setService(DocumentServiceAsync greetingService, DVITA_WebApplication test) {
		this.documentService = greetingService;
		mainWindow = test;
	}



	public void UpdateWindowByDocID(DocumentInfo infos, boolean deleteTree, Integer parentId){

		mainWindow.loadingWindow.start("Loading Similar Documents<br>",1);

		lastProportion = infos.topicProportions;
		lastTopics = infos.topicIDs;

		redrawPie();


		if(deleteTree==true){


			if(!treeNodes.containsKey(infos.docID)){

				PartsTreeNode docEntry=new PartsTreeNode(""+infos.docTitle);
				docEntry.setAttribute("DOCINFOS", infos);
				docEntry.setAttribute("date",DVITA_WebApplication.formatDateTime(infos.docDate));

				docEntry.setAttribute("buttonField","");
				docEntry.setAttribute("docID",infos.docID);
				grid1Tree.add(docEntry,root);
				treeNodes.put(infos.docID,docEntry);


			} else {
				TreeNode parent = grid1Tree.getParent(treeNodes.get(infos.docID));
				grid1Tree.openFolder(parent);
			}
		}
		else{
			if( parentId!=null){

				// hole parent, wenn parent bereits gelöscht --> nehme root als parent
				PartsTreeNode parentNode = treeNodes.get(parentId);
				if(parentNode==null) {
					parentNode = root;
				}


				if(!treeNodes.containsKey(infos.docID)){
					PartsTreeNode children=new PartsTreeNode(""+infos.docTitle);
					children.setAttribute("DOCINFOS", infos);
					children.setAttribute("date",DVITA_WebApplication.formatDateTime(infos.docDate));

					grid1Tree.add(children, parentNode);
					treeNodes.put(infos.docID, children);

				}
				grid1Tree.openFolder(parentNode);

			}


		}

		grid1.deselectAllRecords();
		grid1.selectRecord(treeNodes.get(infos.docID));
		int rowNumber = grid1.getRecordIndex(treeNodes.get(infos.docID));
		grid1.scrollToRow(rowNumber); // TODO funktioniert nicht wirklich

		layout.removeMembers(layout.getMembers());

		////////////////////////////////////////////////////////////////////////////////////////		
		Label titlelabel;
		titlelabel=new Label();
		//titlelabel.setAutoHeight();
		titlelabel.setAutoHeight();
		titlelabel.setWidth100();
		titlelabel.setMargin(10);
		titlelabel.setContents("<b>"+infos.docTitle +", "+DVITA_WebApplication.formatDateTime(infos.docDate)+":</b>");
		//titlelabel.set
		titlelabel.setBackgroundColor("font-weight:bold; color:#FF7621; font-size:large");
		//titlelabel.ge
		//titlelabel.setTitle("test"+DocTitle);
		layout.addMember(titlelabel);


		// Button für Content
		IButton DocumentsText = new IButton();
		DocumentsText.setIcon("icon_documents.gif");
		DocumentsText.setTooltip("Click on the button to see the document's content");
		ClickHandlerShowDocument doctext=new ClickHandlerShowDocument();
		doctext.setData(infos.docID,documentService);
		DocumentsText.addClickHandler(doctext);
		DocumentsText.setWidth(30);
		// Ende Button für Content




		HLayout leftRightArrow =new HLayout();
		leftRightArrow.setAutoHeight();
		leftRightArrow.setWidth100();

		layout.setAlign(Alignment.LEFT);
		layout.addMember(leftRightArrow);
		label = new Label();
		label.setAutoHeight();
		label.setWidth(300);
		label.setAlign(Alignment.CENTER);
		label.setContents("Similar documents between "+DVITA_WebApplication.formatDateTime(topicInterval[(int)infos.intervalID][0])+" and "+DVITA_WebApplication.formatDateTime(topicInterval[(int) infos.intervalID][1]));
		TransferImgButton rightArrow = new  TransferImgButton(TransferImgButton.RIGHT);
		TransferImgButton leftArrow = new  TransferImgButton(TransferImgButton.LEFT);

		if(infos.intervalID==0) {
			leftArrow.disable();
		} else if(infos.intervalID==mainWindow.topicIntervals.length-1) {
			rightArrow.disable();
		}

		LayoutSpacer space =  new LayoutSpacer();
		space.setWidth(20);

		leftRightArrow.addMember(DocumentsText);
		leftRightArrow.addMember(space);
		leftRightArrow.addMember(leftArrow);
		leftRightArrow.addMember(label);
		leftRightArrow.addMember(rightArrow);
		leftRightArrow.setAlign(Alignment.LEFT);
		leftRightArrow.setAlign(VerticalAlignment.CENTER);
		leftRightArrow.setMargin(5);

		CallBackSimilarDocs callback = new CallBackSimilarDocs();
		callback.parentid = infos.docID;

		documentService.similarDocuments(infos.docID, infos.intervalID,callback);

		ClickHandlerForwardBackward  forwardBackward = new ClickHandlerForwardBackward(); 
		forwardBackward.setInfos(leftArrow,rightArrow,infos);
		leftArrow.addClickHandler(forwardBackward);
		rightArrow.addClickHandler(forwardBackward);

	}

	public void redrawPie() {

		if(lastProportion==null) return;

		Chart pie = mainWindow.createPieChart(lastProportion,lastTopics);
		for(Canvas c : pieChartCanvas.getChildren()){
			pieChartCanvas.removeChild(c);
		}
		pie.setWidth("200");
		pie.setHeight100();
		lastPie = pie;
		pieChartCanvas.addChild(pie);

	}
	class ClickHandlerForwardBackward implements com.smartgwt.client.widgets.events.ClickHandler{
		TransferImgButton addleftArrow;
		TransferImgButton addrightArrow; 
		DocumentInfo addinfos;
		@Override
		public void onClick(com.smartgwt.client.widgets.events.ClickEvent event) {
			if(event.getSource()==addleftArrow){
				addinfos.intervalID=addinfos.intervalID-1;
				label.setContents(DVITA_WebApplication.formatDateTime(topicInterval[(int)addinfos.intervalID][0])+"<br>"+DVITA_WebApplication.formatDateTime(topicInterval[(int) addinfos.intervalID][1]));
				addrightArrow.disable();
				addleftArrow.disable();
				UpdateWindowByDocID(addinfos, false, null);
			}
			if(event.getSource()==addrightArrow){

				addinfos.intervalID=addinfos.intervalID+1;

				label.setContents(DVITA_WebApplication.formatDateTime(topicInterval[(int)addinfos.intervalID][0])+"<br>"+DVITA_WebApplication.formatDateTime(topicInterval[(int) addinfos.intervalID][1]));
				addrightArrow.disable();
				addleftArrow.disable();
				UpdateWindowByDocID(addinfos, true, null);

			}
			//	else return;


		}

		public void setInfos(TransferImgButton leftArrow,TransferImgButton rightArrow, DocumentInfo infos) {
			addleftArrow=leftArrow;
			addrightArrow= rightArrow;
			addinfos= infos;
		}

	}

	class CallBackSimilarDocs implements AsyncCallback< DocumentInfo []> {

		int parentid;

		@Override
		public void onFailure(Throwable caught) {

		}
		@Override
		public void onSuccess(DocumentInfo[] result) {


			final ListGrid similarDocsGrid = new ListGrid() {  
				@Override  
				protected Canvas createRecordComponent(final ListGridRecord record, Integer colNum) {  

					String fieldName = this.getFieldName(colNum);  

					if (fieldName.equals("buttonField")) {  
						IButton DocumentsText = new IButton();
						//"show doc " + record.getAttribute("buttonField")
						DocumentsText.setIcon("icon_documents.gif");
						DocumentsText.setTooltip("Click on the button to see the document's content ");
						//DocumentsText.setHeight(18);  
						//DocumentsText.setWidth(85);
						DocumentsText.setAutoFit(true);

						ClickHandlerShowDocument doctext=new ClickHandlerShowDocument();
						doctext.setData(Integer.parseInt(record.getAttribute("docID")),documentService);
						DocumentsText.addClickHandler(doctext);

						return DocumentsText;  
					} else if (fieldName.equals("buttonField2")) {  
						TransferImgButton NewDocs = new TransferImgButton(TransferImgButton.RIGHT_ALL);
						NewDocs.setTooltip("Click on the button to see the next ten similar docements to the selected document");


						ClickHandlerForSimilarDocuments handlerSimilarDocs=new ClickHandlerForSimilarDocuments();
						//handlerSimilarDocs.setID(Integer.parseInt(record.getAttribute("docID")), DocTopicTime,record.getAttribute("titel"));
						handlerSimilarDocs.setData((DocumentInfo) record.getAttributeAsObject("DOCINFOS"),parentid);
						NewDocs.addClickHandler(handlerSimilarDocs);

						return NewDocs;  
					} else {  
						return null;  
					}  

				}  
			};  
			similarDocsGrid.setShowRecordComponents(true);          
			similarDocsGrid.setShowRecordComponentsByCell(true);  
			similarDocsGrid.setCanRemoveRecords(false);  
			similarDocsGrid.setWrapCells(true);

			similarDocsGrid.setWidth100();  

			similarDocsGrid.setShowAllRecords(true);  


			ListGridField positionField = new ListGridField("position","#.",20);
			//positionField.setAutoFitWidth(true);
			ListGridField titleField = new ListGridField("titel", "Titel");  
			ListGridField dateField = new ListGridField("date", "Date",65);  
			//continentField.setAutoFitWidth(true);
			ListGridField buttonField = new ListGridField("buttonField", "Content",50);  
			ListGridField buttonField2 = new ListGridField("buttonField2", "Similar Docs",70);  
			buttonField.setAlign(Alignment.CENTER); 
			//buttonField.setWidth(50);
			buttonField2.setAlign(Alignment.CENTER);  
			//buttonField2.setWidth(75);


			similarDocsGrid.setCanRemoveRecords(false);
			similarDocsGrid.setFields(positionField,titleField, dateField, buttonField, buttonField2);  
			similarDocsGrid.setCanResizeFields(true); 






			ListGridRecord dummy = new ListGridRecord();
			similarDocsGrid.addData(dummy);
			similarDocsGrid.removeData(dummy);


			for(int i=0; i<result.length; i++) {

				ListGridRecord entry = new ListGridRecord();
				entry.setAttribute("position", i+1);
				entry.setAttribute("titel",result[i].docTitle);
				//GWT.log("TITEL : "+result[i].docTitle);
				entry.setAttribute("date",DVITA_WebApplication.formatDateTime(result[i].docDate));
				entry.setAttribute("buttonField","");
				entry.setAttribute("buttonField2","");
				entry.setAttribute("docID",result[i].docID);
				entry.setAttribute("DOCINFOS", result[i]);

				similarDocsGrid.addData(entry);  


			}



			layout.addMember(similarDocsGrid);

			mainWindow.loadingWindow.stop();

		}
	}

	class ClickHandlerForSimilarDocuments implements com.smartgwt.client.widgets.events.ClickHandler{
		DocumentInfo docinfos;
		Integer parentId;
		public void setData(DocumentInfo infos, Integer parent){
			docinfos=infos;
			parentId=parent;
		}

		@Override
		public void onClick(ClickEvent event) {
			UpdateWindowByDocID(docinfos,false,parentId);

		}
	}	





	class  ClickHandlerDocumentSearch implements FormItemClickHandler, KeyPressHandler {



		private void sendKeywordsToServer(String string){
			//serverResponseLabel.setText("");
			mainWindow.loadingWindow.start("Searching for documents<br>",1);
			documentService.documentSearch(string,
					new AsyncCallback< DocumentInfo[]>() {
						public void onFailure(Throwable caught) {
							// Show the RPC error message to the user
						}
						@Override
						public void onSuccess(DocumentInfo[] result){
							generateDocumentTree(result);
							mainWindow.loadingWindow.stop();
						}

					});
		} 

		public void generateDocumentTree(DocumentInfo[] result){


			for(int i=0;i<result.length;i++){
				PartsTreeNode docEntry=new PartsTreeNode(""+result[i].docTitle);
				docEntry.setAttribute("date",DVITA_WebApplication.formatDateTime(result[i].docDate));

				docEntry.setAttribute("DOCINFOS", result[i]);
				treeNodes.put(result[i].docID,docEntry);
				grid1Tree.add(docEntry, root);
			}

		}
		@Override
		public void onFormItemClick(FormItemIconClickEvent event) {
			if(textitem.getEnteredValue().length()<1) return;

			sendKeywordsToServer(textitem.getEnteredValue());
		}

		@Override
		public void onKeyPress(KeyPressEvent event) {
			if(event.getKeyName().equals("Enter")) {
				onFormItemClick(null);
			}

		}  
	}
	public class PartsTreeGrid extends TreeGrid {  
		public PartsTreeGrid() {  
			//setHeight(300);  
			//setShowEdges(true);  
			setBorder("0px");  
			setBodyStyleName("normal");  
			setShowHeader(true);  
			setLeaveScrollbarGap(false); 
			//  setEmptyMessage("<br>Drag & drop parts here");  
			setManyItemsImage("cubes_all.png");  
			setAppImgDir("pieces/16/");
			//setManyItemsImage("icon-documents.gif");
			//setAppImgDir("pieces/16/");
			setCanReorderRecords(true);  
			setCanAcceptDroppedRecords(true);  
			setCanDragRecordsOut(true);
			this.setWrapCells(true);

			ListGridField positionField = new ListGridField("Name","Title");
			ListGridField capitalField = new ListGridField("date", "Date",65);  
			this.setFields(positionField,capitalField);
			this.setWidth100();
		}  

		protected Canvas createRecordComponent(final ListGridRecord record, Integer colNum) {  

			String fieldName = this.getFieldName(colNum);  

			if (fieldName.equals("buttonField")) {  
				IButton DocumentsText = new IButton();
				//	record.getAttribute("buttonField");
				DocumentsText.setIcon("icon_documents.gif");
				DocumentsText.setTooltip("Click on the button to see the document's content");
				//DocumentsText.setHeight(18);  
				//DocumentsText.setWidth(55);              
				DocumentsText.setAutoFit(true);
				ClickHandlerShowDocument doctext=new ClickHandlerShowDocument();
				doctext.setData(Integer.parseInt(record.getAttribute("docID")),documentService);
				DocumentsText.addClickHandler(doctext);

				return DocumentsText;  
			} else {  
				return null;  
			}  

		}  


	}  

	public static class PartsTreeNode extends TreeNode {  
		public PartsTreeNode(String name) {  
			this(name, new PartsTreeNode[]{});  
		}  


		public PartsTreeNode(String name, PartsTreeNode... children) {  
			setAttribute("Name", name);  
			setAttribute("children", children);  

		}  	



	}

	class CellClickHandlerSimilarDocs implements com.smartgwt.client.widgets.grid.events.CellClickHandler {


		@Override
		public void onCellClick(CellClickEvent event) {
			DocumentInfo docInformation=(DocumentInfo)(event.getRecord().getAttributeAsObject("DOCINFOS"));

			// ACHTUNG: wenn man den Tree anpasst, muss man hier auch die Spaltennummern anpassen!!!
			if(event.getColNum()==0 || event.getColNum()==1){
				UpdateWindowByDocID(docInformation, false,null);
			} else if(event.getColNum()==2) {
				// rekusrisives löschen der Kinder aus der HashMap (aus dem Tree geht es automatisch)
				TreeNode node = treeNodes.get(docInformation.docID);
				if(node!=null) {
					for(TreeNode children : grid1Tree.getAllNodes(node)) {
						DocumentInfo docInformation2=(DocumentInfo)(children.getAttributeAsObject("DOCINFOS"));
						treeNodes.remove(docInformation2.docID);
					}
					treeNodes.remove(docInformation.docID);

				}
			}

		}

	}


	public void clearWindow() {
		// lösche das Suchfeld
		textitem.clearValue();

		// lösche den Baum
		for(TreeNode children : grid1Tree.getAllNodes(root)) {
			grid1Tree.remove(children);
		}
		treeNodes.clear();

		// löesche PieChart
		for(Canvas c : pieChartCanvas.getChildren()){
			pieChartCanvas.removeChild(c);
		}

		// lösche rechte Seite
		for(Canvas member : layout.getMembers()) {
			layout.removeMember(member);
		}
	}

	public void setIntervals(String[][] topicIntervals) {

		topicInterval = topicIntervals;
		//GWT.log("topicinterval"+topicIntervals[0]+"topicinterval"+topicIntervals[1]);

		clearWindow();
	}

}

