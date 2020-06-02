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
import javax.servlet.annotation.WebServlet;
import com.google.gson.Gson;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.comment.Comment;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/comments")
public class DataServlet extends HttpServlet {

  //boilerplate test comments
  private final String[] testCommentStrings = {"I hate this website!", "I am neutral about this website", "I love this website!"};

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // import and process test comment strings
    Comment[] testComments = new Comment[testCommentStrings.length];
    for(int c = 0; c < testCommentStrings.length; ++c)
    {
      testComments[c] = new Comment(testCommentStrings[c]);
    }

    //get list of Json objects
    String jsonObjects = convertToJson(testComments);

    //print Json objects
    response.setContentType("application/json;");
    response.getWriter().println(jsonObjects);
  }

  //converts given array of Comment objects to json objects using the Gson library
  private String convertToJson(Comment[] comments) {
    return new Gson().toJson(comments);
  }
}
