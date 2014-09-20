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

import org.spinsuite.fta.base.R;
import org.spinsuite.util.ActivityParameter;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

/**
 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a>
 *
 */
public class V_FarmerInfo extends Activity {

	/**	Parameters					*/
	private ActivityParameter 	param	= null;
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		super.setContentView(R.layout.v_farmer_info);
    	//	Get Field
    	Bundle bundle = getIntent().getExtras();
		if(bundle != null) {
			param = (ActivityParameter)bundle.getParcelable("Param");
		}
		if(param == null)
    		param = new ActivityParameter();
		//	Get Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setSubtitle(getString(R.string.FarmerInfo));
	}
	
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			finish();
			return true;
		}
		//	
        return super.onOptionsItemSelected(item);
    }
}
