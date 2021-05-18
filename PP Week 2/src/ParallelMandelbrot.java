import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import java.io.File;
import java.io.IOException;

public class ParallelMandelbrot extends Thread {

	final static int N = 4096;
	final static int CUTOFF = 100;
	final static String PATH = "./MandelbrotP.png";
	static long lowest = Long.MAX_VALUE;

	static int[][] set = new int[N][N];
	static int[][] colour = new int[N][N];

	public static void main(String[] args) throws Exception {

		// Calculate set
		for (int i = 0; i < 1; i++) {
			long startTime = System.currentTimeMillis();
			compute(4, 2);
			long endTime = System.currentTimeMillis();
			long time = endTime - startTime;
			System.out.println("Calculation completed in " + time + " milliseconds");
			if (time < lowest)
				lowest = time;
		}
		System.out.println(lowest);
		drawImage(N, PATH, set, colour);
	}

	public static int getColour(int id, float level) {
		Color c;
		switch (id) {
		case 0:
			c = new Color(0, 0, level);
			break;
		case 1:
			c = new Color(0, level, 0);
			break;
		case 2:
			c = new Color(level, 0, 0);
			break;
		case 3:
			c = new Color(level, level, 0);
			break;
		case 4:
			c = new Color(0, level, level);
			break;
		case 5:
			c = new Color(level, 0, level);
			break;
		default:
			c = new Color(level, level, level);
			break;
		}
		return c.getRGB();
	}

	public static void drawImage(int size, String file, int[][] set, int[][] colour) throws IOException {
		// Plot image
		BufferedImage img = new BufferedImage(N, N, BufferedImage.TYPE_INT_ARGB);

		// Draw pixels
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {

				int k = set[i][j];
				int col = colour[i][j];

				float level;
				if (k < CUTOFF) {
					level = (float) k / CUTOFF;
				} else {
					level = 0;
				}
				img.setRGB(i, j, getColour(col, level));
			}
		}

		// Print file

		ImageIO.write(img, "PNG", new File(file));
	}

	public static void compute(int maxThreads, int decompositionType) throws InterruptedException {
		ParallelMandelbrot[] openThreads = new ParallelMandelbrot[maxThreads];
		for (int threadNum = 0; threadNum < maxThreads; threadNum++) {
			ParallelMandelbrot thread = new ParallelMandelbrot(threadNum, maxThreads, decompositionType);
			openThreads[threadNum] = thread;
			thread.start();
		}
		for (int threadNum = 0; threadNum < openThreads.length; threadNum++) {
			openThreads[threadNum].join();
		}
	}

	int me;
	int maxThreads;
	int decompositionType;

	public ParallelMandelbrot(int me, int maxThreads, int decompositionType) {
		this.me = me;
		this.maxThreads = maxThreads;
		this.decompositionType = decompositionType;
	}

	public void mandelbrotAlgorithm(int i, int j) {
		double cr = (4.0 * i - 2 * N) / N;
		double ci = (4.0 * j - 2 * N) / N;

		double zr = cr, zi = ci;

		int k = 0;
		while (k < CUTOFF && zr * zr + zi * zi < 4.0) {
			double newr = cr + zr * zr - zi * zi;
			double newi = ci + 2 * zr * zi;
			zr = newr;
			zi = newi;
			k++;
		}
		set[i][j] = k;
		colour[i][j] = me;
	}

	public void horizontalDivide() {
		int begin = (N / maxThreads) * me;
		int end = (N / maxThreads) * (me + 1);
		for (int i = 0; i < N; i++) {
			for (int j = begin; j < end; j++) {
				mandelbrotAlgorithm(i, j);
			}
		}
	}

	public void verticalDivide() {
		int begin = (N / maxThreads) * me;
		int end = (N / maxThreads) * (me + 1);
		for (int i = begin; i < end; i++) {
			for (int j = 0; j < N; j++) {
				mandelbrotAlgorithm(i, j);
			}
		}
	}

	public void boxDivide() {
		int meI = me % (maxThreads / 2);
		int meJ = me / (maxThreads / 2);
		int section = (N / (maxThreads / 2));
		// System.out.printf("me: %d, i: %d, j: %d, startI: %d, endI: %d, startJ: %d,
		// endJ: %d\n", me, meI, meJ,
		// section * meI, section * (meI + 1), section * meJ, section * (meJ + 1));
		for (int i = section * meI; i < section * (meI + 1); i++) {
			for (int j = section * meJ; j < section * (meJ + 1); j++) {
				mandelbrotAlgorithm(i, j);
			}
		}
	}

	public void alternatingColumns() {
		for (int i1 = 0; i1 < N / maxThreads; i1++) {
			for (int j = 0; j < N; j++) {
				int i = (i1 * maxThreads) + me;
				mandelbrotAlgorithm(i, j);
			}
		}
	}

	public void alternatingRows() {
		for (int i = 0; i < N; i++) {
			for (int j1 = 0; j1 < N / 4; j1++) {
				int j = (j1 * 4) + me;
				mandelbrotAlgorithm(i, j);
			}
		}
	}

	public void run() {
		switch (decompositionType) {
		case 0: {
			horizontalDivide();
			break;
		}
		case 1: {
			verticalDivide();
			break;
		}
		case 2: {
			boxDivide();
			break;
		}
		case 3: {
			alternatingColumns();
			break;
		}
		case 4: {
			alternatingRows();
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + decompositionType);
		}
	}

}