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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class FindMeetingQuery {

  /**
   * A point in time that is some event's start or end.
   */
  private static class TimePoint {
    private Event event;
    private int time;
    private boolean start;

    public TimePoint(Event event, int time, boolean start) {
      this.event = event;
      this.time = time;
      this.start = start;
    }

    public Event getEvent() {
      return event;
    }

    public void setEvent(Event event) {
      this.event = event;
    }

    public int getTime() {
      return time;
    }

    public void setTime(int time) {
      this.time = time;
    }

    public boolean isStart() {
      return start;
    }

    public void setStart(boolean start) {
      this.start = start;
    }

    public static int compare(TimePoint lhs, TimePoint rhs) {
      if (lhs.time < rhs.time) {
        return -1;
      }

      if (lhs.time > rhs.time) {
        return 1;
      }

      if (!lhs.isStart() && rhs.isStart()) {
        return -1;
      }

      if (lhs.isStart() && !rhs.isStart()) {
        return 1;
      }

      return 0;
    }
  }

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    List<TimePoint> points = new ArrayList<>();
    List<TimeRange> result = new ArrayList<>();

    points.add(new TimePoint(null, TimeRange.WHOLE_DAY.end(), false));

    for (Event event : events) {
      points.add(new TimePoint(event, event.getWhen().start(), true));
      points.add(new TimePoint(event, event.getWhen().end(), false));
    }

    points.sort(TimePoint::compare);

    int segmentStart = 0;
    int noBlockersStart = 0;
    int blockers = 0;

    for (TimePoint point : points) {
      int segmentEnd = point.getTime();
      boolean wasBlocker = (blockers > 0);

      if (point.getEvent() != null) {
        if (point.isStart()) {
          for (String attendee : point.getEvent().getAttendees()) {
            if (request.getAttendees().contains(attendee)) {
              ++blockers;
            }
          }
        } else {
          for (String attendee : point.getEvent().getAttendees()) {
            if (request.getAttendees().contains(attendee)) {
              --blockers;
            }
          }
        }
      }

      if (blockers == 0 && wasBlocker) {
        noBlockersStart = segmentEnd;
      }

      if ((blockers > 0 || point.getEvent() == null) && !wasBlocker) {
        int duration = segmentEnd - noBlockersStart;
        if (duration >= request.getDuration()) {
          result.add(TimeRange.fromStartDuration(noBlockersStart, duration));
        }
      }

      segmentStart = segmentEnd;
    }

    return result;
  }
}
