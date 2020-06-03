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

    // process and store raw data from query into comments
    Vector<Comment> comments = new Vector();
    for(Entity entity : results.asIterable()) {
      String commentText = (String) entity.getProperty("text");
      comments.add(new Comment(commentText));
    }

    //get list of Json objects and send response
    String jsonObjects = convertToJson(comments);

    response.setContentType("application/json;");
    response.getWriter().println(jsonObjects);
  }

  //converts given array of Comment objects to json objects using the Gson library
  private String convertToJson(Vector<Comment> comments) {
    return new Gson().toJson(comments);
  }
}
