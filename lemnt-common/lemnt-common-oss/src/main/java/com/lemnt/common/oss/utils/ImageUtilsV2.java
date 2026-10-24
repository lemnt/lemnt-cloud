package com.lemnt.common.oss.utils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * 图像等比压缩工具类
 *
 * @author: zc
 * @date: 2024/11/19 15:52
 * @version: 1.0.0
 */
public class ImageUtilsV2 {

    /**
     * 目标图片格式
     */
    private static final String[] SUPPORTED_FORMATS = {"jpeg", "png", "gif", "bmp", "webp"};
    /**
     * 目标图片格式
     */
    private static final String TARGET_FORMAT = "jpg";
    /**
     * 白色背景（用于处理透明通道）
     */
    private static final Color TRANSPARENT_BACKGROUND = Color.WHITE;

    // 简单的后缀到Content-Type映射表
    public static final Map<String, String> SUFFIX_CONTENT_TYPE_MAP = new HashMap<>();

    public static final List<String> WHITE_IP_LIST = Arrays.asList("111.175.90.254");


    static {
        SUFFIX_CONTENT_TYPE_MAP.put("jpg", "image/jpeg");
        SUFFIX_CONTENT_TYPE_MAP.put("jpeg", "image/jpeg");
        SUFFIX_CONTENT_TYPE_MAP.put("png", "image/png");
        SUFFIX_CONTENT_TYPE_MAP.put("gif", "image/gif");
        SUFFIX_CONTENT_TYPE_MAP.put("bmp", "image/bmp");
        SUFFIX_CONTENT_TYPE_MAP.put("webp", "image/webp");
        SUFFIX_CONTENT_TYPE_MAP.put("mp4", "video/mp4");
        SUFFIX_CONTENT_TYPE_MAP.put("mp3", "audio/mpeg");
        SUFFIX_CONTENT_TYPE_MAP.put("pdf", "application/pdf");
        SUFFIX_CONTENT_TYPE_MAP.put("txt", "text/plain");
        SUFFIX_CONTENT_TYPE_MAP.put("html", "text/html");
        SUFFIX_CONTENT_TYPE_MAP.put("json", "application/json");
        SUFFIX_CONTENT_TYPE_MAP.put("xml", "application/xml");
        // 你可以继续补充更多后缀和ContentType
    }

    /**
     * 获取文件的 Content-Type
     * 1. 如果传入的 contentType 不为空则直接返回
     * 2. 否则根据文件名后缀自动推断
     * 3. 推断不到时返回默认值 application/octet-stream
     *
     * @param contentType 当前contentType，可能为null或空
     * @param fileName    文件名
     * @return 确定后的Content-Type字符串
     */
    public static String getContentTypeAuto(String contentType, String fileName) {
        if (contentType != null && !contentType.trim().isEmpty()) {
            return contentType;
        }
        // 通过后缀推断
        String suffix = getSuffix(fileName);
        if (suffix.isEmpty()) {
            return "application/octet-stream"; // 默认二进制流
        }
        String type = SUFFIX_CONTENT_TYPE_MAP.get(suffix.toLowerCase());
        return type != null ? type : "application/octet-stream";
    }

    /**
     * 提取文件后缀  - 识别不准
     *
     * @param fileName 文件名
     * @return 后缀字符串，不带点，例如 "jpg"
     */
    public static String getSuffix(String fileName) {
        if (fileName == null) {
            return "";
        }
        int idx = fileName.lastIndexOf('.');
        return (idx != -1 && idx < fileName.length() - 1) ? fileName.substring(idx + 1).toLowerCase() : "";
    }

