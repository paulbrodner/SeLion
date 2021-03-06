/*-------------------------------------------------------------------------------------------------------------------*\
|  Copyright (C) 2014 PayPal                                                                                          |
|                                                                                                                     |
|  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     |
|  with the License.                                                                                                  |
|                                                                                                                     |
|  You may obtain a copy of the License at                                                                            |
|                                                                                                                     |
|       http://www.apache.org/licenses/LICENSE-2.0                                                                    |
|                                                                                                                     |
|  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   |
|  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  |
|  the specific language governing permissions and limitations under the License.                                     |
\*-------------------------------------------------------------------------------------------------------------------*/

package com.paypal.selion.grid.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.utils.CapabilityMatcher;
import org.openqa.grid.web.servlet.RegistryBasedServlet;

import com.paypal.selion.grid.matchers.SeLionSauceCapabilityMatcher;
import com.paypal.selion.pojos.SeLionGridConstants;
import com.paypal.selion.utils.AuthenticationHelper;
import com.paypal.selion.utils.ServletHelper;

/**
 * This plain vanilla servlet is responsible for supporting login/logout and display of the main page.
 * 
 */
public class LoginServlet extends RegistryBasedServlet {

    public LoginServlet(Registry registry) {
        super(registry);
    }

    public LoginServlet() {
        this(null);
    }

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getParameter("logout") != null && req.getParameter("logout").equals("true")) {

            HttpSession session = req.getSession();
            if (session != null) {
                session.invalidate();
            }
            PrintWriter writer = resp.getWriter();

            writer.write("<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>");
            writer.write("<html xmlns='http://www.w3.org/1999/xhtml'>");
            writer.write("<head>");
            writer.write("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
            writer.write("<title>Grid Management Console</title>");
            writer.write("<link rel='stylesheet' type='text/css' href='/grid/resources/form/view.css' media='all' >");
            writer.write("<script type='text/javascript' src='/grid/resources/form/view.js'></script>");
            writer.write("</head>");
            writer.write("<body id='main_body' >");

            writer.write("<img id='top' src='/grid/resources/form/top.png' alt=''>");
            writer.write("<div id='form_container'>");
            writer.write("<div style=\"margin: 20px 20px 0; padding:0 0 20px;\">");

            writer.write("<div class='form_description'>");
            writer.write("<h2>Grid Management Console</h2>");
            writer.write("<p>You are logged out</p>");
            writer.write("</div>");
            writer.write("<div id='footer'>");
            writer.write("<a align='center' href='/grid/admin/LoginServlet'>Login</a><br>");
            writer.write("<p>Created by the SeLion Project</p>");
            writer.write("</div>");
            writer.write("</div></div> <img id='bottom' src='/grid/resources/form/bottom.png' alt=''>");
            // Facility to login again here must be provided
            writer.write("</body>");
            writer.write("</html>");

        } else {
            process(req, resp);
        }
    }

    /**
     * @param req
     * @param resp
     * @throws IOException
     */
    private void process(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (req.getParameter("form_id") != null && req.getParameter("form_id").equals("login")) {
            String userid = req.getParameter("userid");
            String password = req.getParameter("password");
            // For already created session , if the session has username and the password then use the same to
            // authenticate user else get back to the parameters from the request
            HttpSession currentSession = req.getSession(false);
            if (currentSession != null) {
                userid = (String) currentSession.getAttribute("userId");
                password = (String) currentSession.getAttribute("password");
            }

            if (!AuthenticationHelper.authenticate(userid, password)) {
                /*
                 * To display error message if invalid username or password is entered
                 */
                ServletHelper.loginToGrid(resp.getWriter(),
                        "<b>Invalid Credentials. Enter valid Username and Password</b>");
            } else {

                /*
                 * After successful login main page will be displayed with links to force restart and autoupgrade. Note:
                 * For every re-direction, a new session is created and the userId and password are forwarded with the
                 * session
                 */
                req.getSession(true);
                req.getSession().setAttribute("userId", userid);
                req.getSession().setAttribute("password", password);

                String page = SeLionGridConstants.GRID_HOME_PAGE_URL;
                CapabilityMatcher matcher = getRegistry().getCapabilityMatcher();
                if (matcher instanceof SeLionSauceCapabilityMatcher) {
                    page = SeLionGridConstants.SAUCE_GRID_HOMEPAGE_URL;
                }

                resp.sendRedirect(page);
            }

        } else {

            /*
             * Login form will be displayed to get user name and password. If already created sessions are available,
             * those sessions will be invalidated
             */
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            ServletHelper.loginToGrid(resp.getWriter(), "Enter username and password");
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        process(request, response);

    }
}
