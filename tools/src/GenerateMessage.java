import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class GenerateMessage {
	public static void main(String[] args) throws IOException {
		Map<String, String> headers = new HashMap<String, String>();

		for(int i = 0; i < args.length; i += 2) {
			String key = args[i];
			String value = args[i + 1];
			headers.put(key, value);
		}

		PrintWriter output = new PrintWriter(System.out);
		for(Map.Entry<String, String> header : headers.entrySet()) {
			output.print(header.getKey() + ": " + header.getValue() + "\r\n");
		}

		output.print("\r\n"); //End of headers

		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		for(String line = input.readLine(); line != null; line = input.readLine()) {
			output.print(line + "\r\n");
		}

		output.close();
	}
}
