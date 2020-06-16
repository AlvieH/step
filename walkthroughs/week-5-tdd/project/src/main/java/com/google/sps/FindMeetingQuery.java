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

import java.util.*;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    int duration = (int)request.getDuration();

    // If request is longer than length of day, then there would never be any options
    if (duration > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }

    // Seperate relevant TimeRanges from events, put into ArrayList and sort by ascending meeting start time
    ArrayList<TimeRange> meetings = new ArrayList<TimeRange>();
    events.forEach(event -> {
      // First check if the event in question contains people from request
      Set<String> attendees = new HashSet<String>(request.getAttendees());
      attendees.retainAll(event.getAttendees());

      int numMeetings = meetings.size();

      // Only add meeting if A) there are people attending and B) it's not nested within the previous meeting
      if(!attendees.isEmpty() && 
        (numMeetings == 0 || event.getWhen().end() > meetings.get(numMeetings - 1).end())) {
        meetings.add(event.getWhen());
      }

    });

    Collections.sort(meetings, TimeRange.ORDER_BY_START);

    // If there are no remaining meetings, entire day is free
    if (meetings.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    
    // Check if time before first meeting is an opening 
    ArrayList<TimeRange> openings = new ArrayList<TimeRange>();
    if (meetings.get(0).start() - TimeRange.START_OF_DAY >= duration) {
      openings.add(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, meetings.get(0).start(), false));
    }

    // Run findNextTime on each element of meetings in order to find all stretches of available time
    for (int i = 0; i < meetings.size(); ++i) {
      TimeRange meetingTime = findNextTime(meetings, i, duration);
      if (meetingTime != null) {
        openings.add(meetingTime);
      }
    }
    
    return openings;
  }

  // Takes in sorted list of meetings and returns next available TimeRange given, or null if none available with current startTime
  private TimeRange findNextTime(ArrayList<TimeRange> meetings, int startMeetingIndex, int duration) {
    int startTime = meetings.get(startMeetingIndex).end();

    // If this is the last meeting in the list and there's a meeting time that fits, return a meeting time.
    if (startMeetingIndex == meetings.size() - 1) {
      return (startTime + duration <= TimeRange.END_OF_DAY) ? 
            TimeRange.fromStartEnd(startTime, TimeRange.END_OF_DAY, true) 
            : null;
    }

    /* If next meeting's start time is farther away than duration,
     * return a TimeRange from startTime -> the start of the next meeting. */
    if (meetings.get(startMeetingIndex + 1).start() - startTime >= duration) {
      return TimeRange.fromStartEnd(startTime, meetings.get(startMeetingIndex + 1).start(), false);
    }
    else {
      return null;
    }
  }
}
