import java.awt.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import javax.swing.*;

public class Laplace extends Thread {

	final static int N = 256;
	final static int CELL_SIZE = 2;
	final static int NITER = 100000;
	final static int OUTPUT_FREQ = 1000;
	static int iterGlobal = 0;
	static long currentLowest = 0;
	static int attemptGlobal = 0;

	static CyclicBarrier barrier;

	static float[][] phi;
	static float[][] newPhi;
	static int[][] colour;

	static Display display = new Display();

	public static void main(String args[]) throws Exception {
		// int P = 2;

		// Make voltage non-zero on left and right edges

		for (int threadModifier = 0; threadModifier < 4; threadModifier++) {
			int P = (int) Math.pow(2, threadModifier);
		//int P = 5;
			currentLowest = 0;

			for (int attempt = 0; attempt < 1; attempt++) {
				iterGlobal = 0;
				attemptGlobal = attempt;
				phi = new float[N][N];
				newPhi = new float[N][N];
				colour = new int[N][N];
				for (int j = 0; j < N; j++) {
					phi[0][j] = 1.0F;
					phi[N - 1][j] = 1.0F;
				}

				display.repaint();

				barrier = new CyclicBarrier(P, new Runnable() {
					public void run() {
						if (iterGlobal % OUTPUT_FREQ == 0) {
//							System.out.println("iter = " + iterGlobal);
							//display.repaint();
						}
					}
				});

				// Main update loop.
				Laplace[] openThreads = new Laplace[P];
				for (int threadNum = 0; threadNum < P; threadNum++) {
					Laplace thread = new Laplace(threadNum, P);
					openThreads[threadNum] = thread;
					thread.start();
				}
				for (int threadNum = 0; threadNum < openThreads.length; threadNum++) {
					openThreads[threadNum].join();
				}

			}
			System.out.printf("Lowest time for %d threads: %dms\n", P, currentLowest);
		}
	}

	int me;
	int p;

	public Laplace(int me, int p) {
		this.me = me;
		this.p = p;
	}

	public void run() {
		long startTime = System.currentTimeMillis();

		for (int iter = 0; iter < NITER; iter++) {

			int begin = (((N) / p) * me);
			int end = (((N) / p) * (me + 1));
			if (begin == 0)
				begin = 1;
			if (end == N)
				end = N - 1;

			// Calculate new phi

			for (int i = begin; i < end; i++) {
				for (int j = 1; j < N - 1; j++) {

					newPhi[i][j] = 0.25F * (phi[i][j - 1] + phi[i][j + 1] + phi[i - 1][j] + phi[i + 1][j]);
				}
			}

			// Update all phi values

			for (int i = begin; i < end; i++) {
				for (int j = 1; j < N - 1; j++) {
					phi[i][j] = newPhi[i][j];
					colour[i][j] = me;
				}
			}
			if (me == 0) {
				iterGlobal = iter;
				display.repaint();
			}

			try {
				barrier.await();
			} catch (InterruptedException ex) {
				return;
			} catch (BrokenBarrierException ex) {
				return;
			}

		}

		if (me == 0) {
			long endTime = System.currentTimeMillis();
			long time = (endTime - startTime);
			if(time < currentLowest || currentLowest == 0) currentLowest = time;
			System.out.println("Calculation completed in " + (endTime - startTime) + " milliseconds");
			display.repaint();
		}
	}

	@SuppressWarnings("serial")
	static class Display extends JPanel {

		final static int WINDOW_SIZE = N * CELL_SIZE;
		private JFrame frame;

		Display() {
			setPreferredSize(new Dimension(WINDOW_SIZE, WINDOW_SIZE));
			frame = new JFrame("Laplace");
			frame.setUndecorated(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setContentPane(this);
			frame.pack();
			frame.setVisible(true);
		}

		public void paintComponent(Graphics g) {
			frame.setTitle("Attempt " + attemptGlobal);
			for (int i = 0; i < N; i++) {
				for (int j = 0; j < N; j++) {
					float f = phi[i][j];
					Color c = new Color(f, 0.0F, 1.0F - f, 0.5f);
					for (int k = 0; k < colour[i][j]; k++) {
						c = c.darker();
					}
					g.setColor(c);
					g.fillRect(CELL_SIZE * i, CELL_SIZE * j, CELL_SIZE, CELL_SIZE);
				}
			}
		}
	}
}