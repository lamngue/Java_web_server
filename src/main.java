public class main {

    public static void main(String[] args){
        WebServer webServer = new WebServer(4200, "public_html");
        webServer.runServer();

    }
}