    /**
     * 根据 contentType 返回通用文件后缀（不带 .）
     *
     * @param contentType MIME 类型，例如 "image/png"
     * @return 后缀名，例如 "png"，如果不识别则返回 "bin"
     */
    public static String getSuffixFromContentType(String contentType) {
        if (contentType == null) {
            return "bin";
        }
        ;

        switch (contentType) {
            // 图片
            case "image/jpeg":
                return "jpg";
            case "image/pjpeg":
                return "jpg"; // IE 兼容
            case "image/png":
                return "png";
            case "image/gif":
                return "gif";
            case "image/bmp":
                return "bmp";
            case "image/webp":
                return "webp";
            case "image/tiff":
                return "tiff";
            case "image/x-icon":
            case "image/vnd.microsoft.icon":
                return "ico";

            // 视频
            case "video/mp4":
                return "mp4";
            case "video/quicktime":
                return "mp4";
            case "video/x-msvideo":
                return "avi";
            case "video/x-matroska":
                return "mkv";
            case "video/webm":
                return "webm";
            case "video/mpeg":
                return "mpeg";

            // 音频
            case "audio/mpeg":
                return "mp3";
            case "audio/wav":
                return "wav";
            case "audio/ogg":
                return "ogg";
            case "audio/flac":
                return "flac";

            // 文档类
            case "application/pdf":
                return "pdf";
            case "application/msword":
                return "doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                return "docx";
            case "application/vnd.ms-excel":
                return "xls";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                return "xlsx";
            case "application/vnd.ms-powerpoint":
                return "ppt";
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                return "pptx";
            case "text/plain":
                return "txt";
            case "text/html":
                return "html";
            case "application/json":
                return "json";
            case "application/xml":
            case "text/xml":
                return "xml";

            default:
                // 默认尝试提取后缀（如 "image/custom-type" -> "custom-type"）
                int slashIndex = contentType.lastIndexOf('/');
                if (slashIndex >= 0 && slashIndex < contentType.length() - 1) {
                    return contentType.substring(slashIndex + 1);
                }
                return "bin";
        }
    }

    /**
     * 获取图片文件流
     *
     * @param imageUrl 图片网络地址
     * @return 文件流
     */
    public static byte[] getInputStream(String imageUrl) {
        try (InputStream inputStream = new URL(imageUrl).openStream()) {
            // 将输入流完全读入字节数组（缓存）
            byte[] imageData = inputStream.readAllBytes();
            // 1. 检测真实图片格式
            String actualFormat = detectImageFormat(new ByteArrayInputStream(imageData));

            // 2. 格式验证和转换
            if (!isSupportedFormat(actualFormat)) {
                return null;
            }
            return convertToJpg(new ByteArrayInputStream(imageData));
        } catch (IOException e) {
            throw new RuntimeException("Image processing failed", e);
        }
    }

    public static byte[] getFileByte(String filePath) {
        try {
            // 通过URL对象创建连接
            HttpURLConnection httpUrl = (HttpURLConnection) new URL(filePath).openConnection();
            httpUrl.connect();

            // 读取网络资源的字节对象
            InputStream inputStream = httpUrl.getInputStream();
            return inputStream.readAllBytes();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检测图片真实格式
     */
    public static String detectImageFormat(InputStream inputStream) throws IOException {
        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                return reader.getFormatName().toLowerCase();
            }
            throw new IOException("Cannot detect image format");
        }
    }

