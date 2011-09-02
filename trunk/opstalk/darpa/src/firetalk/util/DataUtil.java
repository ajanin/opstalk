package firetalk.util;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/*
 * @author mkyong
 *
 */
public class DataUtil {

//	public static void main(String[] args) {
//
//		try {
//
//			BufferedImage originalImage = ImageIO.read(new File(
//					"c:\\image\\mkyong.jpg"));
//			int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB
//					: originalImage.getType();
//
//			BufferedImage resizeImageJpg = resizeImage(originalImage, type);
//			ImageIO.write(resizeImageJpg, "jpg", new File(
//					"c:\\image\\mkyong_jpg.jpg"));
//
//			BufferedImage resizeImagePng = resizeImage(originalImage, type);
//			ImageIO.write(resizeImagePng, "png", new File(
//					"c:\\image\\mkyong_png.jpg"));
//
//			BufferedImage resizeImageHintJpg = resizeImageWithHint(
//					originalImage, type);
//			ImageIO.write(resizeImageHintJpg, "jpg", new File(
//					"c:\\image\\mkyong_hint_jpg.jpg"));
//
//			BufferedImage resizeImageHintPng = resizeImageWithHint(
//					originalImage, type);
//			ImageIO.write(resizeImageHintPng, "png", new File(
//					"c:\\image\\mkyong_hint_png.jpg"));
//
//		} catch (IOException e) {
//			System.out.println(e.getMessage());
//		}
//
//	}

	public static BufferedImage resizeImage(BufferedImage originalImage, int height,int width) {
		
		int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB
				: originalImage.getType();
		BufferedImage resizedImage = new BufferedImage(width, height,
				type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, width, height, null);
		g.dispose();

		return resizedImage;
	}

	public static BufferedImage resizeImageWithHint(
			BufferedImage originalImage, int height,int width ) {
		int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB
				: originalImage.getType();
		BufferedImage resizedImage = new BufferedImage(width, height,
				type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, width, height, null);
		g.dispose();
		g.setComposite(AlphaComposite.Src);

		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		return resizedImage;
	}
}