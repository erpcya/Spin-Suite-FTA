/*************************************************************************************
 * Product: Spin-Suite (Making your Business Spin)                                   *
 * This program is free software; you can redistribute it and/or modify it           *
 * under the terms version 2 of the GNU General Public License as published          *
 * by the Free Software Foundation. This program is distributed in the hope          *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied        *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                  *
 * See the GNU General Public License for more details.                              *
 * You should have received a copy of the GNU General Public License along           *
 * with this program; if not, write to the Free Software Foundation, Inc.,           *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                            *
 * For the text or an alternative of this public license, you may reach us           *
 * Copyright (C) 2012-2014 E.R.P. Consultores y Asociados, S.A. All Rights Reserved. *
 * Contributor(s): Yamel Senih www.erpconsultoresyasociados.com                      *
 *************************************************************************************/
package org.spinsuite.fta.view;

import java.util.ArrayList;
import java.util.logging.Level;

import org.spinsuite.fta.adapters.SP_SearchAdapter;
import org.spinsuite.base.DB;
import org.spinsuite.fta.base.R;
import org.spinsuite.util.DisplayMenuItem;
import org.spinsuite.fta.util.SP_DisplayRecordItem;
import org.spinsuite.util.DisplayRecordItem;
import org.spinsuite.util.DisplayType;
import org.spinsuite.util.FilterValue;
import org.spinsuite.util.LogM;
import org.spinsuite.view.lookup.GridField;
import org.spinsuite.view.lookup.InfoField;
import org.spinsuite.view.lookup.VLookupCheckBox;
import org.spinsuite.view.lookup.VLookupDateBox;
import org.spinsuite.view.lookup.VLookupSearch;
import org.spinsuite.view.lookup.VLookupSpinner;
import org.spinsuite.view.lookup.VLookupString;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SearchViewCompat;
import android.support.v4.widget.SearchViewCompat.OnCloseListenerCompat;
import android.support.v4.widget.SearchViewCompat.OnQueryTextListenerCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;

/**
 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a>
 *
 */
public class V_AddSuggestedProduct extends Activity {
	
	/**	Adapter					*/
	private SP_SearchAdapter 		m_SP_SearchAdapter = null;
	/**	Main Layout				*/
	private LinearLayout			ll_ConfigSearch = null;
	/**	List View				*/
	private ListView				lv_SuggestedProducts = null;
	/**	Technical Form			*/
	private int						m_FTA_TechnicalForm_ID = 0;
	/**	Technical Form Line		*/
	private int						m_FTA_TechnicalFormLine_ID = 0;
	/**	Tab Identifier			*/
	private FilterValue				m_criteria = null;
	/**	Old Criteria			*/
	private FilterValue				m_oldCriteria = null;
	/**	View Index Array		*/
	private ArrayList<GridField>	viewList = null;
	/**	Parameter				*/
	private LayoutParams			v_param	= null;
	/**	Activity				*/
	private Activity				v_activity = null;
	
	/**	View Weight				*/
	private static final float 		WEIGHT = 1;
	
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		super.setContentView(R.layout.v_add_suggested_product);
    	//	Get Field
    	Bundle bundle = getIntent().getExtras();
		if(bundle != null) {
			m_FTA_TechnicalFormLine_ID = bundle.getInt("FTA_TechnicalFormLine_ID");
			m_FTA_TechnicalForm_ID = bundle.getInt("FTA_TechnicalForm_ID");
		}
		//	Set Activity
		v_activity = this;
		//	
		ll_ConfigSearch = (LinearLayout) findViewById(R.id.ll_ConfigSearch);
		lv_SuggestedProducts = (ListView) findViewById(R.id.lv_SuggestedProducts);
		//	
		//	
		loadConfig();
		
