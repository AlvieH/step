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

import java.io.IOException;
import java.io.*;
import java.util.Vector;
import javax.servlet.annotation.WebServlet;
import com.google.gson.Gson;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.comment.Comment;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/comments")
public class DataServlet extends HttpServlet {

  //Vector holds comment objects
  private final Vector<Comment> comments;

  //Default ctor
  public DataServlet() {
    comments = new Vector<Comment>();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    //get list of Json objects and send response
    String jsonObjects = convertToJson(comments);

    response.setContentType("application/json;");
    response.getWriter().println(jsonObjects);
  }

  //converts given array of Comment objects to json objects using the Gson library
  private String convertToJson(Vector<Comment> comments) {
    return new Gson().toJson(comments);
  }

  //Respond to post request, fetch content from form and submit to master array
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      // get input from form
      Comment userComment = getUserInput(request);

      // append comment to vector
      appendNewComment(userComment);

      // redirect back to HTML page
      response.sendRedirect("/comments.html");
  }

  /* returns user input in comment form */
  private Comment getUserInput(HttpServletRequest request) {
    //get user input from form
    String userCommentString = request.getParameter("comment-field");
    
    return new Comment(userCommentString);
  }

  /* adds new comment to this.comments */
  private void appendNewComment (Comment newComment) {
    comments.add(newComment);
  }
}
