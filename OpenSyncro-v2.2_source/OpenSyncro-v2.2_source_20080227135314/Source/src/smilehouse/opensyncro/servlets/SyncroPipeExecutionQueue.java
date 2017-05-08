/* OpenSyncro - A web-based enterprise application integration tool
 * Copyright (C) 2008 Smilehouse Oy, support@opensyncro.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package smilehouse.opensyncro.servlets;

import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.ecs.AlignType;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.html.A;
import org.apache.ecs.html.BR;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.HR;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.P;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import smilehouse.opensyncro.system.Persister;
import smilehouse.opensyncro.system.PipeExecutionRequest;
import smilehouse.tools.ui.web.TableTool;
import smilehouse.util.LabelResource;
import smilehouse.util.ParameterManager;
import smilehouse.util.Utils;

/**
 * SyncroPipeExecutionQueue.java
 * 
 * Created: Fri 19.05.2006
 */

public class SyncroPipeExecutionQueue extends SyncroServlet {

	public void initParameters(ParameterManager pm) {
		pm.addParameter(ACTION, ParameterManager.INT);
	}

	public int getTabSetId() {
		return 4;
	}

	public int getTabId() {
		return 1;
	}

	public String handleRequest(HttpServletRequest req, HttpSession session,
			ParameterManager parameters, Persister pers, LabelResource labels,
			int requestType) {

		DateFormat dFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
				DateFormat.MEDIUM);

		if (requestType == POST_REQUEST && parameters.getInt(ACTION) == 1) {
			pers.deleteAllPipeExecutionRequests(null);
		}

		Input clearQueueButton = new Input(Input.BUTTON, "clear_queue", labels
				.getLabel("clear_queue"));

		clearQueueButton.setOnClick("if(!confirm('"
				+ Utils.escapeJavaScriptString(labels
						.getLabel("confirm_clearqueue"))
				+ "')) return false; document.forms[0]." + ACTION
				+ ".value=1;document.forms[0].submit();");

		Form form = getForm("PipeExecutionQueue", clearQueueButton.toString(),
				labels, false, false);
		form.addElement(new Input(Input.HIDDEN, ACTION, "0"));

		List entries = pers.getPipeExecutionRequests(null);

		TableTool tt = new TableTool();
		tt.setZebra(true);
		Table queueTable = tt.getInnerTable().addElement(
				tt.getHeaderRow(new String[] { labels.getLabel("pipe"),
						labels.getLabel("createtime"),
						labels.getLabel("starttime") }));

		int colorIndex = 0;
		for (Iterator i = entries.iterator(); i.hasNext(); colorIndex = 1 - colorIndex) {
			PipeExecutionRequest entry = (PipeExecutionRequest) i.next();

			TR row = tt.getRow();

			Date creationTime = entry.getCreatedDate();
			String formattedCreationTime;
			if (creationTime != null) {
				formattedCreationTime = dFormat.format(creationTime);
			} else {
				formattedCreationTime = "";
			}
			Date startTime = entry.getStartedDate();
			String formattedStartTime;
			if (startTime != null) {
				formattedStartTime = dFormat.format(startTime);
			} else {
				formattedStartTime = "Queued";
			}
			queueTable.addElement(row.addElement(
					new TD().addElement(new A("EditPipe?pipeid="
							+ entry.getPipe().getId(), entry.getPipe()
							.getName()))).addElement(
					new TD().addElement(formattedCreationTime)).addElement(
					new TD().addElement(formattedStartTime)));

		}

		ElementContainer content = new ElementContainer().addElement(new BR())
				.addElement(new P().setAlign(AlignType.CENTER))
				.addElement(tt.getOuterTable(queueTable))
				.addElement(new HR().addElement(form));

		return content.toString();
	}

}