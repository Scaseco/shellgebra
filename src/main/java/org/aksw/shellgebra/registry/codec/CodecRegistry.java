package org.aksw.shellgebra.registry.codec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.aksw.shellgebra.algebra.common.TranscodeMode;
import org.aksw.shellgebra.algebra.common.Transcoding;
import org.aksw.shellgebra.algebra.stream.op.CodecSpec;

// So there are two similar but different views:
// - The 'source' view: Supplier<InputStream> Here we take an op and build an input stream from it.
// - The 'transformer' view: Function<InputStream, InputStream> Here we build a transformation to a byte stream from form A to form B.

/**
 * Currently, the CodecVariants accept a file argument (may be bash process substitution) and are assumed to stream to stdout.
 */
public class CodecRegistry {
    // Host Commands
    private Map<String, CodecSpec> registry = new HashMap<>();

    private List<JavaCodecProvider> javaCodecProviders = new ArrayList<>();

    private static CodecRegistry defaultRegistry = null;

    public static CodecRegistry get() {
        if (defaultRegistry == null) {
            synchronized (CodecRegistry.class) {
                if (defaultRegistry == null) {
                    defaultRegistry = new CodecRegistry();
                    loadDefaults(defaultRegistry);
                }
            }
        }
        return defaultRegistry;
    }

    public Optional<CodecSpec> getCodecSpec(String name) {
        return Optional.ofNullable(registry.get(name));
    }

    public List<CodecVariant> getEncoders(String name) {
        List<CodecVariant> result = getCodecSpec(name).stream()
            .map(CodecSpec::getEncoderVariants)
            .flatMap(Collection::stream)
            .toList();
        return result;
    }

    public List<CodecVariant> getDecoders(String name) {
        List<CodecVariant> result = getCodecSpec(name).stream()
            .map(CodecSpec::getDecoderVariants)
            .flatMap(Collection::stream)
            .toList();
        return result;
    }

//    public CodecSpec requireCodec(String name) {
//        CodecSpec result = getCodecSpec(name);
//        if (result == null) {
//            throw new NoSuchElementException("No codec with name " + name);
//        }
//        return result;
//    }

    public CodecRegistry add(CodecSpec spec) {
        registry.put(spec.getName(), spec);
        return this;
    }

    public static void loadDefaults(CodecRegistry registry) {

        // Add a codec command line builder for the tool
        // then use the tool registry to make the lookup for the tool.
        {
            CodecSpec spec = CodecSpec.newBuilder()
                .setName("bzip2")
                .addDecoderVariant(CodecVariant.of("lbzip2", "-cd"))
                .addDecoderVariant(CodecVariant.of("bzip2", "-cd"))
                .addEncoderVariant(CodecVariant.of("lbzip2", "-c"))
                .addEncoderVariant(CodecVariant.of("bzip2", "-c"))
                .build();
            registry.add(spec);
        }

        {
            CodecSpec spec = CodecSpec.newBuilder()
                .setName("gz") // reuse "gz" which is the name in commons-compress.
                .addDecoderVariant(CodecVariant.of("gzip", "-cd"))
                .addEncoderVariant(CodecVariant.of("gzip", "-c"))
                .build();
            registry.add(spec);
        }

//        {
//            CodecSpec spec = new CodecSpec("cat");
//            spec.getDecoderVariants().add(CodecVariant.of("cat"));
//            registry.add(spec);
//        }

        registry.addJavaCodecProvider(new JavaCodecProviderOverCommonsCompress());
    }

    public Optional<JavaStreamTransform> getJavaCodec(Transcoding transcoding) {
        return getJavaCodec(transcoding.name(), transcoding.mode());
    }

    public Optional<JavaStreamTransform> getJavaCodec(String javaName, TranscodeMode mode) {
        Optional<JavaStreamTransform> result = javaCodecProviders.stream()
            .map(provider -> provider.getCodec(javaName))
            .flatMap(Optional::stream)
            .map(codec -> TranscodeMode.DECODE.equals(mode) ? codec.decoder() : codec.encoder())
            // XXX Validate that provider does not return a transform record with only null values.
            // .filter(st -> st.inputStreamTransform() != null || st.outputStreamTransform() != null)
            .findFirst();
        return result;
    }

    public Optional<JavaCodec> getJavaCodec(String javaName) {
        Optional<JavaCodec> result = javaCodecProviders.stream()
            .map(provider -> provider.getCodec(javaName))
            .flatMap(Optional::stream)
            .findFirst();
        return result;
    }

    public JavaCodec requireJavaCodec(String javaName) {
        JavaCodec result = getJavaCodec(javaName)
            .orElseThrow(() -> new NoSuchElementException(javaName));
        return result;
    }

    public CodecRegistry addJavaCodecProvider(JavaCodecProvider provider) {
        javaCodecProviders.add(0, provider);
        return this;
    }

//    protected CodecRegistry self() {
//        return this;
//    }

    /*
    public static void main(String[] args) throws IOException {
        CodecRegistry reg = CodecRegistry.get();
        SysRuntime runtime = SysRuntimeImpl.forBash();
        CodecSysEnv env = new CodecSysEnv(runtime);
        CodecTransformSys transform = new CodecTransformSys(reg, env); // , CodecTransformSys.Mode.COMMAND_GROUP);

        CodecOpVisitorStream javaStreamer = CodecOpVisitorStream.getSingleton();

        CodecOp op = new CodecOpFile("/home/raven/tmp/codec-test/test.txt.bz2.gz");
        op = new CodecOpCodecName("gz", op);
        op = new CodecOpCodecName("bzip2", op);
        System.out.println(op);

        try (InputStream xin = op.accept(javaStreamer)) {
            System.out.println(IOUtils.toString(xin, StandardCharsets.UTF_8));
        }

        CodecOp cmd = CodecOpTransformer.transform(op, transform);
        System.out.println(cmd);

        try (InputStream xin = cmd.accept(javaStreamer)) {
            System.out.println(IOUtils.toString(xin, StandardCharsets.UTF_8));
        }

        System.out.println(cmd);
    }
    */
}
