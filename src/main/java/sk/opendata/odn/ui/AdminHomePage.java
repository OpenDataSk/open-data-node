/* Copyright (C) 2011 Peter Hanecak <hanecak@opendata.sk>
 *
 * This file is part of Open Data Node.
 *
 * Open Data Node is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Open Data Node is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open Data Node.  If not, see <http://www.gnu.org/licenses/>.
 */

package sk.opendata.odn.ui;

import java.util.Set;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Open Data Node "management console" homepage.
 * 
 */
public class AdminHomePage extends WebPage {

	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.getLogger(AdminHomePage.class);
	
	// TODO: Make it configurable or entirely re-think this "debug feature" as
	// this hard-coded string have to match what is configured in
	// quartz_data.xml .
	public final static String DATANEST_HARVESTER_SCHEDULE_GROUP_NAME = "DatanestHarvesterGroup";
	
	private class ScrapControlForm extends Form<Void> {
		
		private static final long serialVersionUID = 1L;

		public ScrapControlForm(String name) {
			//super(name, new CompoundPropertyModel<ScrapControlFormInputModel>(new ScrapControlFormInputModel()));
			super(name);
			
			add(new Button("scrapButton"));
		}
		
		/**
		 * @see org.apache.wicket.markup.html.form.Form#onSubmit()
		 */
		@Override
		public void onSubmit() {
			try {
				// TODO: Will this be the *same* instance as in
				// 'WicketApplication.<constructor>()'?
				Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
				Set<JobKey> jobKeys = scheduler
						.getJobKeys(GroupMatcher
								.<JobKey> groupEquals(DATANEST_HARVESTER_SCHEDULE_GROUP_NAME));
				for (JobKey jobKey : jobKeys) {
					scheduler.triggerJob(jobKey);
				}
			} catch (SchedulerException e) {
				logger.error("scheduler exception", e);
				// TODO is it a "good practice" to pass that also up to Wicket?
			}
		}

	}

    /**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
    public AdminHomePage(final PageParameters parameters) {
    	add(new ScrapControlForm("scrapControlForm"));
    }
}
