package ca.uwaterloo.cs.jgrok.io;

import java.io.*;
import java.nio.channels.FileChannel;

public class FileOutputAppendStream extends OutputStream {
    RandomAccessFile ra;
    
    public FileOutputAppendStream(String file)
        throws FileNotFoundException, IOException {
        ra = new RandomAccessFile(file, "rw");
        ra.seek(ra.length());
    }
    
    public void close() throws IOException {
        ra.close();
    }
    
    public FileChannel getChannel() {
        return ra.getChannel();
    }
    
    public FileDescriptor getFD() throws IOException {
        return ra.getFD();
    }
    
    public void write(byte[] b) throws IOException {
        ra.write(b);
    }
    
    public void write(byte[] b, int off, int len) throws IOException {
        ra.write(b, off, len);
    }
    
    public void write(int b) throws IOException {
        ra.write(b);
    }
}
