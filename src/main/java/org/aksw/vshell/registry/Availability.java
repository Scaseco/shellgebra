package org.aksw.vshell.registry;

import java.time.Instant;

public record Availability(boolean available, Throwable throwable, Instant timeStamp) {}
