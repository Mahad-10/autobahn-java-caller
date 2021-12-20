package pk.codebase.caller;

import io.crossbar.autobahn.wamp.Client;
import io.crossbar.autobahn.wamp.Session;
import io.crossbar.autobahn.wamp.serializers.CBORSerializer;
import io.crossbar.autobahn.wamp.transports.NettyWebSocket;
import io.crossbar.autobahn.wamp.types.CallResult;
import io.crossbar.autobahn.wamp.types.ExitInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class Caller {
    public static void main(String[] args) {
        String url = "ws://localhost:8080/ws";
        String procedure = "com.things.echo";
        Double num_of_requests = Double.valueOf(10000);
        try {
            url = args[0];
            procedure = args[1];
            num_of_requests = Double.valueOf(args[2]);
        } catch(Exception e){
        }
        connect(url, procedure, num_of_requests);
    }

    private static int connect(String url, String procedure, Double requests) {
        Session wampSession = new Session(Executors.newFixedThreadPool(1));
        double start = System.currentTimeMillis();
        wampSession.addOnJoinListener((session, details) -> {
            System.out.println("called");
            List<CompletableFuture<CallResult>> aList = new ArrayList<>();
            for (int i = 0; i < requests; i++) {
                aList.add(session.call(procedure, "arg1"));
            }

            CompletableFuture<CallResult>[] array = aList.toArray(new CompletableFuture[0]);

            CompletableFuture.allOf(array).whenComplete((unused, throwable) -> {
                double finish = System.currentTimeMillis();
                double timeElapsed = finish - start;
                System.out.println(timeElapsed / 1000);
            });
        });

        List<String> serializers = new ArrayList<>();
        serializers.add(CBORSerializer.NAME);
        NettyWebSocket webSocket = new NettyWebSocket(url, serializers);

        Client client = new Client(webSocket);
        client.add(wampSession, "realm1");
        CompletableFuture<ExitInfo> exitFuture = client.connect();

        try {
            ExitInfo exitInfo = exitFuture.get();
            return exitInfo.code;
        } catch (Exception e) {
            return 1;
        }
    }
}

