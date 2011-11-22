package sk.opendata.odn;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Open Data Node "management console" homepage.
 */
public class HomePage extends WebPage {

	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.getLogger(HomePage.class);

	private class ScrapControlForm extends Form {
		
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
			// TODO: start scraping
			logger.info("TODO: start scraping");
		}

	}

    /**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
    public HomePage(final PageParameters parameters) {
    	add(new ScrapControlForm("scrapControlForm"));
    }
}
