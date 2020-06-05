// Copyright 2020 Google LLC
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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.io.IOException;
import java.io.*;
import java.util.Vector;
import javax.servlet.annotation.WebServlet;
import com.google.gson.Gson;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.comment.Comment;

/** Servlet that is responsible for listing comments from datastore database */
@WebServlet("/list-comments")
public class ListCommentsServlet extends HttpServlet {
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment").addSort("time", SortDirection.ASCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    // Process and store raw data from query into comments
    Vector<Comment> allComments = new Vector();
    for(Entity entity : results.asIterable()) {
      String commentText = (String) entity.getProperty("text");
      allComments.add(new Comment(commentText));
    }

    // Filter allComments down to the most recent n, where n was the user selection
    int numComments = getNumComments(request);
    Vector<Comment> shownComments = newestComments(allComments, numComments);

    // Get list of Json objects and send response
    String jsonObjects = convertToJson(shownComments);

    response.setContentType("application/json;");
    response.getWriter().println(jsonObjects);
  }

  // Converts given array of Comment objects to json objects using the Gson library
  private String convertToJson(Vector<Comment> comments) {
    return new Gson().toJson(comments);
  }

  // Gets 'num-comments' parameter from request and interprets.
  // Returns positive int if num-comments is not 'all', 0 if 'all' was selected
  private int getNumComments (HttpServletRequest request) {
    String numCommentsString = request.getParameter("num-comments");
    
    if (numCommentsString.equals("all")) {
      return 0;
    }
    else {
      return Integer.parseInt(numCommentsString);
    }
  }

  // Returns vector of newest n comments, where the returned vector is a subset of allComments and n=numComments.
  private Vector<Comment> newestComments (Vector<Comment> allComments, int numComments) {
    if (numComments == 0 || numComments > allComments.size()) {
        numComments = allComments.size();
    }

    Vector<Comment> shownComments = new Vector(numComments);
    shownComments.setSize(numComments);
    int allCommentsIndex = allComments.size() - numComments;

    // Copy last n comments into shownComments
    for (int i = 0; i < numComments; i++, allCommentsIndex++) {
      shownComments.setElementAt(allComments.get(allCommentsIndex), i);
    }

    return shownComments;
  }
}
