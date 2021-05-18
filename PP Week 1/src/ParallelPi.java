import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class ParallelPi extends Thread {

	public static void main(String args[]) throws InterruptedException, FileNotFoundException {
		int stepBase = 1000000;
		
		PrintWriter out = new PrintWriter("C:\\Users\\Ollie\\Documents\\pibenchmark.csv");
		out.println("Threads,Steps,Milliseconds,Nanoseconds,Result");
		for (int threads = 1; threads < 9; threads++) {
			for (int stepExponent = 0; stepExponent < 5; stepExponent++) {
				for (int attempt = 0; attempt < 5; attempt++) {
					long numSteps = (long) (stepBase * Math.pow(10, stepExponent));
					System.out.format("%d threads, %d steps, attempt #%d%n", threads, numSteps, attempt + 1);
					long milliStart = System.currentTimeMillis();
					long nanoStart = System.nanoTime();

					double result = parallelPiMany(numSteps, threads);
					long nanoDuration = duration(nanoStart, true);
					long milliDuration = duration(milliStart, false);
					out.println(String.format("%d,%d,%d,%d," + Double.toString(result), threads, numSteps,
							milliDuration, nanoDuration));
					out.flush();
				}
			}
		}

		out.close();

	}

	public static long duration(long startingTime, boolean useNanos) {
		long d = (useNanos ? System.nanoTime() : System.currentTimeMillis()) - startingTime;
		System.out.println("Completed in " + d + (useNanos ? "ns" : "ms"));
		return d;
	}

	public static void sequentialPi(int numSteps) {
		double step = 1.0 / (double) numSteps;
		double sum = 0.0;

		for (int i = 0; i < numSteps; i++) {
			double x = (i + 0.5) * step;
			sum += 4.0 / (1.0 + x * x);
		}

		double pi = step * sum;

		System.out.println("Value of pi: " + pi);
	}

	private long begin, end;
	private double sum, step;

	public ParallelPi(long begin, long end, double step) {
		this.begin = begin;
		this.end = end;
		this.step = step;
		sum = 0.0;
	}

	public void run() {
		for (long i = begin; i < end; i++) {
			double x = (i + 0.5) * step;
			sum += 4.0 / (1.0 + x * x);
		}
	}

	public static double parallelPi(int numSteps) throws InterruptedException {

		double step = 1.0 / (double) numSteps;

		ParallelPi thread1 = new ParallelPi(0, numSteps / 2, step);
		ParallelPi thread2 = new ParallelPi(numSteps / 2, numSteps, step);

		thread1.start();
		thread2.start();

		thread1.join();
		thread2.join();

		double pi = thread1.step * (thread1.sum + thread2.sum);
		System.out.println("Value of pi: " + pi);
		return pi;
	}

	public static double parallelPiMany(long numSteps, int numThreads) throws InterruptedException {

		ParallelPi[] threads = new ParallelPi[numThreads];
		double step = 1.0 / (double) numSteps;
		double sum = 0;

		for (int i = 0; i < numThreads; i++) {
			long begin = (numSteps / numThreads) * i;
			long end = (numSteps / numThreads) * (i + 1);
			ParallelPi thread = new ParallelPi(begin, end, step);
			threads[i] = thread;
			thread.start();
		}

		for (ParallelPi thread : threads) {
			thread.join();
			sum += thread.sum;
		}

		double pi = step * sum;
		System.out.println("Value of pi: " + pi);
		return pi;
	}
}
