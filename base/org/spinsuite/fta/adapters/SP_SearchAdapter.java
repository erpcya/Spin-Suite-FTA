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
package org.spinsuite.fta.adapters;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.spinsuite.fta.base.R;
import org.spinsuite.fta.util.EditTextHolder;
import org.spinsuite.fta.util.SP_DisplayRecordItem;
import org.spinsuite.util.DisplayType;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.view.View.OnFocusChangeListener;

/**
 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a>
 *
 */
public class SP_SearchAdapter extends BaseAdapter implements Filterable {

	/**
	 * 
	 * *** Constructor ***
	 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a> 01/03/2014, 13:02:43
	 * @param ctx
	 * @param data
	 */
	public SP_SearchAdapter(Context ctx, ArrayList<SP_DisplayRecordItem> data) {
		this.data = data;
		numberFormat = DisplayType.getNumberFormat(ctx, DisplayType.QUANTITY);
		inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		notifyDataSetChanged();
	}
	
	/**
	 * 
	 * *** Constructor ***
	 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a> 22/03/2014, 09:22:14
	 * @param ctx
	 * @param view_ID
	 */
	public SP_SearchAdapter(Context ctx) {
		data = new ArrayList<SP_DisplayRecordItem>();
		numberFormat = DisplayType.getNumberFormat(ctx, DisplayType.QUANTITY);
		inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		notifyDataSetChanged();
	}
	
	/**	Data							*/
	private ArrayList<SP_DisplayRecordItem> 	data;
	/**	Backup							*/
	private ArrayList<SP_DisplayRecordItem> 	originalData;
	/**	Decimal Format					*/
	private DecimalFormat						numberFormat = null;
	/**	Inflater						*/
	private LayoutInflater 						inflater = null;
	
	
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {		
		View view = convertView;
		
		//	
		final SP_DisplayRecordItem recordItem = data.get(position);
		//	
		EditTextHolder holderQtyOrdered = new EditTextHolder();
		//	Inflate View
		if(view == null)
			view = inflater.inflate(R.layout.i_suggested_product_search, null);
		//	Set Quantity to Order
		EditText et_QtyOrdered = (EditText)view.findViewById(R.id.et_QtyOrdered);
		//	Instance Holder
		holderQtyOrdered.setText(String.valueOf(recordItem.getQty()));
		holderQtyOrdered.setEditText(et_QtyOrdered);
		holderQtyOrdered.getEditText().setOnFocusChangeListener(new OnFocusChangeListener() {
				
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus) {
					final EditText et_Qty = (EditText) v;
					recordItem.setQty(DisplayType.getNumber(et_Qty.getText().toString()).doubleValue());
					//	Set Item
					data.set(position, recordItem);
					//	Set to Original Data
					if(originalData != null)
						setToOriginalData(recordItem);
				}
			}
		});
		
		//	Set Quantity
		holderQtyOrdered.setText(String.valueOf(recordItem.getQty()));
		view.setTag(holderQtyOrdered);
		
		//	Set Name
		TextView tv_Product = (TextView)view.findViewById(R.id.tv_Product);
		tv_Product.setText(recordItem.getProductName());
		//	Set Day From
		TextView tv_DayFrom = (TextView)view.findViewById(R.id.tv_DayFrom);
		tv_DayFrom.setText(numberFormat.format(recordItem.getDayFrom()));
		//	Set Day To
		TextView tv_DayTo = (TextView)view.findViewById(R.id.tv_DayTo);
		tv_DayTo.setText(numberFormat.format(recordItem.getDayTo()));
		//	Set Quantity Suggested
		TextView tv_QtySuggested = (TextView)view.findViewById(R.id.tv_QtySuggested);
		tv_QtySuggested.setText(numberFormat.format(recordItem.getQtySuggested()) 
				+ (recordItem.getSuggestedUOMSymbol() != null? " " + recordItem.getSuggestedUOMSymbol(): ""));
		//	Set Quantity Dosage
		TextView tv_QtyDosage = (TextView)view.findViewById(R.id.tv_QtyDosage);
		tv_QtyDosage.setText(numberFormat.format(recordItem.getQtyDosage()) 
				+ (recordItem.getDosageUOMSymbol() != null? " " + recordItem.getDosageUOMSymbol(): ""));
		//	Return
		return view;
	}
	
	@Override
	public Filter getFilter() {
		
	    return new Filter() {
	        @SuppressWarnings("unchecked")
	        @Override
	        protected void publishResults(CharSequence constraint, FilterResults results) {
	            data = (ArrayList<SP_DisplayRecordItem>) results.values;
	            if (results.count > 0) {
	            	notifyDataSetChanged();
	            } else {
	            	notifyDataSetInvalidated();
	            }  
	        }

	        @Override
	        protected FilterResults performFiltering(CharSequence constraint) {
	            //	Populate Original Data
	        	if(originalData == null)
	            	originalData = data;
	        	//	Get filter result
	        	ArrayList<SP_DisplayRecordItem> filteredResults = getResults(constraint);
	            //	Result
	            FilterResults results = new FilterResults();
	            //	
	            results.values = filteredResults;
	            results.count = filteredResults.size();
	            
	            return results;
	        }

	        /**
	         * Search
	         * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a> 02/03/2014, 03:19:33
	         * @param constraint
	         * @return
	         * @return ArrayList<DisplayRecordItem>
	         */
	        private ArrayList<SP_DisplayRecordItem> getResults(CharSequence constraint) {
	        	//	Verify
	            if(constraint != null
	            		&& constraint.length() > 0) {
	            	//	new Filter
	            	ArrayList<SP_DisplayRecordItem> filteredResult = new ArrayList<SP_DisplayRecordItem>();
	                for(SP_DisplayRecordItem item : originalData) {
	                    if((item.getProductName() != null 
	                    		&& item.getProductName().toLowerCase().contains(constraint.toString())))
	                        filteredResult.add(item);
	                }
	                return filteredResult;
	            }
	            //	Only Data
	            return originalData;
	            //return data;
	        }
	    };
	}
	
	/**
	 * Set To Original Data
	 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a> 28/08/2014, 10:10:38
	 * @param p_Item
	 * @return void
	 */
	private void setToOriginalData(SP_DisplayRecordItem p_Item) {
		if(p_Item == null
				|| originalData == null)
			return;
		//	Search
		for(int i = 0; i < originalData.size(); i++) {
            if(originalData.get(i).getM_Product_ID() == p_Item.getM_Product_ID())
            		originalData.set(i, p_Item);
        }
	}
	
	@Override
	public int getCount() {
		return data.size();
	}
	
	@Override
	public SP_DisplayRecordItem getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
}
