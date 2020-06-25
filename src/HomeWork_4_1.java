public class HomeWork_4_1 {
    private static final char[] BUF_LETTER = {'A', 'B', 'C'};
    private static final Object MONITOR = new Object();

    static class StreamABC implements Runnable {
        private char letter;
        private static int n = 0;

        public StreamABC(char letter) {
            this.letter = letter;
        }

        @Override
        public void run() {
            for (int i = 0; i < 5; i++) {
                synchronized (MONITOR) {
                    try {
                        while (letter != BUF_LETTER[n])
                            MONITOR.wait();
                        System.out.print(letter);
                        n++;
                        if (n > 2)
                            n = 0;
                        MONITOR.notifyAll();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        for (char c : BUF_LETTER) {
            new Thread(new StreamABC(c)).start();
        }
    }
}
