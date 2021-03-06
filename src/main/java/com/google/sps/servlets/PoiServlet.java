// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.maps.errors.ApiException;
import com.google.sps.data.UserServiceHelper;
import com.google.sps.data.UserServiceHelper.Callback;
import com.google.sps.enums.CategoryGroup;
import com.google.sps.exception.InvalidCategoryGroupException;
import com.google.sps.object.Office;
import com.google.sps.util.GmapsHelper;
import com.google.sps.util.OfficeManager;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles adding and retreiving listings & locations */
@WebServlet("/poi")
public class PoiServlet extends HttpServlet implements Callback {

  private final GmapsHelper gmapsHelper = GmapsHelper.getInstance();
  private final int radiusMeters = 5000;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserServiceHelper.authUser(this, response, request);
  }

  @Override
  public void handleResponse(HttpServletResponse response, HttpServletRequest request) {
    getOffice(request, response);
  }

  private void getOffice(HttpServletRequest request, HttpServletResponse response) {
    String office = request.getParameter("office");
    int poiGroup = Integer.parseInt(request.getParameter("group"));
    Office selectedOffice = OfficeManager.offices.get(office);

    try {
      CategoryGroup categoryGroup = gmapsHelper.getCategoryGroupById(poiGroup);
      String results =
          gmapsHelper.searchNearbyCategoryGroup(
              categoryGroup,
              selectedOffice.getLatitude(),
              selectedOffice.getLongitude(),
              radiusMeters);
      response.setContentType("application/json;");
      response.getWriter().println(results);
    } catch (ApiException | InvalidCategoryGroupException e) {
      e.printStackTrace();
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    } catch (InterruptedException | IOException e) {
      e.printStackTrace();
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }
  }
}
