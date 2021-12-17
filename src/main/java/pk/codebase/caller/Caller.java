package pk.codebase.caller;

import io.crossbar.autobahn.wamp.Client;
import io.crossbar.autobahn.wamp.Session;
import io.crossbar.autobahn.wamp.types.CallResult;
import io.crossbar.autobahn.wamp.types.ExitInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class Caller {
    public static void main(String[] args) {
        connect();
    }

    private static int connect() {
        Session wampSession = new Session(Executors.newFixedThreadPool(1));
        double start = System.currentTimeMillis();
        wampSession.addOnJoinListener((session, details) -> {
            System.out.println("called");
            List<CompletableFuture<CallResult>> aList = new ArrayList<>();
            for (int i = 0; i < 10000; i++) {
                aList.add(session.call("simplethings", "arg1"));
            }

            CompletableFuture<CallResult>[] array = aList.toArray(new CompletableFuture[0]);

            CompletableFuture.allOf(array).whenComplete((unused, throwable) -> {
                double finish = System.currentTimeMillis();
                double timeElapsed = finish - start;
                System.out.println(timeElapsed / 1000);
            });
        });
        Client client = new Client(wampSession, "ws://localhost:8080/ws", "realm1");
        CompletableFuture<ExitInfo> exitFuture = client.connect();

        try {
            ExitInfo exitInfo = exitFuture.get();
            return exitInfo.code;
        } catch (Exception e) {
            return 1;
        }
    }
}

