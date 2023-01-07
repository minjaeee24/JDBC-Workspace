import java.io.*;
import java.util.StringTokenizer;

public class Main {
	
    public static void main(String[] args) throws IOException {
    	
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
    	int num = Integer.parseInt(br.readLine());
    	StringTokenizer st;
    	int sum = 0;
    	for(int i = 0; i < num; i++) {
    		st = new StringTokenizer(br.readLine(), " ");
    		sum = Integer.parseInt(st.nextToken())+ Integer.parseInt(st.nextToken());
    		bw.write(sum);
    		bw.newLine();
    		bw.flush();
    	}
    }
    
}