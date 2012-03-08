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

import junit.framework.TestCase;

import org.apache.wicket.util.tester.WicketTester;

import sk.opendata.odn.ui.AdminHomePage;

/**
 * Simple test using the WicketTester
 */
public class TestAdminHomePage extends TestCase
{
	private WicketTester tester;

	public void setUp()
	{
		tester = new WicketTester();
	}

	public void testRenderMyPage()
	{
		//start and render the test page
		tester.startPage(AdminHomePage.class);

		//assert rendered page class
		tester.assertRenderedPage(AdminHomePage.class);

		tester.assertContains("Open Data Node");
		tester.assertContains("Scrap Right Now!");
	}
}
