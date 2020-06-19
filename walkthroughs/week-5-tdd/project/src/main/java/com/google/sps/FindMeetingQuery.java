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

package com.google.sps;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.List;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // In this case, long to int conversion is safe because duration can never exceed 2^32. Leaving duration as long
    // leads to compile errors.
    int duration = (int) request.getDuration();

    // If request is longer than length of day, then there would never be any options
    if (duration > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }

    // Seperate relevant TimeRanges from events, put into ArrayList and sort by ascending meeting start time
    // Optional attendees are handled by running two sets of queries in parallel: a query if we are including optional attendees,
    // and a query if we are only considering required attendees. If the optional attendee query is empty, then that means that 
    // there are no valid times if we include optional attendees, and optional attendees are ignored.
    List<TimeRange> attendedMeetings = new ArrayList<>();
    List<TimeRange> allAttendedMeetings = new ArrayList<>();
    for (Event event : events) {
      // First check if the event in question contains people from request, add those meetings to attendedMeetings
      // Attendees is only required attendees, allAttendees includes optional attendees
      Set<String> requiredAttendees = new HashSet<>(request.getAttendees());
      Set<String> optionalAttendees = new HashSet<>(request.getOptionalAttendees());
      Set<String> allAttendees = new HashSet<>(Sets.union(requiredAttendees, optionalAttendees));
      
      // I couldn't think of a way to factor this, I'm sorry Zu :'(
      requiredAttendees.retainAll(event.getAttendees());
      allAttendees.retainAll(event.getAttendees());

      if (!requiredAttendees.isEmpty()){
        attendedMeetings.add(event.getWhen());
      }

      if (!allAttendees.isEmpty()) {
        allAttendedMeetings.add(event.getWhen());
      }

    }

    // Sort attendedMeetings so that we can filter out all nested meetings in next step
    Collections.sort(attendedMeetings, TimeRange.ORDER_BY_START);
    Collections.sort(allAttendedMeetings, TimeRange.ORDER_BY_START);

    // All openings represents the availabilities WITH optional attendees, requiredOpenings represents only required attendees.
    // if allOpenings is empty, that means that there is a conflict with optional attendees and optional attendees should be ignored.
    List<TimeRange> requiredOpenings = new ArrayList<>(findOpenings(attendedMeetings, duration));
    List<TimeRange> allOpenings = new ArrayList<>(findOpenings(allAttendedMeetings, duration));

    return allOpenings.size() == 0 ? requiredOpenings : allOpenings;
  }

  // Takes in sorted list of meetings and returns next available TimeRange given, or null if none available with current startTime
  private TimeRange findNextTime(List<TimeRange> meetings, int startMeetingIndex, int duration) {
    int startTime = meetings.get(startMeetingIndex).end();

    // If this is the last meeting in the list and there's a meeting time that fits, return a meeting time.
    if (startMeetingIndex == meetings.size() - 1) {
      return (startTime + duration <= TimeRange.END_OF_DAY) ? 
            TimeRange.fromStartEnd(startTime, TimeRange.END_OF_DAY, true) 
            : null;
    }

    // If next meeting's start time is farther away than duration,
    // return a TimeRange from startTime -> the start of the next meeting.
    if (meetings.get(startMeetingIndex + 1).start() - startTime >= duration) {
      return TimeRange.fromStartEnd(startTime, meetings.get(startMeetingIndex + 1).start(), false);
    }
    else {
      return null;
    }
  }

  // Takes in sorted list of TimeRanges, filters out nested meetings, and then passes into findNextTime
  private List<TimeRange> findOpenings(List<TimeRange> meetings, int duration) {
     List<TimeRange> validMeetings = new ArrayList<>();

    for (TimeRange meeting : meetings) {
      
      int numMeetings = validMeetings.size();

      // Fill in validMeetings. Meetings are sorted by start and end times. If multiple meetings
      // start at the same time, then one or more will be added, with the longest always being kept
      // and the shortest either being kept (if they come before the longest) or removed (if they come
      // after the longest). These meetings will then be sorted out later.
      if(numMeetings == 0 
        || meeting.end() > validMeetings.get(numMeetings - 1).end()) {
        validMeetings.add(meeting);
      }

    }
    // If there are no remaining meetings, entire day is free
    if (validMeetings.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    
    // Check if time before first meeting is an opening 
    List<TimeRange> openings = new ArrayList<>();
    if (validMeetings.get(0).start() - TimeRange.START_OF_DAY >= duration) {
      openings.add(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, validMeetings.get(0).start(), false));
    }

    // Run findNextTime on each element of meetings in order to find all stretches of available time
    for (int i = 0; i < validMeetings.size(); ++i) {
      TimeRange meetingTime = findNextTime(validMeetings, i, duration);
      if (meetingTime != null) {
        openings.add(meetingTime);
      }
    }
    
    return openings;
  }
}
