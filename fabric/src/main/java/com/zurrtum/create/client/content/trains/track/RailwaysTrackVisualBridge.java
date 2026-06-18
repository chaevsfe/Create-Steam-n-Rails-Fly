/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.zurrtum.create.client.content.trains.track;

import com.zurrtum.create.content.trains.track.BezierConnection;

public final class RailwaysTrackVisualBridge {
    private RailwaysTrackVisualBridge() {
    }

    public static TrackRenderer.SegmentAngles segmentAngles(BezierConnection connection) {
        return new TrackRenderer.SegmentAngles(connection);
    }
}
