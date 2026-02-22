package org.aksw.shellgebra.registry.codec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.commons.util.docker.Argv;

/**
 * Specification for which commands can be used to encode/decode the codec with the given name.
 */
public class CodecSpec {
    private String name; // this is the codec name (not the tool name)
    private List<Argv> decoderVariants = new ArrayList<>();
    private List<Argv> encoderVariants = new ArrayList<>();

    private CodecSpec(String name, List<Argv> decoderVariants, List<Argv> encoderVariants) {
        super();
        this.name = name;
        this.decoderVariants = decoderVariants;
        this.encoderVariants = encoderVariants;
    }

    public String getName() {
        return name;
    }

    public List<Argv> getDecoderVariants() {
        return decoderVariants;
    }

    public List<Argv> getEncoderVariants() {
        return encoderVariants;
    }

    public static class Builder {
        private String name;
        private List<Argv> decoderVariants = new ArrayList<>();
        private List<Argv> encoderVariants = new ArrayList<>();

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder addDecoderVariant(String... codecVariant) {
            addDecoderVariant(List.of(codecVariant));
            return this;
        }

        public Builder addDecoderVariant(List<String> codecVariant) {
            addDecoderVariant(Argv.of(codecVariant));
            return this;
        }

        protected Builder addDecoderVariant(Argv codecVariant) {
            this.decoderVariants.add(codecVariant);
            return this;
        }

        public Builder addDecoderVariants(Collection<Argv> codecVariants) {
            this.decoderVariants.addAll(codecVariants);
            return this;
        }

        public Builder addEncoderVariant(List<String> codecVariant) {
            addEncoderVariant(Argv.of(codecVariant));
            return this;
        }

        public Builder addEncoderVariant(String... codecVariant) {
            addEncoderVariant(List.of(codecVariant));
            return this;
        }

        protected Builder addEncoderVariant(Argv codecVariant) {
            this.encoderVariants.add(codecVariant);
            return this;
        }

        public Builder addEncoderVariants(Collection<Argv> codecVariants) {
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