		//	Load
		new LoadViewTask().execute();
    	//	Listener
		lv_SuggestedProducts.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View arg1, int position,
					long arg3) {
				//	Load from Action
				selectedRecord(m_SP_SearchAdapter.getItem(position));
			}
        });
	}
	
	/**
	 * Load Config
	 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a> 18/03/2014, 22:07:43
	 * @return void
	 */
	private void loadConfig(){
		viewList = new ArrayList<GridField>();
		//	Set Parameter
		v_param = new LayoutParams(LayoutParams.MATCH_PARENT, 
				LayoutParams.MATCH_PARENT, WEIGHT);
		//	Add Fields
		//	Add View to Layout
		InfoField field = new InfoField();
		field.DisplayType = DisplayType.YES_NO;
		
		addView(field);
	    	//	Add Button
		Button btn_Search = new Button(this);
		btn_Search.setText(getResources().getString(R.string.msg_Search));
		//	Action
		btn_Search.setOnClickListener(new OnClickListener() {
		
			@Override
			public void onClick(View v) {
				m_criteria = new FilterValue();
				if(m_oldCriteria != null)
					m_criteria = m_oldCriteria;
				//	Add Criteria
				addCriteriaQuery();
				new LoadViewTask().execute();
			}
		});
		//	Add Button
		ll_ConfigSearch.addView(btn_Search, v_param);
		//	Hide
		ll_ConfigSearch.setVisibility(LinearLayout.GONE);
	}
	
	/**
	 * Add Criteria Query
	 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a> 19/03/2014, 10:28:14
	 * @return void
	 */
	private void addCriteriaQuery(){
    	//	Get Values
		StringBuffer sqlWhere = new StringBuffer();
    	for (GridField lookup: viewList) {
    		//	Only Filled
    		if(lookup.isEmpty())
    			continue;
    		InfoField field = lookup.getField();
    		//	Set to model
    		if(sqlWhere.length() > 0)
    			sqlWhere.append(" AND ");
    		//	Add Criteria Column Filter
    		sqlWhere.append(field.ColumnName).append(" = ?");
    		//	Add Value
    		m_criteria.addValue(DisplayType.getJDBC_Value(field.DisplayType, 
    				lookup.getValue()));
		}
    	//	Add SQL
    	m_criteria.setWhereClause(sqlWhere.toString());
	}
	
	/**
	 * Add View to Config Panel
	 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a> 18/03/2014, 22:10:08
	 * @param field
	 * @return void
	 */
	private void addView(InfoField field){
    	
    	GridField lookup = null;
		//	Add
		if(DisplayType.isDate(field.DisplayType)){
			lookup = new VLookupDateBox(this, field);
		} else if(DisplayType.isText(field.DisplayType)){
			VLookupString lookupString = new VLookupString(this, field);
			lookup = lookupString;
		} else if(DisplayType.isBoolean(field.DisplayType)){
			lookup = new VLookupCheckBox(this, field);
		} else if(DisplayType.isLookup(field.DisplayType)){
			//	Table Direct
			if(field.DisplayType == DisplayType.TABLE_DIR){
				//	Optional Null Value
				field.IsMandatory = false;
				lookup = new VLookupSpinner(this, field);
			} else if(field.DisplayType == DisplayType.SEARCH){
				lookup = new VLookupSearch(this, field);
			}
		}
		//	is Filled
		if(lookup != null){
			viewList.add(lookup);
			ll_ConfigSearch.addView(lookup, v_param);
		}
    }
	  
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		//	Inflate menu
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search, menu);
		//	Get Item
		MenuItem item = menu.findItem(R.id.action_search);
		//	Search View
		final View searchView = SearchViewCompat.newSearchView(this);
		if (searchView != null) {
			//	Set Back ground Color
			int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
			EditText searchText = (EditText) searchView.findViewById(id);
			//	Set Parameters
			if(searchText != null)
				searchText.setTextAppearance(this, R.style.TextSearch);
			//	
			SearchViewCompat.setOnQueryTextListener(searchView,
					new OnQueryTextListenerCompat() {
				@Override
				public boolean onQueryTextChange(String newText) {
					if(m_SP_SearchAdapter != null){
						String mFilter = !TextUtils.isEmpty(newText) ? newText : null;
						m_SP_SearchAdapter.getFilter().filter(mFilter);
					}
					return true;
				}
			});
			SearchViewCompat.setOnCloseListener(searchView,
					new OnCloseListenerCompat() {
				@Override
				public boolean onClose() {
					if (!TextUtils.isEmpty(SearchViewCompat.getQuery(searchView))) {
						SearchViewCompat.setQuery(searchView, null, true);
					}
					return true;
				}
                    
			});
			MenuItemCompat.setActionView(item, searchView);
		}
		//	Valid Configuration
		MenuItem itemConfig = menu.findItem(R.id.action_config);
		if(itemConfig != null) 
			itemConfig.setVisible(true);
		//	
		MenuItem itemAdd = menu.findItem(R.id.action_add);
		//	Visible
		if(itemAdd != null)
			itemAdd.setVisible(false);
		//	Close
		MenuItem itemClose = menu.findItem(R.id.action_close);
		//	Visible
		if(itemClose != null)
			itemClose.setVisible(true);

		return true;
	}
	    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.action_add 
				|| itemId == R.id.action_close) {
			selectedRecord(new SP_DisplayRecordItem(-1));
			return true;
		} else if (itemId == R.id.action_config) {
			//	Show
			if(ll_ConfigSearch.getVisibility() == LinearLayout.GONE){
				ll_ConfigSearch.setVisibility(LinearLayout.VISIBLE);
				m_oldCriteria = m_criteria;
			} else {
				ll_ConfigSearch.setVisibility(LinearLayout.GONE);
				m_criteria = m_oldCriteria;
				//	Load New
				new LoadViewTask().execute();
			}
			return true;
		}
		//	
		return super.onOptionsItemSelected(item);
	}	
	
	/**
	 * On Selected Record
	 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a> 01/03/2014, 13:23:17
	 * @param item
	 * @return void
	 */
	private void selectedRecord(SP_DisplayRecordItem item){
		Intent intent = getIntent();
		Bundle bundle = new Bundle();
		bundle.putParcelable("Record", item);
		/*if(m_field != null){
			bundle.putInt(DisplayMenuItem.CONTEXT_ACTIVITY_TYPE, 
					DisplayMenuItem.CONTEXT_ACTIVITY_TYPE_SearchColumn);
			bundle.putString("ColumnName", m_field.ColumnName);
		} else {
			bundle.putInt(DisplayMenuItem.CONTEXT_ACTIVITY_TYPE, 
					DisplayMenuItem.CONTEXT_ACTIVITY_TYPE_SearchWindow);
		}*/
		intent.putExtras(bundle);
		setResult(Activity.RESULT_OK, intent);
		finish();
	}
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	//	
    	if (resultCode == Activity.RESULT_OK) {
	    	if(data != null){
	    		Bundle bundle = data.getExtras();
	    		//	Item
	    		DisplayRecordItem item = (DisplayRecordItem) bundle.getParcelable("Record");
	    		switch (bundle.getInt(DisplayMenuItem.CONTEXT_ACTIVITY_TYPE)) {
	    			case DisplayMenuItem.CONTEXT_ACTIVITY_TYPE_SearchColumn:
						String columnName = bundle.getString("ColumnName");
			    		//	if a field or just search
			    		if(columnName != null){
			    			for (GridField vField: viewList) {
			    	    		if(vField.getColumnName().equals(columnName)){
			    	    			((VLookupSearch) vField).setItem(item);
			    	    			break;
			    	    		}
			    			}
			    		}
						break;
				default:
					break;
	    		}
	    	}
    	}
    }
	
	/**
	 * Get SQL from Parameters
	 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a> 27/08/2014, 15:55:40
	 * @param criteria
	 * @return
	 * @return String
	 */
	private String getSQL(FilterValue criteria) {
		StringBuffer sql = new StringBuffer();
		/*sql.append("SELECT p.M_Product_ID, p.Value || '_' || p.Name ProductName, " +
				"CASE WHEN pa.M_Product_ID = p.M_Product_ID THEN pa.QtySuggested ELSE NULL END QtySuggested, " +
				"CASE WHEN pa.M_Product_ID = p.M_Product_ID THEN pa.Suggested_UOM_ID ELSE p.C_UOM_ID END Suggested_Uom_ID, " +
				"CASE WHEN pa.M_Product_ID = p.M_Product_ID THEN su.UOMSymbol ELSE pu.UOMSymbol END SuggestedUOMSymbol, " +
				"CASE WHEN pa.M_Product_ID = p.M_Product_ID THEN pa.QtyDosage ELSE NULL END QtyDosage, " +
				"CASE WHEN pa.M_Product_ID = p.M_Product_ID THEN pa.Dosage_UOM_ID ELSE p.C_UOM_ID END Dosage_Uom_ID, " +
				"CASE WHEN pa.M_Product_ID = p.M_Product_ID THEN du.UOMSymbol ELSE pu.UOMSymbol END DosageUOMSymbol, " +
				"CASE WHEN pa.M_Product_ID = p.M_Product_ID THEN pa.Qty ELSE NULL END Qty, " +
				"CASE WHEN pa.M_Product_ID = p.M_Product_ID THEN pa.C_UOM_ID ELSE p.C_UOM_ID END C_UOM_ID, " +
				"CASE WHEN pa.M_Product_ID = p.M_Product_ID THEN ou.UOMSymbol ELSE pu.UOMSymbol END OrderUOMSymbol, " +
				"0 DayFrom, 0 DayTo " +
				"FROM M_Product p " +
				"INNER JOIN C_UOM pu ON(pu.C_UOM_ID = p.C_UOM_ID) " +
				"INNER JOIN FTA_TechnicalFormLine tfl ON(tfl.AD_Client_ID = p.AD_Client_ID) " +
				"INNER JOIN FTA_Farming fm ON(tfl.FTA_Farming_ID = fm.FTA_Farming_ID) " +
				"LEFT JOIN FTA_ProductsToApply pa ON(pa.FTA_TechnicalFormLine_ID = tfl.FTA_TechnicalFormLine_ID) " +
				"LEFT JOIN C_UOM su ON(su.C_UOM_ID = pa.Suggested_UOM_ID) " +
				"LEFT JOIN C_UOM du ON(du.C_UOM_ID = pa.Dosage_UOM_ID) " +
				"LEFT JOIN C_UOM ou ON(ou.C_UOM_ID = pa.C_UOM_ID) " +
				"WHERE tfl.FTA_TechnicalForm_ID = ");
		//	
		sql.append(m_FTA_TechnicalForm_ID);*/
		//	
		sql.append("SELECT sp.M_Product_ID, sp.Value || '_' || sp.Name, " +
				"CASE WHEN pa.M_Product_ID = sp.M_Product_ID THEN pa.QtySuggested ELSE fsp.QtyDosage END QtySuggested, " +
				"CASE WHEN pa.M_Product_ID = sp.M_Product_ID THEN pa.Suggested_UOM_ID ELSE su.C_UOM_ID END Suggested_Uom_ID, " +
				"CASE WHEN pa.M_Product_ID = sp.M_Product_ID THEN su.UOMSymbol ELSE su.UOMSymbol END SuggestedUOMSymbol, " +
				"CASE WHEN pa.M_Product_ID = sp.M_Product_ID THEN pa.QtyDosage ELSE fsp.QtyDosage END QtyDosage, " +
				"CASE WHEN pa.M_Product_ID = sp.M_Product_ID THEN pa.Dosage_UOM_ID ELSE su.C_UOM_ID END Dosage_Uom_ID, " +
				"CASE WHEN pa.M_Product_ID = sp.M_Product_ID THEN du.UOMSymbol ELSE su.UOMSymbol END DosageUOMSymbol, " +
				"CASE WHEN pa.M_Product_ID = sp.M_Product_ID THEN pa.Qty ELSE fsp.QtyDosage END Qty, " +
				"CASE WHEN pa.M_Product_ID = sp.M_Product_ID THEN pa.C_UOM_ID ELSE sp.C_UOM_ID END C_UOM_ID, " +
				"CASE WHEN pa.M_Product_ID = sp.M_Product_ID THEN ou.UOMSymbol ELSE su.UOMSymbol END OrderUOMSymbol, " +
				"fsp.DayFrom, fsp.DayTo " +
				"FROM FTA_TechnicalFormLine tfl " +
				"INNER JOIN FTA_Farming fm ON (tfl.FTA_Farming_ID = fm.FTA_Farming_ID) " +
				"INNER JOIN M_Product pc ON (pc.M_Product_ID = fm.Category_ID) " +
				"INNER JOIN FTA_SuggestedProduct fsp ON (fsp.Category_ID = fm.Category_ID OR fsp.Category_ID IS NULL) " +
				"LEFT JOIN M_Product sp ON (sp.M_Product_Category_ID = fsp.M_Product_Category_ID OR sp.M_Product_ID=fsp.M_Product_ID) " +
				"LEFT JOIN FTA_ProductsToApply pa ON(pa.FTA_TechnicalFormLine_ID = tfl.FTA_TechnicalFormLine_ID) " +
				"LEFT JOIN C_UOM su ON(su.C_UOM_ID = COALESCE(pa.Suggested_UOM_ID, fsp.Dosage_UOM_ID)) " +
				"LEFT JOIN C_UOM du ON(du.C_UOM_ID = pa.Dosage_UOM_ID) " +
				"LEFT JOIN C_UOM ou ON(ou.C_UOM_ID = pa.C_UOM_ID) " +
				"WHERE (tfl.FTA_FarmingStage_ID=fsp.FTA_FarmingStage_ID OR fsp.FTA_FarmingStage_ID IS NULL) " +
				"AND (tfl.FTA_ObservationType_ID=fsp.FTA_ObservationType_ID OR fsp.FTA_ObservationType_ID IS NULL) ");
		//	
		sql.append("AND tfl.FTA_TechnicalFormLine_ID = ");
		//	
		sql.append(m_FTA_TechnicalFormLine_ID);
		
		
		return sql.toString();
	}
	
	/**
	 * Include Class Thread
	 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a>
	 *
	 */
	private class LoadViewTask extends AsyncTask<Void, Integer, Integer> {

		/**	Progress Bar			*/
		private ProgressDialog 					v_PDialog;
		/**	Data					*/
		private ArrayList<SP_DisplayRecordItem> data = null;
		
		/**
		 * Init Values
		 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a> 17/05/2014, 12:18:42
		 * @return void
		 */
		private void init(){
	    	//	Load Table Info
			data = new ArrayList<SP_DisplayRecordItem>();
			//	View
		}
		
		@Override
		protected void onPreExecute() {
			v_PDialog = ProgressDialog.show(v_activity, null, 
					getString(R.string.msg_Loading), false, false);
			//	Set Max
		}
		
		@Override
		protected Integer doInBackground(Void... params) {
			init();
			//	Load Data
			loadData();
			//	
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... progress) {
			
		}

		@Override
		protected void onPostExecute(Integer result) {
			loadView();
			v_PDialog.dismiss();
		}
		
	    /**
	     * Load View Objects
	     * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a> 18/02/2014, 16:37:56
	     * @return
	     * @return boolean
	     */
	    protected boolean loadView(){
	    	//	Set Adapter
			m_SP_SearchAdapter = new SP_SearchAdapter(getApplicationContext(), data);
			lv_SuggestedProducts.setAdapter(m_SP_SearchAdapter);
			//	
			return true;
	    }
	    
	    /**
		 * Load Data
		 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a> 01/03/2014, 13:32:16
		 * @return void
		 */
		private void loadData() {
			try{
				//	
				DB conn = new DB(v_activity);
				DB.loadConnection(conn, DB.READ_ONLY);
				Cursor rs = null;
				//	Query
				String[] values = null;
				if(m_criteria != null) {
					values = m_criteria.getValues();
				}	
				//	
				rs = conn.querySQL(getSQL(m_criteria), values);
				//	
				if(rs.moveToFirst()){
					//	Loop
					do{
						int index = 0;
						data.add(new SP_DisplayRecordItem(
								rs.getInt(index++), 
								rs.getString(index++), 
								rs.getDouble(index++), 
								rs.getInt(index++), 
								rs.getString(index++), 
								rs.getDouble(index++), 
								rs.getInt(index++), 
								rs.getString(index++), 
								rs.getDouble(index++), 
								rs.getInt(index++), 
								rs.getString(index++), 
								rs.getInt(index++), 
								rs.getInt(index++)));
					}while(rs.moveToNext());
				}
				//	Close
				DB.closeConnection(conn);
			} catch(Exception e){
				LogM.log(v_activity, getClass(), Level.SEVERE, "Error in Load", e);
			}
		}
	}
}
