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
import java.util.stream.Collectors;

public final class FindMeetingQuery {

  /**
   * A point in time that is some event's start or end.
   */
  private static class TimePoint {
    public enum Kind {
      Start, End
    }

    private Event event;
    private int time;
    private Kind kind;

    public TimePoint(Event event, int time, Kind kind) {
      this.event = event;
      this.time = time;
      this.kind = kind;
    }

    public Event getEvent() {
      return event;
    }

    public int getTime() {
      return time;
    }

    public Kind getKind() {
      return kind;
    }

    public static int compare(TimePoint lhs, TimePoint rhs) {
      if (lhs.time < rhs.time) {
        return -1;
      }

      if (lhs.time > rhs.time) {
        return 1;
      }

      if (lhs.getKind() == Kind.End && rhs.getKind() == Kind.Start) {
        return -1;
      }

      if (lhs.getKind() == Kind.Start && rhs.getKind() == Kind.End) {
        return 1;
      }

      return 0;
    }
  }

  private List<TimeRange> collapseRanges(List<TimeRange> ranges) {
    List<TimeRange> result = new ArrayList<>();
    TimeRange lastRange = TimeRange.fromStartDuration(0, 0);

    for (TimeRange range : ranges) {
      if (lastRange.end() == range.start()) {
        lastRange = TimeRange.fromStartEnd(lastRange.start(), range.end(), false);
      } else {
        if (lastRange.duration() > 0) {
          result.add(lastRange);
        }
        lastRange = range;
      }
    }

    if (lastRange.duration() > 0) {
      result.add(lastRange);
    }

    return result;
  }

  private List<TimeRange> trySolve(List<TimePoint> points, MeetingRequest request, int minOptionalAttendees) {
    List<TimeRange> result = new ArrayList<>();

    int segmentStart = 0;
    int blockers = 0;
    int optionalAttendees = request.getOptionalAttendees().size();

    for (TimePoint point : points) {
      int segmentEnd = point.getTime();

      if (blockers == 0) {
        if (optionalAttendees >= minOptionalAttendees) {
          result.add(TimeRange.fromStartEnd(segmentStart, segmentEnd, false));
        }
      }

      if (point.getEvent() != null) {
        if (point.getKind() == TimePoint.Kind.Start) {
          for (String attendee : point.getEvent().getAttendees()) {
            if (request.getAttendees().contains(attendee)) {
              ++blockers;
            }

            if (request.getOptionalAttendees().contains(attendee)) {
              --optionalAttendees;
            }
          }
        } else {
          for (String attendee : point.getEvent().getAttendees()) {
            if (request.getAttendees().contains(attendee)) {
              --blockers;
            }

            if (request.getOptionalAttendees().contains(attendee)) {
              ++optionalAttendees;
            }
          }
        }
      }

      segmentStart = segmentEnd;
    }

    result = collapseRanges(result);

    return result.stream()
            .filter(range -> range.duration() >= request.getDuration())
            .collect(Collectors.toList());
  }

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    List<TimePoint> points = new ArrayList<>();

    points.add(new TimePoint(null, TimeRange.WHOLE_DAY.end(), TimePoint.Kind.End));

    for (Event event : events) {
      points.add(new TimePoint(event, event.getWhen().start(), TimePoint.Kind.Start));
      points.add(new TimePoint(event, event.getWhen().end(), TimePoint.Kind.End));
    }

    points.sort(TimePoint::compare);

    // Binary search for optimal number of optional attendees
    // Invariant: the optimal number of optional attendees lies in range [lowerBound, upperBound)
    int lowerBound = 0;
    int upperBound = request.getOptionalAttendees().size() + 1;
    List<TimeRange> result = trySolve(points, request, 0);

    while (upperBound - lowerBound > 1) {
      int mid = (upperBound + lowerBound) / 2;
      result = trySolve(points, request, mid);
      if (result.isEmpty()) {
        upperBound = mid;
      } else {
        lowerBound = mid;
      }
    }

    if (result.isEmpty()) {
      result = trySolve(points, request, lowerBound);
    }

    return result;
  }
}
