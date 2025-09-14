package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.aksw.shellgebra.unused.algebra.plan.InputStreamTransform;
import org.aksw.shellgebra.unused.algebra.plan.OutputStreamTransform;
import org.aksw.shellgebra.unused.algebra.plan.OutputStreamTransformList;

import com.google.common.io.ByteSource;

public class TransformedByteSource
    extends ByteSourceWrapper
{
    private List<InputStreamTransform> transforms;

    public TransformedByteSource(ByteSource delegate, List<InputStreamTransform> transforms) {
        super(Objects.requireNonNull(delegate));
        this.transforms = List.copyOf(transforms);
    }

    public List<InputStreamTransform> getTransforms() {
        return transforms;
    }

    @Override
    public InputStream openStream() throws IOException {
        ByteSource d = getDelegate();
        InputStream r = d.openStream();
        for (InputStreamTransform xform : transforms) {
            try {
                InputStream tmp = xform.apply(r);
                Objects.requireNonNull(tmp, "Transform must not return null");
                r = tmp;
            } catch (RuntimeException | IOException t) {
                t.addSuppressed(new RuntimeException("Error applying stream transform"));
                r.close();
                throw t;
            }
        }
        return r;
    }

    public static ByteSource transform(ByteSource bs, InputStreamTransform transform) {
        ByteSource result;
        if (bs instanceof TransformedByteSource tbs) {
            ByteSource delegate = tbs.getDelegate();
            List<InputStreamTransform> before = tbs.getTransforms();
            List<InputStreamTransform> after = new ArrayList<>(before.size() + 1);
            after.addAll(before);
            after.add(transform);
            result = new TransformedByteSource(delegate, after);
        } else {
            result = new TransformedByteSource(bs, List.of(transform));
        }
        return result;
    }

    /** Separate trailing output transforms from the byte source. */
    public record ByteSourceSplit(ByteSource byteSource, OutputStreamTransformList outTransforms) {}

    public static ByteSourceSplit split(ByteSource bs) {
        ByteSourceSplit result;
        if (bs instanceof TransformedByteSource tbs) {
            ByteSource delegate = tbs.getDelegate();
            List<InputStreamTransform> before = tbs.getTransforms();
            List<OutputStreamTransform> suffix = new ArrayList<>(before.size());

            int i;
            for (i = before.size() - 1; i >= 0; --i) {
                InputStreamTransform ist = before.get(i);
                if (ist.isPiped()) {
                    OutputStreamTransform ost = ist.asOutputStreamTransform();
                    suffix.add(ost);
                } else {
                    break;
                }
            }
            ++i;

            OutputStreamTransformList outTransforms = new OutputStreamTransformList(suffix);
            List<InputStreamTransform> after = before.subList(0, i);

            result = after.isEmpty()
                ? new ByteSourceSplit(delegate, outTransforms)
                : new ByteSourceSplit(new TransformedByteSource(delegate, after), outTransforms);
        } else {
            result = new ByteSourceSplit(bs, new OutputStreamTransformList());
        }
        return result;
    }
}
