package org.aksw.shellgebra.algebra.stream.op;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.shellgebra.registry.codec.CodecVariant;

public class CodecSpec {
    private String name; // this is the codec name (not the tool name)
    private List<CodecVariant> decoderVariants = new ArrayList<>();
    private List<CodecVariant> encoderVariants = new ArrayList<>();

    private CodecSpec(String name, List<CodecVariant> decoderVariants, List<CodecVariant> encoderVariants) {
        super();
        this.name = name;
        this.decoderVariants = decoderVariants;
        this.encoderVariants = encoderVariants;
    }

    public String getName() {
        return name;
    }

    public List<CodecVariant> getDecoderVariants() {
        return decoderVariants;
    }

    public List<CodecVariant> getEncoderVariants() {
        return encoderVariants;
    }

    public static class Builder {
        private String name;
        private List<CodecVariant> decoderVariants = new ArrayList<>();
        private List<CodecVariant> encoderVariants = new ArrayList<>();

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder addDecoderVariant(CodecVariant codecVariant) {
            this.decoderVariants.add(codecVariant);
            return this;
        }

        public Builder addDecoderVariants(Collection<CodecVariant> codecVariants) {
            this.decoderVariants.addAll(codecVariants);
            return this;
        }

        public Builder addEncoderVariant(CodecVariant codecVariant) {
            this.encoderVariants.add(codecVariant);
            return this;
        }

        public Builder addEncoderVariants(Collection<CodecVariant> codecVariants) {
            this.encoderVariants.addAll(codecVariants);
            return this;
        }

        public CodecSpec build() {
            return new CodecSpec(name, List.copyOf(decoderVariants), List.copyOf(encoderVariants));
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(CodecSpec spec) {
        return newBuilder()
                .setName(spec.getName())
                .addDecoderVariants(spec.getDecoderVariants())
                .addEncoderVariants(spec.getEncoderVariants());
    }
}
