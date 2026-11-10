package com.ande.pubquizzz.security;

import com.ande.pubquizzz.PubQuizzzApplication;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRememberMeProcessRestartTest {

    private static final String TEST_KEY = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

    @Test
    void userCookieSurvivesActualProcessRestart() throws Exception {
        Path directory = Files.createTempDirectory(Path.of("target"), "remember-process-").toAbsolutePath();
        Process process = null;
        try {
            int port = availablePort();
            URI base = URI.create("http://127.0.0.1:" + port);
            CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
            HttpClient client = HttpClient.newBuilder().cookieHandler(cookies)
                    .connectTimeout(Duration.ofSeconds(2)).build();
            HttpClient probe = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

            process = startApplication(directory, port);
            awaitLoginPage(probe, base, process, directory);
            assertEquals(200, client.send(HttpRequest.newBuilder(base.resolve("/login")).GET().build(),
                    HttpResponse.BodyHandlers.discarding()).statusCode());
            String csrf = cookie(cookies, "XSRF-TOKEN").getValue();
            String body = "username=restart-user&password=restart-password&_csrf="
                    + URLEncoder.encode(csrf, StandardCharsets.UTF_8);
            HttpRequest login = HttpRequest.newBuilder(base.resolve("/login"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build();
            assertEquals(302, client.send(login, HttpResponse.BodyHandlers.discarding()).statusCode());
            assertTrue(cookie(cookies, "remember-me").getMaxAge() > 0);

            stop(process);
            process = null;
            process = startApplication(directory, port);
            awaitLoginPage(probe, base, process, directory);

            HttpResponse<String> restored = client.send(
                    HttpRequest.newBuilder(base.resolve("/api/bootstrap")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, restored.statusCode());
            assertTrue(restored.body().contains("\"isAdmin\":false"));
        } finally {
            stop(process);
            try (Stream<Path> paths = Files.walk(directory)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.deleteIfExists(path);
                }
            }
        }
    }

    private static int availablePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static Process startApplication(Path directory, int port) throws IOException {
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        String classpath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        String database = directory.resolve("database").toString().replace('\\', '/');
        ProcessBuilder builder = new ProcessBuilder(java, "-cp", classpath, PubQuizzzApplication.class.getName(),
                "--spring.profiles.active=test",
                "--server.address=127.0.0.1",
                "--server.port=" + port,
                "--spring.datasource.url=jdbc:h2:file:" + database + ";DB_CLOSE_ON_EXIT=FALSE",
                "--spring.jpa.hibernate.ddl-auto=update",
                "--spring.devtools.restart.enabled=false",
                "--logging.config=classpath:remember-restart-logback.xml",
                "--app.security.remember-me.key=" + TEST_KEY,
                "--app.upload.dir=" + directory,
                "--app.backup.restore-dir=" + directory.resolve("pending-restore"),
                "--ADMIN_USER=restart-admin",
                "--ADMIN_PASSWORD=restart-admin-password",
                "--DEFAULT_USER=restart-user",
                "--DEFAULT_PASSWORD=restart-password");
        builder.redirectErrorStream(true);
        builder.redirectOutput(directory.resolve("application.log").toFile());
        return builder.start();
    }

    private static void awaitLoginPage(HttpClient client, URI base, Process process, Path directory) throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(45).toNanos();
        while (System.nanoTime() < deadline && process.isAlive()) {
            try {
                HttpRequest request = HttpRequest.newBuilder(base.resolve("/login"))
                        .timeout(Duration.ofSeconds(2)).GET().build();
                if (client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode() == 200) {
                    return;
                }
            } catch (IOException ignored) {
                // The server is not listening yet.
            }
            Thread.sleep(100);
        }
        String log = Files.readString(directory.resolve("application.log"));
        throw new AssertionError("Application did not start in time. Test process output:\n" + log);
    }

    private static HttpCookie cookie(CookieManager cookies, String name) {
        return cookies.getCookieStore().getCookies().stream()
                .filter(cookie -> name.equals(cookie.getName()))
                .findFirst().orElseThrow(() -> new AssertionError("Missing cookie: " + name));
    }

    private static void stop(Process process) throws InterruptedException {
        if (process != null && process.isAlive()) {
            process.destroy();
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                process.waitFor(10, TimeUnit.SECONDS);
            }
        }
    }
}
