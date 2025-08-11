package org.aksw.shellgebra.algebra.common;

import java.util.Objects;

public record OpSpecTranscoding(String name, TranscodeMode mode)
    implements OpSpec
{
    public OpSpecTranscoding(String name, TranscodeMode mode) {
        this.name = Objects.requireNonNull(name);
        this.mode = Objects.requireNonNull(mode);
    }

    public static OpSpecTranscoding encode(String name) {
        return new OpSpecTranscoding(name, TranscodeMode.ENCODE);
    }

    public static OpSpecTranscoding decode(String name) {
        return new OpSpecTranscoding(name, TranscodeMode.DECODE);
    }
}
