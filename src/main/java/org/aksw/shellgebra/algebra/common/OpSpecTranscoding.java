package org.aksw.shellgebra.algebra.common;

import java.util.Objects;

public record OpSpecTranscoding(TranscodeMode mode, String name)
    implements OpSpec
{
    public OpSpecTranscoding(TranscodeMode mode, String name) {
        this.name = Objects.requireNonNull(name);
        this.mode = Objects.requireNonNull(mode);
    }

    public static OpSpecTranscoding encode(String name) {
        return new OpSpecTranscoding(TranscodeMode.ENCODE, name);
    }

    public static OpSpecTranscoding decode(String name) {
        return new OpSpecTranscoding(TranscodeMode.DECODE, name);
    }
}
