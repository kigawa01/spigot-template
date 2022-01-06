import net.kigawa.util.Util;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class SpigotRunner {
    private static final String JAVA11 = "C:\\Program Files\\Java\\jdk-11.0.12\\bin\\java.exe";
    private static final String JAVA17 = "C:\\Program Files\\Java\\jdk-17\\bin\\java.exe";
    private static final String JAVA16 = "C:\\Program Files\\Java\\jdk-16.0.1\\bin\\java.exe";
    private static final String MAC_JAVA = "/usr/bin/java";
    private static final String JAVA = MAC_JAVA;
    private static final String MAVEN = "F:\\program\\apache-maven-3.8.4\\bin\\mvn.cmd";
    private static final String PLUGIN = "spigot-plugin";
    private static final String VERSION = "1.0dev";
    private static final String SPIGOT_VERSION = "1.16.5";
    private static SpigotRunner RUNNER;

    static {

    }

    private final File spigotDir;
    private final File pluginDir;
    private final File testDir;
    private final File spigot;
    private final File pluginsDir;
    private final File plugin;
    private final Thread thread;
    private final BufferedWriter processOut;
    private boolean isStop;

    public SpigotRunner() throws FileNotFoundException {
        File root = Paths.get("").toAbsolutePath().toFile();
        File src = new File(root, "src");

        testDir = new File(src, "test");
        testDir.mkdirs();
        spigotDir = new File(testDir, "spigot");
        spigotDir.mkdirs();
        pluginDir = new File(testDir, "plugin");
        pluginDir.mkdirs();
        pluginsDir = new File(spigotDir, "plugins");
        pluginsDir.mkdirs();
        plugin = new File(new File(root, "target"), PLUGIN + "-" + VERSION + ".jar");

        spigot = new File(spigotDir, "spigot-" + SPIGOT_VERSION + ".jar");
        if (!spigot.exists()) buildSpigot();
        if (!spigot.exists()) throw new FileNotFoundException();

        try {
            copyPlugins(root);
        }catch (Exception e){
            e.printStackTrace();
        }

        thread = runScanner();
        thread.start();
        Process process = runCommand(new String[]{
                JAVA, "-jar", "spigot-" + SPIGOT_VERSION + ".jar", "nogui"
        }, spigotDir);
        processOut = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        outLog(process);
        isStop = true;
    }

    public static void main(String[] args) {
        try {
            RUNNER = new SpigotRunner();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void copyPlugins(File root) {
        Util.runCommand(new String[]{
                MAVEN, "package", "-f", "pom.xml"
        }, root);

        File[] files = pluginsDir.listFiles();
        if (files != null)
            for (File plugin : files) {
                if (plugin.getName().endsWith(".jar"))
                    plugin.delete();
            }
        File target;
        files = pluginDir.listFiles();
        try {
            if (files != null)
                for (File plugin : files) {
                    target = new File(pluginsDir, plugin.getName());
                    Files.copy(plugin.toPath(), target.toPath());
                }
            target = new File(pluginsDir, this.plugin.getName());
            Files.copy(this.plugin.toPath(), target.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void outLog(Process process) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result;
            while (((result = br.readLine()) != null)) {
                System.out.println(result);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Process runCommand(String[] command, File dir) {
        Process process = null;
        Runtime runtime = Runtime.getRuntime();
        try {
            process = runtime.exec(command, null, dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return process;
    }

    private Thread runScanner() {
        return new Thread() {
            final Scanner scanner = new Scanner(System.in);

            @Override
            public void run() {
                if (isStop) return;
                String command = scanner.nextLine();
                try {
                    processOut.write(command);
                    processOut.newLine();
                    processOut.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (command.equalsIgnoreCase("stop")) {
                    try {
                        processOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                run();
            }
        };
    }

    private void buildSpigot() {
        try {
            URL url = new URL("https://hub.spigotmc.org/jenkins/job/BuildTools/lastStableBuild/artifact/target/BuildTools.jar");
            Util.download(url, spigotDir, "BuildTools.jar");
            Util.runCommand(new String[]{
                    JAVA, "-jar", "BuildTools.jar", "--rev", SPIGOT_VERSION
            }, spigotDir);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
