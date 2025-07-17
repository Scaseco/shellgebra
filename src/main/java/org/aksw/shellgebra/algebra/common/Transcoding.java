package org.aksw.shellgebra.algebra.common;

import java.util.Objects;

public record Transcoding(String name, TranscodeMode mode) {
    public Transcoding(String name, TranscodeMode mode) {
        this.name = Objects.requireNonNull(name);
        this.mode = Objects.requireNonNull(mode);
    }

    public static Transcoding encode(String name) {
        return new Transcoding(name, TranscodeMode.ENCODE);
    }

    public static Transcoding decode(String name) {
        return new Transcoding(name, TranscodeMode.DECODE);
    }
}
