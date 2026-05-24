public class Main {

    public static void main(String[] args) {

        String folderPath = "/home/eneo/mysoftwarefiles/mytestfiles/Youtube";

        FileScanner scanner = new FileScanner();
        AIService aiService = new AIService();

        scanner.scan(folderPath, filename -> {

            System.out.println("Processing: " + filename);

            String result = aiService.extractMetadata(filename);

            System.out.println(result);
            System.out.println("-------------------");
        });
    }
}