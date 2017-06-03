package band.full.testing.video.generate.hevc;

import static band.full.testing.video.core.Resolution.STD_2160p;
import static java.time.Duration.ofSeconds;

import band.full.testing.video.core.CanvasYCbCr;
import band.full.testing.video.encoder.EncoderHEVC;
import band.full.testing.video.generate.GenerateVideo;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Random;

/**
 * Demonstrating sub-sample detail and gradients with different dithering
 * methods and different bit depths.
 *
 * @author Igor Malinin
 */
@Category(GenerateVideo.class)
public class Gradients2160pHEVC {
    @Ignore("Requires lossless encode and"
            + " reducing bitrate to at least 100Mb/s")
    @Test
    public void gradients() {
        gradients("HEVC/Gradients-06");
    }

    public void gradients(String name) {
        EncoderHEVC.encode(name, e -> {
            CanvasYCbCr c = e.newCanvas();
            c.Cb.fill(c.parameters.ACHROMATIC);
            c.Cr.fill(c.parameters.ACHROMATIC);
            e.render(ofSeconds(10), () -> gradients(c));
        });
    }

    /** Render with new dither per frame */
    private CanvasYCbCr gradients(CanvasYCbCr canvas) {
        canvas.Y.calculate(this::fn);
        return canvas;
    }

    private static final int REMOVE_BITS = 2;
    private static final int REMOVE_BITS_RATIO = 1 << REMOVE_BITS;
    private static final int RANGE = 40;
    private static final int WIDTH = STD_2160p.width;
    private static final double INT_DIVIDER = 1L << 32;

    private static final Random PRNG = new Random();

    private static final double rnd() {
        return PRNG.nextInt() / INT_DIVIDER;
    }

    private int fn(int x, int y) {
        double val = ((double) x) / WIDTH * RANGE;

        if (y < 540) {
            val = ((int) (val * 2.0)) / 2.0; // emulate 1 additional bit
            val += rnd();

            return 64 + ((int) val) * REMOVE_BITS_RATIO;
        }

        if (y == 540 || y == 1080 || y == 1620) return 16;
        if (y > 1080) {
            val += rnd(); // RPDF
        }
        if (y > 1620) {
            val += rnd(); // 2RPDF = TPDF
        }

        return 64 + ((int) val) * REMOVE_BITS_RATIO;
    }
}