    /**
     * 判断是否为需要处理的格式
     */
    public static Boolean isSupportedFormat(String format) {
        for (String supported : SUPPORTED_FORMATS) {
            if (supported.equals(format)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 图片格式转换
     */
    public static byte[] convertToJpg(InputStream inputStream) throws IOException {
        BufferedImage originalImage = ImageIO.read(inputStream);
        if (originalImage == null) {
            throw new IOException("Failed to read image");
        }

        // 处理透明背景
        BufferedImage convertedImage = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );
        Graphics2D g = convertedImage.createGraphics();
        g.setBackground(TRANSPARENT_BACKGROUND);
        g.clearRect(0, 0, originalImage.getWidth(), originalImage.getHeight());
        g.drawImage(originalImage, 0, 0, null);
        g.dispose();
        // 转换为JPG格式
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(convertedImage, TARGET_FORMAT, outputStream);
        // 设置JPG压缩质量（0.85为平衡质量和大小的值）
        return outputStream.toByteArray();
    }

    /**
     * 压缩图片，按给定的宽度或高度等比压缩图片，并将其保存为 JPEG 格式
     *
     * @param inputImage 输入图片流-原图
     * @param outputFile 输出文件-压缩后图
     * @param width      新的宽度
     * @param height     新的高度
     * @throws IOException
     */
    public static void resizeAndSaveImage(InputStream inputImage, File outputFile, int width, int height) throws IOException {
        // 读取原图
        BufferedImage originalImage = ImageIO.read(inputImage);

        // 计算等比缩放后的尺寸
        int newWidth = width;
        int newHeight = height;

        double aspectRatio = (double) originalImage.getWidth() / originalImage.getHeight();
        if (width > 0 && height == 0) {
            // 按宽度压缩
            newHeight = (int) (newWidth / aspectRatio);
        } else if (height > 0 && width == 0) {
            // 按高度压缩
            newWidth = (int) (newHeight * aspectRatio);
        } else {
            // 如果都传入了，默认按宽度调整，高度保持等比
            newHeight = (int) (newWidth / aspectRatio);
        }
        // 创建一个新的图像对象，进行缩放
        Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

        // 将缩放后的图像绘制到新图像中
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        // 保存为 JPEG 格式
        ImageIO.write(resizedImage, "JPEG", outputFile);
    }

    /**
     * 压缩图片，按给定的宽度或高度等比压缩图片，并将其保存为 JPEG 格式
     *
     * @param inputImage 输入图片流-原图
     * @param width      新的宽度
     * @param height     新的高度
     * @throws IOException
     */
    public static byte[] resizeAndSaveImage(InputStream inputImage, int width, int height) throws IOException {
        // 读取原图
        BufferedImage originalImage = ImageIO.read(inputImage);

        // 计算等比缩放后的尺寸
        int newWidth = width;
        int newHeight = height;

        double aspectRatio = (double) originalImage.getWidth() / originalImage.getHeight();
        if (width > 0 && height == 0) {
            // 按宽度压缩
            newHeight = (int) (newWidth / aspectRatio);
        } else if (height > 0 && width == 0) {
            // 按高度压缩
            newWidth = (int) (newHeight * aspectRatio);
        } else {
            // 如果都传入了，默认按宽度调整，高度保持等比
            newHeight = (int) (newWidth / aspectRatio);
        }
        // 判断图片是否透明
        boolean isTransparent = isImageTransparent(originalImage);
        // 创建一个新的图像对象，进行缩放
        Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, isTransparent ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);

        // 将缩放后的图像绘制到新图像中
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setComposite(AlphaComposite.Src);
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        // 保存为 JPEG 格式
        ImageIO.write(resizedImage, isTransparent ? "PNG" : "JPEG", bao);
        return bao.toByteArray();
    }

    /**
     * 将图像转换为 JPEG 格式--jpg自动，一般不用强制使用此方法
     *
     * @param inputImage 输入图像文件
     * @param outputFile 输出文件
     * @throws IOException
     */
    public static void convertToJpeg(File inputImage, File outputFile) throws IOException {
        BufferedImage image = ImageIO.read(inputImage);
        // 将图片保存为 JPEG 格式
        ImageIO.write(image, "JPEG", outputFile);
    }

    /**
     * 从文件加载图片
     *
     * @param imagePath 图片路径
     * @return BufferedImage
     * @throws IOException
     */
    public static BufferedImage loadImageFromFile(String imagePath) throws IOException {
        return ImageIO.read(new File(imagePath));
    }

    public static boolean isImageTransparent(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xff;
                if (alpha < 255) {
                    return true; // 找到透明像素
                }
            }
        }
        return false; // 没有找到透明像素
    }

    /**
     * 获取网络图片的宽高（高效方法，仅读取图片头信息）
     *
     * @param imageUrl 图片完整URL
     * @return 包含宽高的数组 [width, height]，失败时返回null
     */
    public static int[] getImageDimensions(String imageUrl) {
        try (InputStream is = new URL(imageUrl).openStream();
             ImageInputStream iis = ImageIO.createImageInputStream(is)) {

            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                System.err.println("不支持该图片格式: " + imageUrl);
                return null;
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(iis);
                return new int[]{
                        reader.getWidth(0),  // 宽度
                        reader.getHeight(0) // 高度
                };
            } finally {
                reader.dispose();
            }

        } catch (Exception e) {
            System.err.println("获取图片尺寸失败: " + e.getMessage());
            return null;
        }
    }

}
