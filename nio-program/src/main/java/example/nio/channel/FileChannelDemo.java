package example.nio.channel;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileChannelDemo {

    public static void main(String[] args) throws IOException {
        RandomAccessFile file = new RandomAccessFile("data.log", "rw");
        FileChannel inChannel = file.getChannel();

        ByteBuffer byteBuffer = ByteBuffer.allocate(48);

        int bytesRead = inChannel.read(byteBuffer);
        while (bytesRead != -1) {
            System.out.print("Read: " + bytesRead + " byte ---> ");
            byteBuffer.flip();
            while (byteBuffer.hasRemaining()) {
                System.out.print((char) byteBuffer.get());
            }
            System.out.println();
            byteBuffer.clear();
            bytesRead = inChannel.read(byteBuffer);
        }
        file.close();
    }
}
