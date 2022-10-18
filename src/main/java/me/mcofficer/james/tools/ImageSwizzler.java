package me.mcofficer.james.tools;

import javax.annotation.Nullable;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageSwizzler {

    private final static String[] swizzles = {
            "rbg",
            "grb",
            "brg",
            "gbr",
            "bgr",
            "gbb",
            "rbb",
            "rgg",
            "gbb",
            "ggg",
            "rrr",
            "ggb",
            "gbr",
            "ggr",
            "bgg",
            "brr",
            "grr",
            "bgb",
            "brb",
            "grg",
            "ggb",
            "rrb",
            "rrg",
            "gbg",
            "rbr",
            "rgr",
            "b  ",
            "   "
    };

    /**
     * Swizzles (swaps color channels) an image.
     * If arg is defined, swaps an image's color channels in accordance to arg's value in {@link #swizzles}.
     * Else returns an image with 2x height and 3x width, containing images of all color channel swaps defined in swizzles.
     * @param imgStream The image.
     * @param arg The number of the swizzle that should be applied.
     * @return A .png-encoded image.
     * @throws IOException if an error occurs during reading or writing.
     * Blame {@link ImageIO#read(InputStream)} or {@link ImageWriter#write(IIOMetadata, IIOImage, ImageWriteParam)}.
     */
    public InputStream swizzle(InputStream imgStream, @Nullable String arg) throws IOException {
        BufferedImage img = ImageIO.read(imgStream);
        int[][][] channels = splitByChannels(img);

        BufferedImage output;
        if(arg == null || arg.isEmpty())
            output = createAllSwizzles(img, channels);
        else
            output = createSwizzledImage(img.getWidth(), img.getHeight(), channels, swizzles[Integer.parseInt(arg) - 1]);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
        writer.setOutput(new MemoryCacheImageOutputStream(os));

        ImageWriteParam param = writer.getDefaultWriteParam();
        if(param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.5f);
        }

        writer.write(null, new IIOImage(output, null, null), param);
        writer.dispose();

        return new ByteArrayInputStream(os.toByteArray());
    }

    private BufferedImage createSwizzledImage(int width, int height, int[][][] channels, String swizzle) {

        int channelOne = getChannelIndexByChar(swizzle.charAt(0));
        int channelTwo = getChannelIndexByChar(swizzle.charAt(1));
        int channelThree = getChannelIndexByChar(swizzle.charAt(2));

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for(int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                int a = channels[0][col][row];
                int r = 0;
                int g = 0;
                int b = 0;
                if(channelOne != 4) r = channels[channelOne][col][row]; else r = 0;
                if(channelTwo != 4) g = channels[channelTwo][col][row]; else g = 0;
                if(channelThree != 4) b = channels[channelThree][col][row]; else b = 0;
                int pixel = (a << 24) | (r << 16) | (g << 8) | b;
                img.setRGB(col, row, pixel);
            }
        }
        return img;
    }

    private int getChannelIndexByChar(char c) {
        if(c == 'r') return 1;
        else if(c == 'g') return 2;
        else if(c == 'b') return 3;
        else if(c == ' ') return 4;
        else return 0;
    }

    private int[][][] splitByChannels(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        /* An array that contains 4 channels, with each channel being represented by matrix of integers.
         the channel order is ARGB. */
        int[][][] channels = new int[4][width][height];
        for(int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                int rgb = img.getRGB(col, row);
                channels[0][col][row] = (rgb >> 24) & 0x000000FF;
                channels[1][col][row] = (rgb >> 16) & 0x000000FF;
                channels[2][col][row] = (rgb >> 8) & 0x000000FF;
                channels[3][col][row] = (rgb) & 0x000000FF;
            }
        }
        return  channels;
    }

    private BufferedImage createAllSwizzles(BufferedImage img, int[][][] channels) {
        int width = img.getWidth();
        int height = img.getHeight();

        BufferedImage[] images = new BufferedImage[6];
        for (int i = 0; i < 6; i++)
            images[i] = createSwizzledImage(width, height, channels, swizzles[i]);

        BufferedImage output = new BufferedImage(width * 3, height * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = output.createGraphics();
        g2d.drawImage(images[0], null, 0, 0);
        g2d.drawImage(images[1], null, width, 0);
        g2d.drawImage(images[2], null, width * 2, 0);
        g2d.drawImage(images[3], null, 0, height);
        g2d.drawImage(images[4], null, width, height);
        g2d.drawImage(images[5], null, width * 2, height);
        g2d.dispose();

        return output;
    }
}