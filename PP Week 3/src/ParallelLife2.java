import java.awt.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import javax.swing.*;

public class ParallelLife2 extends Thread {

	final static int N = 128;
	final static int CELL_SIZE = 4;
	final static int DELAY = 0;

	static CyclicBarrier barrier;

	static int[][] state = new int[N][N];
	static int[][] colour = new int[N][N];

	static int[][] sums = new int[N][N];

	static Display display = new Display();

	public static void main(String args[]) throws Exception {
		int P = 8;

		// Define initial state of Life board

		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				state[i][j] = 0;
			}
		}
		addqbs(state);

		display.repaint();
		pause();

		barrier = new CyclicBarrier(P, new Runnable() {
			public void run() {

			}
		});

		// Main update loop.

		ParallelLife2[] openThreads = new ParallelLife2[P];
		for (int threadNum = 0; threadNum < P; threadNum++) {
			ParallelLife2 thread = new ParallelLife2(threadNum, P);
			openThreads[threadNum] = thread;
			thread.start();
		}
		for (int threadNum = 0; threadNum < openThreads.length; threadNum++) {
			openThreads[threadNum].join();
		}

	}

	int me;
	int p;

	public ParallelLife2(int me, int p) {
		this.me = me;
		this.p = p;
	}

	public void run() {
		int iter = 0;
		while (true) {
			if(iter == 398) {
				try {
					Thread.sleep(999999);
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			if (me == 0) {
				System.out.println("iter = " + iter++);
			}

			int begin = (N / p) * me;
			int end = (N / p) * (me + 1);

			// Calculate neighbour sums.

			for (int i = begin; i < end; i++) {
				for (int j = 0; j < N; j++) {

					// find neighbours...
					int ip = (i + 1) % N;
					int im = (i - 1 + N) % N;
					int jp = (j + 1) % N;
					int jm = (j - 1 + N) % N;

					sums[i][j] = state[im][jm] + state[im][j] + state[im][jp] + state[i][jm] + state[i][jp]
							+ state[ip][jm] + state[ip][j] + state[ip][jp];
				}
			}

			await();
			// Update state of board values.

			for (int i = begin; i < end; i++) {
				for (int j = 0; j < N; j++) {
					colour[i][j] = me;
					switch (sums[i][j]) {
					case 2:
						break;
					case 3:
						state[i][j] = 1;
						break;
					default:
						state[i][j] = 0;
						break;
					}
				}
			}

			await();
			if (me == 0) {
				display.repaint();
			}

			pause();

		}
	}

	public static void await() {
		try {
			barrier.await();
		} catch (InterruptedException ex) {
			return;
		} catch (BrokenBarrierException ex) {
			return;
		}
	}

	@SuppressWarnings("serial")
	static class Display extends JPanel {

		final static int WINDOW_SIZE = N * CELL_SIZE;

		public Color getColour(int id) {
			int level = 255;
			Color c;
			switch (id) {
			case 0:
				c = new Color(level, 0, 0);
				break;
			case 1:
				c = new Color(0, level, 0);
				break;
			case 2:
				c = new Color(0, 0, level);
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
			return c;
		}

		Display() {

			setPreferredSize(new Dimension(WINDOW_SIZE, WINDOW_SIZE));

			JFrame frame = new JFrame("Life");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setContentPane(this);
			frame.setUndecorated(true);
			frame.pack();
			frame.setVisible(true);
		}

		public void paintComponent(Graphics g) {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, WINDOW_SIZE, WINDOW_SIZE);
			g.setColor(Color.WHITE);
			for (int i = 0; i < N; i++) {
				for (int j = 0; j < N; j++) {
					if (state[i][j] == 1) {
						g.setColor(getColour(colour[i][j]));
						g.fillRect(CELL_SIZE * i, CELL_SIZE * j, CELL_SIZE, CELL_SIZE);
					}
				}
			}
		}
	}

	static void pause() {
		try {
			Thread.sleep(DELAY);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	static void addqbs(int[][] arr) {
		arr[25][1] = 1;

		arr[23][2] = 1;
		arr[25][2] = 1;

		arr[13][3] = 1;
		arr[14][3] = 1;
		arr[21][3] = 1;
		arr[22][3] = 1;
		arr[35][3] = 1;
		arr[36][3] = 1;

		arr[12][4] = 1;
		arr[16][4] = 1;
		arr[21][4] = 1;
		arr[22][4] = 1;
		arr[35][4] = 1;
		arr[36][4] = 1;

		arr[1][5] = 1;
		arr[2][5] = 1;
		arr[11][5] = 1;
		arr[17][5] = 1;
		arr[21][5] = 1;
		arr[22][5] = 1;

		arr[1][6] = 1;
		arr[2][6] = 1;
		arr[11][6] = 1;
		arr[15][6] = 1;
		arr[17][6] = 1;
		arr[18][6] = 1;
		arr[23][6] = 1;
		arr[25][6] = 1;

		arr[11][7] = 1;
		arr[17][7] = 1;
		arr[25][7] = 1;

		arr[12][8] = 1;
		arr[16][8] = 1;

		arr[13][9] = 1;
		arr[14][9] = 1;
	}
}